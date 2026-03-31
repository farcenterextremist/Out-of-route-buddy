package com.example.outofroutebuddy.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class TripTrackingRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (!hasActiveTripSnapshot(context)) {
            Log.d(TAG, "No active trip snapshot available for $action recovery")
            return
        }
        if (!hasForegroundLocationPermission(context)) {
            Log.w(TAG, "Skipping recovery for $action because location permission is missing")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocationPermission(context)) {
            Log.w(TAG, "Skipping recovery for $action because background location is missing")
            return
        }
        TripTrackingService.requestRecovery(context, source = action)
    }

    private fun hasActiveTripSnapshot(context: Context): Boolean {
        val servicePrefs = context.getSharedPreferences(PREFS_SERVICE_STATE, Context.MODE_PRIVATE)
        val persistencePrefs = context.getSharedPreferences(PREFS_TRIP_PERSISTENCE, Context.MODE_PRIVATE)
        return servicePrefs.getBoolean(KEY_WAS_TRACKING, false) &&
            persistencePrefs.getBoolean(KEY_TRIP_RECOVERY_AVAILABLE, false)
    }

    private fun hasForegroundLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted && coarseGranted
    }

    private fun hasBackgroundLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "TripTrackingRecovery"
        private const val PREFS_SERVICE_STATE = "trip_service_state"
        private const val PREFS_TRIP_PERSISTENCE = "trip_persistence"
        private const val KEY_WAS_TRACKING = "was_tracking"
        private const val KEY_TRIP_RECOVERY_AVAILABLE = "trip_recovery_available"
    }
}
