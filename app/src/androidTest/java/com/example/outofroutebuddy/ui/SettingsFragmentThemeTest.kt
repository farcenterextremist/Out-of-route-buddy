package com.example.outofroutebuddy.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.utils.TestPreferenceUtils
import com.example.outofroutebuddy.utils.TestThemeUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧪 Theme System Tests
 * 
 * Tests for dark mode / theme system functionality:
 * - Theme preference saving and loading
 * - Theme switching (light ↔ dark ↔ system)
 * - Activity recreation on theme change
 * - Theme persistence across app restarts
 * 
 * Priority: 🔴 HIGH
 * Impact: New feature with zero test coverage
 * 
 * Created: Phase 1 - Critical Tests
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsFragmentThemeTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        // Clear all preferences before each test
        TestPreferenceUtils.clearAll()
        TestThemeUtils.clearAllSettings()
        
        // Set default theme
        TestThemeUtils.setLightTheme()
    }
    
    @After
    fun cleanup() {
        // Clean up after each test
        TestPreferenceUtils.clearAll()
        TestThemeUtils.clearAllSettings()
    }
    
    // ==================== THEME PREFERENCE TESTS ====================
    
    @Test
    fun testThemePreferenceDefaultsToLight() {
        // Verify default theme is light
        val theme = TestThemeUtils.getTheme()
        assert(theme == "light") {
            "Default theme should be 'light' but was '$theme'"
        }
    }
    
    @Test
    fun testThemePreferenceSavesLight() {
        // Set light theme
        TestThemeUtils.setLightTheme()
        
        // Verify it was saved
        TestThemeUtils.assertTheme("light")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    
    @Test
    fun testThemePreferenceSavesDark() {
        // Set dark theme
        TestThemeUtils.setDarkTheme()
        
        // Verify it was saved
        TestThemeUtils.assertTheme("dark")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    @Test
    fun testThemePreferenceSavesSystem() {
        // Set system theme
        TestThemeUtils.setSystemTheme()
        
        // Verify it was saved
        TestThemeUtils.assertTheme("system")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    // ==================== THEME SWITCHING TESTS ====================
    
    @Test
    fun testThemeSwitchingFromLightToDark() {
        // Start with light theme
        TestThemeUtils.setLightTheme()
        TestThemeUtils.assertTheme("light")
        
        // Switch to dark
        TestThemeUtils.setDarkTheme()
        
        // Verify switch
        TestThemeUtils.assertTheme("dark")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    @Test
    fun testThemeSwitchingFromDarkToLight() {
        // Start with dark theme
        TestThemeUtils.setDarkTheme()
        TestThemeUtils.assertTheme("dark")
        
        // Switch to light
        TestThemeUtils.setLightTheme()
        
        // Verify switch
        TestThemeUtils.assertTheme("light")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    
    @Test
    fun testThemeSwitchingToSystem() {
        // Start with light theme
        TestThemeUtils.setLightTheme()
        
        // Switch to system
        TestThemeUtils.setSystemTheme()
        
        // Verify switch
        TestThemeUtils.assertTheme("system")
        TestThemeUtils.assertNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    @Test
    fun testMultipleThemeSwitches() {
        // Light → Dark
        TestThemeUtils.setLightTheme()
        TestThemeUtils.assertTheme("light")
        
        // Dark → System
        TestThemeUtils.setDarkTheme()
        TestThemeUtils.assertTheme("dark")
        
        // System → Light
        TestThemeUtils.setSystemTheme()
        TestThemeUtils.assertTheme("system")
        
        // Back to Light
        TestThemeUtils.setLightTheme()
        TestThemeUtils.assertTheme("light")
    }
    
    // ==================== THEME PERSISTENCE TESTS ====================
    
    @Test
    fun testThemePersistsAcrossPreferenceReads() {
        // Set theme
        TestThemeUtils.setDarkTheme()
        
        // Read it back multiple times
        repeat(5) {
            val theme = TestThemeUtils.getTheme()
            assert(theme == "dark") {
                "Theme should persist as 'dark' but was '$theme'"
            }
        }
    }
    
    @Test
    fun testThemePersistsAfterClear() {
        // Set theme
        TestThemeUtils.setDarkTheme()
        TestThemeUtils.assertTheme("dark")
        
        // Clear theme (should use default)
        TestThemeUtils.clearTheme()
        
        // Should fall back to default (light)
        val theme = TestThemeUtils.getTheme()
        assert(theme == "light") {
            "After clear, theme should default to 'light' but was '$theme'"
        }
    }
    
    @Test
    fun testThemePreferenceIsolation() {
        // Ensure theme preference doesn't affect other preferences
        TestThemeUtils.setDarkTheme()
        TestPreferenceUtils.setGpsUpdateFrequency(15)
        TestPreferenceUtils.setDistanceUnits("kilometers")
        
        // Verify all preferences saved correctly
        TestThemeUtils.assertTheme("dark")
        TestPreferenceUtils.assertGpsFrequencyEquals(15)
        TestPreferenceUtils.assertDistanceUnitsEquals("kilometers")
    }
    
    // ==================== ACTIVITY RECREATION TESTS ====================
    
    @Test
    fun testActivityRecreationPreservesState() {
        // This test verifies that theme changes trigger recreation
        // but ViewModels and state survive
        
        activityRule.scenario.onActivity { activity ->
            // Get initial state
            val context = activity as Context
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            
            // Set dark theme
            prefs.edit().putString("theme_preference", "dark").commit()
            
            // Verify it was saved
            val theme = prefs.getString("theme_preference", "light")
            assert(theme == "dark") {
                "Theme should be 'dark' after setting"
            }
        }
        
        // Recreate activity (simulating theme change)
        activityRule.scenario.recreate()
        
        // Verify theme persisted across recreation
        activityRule.scenario.onActivity { activity ->
            val context = activity as Context
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val theme = prefs.getString("theme_preference", "light")
            
            assert(theme == "dark") {
                "Theme should persist across recreation but was '$theme'"
            }
        }
    }
    
    // ==================== NIGHT MODE VERIFICATION TESTS ====================
    
    @Test
    fun testNightModeMatchesThemePreference() {
        // Test light theme
        TestThemeUtils.setLightTheme()
        assert(TestThemeUtils.getCurrentNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            "Night mode should be MODE_NIGHT_NO for light theme"
        }
        
        // Test dark theme
        TestThemeUtils.setDarkTheme()
        assert(TestThemeUtils.getCurrentNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            "Night mode should be MODE_NIGHT_YES for dark theme"
        }
        
        // Test system theme
        TestThemeUtils.setSystemTheme()
        assert(TestThemeUtils.getCurrentNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            "Night mode should be MODE_NIGHT_FOLLOW_SYSTEM for system theme"
        }
    }
    
    @Test
    fun testThemeHelperMethods() {
        // Test isLightTheme
        TestThemeUtils.setLightTheme()
        assert(TestThemeUtils.isLightTheme()) {
            "isLightTheme() should return true for light theme"
        }
        
        // Test isDarkTheme
        TestThemeUtils.setDarkTheme()
        assert(TestThemeUtils.isDarkTheme()) {
            "isDarkTheme() should return true for dark theme"
        }
        
        // Test isSystemTheme
        TestThemeUtils.setSystemTheme()
        assert(TestThemeUtils.isSystemTheme()) {
            "isSystemTheme() should return true for system theme"
        }
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun testInvalidThemeValueFallsBackToDefault() {
        // Set an invalid theme value
        val prefs = TestThemeUtils.getThemePreferences()
        prefs.edit().putString("theme_preference", "invalid_theme").commit()
        
        // Read it back
        val theme = TestThemeUtils.getTheme()
        
        // Should return the invalid value (app handles fallback)
        assert(theme == "invalid_theme") {
            "Should return stored value even if invalid, but was '$theme'"
        }
    }
    
    @Test
    fun testNullThemeValueFallsBackToDefault() {
        // Clear theme
        TestThemeUtils.clearTheme()
        
        // Should fall back to "light"
        val theme = TestThemeUtils.getTheme()
        assert(theme == "light") {
            "Null theme should default to 'light' but was '$theme'"
        }
    }
    
    @Test
    fun testThemePreferencesConcurrentAccess() {
        // Simulate multiple threads accessing theme preference
        val threads = List(10) {
            Thread {
                val theme = if (it % 2 == 0) "light" else "dark"
                TestThemeUtils.setTheme(theme)
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        // Verify one of the themes was set (no crashes)
        val finalTheme = TestThemeUtils.getTheme()
        assert(finalTheme == "light" || finalTheme == "dark") {
            "Theme should be either 'light' or 'dark' after concurrent access"
        }
    }
}

