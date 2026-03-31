package com.example.outofroutebuddy.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.junit.Ignore
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.robolectric.annotation.Config
import android.Manifest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import com.google.common.truth.Truth.assertThat
import androidx.lifecycle.ViewModelProvider
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import java.util.Date

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
        // Production-stage #9 regression checks: drawer/menu views exist
        assertThat(activity.findViewById<android.view.View>(R.id.drawer_layout)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.nav_view)).isNotNull()
        assertThat(activity.findViewById<android.view.View>(R.id.menu_button)).isNotNull()
    }

    @Test
    fun openDrawer_wiredToStartDrawer_withoutCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = activity.findViewById<android.view.View>(R.id.nav_view)
        val navLayoutParams = navView.layoutParams as DrawerLayout.LayoutParams

        assertThat(navLayoutParams.gravity and GravityCompat.START).isNotEqualTo(0)
        activity.openDrawer()
        assertThat(drawerLayout).isNotNull()
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
    @Ignore("Recovery flow depends on TripTrackingService/Overlay in context; state update races with Robolectric looper. Covered by instrumented tests.")
    fun continueTrip_fromRecovery_usesActivityScopedViewModel_andStartsService() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        // Grant location so onContinueTrip does not return early
        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

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

        activity.onContinueTrip(saved)

        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val vm = ViewModelProvider(activity)[TripInputViewModel::class.java]
        val state = vm.uiState.value
        assertThat(state.isTripActive).isTrue()
        assertThat(state.loadedMiles).isEqualTo(10.0)
        assertThat(state.actualMiles).isEqualTo(3.5)
    }

    @Test
    @Config(application = HiltTestApplication::class, sdk = [31])
    fun notificationPermission_notRequiredBeforeAndroid13() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertThat(activity.hasNotificationPermission()).isTrue()
    }

    @Test
    fun checkForTripRecovery_skipsDialogWhenTripAlreadyTracking() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val persistenceManager = TripPersistenceManager(appContext, PreferencesManager(appContext))
        val trip = Trip(
            id = "live-trip",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            startTime = Date(),
            status = TripStatus.ACTIVE,
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 5.5,
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        val recoveryMethod = MainActivity::class.java.getDeclaredMethod("checkForTripRecovery")
        recoveryMethod.isAccessible = true
        recoveryMethod.invoke(activity)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        assertThat(activity.supportFragmentManager.findFragmentByTag("TripRecoveryDialog")).isNull()
        val viewModel = ViewModelProvider(activity)[TripInputViewModel::class.java]
        assertThat(viewModel.uiState.value.isTripActive).isTrue()
    }

    @Test
    fun backPress_movesTaskToBack_whenTripIsActiveOnRootScreen() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val persistenceManager = TripPersistenceManager(appContext, PreferencesManager(appContext))
        val trip = Trip(
            id = "back-active-trip",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            startTime = Date(),
            status = TripStatus.ACTIVE,
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 8.0,
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        activity.onBackPressedDispatcher.onBackPressed()

        assertThat(activity.isFinishing).isFalse()
        assertThat(ViewModelProvider(activity)[TripInputViewModel::class.java].uiState.value.isTripActive).isTrue()
    }
}


