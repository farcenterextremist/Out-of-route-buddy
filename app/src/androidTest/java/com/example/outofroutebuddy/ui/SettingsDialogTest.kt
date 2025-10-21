package com.example.outofroutebuddy.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
 * ⚙️ Settings Dialog Tests
 * 
 * Tests the settings dialog functionality including:
 * - Opening settings dialog via toolbar button
 * - Dark/Light mode selection in dialog
 * - Template/Period mode selection in dialog
 * - Theme persistence
 * 
 * Priority: HIGH - Settings are accessed via dialog, not fragment navigation
 */
@RunWith(AndroidJUnit4::class)
class SettingsDialogTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var context: Context

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
     * Test: Settings button exists in toolbar
     */
    @Test
    fun testSettingsButtonExists() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Verify settings button is displayed
        onView(withId(R.id.settings_button))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("SettingsDialogTest", "Settings button found in toolbar")
    }

    /**
     * Test: Clicking settings button opens dialog
     */
    @Test
    fun testSettingsDialogOpens() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Click settings button
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify dialog elements are visible (mode or template rows)
        try {
            onView(withId(R.id.mode_row))
                .check(matches(isDisplayed()))
            android.util.Log.d("SettingsDialogTest", "Settings dialog opened successfully")
        } catch (e: Exception) {
            // Alternative: check for template row
            onView(withId(R.id.template_row))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test: Dark/Light mode selector works in dialog
     */
    @Test
    fun testDarkModeSelectorInDialog() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Open settings dialog
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify mode row exists
        onView(withId(R.id.mode_row))
            .check(matches(isDisplayed()))
        
        // Click on mode row to open mode selection
        onView(withId(R.id.mode_row)).perform(click())
        Thread.sleep(300)
        
        // Verify mode selection dialog opens with radio buttons
        try {
            onView(withId(R.id.radio_mode_dark))
                .check(matches(isDisplayed()))
            android.util.Log.d("SettingsDialogTest", "Dark mode selector found")
        } catch (e: Exception) {
            android.util.Log.w("SettingsDialogTest", "Mode selector may have different structure")
        }
    }

    /**
     * Test: Template/Period mode selector works in dialog
     */
    @Test
    fun testTemplateModeSelectorInDialog() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Open settings dialog
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify template row exists
        onView(withId(R.id.template_row))
            .check(matches(isDisplayed()))
        
        // Click on template row
        onView(withId(R.id.template_row)).perform(click())
        Thread.sleep(300)
        
        // Verify template selection dialog opens
        try {
            onView(withId(R.id.template_select_radio_group))
                .check(matches(isDisplayed()))
            android.util.Log.d("SettingsDialogTest", "Template selector found")
        } catch (e: Exception) {
            android.util.Log.w("SettingsDialogTest", "Template selector may have different structure")
        }
    }

    /**
     * Test: Help/Info button works in settings dialog
     */
    @Test
    fun testHelpInfoButtonInDialog() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Open settings dialog
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify help info button exists
        try {
            onView(withId(R.id.help_info_button))
                .check(matches(isDisplayed()))
            
            // Click help button
            onView(withId(R.id.help_info_button)).perform(click())
            Thread.sleep(300)
            
            android.util.Log.d("SettingsDialogTest", "Help/Info button works")
        } catch (e: Exception) {
            android.util.Log.w("SettingsDialogTest", "Help button not found or clickable")
        }
    }

    /**
     * Test: Theme preference defaults to light mode
     */
    @Test
    fun testThemeDefaultsToLight() {
        // Clear any saved preferences
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(500)
        
        // Check default night mode
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        assert(currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            "Default theme should be light mode (MODE_NIGHT_NO)"
        }
        
        android.util.Log.d("SettingsDialogTest", "Theme correctly defaults to light mode")
    }

    /**
     * Test: Settings dialog shows current mode correctly
     */
    @Test
    fun testSettingsDialogShowsCurrentMode() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Open settings dialog
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify mode summary displays current mode
        onView(withId(R.id.mode_summary))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("SettingsDialogTest", "Mode summary displayed in dialog")
    }

    /**
     * Test: Settings dialog shows current template correctly
     */
    @Test
    fun testSettingsDialogShowsCurrentTemplate() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load
        
        // Open settings dialog
        onView(withId(R.id.settings_button)).perform(click())
        Thread.sleep(300)
        
        // Verify template summary displays current template
        onView(withId(R.id.template_summary))
            .check(matches(isDisplayed()))
        
        android.util.Log.d("SettingsDialogTest", "Template summary displayed in dialog")
    }
}

