package com.example.outofroutebuddy.presentation.ui.dialogs

import com.prolificinteractive.materialcalendarview.CalendarDay
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Unit tests for CustomCalendarDialog decorators (StartDate, EndDate, CurrentDate, DaysWithTrips).
 *
 * Tests edge cases:
 * - Today equals period start (grey should NOT override green)
 * - Today equals period end (grey should NOT override red)
 * - Today as regular day in period (grey applies)
 * - Single-day period (start == end)
 * - DaysWithTripsDecorator time normalization (start-of-day millis in set)
 * - Empty day set
 */
class CustomCalendarDialogDecoratorTest {

    // ==================== StartDateDecorator ====================

    @Test
    fun `StartDateDecorator decorates only start date`() {
        val startDate = CalendarDay.from(2024, 3, 1)
        val decorator = StartDateDecorator(startDate)

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 1)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 2)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 31)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 2, 29)))
    }

    @Test
    fun `StartDateDecorator does not decorate other months`() {
        val startDate = CalendarDay.from(2024, 3, 1)
        val decorator = StartDateDecorator(startDate)

        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 2, 1)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 4, 1)))
    }

    // ==================== EndDateDecorator ====================

    @Test
    fun `EndDateDecorator decorates only end date`() {
        val endDate = CalendarDay.from(2024, 3, 31)
        val decorator = EndDateDecorator(endDate)

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 31)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 30)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 1)))
    }

    @Test
    fun `EndDateDecorator handles leap year February 29`() {
        val endDate = CalendarDay.from(2024, 2, 29)
        val decorator = EndDateDecorator(endDate)

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 2, 29)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 2, 28)))
    }

    // ==================== CurrentDateDecorator - Edge Cases ====================

    @Test
    fun `CurrentDateDecorator does NOT decorate when today equals period start`() {
        val today = CalendarDay.from(2024, 3, 1)
        val startDate = CalendarDay.from(2024, 3, 1)
        val endDate = CalendarDay.from(2024, 3, 31)
        val decorator = CurrentDateDecorator(today, startDate, endDate)

        // Today is start - green takes precedence, grey should not apply
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 1)))
    }

    @Test
    fun `CurrentDateDecorator does NOT decorate when today equals period end`() {
        val today = CalendarDay.from(2024, 3, 31)
        val startDate = CalendarDay.from(2024, 3, 1)
        val endDate = CalendarDay.from(2024, 3, 31)
        val decorator = CurrentDateDecorator(today, startDate, endDate)

        // Today is end - red takes precedence, grey should not apply
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 31)))
    }

    @Test
    fun `CurrentDateDecorator decorates when today is regular day in period`() {
        val today = CalendarDay.from(2024, 3, 15)
        val startDate = CalendarDay.from(2024, 3, 1)
        val endDate = CalendarDay.from(2024, 3, 31)
        val decorator = CurrentDateDecorator(today, startDate, endDate)

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
    }

    @Test
    fun `CurrentDateDecorator does not decorate other days`() {
        val today = CalendarDay.from(2024, 3, 15)
        val startDate = CalendarDay.from(2024, 3, 1)
        val endDate = CalendarDay.from(2024, 3, 31)
        val decorator = CurrentDateDecorator(today, startDate, endDate)

        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 1)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 14)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 16)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 31)))
    }

    @Test
    fun `CurrentDateDecorator handles single-day period where today is start and end`() {
        val today = CalendarDay.from(2024, 3, 15)
        val singleDay = CalendarDay.from(2024, 3, 15)
        val decorator = CurrentDateDecorator(today, singleDay, singleDay)

        // Single-day period: today == start == end, grey should NOT apply (green/red takes it)
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
    }

    // ==================== DaysWithTripsDecorator ====================

    private fun daysWithTripsDecorator(
        dayMillis: Set<Long>,
        periodStart: CalendarDay? = null,
        periodEnd: CalendarDay? = null,
    ) = DaysWithTripsDecorator(dayMillis, periodStart, periodEnd)

    @Test
    fun `DaysWithTripsDecorator decorates dates with trips`() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.MARCH, 15, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis

        val decorator = daysWithTripsDecorator(setOf(millis))

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
    }

    @Test
    fun `DaysWithTripsDecorator does not decorate dates without trips`() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.MARCH, 15, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis

        val decorator = daysWithTripsDecorator(setOf(millis))

        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 14)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 16)))
    }

    @Test
    fun `DaysWithTripsDecorator matches when set stores start-of-day millis`() {
        // Trip at 14:30 — normalize to 00:00 for storage (as the app does)
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 0)
        cal.set(Calendar.MILLISECOND, 500)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis

        val decorator = daysWithTripsDecorator(setOf(millis))

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
    }

    @Test
    fun `DaysWithTripsDecorator handles empty set`() {
        val decorator = daysWithTripsDecorator(emptySet())

        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
        assertFalse(decorator.shouldDecorate(CalendarDay.today()))
    }

    @Test
    fun `DaysWithTripsDecorator handles multiple dates`() {
        val cal1 = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 20, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val decorator = daysWithTripsDecorator(setOf(cal1.timeInMillis, cal2.timeInMillis))

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 10)))
        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 20)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2024, 3, 15)))
    }

    @Test
    fun `DaysWithTripsDecorator handles leap year February 29`() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.FEBRUARY, 29, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis

        val decorator = daysWithTripsDecorator(setOf(millis))

        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 2, 29)))
        assertFalse(decorator.shouldDecorate(CalendarDay.from(2023, 2, 28))) // No Feb 29 in 2023
    }

    @Test
    fun `DaysWithTripsDecorator skips period start and end`() {
        val cal15 = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal16 = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 16, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = CalendarDay.from(2024, 3, 15)
        val end = CalendarDay.from(2024, 3, 20)
        val decorator = daysWithTripsDecorator(
            setOf(cal15.timeInMillis, cal16.timeInMillis),
            start,
            end,
        )
        assertFalse(decorator.shouldDecorate(start))
        assertTrue(decorator.shouldDecorate(CalendarDay.from(2024, 3, 16)))
        assertFalse(decorator.shouldDecorate(end))
    }
}
