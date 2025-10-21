package com.example.outofroutebuddy.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider

/**
 * 🔧 Test Utilities for Preference Testing
 * 
 * Helper utilities to manipulate and verify SharedPreferences in tests.
 * Provides preference manipulation helpers and assertion methods.
 * 
 * Created: Phase 0 - Infrastructure
 * Purpose: Enable preference testing
 */
object TestPreferenceUtils {
    
    private const val APP_SETTINGS_PREFS = "app_settings"
    private const val OUT_OF_ROUTE_PREFS = "OutOfRouteBuddyPreferences"
    
    /**
     * Get app settings SharedPreferences
     */
    fun getAppSettings(): SharedPreferences {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.getSharedPreferences(APP_SETTINGS_PREFS, Context.MODE_PRIVATE)
    }
    
    /**
     * Get main app SharedPreferences
     */
    fun getMainPreferences(): SharedPreferences {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.getSharedPreferences(OUT_OF_ROUTE_PREFS, Context.MODE_PRIVATE)
    }
    
    /**
     * Clear all app settings
     */
    fun clearAppSettings() {
        getAppSettings().edit().clear().commit()
    }
    
    /**
     * Clear main preferences
     */
    fun clearMainPreferences() {
        getMainPreferences().edit().clear().commit()
    }
    
    /**
     * Clear all preferences (for test isolation)
     */
    fun clearAll() {
        clearAppSettings()
        clearMainPreferences()
    }
    
    // ==================== GPS Preferences ====================
    
    fun setGpsUpdateFrequency(seconds: Int) {
        getAppSettings().edit().putInt("gps_update_frequency", seconds).commit()
    }
    
    fun getGpsUpdateFrequency(): Int {
        return getAppSettings().getInt("gps_update_frequency", 10)
    }
    
    fun setHighAccuracyMode(enabled: Boolean) {
        getAppSettings().edit().putBoolean("high_accuracy_mode", enabled).commit()
    }
    
    fun isHighAccuracyMode(): Boolean {
        return getAppSettings().getBoolean("high_accuracy_mode", true)
    }
    
    // ==================== Display Preferences ====================
    
    fun setDistanceUnits(units: String) {
        getAppSettings().edit().putString("distance_units", units).commit()
    }
    
    fun getDistanceUnits(): String {
        return getAppSettings().getString("distance_units", "miles") ?: "miles"
    }
    
    fun setKilometers() = setDistanceUnits("kilometers")
    fun setMiles() = setDistanceUnits("miles")
    fun isKilometers() = getDistanceUnits() == "kilometers"
    fun isMiles() = getDistanceUnits() == "miles"
    
    // ==================== Theme Preferences ====================
    
    fun setTheme(theme: String) {
        getAppSettings().edit().putString("theme_preference", theme).commit()
    }
    
    fun getTheme(): String {
        return getAppSettings().getString("theme_preference", "light") ?: "light"
    }
    
    // ==================== Notification Preferences ====================
    
    fun setNotificationsEnabled(enabled: Boolean) {
        getAppSettings().edit().putBoolean("notifications_enabled", enabled).commit()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return getAppSettings().getBoolean("notifications_enabled", true)
    }
    
    fun setNotificationSound(enabled: Boolean) {
        getAppSettings().edit().putBoolean("notification_sound", enabled).commit()
    }
    
    fun isNotificationSoundEnabled(): Boolean {
        return getAppSettings().getBoolean("notification_sound", false)
    }
    
    // ==================== Trip Preferences ====================
    
    fun setAutoStartTrip(enabled: Boolean) {
        getAppSettings().edit().putBoolean("auto_start_trip", enabled).commit()
    }
    
    fun isAutoStartTripEnabled(): Boolean {
        return getAppSettings().getBoolean("auto_start_trip", false)
    }
    
    fun setLastLoadedMiles(miles: String) {
        getMainPreferences().edit().putString("last_loaded_miles", miles).commit()
    }
    
    fun getLastLoadedMiles(): String {
        return getMainPreferences().getString("last_loaded_miles", "") ?: ""
    }
    
    fun setLastBounceMiles(miles: String) {
        getMainPreferences().edit().putString("last_bounce_miles", miles).commit()
    }
    
    fun getLastBounceMiles(): String {
        return getMainPreferences().getString("last_bounce_miles", "") ?: ""
    }
    
    fun setPeriodMode(mode: String) {
        getMainPreferences().edit().putString("period_mode", mode).commit()
    }
    
    fun getPeriodMode(): String {
        return getMainPreferences().getString("period_mode", "STANDARD") ?: "STANDARD"
    }
    
    // ==================== Assertion Methods ====================
    
    fun assertThemeEquals(expected: String) {
        val actual = getTheme()
        assert(actual == expected) {
            "Expected theme '$expected' but was '$actual'"
        }
    }
    
    fun assertDistanceUnitsEquals(expected: String) {
        val actual = getDistanceUnits()
        assert(actual == expected) {
            "Expected distance units '$expected' but was '$actual'"
        }
    }
    
    fun assertGpsFrequencyEquals(expected: Int) {
        val actual = getGpsUpdateFrequency()
        assert(actual == expected) {
            "Expected GPS frequency $expected but was $actual"
        }
    }
    
    fun assertNotificationsEnabled(expected: Boolean) {
        val actual = areNotificationsEnabled()
        assert(actual == expected) {
            "Expected notifications enabled=$expected but was $actual"
        }
    }
    
    fun assertAutoStartEnabled(expected: Boolean) {
        val actual = isAutoStartTripEnabled()
        assert(actual == expected) {
            "Expected auto-start enabled=$expected but was $actual"
        }
    }
    
    // ==================== Test Setup/Teardown ====================
    
    /**
     * Setup clean state before each test
     */
    fun setupTestDefaults() {
        clearAll()
        setTheme("light")
        setDistanceUnits("miles")
        setGpsUpdateFrequency(10)
        setNotificationsEnabled(true)
        setAutoStartTrip(false)
    }
    
    /**
     * Cleanup after test
     */
    fun cleanup() {
        clearAll()
    }
}

