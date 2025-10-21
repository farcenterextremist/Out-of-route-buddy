package com.example.outofroutebuddy.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.outofroutebuddy.domain.models.Trip
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🛡️ Trip Crash Recovery Manager
 * 
 * Provides automatic crash recovery for in-progress trips:
 * - Auto-saves trip state every 30 seconds
 * - Detects abnormal app termination
 * - Restores incomplete trips on app restart
 * - Prevents data loss from crashes
 * 
 * Priority: CRITICAL
 * Impact: Excellent user experience, no data loss
 */
@Singleton
class TripCrashRecoveryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TripCrashRecovery"
        private const val PREFS_NAME = "trip_crash_recovery"
        private const val KEY_APP_RUNNING = "app_running"
        private const val KEY_TRIP_STATE = "trip_state"
        private const val KEY_LAST_SAVE_TIME = "last_save_time"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val AUTO_SAVE_INTERVAL_MS = 30_000L // 30 seconds
        private const val MAX_RECOVERY_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Managed coroutine scope for auto-save
    private val recoveryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoSaveJob: Job? = null
    
    /**
     * Data class for recoverable trip state
     */
    data class RecoverableTripState(
        val loadedMiles: Double,
        val bounceMiles: Double,
        val actualMiles: Double,
        val startTime: Long,
        val isActive: Boolean,
        val currentPeriodMode: String,
        val gpsTrackingActive: Boolean,
        val saveTime: Long = System.currentTimeMillis()
    )
    
    /**
     * Initialize crash recovery on app start
     * Returns: RecoverableTripState if crash was detected, null otherwise
     */
    fun initialize(): RecoverableTripState? {
        try {
            val wasRunning = prefs.getBoolean(KEY_APP_RUNNING, false)
            
            if (wasRunning) {
                Log.w(TAG, "App was not shut down properly - potential crash detected")
                
                // Increment crash count
                val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0) + 1
                prefs.edit().putInt(KEY_CRASH_COUNT, crashCount).apply()
                
                // Try to recover trip state
                val recoveredState = recoverTripState()
                
                if (recoveredState != null) {
                    Log.i(TAG, "Trip state recovered successfully")
                    return recoveredState
                } else {
                    Log.w(TAG, "No trip state to recover or state too old")
                }
            }
            
            // Mark app as running
            prefs.edit().putBoolean(KEY_APP_RUNNING, true).apply()
            
            Log.d(TAG, "Crash recovery initialized")
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during crash recovery initialization", e)
            return null
        }
    }
    
    /**
     * Start auto-save timer for active trip
     */
    fun startAutoSave(getTripState: () -> RecoverableTripState) {
        // Cancel existing job if any
        autoSaveJob?.cancel()
        
        autoSaveJob = recoveryScope.launch {
            while (isActive) {
                try {
                    delay(AUTO_SAVE_INTERVAL_MS)
                    
                    if (isActive) {
                        val tripState = getTripState()
                        saveTripState(tripState)
                        Log.d(TAG, "Trip state auto-saved")
                    }
                } catch (e: CancellationException) {
                    Log.d(TAG, "Auto-save cancelled")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error during auto-save", e)
                }
            }
        }
        
        Log.d(TAG, "Auto-save started (interval: ${AUTO_SAVE_INTERVAL_MS}ms)")
    }
    
    /**
     * Stop auto-save timer
     */
    fun stopAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = null
        Log.d(TAG, "Auto-save stopped")
    }
    
    /**
     * Save trip state to persistent storage
     */
    fun saveTripState(tripState: RecoverableTripState) {
        try {
            val json = gson.toJson(tripState)
            prefs.edit()
                .putString(KEY_TRIP_STATE, json)
                .putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis())
                .apply()
            
            Log.v(TAG, "Trip state saved: ${tripState.actualMiles} miles")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save trip state", e)
        }
    }
    
    /**
     * Recover trip state from persistent storage
     */
    private fun recoverTripState(): RecoverableTripState? {
        try {
            val json = prefs.getString(KEY_TRIP_STATE, null) ?: return null
            val lastSaveTime = prefs.getLong(KEY_LAST_SAVE_TIME, 0)
            
            // Check if saved state is too old
            val age = System.currentTimeMillis() - lastSaveTime
            if (age > MAX_RECOVERY_AGE_MS) {
                Log.w(TAG, "Saved trip state is too old (${age / 1000}s), discarding")
                clearRecoveryData()
                return null
            }
            
            val tripState = gson.fromJson(json, RecoverableTripState::class.java)
            
            // Validate recovered state
            if (tripState.isActive && tripState.actualMiles >= 0) {
                Log.i(TAG, "Valid trip state recovered: ${tripState.actualMiles} miles")
                return tripState
            } else {
                Log.w(TAG, "Recovered trip state is invalid or not active")
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error recovering trip state", e)
            return null
        }
    }
    
    /**
     * Mark normal app shutdown (no crash)
     */
    fun markNormalShutdown() {
        try {
            prefs.edit()
                .putBoolean(KEY_APP_RUNNING, false)
                .remove(KEY_TRIP_STATE) // Clear saved state on normal exit
                .apply()
            
            Log.d(TAG, "Normal shutdown marked")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking normal shutdown", e)
        }
    }
    
    /**
     * Clear all recovery data
     */
    fun clearRecoveryData() {
        try {
            prefs.edit()
                .remove(KEY_TRIP_STATE)
                .remove(KEY_LAST_SAVE_TIME)
                .putBoolean(KEY_APP_RUNNING, false)
                .apply()
            
            Log.d(TAG, "Recovery data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing recovery data", e)
        }
    }
    
    /**
     * Get crash statistics
     */
    fun getCrashStatistics(): CrashStatistics {
        return CrashStatistics(
            totalCrashes = prefs.getInt(KEY_CRASH_COUNT, 0),
            lastSaveTime = Date(prefs.getLong(KEY_LAST_SAVE_TIME, 0)),
            hasRecoverableState = prefs.contains(KEY_TRIP_STATE)
        )
    }
    
    /**
     * Reset crash counter
     */
    fun resetCrashCounter() {
        prefs.edit().putInt(KEY_CRASH_COUNT, 0).apply()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopAutoSave()
        recoveryScope.cancel()
        Log.d(TAG, "Crash recovery manager cleaned up")
    }
    
    /**
     * Crash statistics data class
     */
    data class CrashStatistics(
        val totalCrashes: Int,
        val lastSaveTime: Date,
        val hasRecoverableState: Boolean
    )
}

