package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.data.TripStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * 🚀 **COMPREHENSIVE MOCK GPS SYNCHRONIZATION SERVICE FOR TESTING**
 *
 * This mock service provides controlled GPS data for testing WITHOUT requiring:
 * - Actual GPS hardware
 * - Location permissions
 * - Real device movement
 *
 * ✅ **Features:**
 * - Simulate realistic GPS updates
 * - Control timing and data flow
 * - Test real-time UI updates
 * - Verify trip calculations
 * - Simulate GPS errors/failures
 * - Test arrival estimation
 * - Simulate location jumps
 * - Test GPS quality variations
 *
 * 📊 **Usage in Tests:**
 * ```kotlin
 * val mock = MockGpsSynchronizationService()
 * 
 * // Simulate driving 10 miles
 * mock.simulateDriving(distanceMiles = 10.0, durationSeconds = 600)
 * 
 * // Verify UI updated
 * assertEquals(10.0, viewModel.uiState.value.actualMiles, 0.1)
 * ```
 */
class MockGpsSynchronizationService : IGpsSynchronizationService {
    
    // ===================================================================================
    // STATE MANAGEMENT
    // ===================================================================================
    
    /**
     * Real-time GPS data flowing to UI
     */
    private val _realTimeGpsData = MutableStateFlow(
        GpsSynchronizationService.RealTimeGpsData(
            totalDistance = 0.0,
            currentLocation = null,
            currentSpeed = 0.0,
            accuracy = 10.0,
            gpsQuality = 0.8,
            satelliteCount = 8,
            isHighAccuracy = true,
            lastUpdate = Date(),
            tripDuration = "0m",
            estimatedArrival = "Unknown"
        )
    )
    override val realTimeGpsData: StateFlow<GpsSynchronizationService.RealTimeGpsData> = 
        _realTimeGpsData.asStateFlow()

    /**
     * GPS synchronization state
     */
    private val _syncState = MutableStateFlow(
        GpsSynchronizationService.GpsSyncState(
            isSynchronized = false,
            lastSyncTime = Date(),
            syncErrors = 0,
            dataQuality = "Good",
            connectionStatus = "Disconnected"
        )
    )
    override val syncState: StateFlow<GpsSynchronizationService.GpsSyncState> = 
        _syncState.asStateFlow()

    // ===================================================================================
    // TRACKING VARIABLES
    // ===================================================================================
    
    private var isSyncingActive = false
    private var totalDistance = 0.0
    private var currentSpeed = 0.0
    private var tripStartTime: Date? = null
    private var locationJumpsDetected = 0
    private var syncErrors = 0
    private var currentLatitude = 40.7128 // Default: New York
    private var currentLongitude = -74.0060
    
    // Destination for arrival estimation
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null
    private var routeDistance: Double = 0.0
    
    // History for quality metrics
    private val speedHistory = mutableListOf<Double>()
    private val accuracyHistory = mutableListOf<Double>()
    
    // Callbacks for testing
    private var onDistanceUpdateCallback: ((Double) -> Unit)? = null
    private var onLocationUpdateCallback: ((TripStateManager.LocationData) -> Unit)? = null
    private var onSpeedUpdateCallback: ((Double) -> Unit)? = null
    
    // ===================================================================================
    // LIFECYCLE METHODS
    // ===================================================================================
    
    /**
     * ✅ Start GPS synchronization
     */
    override fun startSync() {
        isSyncingActive = true
        tripStartTime = Date()
        emitSyncState(
            isSynchronized = true,
            dataQuality = "Good",
            connectionStatus = "Connected"
        )
    }

    /**
     * ✅ Stop GPS synchronization
     */
    override fun stopSync() {
        isSyncingActive = false
        emitSyncState(
            isSynchronized = false,
            dataQuality = "Offline",
            connectionStatus = "Disconnected"
        )
    }
    
    /**
     * ✅ Reset all GPS data to initial state
     */
    fun reset() {
        totalDistance = 0.0
        currentSpeed = 0.0
        tripStartTime = null
        locationJumpsDetected = 0
        syncErrors = 0
        speedHistory.clear()
        accuracyHistory.clear()
        
        _realTimeGpsData.value = GpsSynchronizationService.RealTimeGpsData(
            totalDistance = 0.0,
            currentLocation = null,
            currentSpeed = 0.0,
            accuracy = 10.0,
            gpsQuality = 0.8,
            satelliteCount = 8,
            isHighAccuracy = true,
            lastUpdate = Date(),
            tripDuration = "0m",
            estimatedArrival = "Unknown"
        )
        
        _syncState.value = GpsSynchronizationService.GpsSyncState(
            isSynchronized = false,
            lastSyncTime = Date(),
            syncErrors = 0,
            dataQuality = "Good",
            connectionStatus = "Disconnected"
        )
    }

    // ===================================================================================
    // SIMULATION METHODS (for testing)
    // ===================================================================================
    
    /**
     * 🎭 **SIMULATE DRIVING** - Most useful method for tests!
     * 
     * Simulates a realistic driving scenario with:
     * - Gradual distance increase
     * - Varying speed
     * - GPS quality fluctuations
     * 
     * @param distanceMiles Total miles to simulate
     * @param durationSeconds Trip duration in seconds
     * @param averageSpeedMph Average speed (calculated if not provided)
     */
    fun simulateDriving(
        distanceMiles: Double,
        durationSeconds: Int = (distanceMiles * 60).toInt(), // Assume 1 mile/min by default
        averageSpeedMph: Double = (distanceMiles / durationSeconds) * 3600
    ) {
        val steps = 10 // Simulate 10 GPS updates
        val distanceIncrement = distanceMiles / steps
        val speedVariation = averageSpeedMph * 0.1 // ±10% speed variation
        
        repeat(steps) { step ->
            // Vary speed slightly for realism
            val speedVariance = (Math.random() - 0.5) * 2 * speedVariation
            val currentStepSpeed = averageSpeedMph + speedVariance
            
            // Accumulate distance
            totalDistance += distanceIncrement
            currentSpeed = currentStepSpeed
            
            // Move location slightly
            currentLatitude += 0.001 * distanceIncrement
            currentLongitude += 0.001 * distanceIncrement
            
            // Emit updates
            emitDistance(totalDistance)
            emitSpeed(currentStepSpeed)
            emitLocation(currentLatitude, currentLongitude)
            
            // Update history
            speedHistory.add(currentStepSpeed)
            if (speedHistory.size > 20) speedHistory.removeAt(0)
        }
    }
    
    /**
     * 🎭 Simulate gradual distance increase (like real GPS)
     */
    fun simulateGradualDistance(fromMiles: Double, toMiles: Double, steps: Int = 10) {
        val increment = (toMiles - fromMiles) / steps
        var current = fromMiles
        
        repeat(steps) {
            current += increment
            emitDistance(current)
        }
    }
    
    /**
     * 🎭 Simulate a complete trip from start to finish
     */
    fun simulateCompleteTrip(
        distanceMiles: Double,
        loadedMiles: Double,
        bounceMiles: Double
    ): TripSimulationResult {
        startSync()
        simulateDriving(distanceMiles)
        stopSync()
        
        val dispatched = loadedMiles + bounceMiles
        val oor = distanceMiles - dispatched
        val oorPercentage = if (dispatched > 0) (oor / dispatched) * 100 else 0.0
        
        return TripSimulationResult(
            actualMiles = distanceMiles,
            loadedMiles = loadedMiles,
            bounceMiles = bounceMiles,
            dispatchedMiles = dispatched,
            oorMiles = oor,
            oorPercentage = oorPercentage,
            tripDuration = calculateTripDuration()
        )
    }

    // ===================================================================================
    // BASIC EMISSION METHODS
    // ===================================================================================
    
    /**
     * ✅ Emit distance update
     */
    fun emitDistance(distanceMiles: Double) {
        totalDistance = distanceMiles
        _realTimeGpsData.value = _realTimeGpsData.value.copy(
            totalDistance = distanceMiles,
            lastUpdate = Date(),
            tripDuration = calculateTripDuration()
        )
        onDistanceUpdateCallback?.invoke(distanceMiles)
    }

    /**
     * ✅ Emit location update
     */
    fun emitLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float = 5.0f
    ) {
        currentLatitude = latitude
        currentLongitude = longitude
        
        val locationData = TripStateManager.LocationData(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            timestamp = Date(),
            speed = currentSpeed.toFloat()
        )

        _realTimeGpsData.value = _realTimeGpsData.value.copy(
            currentLocation = locationData,
            accuracy = accuracy.toDouble(),
            lastUpdate = Date()
        )
        
        onLocationUpdateCallback?.invoke(locationData)
    }

    /**
     * ✅ Emit speed update
     */
    fun emitSpeed(speedMph: Double) {
        currentSpeed = speedMph
        speedHistory.add(speedMph)
        if (speedHistory.size > 20) speedHistory.removeAt(0)
        
        _realTimeGpsData.value = _realTimeGpsData.value.copy(
            currentSpeed = speedMph,
            lastUpdate = Date()
        )
        
        onSpeedUpdateCallback?.invoke(speedMph)
        updateArrivalEstimation()
    }

    /**
     * ✅ Emit GPS quality update
     */
    fun emitGpsQuality(
        quality: Double,
        satelliteCount: Int,
        accuracy: Double = 5.0
    ) {
        accuracyHistory.add(accuracy)
        if (accuracyHistory.size > 20) accuracyHistory.removeAt(0)
        
        _realTimeGpsData.value = _realTimeGpsData.value.copy(
            gpsQuality = quality,
            satelliteCount = satelliteCount,
            accuracy = accuracy,
            isHighAccuracy = quality > 0.7 && accuracy < 20.0,
            lastUpdate = Date()
        )
    }

    // ===================================================================================
    // ERROR SIMULATION (for testing error handling)
    // ===================================================================================
    
    /**
     * 🎭 Simulate GPS signal loss
     */
    fun simulateSignalLoss() {
        emitGpsQuality(quality = 0.0, satelliteCount = 0, accuracy = 999.0)
        emitSyncState(
            isSynchronized = false,
            dataQuality = "Poor",
            connectionStatus = "Signal Lost"
        )
        syncErrors++
    }
    
    /**
     * 🎭 Simulate location jump (GPS glitch)
     */
    fun simulateLocationJump(jumpDistanceMiles: Double) {
        locationJumpsDetected++
        currentLatitude += jumpDistanceMiles * 0.014 // ~1 degree ≈ 69 miles
        emitLocation(currentLatitude, currentLongitude)
        emitSyncState(
            isSynchronized = true,
            dataQuality = "Location Jump Detected",
            connectionStatus = "Connected"
        )
    }
    
    /**
     * 🎭 Simulate poor GPS accuracy
     */
    fun simulatePoorAccuracy() {
        emitGpsQuality(quality = 0.3, satelliteCount = 3, accuracy = 50.0)
        emitSyncState(
            isSynchronized = true,
            dataQuality = "Poor",
            connectionStatus = "Weak Signal"
        )
    }
    
    /**
     * 🎭 Simulate GPS recovery after error
     */
    fun simulateRecovery() {
        emitGpsQuality(quality = 0.9, satelliteCount = 10, accuracy = 3.0)
        emitSyncState(
            isSynchronized = true,
            dataQuality = "Excellent",
            connectionStatus = "Connected"
        )
    }

    // ===================================================================================
    // ARRIVAL ESTIMATION
    // ===================================================================================
    
    /**
     * ✅ Set destination for arrival estimation
     */
    fun setDestination(latitude: Double, longitude: Double, routeDistanceMiles: Double) {
        destinationLatitude = latitude
        destinationLongitude = longitude
        routeDistance = routeDistanceMiles
        updateArrivalEstimation()
    }
    
    /**
     * Calculate and update arrival estimation
     */
    private fun updateArrivalEstimation() {
        if (destinationLatitude == null || routeDistance == 0.0) {
            return
        }
        
        val avgSpeed = if (speedHistory.isNotEmpty()) {
            speedHistory.average()
        } else {
            currentSpeed
        }
        
        if (avgSpeed < 1.0) {
            _realTimeGpsData.value = _realTimeGpsData.value.copy(
                estimatedArrival = "Stopped"
            )
            return
        }
        
        val remainingDistance = routeDistance - totalDistance
        if (remainingDistance <= 0) {
            _realTimeGpsData.value = _realTimeGpsData.value.copy(
                estimatedArrival = "Arrived"
            )
            return
        }
        
        val etaMinutes = (remainingDistance / avgSpeed) * 60
        val etaText = when {
            etaMinutes < 1 -> "< 1 min"
            etaMinutes < 60 -> "${etaMinutes.toInt()} min"
            else -> "${(etaMinutes / 60).toInt()}h ${(etaMinutes % 60).toInt()}m"
        }
        
        _realTimeGpsData.value = _realTimeGpsData.value.copy(
            estimatedArrival = etaText
        )
    }

    // ===================================================================================
    // SYNC STATE MANAGEMENT
    // ===================================================================================
    
    /**
     * ✅ Emit GPS sync state
     */
    fun emitSyncState(
        isSynchronized: Boolean,
        dataQuality: String = "Good",
        connectionStatus: String = "Connected"
    ) {
        _syncState.value = GpsSynchronizationService.GpsSyncState(
            isSynchronized = isSynchronized,
            lastSyncTime = Date(),
            syncErrors = syncErrors,
            dataQuality = dataQuality,
            connectionStatus = connectionStatus
        )
    }

    // ===================================================================================
    // CALLBACKS (for advanced testing)
    // ===================================================================================
    
    /**
     * Set callback for distance updates
     */
    fun setOnDistanceUpdate(callback: (Double) -> Unit) {
        onDistanceUpdateCallback = callback
    }
    
    /**
     * Set callback for location updates
     */
    fun setOnLocationUpdate(callback: (TripStateManager.LocationData) -> Unit) {
        onLocationUpdateCallback = callback
    }
    
    /**
     * Set callback for speed updates
     */
    fun setOnSpeedUpdate(callback: (Double) -> Unit) {
        onSpeedUpdateCallback = callback
    }

    // ===================================================================================
    // UTILITY METHODS
    // ===================================================================================
    
    /**
     * Calculate trip duration string
     */
    private fun calculateTripDuration(): String {
        val start = tripStartTime ?: return "0m"
        val durationMs = Date().time - start.time
        val minutes = (durationMs / 1000 / 60).toInt()
        
        return when {
            minutes < 1 -> "< 1m"
            minutes < 60 -> "${minutes}m"
            else -> "${minutes / 60}h ${minutes % 60}m"
        }
    }
    
    /**
     * Get average speed over trip
     */
    fun getAverageSpeed(): Double {
        return if (speedHistory.isNotEmpty()) {
            speedHistory.average()
        } else {
            0.0
        }
    }
    
    /**
     * Get average GPS accuracy
     */
    fun getAverageAccuracy(): Double {
        return if (accuracyHistory.isNotEmpty()) {
            accuracyHistory.average()
        } else {
            10.0
        }
    }
    
    /**
     * Get current state snapshot (useful for assertions in tests)
     */
    fun getStateSnapshot(): MockStateSnapshot {
        return MockStateSnapshot(
            totalDistance = totalDistance,
            currentSpeed = currentSpeed,
            isSynchronized = _syncState.value.isSynchronized,
            syncErrors = syncErrors,
            locationJumps = locationJumpsDetected,
            averageSpeed = getAverageSpeed(),
            averageAccuracy = getAverageAccuracy(),
            gpsQuality = _realTimeGpsData.value.gpsQuality,
            satelliteCount = _realTimeGpsData.value.satelliteCount
        )
    }
    
    // ===================================================================================
    // DATA CLASSES
    // ===================================================================================
    
    /**
     * Result of a simulated trip
     */
    data class TripSimulationResult(
        val actualMiles: Double,
        val loadedMiles: Double,
        val bounceMiles: Double,
        val dispatchedMiles: Double,
        val oorMiles: Double,
        val oorPercentage: Double,
        val tripDuration: String
    )
    
    /**
     * Snapshot of mock state for assertions
     */
    data class MockStateSnapshot(
        val totalDistance: Double,
        val currentSpeed: Double,
        val isSynchronized: Boolean,
        val syncErrors: Int,
        val locationJumps: Int,
        val averageSpeed: Double,
        val averageAccuracy: Double,
        val gpsQuality: Double,
        val satelliteCount: Int
    )
}
