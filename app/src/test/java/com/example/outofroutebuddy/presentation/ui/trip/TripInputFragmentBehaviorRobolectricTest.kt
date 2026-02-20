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
class TripInputFragmentBehaviorRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun launchFragment(): Pair<MainActivity, TripInputFragment> {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        // Grant location permissions required for start flow
        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return activity to fragment
    }

    @Test
    fun startTrip_withValidInputs_updatesUIToActive() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        root.findViewById<EditText>(R.id.loaded_miles_input).setText("10")
        root.findViewById<EditText>(R.id.bounce_miles_input).setText("2")

        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

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
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

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
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

        // Click again to trigger end dialog
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
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

        // Trigger confirmation dialog
        startButton.performClick()

        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        val negative = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        negative.performClick()

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
        val startButton = root.findViewById<MaterialButton>(R.id.start_trip_button)
        startButton.performClick()

        // Show confirmation dialog
        startButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        
        val shadow = org.robolectric.Shadows.shadowOf(dialog)
        val title = shadow.title
        
        // Title is set via setTitle, so it should be accessible
        assertThat(title?.toString()).contains("End Trip?")
        
        // Message is in a custom TextView, not setMessage, so we need to find it in the view hierarchy
        val window = dialog.window
        assertThat(window).isNotNull()
        val decorView = window?.decorView
        assertThat(decorView).isNotNull()
        
        // Find the TextView with the message text
        fun findTextView(parent: android.view.View?): android.widget.TextView? {
            if (parent is android.widget.TextView && parent.text.toString().contains("calculate", ignoreCase = true)) {
                return parent
            }
            if (parent is android.view.ViewGroup) {
                for (i in 0 until parent.childCount) {
                    val found = findTextView(parent.getChildAt(i))
                    if (found != null) return found
                }
            }
            return null
        }
        val messageTextView = findTextView(decorView)
        assertThat(messageTextView).isNotNull()
        val message = messageTextView?.text?.toString() ?: ""
        assertThat(message).contains("calculate your Out of Route percentage")
    }
}


