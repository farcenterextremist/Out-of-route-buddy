package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.data.TripStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * 🚀 MOCK GPS SYNCHRONIZATION SERVICE FOR TESTING
 *
 * This mock service provides controlled GPS data for testing the ViewModel
 * without requiring actual GPS hardware or location permissions.
 *
 * Features:
 * - Simulate GPS distance updates
 * - Control GPS data flow timing
 * - Test real-time UI updates
 * - Verify trip calculations with known distances
 */
class MockGpsSynchronizationService : IGpsSynchronizationService {
    // ✅ MOCK: Real-time GPS data for testing
    private val _realTimeGpsData =
        MutableStateFlow(
            GpsSynchronizationService.RealTimeGpsData(
                totalDistance = 0.0,
                currentLocation = null,
                lastUpdate = Date(),
            ),
        )
    override val realTimeGpsData: StateFlow<GpsSynchronizationService.RealTimeGpsData> = _realTimeGpsData

    // ✅ MOCK: GPS synchronization state
    private val _syncState = MutableStateFlow(GpsSynchronizationService.GpsSyncState())
    override val syncState: StateFlow<GpsSynchronizationService.GpsSyncState> = _syncState

    // ✅ MOCK: Callback for distance updates
    private var onDistanceUpdateCallback: ((Double) -> Unit)? = null

    /**
     * ✅ MOCK: Set callback for distance updates
     *
     * @param callback The callback to call when distance is updated
     */
    fun setOnDistanceUpdate(callback: (Double) -> Unit) {
        onDistanceUpdateCallback = callback
    }

    /**
     * ✅ MOCK: Simulate GPS distance updates for testing
     *
     * @param distance The distance in miles to emit
     */
    fun emitDistance(distance: Double) {
        _realTimeGpsData.value =
            _realTimeGpsData.value.copy(
                totalDistance = distance,
                lastUpdate = Date(),
            )
        // Call the callback if set
        onDistanceUpdateCallback?.invoke(distance)
    }

    /**
     * ✅ MOCK: Simulate GPS location updates for testing
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param accuracy The GPS accuracy in meters
     */
    fun emitLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float = 5.0f,
    ) {
        val locationData =
            TripStateManager.LocationData(
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                timestamp = Date(),
                speed = 0.0f,
            )

        _realTimeGpsData.value =
            _realTimeGpsData.value.copy(
                currentLocation = locationData,
                lastUpdate = Date(),
            )
    }

    /**
     * ✅ MOCK: Simulate GPS speed updates for testing
     *
     * @param speedMph The speed in miles per hour
     */
    fun emitSpeed(speedMph: Double) {
        _realTimeGpsData.value =
            _realTimeGpsData.value.copy(
                currentSpeed = speedMph,
                lastUpdate = Date(),
            )
    }

    /**
     * ✅ MOCK: Simulate GPS quality updates for testing
     *
     * @param quality The GPS quality score (0.0 to 1.0)
     * @param satelliteCount The number of satellites
     */
    fun emitGpsQuality(
        quality: Double,
        satelliteCount: Int,
    ) {
        _realTimeGpsData.value =
            _realTimeGpsData.value.copy(
                gpsQuality = quality,
                satelliteCount = satelliteCount,
                isHighAccuracy = quality > 0.7,
                lastUpdate = Date(),
            )
    }

    /**
     * ✅ MOCK: Start GPS synchronization (no-op for testing)
     */
    override fun startSync() {
        emitSyncState(true, "Mock GPS started")
    }

    /**
     * ✅ MOCK: Stop GPS synchronization (no-op for testing)
     */
    override fun stopSync() {
        emitSyncState(false, "Mock GPS stopped")
    }

    /**
     * ✅ MOCK: Reset all GPS data for testing
     */
    fun reset() {
        _realTimeGpsData.value =
            GpsSynchronizationService.RealTimeGpsData(
                totalDistance = 0.0,
                currentLocation = null,
                lastUpdate = Date(),
            )
        _syncState.value = GpsSynchronizationService.GpsSyncState()
    }

    /**
     * ✅ MOCK: Simulate GPS sync state for testing
     *
     * @param isSynchronized Whether GPS is synchronized
     * @param dataQuality The quality of GPS data
     */
    fun emitSyncState(
        isSynchronized: Boolean,
        dataQuality: String = "Good",
    ) {
        _syncState.value =
            GpsSynchronizationService.GpsSyncState(
                isSynchronized = isSynchronized,
                lastSyncTime = Date(),
                syncErrors = if (isSynchronized) 0 else 1,
                dataQuality = dataQuality,
                connectionStatus = if (isSynchronized) "Connected" else "Disconnected",
            )
    }
} 
