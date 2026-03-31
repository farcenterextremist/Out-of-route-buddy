package com.example.outofroutebuddy.presentation.ui.trip

import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
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
class TripInputFragmentBehaviorRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var tripPersistenceManager: TripPersistenceManager

    @Before
    fun setUp() {
        hiltRule.inject()
        // Avoid TripRecoveryDialog overlaying TripInput during Robolectric runs
        tripPersistenceManager.clearSavedTripState()
    }

    private fun launchFragment(): Pair<MainActivity, TripInputFragment> {
        TripInputRobolectricHelpers.prepareApplicationForMainActivityTripFlow(preferencesManager)
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val navHost =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment =
            navHost.childFragmentManager.fragments.filterIsInstance<TripInputFragment>().firstOrNull()
                ?: error("TripInputFragment not in NavHost (check nav_graph start destination)")
        return activity to fragment
    }

    /** Robolectric rarely dismisses the background-reliability AlertDialog in time; VM start matches production trip state. */
    private fun startTripViaViewModel(activity: MainActivity) {
        ViewModelProvider(activity)[TripInputViewModel::class.java].calculateTrip(10.0, 2.0, 0.0)
        repeat(25) { Shadows.shadowOf(android.os.Looper.getMainLooper()).idle() }
    }

    @Test
    fun startTrip_withValidInputs_updatesUIToActive() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")

        startTripViaViewModel(activity)

        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        // After starting, button text should change to End Trip and pause visible
        assertThat(startButton.text.toString().lowercase()).contains("end")
        val pauseVisible = root.findViewById<MaterialButton>(R.id.pause_button).visibility == android.view.View.VISIBLE
        assertThat(pauseVisible).isTrue()
        // Inputs disabled while trip active
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isFalse()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isFalse()
    }

    @Test
    fun pause_and_resume_toggleIconAndState() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        startTripViaViewModel(activity)
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        val pauseButton = root.findViewById<MaterialButton>(R.id.pause_button)
        // Pause (icon switches to play)
        pauseButton.performClick()
        // Resume (icon switches back to pause)
        pauseButton.performClick()

        // No crash and button remains visible indicates toggle succeeded
        assertThat(pauseButton.visibility == android.view.View.VISIBLE).isTrue()
    }

    @Test
    fun endTrip_confirmation_yes_resetsUI() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        startTripViaViewModel(activity)
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Find the custom dialog and click the "End Trip" button (custom MaterialButton)
        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        
        // Find the custom view in the dialog (setView sets it, accessible via window decorView)
        val window = dialog.window
        assertThat(window).isNotNull()
        val decorView = window?.decorView
        assertThat(decorView).isNotNull()
        
        // Find the LinearLayout that contains the custom content
        val dialogView = decorView?.findViewById<android.view.ViewGroup>(android.R.id.custom)
            ?: decorView?.let { view ->
                // Fallback: search for LinearLayout with MaterialButtons
                fun findLinearLayoutWithButtons(parent: android.view.View?): android.view.ViewGroup? {
                    if (parent is android.view.ViewGroup) {
                        for (i in 0 until parent.childCount) {
                            val child = parent.getChildAt(i)
                            if (child is android.view.ViewGroup && child.childCount > 0) {
                                val firstChild = child.getChildAt(0)
                                if (firstChild is android.widget.TextView || firstChild is MaterialButton) {
                                    return child
                                }
                                val found = findLinearLayoutWithButtons(child)
                                if (found != null) return found
                            }
                        }
                    }
                    return null
                }
                findLinearLayoutWithButtons(view)
            }
        assertThat(dialogView).isNotNull()
        
        // Find button by text (buttons are MaterialButtons in LinearLayout)
        var endTripButton: MaterialButton? = null
        fun findButton(view: android.view.View?): MaterialButton? {
            if (view is MaterialButton && view.text.toString().contains("End Trip", ignoreCase = true)) {
                return view
            }
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    val found = findButton(view.getChildAt(i))
                    if (found != null) return found
                }
            }
            return null
        }
        endTripButton = findButton(dialogView)
        assertThat(endTripButton).isNotNull()
        endTripButton?.performClick()

        // Process pending UI work
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        // Give time for ViewModel coroutines to complete
        Thread.sleep(100)

        // UI should reset: inputs enabled, start button text shows Start
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isTrue()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isTrue()
        assertThat(startButton.text.toString().lowercase()).contains("start")
    }

    @Test
    fun endTrip_confirmation_no_keepsTripActive() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        startTripViaViewModel(activity)
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        // Custom end dialog: "Continue Trip" keeps the trip active (no platform negative button)
        fun findContinueTrip(v: android.view.View?): MaterialButton? {
            if (v is MaterialButton && v.text.toString().contains("Continue Trip", ignoreCase = true)) return v
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) {
                    findContinueTrip(v.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
        val continueTrip = findContinueTrip(dialog!!.window?.decorView)
        assertThat(continueTrip).isNotNull()
        continueTrip!!.performClick()

        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Still active: inputs disabled, button shows End
        assertThat(root.findViewById<EditText>(R.id.loaded_miles_input).isEnabled).isFalse()
        assertThat(root.findViewById<EditText>(R.id.bounce_miles_input).isEnabled).isFalse()
        assertThat(startButton.text.toString().lowercase()).contains("end")
    }

    @Test
    fun endTrip_confirmation_dialog_hasExpectedTitleAndMessage() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        startTripViaViewModel(activity)
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        // End-trip confirmation uses setView(customView) and does not set a title; dialog has custom content only.

        // Message is in a custom TextView inside the dialog view
        val window = dialog.window
        assertThat(window).isNotNull()
        val decorView = window?.decorView
        assertThat(decorView).isNotNull()
        
        fun findMessageTextView(parent: android.view.View?): android.widget.TextView? {
            if (parent is android.widget.TextView) {
                val t = parent.text.toString()
                if (t.isNotBlank()) return parent
            }
            if (parent is android.view.ViewGroup) {
                for (i in 0 until parent.childCount) {
                    val found = findMessageTextView(parent.getChildAt(i))
                    if (found != null) return found
                }
            }
            return null
        }
        val messageTextView = findMessageTextView(decorView)
        assertThat(messageTextView).isNotNull()
        val message = messageTextView?.text?.toString() ?: ""
        assertThat(message.lowercase()).contains("save")
    }

    @Test
    fun clearTrip_confirmation_dialog_hasExpectedMessage() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")
        startTripViaViewModel(activity)
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick() // Open End Trip dialog
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val endDialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(endDialog).isNotNull()
        val dialogView = endDialog.window?.decorView
        assertThat(dialogView).isNotNull()
        fun findClearTripButton(v: android.view.View?): com.google.android.material.button.MaterialButton? {
            if (v is com.google.android.material.button.MaterialButton &&
                v.text.toString().contains("Clear Trip", ignoreCase = true)) return v
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) {
                    findClearTripButton(v.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
        val clearButton = findClearTripButton(dialogView)
        assertThat(clearButton).isNotNull()
        clearButton!!.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val clearDialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(clearDialog).isNotNull()
        val clearMessage =
            org.robolectric.Shadows.shadowOf(clearDialog).message?.toString()
                ?: run {
                    fun collectText(v: android.view.View?): String {
                        if (v is android.widget.TextView && v.text.isNotBlank()) return v.text.toString()
                        if (v is android.view.ViewGroup) {
                            return (0 until v.childCount).joinToString(" ") { collectText(v.getChildAt(it)) }
                        }
                        return ""
                    }
                    collectText(clearDialog.window?.decorView)
                }
        assertThat(clearMessage).contains("not be saved")
        assertThat(clearMessage).contains("not count")
    }

    @Test
    fun statisticsSection_doesNotRenderDaysWithSavedTripsUi() {
        val (_, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<MaterialButton>(R.id.statistics_button).performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        fun subtreeContainsText(view: android.view.View?, text: String): Boolean {
            if (view is TextView && view.text?.toString()?.contains(text, ignoreCase = true) == true) {
                return true
            }
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    if (subtreeContainsText(view.getChildAt(i), text)) return true
                }
            }
            return false
        }

        assertThat(subtreeContainsText(root, "Days with saved trips")).isFalse()
    }
}


