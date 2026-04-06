package com.example.outofroutebuddy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Settings Manager
 * 
 * Manages app settings and preferences
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    // GPS Settings — ListPreference stores string seconds; preset path may store int.
    fun getGpsUpdateFrequency(): Int {
        val raw = prefs.all["gps_update_frequency"] ?: return 10
        val seconds =
            when (raw) {
                is Int -> raw
                is String -> raw.toIntOrNull() ?: 10
                else -> 10
            }
        return seconds.coerceIn(1, 600)
    }

    fun setGpsUpdateFrequency(seconds: Int) =
        prefs.edit {
            putInt("gps_update_frequency", seconds.coerceIn(1, 600))
        }
    
    fun isHighAccuracyMode(): Boolean = prefs.getBoolean("high_accuracy_mode", true)
    fun setHighAccuracyMode(enabled: Boolean) = prefs.edit {putBoolean("high_accuracy_mode", enabled)}
    
    // Display Settings
    fun getDistanceUnits(): String = prefs.getString("distance_units", "miles") ?: "miles"
    fun setDistanceUnits(units: String) = prefs.edit {putString("distance_units", units)}
    
    fun isKilometers(): Boolean = getDistanceUnits() == "kilometers"
    
    // Theme Settings
    fun getThemePreference(): String = prefs.getString("theme_preference", "system") ?: "system"
    fun setThemePreference(theme: String) = prefs.edit {putString("theme_preference", theme)}
    
    // Notification Settings
    fun areNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit {putBoolean("notifications_enabled", enabled)}
    
    fun isNotificationSoundEnabled(): Boolean = prefs.getBoolean("notification_sound", false)
    fun setNotificationSoundEnabled(enabled: Boolean) = prefs.edit {putBoolean("notification_sound", enabled)}
    
    // Trip Settings
    fun isAutoStartTripEnabled(): Boolean = prefs.getBoolean("auto_start_trip", false)
    fun setAutoStartTripEnabled(enabled: Boolean) = prefs.edit {putBoolean("auto_start_trip", enabled)}
    
    fun isAutoSaveTripEnabled(): Boolean = prefs.getBoolean("auto_save_trip", true)
    fun setAutoSaveTripEnabled(enabled: Boolean) = prefs.edit {putBoolean("auto_save_trip", enabled)}

    /**
     * When true (default): removing the app from recents during a trip does not stop GPS; service recovers for accuracy.
     * When false: task removal stops the foreground service; user can reopen and resume from persisted state.
     */
    fun shouldContinueTrackingAfterAppDismissed(): Boolean =
        prefs.getBoolean("continue_tracking_after_app_dismissed", true)
    
    // Advanced Settings
    fun isBatteryOptimizationEnabled(): Boolean = prefs.getBoolean("battery_optimization", true)
    fun setBatteryOptimizationEnabled(enabled: Boolean) = prefs.edit {putBoolean("battery_optimization", enabled)}

    /** GPS preset: power_save (30s, low accuracy), balanced (10s, high), high_accuracy (5s, high). Applied on next trip/service read. */
    fun getGpsPreset(): String = prefs.getString("gps_preset", "balanced") ?: "balanced"
    fun setGpsPreset(preset: String) {
        prefs.edit {
                putString("gps_preset", preset)
                when (preset) {
                    "power_save" -> {
                        putInt("gps_update_frequency", 30)
                        putBoolean("high_accuracy_mode", false)
                    }
                    "high_accuracy" -> {
                        putInt("gps_update_frequency", 5)
                        putBoolean("high_accuracy_mode", true)
                    }
                    else -> { // balanced
                        putInt("gps_update_frequency", 10)
                        putBoolean("high_accuracy_mode", true)
                    }
                }
            }
    }

    /** Debug only: verbose logging flag (no PII in logs). Gated by BuildConfig.DEBUG in UI. */
    fun isVerboseLoggingEnabled(): Boolean = prefs.getBoolean("verbose_logging", false)
    fun setVerboseLoggingEnabled(enabled: Boolean) = prefs.edit { putBoolean("verbose_logging", enabled) }

    fun isShowPauseButtonEnabled(): Boolean = prefs.getBoolean("show_pause_button", false)

    /** Ludacris: optional live trip card rows. Keys unchanged since first ship (`ludicrous_show_*`). */
    fun isLudacrisShowTimeZones(): Boolean = prefs.getBoolean("ludicrous_show_time_zones", false)
    fun isLudacrisShowElevation(): Boolean = prefs.getBoolean("ludicrous_show_elevation", false)
    fun isLudacrisShowMaxSpeed(): Boolean = prefs.getBoolean("ludicrous_show_max_speed", false)
    
    /**
     * Convert miles to the user's preferred unit
     */
    fun convertDistance(miles: Double): Double {
        return if (isKilometers()) miles * 1.60934 else miles
    }
    
    /**
     * Get distance unit label
     */
    fun getDistanceUnitLabel(): String {
        return if (isKilometers()) "km" else "mi"
    }
    
    /**
     * Get short distance unit label
     */
    fun getDistanceUnitShort(): String {
        return if (isKilometers()) "km" else "miles"
    }
}


