package com.example.outofroutebuddy.domain.calendar

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TripCalendarDaySliceTest {

    @Test
    fun `scales miles by half when half of trip duration falls on calendar day`() {
        val start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 23, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val end = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 1, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val trip =
            Trip(
                id = "1",
                loadedMiles = 50.0,
                bounceMiles = 10.0,
                actualMiles = 60.0,
                oorMiles = 0.0,
                oorPercentage = 0.0,
                startTime = start,
                endTime = end,
                status = TripStatus.COMPLETED,
            )

        val day11Start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val day12Start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 12, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val slice = trip.scaledToCalendarDay(day11Start, day12Start)

        assertEquals("1", slice.id)
        assertTrue(slice.isProportionalDaySlice)
        assertEquals(25.0, slice.loadedMiles, 0.01)
        assertEquals(5.0, slice.bounceMiles, 0.01)
        assertEquals(30.0, slice.actualMiles, 0.01)
        assertEquals(60, slice.gpsMetadata.tripDurationMinutes)
    }

    @Test
    fun `returns unchanged when entire trip within calendar day`() {
        val startTrip = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 5, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endTrip = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 5, 11, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val trip =
            Trip(
                id = "1",
                loadedMiles = 10.0,
                bounceMiles = 2.0,
                actualMiles = 12.0,
                oorMiles = 0.0,
                oorPercentage = 0.0,
                startTime = startTrip,
                endTime = endTrip,
                status = TripStatus.COMPLETED,
            )
        val dayStart = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 5, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val dayEndExclusive = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 6, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val out = trip.scaledToCalendarDay(dayStart, dayEndExclusive)
        assertFalse(out.isProportionalDaySlice)
        assertEquals(12.0, out.actualMiles, 0.001)
    }
}
