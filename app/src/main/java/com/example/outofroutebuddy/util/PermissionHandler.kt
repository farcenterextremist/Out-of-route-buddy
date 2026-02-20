package com.example.outofroutebuddy.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 🔐 Permission Handler
 * 
 * Handles permission requests and graceful denial scenarios.
 * 
 * ✅ NEW (#17): Graceful Permission Denial
 * 
 * Features:
 * - Check permission status
 * - Handle mid-trip permission revocation
 * - Show user guidance
 * - Save partial trip data
 * - Allow trip resumption
 * 
 * Priority: MEDIUM
 * Impact: Better UX when permissions are denied/revoked
 */
object PermissionHandler {
    
    private const val TAG = "PermissionHandler"
    
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    const val BACKGROUND_LOCATION_REQUEST_CODE = 1002
    
    /**
     * Check if fine location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if coarse location permission is granted
     */
    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if background location permission is granted (Android 10+)
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on Android 9 and below
        }
    }
    
    /**
     * Check if all required location permissions are granted
     */
    fun hasAllLocationPermissions(context: Context): Boolean {
        return hasLocationPermission(context) && hasCoarseLocationPermission(context)
    }
    
    /**
     * Request location permissions
     */
    fun requestLocationPermissions(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            LOCATION_PERMISSION_REQUEST_CODE
        )
        
        Log.d(TAG, "Location permissions requested")
    }
    
    /**
     * Request background location permission (Android 10+)
     */
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_REQUEST_CODE
            )
            Log.d(TAG, "Background location permission requested")
        } else {
            Log.d(TAG, "Background location permission not required on this Android version")
        }
    }
    
    /**
     * Check if should show rationale for permission
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Get user-friendly permission status message
     */
    fun getPermissionStatusMessage(context: Context): String {
        return when {
            !hasLocationPermission(context) -> {
                "Location permission is required to track trips"
            }
            !hasBackgroundLocationPermission(context) -> {
                "Background location permission recommended for continuous tracking"
            }
            else -> {
                "All permissions granted"
            }
        }
    }
    
    /**
     * Permission status
     */
    data class PermissionStatus(
        val hasFineLocation: Boolean,
        val hasCoarseLocation: Boolean,
        val hasBackgroundLocation: Boolean,
        val allGranted: Boolean,
        val message: String
    )
    
    /**
     * Get comprehensive permission status
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        val fine = hasLocationPermission(context)
        val coarse = hasCoarseLocationPermission(context)
        val background = hasBackgroundLocationPermission(context)
        
        return PermissionStatus(
            hasFineLocation = fine,
            hasCoarseLocation = coarse,
            hasBackgroundLocation = background,
            allGranted = fine && coarse,
            message = getPermissionStatusMessage(context)
        )
    }
    
    /**
     * Handle permission revocation during active trip
     * 
     * @return Guidance message for user
     */
    fun handlePermissionRevocation(): String {
        Log.w(TAG, "⚠️ Location permission was revoked during trip")
        return "Location permission was revoked. Trip paused. Please grant permission to continue."
    }
}










