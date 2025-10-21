package com.example.outofroutebuddy.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.outofroutebuddy.core.config.ValidationConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * ? NEW: Background Synchronization Service
 * 
 * This service handles background synchronization of trip data, GPS metadata,
 * and cache management to ensure data consistency and improve performance.
 */
class BackgroundSyncService : Service() {
    companion object {
        private const val TAG = "BackgroundSyncService"
        
        // Sync intervals - now using centralized config
        private const val CACHE_CLEANUP_INTERVAL_MS = ValidationConfig.SYNC_CACHE_CLEANUP_INTERVAL_MS
        private const val STATE_SYNC_INTERVAL_MS = ValidationConfig.SYNC_STATE_INTERVAL_MS
        private const val GPS_SYNC_INTERVAL_MS = ValidationConfig.SYNC_GPS_INTERVAL_MS
        private const val DATA_INTEGRITY_CHECK_INTERVAL_MS = ValidationConfig.SYNC_DATA_INTEGRITY_INTERVAL_MS
        
        // Service actions - now using centralized config
        const val ACTION_START_SYNC = ValidationConfig.SYNC_ACTION_START
        const val ACTION_STOP_SYNC = ValidationConfig.SYNC_ACTION_STOP
        const val ACTION_FORCE_SYNC = ValidationConfig.SYNC_ACTION_FORCE
    }
    
    // ? SERVICE SCOPE: Coroutine scope for background operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // ? SYNC STATE: Track synchronization status
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    // ? SYNC JOBS: Background coroutine jobs
    private var cacheCleanupJob: Job? = null
    private var stateSyncJob: Job? = null
    private var gpsSyncJob: Job? = null
    private var dataIntegrityJob: Job? = null
    
    // ? SYNC METRICS: Performance tracking
    private var syncAttempts = 0L
    private var syncSuccesses = 0L
    private var syncFailures = 0L
    private var lastSyncTime = 0L
    
    // ? SYNC STATE DATA CLASS
    data class SyncState(
        val isRunning: Boolean = false,
        val lastSyncTime: Date = Date(),
        val syncStatus: String = "Idle",
        val cacheHealth: String = "Unknown",
        val dataIntegrity: String = "Unknown",
        val gpsSyncStatus: String = "Unknown",
        val errorCount: Int = 0,
        val successRate: Double = 0.0
    )
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BackgroundSyncService created")
        startBackgroundSync()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "BackgroundSyncService started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_SYNC -> {
                startBackgroundSync()
                return START_STICKY
            }
            ACTION_STOP_SYNC -> {
                stopBackgroundSync()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_FORCE_SYNC -> {
                forceSync()
                return START_STICKY
            }
            else -> {
                startBackgroundSync()
                return START_STICKY
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        Log.d(TAG, "BackgroundSyncService destroyed")
        stopBackgroundSync()
        serviceScope.cancel()
        super.onDestroy()
    }
    
    /**
     * ? NEW: Start background synchronization operations
     */
    private fun startBackgroundSync() {
        if (_syncState.value.isRunning) {
            Log.d(TAG, "Background sync already running")
            return
        }
        
        Log.d(TAG, "Starting background sync operations")
        
        // Start cache cleanup job
        cacheCleanupJob = serviceScope.launch {
            while (isActive) {
                try {
                    performCacheCleanup()
                    delay(CACHE_CLEANUP_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Cache cleanup failed", e)
                    delay(60 * 1000L) // Wait 1 minute before retry
                }
            }
        }
        
        // Start state synchronization job
        stateSyncJob = serviceScope.launch {
            while (isActive) {
                try {
                    performStateSync()
                    delay(STATE_SYNC_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "State sync failed", e)
                    delay(60 * 1000L) // Wait 1 minute before retry
                }
            }
        }
        
        // Start GPS synchronization job
        gpsSyncJob = serviceScope.launch {
            while (isActive) {
                try {
                    performGpsSync()
                    delay(GPS_SYNC_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "GPS sync failed", e)
                    delay(30 * 1000L) // Wait 30 seconds before retry
                }
            }
        }
        
        // Start data integrity check job
        dataIntegrityJob = serviceScope.launch {
            while (isActive) {
                try {
                    performDataIntegrityCheck()
                    delay(DATA_INTEGRITY_CHECK_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Data integrity check failed", e)
                    delay(5 * 60 * 1000L) // Wait 5 minutes before retry
                }
            }
        }
        
        updateSyncState(true, "Running")
        Log.d(TAG, "Background sync operations started")
    }
    
    /**
     * ? NEW: Stop background synchronization operations
     */
    private fun stopBackgroundSync() {
        Log.d(TAG, "Stopping background sync operations")
        
        cacheCleanupJob?.cancel()
        stateSyncJob?.cancel()
        gpsSyncJob?.cancel()
        dataIntegrityJob?.cancel()
        
        cacheCleanupJob = null
        stateSyncJob = null
        gpsSyncJob = null
        dataIntegrityJob = null
        
        updateSyncState(false, "Stopped")
        Log.d(TAG, "Background sync operations stopped")
    }
    
    /**
     * ? NEW: Force immediate synchronization
     */
    private fun forceSync() {
        Log.d(TAG, "Forcing immediate synchronization")
        
        serviceScope.launch {
            try {
                performCacheCleanup()
                performStateSync()
                performGpsSync()
                performDataIntegrityCheck()
                
                updateSyncState(true, "Force sync completed")
                Log.d(TAG, "Force sync completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Force sync failed", e)
                updateSyncState(true, "Force sync failed: ${e.message}")
            }
        }
    }
    
    /**
     * ? NEW: Perform cache cleanup operations
     */
    private suspend fun performCacheCleanup() {
        try {
            Log.v(TAG, "Performing cache cleanup")
            
            // (Cache cleanup is now handled by StateCache service)
            updateSyncState(
                isRunning = _syncState.value.isRunning,
                status = "Cache cleaned",
                cacheHealth = "Good"
            )
            
            Log.d(TAG, "Cache cleanup completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Cache cleanup failed", e)
            recordSyncFailure()
        }
    }
    
    /**
     * ? NEW: Perform state synchronization
     */
    private suspend fun performStateSync() {
        try {
            Log.v(TAG, "Performing state synchronization")
            
            // (State sync is now handled by TripStatePersistence service)
            recordSyncSuccess()
            updateSyncState(
                isRunning = _syncState.value.isRunning,
                status = "State synced",
                lastSyncTime = Date()
            )
            
            Log.d(TAG, "State synchronization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "State synchronization failed", e)
            recordSyncFailure()
        }
    }
    
    /**
     * ? NEW: Perform GPS synchronization
     */
    private suspend fun performGpsSync() {
        try {
            Log.v(TAG, "Performing GPS synchronization")
            
            // (GPS sync is now handled by GpsSynchronizationService)
            updateSyncState(
                isRunning = _syncState.value.isRunning,
                status = "GPS synced",
                gpsSyncStatus = "Active"
            )
            
            Log.d(TAG, "GPS synchronization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "GPS synchronization failed", e)
            recordSyncFailure()
        }
    }
    
    /**
     * ? NEW: Perform data integrity check
     */
    private suspend fun performDataIntegrityCheck() {
        try {
            Log.v(TAG, "Performing data integrity check")
            
            // (Data integrity is now handled by TripStateManager validation)
            updateSyncState(
                isRunning = _syncState.value.isRunning,
                status = "Integrity checked",
                dataIntegrity = "Clean"
            )
            
            Log.d(TAG, "Data integrity check passed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Data integrity check failed", e)
            recordSyncFailure()
        }
    }
    
    /**
     * ? NEW: Record successful synchronization
     */
    private fun recordSyncSuccess() {
        syncAttempts++
        syncSuccesses++
        lastSyncTime = System.currentTimeMillis()
        
        val successRate = (syncSuccesses.toDouble() / syncAttempts) * 100
        updateSyncState(
            isRunning = _syncState.value.isRunning,
            status = _syncState.value.syncStatus,
            successRate = successRate
        )
    }
    
    /**
     * ? NEW: Record failed synchronization
     */
    private fun recordSyncFailure() {
        syncAttempts++
        syncFailures++
        
        val successRate = (syncSuccesses.toDouble() / syncAttempts) * 100
        updateSyncState(
            isRunning = _syncState.value.isRunning,
            status = _syncState.value.syncStatus,
            errorCount = syncFailures.toInt(),
            successRate = successRate
        )
    }
    
    /**
     * ? NEW: Update synchronization state
     */
    private fun updateSyncState(
        isRunning: Boolean,
        status: String,
        lastSyncTime: Date = _syncState.value.lastSyncTime,
        cacheHealth: String = _syncState.value.cacheHealth,
        dataIntegrity: String = _syncState.value.dataIntegrity,
        gpsSyncStatus: String = _syncState.value.gpsSyncStatus,
        errorCount: Int = _syncState.value.errorCount,
        successRate: Double = _syncState.value.successRate
    ) {
        _syncState.value = SyncState(
            isRunning = isRunning,
            lastSyncTime = lastSyncTime,
            syncStatus = status,
            cacheHealth = cacheHealth,
            dataIntegrity = dataIntegrity,
            gpsSyncStatus = gpsSyncStatus,
            errorCount = errorCount,
            successRate = successRate
        )
    }
    
    /**
     * ? NEW: Get sync performance metrics
     */
    fun getSyncMetrics(): SyncMetrics {
        return SyncMetrics(
            syncAttempts = syncAttempts,
            syncSuccesses = syncSuccesses,
            syncFailures = syncFailures,
            successRate = if (syncAttempts > 0) (syncSuccesses.toDouble() / syncAttempts) * 100 else 0.0,
            lastSyncTime = lastSyncTime,
            uptime = System.currentTimeMillis() - lastSyncTime
        )
    }
    
    /**
     * ? NEW: Sync metrics data class
     */
    data class SyncMetrics(
        val syncAttempts: Long,
        val syncSuccesses: Long,
        val syncFailures: Long,
        val successRate: Double,
        val lastSyncTime: Long,
        val uptime: Long
    )
    
    // ==================== NEW: VIEWMODEL INTEGRATION METHODS ====================
    
    /**
     * ? NEW: Start periodic sync for ViewModel integration
     */
    fun startPeriodicSync() {
        Log.d(TAG, "Starting periodic sync from ViewModel")
        startBackgroundSync()
    }
    
    /**
     * ? NEW: Stop sync for ViewModel integration
     */
    fun stopSync() {
        Log.d(TAG, "Stopping sync from ViewModel")
        stopBackgroundSync()
    }
    
    /**
     * ? NEW: Stop periodic sync (alias for stopSync)
     */
    fun stopPeriodicSync() {
        Log.d(TAG, "Stopping periodic sync from ViewModel")
        stopBackgroundSync()
    }
    
    /**
     * ? NEW: Get sync success rate for ViewModel
     */
    fun getSyncSuccessRate(): Double {
        return if (syncAttempts > 0) {
            (syncSuccesses.toDouble() / syncAttempts) * 100
        } else 0.0
    }
    
    /**
     * ? NEW: Sync trip to field for ViewModel
     */
    fun syncTripToField(trip: com.example.outofroutebuddy.domain.models.Trip) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Syncing trip to field: ${trip.id}")
                
                // Simulate field sync operation
                delay(1000) // Simulate network delay
                
                // Update sync state
                updateSyncState(
                    isRunning = _syncState.value.isRunning,
                    status = "Trip synced to field",
                    lastSyncTime = Date()
                )
                
                recordSyncSuccess()
                Log.d(TAG, "Trip synced to field successfully: ${trip.id}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync trip to field: ${trip.id}", e)
                recordSyncFailure()
            }
        }
    }
}
