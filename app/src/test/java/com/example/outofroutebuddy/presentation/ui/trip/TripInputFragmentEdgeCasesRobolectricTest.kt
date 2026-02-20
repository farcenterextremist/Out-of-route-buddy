package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.google.android.material.button.MaterialButton
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripInputFragmentEdgeCasesRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun launch(): Pair<MainActivity, TripInputFragment> {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
        return activity to fragment
    }

    @Test
    fun invalidInputs_preventStart() {
        val (_, fragment) = launch()
        val root = fragment.requireView()

        // Leave inputs empty
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

        // Should not start: inputs remain enabled and button shows Start
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isTrue()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isTrue()
        assertThat(startButton.text.toString().lowercase()).contains("start")
    }

    @Test
    fun permissionDenied_doesNotStartTrip() {
        val (activity, fragment) = launch()
        val root = fragment.requireView()

        // Fill inputs but do NOT grant location permissions
        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")

        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

        // Process any dialogs/errors
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Should not start without permissions
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isTrue()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isTrue()
        assertThat(startButton.text.toString().lowercase()).contains("start")

        // Now grant and verify can start
        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        startButton.performClick()
        assertThat(startButton.text.toString().lowercase()).contains("end")
    }

    @Test
    fun endDialog_cancel_keepsTripActive() {
        val (activity, fragment) = launch()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)

        // Grant permissions and start
        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        startButton.performClick()
        assertThat(startButton.text.toString().lowercase()).contains("end")

        // Show confirmation dialog
        startButton.performClick()
        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        dialog.cancel() // simulate back/cancel

        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Cancel keeps trip active
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isFalse()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isFalse()
        assertThat(startButton.text.toString().lowercase()).contains("end")
    }
}


