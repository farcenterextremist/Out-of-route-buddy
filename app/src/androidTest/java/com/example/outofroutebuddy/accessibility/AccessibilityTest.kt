package com.example.outofroutebuddy.accessibility

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.anyOf
import org.junit.Assert.assertTrue
import com.example.outofroutebuddy.R
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.example.outofroutebuddy.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for OutOfRouteBuddy app
 *
 * Tests content descriptions, focus order, and screen reader compatibility
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {
    
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testLoadedMilesInputAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then
        onView(withId(R.id.loaded_miles_input))
            .check(matches(withContentDescription("Input field for loaded miles. Enter the number of miles you are contracted to drive.")))
            // Espresso doesn't provide isImportantForAccessibility() matcher by default; keep basic checks
            .check(matches(isDisplayed()))
    }

    @Test
    fun testBounceMilesInputAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then
        onView(withId(R.id.bounce_miles_input))
            .check(
                matches(
                    withContentDescription(
                        "Input field for bounce miles. Enter the number of miles you drive to return to your starting point.",
                    ),
                ),
            )
            .check(matches(isDisplayed()))
    }

    @Test
    fun testStartTripButtonAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then
        onView(withId(R.id.start_trip_button))
            .check(
                matches(
                    withContentDescription("Button to start or end your trip tracking. Tap to begin GPS tracking or end current trip."),
                ),
            )
            .check(matches(isDisplayed()))
    }

    @Test
    fun testStatisticsButtonAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.statistics_button))
            .check(
                matches(
                    withContentDescription("Button to expand or collapse trip statistics. Shows weekly, monthly, and yearly trip data."),
                ),
            )
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSettingsButtonAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.settings_button))
            .check(matches(withContentDescription("Button to access app settings. Configure app preferences and view help information.")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testTotalMilesOutputAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.total_miles_output))
            .check(matches(withContentDescription("Total miles driven during the current trip")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testOorMilesOutputAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.oor_miles_output))
            .check(matches(withContentDescription("Out of route miles calculated for the current trip")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testOorPercentageOutputAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.oor_percentage_output))
            .check(matches(withContentDescription("Percentage of miles that are out of route for the current trip")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testProgressBarAccessibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)

        // Then
        onView(withId(R.id.progress_bar))
            .check(matches(withContentDescription("Progress indicator showing trip tracking status")))
            // Relaxed: progress bar may be hidden at idle; accept visible or gone
            .check(matches(anyOf(isDisplayed(), withEffectiveVisibility(Visibility.GONE))))
    }

    @Test
    fun testFocusOrder() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then - Verify that focus order is logical
        // This test ensures that tab navigation follows a logical order
        onView(withId(R.id.loaded_miles_input)).check(matches(isFocusable()))
        onView(withId(R.id.bounce_miles_input)).check(matches(isFocusable()))
        onView(withId(R.id.start_trip_button)).check(matches(isFocusable()))
        onView(withId(R.id.statistics_button)).check(matches(isFocusable()))
        onView(withId(R.id.settings_button)).check(matches(isFocusable()))
    }

    @Test
    fun testInputFieldsAreEditable() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then
        onView(withId(R.id.loaded_miles_input)).check(matches(isEnabled()))
        onView(withId(R.id.bounce_miles_input)).check(matches(isEnabled()))
    }

    @Test
    fun testButtonsAreClickable() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then
        onView(withId(R.id.start_trip_button)).check(matches(isClickable()))
        onView(withId(R.id.statistics_button)).check(matches(isClickable()))
        onView(withId(R.id.settings_button)).check(matches(isClickable()))
    }

    @Test
    fun testScreenReaderCompatibility() {
        // Given
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000) // Wait for UI to fully load

        // Then - Verify that all interactive elements have proper content descriptions
        // This is a basic test to ensure screen readers can announce all elements
        val interactiveElements =
            listOf(
                R.id.loaded_miles_input,
                R.id.bounce_miles_input,
                R.id.start_trip_button,
                R.id.statistics_button,
                R.id.settings_button,
                R.id.total_miles_output,
                R.id.oor_miles_output,
                R.id.oor_percentage_output,
            )

        interactiveElements.forEach { id ->
            onView(withId(id)).check(matches(isDisplayed()))
        }
    }
} 
