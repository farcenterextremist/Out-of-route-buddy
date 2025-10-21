package com.example.outofroutebuddy.data

import android.content.Context
import android.content.SharedPreferences
import com.example.outofroutebuddy.domain.models.PeriodMode

/**
 * Manages user preferences and settings persistence.
 * Uses SharedPreferences to store user choices across app sessions.
 */
class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    companion object {
        private const val PREFERENCES_NAME = "OutOfRouteBuddyPreferences"
        private const val KEY_PERIOD_MODE = "period_mode"
        private const val KEY_LAST_LOADED_MILES = "last_loaded_miles"
        private const val KEY_LAST_BOUNCE_MILES = "last_bounce_miles"
        private const val KEY_TRIP_ACTIVE = "trip_active"
    }

    /**
     * Save the selected period mode
     */
    fun savePeriodMode(mode: PeriodMode) {
        sharedPreferences.edit()
            .putString(KEY_PERIOD_MODE, mode.name)
            .apply()
    }

    /**
     * Get the saved period mode, defaulting to STANDARD if not set
     */
    fun getPeriodMode(): PeriodMode {
        val modeName = sharedPreferences.getString(KEY_PERIOD_MODE, PeriodMode.STANDARD.name)
        return try {
            PeriodMode.valueOf(modeName ?: PeriodMode.STANDARD.name)
        } catch (e: IllegalArgumentException) {
            PeriodMode.STANDARD
        }
    }

    /**
     * Save the last entered loaded miles for convenience
     */
    fun saveLastLoadedMiles(miles: String) {
        sharedPreferences.edit()
            .putString(KEY_LAST_LOADED_MILES, miles)
            .apply()
    }

    /**
     * Get the last entered loaded miles
     */
    fun getLastLoadedMiles(): String {
        return sharedPreferences.getString(KEY_LAST_LOADED_MILES, "") ?: ""
    }

    /**
     * Save the last entered bounce miles for convenience
     */
    fun saveLastBounceMiles(miles: String) {
        sharedPreferences.edit()
            .putString(KEY_LAST_BOUNCE_MILES, miles)
            .apply()
    }

    /**
     * Get the last entered bounce miles
     */
    fun getLastBounceMiles(): String {
        return sharedPreferences.getString(KEY_LAST_BOUNCE_MILES, "") ?: ""
    }

    /**
     * Save whether a trip is currently active
     */
    fun saveTripActive(isActive: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_TRIP_ACTIVE, isActive)
            .apply()
    }

    /**
     * Get whether a trip is currently active
     */
    fun isTripActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_TRIP_ACTIVE, false)
    }

    /**
     * Clear all saved preferences (for testing or reset)
     */
    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }
} 
