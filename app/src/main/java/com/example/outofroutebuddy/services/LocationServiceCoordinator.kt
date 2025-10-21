package com.example.outofroutebuddy.services

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.outofroutebuddy.services.location.LocationService
import com.example.outofroutebuddy.services.location.LocationProviderStatus
import com.example.outofroutebuddy.services.location.LocationServiceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * ✅ NEW: Location Service Coordinator
 * 
 * This coordinator manages all location-related services and provides a unified
 * interface for location operations, GPS tracking, and location validation.
 */
class LocationServiceCoordinator(
    private val context: Context,
    private val locationService: LocationService,
    private val locationValidationService: LocationValidationService,
    private val gpsSynchronizationService: GpsSynchronizationService
) {
    companion object {
        private const val TAG = "LocationServiceCoordinator"
    }
    
    // ✅ COORDINATOR STATE: Track location coordination status
    private val _coordinatorState = MutableStateFlow(LocationCoordinatorState())
    val coordinatorState: StateFlow<LocationCoordinatorState> = _coordinatorState.asStateFlow()
    
    // ✅ LOCATION STATE: Track current location status
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()
    
    // ✅ GPS STATE: Track GPS-specific status
    private val _gpsState = MutableStateFlow(GpsState())
    val gpsState: StateFlow<GpsState> = _gpsState.asStateFlow()
    
    /**
     * ✅ NEW: Location coordinator state data class
     */
    data class LocationCoordinatorState(
        val isCoordinating: Boolean = false,
        val lastAction: String = "None",
        val lastActionTime: Date = Date(),
        val errorCount: Int = 0,
        val successCount: Int = 0,
        val isHealthy: Boolean = true,
        val servicesActive: List<String> = emptyList()
    )
    
    /**
     * ✅ NEW: Location state data class
     */
    data class LocationState(
        val currentLocation: Location? = null,
        val locationAccuracy: Float = 0f,
        val locationSpeed: Float = 0f,
        val lastUpdateTime: Date = Date(),
        val isLocationServiceActive: Boolean = false,
        val hasLocationPermission: Boolean = false,
        val locationErrorCount: Int = 0,
        val providerStatus: LocationProviderStatus = LocationProviderStatus()
    )
    
    /**
     * ✅ NEW: GPS state data class
     */
    data class GpsState(
        val satelliteCount: Int = 0,
        val gpsQualityPercentage: Double = 0.0,
        val isGpsEnabled: Boolean = false,
        val gpsAccuracy: Float = 0f,
        val gpsSpeedMph: Float = 0f,
        val lastGpsUpdate: Date = Date(),
        val gpsErrorCount: Int = 0,
        val isHighAccuracyMode: Boolean = false
    )
    
    /**
     * ✅ NEW: Start location coordination
     */
    suspend fun startLocationCoordination(): Boolean {
        return try {
            Log.d(TAG, "Starting location coordination")
            
            // Start location service
            locationService.startLocationService()
            
            // Start GPS synchronization
            gpsSynchronizationService.startSync()
            
            // Update coordinator state
            _coordinatorState.value = LocationCoordinatorState(
                isCoordinating = true,
                lastAction = "Location coordination started",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1,
                isHealthy = true,
                servicesActive = listOf("LocationService", "GpsSynchronizationService")
            )
            
            // Update location state
            _locationState.value = _locationState.value.copy(
                isLocationServiceActive = true,
                hasLocationPermission = locationService.hasLocationPermission(),
                providerStatus = locationService.getLocationProviderStatus()
            )
            
            Log.d(TAG, "Location coordination started successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location coordination", e)
            updateCoordinatorError("Failed to start location coordination: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Stop location coordination
     */
    suspend fun stopLocationCoordination(): Boolean {
        return try {
            Log.d(TAG, "Stopping location coordination")
            
            // Stop location service
            locationService.stopLocationService()
            
            // Stop GPS synchronization
            gpsSynchronizationService.stopSync()
            
            // Update coordinator state
            _coordinatorState.value = _coordinatorState.value.copy(
                isCoordinating = false,
                lastAction = "Location coordination stopped",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1,
                servicesActive = emptyList()
            )
            
            // Update location state
            _locationState.value = _locationState.value.copy(
                isLocationServiceActive = false
            )
            
            Log.d(TAG, "Location coordination stopped successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location coordination", e)
            updateCoordinatorError("Failed to stop location coordination: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Get current location flow
     */
    fun getCurrentLocationFlow(): Flow<Location?> = locationService.getCurrentLocation()
    
    /**
     * ✅ NEW: Update location state
     */
    fun updateLocationState(location: Location?) {
        try {
            location?.let { loc ->
                // Update location state
                _locationState.value = _locationState.value.copy(
                    currentLocation = loc,
                    locationAccuracy = loc.accuracy,
                    locationSpeed = loc.speed,
                    lastUpdateTime = Date(),
                    providerStatus = locationService.getLocationProviderStatus()
                )
                
                // Update GPS state
                _gpsState.value = _gpsState.value.copy(
                    satelliteCount = locationService.getSatelliteCount(),
                    gpsQualityPercentage = locationService.getGpsQualityPercentage(),
                    isGpsEnabled = locationService.getLocationProviderStatus().isGpsEnabled,
                    gpsAccuracy = loc.accuracy,
                    gpsSpeedMph = loc.speed * 2.237f, // Convert m/s to mph
                    lastGpsUpdate = Date(),
                    isHighAccuracyMode = loc.accuracy < 10f
                )
                
                Log.v(TAG, "Updated location state: accuracy=${loc.accuracy}, speed=${loc.speed}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update location state", e)
        }
    }
    
    /**
     * ✅ NEW: Validate current location
     */
    fun validateCurrentLocation(): LocationValidationResult {
        return try {
            val currentLocation = _locationState.value.currentLocation
            if (currentLocation != null) {
                val validationResult = locationValidationService.validateLocation(
                    location = currentLocation,
                    lastLocation = null, // We don't have last location in coordinator
                    lastUpdateTime = currentLocation.time,
                    lastSpeed = 0f // We don't have last speed in coordinator
                )
                
                when (validationResult) {
                    is LocationValidationService.ValidationResult.Valid -> {
                        LocationValidationResult(
                            isValid = true,
                            severity = ValidationSeverity.INFO
                        )
                    }
                    is LocationValidationService.ValidationResult.Invalid -> {
                        LocationValidationResult(
                            isValid = false,
                            errorMessage = validationResult.reason,
                            severity = when (validationResult.severity) {
                                LocationValidationService.ValidationSeverity.WARNING -> ValidationSeverity.WARNING
                                LocationValidationService.ValidationSeverity.ERROR -> ValidationSeverity.ERROR
                                LocationValidationService.ValidationSeverity.CRITICAL -> ValidationSeverity.ERROR
                            }
                        )
                    }
                }
            } else {
                LocationValidationResult(
                    isValid = false,
                    errorMessage = "No current location available",
                    severity = ValidationSeverity.ERROR
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate current location", e)
            LocationValidationResult(
                isValid = false,
                errorMessage = "Location validation failed: ${e.message}",
                severity = ValidationSeverity.ERROR
            )
        }
    }
    
    /**
     * ✅ NEW: Get location metrics
     */
    fun getLocationMetrics(): LocationMetrics {
        val locationState = _locationState.value
        val gpsState = _gpsState.value
        
        return LocationMetrics(
            currentAccuracy = locationState.locationAccuracy,
            currentSpeedMph = locationState.locationSpeed * 2.237f,
            satelliteCount = gpsState.satelliteCount,
            gpsQualityPercentage = gpsState.gpsQualityPercentage,
            isHighAccuracyMode = gpsState.isHighAccuracyMode,
            lastUpdateTime = locationState.lastUpdateTime,
            errorCount = locationState.locationErrorCount + gpsState.gpsErrorCount,
            isLocationServiceActive = locationState.isLocationServiceActive,
            hasLocationPermission = locationState.hasLocationPermission
        )
    }
    
    /**
     * ✅ NEW: Request location permission
     */
    fun requestLocationPermission() {
        try {
            locationService.requestLocationPermission()
            Log.d(TAG, "Location permission requested")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location permission", e)
        }
    }
    
    /**
     * ✅ NEW: Check location service health
     */
    fun checkLocationServiceHealth(): LocationServiceHealth {
        val coordinatorState = _coordinatorState.value
        val locationState = _locationState.value
        val gpsState = _gpsState.value
        
        return LocationServiceHealth(
            isHealthy = coordinatorState.isHealthy && locationState.isLocationServiceActive,
            errorRate = if (coordinatorState.successCount + coordinatorState.errorCount > 0) {
                coordinatorState.errorCount.toDouble() / (coordinatorState.successCount + coordinatorState.errorCount)
            } else 0.0,
            lastError = if (coordinatorState.lastAction.startsWith("Error:")) coordinatorState.lastAction else null,
            uptime = Date().time - coordinatorState.lastActionTime.time,
            locationAccuracy = locationState.locationAccuracy,
            gpsQuality = gpsState.gpsQualityPercentage,
            satelliteCount = gpsState.satelliteCount
        )
    }
    
    /**
     * ✅ NEW: Update coordinator error
     */
    private fun updateCoordinatorError(errorMessage: String) {
        _coordinatorState.value = _coordinatorState.value.copy(
            lastAction = "Error: $errorMessage",
            lastActionTime = Date(),
            errorCount = _coordinatorState.value.errorCount + 1,
            isHealthy = false
        )
    }
    
    /**
     * ✅ NEW: Location validation result data class
     */
    data class LocationValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val severity: ValidationSeverity = ValidationSeverity.INFO
    )
    
    /**
     * ✅ NEW: Validation severity enum
     */
    enum class ValidationSeverity {
        INFO, WARNING, ERROR
    }
    
    /**
     * ✅ NEW: Location metrics data class
     */
    data class LocationMetrics(
        val currentAccuracy: Float,
        val currentSpeedMph: Float,
        val satelliteCount: Int,
        val gpsQualityPercentage: Double,
        val isHighAccuracyMode: Boolean,
        val lastUpdateTime: Date,
        val errorCount: Int,
        val isLocationServiceActive: Boolean,
        val hasLocationPermission: Boolean
    )
    
    /**
     * ✅ NEW: Location service health data class
     */
    data class LocationServiceHealth(
        val isHealthy: Boolean,
        val errorRate: Double,
        val lastError: String?,
        val uptime: Long,
        val locationAccuracy: Float,
        val gpsQuality: Double,
        val satelliteCount: Int
    )
} 
