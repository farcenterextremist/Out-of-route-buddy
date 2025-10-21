package com.example.outofroutebuddy.services

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.data.NetworkStateManager
import com.example.outofroutebuddy.data.OfflineDataManager
import com.example.outofroutebuddy.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * ✅ NEW: Offline Service Coordinator
 * 
 * This coordinator manages all offline-related services and provides a unified
 * interface for offline operations, data synchronization, and offline state management.
 */
class OfflineServiceCoordinator(
    private val context: Context,
    private val networkStateManager: NetworkStateManager,
    private val offlineDataManager: OfflineDataManager,
    private val preferencesManager: PreferencesManager,
    private val offlineSyncService: OfflineSyncService,
    private val simpleOfflineService: SimpleOfflineService,
    private val standaloneOfflineService: StandaloneOfflineService
) {
    companion object {
        private const val TAG = "OfflineServiceCoordinator"
    }
    
    // ✅ COORDINATOR STATE: Track offline coordination status
    private val _coordinatorState = MutableStateFlow(OfflineCoordinatorState())
    val coordinatorState: StateFlow<OfflineCoordinatorState> = _coordinatorState.asStateFlow()
    
    // ✅ OFFLINE STATE: Track current offline status
    private val _offlineState = MutableStateFlow(OfflineState())
    val offlineState: StateFlow<OfflineState> = _offlineState.asStateFlow()
    
    // ✅ SYNC STATE: Track synchronization status
    private val _syncState = MutableStateFlow(OfflineSyncState())
    val syncState: StateFlow<OfflineSyncState> = _syncState.asStateFlow()
    
    /**
     * ✅ NEW: Offline coordinator state data class
     */
    data class OfflineCoordinatorState(
        val isCoordinating: Boolean = false,
        val lastAction: String = "None",
        val lastActionTime: Date = Date(),
        val errorCount: Int = 0,
        val successCount: Int = 0,
        val isHealthy: Boolean = true,
        val servicesActive: List<String> = emptyList()
    )
    
    /**
     * ✅ NEW: Offline state data class
     */
    data class OfflineState(
        val isOfflineMode: Boolean = false,
        val networkAvailable: Boolean = true,
        val offlineDataCount: Int = 0,
        val pendingSyncCount: Int = 0,
        val failedSyncCount: Int = 0,
        val lastOfflineSave: Date = Date(),
        val offlineStorageSize: Long = 0L,
        val isOfflineServiceActive: Boolean = false
    )
    
    /**
     * ✅ NEW: Offline sync state data class
     */
    data class OfflineSyncState(
        val isSyncing: Boolean = false,
        val lastSyncTime: Date? = null,
        val syncProgress: Double = 0.0,
        val totalItems: Int = 0,
        val processedItems: Int = 0,
        val failedItems: Int = 0,
        val currentOperation: String? = null,
        val errorMessage: String? = null,
        val syncType: String = "None"
    )
    
    /**
     * ✅ NEW: Start offline coordination
     */
    fun startOfflineCoordination(): Boolean {
        return try {
            Log.d(TAG, "Starting offline coordination")
            
            // Start offline data manager
            offlineDataManager.initializeOfflineStorage()
            
            // Start network state monitoring
            networkStateManager.startMonitoring()
            
            // Update coordinator state
            _coordinatorState.value = OfflineCoordinatorState(
                isCoordinating = true,
                lastAction = "Offline coordination started",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1,
                isHealthy = true,
                servicesActive = listOf("OfflineDataManager", "NetworkStateManager")
            )
            
            // Update offline state
            _offlineState.value = OfflineState(
                isOfflineMode = !networkStateManager.isNetworkAvailable(),
                networkAvailable = networkStateManager.isNetworkAvailable(),
                offlineDataCount = offlineDataManager.getOfflineDataCount(),
                pendingSyncCount = offlineDataManager.getPendingSyncCount(),
                failedSyncCount = offlineDataManager.getFailedSyncCount(),
                isOfflineServiceActive = true
            )
            
            Log.d(TAG, "Offline coordination started successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start offline coordination", e)
            updateCoordinatorError("Failed to start offline coordination: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Stop offline coordination
     */
    fun stopOfflineCoordination(): Boolean {
        return try {
            Log.d(TAG, "Stopping offline coordination")
            
            // Stop network state monitoring
            networkStateManager.stopMonitoring()
            
            // Update coordinator state
            _coordinatorState.value = _coordinatorState.value.copy(
                isCoordinating = false,
                lastAction = "Offline coordination stopped",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1,
                servicesActive = emptyList()
            )
            
            // Update offline state
            _offlineState.value = _offlineState.value.copy(
                isOfflineServiceActive = false
            )
            
            Log.d(TAG, "Offline coordination stopped successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop offline coordination", e)
            updateCoordinatorError("Failed to stop offline coordination: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Start manual synchronization
     */
    fun startManualSync(syncType: OfflineSyncService.SyncType = OfflineSyncService.SyncType.FULL): Boolean {
        return try {
            Log.d(TAG, "Starting manual sync: $syncType")
            
            // Start sync service
            offlineSyncService.startManualSync(syncType)
            
            // Update sync state
            _syncState.value = OfflineSyncState(
                isSyncing = true,
                currentOperation = "Starting sync",
                syncType = syncType.name
            )
            
            // Update coordinator state
            _coordinatorState.value = _coordinatorState.value.copy(
                lastAction = "Manual sync started: $syncType",
                lastActionTime = Date(),
                successCount = _coordinatorState.value.successCount + 1
            )
            
            Log.d(TAG, "Manual sync started successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start manual sync", e)
            updateCoordinatorError("Failed to start manual sync: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Save data offline
     */
    fun saveDataOffline(data: Any, dataType: String): Boolean {
        return try {
            Log.d(TAG, "Saving data offline: $dataType")
            
            // Save to offline storage
            val success = offlineDataManager.saveOfflineData(data, dataType)
            
            if (success) {
                // Update offline state
                _offlineState.value = _offlineState.value.copy(
                    offlineDataCount = offlineDataManager.getOfflineDataCount(),
                    lastOfflineSave = Date()
                )
                
                // Update coordinator state
                _coordinatorState.value = _coordinatorState.value.copy(
                    lastAction = "Data saved offline: $dataType",
                    lastActionTime = Date(),
                    successCount = _coordinatorState.value.successCount + 1
                )
                
                Log.d(TAG, "Data saved offline successfully")
            }
            
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save data offline", e)
            updateCoordinatorError("Failed to save data offline: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Get offline data
     */
    fun getOfflineData(dataType: String): List<Any> {
        return try {
            offlineDataManager.getOfflineData(dataType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offline data", e)
            emptyList()
        }
    }
    
    /**
     * ✅ NEW: Clear offline data
     */
    fun clearOfflineData(dataType: String? = null): Boolean {
        return try {
            Log.d(TAG, "Clearing offline data: ${dataType ?: "all"}")
            
            val success = if (dataType != null) {
                offlineDataManager.clearOfflineData(dataType)
            } else {
                offlineDataManager.clearAllOfflineData()
            }
            
            if (success) {
                // Update offline state
                _offlineState.value = _offlineState.value.copy(
                    offlineDataCount = offlineDataManager.getOfflineDataCount()
                )
                
                // Update coordinator state
                _coordinatorState.value = _coordinatorState.value.copy(
                    lastAction = "Offline data cleared: ${dataType ?: "all"}",
                    lastActionTime = Date(),
                    successCount = _coordinatorState.value.successCount + 1
                )
                
                Log.d(TAG, "Offline data cleared successfully")
            }
            
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear offline data", e)
            updateCoordinatorError("Failed to clear offline data: ${e.message}")
            false
        }
    }
    
    /**
     * ✅ NEW: Check network availability
     */
    fun isNetworkAvailable(): Boolean {
        return networkStateManager.isNetworkAvailable()
    }
    
    /**
     * ✅ NEW: Get offline metrics
     */
    fun getOfflineMetrics(): OfflineMetrics {
        val offlineState = _offlineState.value
        val syncState = _syncState.value
        
        return OfflineMetrics(
            offlineDataCount = offlineState.offlineDataCount,
            pendingSyncCount = offlineState.pendingSyncCount,
            failedSyncCount = offlineState.failedSyncCount,
            networkAvailable = offlineState.networkAvailable,
            isOfflineMode = offlineState.isOfflineMode,
            lastSyncTime = syncState.lastSyncTime,
            syncProgress = syncState.syncProgress,
            isSyncing = syncState.isSyncing,
            offlineStorageSize = offlineState.offlineStorageSize
        )
    }
    
    /**
     * ✅ NEW: Check offline service health
     */
    fun checkOfflineServiceHealth(): OfflineServiceHealth {
        val coordinatorState = _coordinatorState.value
        val offlineState = _offlineState.value
        
        return OfflineServiceHealth(
            isHealthy = coordinatorState.isHealthy && offlineState.isOfflineServiceActive,
            errorRate = if (coordinatorState.successCount + coordinatorState.errorCount > 0) {
                coordinatorState.errorCount.toDouble() / (coordinatorState.successCount + coordinatorState.errorCount)
            } else 0.0,
            lastError = if (coordinatorState.lastAction.startsWith("Error:")) coordinatorState.lastAction else null,
            uptime = Date().time - coordinatorState.lastActionTime.time,
            offlineDataCount = offlineState.offlineDataCount,
            pendingSyncCount = offlineState.pendingSyncCount,
            networkAvailable = offlineState.networkAvailable
        )
    }
    
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
     * ✅ NEW: Offline metrics data class
     */
    data class OfflineMetrics(
        val offlineDataCount: Int,
        val pendingSyncCount: Int,
        val failedSyncCount: Int,
        val networkAvailable: Boolean,
        val isOfflineMode: Boolean,
        val lastSyncTime: Date?,
        val syncProgress: Double,
        val isSyncing: Boolean,
        val offlineStorageSize: Long
    )
    
    /**
     * ✅ NEW: Offline service health data class
     */
    data class OfflineServiceHealth(
        val isHealthy: Boolean,
        val errorRate: Double,
        val lastError: String?,
        val uptime: Long,
        val offlineDataCount: Int,
        val pendingSyncCount: Int,
        val networkAvailable: Boolean
    )
} 
