package com.example.outofroutebuddy.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider

/**
 * 🔧 Test Utilities for Theme Testing
 * 
 * Helper utilities to manipulate and verify theme state in tests.
 * Provides easy access to theme preferences and verification methods.
 * 
 * Created: Phase 0 - Infrastructure
 * Purpose: Enable theme system testing
 */
object TestThemeUtils {
    
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME = "theme_preference"
    
    /**
     * Get SharedPreferences for theme testing
     */
    fun getThemePreferences(): SharedPreferences {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Set theme preference to a specific value
     */
    fun setTheme(theme: String) {
        getThemePreferences()
            .edit()
            .putString(KEY_THEME, theme)
            .commit() // Use commit for synchronous write in tests
    }
    
    /**
     * Get current theme preference
     */
    fun getTheme(): String {
        return getThemePreferences().getString(KEY_THEME, "light") ?: "light"
    }
    
    /**
     * Clear theme preference (reset to default)
     */
    fun clearTheme() {
        getThemePreferences()
            .edit()
            .remove(KEY_THEME)
            .commit()
    }
    
    /**
     * Clear all app settings (for test isolation)
     */
    fun clearAllSettings() {
        getThemePreferences()
            .edit()
            .clear()
            .commit()
    }
    
    /**
     * Set theme to light mode
     */
    fun setLightTheme() {
        setTheme("light")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    
    /**
     * Set theme to dark mode
     */
    fun setDarkTheme() {
        setTheme("dark")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    /**
     * Set theme to system mode
     */
    fun setSystemTheme() {
        setTheme("system")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    /**
     * Get current night mode
     */
    fun getCurrentNightMode(): Int {
        return AppCompatDelegate.getDefaultNightMode()
    }
    
    /**
     * Verify theme is light mode
     */
    fun isLightTheme(): Boolean {
        val pref = getTheme()
        val mode = getCurrentNightMode()
        return pref == "light" && mode == AppCompatDelegate.MODE_NIGHT_NO
    }
    
    /**
     * Verify theme is dark mode
     */
    fun isDarkTheme(): Boolean {
        val pref = getTheme()
        val mode = getCurrentNightMode()
        return pref == "dark" && mode == AppCompatDelegate.MODE_NIGHT_YES
    }
    
    /**
     * Verify theme follows system
     */
    fun isSystemTheme(): Boolean {
        val pref = getTheme()
        val mode = getCurrentNightMode()
        return pref == "system" && mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    
    /**
     * Assert theme is set to expected value
     */
    fun assertTheme(expected: String) {
        val actual = getTheme()
        assert(actual == expected) {
            "Expected theme '$expected' but was '$actual'"
        }
    }
    
    /**
     * Assert night mode is set to expected value
     */
    fun assertNightMode(expected: Int) {
        val actual = getCurrentNightMode()
        assert(actual == expected) {
            "Expected night mode $expected but was $actual"
        }
    }
}

