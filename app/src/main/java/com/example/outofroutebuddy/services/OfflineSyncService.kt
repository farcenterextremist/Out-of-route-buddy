package com.example.outofroutebuddy.services

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.core.config.BuildConfig
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.data.NetworkStateManager
import com.example.outofroutebuddy.data.OfflineDataManager
import com.example.outofroutebuddy.data.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ✅ NEW: Offline Sync Service for Data Synchronization
 * 
 * This service handles synchronization of offline data when network connectivity
 * is restored, including conflict resolution and retry logic.
 */
class OfflineSyncService(
    private val context: Context,
    private val networkStateManager: NetworkStateManager,
    private val offlineDataManager: OfflineDataManager,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "OfflineSyncService"
            private const val MAX_RETRY_ATTEMPTS = BuildConfig.DEFAULT_RETRY_ATTEMPTS
    private const val RETRY_DELAY_MS = ValidationConfig.ERROR_RECOVERY_DELAY
    private const val SYNC_TIMEOUT_MS = BuildConfig.DEFAULT_NETWORK_TIMEOUT
    }
    
    // ✅ SYNC STATE: Current synchronization state
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    // ✅ SYNC JOB: Current synchronization job
    private var syncJob: Job? = null
    private val isSyncing = AtomicBoolean(false)
    
    // ✅ NEW: Sync state data class
    data class SyncState(
        val isActive: Boolean = false,
        val lastSyncTime: Date? = null,
        val syncProgress: Double = 0.0,
        val totalItems: Int = 0,
        val processedItems: Int = 0,
        val failedItems: Int = 0,
        val currentOperation: String? = null,
        val errorMessage: String? = null,
        val syncType: SyncType = SyncType.FULL
    )
    
    // ✅ NEW: Sync type enumeration
    enum class SyncType {
        FULL, INCREMENTAL, TRIPS_ONLY, ANALYTICS_ONLY, CONFLICT_RESOLUTION
    }
    
    // ✅ NEW: Sync result data class
    data class SyncResult(
        val success: Boolean,
        val itemsProcessed: Int,
        val itemsFailed: Int,
        val conflictsResolved: Int,
        val errorMessage: String? = null,
        val duration: Long = 0L
    )
    
    init {
        // ✅ INITIALIZE: Set up network state monitoring
        setupNetworkMonitoring()
    }
    
    /**
     * ✅ NEW: Set up network state monitoring for auto-sync
     */
    private fun setupNetworkMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            networkStateManager.networkState
                .filter { it.isConnected }
                .distinctUntilChanged()
                .collect { _ ->
                    Log.d(TAG, "Network restored - checking for offline data to sync")
                    if (hasOfflineDataToSync()) {
                        startAutoSync()
                    }
                }
        }
    }
    
    /**
     * ✅ NEW: Check if there's offline data to sync
     */
    private fun hasOfflineDataToSync(): Boolean {
        val pendingTrips = offlineDataManager.getPendingSyncTrips()
        val failedTrips = offlineDataManager.getFailedSyncTrips()
        val storage = offlineDataManager.offlineStorage.value
        
        return pendingTrips.isNotEmpty() || 
               failedTrips.isNotEmpty() || 
               storage.analytics.any { it.syncStatus == OfflineDataManager.SyncStatus.PENDING }
    }
    
    /**
     * ✅ NEW: Start automatic synchronization
     */
    private fun startAutoSync() {
        if (isSyncing.get()) {
            Log.d(TAG, "Sync already in progress, skipping auto-sync")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting automatic synchronization")
                performSync(SyncType.INCREMENTAL)
            } catch (e: Exception) {
                Log.e(TAG, "Auto-sync failed", e)
            }
        }
    }
    
    /**
     * ✅ NEW: Start manual synchronization
     */
    fun startManualSync(syncType: SyncType = SyncType.FULL): Job {
        if (isSyncing.get()) {
            Log.w(TAG, "Sync already in progress")
            return Job()
        }
        
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting manual synchronization: $syncType")
                performSync(syncType)
            } catch (e: Exception) {
                Log.e(TAG, "Manual sync failed", e)
            }
        }
    }
    
    /**
     * ✅ NEW: Perform synchronization
     */
    private suspend fun performSync(syncType: SyncType): SyncResult {
        val startTime = System.currentTimeMillis()
        
        try {
            isSyncing.set(true)
            updateSyncState { it.copy(currentOperation = "Initializing sync") }
            
            Log.d(TAG, "Starting sync: $syncType")
            
            val result = when (syncType) {
                SyncType.FULL -> performFullSync()
                SyncType.INCREMENTAL -> performIncrementalSync()
                SyncType.TRIPS_ONLY -> performTripsSync()
                SyncType.ANALYTICS_ONLY -> performAnalyticsSync()
                SyncType.CONFLICT_RESOLUTION -> performConflictResolution()
            }
            
            val duration = System.currentTimeMillis() - startTime
            val finalResult = result.copy(duration = duration)
            
            Log.i(TAG, "Sync completed: $finalResult")
            
            // ✅ UPDATE: Final sync state
            updateSyncState { it.copy(
                isActive = false,
                lastSyncTime = Date(),
                syncProgress = 1.0,
                totalItems = result.itemsProcessed + result.itemsFailed,
                processedItems = result.itemsProcessed,
                failedItems = result.itemsFailed,
                currentOperation = "Sync completed",
                errorMessage = result.errorMessage
            ) }
            
            return finalResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            
            updateSyncState { it.copy(
                isActive = false,
                errorMessage = e.message,
                currentOperation = "Sync failed"
            ) }
            
            return SyncResult(
                success = false,
                itemsProcessed = 0,
                itemsFailed = 0,
                conflictsResolved = 0,
                errorMessage = e.message,
                duration = System.currentTimeMillis() - startTime
            )
            
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * ✅ NEW: Perform full synchronization
     */
    private suspend fun performFullSync(): SyncResult {
        updateSyncState { it.copy(currentOperation = "Performing full sync") }
        
        val tripsResult = performTripsSync()
        val analyticsResult = performAnalyticsSync()
        val conflictResult = performConflictResolution()
        
        return SyncResult(
            success = tripsResult.success && analyticsResult.success,
            itemsProcessed = tripsResult.itemsProcessed + analyticsResult.itemsProcessed,
            itemsFailed = tripsResult.itemsFailed + analyticsResult.itemsFailed,
            conflictsResolved = conflictResult.conflictsResolved
        )
    }
    
    /**
     * ✅ NEW: Perform incremental synchronization
     */
    private suspend fun performIncrementalSync(): SyncResult {
        updateSyncState { it.copy(currentOperation = "Performing incremental sync") }
        
        val pendingTrips = offlineDataManager.getPendingSyncTrips()
        val failedTrips = offlineDataManager.getFailedSyncTrips()
        val storage = offlineDataManager.offlineStorage.value
        val pendingAnalytics = storage.analytics.filter { it.syncStatus == OfflineDataManager.SyncStatus.PENDING }
        
        var totalProcessed = 0
        var totalFailed = 0
        var conflictsResolved = 0
        
        // ✅ SYNC: Pending trips
        if (pendingTrips.isNotEmpty()) {
            updateSyncState { it.copy(currentOperation = "Syncing pending trips") }
            val tripsResult = syncTrips(pendingTrips)
            totalProcessed += tripsResult.itemsProcessed
            totalFailed += tripsResult.itemsFailed
        }
        
        // ✅ SYNC: Failed trips with retry
        if (failedTrips.isNotEmpty()) {
            updateSyncState { it.copy(currentOperation = "Retrying failed trips") }
            val retryResult = retryFailedTrips(failedTrips)
            totalProcessed += retryResult.itemsProcessed
            totalFailed += retryResult.itemsFailed
        }
        
        // ✅ SYNC: Pending analytics
        if (pendingAnalytics.isNotEmpty()) {
            updateSyncState { it.copy(currentOperation = "Syncing analytics") }
            val analyticsResult = syncAnalytics(pendingAnalytics)
            totalProcessed += analyticsResult.itemsProcessed
            totalFailed += analyticsResult.itemsFailed
        }
        
        // ✅ RESOLVE: Conflicts
        updateSyncState { it.copy(currentOperation = "Resolving conflicts") }
        val conflictResult = performConflictResolution()
        conflictsResolved = conflictResult.conflictsResolved
        
        return SyncResult(
            success = totalFailed == 0,
            itemsProcessed = totalProcessed,
            itemsFailed = totalFailed,
            conflictsResolved = conflictsResolved
        )
    }
    
    /**
     * ✅ NEW: Perform trips synchronization
     */
    private suspend fun performTripsSync(): SyncResult {
        updateSyncState { it.copy(currentOperation = "Syncing trips") }
        
        val allTrips = offlineDataManager.getAllOfflineTrips()
        return syncTrips(allTrips)
    }
    
    /**
     * ✅ NEW: Perform analytics synchronization
     */
    private suspend fun performAnalyticsSync(): SyncResult {
        updateSyncState { it.copy(currentOperation = "Syncing analytics") }
        
        val storage = offlineDataManager.offlineStorage.value
        val allAnalytics = storage.analytics
        return syncAnalytics(allAnalytics)
    }
    
    /**
     * ✅ NEW: Perform conflict resolution
     */
    private suspend fun performConflictResolution(): SyncResult {
        updateSyncState { it.copy(currentOperation = "Resolving conflicts") }
        
        val storage = offlineDataManager.offlineStorage.value
        val conflicts = storage.trips.values.filter { it.conflictResolution != null }
        
        var resolvedCount = 0
        
        for (trip in conflicts) {
            try {
                val resolution = trip.conflictResolution ?: continue // Skip if no resolution strategy
                when (resolution.resolutionStrategy) {
                    OfflineDataManager.ResolutionStrategy.USE_LOCAL -> {
                        // Use local version
                        offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.PENDING)
                        resolvedCount++
                    }
                    OfflineDataManager.ResolutionStrategy.USE_REMOTE -> {
                        // Use remote version (would need to fetch from server)
                        offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.SYNCED)
                        resolvedCount++
                    }
                    OfflineDataManager.ResolutionStrategy.MERGE -> {
                        // Merge versions (would need custom merge logic)
                        val mergedData = mergeTripData(resolution.localVersion, resolution.remoteVersion)
                        // Update trip with merged data
                        resolvedCount++
                    }
                    OfflineDataManager.ResolutionStrategy.MANUAL -> {
                        // Requires manual intervention - skip for now
                        Log.w(TAG, "Manual conflict resolution required for trip: ${trip.localId}")
                    }
                    OfflineDataManager.ResolutionStrategy.SKIP -> {
                        // Skip this conflict
                        Log.d(TAG, "Skipping conflict for trip: ${trip.localId}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resolve conflict for trip: ${trip.localId}", e)
            }
        }
        
        return SyncResult(
            success = true,
            itemsProcessed = 0,
            itemsFailed = 0,
            conflictsResolved = resolvedCount
        )
    }
    
    /**
     * ✅ NEW: Sync trips with retry logic
     */
    private suspend fun syncTrips(trips: List<OfflineDataManager.OfflineTrip>): SyncResult {
        var processed = 0
        var failed = 0
        
        for ((index, trip) in trips.withIndex()) {
            try {
                updateSyncState { it.copy(
                    syncProgress = (index + 1).toDouble() / trips.size,
                    currentOperation = "Syncing trip ${index + 1}/${trips.size}"
                ) }
                
                // ✅ SIMULATE: Trip sync (replace with actual API call)
                val success = simulateTripSync(trip)
                
                if (success) {
                    offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.SYNCED)
                    processed++
                    Log.d(TAG, "Trip synced successfully: ${trip.localId}")
                } else {
                    offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.FAILED)
                    failed++
                    Log.w(TAG, "Trip sync failed: ${trip.localId}")
                }
                
                // ✅ DELAY: Between syncs to avoid overwhelming the server
                delay(100)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing trip: ${trip.localId}", e)
                offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.FAILED)
                failed++
            }
        }
        
        return SyncResult(
            success = failed == 0,
            itemsProcessed = processed,
            itemsFailed = failed,
            conflictsResolved = 0
        )
    }
    
    /**
     * ✅ NEW: Sync analytics with retry logic
     */
    private suspend fun syncAnalytics(analytics: List<OfflineDataManager.OfflineAnalytics>): SyncResult {
        var processed = 0
        var failed = 0
        
        for ((index, analytic) in analytics.withIndex()) {
            try {
                updateSyncState { it.copy(
                    syncProgress = (index + 1).toDouble() / analytics.size,
                    currentOperation = "Syncing analytics ${index + 1}/${analytics.size}"
                ) }
                
                // ✅ SIMULATE: Analytics sync (replace with actual API call)
                val success = simulateAnalyticsSync(analytic)
                
                if (success) {
                    processed++
                    Log.d(TAG, "Analytics synced successfully: ${analytic.id}")
                } else {
                    failed++
                    Log.w(TAG, "Analytics sync failed: ${analytic.id}")
                }
                
                // ✅ DELAY: Between syncs
                delay(50)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing analytics: ${analytic.id}", e)
                failed++
            }
        }
        
        return SyncResult(
            success = failed == 0,
            itemsProcessed = processed,
            itemsFailed = failed,
            conflictsResolved = 0
        )
    }
    
    /**
     * ✅ NEW: Retry failed trips
     */
    private suspend fun retryFailedTrips(failedTrips: List<OfflineDataManager.OfflineTrip>): SyncResult {
        var processed = 0
        var failed = 0
        
        for (trip in failedTrips) {
            if (trip.retryCount >= MAX_RETRY_ATTEMPTS) {
                Log.w(TAG, "Trip exceeded max retry attempts: ${trip.localId}")
                failed++
                continue
            }
            
            try {
                // ✅ RETRY: With exponential backoff
                delay(RETRY_DELAY_MS * (1 shl trip.retryCount))
                
                val success = simulateTripSync(trip)
                
                if (success) {
                    offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.SYNCED)
                    processed++
                    Log.d(TAG, "Trip retry successful: ${trip.localId}")
                } else {
                    offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.FAILED)
                    failed++
                    Log.w(TAG, "Trip retry failed: ${trip.localId}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying trip: ${trip.localId}", e)
                offlineDataManager.updateTripSyncStatus(trip.localId, OfflineDataManager.SyncStatus.FAILED)
                failed++
            }
        }
        
        return SyncResult(
            success = failed == 0,
            itemsProcessed = processed,
            itemsFailed = failed,
            conflictsResolved = 0
        )
    }
    
    /**
     * ✅ NEW: Simulate trip synchronization (replace with actual API call)
     */
    private suspend fun simulateTripSync(_trip: OfflineDataManager.OfflineTrip): Boolean {
        return withTimeout(SYNC_TIMEOUT_MS) {
            // ✅ SIMULATE: Network delay and potential failure
            delay((100..500).random().toLong())
            
            // ✅ SIMULATE: 90% success rate
            (0..100).random() < 90
        }
    }
    
    /**
     * ✅ NEW: Simulate analytics synchronization (replace with actual API call)
     */
    private suspend fun simulateAnalyticsSync(_analytic: OfflineDataManager.OfflineAnalytics): Boolean {
        return withTimeout(SYNC_TIMEOUT_MS) {
            // ✅ SIMULATE: Network delay and potential failure
            delay((50..200).random().toLong())
            
            // ✅ SIMULATE: 95% success rate
            (0..100).random() < 95
        }
    }
    
    /**
     * ✅ NEW: Merge trip data for conflict resolution
     */
    private fun mergeTripData(local: Map<String, Any>, remote: Map<String, Any>?): Map<String, Any> {
        if (remote == null) return local
        
        val merged = local.toMutableMap()
        
        // ✅ MERGE: Use remote data for certain fields, local for others
        // This is a simplified merge - in practice, you'd have more sophisticated logic
        remote.forEach { (key, value) ->
            when (key) {
                "lastModified", "serverVersion" -> merged[key] = value
                else -> {
                    // Keep local version for most fields
                    if (!merged.containsKey(key)) {
                        merged[key] = value
                    }
                }
            }
        }
        
        return merged
    }
    
    /**
     * ✅ NEW: Update sync state
     */
    private fun updateSyncState(update: (SyncState) -> SyncState) {
        _syncState.value = update(_syncState.value)
    }
    
    /**
     * ✅ NEW: Check if sync is active
     */
    fun isSyncActive(): Boolean = isSyncing.get()
    
    /**
     * ✅ NEW: Cancel current sync
     */
    fun cancelSync() {
        syncJob?.cancel()
        isSyncing.set(false)
        updateSyncState { it.copy(isActive = false, currentOperation = "Sync cancelled") }
        Log.i(TAG, "Sync cancelled")
    }
    
    /**
     * ✅ NEW: Get sync statistics
     */
    fun getSyncStatistics(): SyncStatistics {
        val state = _syncState.value
        val storage = offlineDataManager.getStorageStatistics()
        
        return SyncStatistics(
            lastSyncTime = state.lastSyncTime,
            totalTrips = storage.totalTrips,
            pendingTrips = storage.pendingTrips,
            failedTrips = storage.failedTrips,
            totalAnalytics = storage.totalAnalytics,
            pendingAnalytics = storage.pendingAnalytics,
            isNetworkAvailable = networkStateManager.isNetworkAvailable(),
            isOffline = networkStateManager.isOffline()
        )
    }
    
    /**
     * ✅ NEW: Sync statistics data class
     */
    data class SyncStatistics(
        val lastSyncTime: Date?,
        val totalTrips: Int,
        val pendingTrips: Int,
        val failedTrips: Int,
        val totalAnalytics: Int,
        val pendingAnalytics: Int,
        val isNetworkAvailable: Boolean,
        val isOffline: Boolean
    )
} 
