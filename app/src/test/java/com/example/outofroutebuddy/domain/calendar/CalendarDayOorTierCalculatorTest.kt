package com.example.outofroutebuddy.domain.calendar

import com.example.outofroutebuddy.domain.models.CalendarDayOorTier
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

class CalendarDayOorTierCalculatorTest {

    private fun day(year: Int, month0: Int, day: Int): Date {
        val c = Calendar.getInstance()
        c.set(year, month0, day, 12, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

    private fun startOfDayMillis(year: Int, month0: Int, day: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month0, day, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun trip(
        start: Date,
        end: Date,
        loaded: Double,
        bounce: Double,
        actual: Double,
        oor: Double,
    ): Trip = Trip(
        loadedMiles = loaded,
        bounceMiles = bounce,
        actualMiles = actual,
        oorMiles = oor,
        oorPercentage = if (loaded + bounce > 0) (oor / (loaded + bounce)) * 100.0 else 0.0,
        startTime = start,
        endTime = end,
        status = TripStatus.COMPLETED,
    )

    @Test
    fun `blended tier green at exactly 10 percent`() {
        val d = day(2024, Calendar.MARCH, 10)
        val t = trip(d, d, loaded = 100.0, bounce = 0.0, actual = 110.0, oor = 10.0)
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t))
        assertEquals(CalendarDayOorTier.GREEN, tiers[startOfDayMillis(2024, Calendar.MARCH, 10)])
    }

    @Test
    fun `blended tier yellow just above 10 percent`() {
        val d = day(2024, Calendar.MARCH, 11)
        val t = trip(d, d, loaded = 100.0, bounce = 0.0, actual = 110.1, oor = 10.1)
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t))
        assertEquals(CalendarDayOorTier.YELLOW_OUTLINE, tiers[startOfDayMillis(2024, Calendar.MARCH, 11)])
    }

    @Test
    fun `blended tier yellow at exactly 14 percent`() {
        val d = day(2024, Calendar.MARCH, 12)
        val t = trip(d, d, loaded = 100.0, bounce = 0.0, actual = 114.0, oor = 14.0)
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t))
        assertEquals(CalendarDayOorTier.YELLOW_OUTLINE, tiers[startOfDayMillis(2024, Calendar.MARCH, 12)])
    }

    @Test
    fun `blended tier red above 14 percent`() {
        val d = day(2024, Calendar.MARCH, 13)
        val t = trip(d, d, loaded = 100.0, bounce = 0.0, actual = 114.1, oor = 14.1)
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t))
        assertEquals(CalendarDayOorTier.RED, tiers[startOfDayMillis(2024, Calendar.MARCH, 13)])
    }

    @Test
    fun `two trips same day blend oor and dispatched`() {
        val d1 = day(2024, Calendar.MARCH, 20)
        val t1 = trip(d1, d1, loaded = 50.0, bounce = 0.0, actual = 55.0, oor = 5.0) // 10%
        val t2 = trip(d1, d1, loaded = 50.0, bounce = 0.0, actual = 60.0, oor = 10.0) // 20%
        // total oor 15, dispatched 100 -> 15% -> red
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t1, t2))
        assertEquals(CalendarDayOorTier.RED, tiers[startOfDayMillis(2024, Calendar.MARCH, 20)])
    }

    @Test
    fun `midnight spanning trip marks both days`() {
        val start = day(2024, Calendar.MARCH, 31)
        val endCal = Calendar.getInstance().apply {
            time = start
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 23)
        }
        val end = endCal.time
        val t = trip(start, end, loaded = 100.0, bounce = 0.0, actual = 105.0, oor = 5.0)
        val tiers = CalendarDayOorTierCalculator.blendedOorTiersByDay(listOf(t))
        assertEquals(CalendarDayOorTier.GREEN, tiers[startOfDayMillis(2024, Calendar.MARCH, 31)])
        assertEquals(CalendarDayOorTier.GREEN, tiers[startOfDayMillis(2024, Calendar.APRIL, 1)])
    }
}
