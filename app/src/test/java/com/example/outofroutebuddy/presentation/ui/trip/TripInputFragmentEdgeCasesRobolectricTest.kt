package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.ui.TripInputRobolectricHelpers
import com.google.android.material.button.MaterialButton
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import javax.inject.Inject
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

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var tripPersistenceManager: TripPersistenceManager

    @Before
    fun setUp() {
        hiltRule.inject()
        tripPersistenceManager.clearSavedTripState()
    }

    private fun launchWithoutPrefsPrep(): Pair<MainActivity, TripInputFragment> {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val navHost =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment =
            navHost.childFragmentManager.fragments.filterIsInstance<TripInputFragment>().firstOrNull()
                ?: error("TripInputFragment not in NavHost")
        return activity to fragment
    }

    private fun launchWithPermissionsReady(): Pair<MainActivity, TripInputFragment> {
        TripInputRobolectricHelpers.prepareApplicationForMainActivityTripFlow(preferencesManager)
        return launchWithoutPrefsPrep()
    }

    private fun startTripViaViewModel(activity: MainActivity) {
        ViewModelProvider(activity)[TripInputViewModel::class.java].calculateTrip(10.0, 2.0, 0.0)
        repeat(25) { Shadows.shadowOf(android.os.Looper.getMainLooper()).idle() }
    }

    @Test
    fun invalidInputs_preventStart() {
        val (_, fragment) = launchWithoutPrefsPrep()
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
        val (activity, fragment) = launchWithoutPrefsPrep()
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

        val shadow = Shadows.shadowOf(activity)
        shadow.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
        )
        if (Build.VERSION.SDK_INT >= 33) {
            shadow.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
        preferencesManager.setHasSeenPeriodOnboarding(true)
        startTripViaViewModel(activity)
        assertThat(startButton.text.toString().lowercase()).contains("end")
    }

    @Test
    fun endDialog_cancel_keepsTripActive() {
        val (activity, fragment) = launchWithPermissionsReady()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)

        startTripViaViewModel(activity)
        assertThat(startButton.text.toString().lowercase()).contains("end")

        // Show confirmation dialog
        startButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        // End dialog is custom: use "Continue Trip" (same as dismiss without ending)
        fun findContinueTrip(v: android.view.View?): com.google.android.material.button.MaterialButton? {
            if (v is com.google.android.material.button.MaterialButton &&
                v.text.toString().contains("Continue Trip", ignoreCase = true)
            ) {
                return v
            }
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) {
                    findContinueTrip(v.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
        findContinueTrip(dialog!!.window?.decorView)!!.performClick()

        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Cancel keeps trip active
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isFalse()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isFalse()
        assertThat(startButton.text.toString().lowercase()).contains("end")
    }
}


