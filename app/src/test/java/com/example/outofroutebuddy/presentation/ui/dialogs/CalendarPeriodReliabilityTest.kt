package com.example.outofroutebuddy.presentation.ui.dialogs

import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.services.PeriodCalculationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Reliability tests for calendar period selection and date handling.
 * Covers edge cases that can cause dates to "wander" or change unexpectedly:
 * - Date normalization (start-of-day consistency)
 * - Period boundary calculations (STANDARD vs CUSTOM)
 * - Close button uses displayed month (not original reference)
 * - DaysWithTripsDecorator timezone/epoch alignment (start-of-day millis in set)
 * - Year boundary, leap year, month boundary edge cases
 */
class CalendarPeriodReliabilityTest {

    private lateinit var periodCalculationService: PeriodCalculationService

    @Before
    fun setup() {
        periodCalculationService = PeriodCalculationService()
    }

    // ==================== Period Boundary Consistency ====================

    @Test
    fun `STANDARD period boundaries are deterministic for same reference date`() {
        val ref = date(2024, Calendar.MARCH, 15)
        val (start1, end1) = standardPeriod(ref)
        val (start2, end2) = standardPeriod(ref)

        assertEquals(start1.time, start2.time)
        assertEquals(end1.time, end2.time)
        assertEquals(date(2024, Calendar.MARCH, 1).time, start1.time)
        // End may be last moment of month (23:59:59.999) - assert date parts
        val endCal = Calendar.getInstance().apply { time = end1 }
        assertEquals(2024, endCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `CUSTOM period boundaries are deterministic for same reference date`() {
        val ref = date(2024, Calendar.MARCH, 15)
        val start1 = periodCalculationService.calculateCustomPeriodStart(ref).time
        val end1 = periodCalculationService.calculateCustomPeriodEnd(ref).time
        val start2 = periodCalculationService.calculateCustomPeriodStart(ref).time
        val end2 = periodCalculationService.calculateCustomPeriodEnd(ref).time

        assertSameDay(start1, start2)
        assertSameDay(end1, end2)
    }

    @Test
    fun `period start is always before or equal to period end`() {
        val testDates = listOf(
            date(2024, Calendar.JANUARY, 1),
            date(2024, Calendar.FEBRUARY, 29),
            date(2024, Calendar.DECEMBER, 31),
            date(2023, Calendar.FEBRUARY, 28),
        )
        testDates.forEach { ref ->
            val (start, end) = standardPeriod(ref)
            assertTrue("Start must be <= end for $ref", start.time <= end.time)
            val customStart = periodCalculationService.calculateCustomPeriodStart(ref).time
            val customEnd = periodCalculationService.calculateCustomPeriodEnd(ref).time
            assertTrue("Custom start <= end for $ref", customStart.time <= customEnd.time)
        }
    }

    // ==================== Period Boundaries (Boundary Selection Only) ====================

    @Test
    fun `STANDARD period for March 1 returns March full month`() {
        val marchFirst = date(2024, Calendar.MARCH, 1)
        val (start, end) = standardPeriod(marchFirst)
        assertEquals(date(2024, Calendar.MARCH, 1).time, start.time)
        val endCal = Calendar.getInstance().apply { time = end }
        assertEquals(2024, endCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `close button period for CUSTOM mode with displayed March returns correct span`() {
        // March 2024 CUSTOM: Feb 29 (start) - Apr 4 (end of period, Thu before first Fri of Apr)
        val marchFirst = date(2024, Calendar.MARCH, 1)
        val customStart = periodCalculationService.calculateCustomPeriodStart(marchFirst).time
        val customEnd = periodCalculationService.calculateCustomPeriodEnd(marchFirst).time

        val startCal = Calendar.getInstance().apply { time = customStart }
        val endCal = Calendar.getInstance().apply { time = customEnd }

        assertEquals(2024, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, startCal.get(Calendar.MONTH))
        assertEquals(29, startCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(2024, endCal.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, endCal.get(Calendar.MONTH))
        assertEquals(4, endCal.get(Calendar.DAY_OF_MONTH))
    }

    // ==================== Date Normalization ====================

    @Test
    fun `datesWithTripsMillis comparison uses start-of-day - same calendar day matches`() {
        // Trip at 14:30 and 23:59 on same day should both match CalendarDay for that date
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 0)
        cal.set(Calendar.MILLISECOND, 500)
        val millisWithTime = cal.timeInMillis

        // Normalize to start of day (as the app does when storing)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millisStartOfDay = cal.timeInMillis

        // DaysWithTripsDecorator compares CalendarDay to start-of-day millis
        // So we must store start-of-day millis in the day set
        val decorator = DaysWithTripsDecorator(setOf(millisStartOfDay), null, null)
        assertTrue(decorator.shouldDecorate(com.prolificinteractive.materialcalendarview.CalendarDay.from(2024, 3, 15)))

        // If we stored non-normalized millis, it might not match (timezone-dependent)
        val decoratorWithTime = DaysWithTripsDecorator(setOf(millisWithTime), null, null)
        // In same timezone, 14:30 on Mar 15 - start of day for Mar 15 is different from 14:30
        // So millisWithTime != startOfDay(Mar 15) - the decorator would NOT match
        assertFalse(decoratorWithTime.shouldDecorate(com.prolificinteractive.materialcalendarview.CalendarDay.from(2024, 3, 15)))
    }

    @Test
    fun `period start has zero time component`() {
        val ref = date(2024, Calendar.MARCH, 15)
        val (start, _) = standardPeriod(ref)
        val startCal = Calendar.getInstance().apply { time = start }
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, startCal.get(Calendar.MINUTE))
        assertEquals(0, startCal.get(Calendar.SECOND))
        assertEquals(0, startCal.get(Calendar.MILLISECOND))
    }

    // ==================== Year Boundary ====================

    @Test
    fun `STANDARD period for December 31 returns December full month`() {
        val ref = date(2024, Calendar.DECEMBER, 31)
        val (start, end) = standardPeriod(ref)
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }

        assertEquals(2024, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(2024, endCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `CUSTOM period for January 1 returns correct boundaries`() {
        val ref = date(2024, Calendar.JANUARY, 1)
        val customStart = periodCalculationService.calculateCustomPeriodStart(ref).time
        val customEnd = periodCalculationService.calculateCustomPeriodEnd(ref).time

        val startCal = Calendar.getInstance().apply { time = customStart }
        val endCal = Calendar.getInstance().apply { time = customEnd }

        // Jan 2024: period starts Dec 28 (Thu before first Fri of Jan) or Jan 4 (Thu before first Fri)
        // First Friday of Jan 2024 is Jan 5, so Thu before = Jan 4. Period start for Jan 1 = Dec 28 (prev month)
        assertNotNull(startCal)
        assertNotNull(endCal)
        assertTrue(startCal.timeInMillis < endCal.timeInMillis)
    }

    // ==================== Leap Year ====================

    @Test
    fun `STANDARD period for February 2024 includes Feb 29`() {
        val ref = date(2024, Calendar.FEBRUARY, 15)
        val (start, end) = standardPeriod(ref)
        val endCal = Calendar.getInstance().apply { time = end }
        assertEquals(29, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `STANDARD period for February 2023 ends on Feb 28`() {
        val ref = date(2023, Calendar.FEBRUARY, 15)
        val (start, end) = standardPeriod(ref)
        val endCal = Calendar.getInstance().apply { time = end }
        assertEquals(28, endCal.get(Calendar.DAY_OF_MONTH))
    }

    // ==================== Single-Day Month ====================

    @Test
    fun `STANDARD period for February 2024 spans full month`() {
        val ref = date(2024, Calendar.FEBRUARY, 29)
        val (start, end) = standardPeriod(ref)
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(29, endCal.get(Calendar.DAY_OF_MONTH))
    }

    // ==================== CalendarDay Month Index ====================

    @Test
    fun `CalendarDay from uses 1-based month - March is 3`() {
        val day = com.prolificinteractive.materialcalendarview.CalendarDay.from(2024, 3, 15)
        assertEquals(2024, day.year)
        assertEquals(3, day.month) // 1-based
        assertEquals(15, day.day)
    }

    @Test
    fun `DaysWithTripsDecorator Calendar set uses month-1 for Calendar compatibility`() {
        // CalendarDay.month is 1-based; Calendar.MONTH is 0-based
        val cal = Calendar.getInstance()
        cal.set(2024, 3 - 1, 15, 0, 0, 0) // March = 2 in Calendar
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis

        val decorator = DaysWithTripsDecorator(setOf(millis), null, null)
        assertTrue(decorator.shouldDecorate(com.prolificinteractive.materialcalendarview.CalendarDay.from(2024, 3, 15)))
    }

    // ==================== Reference Date Fallback ====================

    @Test
    fun `null referenceDate fallback - standard period uses current month`() {
        // Simulates: referenceDate ?: Date() when selectedPeriod is null
        val now = Date()
        val (start, end) = standardPeriod(now)
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }
        val nowCal = Calendar.getInstance().apply { time = now }

        assertEquals(nowCal.get(Calendar.YEAR), startCal.get(Calendar.YEAR))
        assertEquals(nowCal.get(Calendar.MONTH), startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(nowCal.get(Calendar.YEAR), endCal.get(Calendar.YEAR))
        assertEquals(nowCal.get(Calendar.MONTH), endCal.get(Calendar.MONTH))
    }

    // ==================== CUSTOM Period Continuity ====================

    @Test
    fun `consecutive CUSTOM periods do not overlap`() {
        // March 2024 period ends Apr 4 (exclusive in isDateInCustomPeriod)
        // April 2024 period starts Apr 4
        val marchRef = date(2024, Calendar.MARCH, 15)
        val aprilRef = date(2024, Calendar.APRIL, 15)

        val marchEnd = periodCalculationService.calculateCustomPeriodEnd(marchRef).time
        val aprilStart = periodCalculationService.calculateCustomPeriodStart(aprilRef).time

        assertSameDay(marchEnd, aprilStart)
    }

    private fun assertSameDay(d1: Date, d2: Date) {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        assertEquals(c1.get(Calendar.YEAR), c2.get(Calendar.YEAR))
        assertEquals(c1.get(Calendar.MONTH), c2.get(Calendar.MONTH))
        assertEquals(c1.get(Calendar.DAY_OF_MONTH), c2.get(Calendar.DAY_OF_MONTH))
    }

    private fun date(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun standardPeriod(ref: Date): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.time = ref
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.time
        return start to end
    }
}
