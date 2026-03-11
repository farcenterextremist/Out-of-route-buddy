package com.example.outofroutebuddy.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.services.BackgroundSyncService
import com.example.outofroutebuddy.services.DriveState
import com.example.outofroutebuddy.services.OptimizedGpsDataFlow
import com.example.outofroutebuddy.services.TripCrashRecoveryManager
import com.example.outofroutebuddy.services.TripEndedOverlayService
import com.example.outofroutebuddy.services.TripTrackingService
import com.example.outofroutebuddy.services.UnifiedLocationService
import com.example.outofroutebuddy.services.UnifiedOfflineService
import com.example.outofroutebuddy.services.UnifiedTripService
import com.example.outofroutebuddy.di.IoDispatcher
import com.example.outofroutebuddy.validation.ValidationFramework
import com.example.outofroutebuddy.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import com.example.outofroutebuddy.utils.extensions.startOfDay

/**
 * ViewModel for trip input, GPS tracking, and persistence. Main-safe; uses [viewModelScope] and [IoDispatcher] for I/O.
 *
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
        private val tripPersistenceManager: TripPersistenceManager,
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        companion object {
            private const val TAG = "TripInputViewModel"
            private const val DEFAULT_PERIOD_LABEL = "N/A"
            private const val MIN_PERSISTED_ACTUAL_MILES = 0.001
            private val PERIOD_LABEL_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.US)
        }

        // ✅ SIMPLIFIED: UI State
        private val _uiState = MutableStateFlow(TripInputUiState())
        val uiState: StateFlow<TripInputUiState> = _uiState.asStateFlow()

        // ✅ SIMPLIFIED: Events
        private val _events = MutableSharedFlow<TripEvent>()
        val events: SharedFlow<TripEvent> = _events.asSharedFlow()

        // Current trip data
        private var currentTrip: Trip? = null
        
        // ✅ FIX: Track if we've loaded persisted state to prevent TripStateManager from overriding it
        private var hasLoadedPersistedState = false
        private val endTripInProgress = AtomicBoolean(false)

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
                    AppLogger.d(TAG, "Loading initial data")

                    // ✅ NEW (#12): Check if there's a recovered trip from crash
                    val app = application as? OutOfRouteApplication
                    val recoveredState = app?.let { OutOfRouteApplication.recoveredTripState }
                    
                    if (recoveredState != null && recoveredState.isActive) {
                        AppLogger.d(TAG, "Restoring recovered trip state")
                        
                        // Restore the trip state
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isTripActive = true,
                                loadedMiles = recoveredState.loadedMiles,
                                bounceMiles = recoveredState.bounceMiles,
                                actualMiles = recoveredState.actualMiles,
                                tripStatusMessage = "Trip recovered after app interruption",
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
                        // ✅ FIX: Check for persisted state from previous session
                        AppLogger.d(TAG, "Checking for persisted trip state")
                        val persistedState = tripPersistenceManager.loadSavedTripState()
                        if (persistedState != null) {
                            // ✅ FIX: If persisted state exists, it means an active trip was saved
                            // (We only persist active trips; completed trips clear persistence)
                            // So treat it as active regardless of the saved status enum value
                            // (in case of deserialization issues with the enum)
                            val isActive = true // Persisted state always indicates an active trip
                            AppLogger.d(TAG, "Found persisted trip state; resuming active trip")
                            
                            // ✅ FIX: Mark that we've loaded persisted state to prevent TripStateManager from overriding it
                            hasLoadedPersistedState = true
                            
                            // ✅ FIX: Sync TripStateManager with persisted state so observer doesn't override it
                            // TripStateManager needs to know about the active trip so it doesn't emit inactive state
                            try {
                                val loadedMilesStr = persistedState.loadedMiles.toString()
                                val bounceMilesStr = persistedState.bounceMiles.toString()
                                val startSuccess = tripStateManager.startTrip(loadedMilesStr, bounceMilesStr)
                                if (startSuccess) {
                                    Log.d(TAG, "TripStateManager synced with persisted state")
                                } else {
                                    Log.w(TAG, "Failed to sync TripStateManager with persisted state")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error syncing TripStateManager with persisted state", e)
                            }
                            
                            // Restore current trip object
                            currentTrip = persistedState.trip
                            
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    isTripActive = isActive,
                                    loadedMiles = persistedState.loadedMiles,
                                    bounceMiles = persistedState.bounceMiles,
                                    actualMiles = persistedState.actualMiles,
                                    oorMiles = 0.0, // OOR is recalculated when trip ends
                                    oorPercentage = 0.0,
                                    tripStatusMessage = if (isActive) "Active trip resumed" else "",
                                    showStatistics = true,
                                )
                            }
                            
                            // Resume auto-save if trip is active
                            if (isActive) {
                                startAutoSave()
                            }
                        } else {
                            Log.d(TAG, "No persisted state found - trip is inactive")
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
                    }
                    refreshAggregateStatistics()
                    initializeSelectedPeriodFromPreferences()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load initial data", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }

        /** Load only monthly aggregate statistics (weekly/yearly removed per backend). */
        private fun refreshAggregateStatistics() {
            viewModelScope.launch(ioDispatcher) {
                try {
                    val monthlyStats = tripRepository.getMonthlyTripStatistics()
                    withContext(Dispatchers.Main) {
                        _uiState.update { currentState ->
                            currentState.copy(monthlyStatistics = mapToSummary(monthlyStats))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh aggregate statistics", e)
                }
            }
        }

        private fun initializeSelectedPeriodFromPreferences() {
            try {
                val periodMode = preferencesManager.getPeriodMode()
                val (startDate, endDate) = unifiedTripService.getCurrentPeriodDates(periodMode)
                updatePeriodStatistics(periodMode, startDate, endDate, emitEvent = false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize selected period", e)
            }
        }

        private fun updatePeriodStatistics(
            periodMode: PeriodMode,
            startDate: Date,
            endDate: Date,
            emitEvent: Boolean
        ) {
            viewModelScope.launch(ioDispatcher) {
                try {
                    val stats = tripRepository.getTripStatistics(startDate, endDate)
                    val trips = try {
                        tripRepository.getTripsByDateRange(startDate, endDate).first()
                    } catch (ex: Exception) {
                        Log.w(TAG, "Failed to load trips for period stats", ex)
                        emptyList()
                    }
                    // Unique dates (start of day) that have at least one saved trip; includes midnight-spanning days
                    val datesWithTrips = computeDatesWithTrips(trips)

                    // Year statistics: Jan 1 - Dec 31 of the period's year
                    val (yearStart, yearEnd) = getYearRangeForSelectedPeriod(startDate, endDate)
                    val yearStats = tripRepository.getTripStatistics(yearStart, yearEnd)
                    val yearPeriodStats = PeriodStatistics(
                        totalTrips = yearStats.totalTrips,
                        totalDistance = yearStats.totalActualMiles,
                        totalOorMiles = yearStats.totalOorMiles,
                        averageOorPercentage = yearStats.avgOorPercentage,
                        periodMode = periodMode,
                        startDate = yearStart,
                        endDate = yearEnd
                    )

                    val periodCalculation = UnifiedTripService.PeriodCalculation(
                        periodMode = periodMode,
                        startDate = startDate,
                        endDate = endDate,
                        totalTrips = stats.totalTrips,
                        totalDistance = stats.totalActualMiles,
                        totalOorMiles = stats.totalOorMiles,
                        averageOorPercentage = stats.avgOorPercentage,
                        trips = trips
                    )

                    val label = formatPeriodLabel(periodMode, startDate, endDate)

                    withContext(Dispatchers.Main) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                periodStatistics = if (currentState.isPeriodSectionCleared) {
                                    null
                                } else {
                                    PeriodStatistics(
                                        totalTrips = stats.totalTrips,
                                        totalDistance = stats.totalActualMiles,
                                        totalOorMiles = stats.totalOorMiles,
                                        averageOorPercentage = stats.avgOorPercentage,
                                        periodMode = periodMode,
                                        startDate = startDate,
                                        endDate = endDate
                                    )
                                },
                                yearStatistics = if (currentState.isYearSectionCleared) null else yearPeriodStats,
                                selectedPeriod = SelectedPeriod(
                                    startDate = startDate,
                                    endDate = endDate,
                                    periodMode = periodMode,
                                    label = label
                                ),
                                selectedPeriodLabel = label,
                                datesWithTripsInPeriod = datesWithTrips,
                                showStatistics = true
                            )
                        }
                    }

                    if (emitEvent) {
                        _events.emit(TripEvent.PeriodStatisticsCalculated(periodCalculation))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update period statistics", e)
                    if (emitEvent) {
                        _events.emit(TripEvent.Error("Failed to calculate period statistics: ${e.message}"))
                    }
                }
            }
        }

        /**
         * Computes unique start-of-day dates that have at least one trip.
         * For midnight-spanning trips (startTime and endTime on different days), includes all days in range.
         */
        private fun computeDatesWithTrips(trips: List<Trip>): List<Date> {
            val allDates = mutableSetOf<Long>()
            for (trip in trips) {
                val start = trip.startTime ?: trip.endTime
                val end = trip.endTime ?: trip.startTime
                if (start == null) continue
                val cal = Calendar.getInstance()
                cal.time = start
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startDayMillis = cal.timeInMillis
                if (end != null && end.time > start.time) {
                    val endCal = Calendar.getInstance()
                    endCal.time = end
                    endCal.set(Calendar.HOUR_OF_DAY, 0)
                    endCal.set(Calendar.MINUTE, 0)
                    endCal.set(Calendar.SECOND, 0)
                    endCal.set(Calendar.MILLISECOND, 0)
                    val iter = Calendar.getInstance().apply { timeInMillis = startDayMillis }
                    while (iter.timeInMillis <= endCal.timeInMillis) {
                        allDates.add(iter.timeInMillis)
                        iter.add(Calendar.DAY_OF_MONTH, 1)
                    }
                } else {
                    allDates.add(startDayMillis)
                }
            }
            return allDates.map { Date(it) }.sortedBy { it.time }
        }

        /**
         * Year section is anchored to period end date so custom ranges crossing New Year
         * show totals for the year the selected period ends in.
         */
        internal fun getYearRangeForSelectedPeriod(startDate: Date, endDate: Date): Pair<Date, Date> {
            return getYearRange(endDate)
        }

        /** Returns (Jan 1 00:00:00, Dec 31 23:59:59.999) for the year containing the given date. Internal for testing. */
        internal fun getYearRange(date: Date): Pair<Date, Date> {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val yearStart = cal.time
            cal.set(Calendar.MONTH, Calendar.DECEMBER)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val yearEnd = cal.time
            return yearStart to yearEnd
        }

        private fun mapToSummary(stats: com.example.outofroutebuddy.domain.repository.TripStatistics): SummaryStatistics {
            return SummaryStatistics(
                totalTrips = stats.totalTrips,
                totalMiles = stats.totalActualMiles,
                oorMiles = stats.totalOorMiles,
                oorPercentage = stats.avgOorPercentage,
                extraFields = emptyMap()
            )
        }

        /**
         * Maps PeriodStatistics to SummaryStatistics for the stats row display.
         * Used when stats row shows selected period (not current month).
         * Add optional metrics via extraFields when needed.
         */
        fun mapPeriodToSummary(period: PeriodStatistics?): SummaryStatistics? {
            if (period == null) return null
            return SummaryStatistics(
                totalTrips = period.totalTrips,
                totalMiles = period.totalDistance,
                oorMiles = period.totalOorMiles,
                oorPercentage = period.averageOorPercentage,
                extraFields = emptyMap()
            )
        }

        private fun formatPeriodLabel(periodMode: PeriodMode, startDate: Date, endDate: Date): String {
            return when (periodMode) {
                PeriodMode.STANDARD -> "${PERIOD_LABEL_FORMAT.format(startDate)} – ${PERIOD_LABEL_FORMAT.format(endDate)}"
                PeriodMode.CUSTOM -> "${PERIOD_LABEL_FORMAT.format(startDate)} - ${PERIOD_LABEL_FORMAT.format(endDate)}"
            }
        }

        private fun refreshSelectedPeriod() {
            val selected = _uiState.value.selectedPeriod ?: return
            updatePeriodStatistics(selected.periodMode, selected.startDate, selected.endDate, emitEvent = false)
        }

        /**
         * Returns true when the currently selected stats period should roll forward to the current cycle.
         * This keeps Month/Year rows aligned with natural month/year boundaries once the selected period ends.
         */
        internal fun shouldResetToCurrentPeriod(
            selectedPeriod: SelectedPeriod?,
            currentMode: PeriodMode,
            now: Date = Date(),
        ): Boolean {
            if (selectedPeriod == null) return true
            if (selectedPeriod.periodMode != currentMode) return true
            return now.before(selectedPeriod.startDate) || now.after(selectedPeriod.endDate)
        }

        /**
         * Resolve an effective selected period:
         * - Keep the current selection if it still contains "now" and uses the active mode.
         * - Otherwise, roll to the current period for the active mode.
         */
        private fun resolveEffectiveSelectedPeriod(selectedPeriod: SelectedPeriod?): SelectedPeriod {
            val currentMode = preferencesManager.getPeriodMode()
            if (!shouldResetToCurrentPeriod(selectedPeriod, currentMode)) {
                return selectedPeriod!!
            }

            val (currentStart, currentEnd) = unifiedTripService.getCurrentPeriodDates(currentMode)
            return SelectedPeriod(
                startDate = currentStart,
                endDate = currentEnd,
                periodMode = currentMode,
                label = formatPeriodLabel(currentMode, currentStart, currentEnd),
            )
        }

        /**
         * Runs both monthly and period refresh in one coroutine so the UI sees a consistent
         * state after a trip is saved (monthly statistics + days with trips + calendar data).
         * periodStatistics and yearStatistics are always updated together so the stats rows
         * (monthly/yearly) never show one with data and the other with placeholder.
         */
        private suspend fun refreshStatisticsAfterSave(selectedPeriod: SelectedPeriod?) {
            withContext(ioDispatcher) {
                try {
                    val effectivePeriod = resolveEffectiveSelectedPeriod(selectedPeriod)
                    val monthlyStats = tripRepository.getMonthlyTripStatistics()
                    val stats = tripRepository.getTripStatistics(effectivePeriod.startDate, effectivePeriod.endDate)
                    val trips = try {
                        tripRepository.getTripsByDateRange(effectivePeriod.startDate, effectivePeriod.endDate).first()
                    } catch (ex: Exception) {
                        Log.w(TAG, "Failed to load trips for effective period stats", ex)
                        emptyList()
                    }
                    val datesWithTrips = computeDatesWithTrips(trips)
                    val (yearStart, yearEnd) = getYearRangeForSelectedPeriod(effectivePeriod.startDate, effectivePeriod.endDate)
                    val yearStats = tripRepository.getTripStatistics(yearStart, yearEnd)
                    val yearPeriodStats = PeriodStatistics(
                        totalTrips = yearStats.totalTrips,
                        totalDistance = yearStats.totalActualMiles,
                        totalOorMiles = yearStats.totalOorMiles,
                        averageOorPercentage = yearStats.avgOorPercentage,
                        periodMode = effectivePeriod.periodMode,
                        startDate = yearStart,
                        endDate = yearEnd
                    )
                    val label = formatPeriodLabel(effectivePeriod.periodMode, effectivePeriod.startDate, effectivePeriod.endDate)
                    withContext(Dispatchers.Main) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                monthlyStatistics = mapToSummary(monthlyStats),
                                periodStatistics = if (currentState.isPeriodSectionCleared) {
                                    null
                                } else {
                                    PeriodStatistics(
                                        totalTrips = stats.totalTrips,
                                        totalDistance = stats.totalActualMiles,
                                        totalOorMiles = stats.totalOorMiles,
                                        averageOorPercentage = stats.avgOorPercentage,
                                        periodMode = effectivePeriod.periodMode,
                                        startDate = effectivePeriod.startDate,
                                        endDate = effectivePeriod.endDate
                                    )
                                },
                                yearStatistics = if (currentState.isYearSectionCleared) null else yearPeriodStats,
                                selectedPeriod = SelectedPeriod(
                                    startDate = effectivePeriod.startDate,
                                    endDate = effectivePeriod.endDate,
                                    periodMode = effectivePeriod.periodMode,
                                    label = label
                                ),
                                selectedPeriodLabel = label,
                                datesWithTripsInPeriod = datesWithTrips,
                                showStatistics = true
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh statistics after save", e)
                    _events.emit(TripEvent.Error("Failed to refresh trip statistics after save"))
                }
            }
        }

        /**
         * Ensures selected period is still in-cycle (month/year rollover aware) and refreshes stats.
         * Safe to call from UI lifecycle events (e.g., onResume).
         */
        fun ensureStatisticsPeriodIsCurrentCycle() {
            viewModelScope.launch {
                refreshStatisticsAfterSave(_uiState.value.selectedPeriod)
            }
        }

        /**
         * Refreshes period statistics and [datesWithTripsInPeriod] so the calendar dialog
         * shows the latest saved trips. Call from the UI before opening the calendar picker.
         */
        suspend fun refreshPeriodForCalendar() {
            val selected = _uiState.value.selectedPeriod
            refreshStatisticsAfterSave(selected)
        }

        /**
         * Returns distinct start-of-day dates that have at least one saved trip in [minDate]..[maxDate].
         * Uses the same midnight-spanning logic as [computeDatesWithTrips]: each trip contributes every
         * calendar day from start to end, so both days show the yellow circle for midnight-spanning trips.
         * Used so the calendar dialog can show trip dots across the full ±1 year range.
         */
        suspend fun getDatesWithTripsForCalendarRange(minDate: Date, maxDate: Date): List<Date> = withContext(ioDispatcher) {
            val minStart = minDate.startOfDay().time
            val maxStart = maxDate.startOfDay().time
            // Fetch trips in a slightly expanded range so we include trips that span across the boundary
            val expandedMin = java.util.Calendar.getInstance().apply { time = minDate; add(java.util.Calendar.DAY_OF_MONTH, -1) }.time
            val expandedMax = java.util.Calendar.getInstance().apply { time = maxDate; add(java.util.Calendar.DAY_OF_MONTH, 1) }.time
            val trips = try {
                tripRepository.getTripsByDateRange(expandedMin, expandedMax).first()
            } catch (e: Exception) {
                Log.w(TAG, "getDatesWithTripsForCalendarRange failed", e)
                emptyList()
            }
            val allDatesWithTrips = computeDatesWithTrips(trips)
            allDatesWithTrips.filter { it.time in minStart..maxStart }.distinctBy { it.time }.sortedBy { it.time }
        }

        /**
         * ✅ SIMPLIFIED: Observe trip state
         */
        private fun observeTripState() {
            viewModelScope.launch {
                try {
                    tripStateManager.tripState.collect { tripState ->
                        Log.v(TAG, "Trip state updated: $tripState")
                        updateUiWithTripState(tripState)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to observe trip state", e)
                }
            }
        }

        /**
         * ✅ SIMPLIFIED: Update UI with trip state
         * ✅ FIX: Don't override isTripActive if we've already loaded persisted state (which indicates an active trip)
         */
        private fun updateUiWithTripState(tripState: TripStateManager.TripState) {
            _uiState.update { uiState ->
                // ✅ FIX: If we loaded persisted state (active trip), don't let TripStateManager override it
                // TripStateManager always starts inactive, but persisted state indicates an active trip
                val shouldPreserveActiveState = hasLoadedPersistedState && uiState.isTripActive
                val isTripActive = if (shouldPreserveActiveState) {
                    Log.d(TAG, "Preserving active state from persisted trip (TripStateManager would override to inactive)")
                    true
                } else {
                    tripState.isActive
                }
                
                uiState.copy(
                    isTripActive = isTripActive,
                    tripStatusMessage = "Trip status: ${if (isTripActive) "Active" else "Inactive"}",
                    showStatistics = !isTripActive,
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
                        Log.v(TAG, "GPS data updated: distance=${gpsData.totalDistance}mi, accuracy=${gpsData.accuracy}m")

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
                    combine(
                        TripTrackingService.tripMetrics,
                        TripTrackingService.driveState
                    ) { metrics, driveState ->
                        Pair(metrics, driveState)
                    }.collect { (metrics, driveState) ->
                        Log.v(TAG, "TripTrackingService metrics updated: totalMiles=${metrics.totalMiles}, driveState=$driveState")

                        // Only update if trip is active
                        val state = _uiState.value
                        if (state.isTripActive) {
                            val statusMessage = when {
                                state.isPaused -> "Trip tracking paused - distance accumulation stopped"
                                driveState == DriveState.WALKING_OR_STATIONARY ->
                                    "Not counting: walking/stopped detected - Distance: ${String.format(Locale.US, "%.1f", metrics.totalMiles)} mi"
                                else ->
                                    "GPS tracking - Distance: ${String.format(Locale.US, "%.1f", metrics.totalMiles)} mi"
                            }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    actualMiles = metrics.totalMiles,
                                    tripStatusMessage = statusMessage
                                )
                            }
                            
                            // ✅ NEW: Update trip progress for persistence
                            updateTripProgress(metrics.totalMiles)
                            
                            // ✅ FIX: Persist trip state every time GPS updates
                            saveTripStateForPersistence()
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
                    // H2: Do not start trip if app (database) is unhealthy. Treat non–OutOfRouteApplication (e.g. test) as healthy.
                    if ((application as? OutOfRouteApplication)?.isHealthy() == false) {
                        _events.emit(TripEvent.Error(application.getString(R.string.database_unavailable)))
                        return@launch
                    }
                    AppLogger.d(TAG, "Calculating trip")

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
                    
                    // ✅ FIX: Create trip object for persistence
                    currentTrip = Trip(
                        id = "trip-${System.currentTimeMillis()}",
                        loadedMiles = loadedMiles,
                        bounceMiles = bounceMiles,
                        actualMiles = if (actualMiles > 0.0) actualMiles else 0.0,
                        oorMiles = 0.0,
                        oorPercentage = 0.0,
                        startTime = Date(),
                        endTime = null,
                        status = TripStatus.ACTIVE
                    )
                    AppLogger.d(TAG, "Active trip object created")

                    // Keep TripStateManager in sync with ViewModel start flow so persistence and metadata
                    // are sourced from the same active trip state.
                    try {
                        val synced = tripStateManager.startTrip(loadedMiles.toString(), bounceMiles.toString())
                        if (!synced) {
                            Log.w(TAG, "TripStateManager rejected start sync for loaded/bounce values")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync TripStateManager on trip start", e)
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
                        if (!TripTrackingService.canShowTripNotifications(application)) {
                            _events.emit(
                                TripEvent.Error(
                                    "Trip started, but trip notifications are blocked. Enable notifications for OutOfRouteBuddy to see 'Trip in progress' in pull-down."
                                )
                            )
                        }
                        TripTrackingService.startService(
                            context = application,
                            loadedMiles = loadedMiles,
                            bounceMiles = bounceMiles
                        )
                        TripEndedOverlayService.startWhenTripActive(application)
                        Log.d(TAG, "GPS tracking service started")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start GPS tracking service", e)
                        _events.emit(TripEvent.Error("GPS tracking may not be available: ${e.message}"))
                    }
                    
                    // ✅ NEW (#12): Start auto-save when trip becomes active
                    startAutoSave()
                    
                    // ✅ FIX: Save loaded/bounce miles to preferences for persistence
                    preferencesManager.saveLastLoadedMiles(loadedMiles.toString())
                    preferencesManager.saveLastBounceMiles(bounceMiles.toString())
                    
                    // ✅ NEW: Save trip state for persistence IMMEDIATELY
                    AppLogger.v(TAG, "About to save trip state for persistence")
                    saveTripStateForPersistence()
                    AppLogger.v(TAG, "Trip state save completed")
                    Log.d(TAG, "Trip started: loaded=${loadedMiles}mi, bounce=${bounceMiles}mi → GPS tracking active")

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

                    AppLogger.d(TAG, "Trip save flow completed with result: $saveResult")
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
            if (!endTripInProgress.compareAndSet(false, true)) {
                Log.w(TAG, "Ignoring duplicate endTrip invocation while another end flow is running")
                return@launch
            }
            try {
                AppLogger.d(TAG, "Ending trip")

                // Get current trip data
                val state = _uiState.value
                
                // ✅ FIX: Calculate OOR NOW when trip ends (not during active trip)
                val calculationResult = unifiedTripService.calculateTrip(
                    loadedMiles = state.loadedMiles,
                    bounceMiles = state.bounceMiles,
                    actualMiles = state.actualMiles
                )
                
                AppLogger.d(TAG, "Trip end calculation completed")
                
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
                TripEndedOverlayService.dismissBubble(application)
                // Resets overlay detector so next trip can show bubble again (Health & resilience).
                TripEndedOverlayService.notifyTripEndedFromInApp(application)

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
                
                // Trip date used for period grouping = end date (when user taps End Trip).
                // DomainTripRepositoryAdapter maps trip.startTime to TripEntity.date for DB storage.
                val tripEndDate = Date()
                // C1 (R1): Persist completed trip with GPS metadata via saveCompletedTrip (single insert path).
                val saveResult = try {
                    // Persist very short trips as a tiny floor value so they are still saved/viewable/deletable.
                    val persistedActualMiles = state.actualMiles.coerceAtLeast(MIN_PERSISTED_ACTUAL_MILES)
                    val tripStartDate = currentTrip?.startTime ?: tripEndDate
                    val tripId = tripStatePersistence.saveCompletedTrip(
                        actualMiles = persistedActualMiles,
                        loadedMiles = state.loadedMiles,
                        bounceMiles = state.bounceMiles,
                        tripStartTime = tripStartDate,
                        tripEndTime = tripEndDate,
                    )
                    AppLogger.d(TAG, "Trip saved via saveCompletedTrip")
                    currentTrip = Trip(
                        id = "trip-$tripId",
                        loadedMiles = state.loadedMiles,
                        bounceMiles = state.bounceMiles,
                        actualMiles = persistedActualMiles,
                        oorMiles = calculationResult.oorMiles,
                        oorPercentage = calculationResult.oorPercentage,
                        startTime = tripStartDate,
                        endTime = tripEndDate,
                        status = TripStatus.COMPLETED
                    )
                    "success"
                } catch (e: Exception) {
                    AppLogger.e(TAG, "saveCompletedTrip failed", e)
                    _events.emit(TripEvent.SaveError(application.getString(R.string.database_unavailable)))
                    "failed"
                }
                
                // Update status message based on save result
                _uiState.update { it.copy(
                    tripStatusMessage = if (saveResult == "success") "Trip saved!" else "Trip ended"
                ) }
                
                // ✅ NEW (#12): Stop auto-save when trip ends
                stopAutoSave(clearRecoveryState = saveResult == "success")

                // Keep trip state manager lifecycle aligned with end-trip completion.
                try {
                    tripStateManager.endTrip()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to end TripStateManager state", e)
                }
                
                // ✅ NEW: Clear trip persistence only when save succeeds.
                // Keeping persistence on save failure protects crash/restart recovery.
                if (saveResult == "success") {
                    clearTripPersistence()
                } else {
                    Log.w(TAG, "Trip save failed; preserving persisted active-state data for recovery")
                }

                // ✅ FIX: Refresh monthly + period/year stats synchronously in this flow so
                // month/year fields update immediately after End Trip (no app restart needed).
                val selected = _uiState.value.selectedPeriod
                refreshStatisticsAfterSave(selected)

                if (saveResult == "success") {
                    _events.emit(TripEvent.TripSaved)
                } else {
                    _events.emit(TripEvent.TripEnded)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ending trip", e)
                _events.emit(TripEvent.CalculationError("Failed to end trip: ${e.message}"))
                
                // ✅ NEW (#12): Stop auto-save even on error
                stopAutoSave(clearRecoveryState = false)
            } finally {
                endTripInProgress.set(false)
            }
        }
    }
    
    /**
     * ✅ NEW: Pause trip tracking (for when user steps out of vehicle)
     */
    fun pauseTrip() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Pausing trip tracking")
                
                // ✅ CRITICAL FIX: Pause the GPS tracking service first
                // This stops distance accumulation at the service level
                TripTrackingService.pauseService(application)
                
                // Update UI state to show paused
                _uiState.update { currentState ->
                    currentState.copy(
                        isPaused = true,
                        tripStatusMessage = "Trip tracking paused - distance accumulation stopped"
                    )
                }
                
                Log.d(TAG, "Trip paused successfully - service notified")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing trip", e)
                _events.emit(TripEvent.CalculationError("Failed to pause trip: ${e.message}"))
            }
        }
    }
    
    /**
     * ✅ NEW: Resume trip tracking
     */
    fun resumeTrip() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Resuming trip tracking")
                
                // ✅ CRITICAL FIX: Resume the GPS tracking service first
                // This resumes distance accumulation at the service level
                TripTrackingService.resumeService(application)
                
                // Update UI state to show active
                _uiState.update { currentState ->
                    currentState.copy(
                        isPaused = false,
                        tripStatusMessage = "Trip tracking active - distance accumulating"
                    )
                }
                
                Log.d(TAG, "Trip resumed successfully - service notified")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming trip", e)
                _events.emit(TripEvent.CalculationError("Failed to resume trip: ${e.message}"))
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
            calculateCurrentPeriodStatistics()
        }

        /**
         * ✅ NEW: Get display text for current period mode
         */
        fun getCurrentPeriodModeDisplayText(): String {
            return when (preferencesManager.getPeriodMode()) {
                PeriodMode.STANDARD -> "Standard (1st–last of month)"
                PeriodMode.CUSTOM -> "Custom (Thu before first Fri)"
            }
        }

        /**
         * Option A behavior: clear Month section display only.
         * This is non-destructive and does not delete any persisted trips.
         */
        fun clearSelectedPeriodSectionDisplay() {
            _uiState.update { currentState ->
                currentState.copy(
                    periodStatistics = null,
                    isPeriodSectionCleared = true
                )
            }
        }

        /**
         * Option A behavior: clear Year section display only.
         * This is non-destructive and does not delete any persisted trips.
         */
        fun clearYearSectionDisplay() {
            _uiState.update { currentState ->
                currentState.copy(
                    yearStatistics = null,
                    isYearSectionCleared = true
                )
            }
        }

        /**
         * ✅ NEW: Calculate period statistics for current period based on selected mode
         */
        fun calculateCurrentPeriodStatistics() {
            val currentPeriodMode = preferencesManager.getPeriodMode()
            Log.d(TAG, "Calculating current period statistics for mode: $currentPeriodMode")
            val (startDate, endDate) = unifiedTripService.getCurrentPeriodDates(currentPeriodMode)
            updatePeriodStatistics(currentPeriodMode, startDate, endDate, emitEvent = true)
        }

        /**
         * ✅ SIMPLIFIED: Calculate period statistics using unified service
         */
        fun calculatePeriodStatistics(
            periodMode: PeriodMode,
            startDate: Date,
            endDate: Date
        ) {
            Log.d(TAG, "Calculating period statistics: $periodMode from $startDate to $endDate")
            updatePeriodStatistics(periodMode, startDate, endDate, emitEvent = true)
        }

        fun onCalendarPeriodSelected(periodMode: PeriodMode, startDate: Date, endDate: Date) {
            Log.d(TAG, "Calendar period selected: $periodMode from $startDate to $endDate")
            updatePeriodStatistics(periodMode, startDate, endDate, emitEvent = true)
        }

        /**
         * ✅ SIMPLIFIED: Get location statistics using unified service
         */
        fun getLocationStatistics() {
            // ✅ NEW (#27): Use ioDispatcher for statistics gathering (testable)
            viewModelScope.launch(ioDispatcher) {
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
            // ✅ NEW (#27): Use ioDispatcher for statistics gathering (testable)
            viewModelScope.launch(ioDispatcher) {
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
            // ✅ NEW (#27): Use ioDispatcher for statistics gathering (testable)
            viewModelScope.launch(ioDispatcher) {
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
            val isPaused: Boolean = false,
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
            val yearStatistics: PeriodStatistics? = null,
            val monthlyStatistics: SummaryStatistics? = null,
            val selectedPeriod: SelectedPeriod? = null,
            val selectedPeriodLabel: String = DEFAULT_PERIOD_LABEL,
            /** True when user explicitly cleared Month section display (non-destructive UI clear). */
            val isPeriodSectionCleared: Boolean = false,
            /** True when user explicitly cleared Year section display (non-destructive UI clear). */
            val isYearSectionCleared: Boolean = false,
            /** Dates (start-of-day) in the selected period that have at least one saved (ended) trip. Used for calendar/period clickable days. */
            val datesWithTripsInPeriod: List<Date> = emptyList(),
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

        /**
         * Summary stats for the stat card row. Add new metrics via [extraFields] and the
         * stats_extra_rows_container in statistics_row.xml so stat cards stay versatile without layout changes.
         */
        data class SummaryStatistics(
            val totalTrips: Int,
            val totalMiles: Double,
            val oorMiles: Double,
            val oorPercentage: Double,
            val extraFields: Map<String, String> = emptyMap()
        )

        data class SelectedPeriod(
            val startDate: Date,
            val endDate: Date,
            val periodMode: PeriodMode,
            val label: String
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
        private fun stopAutoSave(clearRecoveryState: Boolean = true) {
            crashRecoveryManager.stopAutoSave()
            if (clearRecoveryState) {
                crashRecoveryManager.clearRecoveryData()
                Log.d(TAG, "Auto-save stopped and recovery data cleared")
            } else {
                Log.d(TAG, "Auto-save stopped; recovery data preserved")
            }
        }

        // ==================== TRIP PERSISTENCE & RECOVERY ====================

        /**
         * ✅ NEW: Check for trip recovery on app startup
         */
        fun checkForTripRecovery(): TripPersistenceManager.SavedTripState? {
            return try {
                Log.d(TAG, "Checking for trip recovery")
                tripPersistenceManager.loadSavedTripState()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for trip recovery", e)
                null
            }
        }

        /**
         * ✅ NEW: Continue a recovered trip
         * Reinstates notification and status-bar icon via TripTrackingService.startTrip();
         * also starts overlay monitoring for trip-ended detection.
         */
        fun continueRecoveredTrip(savedState: TripPersistenceManager.SavedTripState) {
            try {
                Log.d(TAG, "Continuing recovered trip: ${savedState.trip.id}")
                
                // Restore trip state
                _uiState.update { currentState ->
                    currentState.copy(
                        loadedMiles = savedState.loadedMiles,
                        bounceMiles = savedState.bounceMiles,
                        actualMiles = savedState.actualMiles,
                        isTripActive = true,
                        tripStatusMessage = "Trip recovered - Distance: ${String.format(Locale.US, "%.1f", savedState.actualMiles)}mi"
                    )
                }
                
                // Restore current trip
                currentTrip = savedState.trip
                
                // Start GPS tracking service (reinstates notification + tiny icon via startTrip())
                TripTrackingService.startService(
                    context = application,
                    loadedMiles = savedState.loadedMiles,
                    bounceMiles = savedState.bounceMiles,
                    initialTotalMiles = savedState.actualMiles
                )
                // Reinstate overlay monitoring so trip-ended bubble/notification work again
                TripEndedOverlayService.startWhenTripActive(application)
                
                // Start auto-save for crash recovery
                startAutoSave()
                
                Log.d(TAG, "Recovered trip started successfully; notification and icon reinstated")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to continue recovered trip", e)
                viewModelScope.launch {
                    _events.emit(TripEvent.Error("Failed to recover trip: ${e.message}"))
                }
            }
        }

        /**
         * ✅ NEW: Start a new trip (clearing recovery data)
         */
        fun startNewTrip() {
            try {
                Log.d(TAG, "Starting new trip, clearing recovery data")
                
                // Clear any saved trip state
                tripPersistenceManager.clearSavedTripState()
                
                // Reset UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        loadedMiles = 0.0,
                        bounceMiles = 0.0,
                        actualMiles = 0.0,
                        isTripActive = false,
                        tripStatusMessage = "Ready to start new trip"
                    )
                }
                
                // Clear current trip
                currentTrip = null
                
                Log.d(TAG, "New trip state initialized")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start new trip", e)
                viewModelScope.launch {
                    _events.emit(TripEvent.Error("Failed to start new trip: ${e.message}"))
                }
            }
        }

        /**
         * ✅ NEW: Clear trip (stop tracking, reset state, clear persistence)
         */
        fun clearTrip() {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Clearing trip - stopping tracking and resetting state")
                    
                    // Stop GPS tracking service if it's running
                    try {
                        TripTrackingService.stopService(application)
                        Log.d(TAG, "GPS tracking service stopped")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to stop GPS tracking service", e)
                    }
                    TripEndedOverlayService.dismissBubble(application)
                    TripEndedOverlayService.notifyTripEndedFromInApp(application)
                    
                    // Stop auto-save
                    stopAutoSave()
                    
                    // Clear trip persistence
                    clearTripPersistence()
                    
                    // Reset UI state
                    _uiState.update { currentState ->
                        currentState.copy(
                            loadedMiles = 0.0,
                            bounceMiles = 0.0,
                            actualMiles = 0.0,
                            isTripActive = false,
                            oorMiles = 0.0,
                            oorPercentage = 0.0,
                            tripStatusMessage = "Trip cleared",
                            showStatistics = false
                        )
                    }
                    
                    // Clear current trip
                    currentTrip = null
                    
                    Log.d(TAG, "Trip cleared successfully")

                    val selected = _uiState.value.selectedPeriod
                    refreshStatisticsAfterSave(selected)
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing trip", e)
                    _events.emit(TripEvent.CalculationError("Failed to clear trip: ${e.message}"))
                }
            }
        }

        /**
         * ✅ NEW: Save trip state for persistence (called during active trip)
         */
        private fun saveTripStateForPersistence() {
            try {
                val state = _uiState.value
                val trip = currentTrip
                AppLogger.d(TAG, "Saving trip state for persistence")
                
                if (state.isTripActive && trip != null) {
                    AppLogger.d(TAG, "Persisting active trip state")
                    val loadedMiles = state.loadedMiles
                    val bounceMiles = state.bounceMiles
                    val actualMiles = state.actualMiles
                    
                    // Create location data from GPS quality if available
                    val lastLocation = state.gpsQuality?.let { gpsQuality ->
                        TripPersistenceManager.LocationData(
                            latitude = 0.0, // Would need actual location
                            longitude = 0.0, // Would need actual location
                            accuracy = gpsQuality.accuracy,
                            timestamp = Date(gpsQuality.lastUpdate),
                            speed = 0f
                        )
                    }
                    
                    // Create GPS metadata
                    val gpsMetadata = TripPersistenceManager.GpsMetadata(
                        totalPoints = 0, // Would track this
                        validPoints = 0, // Would track this
                        avgAccuracy = state.gpsQuality?.accuracy?.toDouble() ?: 0.0
                    )
                    
                    // Save trip state
                    tripPersistenceManager.saveActiveTripState(
                        trip = trip,
                        loadedMiles = loadedMiles,
                        bounceMiles = bounceMiles,
                        actualMiles = actualMiles,
                        lastLocation = lastLocation,
                        gpsMetadata = gpsMetadata
                    )
                    
                    AppLogger.d(TAG, "Trip state successfully saved to persistence")
                } else {
                    AppLogger.w(TAG, "Skipped trip-state save because no active trip was available")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save trip state for persistence", e)
            }
        }

        /**
         * ✅ NEW: Update trip progress during active trip
         */
        private fun updateTripProgress(actualMiles: Double) {
            try {
                if (_uiState.value.isTripActive) {
                    // Update persistence with new progress
                    val lastLocation = _uiState.value.gpsQuality?.let { gpsQuality ->
                        TripPersistenceManager.LocationData(
                            latitude = 0.0, // Would need actual location
                            longitude = 0.0, // Would need actual location
                            accuracy = gpsQuality.accuracy,
                            timestamp = Date(gpsQuality.lastUpdate),
                            speed = 0f
                        )
                    }
                    
                    tripPersistenceManager.updateTripProgress(actualMiles, lastLocation)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update trip progress", e)
            }
        }

        /**
         * ✅ NEW: Clear trip persistence (called when trip ends)
         */
        private fun clearTripPersistence() {
            try {
                tripPersistenceManager.clearSavedTripState()
                Log.d(TAG, "Trip persistence cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear trip persistence", e)
            }
        }
    } 
