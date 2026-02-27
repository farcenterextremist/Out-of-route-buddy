package com.example.outofroutebuddy.data

import com.example.outofroutebuddy.data.TripStateManager.LocationData
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for location jump detection in TripStateManager.
 * Per docs/technical/JUMP_DETECTION_AND_TRIP_STATE.md.
 *
 * A "jump" is when implied speed (distance/time) exceeds 120 mph (~53.6 m/s).
 */
class TripStateManagerJumpDetectionTest {

    private lateinit var mockPreferencesManager: PreferencesManager
    private lateinit var tripStateManager: TripStateManager

    @Before
    fun setUp() {
        mockPreferencesManager = mockk(relaxed = true)
        every { mockPreferencesManager.getLastLoadedMiles() } returns ""
        every { mockPreferencesManager.getLastBounceMiles() } returns ""
        tripStateManager = TripStateManager(mockPreferencesManager)
    }

    @Test
    fun `two points 1 km apart 1 second apart results in one jump`() {
        // Start trip to enable location updates
        tripStateManager.startTrip("100", "25")

        val baseTime = System.currentTimeMillis()
        val loc1 = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = Date(baseTime),
            speed = 0f,
        )
        // ~1 km north: 1 degree lat ≈ 111 km, so 1/111 degree ≈ 0.009 degree
        val loc2 = LocationData(
            latitude = 40.7218, // ~1 km north
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = Date(baseTime + 1000), // 1 second later
            speed = 0f,
        )

        tripStateManager.updateLocation(loc1)
        tripStateManager.updateLocation(loc2)

        val metadata = tripStateManager.getCurrentState().gpsMetadata
        assertEquals(
            "Implied speed ~1000 m/s >> 53.6 m/s threshold; should count as 1 jump",
            1,
            metadata.locationJumps,
        )
    }

    @Test
    fun `two points with normal speed do not count as jump`() {
        tripStateManager.startTrip("100", "25")

        val baseTime = System.currentTimeMillis()
        // ~50m apart, 2 seconds = 25 m/s (well under 53.6 m/s)
        val loc1 = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = Date(baseTime),
            speed = 0f,
        )
        val loc2 = LocationData(
            latitude = 40.7132, // ~50m north
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = Date(baseTime + 2000),
            speed = 0f,
        )

        tripStateManager.updateLocation(loc1)
        tripStateManager.updateLocation(loc2)

        val metadata = tripStateManager.getCurrentState().gpsMetadata
        assertEquals(
            "Implied speed ~25 m/s < 53.6 m/s; should be 0 jumps",
            0,
            metadata.locationJumps,
        )
    }

    @Test
    fun `first location update has no jump - no lastLocation`() {
        tripStateManager.startTrip("100", "25")

        val loc = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = Date(),
            speed = 0f,
        )
        tripStateManager.updateLocation(loc)

        val metadata = tripStateManager.getCurrentState().gpsMetadata
        assertEquals(0, metadata.locationJumps)
    }
}
