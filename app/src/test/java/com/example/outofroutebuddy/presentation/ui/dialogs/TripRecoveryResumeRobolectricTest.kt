package com.example.outofroutebuddy.presentation.ui.dialogs

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Date

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, sdk = [34])
class TripRecoveryResumeRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    @Ignore("Recovery flow depends on real Application/Service context; Robolectric state not updated. Covered by instrumented tests.")
    fun continueTrip_resumesTracking_andSeedsMilesFromPersistence() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Seed persisted trip state
        val trip = Trip(
            id = "trip-test",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.3,
            status = TripStatus.ACTIVE,
            startTime = Date()
        )

        val persistence = TripPersistenceManager(context, PreferencesManager(context))
        persistence.saveActiveTripState(
            trip = trip,
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles,
            lastLocation = null,
            gpsMetadata = null
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val savedState = TripPersistenceManager.SavedTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.3,
            lastLocation = null,
            gpsMetadata = null,
            startTime = Date(),
            recoveryTime = Date()
        )
        (activity as TripRecoveryDialog.TripRecoveryListener).onContinueTrip(savedState)

        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val viewModel = androidx.lifecycle.ViewModelProvider(activity)[TripInputViewModel::class.java]
        val uiState = viewModel.uiState.value

        assertThat(uiState.isTripActive).isTrue()
        assertThat(uiState.loadedMiles).isEqualTo(10.0)
        assertThat(uiState.actualMiles).isEqualTo(12.3)
        assertThat(uiState.tripStatusMessage.lowercase()).contains("trip")
    }
}


