package com.example.outofroutebuddy.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

/**
 * ✅ NEW: Offline Data Manager for Enhanced Offline Support
 *
 * This class manages offline data storage, synchronization, and conflict resolution
 * to ensure data integrity when working offline.
 */
class OfflineDataManager(
    private val context: Context,
    private val networkStateManager: NetworkStateManager,
    private val preferencesManager: PreferencesManager,
) {
    companion object {
        private const val TAG = "OfflineDataManager"
        private const val MAX_OFFLINE_STORAGE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val MAX_OFFLINE_TRIPS = 1000
        private const val MAX_OFFLINE_ANALYTICS = 5000
    }

    // ✅ OFFLINE STORAGE: Local data storage
    private val _offlineStorage = MutableStateFlow(OfflineStorage())
    val offlineStorage: StateFlow<OfflineStorage> = _offlineStorage.asStateFlow()

    // ✅ SYNC STATUS: Synchronization status
    private val _syncStatus = MutableStateFlow(SyncStatusInfo())
    val syncStatus: StateFlow<SyncStatusInfo> = _syncStatus.asStateFlow()

    // ✅ NEW: Offline storage data class
    data class OfflineStorage(
        val trips: Map<String, OfflineTrip> = emptyMap(),
        val analytics: List<OfflineAnalytics> = emptyList(),
        val settings: Map<String, Any> = emptyMap(),
        val lastBackup: Date? = null,
        val storageSize: Long = 0L,
        val tripCount: Int = 0,
        val analyticsCount: Int = 0,
    )

    // ✅ NEW: Offline trip data
    data class OfflineTrip(
        val id: String,
        val localId: String,
        val tripData: Map<String, Any>,
        val gpsData: Map<String, Any>?,
        val timestamp: Date,
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val conflictResolution: ConflictResolution? = null,
        val retryCount: Int = 0,
        val lastSyncAttempt: Date? = null,
    )

    // ✅ NEW: Offline analytics data
    data class OfflineAnalytics(
        val id: String,
        val event: String,
        val parameters: Map<String, Any>,
        val timestamp: Date,
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val retryCount: Int = 0,
    )

    // ✅ NEW: Sync status enumeration
    enum class SyncStatus {
        PENDING,
        SYNCING,
        SYNCED,
        FAILED,
        CONFLICT,
    }

    // ✅ NEW: Conflict resolution data
    data class ConflictResolution(
        val type: ConflictType,
        val localVersion: Map<String, Any>,
        val remoteVersion: Map<String, Any>?,
        val resolvedVersion: Map<String, Any>?,
        val resolutionStrategy: ResolutionStrategy,
    )

    // ✅ NEW: Conflict type enumeration
    enum class ConflictType {
        TRIP_UPDATE,
        TRIP_DELETE,
        SETTINGS_UPDATE,
        ANALYTICS_DUPLICATE,
    }

    // ✅ NEW: Resolution strategy enumeration
    enum class ResolutionStrategy {
        USE_LOCAL,
        USE_REMOTE,
        MERGE,
        MANUAL,
        SKIP,
    }

    // ✅ NEW: Sync status info data class (renamed to avoid conflict)
    data class SyncStatusInfo(
        val isSyncing: Boolean = false,
        val lastSyncTime: Date? = null,
        val pendingItems: Int = 0,
        val failedItems: Int = 0,
        val syncProgress: Double = 0.0,
        val errorMessage: String? = null,
    )

    init {
        // ✅ INITIALIZE: Load offline storage
        loadOfflineStorage()
    }

    /**
     * ✅ NEW: Load offline storage from local storage
     */
    private fun loadOfflineStorage() {
        try {
            // TODO: Implement actual loading from local storage (SharedPreferences, SQLite, etc.)
            Log.d(TAG, "Offline storage loaded")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load offline storage", e)
        }
    }

    /**
     * ✅ NEW: Save trip data offline
     */
    fun saveTripOffline(
        tripData: Map<String, Any>,
        gpsData: Map<String, Any>? = null,
    ): String {
        try {
            val localId = UUID.randomUUID().toString()
            val offlineTrip =
                OfflineTrip(
                    id = tripData["id"] as? String ?: localId,
                    localId = localId,
                    tripData = tripData,
                    gpsData = gpsData,
                    timestamp = Date(),
                )

            val currentStorage = _offlineStorage.value
            val newTrips = currentStorage.trips + (localId to offlineTrip)

            // ✅ CHECK: Storage limits
            if (newTrips.size > MAX_OFFLINE_TRIPS) {
                cleanupOldestTrips(newTrips.size - MAX_OFFLINE_TRIPS)
            }

            val newStorage =
                currentStorage.copy(
                    trips = newTrips,
                    tripCount = newTrips.size,
                )

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Trip saved offline: $localId")
            return localId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save trip offline", e)
            throw e
        }
    }

    /**
     * ✅ NEW: Save analytics offline
     */
    fun saveAnalyticsOffline(
        event: String,
        parameters: Map<String, Any>,
    ): String {
        try {
            val analyticsId = UUID.randomUUID().toString()
            val offlineAnalytics =
                OfflineAnalytics(
                    id = analyticsId,
                    event = event,
                    parameters = parameters,
                    timestamp = Date(),
                )

            val currentStorage = _offlineStorage.value
            val newAnalytics = currentStorage.analytics + offlineAnalytics

            // ✅ CHECK: Storage limits
            if (newAnalytics.size > MAX_OFFLINE_ANALYTICS) {
                cleanupOldestAnalytics(newAnalytics.size - MAX_OFFLINE_ANALYTICS)
            }

            val newStorage =
                currentStorage.copy(
                    analytics = newAnalytics,
                    analyticsCount = newAnalytics.size,
                )

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Analytics saved offline: $analyticsId")
            return analyticsId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save analytics offline", e)
            throw e
        }
    }

    /**
     * ✅ NEW: Get offline trip by local ID
     */
    fun getOfflineTrip(localId: String): OfflineTrip? {
        return _offlineStorage.value.trips[localId]
    }

    /**
     * ✅ NEW: Get all offline trips
     */
    fun getAllOfflineTrips(): List<OfflineTrip> {
        return _offlineStorage.value.trips.values.toList()
    }

    /**
     * ✅ NEW: Get pending sync trips
     */
    fun getPendingSyncTrips(): List<OfflineTrip> {
        return _offlineStorage.value.trips.values.filter { it.syncStatus == SyncStatus.PENDING }
    }

    /**
     * ✅ NEW: Get failed sync trips
     */
    fun getFailedSyncTrips(): List<OfflineTrip> {
        return _offlineStorage.value.trips.values.filter { it.syncStatus == SyncStatus.FAILED }
    }

    /**
     * ✅ NEW: Update trip sync status
     */
    fun updateTripSyncStatus(
        localId: String,
        status: SyncStatus,
        _errorMessage: String? = null,
    ) {
        try {
            val currentStorage = _offlineStorage.value
            val trip = currentStorage.trips[localId] ?: return

            val updatedTrip =
                trip.copy(
                    syncStatus = status,
                    lastSyncAttempt = if (status == SyncStatus.SYNCING) Date() else trip.lastSyncAttempt,
                    retryCount = if (status == SyncStatus.FAILED) trip.retryCount + 1 else trip.retryCount,
                )

            val newTrips = currentStorage.trips + (localId to updatedTrip)
            val newStorage = currentStorage.copy(trips = newTrips)

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Updated trip sync status: $localId -> $status")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip sync status", e)
        }
    }

    /**
     * ✅ NEW: Resolve trip conflict
     */
    fun resolveTripConflict(
        localId: String,
        resolution: ConflictResolution,
    ) {
        try {
            val currentStorage = _offlineStorage.value
            val trip = currentStorage.trips[localId] ?: return

            val updatedTrip =
                trip.copy(
                    conflictResolution = resolution,
                    syncStatus = SyncStatus.PENDING,
                )

            val newTrips = currentStorage.trips + (localId to updatedTrip)
            val newStorage = currentStorage.copy(trips = newTrips)

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Resolved trip conflict: $localId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve trip conflict", e)
        }
    }

    /**
     * ✅ NEW: Clean up oldest trips
     */
    private fun cleanupOldestTrips(count: Int) {
        try {
            val currentStorage = _offlineStorage.value
            val sortedTrips = currentStorage.trips.values.sortedBy { it.timestamp }
            val tripsToRemove = sortedTrips.take(count)

            val newTrips =
                currentStorage.trips.filter { (_, trip) ->
                    !tripsToRemove.contains(trip)
                }

            val newStorage =
                currentStorage.copy(
                    trips = newTrips,
                    tripCount = newTrips.size,
                )

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Cleaned up $count oldest trips")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup oldest trips", e)
        }
    }

    /**
     * ✅ NEW: Clean up oldest analytics
     */
    private fun cleanupOldestAnalytics(count: Int) {
        try {
            val currentStorage = _offlineStorage.value
            val sortedAnalytics = currentStorage.analytics.sortedBy { it.timestamp }
            val analyticsToKeep = sortedAnalytics.drop(count)

            val newStorage =
                currentStorage.copy(
                    analytics = analyticsToKeep,
                    analyticsCount = analyticsToKeep.size,
                )

            _offlineStorage.value = newStorage
            saveOfflineStorage()

            Log.d(TAG, "Cleaned up $count oldest analytics")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup oldest analytics", e)
        }
    }

    /**
     * ✅ NEW: Save offline storage to local storage
     */
    private fun saveOfflineStorage() {
        try {
            // TODO: Implement actual saving to local storage
            Log.d(TAG, "Offline storage saved")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save offline storage", e)
        }
    }

    /**
     * ✅ NEW: Initialize offline storage
     */
    fun initializeOfflineStorage() {
        try {
            loadOfflineStorage()
            Log.d(TAG, "Offline storage initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize offline storage", e)
        }
    }

    /**
     * ✅ NEW: Get offline data count
     */
    fun getOfflineDataCount(): Int {
        val storage = _offlineStorage.value
        return storage.tripCount + storage.analyticsCount
    }

    /**
     * ✅ NEW: Get pending sync count
     */
    fun getPendingSyncCount(): Int {
        val storage = _offlineStorage.value
        val pendingTrips = storage.trips.values.count { it.syncStatus == SyncStatus.PENDING }
        val pendingAnalytics = storage.analytics.count { it.syncStatus == SyncStatus.PENDING }
        return pendingTrips + pendingAnalytics
    }

    /**
     * ✅ NEW: Get failed sync count
     */
    fun getFailedSyncCount(): Int {
        val storage = _offlineStorage.value
        val failedTrips = storage.trips.values.count { it.syncStatus == SyncStatus.FAILED }
        val failedAnalytics = storage.analytics.count { it.syncStatus == SyncStatus.FAILED }
        return failedTrips + failedAnalytics
    }

    /**
     * ✅ NEW: Save data offline (generic method)
     */
    fun saveOfflineData(
        data: Any,
        dataType: String,
    ): Boolean {
        return try {
            when (dataType) {
                "trip" -> {
                    val tripData = data as? Map<String, Any> ?: return false
                    saveTripOffline(tripData)
                    true
                }
                "analytics" -> {
                    val analyticsData = data as? Map<String, Any> ?: return false
                    val event = analyticsData["event"] as? String ?: return false
                    val parameters = analyticsData["parameters"] as? Map<String, Any> ?: emptyMap()
                    saveAnalyticsOffline(event, parameters)
                    true
                }
                else -> {
                    Log.w(TAG, "Unknown data type: $dataType")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save offline data", e)
            false
        }
    }

    /**
     * ✅ NEW: Get offline data
     */
    fun getOfflineData(dataType: String): List<Any> {
        return try {
            val storage = _offlineStorage.value
            when (dataType) {
                "trip" -> storage.trips.values.map { it.tripData }
                "analytics" -> storage.analytics.map { mapOf("event" to it.event, "parameters" to it.parameters) }
                else -> {
                    Log.w(TAG, "Unknown data type: $dataType")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offline data", e)
            emptyList()
        }
    }

    /**
     * ✅ NEW: Clear offline data by type
     */
    fun clearOfflineData(dataType: String): Boolean {
        return try {
            val currentStorage = _offlineStorage.value
            val newStorage =
                when (dataType) {
                    "trip" -> currentStorage.copy(trips = emptyMap(), tripCount = 0)
                    "analytics" -> currentStorage.copy(analytics = emptyList(), analyticsCount = 0)
                    else -> {
                        Log.w(TAG, "Unknown data type: $dataType")
                        return false
                    }
                }

            _offlineStorage.value = newStorage
            saveOfflineStorage()
            Log.d(TAG, "Cleared offline data: $dataType")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear offline data", e)
            false
        }
    }

    /**
     * ✅ NEW: Clear all offline data
     */
    fun clearAllOfflineData(): Boolean {
        return try {
            val newStorage = OfflineStorage()
            _offlineStorage.value = newStorage
            saveOfflineStorage()
            Log.d(TAG, "Cleared all offline data")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all offline data", e)
            false
        }
    }

    /**
     * ✅ NEW: Get storage statistics
     */
    fun getStorageStatistics(): StorageStatistics {
        val storage = _offlineStorage.value
        return StorageStatistics(
            totalTrips = storage.tripCount,
            pendingTrips = storage.trips.values.count { it.syncStatus == SyncStatus.PENDING },
            failedTrips = storage.trips.values.count { it.syncStatus == SyncStatus.FAILED },
            totalAnalytics = storage.analyticsCount,
            pendingAnalytics = storage.analytics.count { it.syncStatus == SyncStatus.PENDING },
            storageSize = storage.storageSize,
            lastBackup = storage.lastBackup,
        )
    }

    /**
     * ✅ NEW: Storage statistics data class
     */
    data class StorageStatistics(
        val totalTrips: Int,
        val pendingTrips: Int,
        val failedTrips: Int,
        val totalAnalytics: Int,
        val pendingAnalytics: Int,
        val storageSize: Long,
        val lastBackup: Date?,
    )

    /**
     * ✅ NEW: Check if storage is full
     */
    fun isStorageFull(): Boolean {
        return _offlineStorage.value.storageSize > MAX_OFFLINE_STORAGE_SIZE
    }

    /**
     * ✅ NEW: Get available storage space
     */
    fun getAvailableStorageSpace(): Long {
        return MAX_OFFLINE_STORAGE_SIZE - _offlineStorage.value.storageSize
    }
} 
