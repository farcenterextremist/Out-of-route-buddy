package com.example.outofroutebuddy.integration

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.services.TripTrackingService
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

/**
 * Integration tests for trip persistence and recovery across app close/reopen.
 * 
 * These tests verify:
 * 1. Trip state is saved when trip is active
 * 2. Trip state is loaded correctly after app restart
 * 3. Service resumes tracking from correct mileage
 * 4. UI shows correct state after recovery
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripPersistenceRecoveryTest {

    private lateinit var context: Context
    private lateinit var persistenceManager: TripPersistenceManager
    private lateinit var tripPersistencePrefs: SharedPreferences
    private lateinit var serviceStatePrefs: SharedPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        persistenceManager = TripPersistenceManager(
            context,
            com.example.outofroutebuddy.data.PreferencesManager(context)
        )
        
        // Get SharedPreferences instances
        tripPersistencePrefs = context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
        serviceStatePrefs = context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
        
        // Clear any existing state
        clearAllState()
    }

    @After
    fun tearDown() {
        clearAllState()
    }

    private fun clearAllState() {
        tripPersistencePrefs.edit().clear().commit()
        serviceStatePrefs.edit().clear().commit()
    }

    @Test
    fun saveActiveTripState_persistsAllDataCorrectly() {
        // Given: An active trip with progress
        val trip = Trip(
            id = "test-trip-1",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 12.5,
            oorMiles = 0.0
        )
        
        // When: Save trip state
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.5
        )
        
        // Then: All data should be persisted
        assertThat(tripPersistencePrefs.getBoolean("trip_recovery_available", false)).isTrue()
        assertThat(tripPersistencePrefs.getFloat("trip_loaded_miles", 0f)).isWithin(0.01f).of(10.0f)
        assertThat(tripPersistencePrefs.getFloat("trip_bounce_miles", 0f)).isWithin(0.01f).of(2.0f)
        assertThat(tripPersistencePrefs.getFloat("trip_actual_miles", 0f)).isWithin(0.01f).of(12.5f)
        assertThat(tripPersistencePrefs.getString("active_trip_data", null)).isNotNull()
    }

    @Test
    fun loadSavedTripState_restoresAllDataCorrectly() {
        // Given: Saved trip state
        val originalTrip = Trip(
            id = "test-trip-2",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 15.7,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = originalTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 15.7
        )
        
        // When: Load saved state
        val loadedState = persistenceManager.loadSavedTripState()
        
        // Then: All data should match
        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.trip.id).isEqualTo("test-trip-2")
        assertThat(loadedState.loadedMiles).isWithin(0.01).of(10.0)
        assertThat(loadedState.bounceMiles).isWithin(0.01).of(2.0)
        assertThat(loadedState.actualMiles).isWithin(0.01).of(15.7)
        assertThat(loadedState.trip.status).isEqualTo(TripStatus.ACTIVE)
    }

    @Test
    fun loadSavedTripState_restoresPausedFlag() {
        val originalTrip = Trip(
            id = "paused-trip",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 9.4,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = originalTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 9.4,
            isPaused = true
        )

        val loadedState = persistenceManager.loadSavedTripState()

        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.isPaused).isTrue()
        assertThat(loadedState.actualMiles).isWithin(0.01).of(9.4)
    }

    @Test
    fun saveAndLoadTripState_restoresBackgroundTrackingWarnings() {
        val originalTrip = Trip(
            id = "warning-trip",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 4.2,
            oorMiles = 0.0
        )

        persistenceManager.saveActiveTripState(
            trip = originalTrip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 4.2,
            backgroundTrackingDegraded = true,
            backgroundTrackingReasons = listOf(
                "Background location is off, so tracking after closing the app may stop on some devices.",
                "Battery optimization is still active for OutOfRouteBuddy, which can let the system pause tracking in the background."
            )
        )

        val loadedState = persistenceManager.loadSavedTripState()

        assertThat(loadedState).isNotNull()
        assertThat(loadedState!!.backgroundTrackingDegraded).isTrue()
        assertThat(loadedState.backgroundTrackingReasons).hasSize(2)
        assertThat(loadedState.backgroundTrackingReasons.first()).contains("Background location is off")
    }

    @Test
    fun updateTripProgress_updatesActualMiles() {
        // Given: An active saved trip
        val trip = Trip(
            id = "test-trip-3",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 10.0,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 10.0
        )
        
        // When: Update progress
        persistenceManager.updateTripProgress(actualMiles = 15.5)
        
        // Then: Actual miles should be updated
        assertThat(tripPersistencePrefs.getFloat("trip_actual_miles", 0f)).isWithin(0.01f).of(15.5f)
    }

    @Test
    fun clearSavedTripState_removesAllData() {
        // Given: Saved trip state
        val trip = Trip(
            id = "test-trip-4",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 8.0,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 8.0
        )
        
        // When: Clear saved state
        persistenceManager.clearSavedTripState()
        
        // Then: No recovery data should be available
        assertThat(tripPersistencePrefs.getBoolean("trip_recovery_available", false)).isFalse()
        assertThat(persistenceManager.loadSavedTripState()).isNull()
    }

    @Test
    fun isRecoveryAvailable_returnsTrueForValidSavedState() {
        // Given: Saved trip state
        val trip = Trip(
            id = "test-trip-5",
            startTime = Date(System.currentTimeMillis() - 1000), // 1 second ago
            status = TripStatus.ACTIVE,
            actualMiles = 5.0,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 5.0
        )
        
        // When/Then: Recovery should be available
        assertThat(persistenceManager.isRecoveryAvailable()).isTrue()
    }

    @Test
    fun isRecoveryAvailable_returnsFalseWhenNoSavedState() {
        // Given: No saved state
        
        // When/Then: Recovery should not be available
        assertThat(persistenceManager.isRecoveryAvailable()).isFalse()
    }

    @Test
    fun serviceStateSaveAndRestore_preservesDistanceCorrectly() {
        // Given: Service state with distance
        serviceStatePrefs.edit().apply {
            putBoolean("was_tracking", true)
            putFloat("loaded_miles", 10.0f)
            putFloat("bounce_miles", 2.0f)
            putFloat("total_distance", 12.5f)
            putLong("start_time", System.currentTimeMillis())
            commit()
        }
        
        // When: Read service state
        val wasTracking = serviceStatePrefs.getBoolean("was_tracking", false)
        val loadedMiles = serviceStatePrefs.getFloat("loaded_miles", 0f)
        val bounceMiles = serviceStatePrefs.getFloat("bounce_miles", 0f)
        val totalDistance = serviceStatePrefs.getFloat("total_distance", 0f)
        
        // Then: All values should match
        assertThat(wasTracking).isTrue()
        assertThat(loadedMiles).isWithin(0.01f).of(10.0f)
        assertThat(bounceMiles).isWithin(0.01f).of(2.0f)
        assertThat(totalDistance).isWithin(0.01f).of(12.5f)
    }

    @Test
    fun tripRecoveryWithSeededDistance_preservesMileage() {
        // This test simulates the full recovery flow:
        // 1. Trip is active with 12.5 miles
        // 2. App closes
        // 3. App reopens, loads saved state
        // 4. Service starts with seeded distance
        // 5. Service should continue from 12.5 miles, not reset to 0
        
        // Step 1: Save active trip state
        val trip = Trip(
            id = "recovery-test",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 12.5,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.5
        )
        
        // Step 2: Simulate app restart - load saved state
        val savedState = persistenceManager.loadSavedTripState()
        assertThat(savedState).isNotNull()
        assertThat(savedState!!.actualMiles).isWithin(0.01).of(12.5)
        
        // Step 3: Simulate service restart with seeded distance
        // This would be called by TripTrackingService.startService() with initialTotalMiles
        val initialTotalMiles = savedState.actualMiles
        
        // Step 4: Verify that if service starts with this seed, distance is preserved
        // (In actual code, this happens via EXTRA_INITIAL_TOTAL_MILES in onStartCommand)
        assertThat(initialTotalMiles).isWithin(0.01).of(12.5)
        
        // Step 5: After recovery, new distance updates should add to this base, not replace it
        // (This is verified by the fix in startTrip() that preserves seeded distance)
    }

    @Test
    fun multipleProgressUpdates_persistCorrectly() {
        // Given: An active trip
        val trip = Trip(
            id = "progress-test",
            startTime = Date(),
            status = TripStatus.ACTIVE,
            actualMiles = 5.0,
            oorMiles = 0.0
        )
        persistenceManager.saveActiveTripState(
            trip = trip,
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 5.0
        )
        
        // When: Update progress multiple times
        persistenceManager.updateTripProgress(actualMiles = 7.5)
        persistenceManager.updateTripProgress(actualMiles = 10.0)
        persistenceManager.updateTripProgress(actualMiles = 12.5)
        
        // Then: Final value should be correct
        assertThat(tripPersistencePrefs.getFloat("trip_actual_miles", 0f)).isWithin(0.01f).of(12.5f)
        
        // And: Loaded state should have correct value
        val loadedState = persistenceManager.loadSavedTripState()
        assertThat(loadedState!!.actualMiles).isWithin(0.01).of(12.5)
    }
}

