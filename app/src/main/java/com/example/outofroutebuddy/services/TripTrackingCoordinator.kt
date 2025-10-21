package com.example.outofroutebuddy.services

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.domain.models.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * ✅ NEW: Trip Tracking Coordinator
 * 
 * This coordinator manages the interaction between the ViewModel and TripTrackingService,
 * providing a clean interface for trip tracking operations and state management.
 */
class TripTrackingCoordinator(
    private val context: Context,
    private val tripStateManager: TripStateManager,
    private val gpsSynchronizationService: GpsSynchronizationService
) {
    companion object {
        private const val TAG = "TripTrackingCoordinator"
    }
    
    // ✅ COORDINATOR STATE: Track coordination status
    private val _coordinatorState = MutableStateFlow(CoordinatorState())
    val coordinatorState: StateFlow<CoordinatorState> = _coordinatorState.asStateFlow()
    
    // ✅ TRIP TRACKING STATE: Track current trip status
    private val _tripTrackingState = MutableStateFlow(TripTrackingState())
    val tripTrackingState: StateFlow<TripTrackingState> = _tripTrackingState.asStateFlow()
    
    // ✅ CURRENT TRIP: Track the active trip
    private var currentTrip: Trip? = null
    
    /**
     * ✅ NEW: Coordinator state data class
     */
    data class CoordinatorState(
        val isCoordinating: Boolean = false,
        val lastAction: String = "None",
        val lastActionTime: Date = Date(),
        val errorCount: Int = 0,
        val successCount: Int = 0,
        val isHealthy: Boolean = true
    )
    
    /**
     * ✅ NEW: Trip tracking state data class
     */
    data class TripTrackingState(
        val isTracking: Boolean = false,
        val tripId: String? = null,
        val startTime: Date? = null,
        val loadedMiles: Double = 0.0,
        val bounceMiles: Double = 0.0,
        val currentDistance: Double = 0.0,
        val gpsQuality: Double = 0.0,
        val locationAccuracy: Double = 0.0,
        val lastLocationUpdate: Date = Date(),
        val serviceStatus: String = "Stopped"
    )
    
    /**
     * ✅ NEW: Start trip tracking with coordination
     */
    fun startTripTracking(loadedMiles: Double, bounceMiles: Double): Boolean {
        return try {
            Log.d(TAG, "Starting trip tracking coordination: loaded=$loadedMiles, bounce=$bounceMiles")
            
            // Create new trip
            val trip = Trip(
                id = java.util.UUID.randomUUID().toString(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                startTime = Date(),
                status = com.example.outofroutebuddy.domain.models.TripStatus.ACTIVE
            )
            
            currentTrip = trip
            
            // Start TripTrackingService
            TripTrackingService.startService(context, loadedMiles, bounceMiles)
            
            // Start GPS synchronization
            gpsSynchronizationService.startSync()
            
            // Update coordinator state
            _coordinatorState.value = CoordinatorState(
                isCoordinating = true,
                lastAction = "Trip tracking started",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1,
                isHealthy = true
            )
            
            // Update trip tracking state
            _tripTrackingState.value = TripTrackingState(
                isTracking = true,
                tripId = trip.id,
                startTime = trip.startTime,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                serviceStatus = "Running"
            )
            
            Log.d(TAG, "Trip tracking coordination started successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip tracking coordination", e)
            updateCoordinatorError("Failed to start trip tracking: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Stop trip tracking with coordination
     */
    fun stopTripTracking(): Boolean {
        return try {
            Log.d(TAG, "Stopping trip tracking coordination")
            
            // Stop TripTrackingService
            TripTrackingService.stopService(context)
            
            // Stop GPS synchronization
            gpsSynchronizationService.stopSync()
            
            // Update coordinator state
            _coordinatorState.value = _coordinatorState.value.copy(
                isCoordinating = false,
                lastAction = "Trip tracking stopped",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1
            )
            
            // Update trip tracking state
            _tripTrackingState.value = _tripTrackingState.value.copy(
                isTracking = false,
                serviceStatus = "Stopped"
            )
            
            // Clear current trip
            currentTrip = null
            
            Log.d(TAG, "Trip tracking coordination stopped successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trip tracking coordination", e)
            updateCoordinatorError("Failed to stop trip tracking: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Get current trip
     */
    fun getCurrentTrip(): Trip? = currentTrip
    
    /**
     * ✅ NEW: Update trip with GPS data
     */
    fun updateTripWithGpsData(gpsDistance: Double, gpsAccuracy: Double, gpsQuality: Double) {
        try {
            currentTrip?.let { trip ->
                val updatedTrip = trip.copy(
                    gpsDistance = gpsDistance,
                    gpsAccuracy = gpsAccuracy,
                    gpsQuality = gpsQuality
                )
                currentTrip = updatedTrip
                
                // Update trip tracking state
                _tripTrackingState.value = _tripTrackingState.value.copy(
                    currentDistance = gpsDistance,
                    gpsQuality = gpsQuality,
                    locationAccuracy = gpsAccuracy,
                    lastLocationUpdate = Date()
                )
                
                Log.v(TAG, "Updated trip with GPS data: distance=$gpsDistance, accuracy=$gpsAccuracy, quality=$gpsQuality")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip with GPS data", e)
        }
    }
    
    /**
     * ✅ NEW: Get trip metrics
     */
    fun getTripMetrics(): TripMetrics {
        return currentTrip?.let { trip ->
            TripMetrics(
                totalMiles = trip.actualMiles,
                oorMiles = trip.oorMiles
            )
        } ?: TripMetrics()
    }
    
    /**
     * ✅ NEW: Check if trip tracking is active
     */
    fun isTripTrackingActive(): Boolean = _tripTrackingState.value.isTracking
    
    /**
     * ✅ NEW: Get service status
     */
    fun getServiceStatus(): String = _tripTrackingState.value.serviceStatus
    
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
     * ✅ NEW: Get coordinator health status
     */
    fun getCoordinatorHealth(): CoordinatorHealth {
        val state = _coordinatorState.value
        return CoordinatorHealth(
            isHealthy = state.isHealthy,
            errorRate = if (state.successCount + state.errorCount > 0) {
                state.errorCount.toDouble() / (state.successCount + state.errorCount)
            } else 0.0,
            lastError = if (state.lastAction.startsWith("Error:")) state.lastAction else null,
            uptime = Date().time - state.lastActionTime.time
        )
    }
    
    /**
     * ✅ NEW: Coordinator health data class
     */
    data class CoordinatorHealth(
        val isHealthy: Boolean,
        val errorRate: Double,
        val lastError: String?,
        val uptime: Long
    )
    
    /**
     * ✅ NEW: Trip metrics data class
     */
    data class TripMetrics(
        val totalMiles: Double = 0.0,
        val oorMiles: Double = 0.0
    )
} 
