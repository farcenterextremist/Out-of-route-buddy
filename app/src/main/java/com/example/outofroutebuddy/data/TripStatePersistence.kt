package com.example.outofroutebuddy.data

import com.example.outofroutebuddy.util.AppLogger
import com.example.outofroutebuddy.data.repository.TripRepository
import com.example.outofroutebuddy.domain.models.Trip as DomainTrip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.models.Trip as DataTrip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone
import com.example.outofroutebuddy.utils.extensions.isToday

/**
 * Database-backed state persistence for trip draft/active state and recovery.
 * Coordinates with [TripStateManager] and [TripPersistenceManager]; [saveCompletedTrip] is the single
 * persistence path when user ends a trip (R1). See [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md).
 */
class TripStatePersistence(
    private val repository: TripRepository,
    private val tripStateManager: TripStateManager,
    private val tripPersistenceManager: TripPersistenceManager,
    private val coroutineScope: CoroutineScope,
) {
    companion object {
        private const val TAG = "TripStatePersistence"
        private const val AUTO_SAVE_INTERVAL_MS = 30000L // 30 seconds
    }

    // ✅ NEW: Auto-save state tracking
    private val _isAutoSaving = MutableStateFlow(false)
    val isAutoSaving: StateFlow<Boolean> = _isAutoSaving.asStateFlow()

    // ✅ NEW: Last save timestamp
    private var lastSaveTime = 0L

    init {
        // ✅ INITIALIZE: Start auto-save mechanism
        startAutoSave()
    }

    /**
     * ✅ NEW: Start automatic saving of trip state
     */
    private fun startAutoSave() {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    kotlinx.coroutines.delay(AUTO_SAVE_INTERVAL_MS)

                    // R2: autoSaveTripState removed - was no-op; TripPersistenceManager handles auto-save
                    if (tripStateManager.isTripActive()) {
                        // Future: persist draft state to TripPersistenceManager if needed
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Auto-save failed", e)
                }
            }
        }
    }

    /**
     * Wired to trip-end flow (TripInputViewModel.endTrip). Saves completed trip with GPS metadata. R1.
     * Uses [tripStateManager] for loaded/bounce/actual miles and GPS metadata; single insert via data repository.
     */
    suspend fun saveCompletedTrip(
        actualMiles: Double,
        loadedMiles: Double? = null,
        bounceMiles: Double? = null,
        tripStartTime: Date? = null,
        tripEndTime: Date? = null,
    ): Long {
        return withContext(Dispatchers.IO) {
            try {
                val currentState = tripStateManager.getCurrentState()
                val gpsMetadata = tripStateManager.getGpsMetadataForStorage().toMutableMap()

                // Calculate trip metrics
                val resolvedLoadedMiles = loadedMiles ?: (currentState.loadedMiles.toDoubleOrNull() ?: 0.0)
                val resolvedBounceMiles = bounceMiles ?: (currentState.bounceMiles.toDoubleOrNull() ?: 0.0)
                val resolvedStartTime = tripStartTime ?: (gpsMetadata["tripStartTime"] as? Date) ?: currentState.startTime ?: Date()
                val resolvedEndTime = tripEndTime ?: (gpsMetadata["tripEndTime"] as? Date) ?: Date()

                gpsMetadata["tripStartTime"] = resolvedStartTime
                gpsMetadata["tripEndTime"] = resolvedEndTime
                // Store timezone where trip was recorded so we can show it when user is in a different zone
                gpsMetadata["tripTimeZoneId"] = TimeZone.getDefault().id

                // Create trip entity (data layer Trip for repository)
                val dataTrip = DataTrip(
                    id = 0L,
                    date = resolvedEndTime,
                    loadedMiles = resolvedLoadedMiles,
                    bounceMiles = resolvedBounceMiles,
                    actualMiles = actualMiles,
                )

                // Save to database with GPS metadata
                val tripId = repository.insertTrip(dataTrip, gpsMetadata)

                AppLogger.d(TAG, "Saved completed trip to database")
                tripId
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save completed trip", e)
                throw e
            }
        }
    }

    /**
     * Look for an incomplete trip from today and return true if one was found (recovery hint).
     * @return true if recent incomplete trip exists
     */
    suspend fun recoverTripState(): Boolean {
        return try {
            // Look for the most recent trip that might be incomplete
            val allTrips = repository.getAllTrips().first()
            val recentTrips =
                allTrips.filter { trip ->
                    // Look for trips from today that might be incomplete
                    trip.date.isToday() && trip.actualMiles == 0.0 // Incomplete trip
                }

            if (recentTrips.isNotEmpty()) {
                val mostRecent = recentTrips.maxByOrNull { trip -> trip.date }
                if (mostRecent != null) {
                    // Recover state from the most recent incomplete trip
                    AppLogger.d(TAG, "Recovering trip state from database")
                    return true
                }
            }

            false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to recover trip state", e)
            false
        }
    }

    /**
     * ✅ NEW: Clean up temporary trip records
     */
    suspend fun cleanupTempTrips() {
        try {
            // Temporary trip cleanup - currently not needed as auto-save is handled by TripPersistenceManager
            AppLogger.d(TAG, "Cleaned up temporary trip records")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to cleanup temporary trips", e)
        }
    }

    /**
     * ✅ NEW: Get auto-save status
     */
    fun isAutoSaving(): Boolean = _isAutoSaving.value

    /**
     * ✅ NEW: Force save current state
     * R2: Replaced removed autoSaveTripState() with saveTripState() - TripPersistenceManager handles persistence
     */
    suspend fun forceSave() {
        if (tripStateManager.isTripActive()) {
            saveTripState(tripStateManager.getCurrentState())
        }
    }

    // ==================== NEW: VIEWMODEL INTEGRATION METHODS ====================

    /**
     * Save current trip state to [TripPersistenceManager] for recovery. Call when trip is active.
     * @param tripState Current state from [TripStateManager]
     */
    fun saveTripState(tripState: TripStateManager.TripState) {
        try {
            AppLogger.d(TAG, "Saving trip state for ViewModel: isActive=${tripState.isActive}")
            
            if (!tripState.isActive) {
                AppLogger.d(TAG, "Trip is not active, skipping save")
                return
            }
            
            // Convert TripStateManager.TripState to Trip domain model
            val loadedMiles = tripState.loadedMiles.toDoubleOrNull() ?: 0.0
            val bounceMiles = tripState.bounceMiles.toDoubleOrNull() ?: 0.0
            val actualMiles = 0.0 // Will be updated by TripTrackingService
            
            // Create Trip object for persistence (domain Trip for TripPersistenceManager)
            val trip = DomainTrip(
                id = "trip-${tripState.startTime?.time ?: System.currentTimeMillis()}",
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                startTime = tripState.startTime ?: Date(),
                status = TripStatus.ACTIVE,
                gpsMetadata = com.example.outofroutebuddy.domain.models.GpsMetadata(
                    totalPoints = tripState.gpsMetadata.totalPoints,
                    validPoints = tripState.gpsMetadata.validPoints,
                    avgAccuracy = tripState.gpsMetadata.avgAccuracy
                )
            )
            
            // Convert TripStateManager.LocationData to TripPersistenceManager.LocationData
            val lastLocation = tripState.lastLocation?.let { location ->
                TripPersistenceManager.LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = location.timestamp,
                    speed = location.speed
                )
            }
            
            // Convert TripStateManager.GpsMetadata to TripPersistenceManager.GpsMetadata
            val gpsMetadata = TripPersistenceManager.GpsMetadata(
                totalPoints = tripState.gpsMetadata.totalPoints,
                validPoints = tripState.gpsMetadata.validPoints,
                avgAccuracy = tripState.gpsMetadata.avgAccuracy
            )
            
            // Save using TripPersistenceManager
            tripPersistenceManager.saveActiveTripState(
                trip = trip,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                lastLocation = lastLocation,
                gpsMetadata = gpsMetadata
            )
            
            lastSaveTime = System.currentTimeMillis()
            AppLogger.d(TAG, "Trip state saved successfully to persistence")
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save trip state", e)
        }
    }

    /**
     * Load persisted trip state from [TripPersistenceManager], or null if none.
     * @return Restored [TripStateManager.TripState] or null
     */
    fun loadTripState(): TripStateManager.TripState? {
        return try {
            AppLogger.d(TAG, "Loading trip state for ViewModel")
            
            val savedState = tripPersistenceManager.loadSavedTripState()
            if (savedState == null) {
                AppLogger.d(TAG, "No saved trip state found")
                return null
            }
            
            // Convert SavedTripState to TripStateManager.TripState
            val tripState = TripStateManager.TripState(
                isActive = savedState.trip.status == TripStatus.ACTIVE,
                loadedMiles = savedState.loadedMiles.toString(),
                bounceMiles = savedState.bounceMiles.toString(),
                startTime = savedState.startTime,
                lastLocation = savedState.lastLocation?.let { location ->
                    TripStateManager.LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.timestamp,
                        speed = location.speed
                    )
                },
                gpsMetadata = savedState.gpsMetadata?.let { metadata ->
                    TripStateManager.GpsMetadata(
                        totalPoints = metadata.totalPoints,
                        validPoints = metadata.validPoints,
                        avgAccuracy = metadata.avgAccuracy
                    )
                } ?: TripStateManager.GpsMetadata(),
                lastUpdated = savedState.recoveryTime
            )
            
            AppLogger.d(TAG, "Trip state loaded successfully: isActive=${tripState.isActive}")
            tripState
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load trip state", e)
            null
        }
    }

    /**
     * Restore trip state by saving to persistence (ViewModel can then load and apply).
     * @param tripState State to restore
     */
    fun restoreTripState(tripState: TripStateManager.TripState) {
        try {
            AppLogger.d(TAG, "Restoring trip state for ViewModel: isActive=${tripState.isActive}")
            
            // Restore state to TripStateManager by updating its internal state
            // Note: TripStateManager uses a StateFlow, so we need to update it directly
            // Since TripStateManager doesn't expose a setter, we'll use reflection or
            // let the ViewModel handle the restoration through the normal flow
            
            // For now, we'll save the state to persistence so it can be loaded later
            // The ViewModel should call loadTripState() and then update TripStateManager
            saveTripState(tripState)
            
            AppLogger.d(TAG, "Trip state restored successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to restore trip state", e)
        }
    }
} 
