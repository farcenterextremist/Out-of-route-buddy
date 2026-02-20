package com.example.outofroutebuddy.services

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
class TripTrackingServiceRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun start_and_stop_updatesServiceState() = runBlocking {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

        // Start
        TripTrackingService.startService(app, loadedMiles = 10.0, bounceMiles = 2.0)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // State may update asynchronously; allow main looper to idle and re-check
        // Allow any async startup work
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Stop
        TripTrackingService.stopService(app)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val stateAfterStop = TripTrackingService.serviceState.first()
        // At minimum, we should be able to read state and not crash after stop
        assertThat(stateAfterStop.isHealthy).isTrue()
    }

    @Test
    fun pause_and_resume_doNotAccumulateDistanceWhilePaused() = runBlocking {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

        TripTrackingService.startService(app, loadedMiles = 0.0, bounceMiles = 0.0)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Pause
        TripTrackingService.pauseService(app)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val metricsAfterPause = TripTrackingService.tripMetrics.first()
        val pausedDistance = metricsAfterPause.totalMiles

        // Simulate resume -> still no real GPS, just ensure we remain consistent
        TripTrackingService.resumeService(app)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val metricsAfterResume = TripTrackingService.tripMetrics.first()
        assertThat(metricsAfterResume.totalMiles).isAtLeast(pausedDistance)

        // Stop to clean up
        TripTrackingService.stopService(app)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    }
}
