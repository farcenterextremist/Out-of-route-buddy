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
    
    // GPS Settings
    fun getGpsUpdateFrequency(): Int = prefs.getInt("gps_update_frequency", 10)
    fun setGpsUpdateFrequency(seconds: Int) = prefs.edit {putInt("gps_update_frequency", seconds)}
    
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
    
    // Advanced Settings
    fun isBatteryOptimizationEnabled(): Boolean = prefs.getBoolean("battery_optimization", true)
    fun setBatteryOptimizationEnabled(enabled: Boolean) = prefs.edit {putBoolean("battery_optimization", enabled)}
    
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


