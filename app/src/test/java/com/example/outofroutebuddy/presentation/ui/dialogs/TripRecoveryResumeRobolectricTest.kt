package com.example.outofroutebuddy.presentation.ui.dialogs

import android.app.Dialog
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Date

@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(sdk = [34])
class TripRecoveryResumeRobolectricTest {

    @Test
    fun continueTrip_resumesTracking_andSeedsMilesFromPersistence() {
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

        // Launch activity - it should detect recovery and show dialog
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        // Find the recovery dialog and click Continue
        val latestDialog: Dialog? = ShadowAlertDialog.getLatestAlertDialog()
        // Some implementations inflate a custom dialog; fallback to tag lookup if needed
        if (latestDialog != null) {
            // Try to click the positive/continue button by text if present
            val shadow = Shadows.shadowOf(latestDialog)
            // No direct button handle; rely on existing dialog tests for basic presence
            latestDialog.findViewById<android.widget.Button>(com.example.outofroutebuddy.R.id.button_continue_trip)?.performClick()
        } else {
            // If not captured by ShadowAlertDialog (custom content view), invoke dialog fragment via tag
            val fragment = activity.supportFragmentManager.findFragmentByTag("TripRecoveryDialog") as? TripRecoveryDialog
            // Simulate continue
            fragment?.let {
                val field = TripRecoveryDialog::class.java.getDeclaredField("savedState").apply { isAccessible = true }
                val savedState = field.get(it) as TripPersistenceManager.SavedTripState
                (activity as TripRecoveryDialog.TripRecoveryListener).onContinueTrip(savedState)
            }
        }

        // Obtain the Activity-scoped ViewModel and verify state
        val viewModel = androidx.lifecycle.ViewModelProvider(activity)[TripInputViewModel::class.java]
        val uiState = viewModel.uiState.value

        assertThat(uiState.isTripActive).isTrue()
        assertThat(Math.abs(uiState.actualMiles - 12.3)).isLessThan(1e-6)
        // Status message should reflect recovery/active state
        assertThat(uiState.tripStatusMessage.lowercase()).contains("trip")
    }
}


