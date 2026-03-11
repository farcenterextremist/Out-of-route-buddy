package com.example.outofroutebuddy.services

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.util.FormatUtils
import com.example.outofroutebuddy.util.RateLimiter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlin.math.*
import java.util.*

/**
 * ✅ UNIFIED: Location Service - Merged from multiple location services
 * 
 * This service combines the functionality of:
 * - LocationValidationService (GPS validation and quality assessment)
 * - GpsSynchronizationService (GPS data processing)
 * - LocationServiceCoordinator (coordination)
 * - LocationCache (performance optimization)
 * 
 * Benefits:
 * - Single point of entry for all location operations
 * - Simplified dependency injection
 * - Reduced code duplication
 * - Easier to maintain and test
 */
open class UnifiedLocationService(
    private val context: Context,
    private val tripStateManager: TripStateManager
) {
    
    private val logger = LoggerFactory.getLogger(UnifiedLocationService::class.java)
    
    companion object {
        private const val TAG = "UnifiedLocationService"
        
        // ✅ CENTRALIZED: Using ValidationConfig for all constants
        val DEFAULT_MAX_LOCATION_AGE = ValidationConfig.MAX_LOCATION_AGE
        val DEFAULT_MAX_ACCURACY = ValidationConfig.MAX_ACCURACY
        val DEFAULT_MAX_SPEED_CHANGE = ValidationConfig.MAX_SPEED_CHANGE
        val DEFAULT_MIN_DISTANCE_THRESHOLD = ValidationConfig.MIN_DISTANCE_THRESHOLD
        val DEFAULT_MAX_STATIONARY_TIME = ValidationConfig.MAX_STATIONARY_TIME
        val DEFAULT_MAX_DISTANCE_BETWEEN_UPDATES = ValidationConfig.MAX_DISTANCE_BETWEEN_UPDATES
        
        // Vehicle-specific GPS quality thresholds
        val VEHICLE_MIN_ACCURACY = ValidationConfig.VEHICLE_MIN_ACCURACY
        val VEHICLE_MAX_SPEED_MPH = ValidationConfig.VEHICLE_MAX_SPEED_MPH
        val VEHICLE_MIN_SPEED_MPH = ValidationConfig.VEHICLE_MIN_SPEED_MPH
        val VEHICLE_MAX_ACCELERATION_MPH_PER_SEC = ValidationConfig.VEHICLE_MAX_ACCELERATION_MPH_PER_SEC
        
        // Conversion constants
        val MPS_TO_MPH = ValidationConfig.MPS_TO_MPH
        val MPH_TO_MPS = ValidationConfig.MPH_TO_MPS
        val EARTH_RADIUS_METERS = ValidationConfig.EARTH_RADIUS_METERS
    }
    
    // ✅ ROBUSTNESS: Managed coroutine scope for proper cancellation
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tripStateMonitorJob: Job? = null
    
    // ✅ UNIFIED: State flows
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()
    
    private val _gpsData = MutableStateFlow(GpsData())
    val gpsData: StateFlow<GpsData> = _gpsData.asStateFlow()
    
    private val _realTimeGpsData = MutableStateFlow(RealTimeGpsData())
    val realTimeGpsData: StateFlow<RealTimeGpsData> = _realTimeGpsData.asStateFlow()
    
    // ✅ UNIFIED: Cache for performance
    private val locationCache = mutableMapOf<String, CachedLocation>()
    private val maxCacheSize = 100
    // ✅ NEW (#13): Mutex for thread-safe cache access
    private val cacheMutex = Mutex()
    
    // ✅ NEW (#6): GPS Circuit Breaker for failure prevention
    private val gpsCircuitBreaker = GpsCircuitBreaker()
    
    // ✅ NEW (#10): Rate Limiter for GPS updates (10 updates per second max)
    private val gpsRateLimiter = RateLimiter(
        maxRequests = 10,
        timeWindowMs = 1000L
    )
    
    // ✅ UNIFIED: Data classes
    data class LocationState(
        val isTracking: Boolean = false,
        val lastLocation: Location? = null,
        val totalDistance: Double = 0.0,
        val currentSpeed: Float = 0f,
        val accuracy: Float = 0f,
        val satelliteCount: Int = 0,
        val signalStrength: Int = 0,
        val lastUpdate: Long = 0L,
        val validationIssues: List<ValidationIssue> = emptyList()
    )
    
    data class GpsData(
        val totalDistance: Double = 0.0,
        val accuracy: Double = 0.0,
        val speed: Float = 0f,
        val bearing: Float = 0f,
        val altitude: Double = 0.0,
        val timestamp: Long = 0L,
        val quality: GpsQuality = GpsQuality.UNKNOWN
    )
    
    data class RealTimeGpsData(
        val totalDistance: Double = 0.0,
        val accuracy: Double = 0.0,
        val currentSpeed: Float = 0f,
        val averageSpeed: Float = 0f,
        val maxSpeed: Float = 0f,
        val tripDuration: Long = 0L,
        val locationCount: Int = 0,
        val validLocationCount: Int = 0,
        val lastUpdate: Long = 0L
    )
    
    data class CachedLocation(
        val location: Location,
        val validationResult: ValidationResult,
        val timestamp: Long
    )
    
    data class ValidationIssue(
        val type: ValidationIssueType,
        val message: String,
        val severity: ValidationSeverity
    )
    
    enum class ValidationIssueType {
        ACCURACY_TOO_LOW,
        SPEED_TOO_HIGH,
        LOCATION_TOO_OLD,
        DISTANCE_TOO_LARGE,
        STATIONARY_TOO_LONG
    }
    
    enum class ValidationSeverity {
        WARNING, ERROR, CRITICAL
    }
    
    enum class GpsQuality {
        UNKNOWN, POOR, FAIR, GOOD, EXCELLENT
    }
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String, val severity: ValidationSeverity) : ValidationResult()
    }
    
    init {
        initializeLocationService()
    }
    
    /**
     * ✅ UNIFIED: Initialize location service
     */
    private fun initializeLocationService() {
        try {
            Log.d(TAG, "Initializing unified location service")
            
            // Start monitoring trip state with managed scope
            tripStateMonitorJob = serviceScope.launch {
                tripStateManager.tripState.collect { tripState ->
                    if (isActive) { // ✅ ROBUSTNESS: Check for cancellation
                        if (tripState.isActive) {
                            startLocationTracking()
                        } else {
                            stopLocationTracking()
                        }
                    }
                }
            }
            
            Log.d(TAG, "Unified location service initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize location service", e)
        }
    }
    
    /**
     * ✅ ROBUSTNESS: Clean up resources when service is no longer needed
     */
    fun cleanup() {
        try {
            tripStateMonitorJob?.cancel()
            serviceScope.cancel()
            Log.d(TAG, "Unified location service cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Start location tracking
     */
    private fun startLocationTracking() {
        try {
            _locationState.value = _locationState.value.copy(isTracking = true)
            Log.d(TAG, "Location tracking started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location tracking", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Stop location tracking
     */
    private fun stopLocationTracking() {
        try {
            _locationState.value = _locationState.value.copy(isTracking = false)
            Log.d(TAG, "Location tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location tracking", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Process location update
     * ✅ NEW (#13): Now suspends for thread-safe caching
     * ✅ NEW (#6): Circuit breaker protection
     * ✅ NEW (#10): Rate limiting protection
     */
    suspend fun processLocationUpdate(location: Location): ValidationResult {
        return try {
            // ✅ NEW (#10): Check rate limiter first
            if (!gpsRateLimiter.acquire()) {
                Log.d(TAG, "GPS update rate limited (max 10/second)")
                return ValidationResult.Invalid("Rate limited", ValidationSeverity.WARNING)
            }
            
            // ✅ NEW (#6): Check circuit breaker before processing
            if (!gpsCircuitBreaker.canAttempt()) {
                Log.w(TAG, "GPS circuit breaker OPEN - skipping location update")
                return ValidationResult.Invalid("GPS circuit breaker open", ValidationSeverity.WARNING)
            }
            
            Log.d(TAG, "Processing location update (coordinates redacted per SECURITY_NOTES)")
            
            // Validate location
            val validationResult = validateLocation(location)
            
            // ✅ NEW (#6): Record success/failure with circuit breaker
            when (validationResult) {
                is ValidationResult.Valid -> {
                    gpsCircuitBreaker.recordSuccess()
                }
                is ValidationResult.Invalid -> {
                    if (validationResult.severity == ValidationSeverity.CRITICAL) {
                        gpsCircuitBreaker.recordFailure("Critical validation failure: ${validationResult.reason}")
                    }
                }
            }
            
            // Cache location for performance (thread-safe)
            cacheLocation(location, validationResult)
            
            // Update state if valid
            if (validationResult is ValidationResult.Valid) {
                updateLocationState(location)
                updateGpsData(location)
                updateRealTimeGpsData(location)
            }
            
            validationResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process location update", e)
            // ✅ NEW (#6): Record exception as failure
            gpsCircuitBreaker.recordFailure("Exception: ${e.message}")
            ValidationResult.Invalid("Processing error: ${e.message}", ValidationSeverity.ERROR)
        }
    }
    
    /**
     * ✅ UNIFIED: Validate location
     */
    private fun validateLocation(location: Location): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        
        // Check location age
        val locationAge = System.currentTimeMillis() - location.time
        if (locationAge > DEFAULT_MAX_LOCATION_AGE) {
            issues.add(ValidationIssue(
                ValidationIssueType.LOCATION_TOO_OLD,
                "Location is ${locationAge}ms old (max: ${DEFAULT_MAX_LOCATION_AGE}ms)",
                ValidationSeverity.ERROR
            ))
        }
        
        // Check accuracy
        if (location.hasAccuracy() && location.accuracy > DEFAULT_MAX_ACCURACY) {
            issues.add(ValidationIssue(
                ValidationIssueType.ACCURACY_TOO_LOW,
                "Accuracy ${location.accuracy}m is too low (max: ${DEFAULT_MAX_ACCURACY}m)",
                ValidationSeverity.WARNING
            ))
        }
        
        // Check speed
        if (location.hasSpeed()) {
            val speedMph = location.speed * MPS_TO_MPH
            if (speedMph > VEHICLE_MAX_SPEED_MPH) {
                issues.add(ValidationIssue(
                    ValidationIssueType.SPEED_TOO_HIGH,
                    "Speed ${String.format("%.1f", speedMph)}mph is too high (max: ${VEHICLE_MAX_SPEED_MPH}mph)",
                    ValidationSeverity.ERROR
                ))
            }
        }
        
        // Check distance from last location
        val lastLocation = _locationState.value.lastLocation
        if (lastLocation != null) {
            val distance = location.distanceTo(lastLocation)
            if (distance > DEFAULT_MAX_DISTANCE_BETWEEN_UPDATES) {
                issues.add(ValidationIssue(
                    ValidationIssueType.DISTANCE_TOO_LARGE,
                    "Distance ${String.format("%.1f", distance)}m is too large (max: ${DEFAULT_MAX_DISTANCE_BETWEEN_UPDATES}m)",
                    ValidationSeverity.WARNING
                ))
            }
        }
        
        return if (issues.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                issues.joinToString("; ") { it.message },
                issues.maxByOrNull { it.severity }?.severity ?: ValidationSeverity.WARNING
            )
        }
    }
    
    /**
     * ✅ UNIFIED: Cache location for performance
     * ✅ NEW (#13): Thread-safe with Mutex
     */
    private suspend fun cacheLocation(location: Location, validationResult: ValidationResult) {
        val cacheKey = "${location.latitude}_${location.longitude}_${location.time}"
        
        // ✅ Thread-safe cache access
        cacheMutex.withLock {
            // Add to cache
            locationCache[cacheKey] = CachedLocation(location, validationResult, System.currentTimeMillis())
            
            // Maintain cache size
            if (locationCache.size > maxCacheSize) {
                val oldestKey = locationCache.minByOrNull { it.value.timestamp }?.key
                oldestKey?.let { locationCache.remove(it) }
            }
        }
    }
    
    /**
     * ✅ UNIFIED: Update location state
     */
    private fun updateLocationState(location: Location) {
        val currentState = _locationState.value
        val newDistance = currentState.totalDistance + calculateDistanceIncrement(location)
        
        _locationState.value = currentState.copy(
            lastLocation = location,
            totalDistance = newDistance,
            currentSpeed = location.speed,
            accuracy = location.accuracy,
            lastUpdate = System.currentTimeMillis(),
            validationIssues = emptyList() // Clear issues for valid location
        )
    }
    
    /**
     * ✅ UNIFIED: Update GPS data
     */
    private fun updateGpsData(location: Location) {
        val quality = calculateGpsQuality(location)
        
        _gpsData.value = GpsData(
            totalDistance = _locationState.value.totalDistance,
            accuracy = location.accuracy.toDouble(),
            speed = location.speed,
            bearing = location.bearing,
            altitude = location.altitude,
            timestamp = location.time,
            quality = quality
        )
    }
    
    /**
     * ✅ UNIFIED: Update real-time GPS data
     */
    private fun updateRealTimeGpsData(location: Location) {
        val currentData = _realTimeGpsData.value
        val newLocationCount = currentData.locationCount + 1
        val newValidLocationCount = currentData.validLocationCount + 1
        
        // Calculate average speed
        val newAverageSpeed = if (newValidLocationCount > 1) {
            (currentData.averageSpeed * (newValidLocationCount - 1) + location.speed) / newValidLocationCount
        } else {
            location.speed
        }
        
        // Update max speed
        val newMaxSpeed = maxOf(currentData.maxSpeed, location.speed)
        
        _realTimeGpsData.value = RealTimeGpsData(
            totalDistance = _locationState.value.totalDistance,
            accuracy = location.accuracy.toDouble(),
            currentSpeed = location.speed,
            averageSpeed = newAverageSpeed,
            maxSpeed = newMaxSpeed,
            tripDuration = System.currentTimeMillis() - (tripStateManager.tripState.value.startTime?.time ?: System.currentTimeMillis()),
            locationCount = newLocationCount,
            validLocationCount = newValidLocationCount,
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    /**
     * ✅ CRITICAL FIX: Calculate distance increment with proper unit conversion
     * 
     * 🐛 BUG FIXED: This was the root cause of "total miles" not displaying correctly
     * - BEFORE: location.distanceTo(lastLocation) / 1000.0  (converted to KILOMETERS)
     * - AFTER:  location.distanceTo(lastLocation) / 1609.34 (converts to MILES)
     * 
     * 📊 IMPACT: Android Location.distanceTo() returns meters, but UI displays miles
     * - 1 mile = 1609.34 meters (exact conversion factor)
     * - 1 km = 1000 meters (what we were incorrectly using)
     * 
     * 🔧 WHY THIS WORKS:
     * 1. Android GPS returns distances in meters
     * 2. US users expect miles in the UI
     * 3. This conversion feeds into TripMetrics.totalMiles
     * 4. TripInputViewModel displays this as "Total Miles" in real-time
     * 
     * ✅ VERIFIED: Real-world testing confirmed miles now display correctly
     */
    private fun calculateDistanceIncrement(location: Location): Double {
        val lastLocation = _locationState.value.lastLocation
        return if (lastLocation != null) {
            location.distanceTo(lastLocation) / 1609.34 // ✅ Convert meters to MILES (not km!)
        } else {
            0.0
        }
    }
    
    /**
     * ✅ UNIFIED: Calculate GPS quality
     */
    private fun calculateGpsQuality(location: Location): GpsQuality {
        return when {
            location.accuracy <= 5f -> GpsQuality.EXCELLENT
            location.accuracy <= 10f -> GpsQuality.GOOD
            location.accuracy <= 20f -> GpsQuality.FAIR
            location.accuracy <= 50f -> GpsQuality.POOR
            else -> GpsQuality.UNKNOWN
        }
    }
    
    /**
     * ✅ UNIFIED: Get current location
     */
    fun getCurrentLocation(): Location? {
        return _locationState.value.lastLocation
    }
    
    /**
     * ✅ UNIFIED: Get total distance
     */
    fun getTotalDistance(): Double {
        return _locationState.value.totalDistance
    }
    
    /**
     * ✅ UNIFIED: Get current speed
     */
    fun getCurrentSpeed(): Float {
        return _locationState.value.currentSpeed
    }
    
    /**
     * ✅ UNIFIED: Get GPS accuracy
     */
    fun getGpsAccuracy(): Float {
        return _locationState.value.accuracy
    }
    
    /**
     * ✅ UNIFIED: Check if location tracking is active
     */
    fun isLocationTrackingActive(): Boolean {
        return _locationState.value.isTracking
    }
    
    /**
     * ✅ UNIFIED: Get location statistics
     */
    fun getLocationStatistics(): LocationStatistics {
        val currentData = _realTimeGpsData.value
        val currentState = _locationState.value
        
        return LocationStatistics(
            totalDistance = currentState.totalDistance,
            averageSpeed = currentData.averageSpeed,
            maxSpeed = currentData.maxSpeed,
            currentSpeed = currentState.currentSpeed,
            accuracy = currentState.accuracy,
            locationCount = currentData.locationCount,
            validLocationCount = currentData.validLocationCount,
            tripDuration = currentData.tripDuration,
            isTracking = currentState.isTracking
        )
    }
    
    /**
     * ✅ UNIFIED: Clear location data
     * ✅ NEW (#13): Thread-safe with Mutex
     */
    suspend fun clearLocationData() {
        try {
            _locationState.value = LocationState()
            _gpsData.value = GpsData()
            _realTimeGpsData.value = RealTimeGpsData()
            
            // ✅ Thread-safe cache clear
            cacheMutex.withLock {
                locationCache.clear()
            }
            
            // ✅ NEW (#6): Reset circuit breaker when clearing data
            gpsCircuitBreaker.reset()
            
            Log.d(TAG, "Location data cleared and circuit breaker reset")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear location data", e)
        }
    }
    
    /**
     * ✅ NEW (#6): Get circuit breaker status
     */
    suspend fun getCircuitBreakerStatus(): GpsCircuitBreaker.CircuitBreakerState {
        return gpsCircuitBreaker.getState()
    }
    
    /**
     * ✅ NEW (#6): Get GPS health statistics
     */
    suspend fun getGpsHealthStatistics(): GpsCircuitBreaker.CircuitBreakerStatistics {
        return gpsCircuitBreaker.getStatistics()
    }
    
    /**
     * ✅ NEW (#10): Get rate limiter statistics
     */
    suspend fun getRateLimiterStatistics(): RateLimiter.RateLimiterStatistics {
        return gpsRateLimiter.getStatistics()
    }
    
    // ✅ DATA CLASSES
    data class LocationStatistics(
        val totalDistance: Double,
        val averageSpeed: Float,
        val maxSpeed: Float,
        val currentSpeed: Float,
        val accuracy: Float,
        val locationCount: Int,
        val validLocationCount: Int,
        val tripDuration: Long,
        val isTracking: Boolean
    )
} 
