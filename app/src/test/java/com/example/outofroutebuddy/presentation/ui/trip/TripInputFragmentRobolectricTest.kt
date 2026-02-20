package com.example.outofroutebuddy.presentation.ui.trip

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
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

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripInputFragmentRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun onCreateView_initialViewsPresent_andStartButtonText() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = activityController.get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        val root = fragment.requireView()
        assertThat(root.findViewById<android.view.View>(R.id.loaded_miles_input)).isNotNull()
        assertThat(root.findViewById<android.view.View>(R.id.bounce_miles_input)).isNotNull()
        val startButton = root.findViewById<com.google.android.material.button.MaterialButton>(R.id.start_trip_button)
        assertThat(startButton).isNotNull()
        assertThat(startButton.text.toString().lowercase()).contains("start")
    }

    @Test
    fun onPause_savesTextInputsToPrefs() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = activityController.get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        val root = fragment.requireView()
        val loaded = root.findViewById<android.widget.EditText>(R.id.loaded_miles_input)
        val bounce = root.findViewById<android.widget.EditText>(R.id.bounce_miles_input)
        loaded.setText("12.3")
        bounce.setText("4.5")

        // Trigger pause which writes to SharedPreferences
        activityController.pause()

        val prefs = fragment.requireContext().getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
        assertThat(prefs.getString("loaded_miles_input", "")).isEqualTo("12.3")
        assertThat(prefs.getString("bounce_miles_input", "")).isEqualTo("4.5")
    }

    @Test
    fun onViewCreated_restoresTextInputsFromPrefs_whenTripNotActive() {
        // Pre-populate SharedPreferences before launching fragment
        val preActivity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        preActivity.getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
            .edit()
            .putString("loaded_miles_input", "7.8")
            .putString("bounce_miles_input", "1.2")
            .apply()

        val activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = activityController.get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        val root = fragment.requireView()
        val loaded = root.findViewById<android.widget.EditText>(R.id.loaded_miles_input)
        val bounce = root.findViewById<android.widget.EditText>(R.id.bounce_miles_input)
        assertThat(loaded.text.toString()).isEqualTo("7.8")
        assertThat(bounce.text.toString()).isEqualTo("1.2")
    }
}


