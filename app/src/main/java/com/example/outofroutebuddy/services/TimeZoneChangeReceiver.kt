package com.example.outofroutebuddy.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import java.util.TimeZone

/**
 * BroadcastReceiver for handling time zone changes
 * 
 * This receiver is registered globally in the manifest to ensure
 * time zone changes are handled even when the app is not running.
 * 
 * Features:
 * - Minimal battery impact (lightweight processing)
 * - No UI changes (data-only updates)
 * - Error handling and logging
 * - Focus on data consistency
 */
class TimeZoneChangeReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "TimeZoneChangeReceiver"
        private const val ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                ACTION_TIMEZONE_CHANGED -> {
                    handleTimeZoneChange(context)
                }
                else -> {
                    Log.d(TAG, "Received unexpected action: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling time zone change", e)
            // Don't crash the receiver - just log the error
        }
    }
    
    /**
     * Handle time zone change with minimal impact
     */
    private fun handleTimeZoneChange(context: Context) {
        try {
            val newTimeZone = TimeZone.getDefault()
            val timeZoneId = newTimeZone.id
            val offset = newTimeZone.getOffset(System.currentTimeMillis())
            
            Log.i(TAG, "Time zone changed to: $timeZoneId (offset: ${offset}ms)")
            
            // Update any cached time zone data
            val prefs = context.getSharedPreferences("TimeZonePrefs", Context.MODE_PRIVATE)
            prefs.edit {
                    putString("last_timezone", timeZoneId)
                    .putLong("last_timezone_change", System.currentTimeMillis())
                    .putInt("timezone_offset", offset)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time zone preferences", e)
        }
    }
    
    /**
     * Update any cached time zone data
     */
    private fun updateTimeZoneData(context: Context, timeZoneId: String, offset: Int) {
        try {
            // Store the new time zone info in SharedPreferences for reference
            val prefs = context.getSharedPreferences("TimeZonePrefs", Context.MODE_PRIVATE)
            prefs.edit {
                    putString("last_timezone", timeZoneId)
                    .putLong("last_timezone_change", System.currentTimeMillis())
                    .putInt("timezone_offset", offset)
                }
                
            Log.d(TAG, "Updated time zone data: $timeZoneId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time zone data", e)
        }
    }
} 
