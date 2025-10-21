package com.example.outofroutebuddy.workers

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ NEW (#30): WorkManager Initializer
 * 
 * Centralized initialization and configuration for WorkManager-based background tasks.
 * Provides battery-efficient, constraint-aware background job scheduling.
 * 
 * Features:
 * - Automatic retry with exponential backoff
 * - Network and battery constraints
 * - Survives app restarts and device reboots
 * - Better than manual Service scheduling
 * 
 * Priority: MEDIUM
 * Impact: Battery optimization and reliability
 */
@Singleton
class WorkManagerInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WorkManagerInit"
    }
    
    /**
     * Initialize all background workers
     */
    fun initialize() {
        Log.d(TAG, "Initializing WorkManager tasks")
        
        try {
            // Schedule periodic sync (15-minute intervals)
            SyncWorker.schedulePeriodicSync(context)
            
            // Schedule cache cleanup (hourly)
            SyncWorker.scheduleCacheCleanup(context)
            
            // Schedule data integrity checks (every 6 hours)
            SyncWorker.scheduleDataIntegrityCheck(context)
            
            Log.i(TAG, "WorkManager tasks initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WorkManager tasks", e)
        }
    }
    
    /**
     * Cancel all background workers
     */
    fun cancelAll() {
        Log.d(TAG, "Cancelling all WorkManager tasks")
        SyncWorker.cancelAllWork(context)
    }
    
    /**
     * Get WorkManager configuration
     */
    fun getConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }
}

