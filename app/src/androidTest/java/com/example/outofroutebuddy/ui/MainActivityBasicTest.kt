package com.example.outofroutebuddy.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🏠 Main Activity Basic Tests
 * 
 * Tests basic MainActivity functionality including:
 * - Activity launches successfully
 * - Key UI elements are visible
 * - Toolbar is displayed correctly
 * - Default theme is applied
 * 
 * Priority: HIGH - Core app functionality
 */
@RunWith(AndroidJUnit4::class)
class MainActivityBasicTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    /**
     * Test: MainActivity launches successfully
     */
    @Test
    fun testMainActivityLaunches() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        scenario.onActivity { activity ->
            assert(activity != null) { "MainActivity should launch successfully" }
            android.util.Log.d("MainActivityTest", "MainActivity launched successfully")
        }
    }

    /**
     * Test: Toolbar is displayed
     */
    @Test
    fun testToolbarDisplayed() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        onView(withId(R.id.custom_toolbar_layout))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("MainActivityTest", "Toolbar is displayed")
    }

    /**
     * Test: Settings button is visible in toolbar
     */
    @Test
    fun testSettingsButtonVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        onView(withId(R.id.settings_button))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
        
        android.util.Log.d("MainActivityTest", "Settings button is visible and clickable")
    }

    /**
     * Test: Main input fields are visible
     */
    @Test
    fun testMainInputFieldsVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Check for key input fields
        onView(withId(R.id.loaded_miles_input))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.bounce_miles_input))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("MainActivityTest", "Main input fields are visible")
    }

    /**
     * Test: Start trip button is visible
     */
    @Test
    fun testStartTripButtonVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        onView(withId(R.id.start_trip_button))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
        
        android.util.Log.d("MainActivityTest", "Start trip button is visible and clickable")
    }

    /**
     * Test: Statistics button is visible
     */
    @Test
    fun testStatisticsButtonVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        onView(withId(R.id.statistics_button))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("MainActivityTest", "Statistics button is visible")
    }

    /**
     * Test: Default theme is light mode on first launch
     */
    @Test
    fun testDefaultThemeIsLight() {
        // Clear preferences
        context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        
        // Force light mode for this test
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(500)
        
        scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            val themeValue = prefs.getString("theme_preference", "light")
            
            assert(themeValue == "light" || themeValue == null || themeValue == "") {
                "Default theme should be light"
            }
            
            android.util.Log.d("MainActivityTest", "Default theme is light: $themeValue")
        }
    }

    /**
     * Test: App doesn't crash during initialization
     */
    @Test
    fun testNoCrashOnLaunch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
        
        scenario.onActivity { activity ->
            // Just verify activity is still alive
            assert(!activity.isFinishing) { "Activity should not be finishing" }
            assert(!activity.isDestroyed) { "Activity should not be destroyed" }
            
            android.util.Log.d("MainActivityTest", "App launched without crash")
        }
    }

    /**
     * Test: Today's info section is visible
     */
    @Test
    fun testTodaysInfoVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        onView(withId(R.id.todays_info_card))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("MainActivityTest", "Today's info section is visible")
    }

    /**
     * Test: Output fields for OOR stats are visible
     */
    @Test
    fun testOorOutputFieldsVisible() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Check for OOR output fields
        onView(withId(R.id.total_miles_output))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.oor_miles_output))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.oor_percentage_output))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("MainActivityTest", "OOR output fields are visible")
    }
}

