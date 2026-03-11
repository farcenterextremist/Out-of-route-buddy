package com.example.outofroutebuddy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.outofroutebuddy.util.AppLogger
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
        private const val TAG = "PreferencesManager"
        private const val PREFERENCES_NAME = "OutOfRouteBuddyPreferences"
        private const val KEY_PERIOD_MODE = "period_mode"
        private const val KEY_HAS_SEEN_PERIOD_ONBOARDING = "has_seen_period_onboarding"
        private const val KEY_LAST_LOADED_MILES = "last_loaded_miles"
        private const val KEY_LAST_BOUNCE_MILES = "last_bounce_miles"
        private const val KEY_TRIP_ACTIVE = "trip_active"
        private const val KEY_LAST_AUTO_PRUNE_TIMESTAMP_MS = "last_auto_prune_timestamp_ms"
    }

    /**
     * Save the selected period mode
     */
    fun savePeriodMode(mode: PeriodMode) {
        sharedPreferences.edit {
                putString(KEY_PERIOD_MODE, mode.name)
            }
    }

    /**
     * Get the saved period mode, defaulting to STANDARD if not set or invalid.
     * L1: Logs at debug when using default (no saved preference); logs at warn when value invalid.
     */
    fun getPeriodMode(): PeriodMode {
        val modeName = sharedPreferences.getString(KEY_PERIOD_MODE, null)
        if (modeName.isNullOrEmpty()) {
            AppLogger.d(TAG, "Using default period mode (no saved preference)")
            return PeriodMode.STANDARD
        }
        return try {
            PeriodMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            AppLogger.w(TAG, "Invalid period mode '$modeName', defaulting to STANDARD", e)
            PeriodMode.STANDARD
        }
    }

    fun hasSeenPeriodOnboarding(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_SEEN_PERIOD_ONBOARDING, false)

    fun setHasSeenPeriodOnboarding(seen: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HAS_SEEN_PERIOD_ONBOARDING, seen) }
    }

    /**
     * Save the last entered loaded miles for convenience
     */
    fun saveLastLoadedMiles(miles: String) {
        sharedPreferences.edit {
                putString(KEY_LAST_LOADED_MILES, miles)
            }
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
        sharedPreferences.edit {
                putString(KEY_LAST_BOUNCE_MILES, miles)
            }
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
        sharedPreferences.edit {
                putBoolean(KEY_TRIP_ACTIVE, isActive)
            }
    }

    /**
     * Get whether a trip is currently active
     */
    fun isTripActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_TRIP_ACTIVE, false)
    }

    /**
     * Save last successful automatic prune run timestamp (epoch millis).
     */
    fun saveLastAutoPruneTimestamp(timestampMs: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_AUTO_PRUNE_TIMESTAMP_MS, timestampMs)
        }
    }

    /**
     * Get last successful automatic prune run timestamp.
     * Returns 0L when never run.
     */
    fun getLastAutoPruneTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_AUTO_PRUNE_TIMESTAMP_MS, 0L)
    }

    /**
     * Clear all saved preferences (for testing or reset)
     */
    fun clearAllPreferences() {
        sharedPreferences.edit {clear()}
    }
} 
