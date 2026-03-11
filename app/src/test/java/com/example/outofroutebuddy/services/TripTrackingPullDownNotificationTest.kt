package com.example.outofroutebuddy.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.core.config.BuildConfig as CoreBuildConfig
import com.example.outofroutebuddy.R
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Tests for the pull-down notification (notification shade) feature.
 * When a trip is active: notification shows "Trip in progress".
 * When trip-ended is detected: notification shows "Trip ending".
 *
 * These tests verify string resources and that the service handles the
 * trip-ending action without crashing; full notification content is
 * covered by the string resource contract and manual/UI testing.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
class TripTrackingPullDownNotificationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    @Test
    fun notificationStringResources_existAndMatchExpectedPullDownText() {
        val started = context.getString(R.string.trip_notification_started)
        val inProgress = context.getString(R.string.trip_notification_in_progress)
        val ending = context.getString(R.string.trip_notification_ending)
        val ended = context.getString(R.string.trip_notification_ended)
        assertThat(started).isEqualTo("Trip started")
        assertThat(inProgress).isEqualTo("Trip in progress")
        assertThat(ending).isEqualTo("Trip ending")
        assertThat(ended).isEqualTo("Trip ended")
    }

    @Test
    fun notificationTitleStringResource_usedForShadeTitle() {
        val title = context.getString(R.string.notification_trip_title)
        assertThat(title).isEqualTo("Out of Route")
    }

    @Test
    fun notificationState_transitionsFromInProgress_toEnding_toInProgressOnContinue() {
        val inProgress = context.getString(R.string.trip_notification_in_progress)
        val ending = context.getString(R.string.trip_notification_ending)

        assertThat(
            TripTrackingService.resolveNotificationText(
                promptState = TripTrackingService.PROMPT_STATE_IN_PROGRESS,
                inProgressText = inProgress,
                endingText = ending,
            )
        ).isEqualTo("Trip in progress")

        assertThat(
            TripTrackingService.resolveNotificationText(
                promptState = TripTrackingService.PROMPT_STATE_ENDING_DETECTED,
                inProgressText = inProgress,
                endingText = ending,
            )
        ).isEqualTo("Trip ending")

        assertThat(
            TripTrackingService.resolveNotificationText(
                promptState = TripTrackingService.PROMPT_STATE_USER_CONTINUED_COOLDOWN,
                inProgressText = inProgress,
                endingText = ending,
            )
        ).isEqualTo("Trip in progress")
    }

    @Test
    fun actionTripEndingDetected_handledWithoutCrash_andServiceRemainsRunning() = runBlocking {
        TripTrackingService.startService(context, loadedMiles = 10.0, bounceMiles = 2.0)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val intentEnding = Intent(context, TripTrackingService::class.java).apply {
            action = ACTION_TRIP_ENDING_DETECTED
        }
        context.startService(intentEnding)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val state = TripTrackingService.serviceState.first()
        assertThat(state.isHealthy).isTrue()

        TripTrackingService.stopService(context)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    @Test
    fun startTrip_thenResume_afterPause_doesNotCrash() = runBlocking {
        TripTrackingService.startService(context, loadedMiles = 0.0, bounceMiles = 0.0)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        TripTrackingService.pauseService(context)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        TripTrackingService.resumeService(context)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val state = TripTrackingService.serviceState.first()
        assertThat(state.isHealthy).isTrue()

        TripTrackingService.stopService(context)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    @Test
    fun start_then_end_trip_keepsServiceHealthy_forEndedNotificationPath() = runBlocking {
        TripTrackingService.startService(context, loadedMiles = 3.0, bounceMiles = 1.0)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        TripTrackingService.stopService(context)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val state = TripTrackingService.serviceState.first()
        assertThat(state.isHealthy).isTrue()
    }

    private fun getActiveTripNotificationText(): String? {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Shadows.shadowOf(manager).getNotification(CoreBuildConfig.NOTIFICATION_ID)
        val shadow = notification?.let { Shadows.shadowOf(it) }
        return shadow?.contentText?.toString()
            ?: notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: notification?.tickerText?.toString()
    }

}
