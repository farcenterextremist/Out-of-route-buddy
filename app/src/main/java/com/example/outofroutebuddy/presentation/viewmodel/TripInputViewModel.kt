package com.example.outofroutebuddy.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.services.BackgroundSyncService
import com.example.outofroutebuddy.services.OptimizedGpsDataFlow
import com.example.outofroutebuddy.services.TripCrashRecoveryManager
import com.example.outofroutebuddy.services.TripTrackingService
import com.example.outofroutebuddy.services.UnifiedLocationService
import com.example.outofroutebuddy.services.UnifiedOfflineService
import com.example.outofroutebuddy.services.UnifiedTripService
import com.example.outofroutebuddy.validation.ValidationFramework
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ✅ CRITICAL GPS TRACKING FIXES: ViewModel for handling trip input and real-time GPS tracking
 *
 * 🐛 MAJOR BUGS FIXED (Real-world testing verified):
 * 1. Unit Conversion Bug: GPS distances were converted to km instead of miles
 * 2. Service Lifecycle Bug: GPS service wasn't started during trips
 * 
 * 🔧 GPS TRACKING ARCHITECTURE:
 * TripInputViewModel → TripTrackingService → UnifiedLocationService → GPS Hardware
 *                    ↓
 *               Real-time UI Updates
 * 
 * ✅ CRITICAL FIXES IMPLEMENTED:
 * - Start GPS service in calculateTrip() for live tracking
 * - Observe TripTrackingService.tripMetrics for real-time updates  
 * - Stop GPS service in endTrip() to prevent battery drain
 * - Proper unit conversion: meters → miles (1609.34 factor)
 * 
 * 📊 DATA FLOW:
 * 1. User starts trip → TripTrackingService.startService()
 * 2. GPS updates → UnifiedLocationService.calculateDistanceIncrement()
 * 3. Distance calculation → TripTrackingService.tripMetrics
 * 4. UI updates → observeGpsTrackingData() → actualMiles display
 * 5. Trip ends → TripTrackingService.stopService()
 * 
 * ✅ VERIFIED: Real-world testing confirmed "Total Miles" now updates correctly
 */
@HiltViewModel
class TripInputViewModel
    @Inject
    constructor(
        private val tripRepository: TripRepository,
        private val preferencesManager: PreferencesManager,
        private val tripStateManager: TripStateManager,
        private val tripStatePersistence: TripStatePersistence,
        private val stateCache: StateCache,
        private val backgroundSyncService: BackgroundSyncService,
        private val optimizedGpsDataFlow: OptimizedGpsDataFlow,
        private val validationFramework: ValidationFramework,
        // ✅ UNIFIED: New unified services
        private val unifiedLocationService: UnifiedLocationService,
        private val unifiedTripService: UnifiedTripService,
        private val unifiedOfflineService: UnifiedOfflineService,
        // ✅ CRASH RECOVERY: Added in #12
        private val crashRecoveryManager: TripCrashRecoveryManager,
        private val application: android.app.Application,
    ) : ViewModel() {
        companion object {
            private const val TAG = "TripInputViewModel"
        }

        // ✅ SIMPLIFIED: UI State
        private val _uiState = MutableStateFlow(TripInputUiState())
        val uiState: StateFlow<TripInputUiState> = _uiState.asStateFlow()

        // ✅ SIMPLIFIED: Events
        private val _events = MutableSharedFlow<TripEvent>()
        val events: SharedFlow<TripEvent> = _events.asSharedFlow()

        // Current trip data
        private var currentTrip: Trip? = null

        init {
            loadInitialData()
            observeTripState()
            observeLocationData()
            observeGpsTrackingData()
        }

        // ==================== BASIC FUNCTIONALITY ====================

        /**
         * ✅ SIMPLIFIED: Load initial data
         * ✅ CRASH RECOVERY: Check for recovered trip state (#12)
         */
        private fun loadInitialData() {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Loading initial data")

                    // ✅ NEW (#12): Check if there's a recovered trip from crash
                    val app = application as? OutOfRouteApplication
                    val recoveredState = app?.let { OutOfRouteApplication.recoveredTripState }
                    
                    if (recoveredState != null && recoveredState.isActive) {
                        Log.i(TAG, "Restoring crashed trip: ${recoveredState.actualMiles} miles")
                        
                        // Restore the trip state
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isTripActive = true,
                                loadedMiles = recoveredState.loadedMiles,
                                bounceMiles = recoveredState.bounceMiles,
                                actualMiles = recoveredState.actualMiles,
                                tripStatusMessage = "⚠️ Trip recovered from crash",
                                showStatistics = true,
                                oorMiles = 0.0,
                                oorPercentage = 0.0,
                            )
                        }
                        
                        // Resume auto-save
                        startAutoSave()
                        
                        // Clear recovered state so it's not restored again
                        OutOfRouteApplication.clearRecoveredState()
                        
                    } else {
                        // Force trip to be inactive on initial load
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isTripActive = false,
                                tripStatusMessage = "",
                                showStatistics = false,
                                oorMiles = 0.0,
                                oorPercentage = 0.0,
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load initial data", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Observe trip state
         */
        private fun observeTripState() {
            viewModelScope.launch {
                try {
                    tripStateManager.tripState.collect { tripState ->
                        Log.d(TAG, "Trip state updated: $tripState")
                        updateUiWithTripState(tripState)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to observe trip state", e)
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Update UI with trip state
         */
        private fun updateUiWithTripState(tripState: TripStateManager.TripState) {
            _uiState.update { uiState ->
                uiState.copy(
                    isTripActive = tripState.isActive,
                    tripStatusMessage = "Trip status: ${if (tripState.isActive) "Active" else "Inactive"}",
                    showStatistics = !tripState.isActive,
                )
            }
        }

        /**
         * ✅ SIMPLIFIED: Observe location data using unified service
         */
        private fun observeLocationData() {
            viewModelScope.launch {
                try {
                    unifiedLocationService.realTimeGpsData.collect { gpsData ->
                        Log.d(TAG, "GPS data updated: distance=${gpsData.totalDistance}mi, accuracy=${gpsData.accuracy}m")

                        // Create GPS quality info
                        val gpsQualityInfo =
                            GpsQualityInfo(
                                accuracy = gpsData.accuracy.toFloat(),
                                signalStrength = 0,
                                satelliteCount = 0,
                                lastUpdate = System.currentTimeMillis(),
                            )

                        // ✅ FIX: During active trip, ONLY update Total Miles (actualMiles)
                        // OOR calculation should ONLY happen when trip ends
                        val state = _uiState.value
                        if (state.isTripActive) {
                            // Update ONLY actualMiles (Total Miles) with GPS data
                            // DO NOT calculate OOR during active trip
                            _uiState.update { currentState ->
                                currentState.copy(
                                    actualMiles = gpsData.totalDistance,
                                    tripStatusMessage = "Trip active - Distance: ${String.format(Locale.US, "%.1f", gpsData.totalDistance)}mi",
                                    gpsQuality = gpsQualityInfo,
                                )
                            }
                        } else {
                            // When trip is not active, just update GPS data
                            _uiState.update { currentState ->
                                currentState.copy(
                                    actualMiles = gpsData.totalDistance,
                                    gpsQuality = gpsQualityInfo,
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to observe GPS data", e)
                }
            }
        }

        /**
         * ✅ CRITICAL FIX: Observe GPS tracking data from TripTrackingService for real-time updates
         * 
         * 🐛 BUG FIXED: GPS service wasn't being started, so no real-time updates occurred
         * - BEFORE: Only calculated trip at start/end, no live GPS tracking
         * - AFTER:  Continuous GPS tracking with real-time UI updates
         * 
         * 🔧 WHY THIS WORKS:
         * 1. TripTrackingService.startService() initiates GPS tracking
         * 2. TripTrackingService.tripMetrics emits live distance updates
         * 3. This observer collects those updates and updates UI state
         * 4. UI displays "Total Miles" updating in real-time
         * 
         * 📊 DATA FLOW:
         * GPS → UnifiedLocationService → TripTrackingService → TripInputViewModel → UI
         * 
         * ✅ VERIFIED: Real-world testing confirmed live distance updates work
         */
        private fun observeGpsTrackingData() {
            viewModelScope.launch {
                try {
                    TripTrackingService.tripMetrics.collect { metrics ->
                        Log.d(TAG, "TripTrackingService metrics updated: totalMiles=${metrics.totalMiles}")

                        // Only update if trip is active
                        val state = _uiState.value
                        if (state.isTripActive) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    actualMiles = metrics.totalMiles, // ✅ Real-time GPS distance
                                    tripStatusMessage = "GPS tracking - Distance: ${String.format(Locale.US, "%.1f", metrics.totalMiles)}mi"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to observe TripTrackingService metrics", e)
                }
            }
        }

        // ==================== TRIP INPUT METHODS ====================

        /**
         * ✅ SIMPLIFIED: Calculate trip using unified service
         */
        fun calculateTrip(
            loadedMiles: Double,
            bounceMiles: Double,
            actualMiles: Double,
        ) {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Calculating trip: loaded=$loadedMiles, bounce=$bounceMiles, actual=$actualMiles")

                    // ✅ SIMPLIFIED: Use unified trip service for calculation
                    val calculationResult = unifiedTripService.calculateTrip(
                        loadedMiles = loadedMiles,
                        bounceMiles = bounceMiles,
                        actualMiles = actualMiles
                    )

                    if (!calculationResult.isValid) {
                        val userMessage = calculationResult.validationIssues.joinToString("; ")
                        Log.w(TAG, "Trip validation failed: $userMessage")
                        _events.emit(TripEvent.ValidationError(userMessage))
                        return@launch
                    }

                    // ✅ FIX: When starting trip, ONLY set loaded/bounce miles
                    // OOR should remain at 0 until trip ends
                    _uiState.update { currentState ->
                        currentState.copy(
                            isTripActive = true,
                            loadedMiles = loadedMiles,
                            bounceMiles = bounceMiles,
                            // DO NOT set OOR values when starting trip
                            oorMiles = 0.0,
                            oorPercentage = 0.0,
                            // Preserve provided actualMiles when non-zero; otherwise start at 0.0 and let GPS update
                            actualMiles = if (actualMiles > 0.0) actualMiles else 0.0,
                            tripStatusMessage = "Trip started successfully",
                            showStatistics = true,
                        )
                    }
                    
                    // ✅ CRITICAL FIX: Start GPS tracking service for real-time updates
                    // 
                    // 🐛 BUG FIXED: GPS service wasn't being started during trip calculation
                    // - BEFORE: Trip calculated once at start, no live tracking
                    // - AFTER:  Continuous GPS tracking throughout trip duration
                    // 
                    // 🔧 WHY THIS WORKS:
                    // 1. TripTrackingService runs as foreground service (survives app backgrounding)
                    // 2. UnifiedLocationService provides GPS updates to TripTrackingService
                    // 3. TripTrackingService.tripMetrics emits live distance updates
                    // 4. observeGpsTrackingData() collects updates and refreshes UI
                    // 
                    // 📊 SERVICE LIFECYCLE:
                    // Start → GPS Updates → Distance Calculation → UI Refresh → Stop
                    try {
                        TripTrackingService.startService(
                            context = application,
                            loadedMiles = loadedMiles,
                            bounceMiles = bounceMiles
                        )
                        Log.d(TAG, "GPS tracking service started")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start GPS tracking service", e)
                        _events.emit(TripEvent.Error("GPS tracking may not be available: ${e.message}"))
                    }
                    
                    // ✅ NEW (#12): Start auto-save when trip becomes active
                    startAutoSave()

                    // ✅ SIMPLIFIED: Save with offline fallback using unified service
                    val tripData = mapOf(
                        "loadedMiles" to loadedMiles,
                        "bounceMiles" to bounceMiles,
                        "actualMiles" to actualMiles,
                        "oorMiles" to calculationResult.oorMiles,
                        "oorPercentage" to calculationResult.oorPercentage
                    )

                    val saveResult = unifiedOfflineService.saveDataWithOfflineFallback(
                        data = tripData,
                        dataType = "trip"
                    ) {
                        // Online save function (simulated)
                        Log.d(TAG, "Saving trip online")
                        true // Simulate successful online save
                    }

                    Log.d(TAG, "Trip saved: $saveResult")
                    _events.emit(TripEvent.TripCalculated(calculationResult.oorMiles, calculationResult.oorPercentage))

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to calculate trip", e)
                    _uiState.update { it.copy(error = e.message) }
                    _events.emit(TripEvent.Error("Failed to calculate trip: ${e.message}"))
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: End the current trip and reset state
         */
    fun endTrip() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Ending trip")

                // Get current trip data
                val state = _uiState.value
                
                // ✅ FIX: Calculate OOR NOW when trip ends (not during active trip)
                val calculationResult = unifiedTripService.calculateTrip(
                    loadedMiles = state.loadedMiles,
                    bounceMiles = state.bounceMiles,
                    actualMiles = state.actualMiles
                )
                
                Log.d(TAG, "OOR Calculation Result: oorMiles=${calculationResult.oorMiles}, oorPercentage=${calculationResult.oorPercentage}")
                
                // ✅ CRITICAL FIX: Stop GPS tracking service to prevent battery drain
                // 
                // 🔧 WHY THIS WORKS:
                // 1. TripTrackingService runs as foreground service (battery intensive)
                // 2. Must be explicitly stopped when trip ends
                // 3. Prevents continuous GPS tracking after trip completion
                // 4. Clean service lifecycle: Start → Track → Stop
                // 
                // 📊 RESOURCE MANAGEMENT:
                // - Stops GPS location updates
                // - Removes foreground service notification
                // - Releases location manager resources
                // - Prevents background battery drain
                try {
                    TripTrackingService.stopService(application)
                    Log.d(TAG, "GPS tracking service stopped")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to stop GPS tracking service", e)
                }
                
                // ✅ FIX: Always update UI with OOR calculations first (before trying to save)
                _uiState.update { currentState ->
                    currentState.copy(
                        isTripActive = false,
                        oorMiles = calculationResult.oorMiles,
                        oorPercentage = calculationResult.oorPercentage,
                        tripStatusMessage = "Trip ended",
                        showStatistics = true,
                        error = null
                    )
                }
                
                // ✅ FIX: Only create Trip object if actualMiles is meaningful (>= 0.001)
                // For trips with 0.0 actualMiles, just mark as ended
                val tripData = if (state.actualMiles >= 0.001) {
                    try {
                        Trip(
                            id = "trip-${System.currentTimeMillis()}",
                            loadedMiles = state.loadedMiles,
                            bounceMiles = state.bounceMiles,
                            actualMiles = state.actualMiles,
                            oorMiles = calculationResult.oorMiles,
                            oorPercentage = calculationResult.oorPercentage,
                            startTime = Date(),
                            endTime = Date(),
                            status = TripStatus.COMPLETED
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not create Trip object (validation error), but OOR is still displayed: ${e.message}")
                        null
                    }
                } else {
                    // For trips with 0.0 actualMiles, don't create Trip object
                    null
                }

                // Save trip data (if trip object was created)
                val saveResult = if (tripData != null) {
                    unifiedOfflineService.saveDataWithOfflineFallback(
                        tripData,
                        "trip_data",
                        { true } // Simple online save function that always returns true
                    )
                } else {
                    // If no trip object, just mark as ended
                    "ended_without_save"
                }
                
                // Update status message based on save result
                _uiState.update { it.copy(
                    tripStatusMessage = if (saveResult == "success") "Trip saved!" else "Trip ended"
                ) }

                if (saveResult == "success") {
                    _events.emit(TripEvent.TripSaved)
                } else {
                    _events.emit(TripEvent.TripEnded)
                }
                
                // ✅ NEW (#12): Stop auto-save when trip ends
                stopAutoSave()

            } catch (e: Exception) {
                Log.e(TAG, "Error ending trip", e)
                _events.emit(TripEvent.CalculationError("Failed to end trip: ${e.message}"))
                
                // ✅ NEW (#12): Stop auto-save even on error
                stopAutoSave()
            }
        }
    }

    fun resetTrip() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Resetting trip for next trip")
                
                // Reset all trip-related values for the next trip
                _uiState.update { currentState ->
                    currentState.copy(
                        isTripActive = false,
                        tripStatusMessage = "Ready for new trip",
                        showStatistics = false,
                        loadedMiles = 0.0,
                        bounceMiles = 0.0,
                        actualMiles = 0.0,
                        oorMiles = 0.0,
                        oorPercentage = 0.0,
                        error = null
                    )
                }
                
                _events.emit(TripEvent.TripEnded)
                
                // ✅ NEW (#12): Stop auto-save when resetting
                stopAutoSave()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting trip", e)
                _events.emit(TripEvent.CalculationError("Failed to reset trip: ${e.message}"))
            }
        }
    }

        /**
         * ✅ SIMPLIFIED: Get current period mode from preferences
         */
        fun getCurrentPeriodMode(): PeriodMode {
            return preferencesManager.getPeriodMode()
        }

        /**
         * ✅ SIMPLIFIED: Save period mode to preferences
         */
        fun savePeriodMode(periodMode: PeriodMode) {
            preferencesManager.savePeriodMode(periodMode)
            Log.d(TAG, "Period mode saved: $periodMode")
        }

        /**
         * ✅ NEW: Get display text for current period mode
         */
        fun getCurrentPeriodModeDisplayText(): String {
            return when (preferencesManager.getPeriodMode()) {
                PeriodMode.STANDARD -> "Standard (Daily)"
                PeriodMode.CUSTOM -> "Custom Period"
            }
        }

        /**
         * ✅ NEW: Calculate period statistics for current period based on selected mode
         */
        fun calculateCurrentPeriodStatistics() {
            val currentPeriodMode = preferencesManager.getPeriodMode()
            Log.d(TAG, "Calculating current period statistics for mode: $currentPeriodMode")
            
            // ✅ NEW (#27): Use Dispatchers.IO for heavy calculations
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val periodCalculation = unifiedTripService.calculateCurrentPeriodStatistics(currentPeriodMode)
                    
                    // Update UI with period statistics
                    _uiState.update { currentState ->
                        currentState.copy(
                            periodStatistics = PeriodStatistics(
                                totalTrips = periodCalculation.totalTrips,
                                totalDistance = periodCalculation.totalDistance,
                                totalOorMiles = periodCalculation.totalOorMiles,
                                averageOorPercentage = periodCalculation.averageOorPercentage,
                                periodMode = currentPeriodMode,
                                startDate = periodCalculation.startDate,
                                endDate = periodCalculation.endDate
                            ),
                            showStatistics = true
                        )
                    }

                    Log.d(TAG, "Current period statistics calculated: ${periodCalculation.totalTrips} trips")
                    _events.emit(TripEvent.PeriodStatisticsCalculated(periodCalculation))

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to calculate current period statistics", e)
                    _uiState.update { it.copy(error = e.message) }
                    _events.emit(TripEvent.Error("Failed to calculate current period statistics: ${e.message}"))
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Calculate period statistics using unified service
         */
        fun calculatePeriodStatistics(
            periodMode: PeriodMode,
            startDate: Date,
            endDate: Date
        ) {
            // ✅ NEW (#27): Use Dispatchers.IO for heavy calculations
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    Log.d(TAG, "Calculating period statistics: $periodMode from $startDate to $endDate")

                    val periodCalculation = unifiedTripService.calculatePeriodStatistics(
                        periodMode = periodMode,
                        startDate = startDate,
                        endDate = endDate
                    )

                    // Update UI with period statistics
                    _uiState.update { currentState ->
                        currentState.copy(
                            periodStatistics = PeriodStatistics(
                                totalTrips = periodCalculation.totalTrips,
                                totalDistance = periodCalculation.totalDistance,
                                totalOorMiles = periodCalculation.totalOorMiles,
                                averageOorPercentage = periodCalculation.averageOorPercentage,
                                periodMode = periodMode,
                                startDate = startDate,
                                endDate = endDate
                            ),
                            showStatistics = true
                        )
                    }

                    Log.d(TAG, "Period statistics calculated: ${periodCalculation.totalTrips} trips")
                    _events.emit(TripEvent.PeriodStatisticsCalculated(periodCalculation))

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to calculate period statistics", e)
                    _uiState.update { it.copy(error = e.message) }
                    _events.emit(TripEvent.Error("Failed to calculate period statistics: ${e.message}"))
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Get location statistics using unified service
         */
        fun getLocationStatistics() {
            // ✅ NEW (#27): Use Dispatchers.IO for statistics gathering
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val locationStats = unifiedLocationService.getLocationStatistics()
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            locationStatistics = LocationStatistics(
                                totalDistance = locationStats.totalDistance,
                                averageSpeed = locationStats.averageSpeed,
                                maxSpeed = locationStats.maxSpeed,
                                currentSpeed = locationStats.currentSpeed,
                                accuracy = locationStats.accuracy,
                                locationCount = locationStats.locationCount,
                                validLocationCount = locationStats.validLocationCount,
                                tripDuration = locationStats.tripDuration,
                                isTracking = locationStats.isTracking
                            )
                        )
                    }

                    Log.d(TAG, "Location statistics updated: ${locationStats.totalDistance}mi total")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get location statistics", e)
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Get trip statistics using unified service
         */
        fun getTripStatistics() {
            // ✅ NEW (#27): Use Dispatchers.IO for statistics gathering
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val tripStats = unifiedTripService.getTripStatistics()
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            tripStatistics = TripStatistics(
                                totalTrips = tripStats.totalTrips,
                                totalDistance = tripStats.totalDistance,
                                totalOorMiles = tripStats.totalOorMiles,
                                averageDistance = tripStats.averageDistance,
                                averageOorPercentage = tripStats.averageOorPercentage,
                                isTracking = tripStats.isTracking,
                                lastTripEndTime = tripStats.lastTripEndTime
                            )
                        )
                    }

                    Log.d(TAG, "Trip statistics updated: ${tripStats.totalTrips} trips")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get trip statistics", e)
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Get offline statistics using unified service
         */
        fun getOfflineStatistics() {
            // ✅ NEW (#27): Use Dispatchers.IO for statistics gathering
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val offlineStats = unifiedOfflineService.getOfflineStatistics()
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            offlineStatistics = OfflineStatistics(
                                pendingTrips = offlineStats.pendingTrips,
                                pendingAnalytics = offlineStats.pendingAnalytics,
                                lastSyncTime = offlineStats.lastSyncTime,
                                isOffline = offlineStats.isOffline,
                                storageSize = offlineStats.storageSize
                            )
                        )
                    }

                    Log.d(TAG, "Offline statistics updated: ${offlineStats.pendingTrips} pending trips")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get offline statistics", e)
                }
            }
        }

        // ==================== HELPER METHODS ====================

        /**
         * ✅ SIMPLIFIED: Get user-friendly validation message
         */
        private fun getUserFriendlyValidationMessage(validationResult: ValidationFramework.ValidationResult): String {
            return validationResult.errors.joinToString("; ") { it.message }
        }

        // ==================== DATA CLASSES ====================

        data class TripInputUiState(
            val isLoading: Boolean = true,
            val isTripActive: Boolean = false,
            val tripStatusMessage: String = "",
            val showStatistics: Boolean = false,
            val loadedMiles: Double = 0.0,
            val bounceMiles: Double = 0.0,
            val oorMiles: Double = 0.0,
            val oorPercentage: Double = 0.0,
            val actualMiles: Double = 0.0,
            val gpsQuality: GpsQualityInfo? = null,
            val error: String? = null,
            val periodStatistics: PeriodStatistics? = null,
            val locationStatistics: LocationStatistics? = null,
            val tripStatistics: TripStatistics? = null,
            val offlineStatistics: OfflineStatistics? = null,
        )

        data class GpsQualityInfo(
            val accuracy: Float,
            val signalStrength: Int,
            val satelliteCount: Int,
            val lastUpdate: Long,
        )

        data class PeriodStatistics(
            val totalTrips: Int,
            val totalDistance: Double,
            val totalOorMiles: Double,
            val averageOorPercentage: Double,
            val periodMode: PeriodMode,
            val startDate: Date,
            val endDate: Date
        )

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

        data class TripStatistics(
            val totalTrips: Int,
            val totalDistance: Double,
            val totalOorMiles: Double,
            val averageDistance: Double,
            val averageOorPercentage: Double,
            val isTracking: Boolean,
            val lastTripEndTime: Date?
        )

        data class OfflineStatistics(
            val pendingTrips: Int,
            val pendingAnalytics: Int,
            val lastSyncTime: Long?,
            val isOffline: Boolean,
            val storageSize: Long
        )

        sealed class TripEvent {
            data class TripCalculated(val oorMiles: Double, val oorPercentage: Double) : TripEvent()
            data class PeriodStatisticsCalculated(val periodCalculation: UnifiedTripService.PeriodCalculation) : TripEvent()
            data class ValidationError(val message: String) : TripEvent()
            data class Error(val message: String) : TripEvent()
            object TripSaved : TripEvent()
            object TripEnded : TripEvent()
            data class SaveError(val message: String) : TripEvent()
            data class CalculationError(val message: String) : TripEvent()
        }
        
        // ==================== CRASH RECOVERY (#12) ====================
        
        /**
         * ✅ NEW (#12): Start auto-save timer for crash recovery
         */
        private fun startAutoSave() {
            crashRecoveryManager.startAutoSave {
                val state = _uiState.value
                TripCrashRecoveryManager.RecoverableTripState(
                    loadedMiles = state.loadedMiles,
                    bounceMiles = state.bounceMiles,
                    actualMiles = state.actualMiles,
                    startTime = System.currentTimeMillis(),
                    isActive = state.isTripActive,
                    currentPeriodMode = getCurrentPeriodMode().name,
                    gpsTrackingActive = state.gpsQuality != null
                )
            }
            Log.d(TAG, "Auto-save started for crash recovery")
        }
        
        /**
         * ✅ NEW (#12): Stop auto-save timer
         */
        private fun stopAutoSave() {
            crashRecoveryManager.stopAutoSave()
            crashRecoveryManager.clearRecoveryData()
            Log.d(TAG, "Auto-save stopped and recovery data cleared")
        }
    } 
