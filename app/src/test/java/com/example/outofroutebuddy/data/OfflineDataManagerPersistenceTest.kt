package com.example.outofroutebuddy.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.data.OfflineDataManager.SyncStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for OfflineDataManager load/save persistence.
 *
 * Verifies load/save round-trip and data survival across new instance (simulated restart).
 * Per docs/technical/OFFLINE_PERSISTENCE.md and docs/qa/TEST_PLAN_offline_persistence.md.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineDataManagerPersistenceTest {

    @Test
    fun saveTripAndLoadInNewInstance_persistsData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val networkStateManager = NetworkStateManager(context)
        val preferencesManager = PreferencesManager(context)

        val manager1 = OfflineDataManager(context, networkStateManager, preferencesManager)
        val localId = manager1.saveTripOffline(
            tripData = mapOf(
                "id" to "trip-1",
                "loadedMiles" to 100.0,
                "bounceMiles" to 25.0,
                "actualMiles" to 125.0
            ),
            gpsData = mapOf("lat" to 37.77, "lng" to -122.42)
        )

        assertThat(localId).isNotEmpty()
        assertThat(manager1.getOfflineDataCount()).isEqualTo(1)

        val manager2 = OfflineDataManager(context, networkStateManager, preferencesManager)
        assertThat(manager2.getOfflineDataCount()).isEqualTo(1)
        val loadedTrip = manager2.getOfflineTrip(localId)
        assertThat(loadedTrip).isNotNull()
        org.junit.Assert.assertEquals(125.0, (loadedTrip!!.tripData["actualMiles"] as Number).toDouble(), 0.01)
        assertThat(loadedTrip.syncStatus).isEqualTo(SyncStatus.PENDING)
    }

    // Sync status persistence: verified by saveTripAndLoadInNewInstance (trip data round-trip).
    // Full sync status test may need instrumented test due to DataStore process-scoped caching.
}
