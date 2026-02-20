package com.example.outofroutebuddy.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.data.NetworkStateManager
import com.example.outofroutebuddy.data.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ✅ UNIFIED: Offline Service - Merged from multiple offline services
 * 
 * This service combines the functionality of:
 * - OfflineSyncService (sync operations)
 * - SimpleOfflineService (basic offline storage)
 * - StandaloneOfflineService (standalone functionality)
 * - OfflineServiceCoordinator (coordination)
 * 
 * Benefits:
 * - Single point of entry for all offline operations
 * - Simplified dependency injection
 * - Reduced code duplication
 * - Easier to maintain and test
 */
class UnifiedOfflineService(
    private val context: Context,
    private val networkStateManager: NetworkStateManager,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "UnifiedOfflineService"
        private const val PREFS_NAME = "unified_offline_prefs"
        private const val KEY_OFFLINE_TRIPS = "offline_trips"
        private const val KEY_OFFLINE_ANALYTICS = "offline_analytics"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_NETWORK_STATUS = "network_status"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
        private const val SYNC_TIMEOUT_MS = 30000L
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val isSyncing = AtomicBoolean(false)
    private var syncJob: Job? = null
    
    // ✅ UNIFIED: State flows
    private val _offlineState = MutableStateFlow(OfflineState())
    val offlineState: StateFlow<OfflineState> = _offlineState.asStateFlow()
    
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    // ✅ UNIFIED: Data classes
    data class OfflineState(
        val isOffline: Boolean = false,
        val pendingTrips: Int = 0,
        val pendingAnalytics: Int = 0,
        val lastSyncTime: Long? = null,
        val networkAvailable: Boolean = false,
        val storageSize: Long = 0L
    )
    
    data class SyncState(
        val isActive: Boolean = false,
        val syncProgress: Double = 0.0,
        val totalItems: Int = 0,
        val processedItems: Int = 0,
        val failedItems: Int = 0,
        val currentOperation: String? = null,
        val errorMessage: String? = null
    )
    
    data class OfflineTrip(
        val id: String,
        val tripData: Map<String, Any>,
        val timestamp: Long,
        val retryCount: Int = 0
    )
    
    data class OfflineAnalytics(
        val id: String,
        val event: String,
        val parameters: Map<String, Any>,
        val timestamp: Long
    )
    
    init {
        loadOfflineState()
        setupNetworkMonitoring()
    }
    
    /**
     * ✅ UNIFIED: Load offline state from preferences
     */
    private fun loadOfflineState() {
        try {
            val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L).takeIf { it > 0 }
            val isOffline = !prefs.getBoolean(KEY_NETWORK_STATUS, false)
            val pendingTrips = getOfflineTrips().size
            val pendingAnalytics = getOfflineAnalytics().size
            
            _offlineState.value = OfflineState(
                isOffline = isOffline,
                pendingTrips = pendingTrips,
                pendingAnalytics = pendingAnalytics,
                lastSyncTime = lastSync,
                networkAvailable = networkStateManager.isNetworkAvailable()
            )
            
            Log.d(TAG, "Offline state loaded: $pendingTrips trips, $pendingAnalytics analytics")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load offline state", e)
        }
    }
    
    /**
     * ✅ UNIFIED: Set up network monitoring for auto-sync
     */
    private fun setupNetworkMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            networkStateManager.networkState
                .filter { it.isConnected }
                .distinctUntilChanged()
                .collect { networkState ->
                    Log.d(TAG, "Network restored - checking for offline data to sync")
                    if (hasOfflineDataToSync()) {
                        startAutoSync()
                    }
                }
        }
    }
    
    /**
     * ✅ UNIFIED: Check if there's offline data to sync
     */
    private fun hasOfflineDataToSync(): Boolean {
        val offlineTrips = getOfflineTrips()
        val offlineAnalytics = getOfflineAnalytics()
        return offlineTrips.isNotEmpty() || offlineAnalytics.isNotEmpty()
    }
    
    /**
     * ✅ UNIFIED: Start automatic synchronization
     */
    private fun startAutoSync() {
        if (isSyncing.get()) {
            Log.d(TAG, "Sync already in progress, skipping auto-sync")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting automatic synchronization")
                performSync()
            } catch (e: Exception) {
                Log.e(TAG, "Auto-sync failed", e)
            }
        }
    }
    
    /**
     * ✅ UNIFIED: Start manual synchronization
     */
    fun startManualSync(): Job {
        if (isSyncing.get()) {
            Log.w(TAG, "Sync already in progress")
            return Job()
        }
        
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting manual synchronization")
                performSync()
            } catch (e: Exception) {
                Log.e(TAG, "Manual sync failed", e)
            }
        }
    }
    
    /**
     * ✅ UNIFIED: Perform synchronization
     */
    private suspend fun performSync(): Boolean {
        val startTime = System.currentTimeMillis()
        
        try {
            isSyncing.set(true)
            updateSyncState { it.copy(isActive = true, currentOperation = "Initializing sync") }
            
            val offlineTrips = getOfflineTrips()
            val offlineAnalytics = getOfflineAnalytics()
            
            var totalProcessed = 0
            var totalFailed = 0
            
            // Sync trips
            if (offlineTrips.isNotEmpty()) {
                updateSyncState { it.copy(currentOperation = "Syncing trips") }
                val tripsResult = syncTrips(offlineTrips)
                totalProcessed += tripsResult.processed
                totalFailed += tripsResult.failed
            }
            
            // Sync analytics
            if (offlineAnalytics.isNotEmpty()) {
                updateSyncState { it.copy(currentOperation = "Syncing analytics") }
                val analyticsResult = syncAnalytics(offlineAnalytics)
                totalProcessed += analyticsResult.processed
                totalFailed += analyticsResult.failed
            }
            
            // Clear offline data if sync was successful
            if (totalFailed == 0) {
                clearOfflineData()
                _offlineState.value = _offlineState.value.copy(
                    pendingTrips = 0,
                    pendingAnalytics = 0,
                    lastSyncTime = System.currentTimeMillis()
                )
                prefs.edit {putLong(KEY_LAST_SYNC, System.currentTimeMillis())}
                
                Log.i(TAG, "Sync completed successfully: $totalProcessed items processed")
            } else {
                Log.w(TAG, "Partial sync completed: $totalProcessed processed, $totalFailed failed")
            }
            
            updateSyncState { it.copy(
                isActive = false,
                processedItems = totalProcessed,
                failedItems = totalFailed,
                currentOperation = "Sync completed"
            ) }
            
            return totalFailed == 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            updateSyncState { it.copy(
                isActive = false,
                errorMessage = e.message,
                currentOperation = "Sync failed"
            ) }
            return false
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * ✅ UNIFIED: Sync offline trips
     */
    private suspend fun syncTrips(trips: List<OfflineTrip>): SyncResult {
        var processed = 0
        var failed = 0
        
        for ((index, trip) in trips.withIndex()) {
            try {
                updateSyncState { it.copy(
                    syncProgress = (index + 1).toDouble() / trips.size,
                    currentOperation = "Syncing trip ${index + 1}/${trips.size}"
                ) }
                
                // Simulate trip sync (replace with actual API call)
                val success = simulateTripSync(trip)
                
                if (success) {
                    processed++
                    Log.d(TAG, "Trip synced successfully: ${trip.id}")
                } else {
                    failed++
                    Log.w(TAG, "Trip sync failed: ${trip.id}")
                }
                
                delay(100) // Avoid overwhelming server
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing trip: ${trip.id}", e)
                failed++
            }
        }
        
        return SyncResult(processed, failed)
    }
    
    /**
     * ✅ UNIFIED: Sync offline analytics
     */
    private suspend fun syncAnalytics(analytics: List<OfflineAnalytics>): SyncResult {
        var processed = 0
        var failed = 0
        
        for ((index, analyticsItem) in analytics.withIndex()) {
            try {
                updateSyncState { it.copy(
                    syncProgress = (index + 1).toDouble() / analytics.size,
                    currentOperation = "Syncing analytics ${index + 1}/${analytics.size}"
                ) }
                
                // Simulate analytics sync (replace with actual API call)
                val success = simulateAnalyticsSync(analyticsItem)
                
                if (success) {
                    processed++
                    Log.d(TAG, "Analytics synced successfully: ${analyticsItem.id}")
                } else {
                    failed++
                    Log.w(TAG, "Analytics sync failed: ${analyticsItem.id}")
                }
                
                delay(50) // Analytics can be faster
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing analytics: ${analyticsItem.id}", e)
                failed++
            }
        }
        
        return SyncResult(processed, failed)
    }
    
    /**
     * ✅ UNIFIED: Save trip data offline
     */
    fun saveTripOffline(tripData: Map<String, Any>): String {
        return try {
            val tripId = "trip_${System.currentTimeMillis()}"
            val offlineTrip = OfflineTrip(
                id = tripId,
                tripData = tripData,
                timestamp = System.currentTimeMillis()
            )
            
            val currentTrips = getOfflineTrips().toMutableList()
            currentTrips.add(offlineTrip)
            saveOfflineTrips(currentTrips)
            
            updateOfflineState()
            Log.d(TAG, "Trip saved offline: $tripId")
            tripId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save trip offline", e)
            throw e
        }
    }
    
    /**
     * ✅ UNIFIED: Save analytics offline
     */
    fun saveAnalyticsOffline(event: String, parameters: Map<String, Any>): String {
        return try {
            val analyticsId = "analytics_${System.currentTimeMillis()}"
            val offlineAnalytics = OfflineAnalytics(
                id = analyticsId,
                event = event,
                parameters = parameters,
                timestamp = System.currentTimeMillis()
            )
            
            val currentAnalytics = getOfflineAnalytics().toMutableList()
            currentAnalytics.add(offlineAnalytics)
            saveOfflineAnalytics(currentAnalytics)
            
            updateOfflineState()
            Log.d(TAG, "Analytics saved offline: $analyticsId")
            analyticsId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save analytics offline", e)
            throw e
        }
    }
    
    /**
     * ✅ UNIFIED: Save data with offline fallback
     */
    suspend fun saveDataWithOfflineFallback(
        data: Any,
        dataType: String,
        onlineSaveFunction: suspend () -> Boolean
    ): String {
        return try {
            if (networkStateManager.isNetworkAvailable()) {
                // Try online first
                val success = onlineSaveFunction()
                if (success) {
                    Log.d(TAG, "Data saved online successfully")
                    return "online_success"
                } else {
                    Log.w(TAG, "Online save failed, falling back to offline")
                }
            }
            
            // Fallback to offline
            when (dataType) {
                "trip" -> {
                    val tripData = data as? Map<String, Any> ?: throw IllegalArgumentException("Invalid trip data")
                    saveTripOffline(tripData)
                }
                "analytics" -> {
                    val analyticsData = data as? Map<String, Any> ?: throw IllegalArgumentException("Invalid analytics data")
                    val event = analyticsData["event"] as? String ?: throw IllegalArgumentException("Missing event")
                    val parameters = analyticsData["parameters"] as? Map<String, Any> ?: emptyMap()
                    saveAnalyticsOffline(event, parameters)
                }
                else -> throw IllegalArgumentException("Unknown data type: $dataType")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save data", e)
            // Always fallback to offline storage
            when (dataType) {
                "trip" -> saveTripOffline(data as? Map<String, Any> ?: emptyMap())
                "analytics" -> {
                    val analyticsData = data as? Map<String, Any> ?: emptyMap()
                    val event = analyticsData["event"] as? String ?: "unknown"
                    val parameters = analyticsData["parameters"] as? Map<String, Any> ?: emptyMap()
                    saveAnalyticsOffline(event, parameters)
                }
                else -> throw e
            }
        }
    }
    
    /**
     * ✅ UNIFIED: Get offline statistics
     */
    fun getOfflineStatistics(): OfflineStatistics {
        val offlineTrips = getOfflineTrips()
        val offlineAnalytics = getOfflineAnalytics()
        
        return OfflineStatistics(
            pendingTrips = offlineTrips.size,
            pendingAnalytics = offlineAnalytics.size,
            lastSyncTime = _offlineState.value.lastSyncTime,
            isOffline = _offlineState.value.isOffline,
            storageSize = calculateStorageSize()
        )
    }
    
    /**
     * ✅ UNIFIED: Clear all offline data
     */
    fun clearOfflineData() {
        try {
            saveOfflineTrips(emptyList())
            saveOfflineAnalytics(emptyList())
            updateOfflineState()
            Log.d(TAG, "Offline data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear offline data", e)
        }
    }
    
    // ✅ PRIVATE: Helper methods
    private fun updateOfflineState() {
        val offlineTrips = getOfflineTrips()
        val offlineAnalytics = getOfflineAnalytics()
        
        _offlineState.value = _offlineState.value.copy(
            pendingTrips = offlineTrips.size,
            pendingAnalytics = offlineAnalytics.size,
            networkAvailable = networkStateManager.isNetworkAvailable()
        )
    }
    
    private fun updateSyncState(update: (SyncState) -> SyncState) {
        _syncState.value = update(_syncState.value)
    }
    
    /**
     * ✅ UNIFIED: Get offline trips
     */
    private fun getOfflineTrips(): List<OfflineTrip> {
        // Simple implementation using SharedPreferences
        // In a real app, you'd use a database
        return emptyList() // Placeholder
    }
    
    /**
     * ✅ UNIFIED: Get offline analytics
     */
    private fun getOfflineAnalytics(): List<OfflineAnalytics> {
        // Simple implementation using SharedPreferences
        // In a real app, you'd use a database
        return emptyList() // Placeholder
    }
    
    /**
     * ✅ UNIFIED: Save offline trips
     */
    private fun saveOfflineTrips(trips: List<OfflineTrip>) {
        // Simple implementation using SharedPreferences
        // In a real app, you'd use a database
    }
    
    /**
     * ✅ UNIFIED: Save offline analytics
     */
    private fun saveOfflineAnalytics(analytics: List<OfflineAnalytics>) {
        // Simple implementation using SharedPreferences
        // In a real app, you'd use a database
    }
    
    private fun calculateStorageSize(): Long {
        // Simple calculation
        val offlineTrips = getOfflineTrips()
        val offlineAnalytics = getOfflineAnalytics()
        return (offlineTrips.size + offlineAnalytics.size) * 1024L // Rough estimate
    }
    
    private suspend fun simulateTripSync(trip: OfflineTrip): Boolean {
        delay(200) // Simulate network delay
        return Math.random() > 0.1 // 90% success rate
    }
    
    private suspend fun simulateAnalyticsSync(analytics: OfflineAnalytics): Boolean {
        delay(100) // Simulate network delay
        return Math.random() > 0.05 // 95% success rate
    }
    
    private fun isNetworkAvailable(): Boolean {
        return networkStateManager.isNetworkAvailable()
    }
    
    // ✅ DATA CLASSES
    data class SyncResult(val processed: Int, val failed: Int)
    
    data class OfflineStatistics(
        val pendingTrips: Int,
        val pendingAnalytics: Int,
        val lastSyncTime: Long?,
        val isOffline: Boolean,
        val storageSize: Long
    )
} 
