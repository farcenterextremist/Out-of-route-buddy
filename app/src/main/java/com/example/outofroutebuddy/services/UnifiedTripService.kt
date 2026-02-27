package com.example.outofroutebuddy.services

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * ✅ UNIFIED: Trip Service - Merged from multiple trip services
 * 
 * This service combines the functionality of:
 * - TripTrackingService (trip tracking and management)
 * - TripCalculationService (trip calculations)
 * - TripTrackingCoordinator (coordination)
 * - PeriodCalculationService (period calculations)
 * 
 * Benefits:
 * - Single point of entry for all trip operations
 * - Simplified dependency injection
 * - Reduced code duplication
 * - Easier to maintain and test
 */
open class UnifiedTripService(
    private val context: Context,
    private val tripStateManager: TripStateManager,
    private val periodCalculationService: PeriodCalculationService
) {
    
    companion object {
        private const val TAG = "UnifiedTripService"
        
        // ✅ CENTRALIZED: Using ValidationConfig for all constants
        val DEFAULT_PERIOD_MODE = PeriodMode.STANDARD
        val DEFAULT_MAX_TRIP_DURATION = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val DEFAULT_MIN_TRIP_DISTANCE = 0.1 // 0.1 miles minimum
        val DEFAULT_MAX_TRIP_DISTANCE = 10000.0 // 10,000 miles maximum
    }
    
    // ✅ ROBUSTNESS: Managed coroutine scope for proper cancellation
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tripStateMonitorJob: Job? = null
    
    // ✅ NEW (#13): Mutex for thread-safe statistics calculations
    private val statisticsMutex = Mutex()
    
    // ✅ UNIFIED: State flows
    private val _tripState = MutableStateFlow(TripServiceState())
    val tripState: StateFlow<TripServiceState> = _tripState.asStateFlow()
    
    private val _calculationState = MutableStateFlow(CalculationState())
    val calculationState: StateFlow<CalculationState> = _calculationState.asStateFlow()
    
    // ✅ UNIFIED: Data classes
    data class TripServiceState(
        val isTracking: Boolean = false,
        val currentTrip: Trip? = null,
        val tripHistory: List<Trip> = emptyList(),
        val totalTrips: Int = 0,
        val totalDistance: Double = 0.0,
        val totalOorMiles: Double = 0.0,
        val lastTripEndTime: Date? = null,
        val trackingStartTime: Date? = null
    )
    
    data class CalculationState(
        val isCalculating: Boolean = false,
        val calculationProgress: Double = 0.0,
        val currentOperation: String? = null,
        val lastCalculationTime: Date? = null,
        val calculationResults: List<CalculationResult> = emptyList()
    )
    
    data class CalculationResult(
        val tripId: String,
        val loadedMiles: Double,
        val bounceMiles: Double,
        val actualMiles: Double,
        val oorMiles: Double,
        val oorPercentage: Double,
        val calculationTime: Date,
        val isValid: Boolean = true,
        val validationIssues: List<String> = emptyList()
    )
    
    data class PeriodCalculation(
        val periodMode: PeriodMode,
        val startDate: Date,
        val endDate: Date,
        val totalTrips: Int,
        val totalDistance: Double,
        val totalOorMiles: Double,
        val averageOorPercentage: Double,
        val trips: List<Trip>
    )
    
    init {
        initializeTripService()
    }
    
    /**
     * ✅ UNIFIED: Initialize trip service
     */
    private fun initializeTripService() {
        try {
            Log.d(TAG, "Initializing unified trip service")
            
            // Start monitoring trip state with managed scope
            tripStateMonitorJob = serviceScope.launch {
                tripStateManager.tripState.collect { tripState ->
                    if (isActive) { // ✅ ROBUSTNESS: Check for cancellation
                        if (tripState.isActive) {
                            startTripTracking()
                        } else {
                            stopTripTracking()
                        }
                    }
                }
            }
            
            Log.d(TAG, "Unified trip service initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize trip service", e)
        }
    }
    
    /**
     * ✅ ROBUSTNESS: Clean up resources when service is no longer needed
     */
    fun cleanup() {
        try {
            tripStateMonitorJob?.cancel()
            serviceScope.cancel()
            Log.d(TAG, "Unified trip service cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Start trip tracking
     */
    private fun startTripTracking() {
        try {
            _tripState.value = _tripState.value.copy(
                isTracking = true,
                trackingStartTime = Date()
            )
            Log.d(TAG, "Trip tracking started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip tracking", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Stop trip tracking
     */
    private fun stopTripTracking() {
        try {
            _tripState.value = _tripState.value.copy(
                isTracking = false,
                lastTripEndTime = Date()
            )
            Log.d(TAG, "Trip tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trip tracking", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Calculate trip with comprehensive validation
     */
    suspend fun calculateTrip(
        loadedMiles: Double,
        bounceMiles: Double,
        actualMiles: Double
    ): CalculationResult {
        return try {
            Log.d(TAG, "Calculating trip: loaded=$loadedMiles, bounce=$bounceMiles, actual=$actualMiles")
            
            updateCalculationState { it.copy(
                isCalculating = true,
                currentOperation = "Validating trip data"
            ) }
            
            // Validate input data
            val validationIssues = validateTripData(loadedMiles, bounceMiles, actualMiles)
            
            if (validationIssues.isNotEmpty()) {
                val result = CalculationResult(
                    tripId = generateTripId(),
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                    oorMiles = 0.0,
                    oorPercentage = 0.0,
                    calculationTime = Date(),
                    isValid = false,
                    validationIssues = validationIssues
                )
                
                updateCalculationState { it.copy(
                    isCalculating = false,
                    calculationResults = it.calculationResults + result
                ) }
                
                return result
            }
            
            updateCalculationState { it.copy(currentOperation = "Calculating OOR miles") }
            
            // Calculate OOR miles
            val dispatchedMiles = loadedMiles + bounceMiles
            // ✅ FIXED: Correct OOR calculation formula
            // OOR = Actual Miles - Dispatched Miles (not Dispatched Miles - Actual Miles)
            val oorMiles = actualMiles - dispatchedMiles
            val oorPercentage = if (dispatchedMiles > 0) {
                (oorMiles / dispatchedMiles) * 100.0
            } else {
                0.0
            }
            
            updateCalculationState { it.copy(currentOperation = "Creating trip record") }
            
            // Create trip record
            // ✅ FIX: Set status based on actualMiles - ACTIVE if 0 (GPS initializing), COMPLETED otherwise
            val tripStatus = if (actualMiles == 0.0) {
                com.example.outofroutebuddy.domain.models.TripStatus.ACTIVE
            } else {
                com.example.outofroutebuddy.domain.models.TripStatus.COMPLETED
            }
            
            val trip = Trip(
                id = generateTripId(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                startTime = _tripState.value.trackingStartTime,
                endTime = if (actualMiles == 0.0) null else Date(),
                status = tripStatus
            )
            
            // Update trip state
            val currentState = _tripState.value
            val newTripHistory = currentState.tripHistory + trip
            val newTotalDistance = currentState.totalDistance + actualMiles
            val newTotalOorMiles = currentState.totalOorMiles + oorMiles
            
            _tripState.value = currentState.copy(
                currentTrip = trip,
                tripHistory = newTripHistory,
                totalTrips = newTripHistory.size,
                totalDistance = newTotalDistance,
                totalOorMiles = newTotalOorMiles,
                lastTripEndTime = Date()
            )
            
            val result = CalculationResult(
                tripId = trip.id,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                calculationTime = Date(),
                isValid = true
            )
            
            updateCalculationState { it.copy(
                isCalculating = false,
                calculationResults = it.calculationResults + result,
                lastCalculationTime = Date()
            ) }
            
            Log.d(TAG, "Trip calculated successfully: OOR=${oorMiles}mi (${String.format("%.1f", oorPercentage)}%)")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate trip", e)
            updateCalculationState { it.copy(isCalculating = false) }
            
            CalculationResult(
                tripId = generateTripId(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = 0.0,
                oorPercentage = 0.0,
                calculationTime = Date(),
                isValid = false,
                validationIssues = listOf("Calculation error: ${e.message}")
            )
        }
    }
    
    /**
     * ✅ NEW: Calculate period statistics for current period based on mode
     */
    suspend fun calculateCurrentPeriodStatistics(periodMode: PeriodMode): PeriodCalculation {
        val (startDate, endDate) = getCurrentPeriodDates(periodMode)
        return calculatePeriodStatistics(periodMode, startDate, endDate)
    }

    /**
     * Returns the current period date range based on period mode.
     *
     * - STANDARD: First day of current month through last day of current month. Always includes today,
     *   so a trip ended today will appear in "days with trips" and monthly statistics.
     * - CUSTOM: Thursday before first Friday of the month through the following period end. May not
     *   include today if today falls outside that business period; trips ended today may not appear
     *   in the period view until the next period that includes today.
     */
    fun getCurrentPeriodDates(periodMode: PeriodMode): Pair<Date, Date> {
        val now = Date()
        return when (periodMode) {
            PeriodMode.STANDARD -> {
                // STANDARD mode: 1st of month to last of month (full calendar month); always includes today
                val calendar = Calendar.getInstance()
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.time
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val endOfMonth = calendar.time
                startOfMonth to endOfMonth
            }
            PeriodMode.CUSTOM -> {
                // CUSTOM mode: Current business period
                val customStart = periodCalculationService.getCurrentCustomPeriodStart()
                val customEnd = periodCalculationService.getCurrentCustomPeriodEnd()
                customStart to customEnd
            }
        }
    }

    /**
     * ✅ UNIFIED: Calculate period statistics with business logic for different period modes
     */
    suspend fun calculatePeriodStatistics(
        periodMode: PeriodMode,
        startDate: Date,
        endDate: Date
    ): PeriodCalculation {
        return try {
            Log.d(TAG, "Calculating period statistics: $periodMode from $startDate to $endDate")
            
            updateCalculationState { it.copy(
                isCalculating = true,
                currentOperation = "Calculating period statistics for $periodMode mode"
            ) }
            
            // ✅ NEW: Business logic for different period modes
            val (effectiveStartDate, effectiveEndDate) = when (periodMode) {
                PeriodMode.STANDARD -> {
                    // STANDARD mode: Use provided dates as-is (daily calculations)
                    Log.d(TAG, "STANDARD mode: Using provided date range for daily calculations")
                    startDate to endDate
                }
                PeriodMode.CUSTOM -> {
                    // CUSTOM mode: Use business period logic
                    Log.d(TAG, "CUSTOM mode: Calculating business period boundaries")
                    val customStart = periodCalculationService.calculateCustomPeriodStart(startDate).time
                    val customEnd = periodCalculationService.calculateCustomPeriodEnd(startDate).time
                    Log.d(TAG, "Custom period: ${customStart} to ${customEnd}")
                    customStart to customEnd
                }
            }
            
            updateCalculationState { it.copy(
                calculationProgress = 0.3,
                currentOperation = "Filtering trips for period: ${effectiveStartDate} to ${effectiveEndDate}"
            ) }
            
            // ✅ NEW (#13): Thread-safe access to trip history for statistics
            val result = statisticsMutex.withLock {
                // Filter trips for the calculated period
                val periodTrips = _tripState.value.tripHistory.filter { trip ->
                    trip.startTime != null && 
                    trip.startTime >= effectiveStartDate && 
                    trip.startTime <= effectiveEndDate
                }
                
                Log.d(TAG, "Found ${periodTrips.size} trips in period (${effectiveStartDate} to ${effectiveEndDate})")
                
                updateCalculationState { it.copy(
                    calculationProgress = 0.6,
                    currentOperation = "Processing ${periodTrips.size} trips for $periodMode mode"
                ) }
                
                // Calculate statistics
                val totalTrips = periodTrips.size
                val totalDistance = periodTrips.sumOf { it.actualMiles }
                val totalOorMiles = periodTrips.sumOf { it.oorMiles }
                val averageOorPercentage = if (totalTrips > 0) {
                    periodTrips.map { it.oorPercentage }.average()
                } else {
                    0.0
                }
                
                updateCalculationState { it.copy(
                    calculationProgress = 1.0,
                    currentOperation = "Period calculation completed for $periodMode mode"
                ) }
                
                PeriodCalculation(
                    periodMode = periodMode,
                    startDate = effectiveStartDate,
                    endDate = effectiveEndDate,
                    totalTrips = totalTrips,
                    totalDistance = totalDistance,
                    totalOorMiles = totalOorMiles,
                    averageOorPercentage = averageOorPercentage,
                    trips = periodTrips
                )
            }
            
            updateCalculationState { it.copy(
                isCalculating = false,
                lastCalculationTime = Date()
            ) }
            
            Log.d(TAG, "Period statistics calculated for $periodMode mode: ${result.totalTrips} trips, ${String.format("%.1f", result.totalDistance)}mi total, ${String.format("%.1f", result.totalOorMiles)}mi OOR")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate period statistics for $periodMode mode", e)
            updateCalculationState { it.copy(isCalculating = false) }
            
            PeriodCalculation(
                periodMode = periodMode,
                startDate = startDate,
                endDate = endDate,
                totalTrips = 0,
                totalDistance = 0.0,
                totalOorMiles = 0.0,
                averageOorPercentage = 0.0,
                trips = emptyList()
            )
        }
    }
    
    /**
     * ✅ UNIFIED: Validate trip data
     */
    private fun validateTripData(
        loadedMiles: Double,
        bounceMiles: Double,
        actualMiles: Double
    ): List<String> {
        val issues = mutableListOf<String>()
        
        // Check for negative values
        if (loadedMiles < 0.0) {
            issues.add("Loaded miles cannot be negative")
        }
        if (bounceMiles < 0.0) {
            issues.add("Bounce miles cannot be negative")
        }
        if (actualMiles < 0.0) {
            issues.add("Actual miles cannot be negative")
        }
        
        // Check for unrealistic values
        if (loadedMiles > DEFAULT_MAX_TRIP_DISTANCE) {
            issues.add("Loaded miles (${loadedMiles}mi) exceeds maximum (${DEFAULT_MAX_TRIP_DISTANCE}mi)")
        }
        if (bounceMiles > DEFAULT_MAX_TRIP_DISTANCE) {
            issues.add("Bounce miles (${bounceMiles}mi) exceeds maximum (${DEFAULT_MAX_TRIP_DISTANCE}mi)")
        }
        if (actualMiles > DEFAULT_MAX_TRIP_DISTANCE) {
            issues.add("Actual miles (${actualMiles}mi) exceeds maximum (${DEFAULT_MAX_TRIP_DISTANCE}mi)")
        }
        
        // ✅ GPS-AWARE VALIDATION: Only check minimum distance for actual miles if they're significantly above 0
        // This allows GPS to start at 0.0 and gradually increase as the trip progresses
        if (actualMiles > 0.0 && actualMiles < DEFAULT_MIN_TRIP_DISTANCE) {
            // Only warn if actual miles is above 0 but below minimum - this suggests GPS might be starting up
            // Don't block the trip calculation for this case
            Log.w(TAG, "Actual miles (${actualMiles}mi) is below minimum (${DEFAULT_MIN_TRIP_DISTANCE}mi) - GPS may still be initializing")
        }
        
        // Check for logical consistency - only if we have meaningful actual miles
        if (actualMiles > 1.0) { // Only check if actual miles is meaningful
            val totalDispatched = loadedMiles + bounceMiles
            if (totalDispatched > 0 && actualMiles > totalDispatched * 1.5) {
                issues.add("Actual miles (${actualMiles}mi) is significantly higher than dispatched miles (${totalDispatched}mi)")
            }
        }
        
        return issues
    }
    
    /**
     * ✅ UNIFIED: Generate unique trip ID
     */
    private fun generateTripId(): String {
        return "trip_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    /**
     * ✅ UNIFIED: Update calculation state
     */
    private fun updateCalculationState(update: (CalculationState) -> CalculationState) {
        _calculationState.value = update(_calculationState.value)
    }
    
    /**
     * ✅ UNIFIED: Get current trip
     */
    fun getCurrentTrip(): Trip? {
        return _tripState.value.currentTrip
    }
    
    /**
     * ✅ UNIFIED: Get trip history
     */
    fun getTripHistory(): List<Trip> {
        return _tripState.value.tripHistory
    }
    
    /**
     * ✅ UNIFIED: Get total trips count
     */
    fun getTotalTrips(): Int {
        return _tripState.value.totalTrips
    }
    
    /**
     * ✅ UNIFIED: Get total distance
     */
    fun getTotalDistance(): Double {
        return _tripState.value.totalDistance
    }
    
    /**
     * ✅ UNIFIED: Get total OOR miles
     */
    fun getTotalOorMiles(): Double {
        return _tripState.value.totalOorMiles
    }
    
    /**
     * ✅ UNIFIED: Check if trip tracking is active
     */
    fun isTripTrackingActive(): Boolean {
        return _tripState.value.isTracking
    }
    
    /**
     * ✅ UNIFIED: Get trip statistics (suspend to avoid blocking Main; call from coroutine)
     */
    suspend fun getTripStatistics(): TripStatistics {
        return statisticsMutex.withLock {
            val currentState = _tripState.value
            val totalTrips = currentState.totalTrips

            val averageDistance = if (totalTrips > 0) {
                currentState.totalDistance / totalTrips
            } else {
                0.0
            }

            val averageOorPercentage = if (totalTrips > 0) {
                currentState.totalOorMiles / currentState.totalDistance * 100.0
            } else {
                0.0
            }

            TripStatistics(
                totalTrips = totalTrips,
                totalDistance = currentState.totalDistance,
                totalOorMiles = currentState.totalOorMiles,
                averageDistance = averageDistance,
                averageOorPercentage = averageOorPercentage,
                isTracking = currentState.isTracking,
                lastTripEndTime = currentState.lastTripEndTime
            )
        }
    }
    
    /**
     * ✅ UNIFIED: Clear trip data
     */
    fun clearTripData() {
        try {
            _tripState.value = TripServiceState()
            _calculationState.value = CalculationState()
            
            Log.d(TAG, "Trip data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear trip data", e)
        }
    }
    
    // ✅ DATA CLASSES
    data class TripStatistics(
        val totalTrips: Int,
        val totalDistance: Double,
        val totalOorMiles: Double,
        val averageDistance: Double,
        val averageOorPercentage: Double,
        val isTracking: Boolean,
        val lastTripEndTime: Date?
    )
} 
