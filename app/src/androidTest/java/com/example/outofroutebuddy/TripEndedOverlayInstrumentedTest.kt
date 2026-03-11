package com.example.outofroutebuddy

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.outofroutebuddy.services.TripEndedOverlayService
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the trip-ended overlay alert flow:
 * - Grant overlay permission (SYSTEM_ALERT_WINDOW) so the bubble can be shown.
 * - Launch MainActivity with EXTRA_OPEN_TRIP_ENDED_DIALOG (simulating user tapped the bubble).
 * - Assert "End trip?" dialog appears with "Yes, complete trip" and "No, continue trip".
 * - Optionally click "Yes, complete trip" or "No, continue trip" and assert dialog dismisses.
 *
 * **Prerequisites:** Same as other instrumented tests (TripInputFragment as start screen).
 * Clear app data or run after setup that clears recovery prefs and period onboarding.
 *
 * **Note:** A full-trip scenario (start trip → wait for detector → overlay appears → tap → dialog)
 * would require either mocking TripMetrics or a long wait for real GPS; not included here.
 */
@RunWith(AndroidJUnit4::class)
class TripEndedOverlayInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Clear recovery and period onboarding so MainActivity shows TripInputFragment
        context.getSharedPreferences("trip_crash_recovery", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putBoolean("app_running", false)
            .apply()
        context.getSharedPreferences("OutOfRouteBuddyPreferences", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("has_seen_period_onboarding", true)
            .putString("period_mode", "STANDARD")
            .apply()
        OutOfRouteApplication.clearRecoveredState()

        // Grant overlay permission so the trip-ended bubble can be drawn (required for overlay service)
        grantOverlayPermissionIfPossible(context.packageName)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    /**
     * Grant SYSTEM_ALERT_WINDOW via shell so the overlay service can show the bubble.
     * If this fails (e.g. test runner without shell), overlay tests may still run but the
     * service will log "Overlay permission not granted".
     */
    private fun grantOverlayPermissionIfPossible(packageName: String) {
        try {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                uiAutomation.executeShellCommand("appops set $packageName SYSTEM_ALERT_WINDOW allow")
                Thread.sleep(300)
            }
        } catch (e: Exception) {
            // Ignore: test can still run; overlay may not appear without permission
        }
    }

    /**
     * Simulates user tapping the trip-ended overlay bubble: launch MainActivity with
     * EXTRA_OPEN_TRIP_ENDED_DIALOG. MainActivity onResume will navigate to TripInputFragment
     * and show the "End trip?" dialog.
     */
    @Test
    fun launchWithTripEndedDialogExtra_showsEndTripOverlayDialog() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
        }
        scenario = ActivityScenario.launch(intent)

        // Wait for nav to be ready and dialog to be shown (onResume posts to show dialog)
        Thread.sleep(2500)

        val yesText = context.getString(R.string.yes_complete_trip)
        val noText = context.getString(R.string.no_continue_trip)

        onView(withText(yesText)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(noText)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    /**
     * Same as above, then tap "Yes, complete trip" and assert dialog is dismissed.
     */
    @Test
    fun launchWithTripEndedDialogExtra_tapYesCompleteTrip_dismissesDialog() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
        }
        scenario = ActivityScenario.launch(intent)

        Thread.sleep(2500)

        val yesText = context.getString(R.string.yes_complete_trip)
        onView(withText(yesText)).inRoot(isDialog()).perform(click())

        Thread.sleep(500)
        // Dialog dismissed; trip input screen should be visible (Start Trip button)
        val startTripText = context.getString(R.string.start_trip)
        onView(withText(startTripText)).check(matches(isDisplayed()))
    }

    /**
     * Same as above, then tap "No, continue trip" and assert dialog is dismissed.
     */
    @Test
    fun launchWithTripEndedDialogExtra_tapNoContinueTrip_dismissesDialog() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
        }
        scenario = ActivityScenario.launch(intent)

        Thread.sleep(2500)

        val noText = context.getString(R.string.no_continue_trip)
        onView(withText(noText)).inRoot(isDialog()).perform(click())

        Thread.sleep(500)
        val startTripText = context.getString(R.string.start_trip)
        onView(withText(startTripText)).check(matches(isDisplayed()))
    }
}
