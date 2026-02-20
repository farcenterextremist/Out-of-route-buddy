package com.example.outofroutebuddy.presentation.ui.dialogs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.domain.models.Trip
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Date

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripRecoveryDialogRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun buildSavedState(): TripPersistenceManager.SavedTripState {
        val trip = Trip(id = "t1", startTime = Date(), status = com.example.outofroutebuddy.domain.models.TripStatus.ACTIVE)
        return TripPersistenceManager.SavedTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 1.0,
            lastLocation = null,
            gpsMetadata = null,
            startTime = trip.startTime!!,
            recoveryTime = Date()
        )
    }

    @Test
    fun dialog_shows_and_buttons_exist() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val dialog = TripRecoveryDialog.newInstance(buildSavedState())
        dialog.show(activity.supportFragmentManager, "TripRecoveryDialog")
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Since TripRecoveryDialog uses a custom Dialog, fall back to view lookups on window
        val window = dialog.dialog?.window
        val content = window?.decorView
        assertThat(content?.findViewById<android.view.View>(R.id.button_continue_trip)).isNotNull()
        assertThat(content?.findViewById<android.view.View>(R.id.button_start_new)).isNotNull()
    }

    @Test
    fun clicking_continue_calls_listener_and_dismisses() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        var continued = false
        val host = TestHostFragment(
            onContinue = { continued = true },
            onStartNew = { }
        )
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, host, "host")
            .commitNow()

        val dialog = TripRecoveryDialog.newInstance(buildSavedState())
        dialog.show(host.childFragmentManager, "TripRecoveryDialog")
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val window = dialog.dialog?.window
        val content = window?.decorView
        val btn = content?.findViewById<android.widget.Button>(R.id.button_continue_trip)
        btn!!.performClick()

        assertThat(continued).isTrue()
        assertThat(dialog.dialog?.isShowing ?: false).isFalse()
    }

    @Test
    fun clicking_start_new_calls_listener_and_dismisses() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        var startedNew = false
        val host = TestHostFragment(
            onContinue = { },
            onStartNew = { startedNew = true }
        )
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, host, "host")
            .commitNow()

        val dialog = TripRecoveryDialog.newInstance(buildSavedState())
        dialog.show(host.childFragmentManager, "TripRecoveryDialog")
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val window = dialog.dialog?.window
        val content = window?.decorView
        val btn = content?.findViewById<android.widget.Button>(R.id.button_start_new)
        btn!!.performClick()

        assertThat(startedNew).isTrue()
        assertThat(dialog.dialog?.isShowing ?: false).isFalse()
    }
}

class TestHostFragment(
    private val onContinue: () -> Unit,
    private val onStartNew: () -> Unit
) : androidx.fragment.app.Fragment(), TripRecoveryDialog.TripRecoveryListener {
    override fun onContinueTrip(savedState: TripPersistenceManager.SavedTripState) { onContinue() }
    override fun onStartNewTrip() { onStartNew() }
}


