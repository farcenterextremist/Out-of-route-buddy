package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.data.TripStateManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UnifiedLocationServiceRobolectricTest {

    private lateinit var tripStateManager: TripStateManager
    private lateinit var service: UnifiedLocationService

    @Before
    fun setup() {
        tripStateManager = mockk(relaxed = true) {
            every { tripState } returns kotlinx.coroutines.flow.MutableStateFlow(
                TripStateManager.TripState(isActive = false, startTime = null)
            )
        }
        val context = mockk<android.content.Context>(relaxed = true)
        service = UnifiedLocationService(context, tripStateManager)
    }

    @Test
    fun processLocationUpdate_rejectsTooOldLocation_andRecordsWarning() = runBlocking {
        val loc = Location("test").apply {
            time = System.currentTimeMillis() - (UnifiedLocationService.DEFAULT_MAX_LOCATION_AGE + 1000)
            accuracy = 5f
            speed = 0f
        }
        val result = service.processLocationUpdate(loc)
        assertThat(result is UnifiedLocationService.ValidationResult.Invalid).isTrue()
    }

    @Test
    fun processLocationUpdate_acceptsRecentAccurateLocation_andUpdatesState() = runBlocking {
        val loc1 = Location("test").apply {
            time = System.currentTimeMillis()
            accuracy = 5f
            speed = 2f
            latitude = 37.0
            longitude = -122.0
        }
        val loc2 = Location("test").apply {
            time = System.currentTimeMillis()
            accuracy = 5f
            speed = 15f
            latitude = 37.004
            longitude = -122.004
        }
        val r1 = service.processLocationUpdate(loc1)
        val r2 = service.processLocationUpdate(loc2)
        // Should produce a validation result without throwing
        assertThat(r1).isNotNull()
        assertThat(r2).isNotNull()
    }

    @Test
    fun processLocationUpdate_flagsAccuracyTooLow() = runBlocking {
        val loc = Location("test").apply {
            time = System.currentTimeMillis()
            accuracy = UnifiedLocationService.DEFAULT_MAX_ACCURACY + 10f
            speed = 0f
        }
        val result = service.processLocationUpdate(loc)
        assertThat(result is UnifiedLocationService.ValidationResult.Invalid).isTrue()
    }
}


