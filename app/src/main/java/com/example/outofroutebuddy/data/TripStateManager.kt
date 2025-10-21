package com.example.outofroutebuddy.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * ✅ NEW: Single Source of Truth for Trip State
 *
 * This class manages all trip-related state and ensures consistency
 * across ViewModel, Preferences, and Service layers.
 */
class TripStateManager(
    private val preferencesManager: PreferencesManager,
) {
    companion object {
        private const val TAG = "TripStateManager"
    }

    // ✅ SINGLE SOURCE OF TRUTH: Centralized trip state
    private val _tripState = MutableStateFlow(TripState())
    val tripState: StateFlow<TripState> = _tripState.asStateFlow()

    // ✅ NEW: Trip state data class
    data class TripState(
        val isActive: Boolean = false,
        val loadedMiles: String = "",
        val bounceMiles: String = "",
        val startTime: Date? = null,
        val lastLocation: LocationData? = null,
        val gpsMetadata: GpsMetadata = GpsMetadata(),
        val lastUpdated: Date = Date(),
    )

    // ✅ NEW: Location data for GPS tracking
    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Date,
        val speed: Float = 0f,
    )

    // ✅ NEW: GPS metadata for trip analysis
    data class GpsMetadata(
        val totalPoints: Int = 0,
        val validPoints: Int = 0,
        val rejectedPoints: Int = 0,
        val avgAccuracy: Double = 0.0,
        val minAccuracy: Double = 0.0,
        val maxAccuracy: Double = 0.0,
        val avgSpeed: Double = 0.0,
        val maxSpeed: Double = 0.0,
        val locationJumps: Int = 0,
        val accuracyWarnings: Int = 0,
        val speedAnomalies: Int = 0,
        val interruptions: Int = 0,
    )

    init {
        // ✅ INITIALIZE: Load persisted state on startup
        loadPersistedState()
    }

    /**
     * ✅ NEW: Load persisted state from preferences
     */
    private fun loadPersistedState() {
        try {
            // Force trip to be inactive on app startup
            val isActive = false // Always start with inactive trip
            val loadedMiles = preferencesManager.getLastLoadedMiles()
            val bounceMiles = preferencesManager.getLastBounceMiles()

            _tripState.value =
                TripState(
                    isActive = isActive,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    lastUpdated = Date(),
                )

            // Clear the persisted active state to ensure consistency
            preferencesManager.saveTripActive(false)

            Log.d(TAG, "Loaded persisted state: isActive=$isActive, loadedMiles=$loadedMiles, bounceMiles=$bounceMiles")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load persisted state", e)
            // Start with clean state on error
            _tripState.value = TripState()
        }
    }

    /**
     * ✅ NEW: Start trip with validation
     */
    fun startTrip(
        loadedMiles: String,
        bounceMiles: String,
    ): Boolean {
        return try {
            // Validate input
            if (!isValidTripInput(loadedMiles, bounceMiles)) {
                Log.w(TAG, "Invalid trip input: loadedMiles=$loadedMiles, bounceMiles=$bounceMiles")
                return false
            }

            // Update state
            val newState =
                _tripState.value.copy(
                    isActive = true,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    startTime = Date(),
                    lastUpdated = Date(),
                )

            _tripState.value = newState

            // Persist to preferences
            preferencesManager.saveTripActive(true)
            preferencesManager.saveLastLoadedMiles(loadedMiles)
            preferencesManager.saveLastBounceMiles(bounceMiles)

            Log.d(TAG, "Trip started successfully: loadedMiles=$loadedMiles, bounceMiles=$bounceMiles")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip", e)
            false
        }
    }

    /**
     * ✅ NEW: End trip with cleanup
     */
    fun endTrip(): Boolean {
        return try {
            val currentState = _tripState.value

            // Update state
            val newState =
                _tripState.value.copy(
                    isActive = false,
                    lastUpdated = Date(),
                )

            _tripState.value = newState

            // Persist to preferences
            preferencesManager.saveTripActive(false)

            Log.d(TAG, "Trip ended successfully. Duration: ${calculateTripDuration(currentState.startTime)}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end trip", e)
            false
        }
    }

    /**
     * ✅ NEW: Stop trip (alias for endTrip)
     */
    fun stopTrip(): Boolean = endTrip()

    /**
     * ✅ NEW: Update GPS location data
     */
    fun updateLocation(location: LocationData) {
        try {
            val currentState = _tripState.value
            val currentMetadata = currentState.gpsMetadata

            // Update GPS metadata
            val newMetadata = updateGpsMetadata(currentMetadata, location)

            val newState =
                currentState.copy(
                    lastLocation = location,
                    gpsMetadata = newMetadata,
                    lastUpdated = Date(),
                )

            _tripState.value = newState

            Log.v(TAG, "Location updated: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update location", e)
        }
    }

    /**
     * ✅ NEW: Update GPS metadata with new location
     */
    private fun updateGpsMetadata(
        current: GpsMetadata,
        newLocation: LocationData,
    ): GpsMetadata {
        val totalPoints = current.totalPoints + 1
        val accuracy = newLocation.accuracy.toDouble()
        val speed = newLocation.speed.toDouble()

        // Determine if point is valid (accuracy < 20 meters)
        val isValid = accuracy < 20.0
        val validPoints = if (isValid) current.validPoints + 1 else current.validPoints
        val rejectedPoints = if (!isValid) current.rejectedPoints + 1 else current.rejectedPoints

        // Update accuracy stats
        val newAvgAccuracy = if (totalPoints == 1) accuracy else (current.avgAccuracy * (totalPoints - 1) + accuracy) / totalPoints
        val newMinAccuracy = minOf(current.minAccuracy, accuracy)
        val newMaxAccuracy = maxOf(current.maxAccuracy, accuracy)

        // Update speed stats
        val newAvgSpeed = if (totalPoints == 1) speed else (current.avgSpeed * (totalPoints - 1) + speed) / totalPoints
        val newMaxSpeed = maxOf(current.maxSpeed, speed)

        // Detect anomalies
        val accuracyWarning = if (accuracy > 50.0) 1 else 0
        val speedAnomaly = if (speed > 80.0) 1 else 0 // 80 mph threshold

        return GpsMetadata(
            totalPoints = totalPoints,
            validPoints = validPoints,
            rejectedPoints = rejectedPoints,
            avgAccuracy = newAvgAccuracy,
            minAccuracy = newMinAccuracy,
            maxAccuracy = newMaxAccuracy,
            avgSpeed = newAvgSpeed,
            maxSpeed = newMaxSpeed,
            locationJumps = current.locationJumps, // TODO: Implement jump detection
            accuracyWarnings = current.accuracyWarnings + accuracyWarning,
            speedAnomalies = current.speedAnomalies + speedAnomaly,
            interruptions = current.interruptions,
        )
    }

    /**
     * ✅ NEW: Validate trip input
     */
    private fun isValidTripInput(
        loadedMiles: String,
        bounceMiles: String,
    ): Boolean {
        return try {
            val loaded = loadedMiles.toDoubleOrNull() ?: return false
            val bounce = bounceMiles.toDoubleOrNull() ?: return false

            loaded >= 0.0 && bounce >= 0.0 && (loaded + bounce) > 0.0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ✅ NEW: Calculate trip duration
     */
    private fun calculateTripDuration(startTime: Date?): String {
        if (startTime == null) return "Unknown"

        val duration = Date().time - startTime.time
        val minutes = duration / (1000 * 60)
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            else -> "${minutes}m"
        }
    }

    /**
     * ✅ NEW: Get current trip state
     */
    fun getCurrentState(): TripState = _tripState.value

    /**
     * ✅ NEW: Check if trip is active
     */
    fun isTripActive(): Boolean = _tripState.value.isActive

    /**
     * ✅ NEW: Get GPS metadata for database storage
     */
    fun getGpsMetadataForStorage(): Map<String, Any> {
        val metadata = _tripState.value.gpsMetadata
        val startTime = _tripState.value.startTime
        val lastLocation = _tripState.value.lastLocation

        return mapOf(
            "totalGpsPoints" to metadata.totalPoints,
            "validGpsPoints" to metadata.validPoints,
            "rejectedGpsPoints" to metadata.rejectedPoints,
            "avgGpsAccuracy" to metadata.avgAccuracy,
            "minGpsAccuracy" to metadata.minAccuracy,
            "maxGpsAccuracy" to metadata.maxAccuracy,
            "avgSpeedMph" to metadata.avgSpeed,
            "maxSpeedMph" to metadata.maxSpeed,
            "locationJumpsDetected" to metadata.locationJumps,
            "accuracyWarnings" to metadata.accuracyWarnings,
            "speedAnomalies" to metadata.speedAnomalies,
            "interruptionCount" to metadata.interruptions,
            "tripStartTime" to (startTime ?: Date()),
            "tripEndTime" to Date(),
            "wasInterrupted" to (metadata.interruptions > 0),
            "lastLocationLat" to (lastLocation?.latitude ?: 0.0),
            "lastLocationLng" to (lastLocation?.longitude ?: 0.0),
            "lastLocationTime" to (lastLocation?.timestamp ?: Date()),
        )
    }

    // ==================== NEW: VIEWMODEL INTEGRATION METHODS ====================

    /**
     * ✅ NEW: Get current trip state for ViewModel
     */
    fun getCurrentTripState(): TripState {
        return _tripState.value
    }

    /**
     * ✅ NEW: Restore trip state for ViewModel
     */
    fun restoreTripState(tripState: TripState) {
        try {
            Log.d(TAG, "Restoring trip state for ViewModel")

            _tripState.value = tripState

            // Persist to preferences
            preferencesManager.saveTripActive(tripState.isActive)
            preferencesManager.saveLastLoadedMiles(tripState.loadedMiles)
            preferencesManager.saveLastBounceMiles(tripState.bounceMiles)

            Log.d(TAG, "Trip state restored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore trip state", e)
        }
    }
} 
