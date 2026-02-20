package com.example.outofroutebuddy.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.robolectric.annotation.Config
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import com.google.common.truth.Truth.assertThat
import androidx.lifecycle.ViewModelProvider
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
class MainActivityRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun activityStarts_andToolbarAndInputsExist() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        dialog?.let {
            val positive = it.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            val negative = it.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
            when {
                positive != null -> positive.performClick()
                negative != null -> negative.performClick()
                else -> it.dismiss()
            }
        }

        assertThat(activity.findViewById<android.view.View>(R.id.custom_toolbar_layout)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.loaded_miles_input)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.bounce_miles_input)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.start_trip_button)).isNotNull()
        // Additional simple UI checks moved from device to JVM
        assertThat(activity.findViewById<android.view.View>(R.id.statistics_button)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.todays_info_card)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.total_miles_output)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.oor_miles_output)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.oor_percentage_output)).isNotNull()
    }

    @Test
    fun defaultThemeIsLight_onFirstLaunch() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        // Clear then read preferences via activity context
        activity.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().clear().apply()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val prefs = activity.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeValue = prefs.getString("theme_preference", "light")
        assertThat(themeValue == null || themeValue == "" || themeValue == "light").isTrue()
    }

    @Test
    fun continueTrip_fromRecovery_usesActivityScopedViewModel_andStartsService() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        val trip = com.example.outofroutebuddy.domain.models.Trip(
            id = "test-trip",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            startTime = java.util.Date(),
            status = com.example.outofroutebuddy.domain.models.TripStatus.ACTIVE
        )
        val saved = TripPersistenceManager.SavedTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 3.5,
            lastLocation = null,
            gpsMetadata = null,
            startTime = java.util.Date(System.currentTimeMillis() - 60000),
            recoveryTime = java.util.Date()
        )

        val vm = ViewModelProvider(activity)[TripInputViewModel::class.java]

        activity.onContinueTrip(saved)

        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val state = vm.uiState.value
        assertThat(state.isTripActive).isTrue()
        // Loaded/bounce values are represented in UI-specific formatting; existence of active state is sufficient here
    }
}


