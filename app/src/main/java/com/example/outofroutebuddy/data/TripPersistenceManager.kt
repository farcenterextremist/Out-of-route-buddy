package com.example.outofroutebuddy.data

import android.content.Context
import com.example.outofroutebuddy.util.AppLogger
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ NEW: Trip Persistence Manager for Phone Restart & App Closing Recovery
 *
 * This class handles saving and loading trip state to survive:
 * - Phone restarts
 * - App closing/killing
 * - System crashes
 * - Memory pressure
 *
 * Uses SharedPreferences for reliable persistence across app lifecycle events.
 */
@Singleton
class TripPersistenceManager @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "TripPersistenceManager"
        
        // SharedPreferences keys
        private const val KEY_ACTIVE_TRIP = "active_trip_data"
        private const val KEY_TRIP_START_TIME = "trip_start_time"
        private const val KEY_TRIP_LOADED_MILES = "trip_loaded_miles"
        private const val KEY_TRIP_BOUNCE_MILES = "trip_bounce_miles"
        private const val KEY_TRIP_ACTUAL_MILES = "trip_actual_miles"
        private const val KEY_TRIP_LAST_LOCATION = "trip_last_location"
        private const val KEY_TRIP_GPS_METADATA = "trip_gps_metadata"
        private const val KEY_TRIP_RECOVERY_AVAILABLE = "trip_recovery_available"
        private const val KEY_TRIP_IS_PAUSED = "trip_is_paused"
        private const val KEY_BG_TRACKING_DEGRADED = "bg_tracking_degraded"
        private const val KEY_BG_TRACKING_REASONS = "bg_tracking_reasons"

        // Recovery timeout (24 hours)
        private const val RECOVERY_TIMEOUT_MS = 24 * 60 * 60 * 1000L
    }

    private val gson = Gson()

    /**
     * ✅ Save active trip state for recovery
     */
    fun saveActiveTripState(
        trip: Trip,
        loadedMiles: Double,
        bounceMiles: Double,
        actualMiles: Double,
        lastLocation: LocationData? = null,
        gpsMetadata: GpsMetadata? = null,
        isPaused: Boolean? = null,
        backgroundTrackingDegraded: Boolean = false,
        backgroundTrackingReasons: List<String> = emptyList(),
    ) {
        try {
            AppLogger.d(TAG,"Saving active trip state for recovery")
            
            val prefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Save trip data
            editor.putString(KEY_ACTIVE_TRIP, gson.toJson(trip))
            trip.startTime?.let { startTime ->
                editor.putLong(KEY_TRIP_START_TIME, startTime.time)
            }
            editor.putFloat(KEY_TRIP_LOADED_MILES, loadedMiles.toFloat())
            editor.putFloat(KEY_TRIP_BOUNCE_MILES, bounceMiles.toFloat())
            editor.putFloat(KEY_TRIP_ACTUAL_MILES, actualMiles.toFloat())
            
            // Save last location if available
            lastLocation?.let { location ->
                editor.putString(KEY_TRIP_LAST_LOCATION, gson.toJson(location))
            }
            
            // Save GPS metadata if available
            gpsMetadata?.let { metadata ->
                editor.putString(KEY_TRIP_GPS_METADATA, gson.toJson(metadata))
            }
            
            // Mark recovery as available
            editor.putBoolean(KEY_TRIP_RECOVERY_AVAILABLE, true)

            // Persist pause state so it survives app close/reopen (only when provided)
            isPaused?.let { editor.putBoolean(KEY_TRIP_IS_PAUSED, it) }
            editor.putBoolean(KEY_BG_TRACKING_DEGRADED, backgroundTrackingDegraded)
            editor.putString(KEY_BG_TRACKING_REASONS, gson.toJson(backgroundTrackingReasons))

            // ✅ FIX: Use commit() instead of apply() to ensure data is written to disk immediately
            // This prevents data loss when app is closed immediately after starting a trip
            val success = editor.commit()
            
            if (success) {
                AppLogger.d(TAG,"Trip state saved successfully")
            } else {
                AppLogger.e(TAG, "Failed to save trip state: commit() returned false")
            }
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save active trip state", e)
        }
    }

    /**
     * ✅ Load saved trip state for recovery
     */
    fun loadSavedTripState(): SavedTripState? {
        return try {
            AppLogger.d(TAG,"Loading saved trip state for recovery")
            
            val prefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            
            // Check if recovery is available
            if (!prefs.getBoolean(KEY_TRIP_RECOVERY_AVAILABLE, false)) {
                AppLogger.d(TAG,"No trip recovery data available")
                return null
            }
            
            // Check if recovery data is still valid (not too old)
            val startTime = prefs.getLong(KEY_TRIP_START_TIME, 0L)
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - startTime > RECOVERY_TIMEOUT_MS) {
                AppLogger.d(TAG,"Trip recovery data expired, clearing")
                clearSavedTripState()
                return null
            }
            
            // Load trip data
            val tripJson = prefs.getString(KEY_ACTIVE_TRIP, null)
            if (tripJson == null) {
                AppLogger.w(TAG, "No trip data found in recovery")
                return null
            }
            
            val trip = gson.fromJson(tripJson, Trip::class.java)
            val loadedMiles = prefs.getFloat(KEY_TRIP_LOADED_MILES, 0f).toDouble()
            val bounceMiles = prefs.getFloat(KEY_TRIP_BOUNCE_MILES, 0f).toDouble()
            val actualMiles = prefs.getFloat(KEY_TRIP_ACTUAL_MILES, 0f).toDouble()
            
            // Load optional data
            val lastLocationJson = prefs.getString(KEY_TRIP_LAST_LOCATION, null)
            val lastLocation = lastLocationJson?.let { gson.fromJson(it, LocationData::class.java) }
            
            val gpsMetadataJson = prefs.getString(KEY_TRIP_GPS_METADATA, null)
            val gpsMetadata = gpsMetadataJson?.let { gson.fromJson(it, GpsMetadata::class.java) }

            val isPaused = prefs.getBoolean(KEY_TRIP_IS_PAUSED, false)
            val backgroundTrackingDegraded = prefs.getBoolean(KEY_BG_TRACKING_DEGRADED, false)
            val backgroundTrackingReasonsJson = prefs.getString(KEY_BG_TRACKING_REASONS, null)
            val backgroundTrackingReasons =
                backgroundTrackingReasonsJson?.let {
                    gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
                }.orEmpty()

            val savedState = SavedTripState(
                trip = trip,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                lastLocation = lastLocation,
                gpsMetadata = gpsMetadata,
                startTime = Date(startTime),
                recoveryTime = Date(currentTime),
                isPaused = isPaused,
                backgroundTrackingDegraded = backgroundTrackingDegraded,
                backgroundTrackingReasons = backgroundTrackingReasons,
            )
            
            AppLogger.d(TAG,"Trip state loaded successfully")
            savedState
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load saved trip state", e)
            null
        }
    }

    /**
     * ✅ Clear saved trip state (after successful recovery or trip completion)
     */
    fun clearSavedTripState() {
        try {
            AppLogger.d(TAG, "Clearing saved trip state")
            
            val prefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            editor.remove(KEY_ACTIVE_TRIP)
            editor.remove(KEY_TRIP_START_TIME)
            editor.remove(KEY_TRIP_LOADED_MILES)
            editor.remove(KEY_TRIP_BOUNCE_MILES)
            editor.remove(KEY_TRIP_ACTUAL_MILES)
            editor.remove(KEY_TRIP_LAST_LOCATION)
            editor.remove(KEY_TRIP_GPS_METADATA)
            editor.remove(KEY_TRIP_RECOVERY_AVAILABLE)
            editor.remove(KEY_TRIP_IS_PAUSED)
            editor.remove(KEY_BG_TRACKING_DEGRADED)
            editor.remove(KEY_BG_TRACKING_REASONS)

            // ✅ FIX: Use commit() to ensure data is deleted from disk immediately
            val success = editor.commit()
            
            if (success) {
                AppLogger.d(TAG, "Trip state cleared successfully")
            } else {
                AppLogger.e(TAG, "Failed to clear trip state: commit() returned false")
            }
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to clear saved trip state", e)
        }
    }

    /**
     * ✅ Check if trip recovery is available
     */
    fun isRecoveryAvailable(): Boolean {
        return try {
            val prefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            val isAvailable = prefs.getBoolean(KEY_TRIP_RECOVERY_AVAILABLE, false)
            
            if (isAvailable) {
                // Check if data is still valid
                val startTime = prefs.getLong(KEY_TRIP_START_TIME, 0L)
                val currentTime = System.currentTimeMillis()
                val isValid = currentTime - startTime <= RECOVERY_TIMEOUT_MS
                
                if (!isValid) {
                    AppLogger.d(TAG, "Recovery data expired, clearing")
                    clearSavedTripState()
                    return false
                }
            }
            
            isAvailable
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to check recovery availability", e)
            false
        }
    }

    /**
     * ✅ Update trip progress (called during active trip)
     */
    fun updateTripProgress(actualMiles: Double, lastLocation: LocationData? = null) {
        try {
            if (!isRecoveryAvailable()) return
            
            val prefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            editor.putFloat(KEY_TRIP_ACTUAL_MILES, actualMiles.toFloat())
            
            lastLocation?.let { location ->
                editor.putString(KEY_TRIP_LAST_LOCATION, gson.toJson(location))
            }
            
            // ✅ FIX: Use commit() to ensure progress updates are written to disk immediately
            val success = editor.commit()
            
            if (success) {
                AppLogger.d(TAG, "Trip progress updated")
            } else {
                AppLogger.e(TAG, "Failed to update trip progress: commit() returned false")
            }
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update trip progress", e)
        }
    }

    /**
     * ✅ Data class for saved trip state.
     * Explicit serialVersionUID so adding isPaused does not break deserialization from older app versions.
     */
    data class SavedTripState(
        val trip: Trip,
        val loadedMiles: Double,
        val bounceMiles: Double,
        val actualMiles: Double,
        val lastLocation: LocationData? = null,
        val gpsMetadata: GpsMetadata? = null,
        val startTime: Date,
        val recoveryTime: Date,
        val isPaused: Boolean = false,
        val backgroundTrackingDegraded: Boolean = false,
        val backgroundTrackingReasons: List<String> = emptyList(),
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * ✅ Location data for GPS tracking
     */
    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Date,
        val speed: Float = 0f
    )

    /**
     * ✅ GPS metadata for trip analysis
     */
    data class GpsMetadata(
        val totalPoints: Int = 0,
        val validPoints: Int = 0,
        val rejectedPoints: Int = 0,
        val avgAccuracy: Double = 0.0,
        val minAccuracy: Double = 0.0,
        val maxAccuracy: Double = 0.0,
        val avgSpeed: Double = 0.0,
        val maxSpeed: Double = 0.0,
        val locationJumps: Int = 0,
        val accuracyWarnings: Int = 0,
        val speedAnomalies: Int = 0,
        val interruptions: Int = 0
    )
}
