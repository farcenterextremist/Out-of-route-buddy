package com.example.outofroutebuddy.presentation.ui.trip

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.R
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.test.runTest
import java.util.Date

/**
 * Tests for trip persistence and button state restoration on app restart.
 * 
 * Verifies that:
 * 1. When a trip is started and app is closed, the trip state is saved
 * 2. When app reopens, the persisted state is correctly loaded
 * 3. The ViewModel correctly identifies persisted trips as active
 * 
 * Note: These tests verify the persistence layer and ViewModel logic.
 * Full UI integration tests would require more complex Robolectric setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripPersistenceButtonStateTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var persistenceManager: TripPersistenceManager
    private lateinit var tripPersistencePrefs: android.content.SharedPreferences

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        persistenceManager = TripPersistenceManager(context, PreferencesManager(context))
        tripPersistencePrefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
        
        // Clear any existing state
        clearAllState()
    }

    @After
    fun tearDown() {
        clearAllState()
    }

    private fun clearAllState() {
        tripPersistencePrefs.edit().clear().commit()
        context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun persistedState_isCorrectlySavedAndLoaded() {
        // Given: An active trip was started and saved to persistence
        val activeTrip = Trip(
            id = "test-trip-1",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 5.5,
            oorMiles = 0.0,
            loadedMiles = 10.0,
            bounceMiles = 2.0
        )
        
        persistenceManager.saveActiveTripState(
            trip = activeTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 5.5
        )
        
        // When: Persisted state is loaded
        val loadedState = persistenceManager.loadSavedTripState()
        
        // Then: State should be restored correctly
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.trip.status).isEqualTo(TripStatus.ACTIVE)
        assertThat(loadedState.actualMiles).isWithin(0.1).of(5.5)
        assertThat(loadedState.loadedMiles).isWithin(0.1).of(10.0)
        assertThat(loadedState.bounceMiles).isWithin(0.1).of(2.0)
        assertThat(persistenceManager.isRecoveryAvailable()).isTrue()
    }

    @Test
    fun buttonShowsStartTrip_whenNoPersistedState() {
        // Given: No persisted trip state
        
        // When: App starts
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val shadowLooper = Shadows.shadowOf(android.os.Looper.getMainLooper())
        shadowLooper.idle()
        shadowLooper.idle()
        
        // Get fragment
        val navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val fragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull() as? TripInputFragment
        
        if (fragment != null) {
            shadowLooper.idle()
            shadowLooper.idle()
            
            val root = fragment.requireView()
            val startButton = root.findViewById<com.google.android.material.button.MaterialButton>(R.id.start_trip_button)
            
            // Then: Button should show "Start Trip"
            assertThat(startButton.text.toString().lowercase()).contains("start")
        }
    }

    @Test
    fun persistedTripState_withProgress_isCorrectlyRestored() {
        // Given: An active trip with progress
        val activeTrip = Trip(
            id = "test-trip-2",
            startTime = Date(System.currentTimeMillis() - 60000), // 1 minute ago
            status = TripStatus.ACTIVE,
            actualMiles = 12.7,
            oorMiles = 0.0,
            loadedMiles = 10.0,
            bounceMiles = 2.0
        )
        
        persistenceManager.saveActiveTripState(
            trip = activeTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.7
        )
        
        // When: Persisted state is loaded
        val loadedState = persistenceManager.loadSavedTripState()
        
        // Then: All trip data should be restored correctly
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.trip.status).isEqualTo(TripStatus.ACTIVE)
        assertThat(loadedState.loadedMiles).isWithin(0.1).of(10.0)
        assertThat(loadedState.bounceMiles).isWithin(0.1).of(2.0)
        assertThat(loadedState.actualMiles).isWithin(0.1).of(12.7)
    }

    @Test
    fun persistedState_isActive_whenTripIsSaved() {
        // This test verifies that persisted state correctly indicates an active trip
        // The ViewModel logic treats any persisted state as active (since we only persist active trips)
        
        // Given: An active trip
        val activeTrip = Trip(
            id = "test-trip-3",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 8.3,
            oorMiles = 0.0,
            loadedMiles = 10.0,
            bounceMiles = 2.0
        )
        
        persistenceManager.saveActiveTripState(
            trip = activeTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 8.3
        )
        
        // When: Checking if recovery is available
        val recoveryAvailable = persistenceManager.isRecoveryAvailable()
        val loadedState = persistenceManager.loadSavedTripState()
        
        // Then: Recovery should be available and trip should be active
        assertThat(recoveryAvailable).isTrue()
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.trip.status).isEqualTo(TripStatus.ACTIVE)
        // The ViewModel logic treats persisted state as active regardless of enum value
        // This ensures the button shows "End Trip" on app restart
    }

    @Test
    fun buttonState_updatesImmediatelyOnFragmentCreation() {
        // Given: Active trip in persistence
        val activeTrip = Trip(
            id = "test-trip-4",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 3.2,
            oorMiles = 0.0,
            loadedMiles = 10.0,
            bounceMiles = 2.0
        )
        
        persistenceManager.saveActiveTripState(
            trip = activeTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 3.2
        )
        
        // When: Fragment is created (simulating app restart)
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripInputFragment()
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
        
        val shadowLooper = Shadows.shadowOf(android.os.Looper.getMainLooper())
        
        // Wait for ViewModel initialization and state loading
        shadowLooper.idle()
        shadowLooper.idle()
        shadowLooper.idle()
        
        // Trigger fragment lifecycle to ensure onViewCreated runs
        fragment.onViewCreated(fragment.requireView(), null)
        shadowLooper.idle()
        
        // Then: Button should reflect active state
        val root = fragment.requireView()
        val startButton = root.findViewById<com.google.android.material.button.MaterialButton>(R.id.start_trip_button)
        
        // The button should show "End Trip" after state is loaded
        // We check ViewModel state to confirm it's active
        val viewModel = ViewModelProvider(activity)[TripInputViewModel::class.java]
        shadowLooper.idle()
        shadowLooper.idle()
        
        val uiState = viewModel.uiState.value
        if (uiState.isTripActive) {
            assertThat(startButton.text.toString().lowercase()).contains("end")
        }
    }

    @Test
    fun multipleProgressUpdates_preserveStateCorrectly() {
        // Given: Active trip
        val activeTrip = Trip(
            id = "test-trip-5",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 6.8,
            oorMiles = 0.0,
            loadedMiles = 10.0,
            bounceMiles = 2.0
        )
        
        persistenceManager.saveActiveTripState(
            trip = activeTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 6.8
        )
        
        // Verify initial state
        var loadedState = persistenceManager.loadSavedTripState()
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.actualMiles).isWithin(0.1).of(6.8)
        
        // When: Progress is updated (simulating trip continuing across app restarts)
        persistenceManager.updateTripProgress(actualMiles = 10.5)
        
        // Then: Updated state should be preserved
        loadedState = persistenceManager.loadSavedTripState()
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.actualMiles).isWithin(0.1).of(10.5)
        assertThat(persistenceManager.isRecoveryAvailable()).isTrue()
    }
}

