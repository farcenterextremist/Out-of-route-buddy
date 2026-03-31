package com.example.outofroutebuddy.services

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripTrackingRecoveryReceiverTest {
    private lateinit var context: Application
    private lateinit var receiver: TripTrackingRecoveryReceiver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        receiver = TripTrackingRecoveryReceiver()
        shadowOf(context).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
        context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun onReceive_requestsRecoveryWhenTripSnapshotExists() {
        context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("was_tracking", true)
            .commit()
        context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("trip_recovery_available", true)
            .commit()

        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        val startedService = shadowOf(context).nextStartedService
        assertThat(startedService).isNotNull()
        assertThat(startedService.component?.className)
            .isEqualTo(TripTrackingService::class.java.name)
        assertThat(startedService.action).isEqualTo(ACTION_RECOVER_ACTIVE_TRIP)
    }

    @Test
    fun onReceive_skipsRecoveryWhenBackgroundPermissionMissing() {
        context.getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("was_tracking", true)
            .commit()
        context.getSharedPreferences("trip_persistence", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("trip_recovery_available", true)
            .commit()
        shadowOf(context).denyPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertThat(shadowOf(context).nextStartedService).isNull()
    }
}
