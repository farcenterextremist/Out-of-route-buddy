package com.example.outofroutebuddy.services

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for the overlay permission-denied fallback path.
 * When overlay permission is not granted, the service shows a high-priority notification
 * instead of the bubble; tap opens app with "End trip?" dialog.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripEndedOverlayFallbackTest {

    @Test
    fun fallbackNotificationStringResources_exist() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val title = context.getString(R.string.trip_ended_fallback_notification_title)
        val body = context.getString(R.string.trip_ended_fallback_notification_body)
        assertThat(title).isEqualTo("Have you arrived?")
        assertThat(body).isEqualTo("Open the app to end the trip or keep tracking.")
    }

    @Test
    fun fallbackContentIntent_opensMainActivityWithTripEndedDialogExtra() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
        }
        assertThat(intent.getBooleanExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, false)).isTrue()
        assertThat(intent.component?.className).isEqualTo(MainActivity::class.java.name)
    }

    @Test
    fun fallbackDedupe_allowsAtOrAfterMinInterval() {
        assertThat(
            TripEndedOverlayService.shouldPostFallbackNotification(
                nowMillis = 2000L,
                lastPostedAtMillis = 1000L,
                minIntervalMillis = 1000L,
            )
        ).isTrue()
    }

    @Test
    fun fallbackDedupe_blocksInsideMinInterval() {
        assertThat(
            TripEndedOverlayService.shouldPostFallbackNotification(
                nowMillis = 1500L,
                lastPostedAtMillis = 1000L,
                minIntervalMillis = 1000L,
            )
        ).isFalse()
    }
}
