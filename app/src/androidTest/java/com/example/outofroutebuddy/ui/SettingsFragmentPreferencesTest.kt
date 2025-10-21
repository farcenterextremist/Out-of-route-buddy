package com.example.outofroutebuddy.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.utils.TestPreferenceUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧪 Settings UI Tests
 * 
 * Tests for Settings Fragment preferences:
 * - GPS update frequency preference
 * - Distance units preference (miles/kilometers)
 * - Notification preferences
 * - Auto-start trip preference
 * - Preference persistence
 * 
 * Priority: 🔴 HIGH
 * Impact: Core user settings with no test coverage
 * 
 * Created: Phase 1 - Critical Tests
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsFragmentPreferencesTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        // Clear all preferences and set defaults
        TestPreferenceUtils.setupTestDefaults()
    }
    
    @After
    fun cleanup() {
        TestPreferenceUtils.cleanup()
    }
    
    // ==================== GPS FREQUENCY TESTS ====================
    
    @Test
    fun testGpsFrequencyDefaultValue() {
        val frequency = TestPreferenceUtils.getGpsUpdateFrequency()
        assert(frequency == 10) {
            "Default GPS frequency should be 10 seconds but was $frequency"
        }
    }
    
    @Test
    fun testGpsFrequencyChange() {
        // Change frequency to 5 seconds
        TestPreferenceUtils.setGpsUpdateFrequency(5)
        
        // Verify it was saved
        TestPreferenceUtils.assertGpsFrequencyEquals(5)
    }
    
    @Test
    fun testGpsFrequencyPersistence() {
        // Set frequency
        TestPreferenceUtils.setGpsUpdateFrequency(15)
        
        // Read it back multiple times
        repeat(3) {
            val frequency = TestPreferenceUtils.getGpsUpdateFrequency()
            assert(frequency == 15) {
                "GPS frequency should persist as 15 but was $frequency"
            }
        }
    }
    
    @Test
    fun testGpsFrequencyEdgeValues() {
        // Test minimum value (1 second)
        TestPreferenceUtils.setGpsUpdateFrequency(1)
        TestPreferenceUtils.assertGpsFrequencyEquals(1)
        
        // Test maximum value (60 seconds)
        TestPreferenceUtils.setGpsUpdateFrequency(60)
        TestPreferenceUtils.assertGpsFrequencyEquals(60)
        
        // Test common values
        listOf(5, 10, 15, 30).forEach { freq ->
            TestPreferenceUtils.setGpsUpdateFrequency(freq)
            TestPreferenceUtils.assertGpsFrequencyEquals(freq)
        }
    }
    
    // ==================== DISTANCE UNITS TESTS ====================
    
    @Test
    fun testDistanceUnitsDefaultValue() {
        val units = TestPreferenceUtils.getDistanceUnits()
        assert(units == "miles") {
            "Default distance units should be 'miles' but was '$units'"
        }
    }
    
    @Test
    fun testDistanceUnitsChangeToKilometers() {
        // Change to kilometers
        TestPreferenceUtils.setKilometers()
        
        // Verify
        TestPreferenceUtils.assertDistanceUnitsEquals("kilometers")
        assert(TestPreferenceUtils.isKilometers()) {
            "isKilometers() should return true"
        }
    }
    
    @Test
    fun testDistanceUnitsChangeToMiles() {
        // Start with kilometers
        TestPreferenceUtils.setKilometers()
        
        // Change to miles
        TestPreferenceUtils.setMiles()
        
        // Verify
        TestPreferenceUtils.assertDistanceUnitsEquals("miles")
        assert(TestPreferenceUtils.isMiles()) {
            "isMiles() should return true"
        }
    }
    
    @Test
    fun testDistanceUnitsPersistence() {
        // Set to kilometers
        TestPreferenceUtils.setKilometers()
        
        // Verify persistence
        repeat(3) {
            val units = TestPreferenceUtils.getDistanceUnits()
            assert(units == "kilometers") {
                "Units should persist as 'kilometers' but was '$units'"
            }
        }
    }
    
    @Test
    fun testDistanceUnitsToggling() {
        // Start with default (miles)
        assert(TestPreferenceUtils.isMiles())
        
        // Toggle to kilometers
        TestPreferenceUtils.setKilometers()
        assert(TestPreferenceUtils.isKilometers())
        
        // Toggle back to miles
        TestPreferenceUtils.setMiles()
        assert(TestPreferenceUtils.isMiles())
        
        // Multiple toggles
        repeat(5) {
            TestPreferenceUtils.setKilometers()
            assert(TestPreferenceUtils.isKilometers())
            
            TestPreferenceUtils.setMiles()
            assert(TestPreferenceUtils.isMiles())
        }
    }
    
    // ==================== NOTIFICATION TESTS ====================
    
    @Test
    fun testNotificationsEnabledByDefault() {
        val enabled = TestPreferenceUtils.areNotificationsEnabled()
        assert(enabled) {
            "Notifications should be enabled by default"
        }
    }
    
    @Test
    fun testNotificationsToggle() {
        // Disable notifications
        TestPreferenceUtils.setNotificationsEnabled(false)
        TestPreferenceUtils.assertNotificationsEnabled(false)
        
        // Re-enable notifications
        TestPreferenceUtils.setNotificationsEnabled(true)
        TestPreferenceUtils.assertNotificationsEnabled(true)
    }
    
    @Test
    fun testNotificationSoundDefaultValue() {
        val enabled = TestPreferenceUtils.isNotificationSoundEnabled()
        assert(!enabled) {
            "Notification sound should be disabled by default but was enabled"
        }
    }
    
    @Test
    fun testNotificationSoundToggle() {
        // Enable sound
        TestPreferenceUtils.setNotificationSound(true)
        assert(TestPreferenceUtils.isNotificationSoundEnabled())
        
        // Disable sound
        TestPreferenceUtils.setNotificationSound(false)
        assert(!TestPreferenceUtils.isNotificationSoundEnabled())
    }
    
    // ==================== AUTO-START TRIP TESTS ====================
    
    @Test
    fun testAutoStartTripDisabledByDefault() {
        val enabled = TestPreferenceUtils.isAutoStartTripEnabled()
        assert(!enabled) {
            "Auto-start trip should be disabled by default but was enabled"
        }
    }
    
    @Test
    fun testAutoStartTripToggle() {
        // Enable auto-start
        TestPreferenceUtils.setAutoStartTrip(true)
        TestPreferenceUtils.assertAutoStartEnabled(true)
        
        // Disable auto-start
        TestPreferenceUtils.setAutoStartTrip(false)
        TestPreferenceUtils.assertAutoStartEnabled(false)
    }
    
    @Test
    fun testAutoStartTripPersistence() {
        // Enable and verify persistence
        TestPreferenceUtils.setAutoStartTrip(true)
        
        repeat(3) {
            val enabled = TestPreferenceUtils.isAutoStartTripEnabled()
            assert(enabled) {
                "Auto-start should persist as enabled"
            }
        }
    }
    
    // ==================== HIGH ACCURACY MODE TESTS ====================
    
    @Test
    fun testHighAccuracyModeEnabledByDefault() {
        val enabled = TestPreferenceUtils.isHighAccuracyMode()
        assert(enabled) {
            "High accuracy mode should be enabled by default"
        }
    }
    
    @Test
    fun testHighAccuracyModeToggle() {
        // Disable high accuracy
        TestPreferenceUtils.setHighAccuracyMode(false)
        assert(!TestPreferenceUtils.isHighAccuracyMode())
        
        // Re-enable
        TestPreferenceUtils.setHighAccuracyMode(true)
        assert(TestPreferenceUtils.isHighAccuracyMode())
    }
    
    // ==================== PERIOD MODE TESTS ====================
    
    @Test
    fun testPeriodModeDefaultValue() {
        val mode = TestPreferenceUtils.getPeriodMode()
        assert(mode == "STANDARD") {
            "Default period mode should be 'STANDARD' but was '$mode'"
        }
    }
    
    @Test
    fun testPeriodModeChange() {
        // Change to CUSTOM
        TestPreferenceUtils.setPeriodMode("CUSTOM")
        
        // Verify
        val mode = TestPreferenceUtils.getPeriodMode()
        assert(mode == "CUSTOM") {
            "Period mode should be 'CUSTOM' but was '$mode'"
        }
    }
    
    @Test
    fun testPeriodModePersistence() {
        // Set to CUSTOM
        TestPreferenceUtils.setPeriodMode("CUSTOM")
        
        // Verify persistence
        repeat(3) {
            val mode = TestPreferenceUtils.getPeriodMode()
            assert(mode == "CUSTOM") {
                "Period mode should persist as 'CUSTOM' but was '$mode'"
            }
        }
    }
    
    // ==================== LAST ENTERED VALUES TESTS ====================
    
    @Test
    fun testLastLoadedMilesPersistence() {
        // Set last loaded miles
        TestPreferenceUtils.setLastLoadedMiles("500.5")
        
        // Verify
        val miles = TestPreferenceUtils.getLastLoadedMiles()
        assert(miles == "500.5") {
            "Last loaded miles should be '500.5' but was '$miles'"
        }
    }
    
    @Test
    fun testLastBounceMilesPersistence() {
        // Set last bounce miles
        TestPreferenceUtils.setLastBounceMiles("75.25")
        
        // Verify
        val miles = TestPreferenceUtils.getLastBounceMiles()
        assert(miles == "75.25") {
            "Last bounce miles should be '75.25' but was '$miles'"
        }
    }
    
    @Test
    fun testLastEnteredValuesIndependent() {
        // Set both values
        TestPreferenceUtils.setLastLoadedMiles("100")
        TestPreferenceUtils.setLastBounceMiles("20")
        
        // Verify both saved correctly
        assert(TestPreferenceUtils.getLastLoadedMiles() == "100")
        assert(TestPreferenceUtils.getLastBounceMiles() == "20")
        
        // Change one, verify other unchanged
        TestPreferenceUtils.setLastLoadedMiles("200")
        assert(TestPreferenceUtils.getLastLoadedMiles() == "200")
        assert(TestPreferenceUtils.getLastBounceMiles() == "20") // Unchanged
    }
    
    // ==================== PREFERENCE ISOLATION TESTS ====================
    
    @Test
    fun testPreferencesDoNotInterfere() {
        // Set all preferences
        TestPreferenceUtils.setGpsUpdateFrequency(15)
        TestPreferenceUtils.setDistanceUnits("kilometers")
        TestPreferenceUtils.setTheme("dark")
        TestPreferenceUtils.setNotificationsEnabled(false)
        TestPreferenceUtils.setAutoStartTrip(true)
        
        // Verify all saved correctly
        TestPreferenceUtils.assertGpsFrequencyEquals(15)
        TestPreferenceUtils.assertDistanceUnitsEquals("kilometers")
        TestPreferenceUtils.assertThemeEquals("dark")
        TestPreferenceUtils.assertNotificationsEnabled(false)
        TestPreferenceUtils.assertAutoStartEnabled(true)
    }
    
    @Test
    fun testClearAllPreferences() {
        // Set some preferences
        TestPreferenceUtils.setGpsUpdateFrequency(20)
        TestPreferenceUtils.setDistanceUnits("kilometers")
        TestPreferenceUtils.setAutoStartTrip(true)
        
        // Clear all
        TestPreferenceUtils.clearAll()
        
        // Verify defaults restored
        assert(TestPreferenceUtils.getGpsUpdateFrequency() == 10) // Default
        assert(TestPreferenceUtils.getDistanceUnits() == "miles") // Default
        assert(!TestPreferenceUtils.isAutoStartTripEnabled()) // Default false
    }
    
    @Test
    fun testPreferencesPersistAcrossActivityRecreation() {
        // Set preferences
        TestPreferenceUtils.setGpsUpdateFrequency(25)
        TestPreferenceUtils.setDistanceUnits("kilometers")
        
        // Simulate activity recreation
        activityRule.scenario.recreate()
        
        // Verify persistence
        activityRule.scenario.onActivity {
            TestPreferenceUtils.assertGpsFrequencyEquals(25)
            TestPreferenceUtils.assertDistanceUnitsEquals("kilometers")
        }
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun testEmptyStringPreferences() {
        // Test empty strings
        TestPreferenceUtils.setLastLoadedMiles("")
        TestPreferenceUtils.setLastBounceMiles("")
        
        assert(TestPreferenceUtils.getLastLoadedMiles() == "")
        assert(TestPreferenceUtils.getLastBounceMiles() == "")
    }
    
    @Test
    fun testLargeNumericValues() {
        // Test very large values
        TestPreferenceUtils.setLastLoadedMiles("999999.99")
        TestPreferenceUtils.setGpsUpdateFrequency(3600) // 1 hour
        
        assert(TestPreferenceUtils.getLastLoadedMiles() == "999999.99")
        assert(TestPreferenceUtils.getGpsUpdateFrequency() == 3600)
    }
    
    @Test
    fun testRapidPreferenceChanges() {
        // Rapidly toggle preferences
        repeat(10) {
            TestPreferenceUtils.setNotificationsEnabled(it % 2 == 0)
            TestPreferenceUtils.setAutoStartTrip(it % 2 == 1)
        }
        
        // Final values should be correct
        assert(!TestPreferenceUtils.areNotificationsEnabled()) // Even iterations (10 is even, last was false)
        assert(TestPreferenceUtils.isAutoStartTripEnabled()) // Odd iterations
    }
}

