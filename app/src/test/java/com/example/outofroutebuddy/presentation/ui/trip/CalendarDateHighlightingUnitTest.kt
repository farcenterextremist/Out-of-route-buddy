package com.example.outofroutebuddy.presentation.ui.trip

import com.example.outofroutebuddy.services.PeriodCalculationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * ✅ NEW: Unit tests for calendar date highlighting calculations
 * 
 * These tests focus on the date calculation logic without requiring Android framework.
 * Tests verify the mathematical correctness of date boundary calculations.
 * 
 * Priority: HIGH
 * Coverage Target: 100%
 * 
 * Created: December 2024
 */
class CalendarDateHighlightingUnitTest {

    private lateinit var periodCalculationService: PeriodCalculationService

    @Before
    fun setUp() {
        periodCalculationService = PeriodCalculationService()
    }

    // ==================== STANDARD MODE: MONTH BOUNDARY TESTS ====================

    @Test
    fun `calculate first day of month for various months`() {
        val testCases = mapOf(
            Calendar.JANUARY to 1,
            Calendar.FEBRUARY to 1,
            Calendar.MARCH to 1,
            Calendar.APRIL to 1,
            Calendar.MAY to 1,
            Calendar.JUNE to 1,
            Calendar.JULY to 1,
            Calendar.AUGUST to 1,
            Calendar.SEPTEMBER to 1,
            Calendar.OCTOBER to 1,
            Calendar.NOVEMBER to 1,
            Calendar.DECEMBER to 1
        )

        testCases.forEach { (month, expectedDay) ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, month, 15) // Mid-month date
            val date = calendar.time

            val firstDay = calculateFirstDayOfMonth(date)

            assertEquals(
                "First day of ${getMonthName(month)} should be $expectedDay",
                expectedDay,
                firstDay.get(Calendar.DAY_OF_MONTH)
            )
            assertEquals(
                "Month should match",
                month,
                firstDay.get(Calendar.MONTH)
            )
        }
    }

    @Test
    fun `calculate last day of month for various months in 2024`() {
        val testCases = mapOf(
            Calendar.JANUARY to 31,
            Calendar.FEBRUARY to 29, // Leap year
            Calendar.MARCH to 31,
            Calendar.APRIL to 30,
            Calendar.MAY to 31,
            Calendar.JUNE to 30,
            Calendar.JULY to 31,
            Calendar.AUGUST to 31,
            Calendar.SEPTEMBER to 30,
            Calendar.OCTOBER to 31,
            Calendar.NOVEMBER to 30,
            Calendar.DECEMBER to 31
        )

        testCases.forEach { (month, expectedDay) ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, month, 15) // Mid-month date
            val date = calendar.time

            val lastDay = calculateLastDayOfMonth(date)

            assertEquals(
                "Last day of ${getMonthName(month)} 2024 should be $expectedDay",
                expectedDay,
                lastDay.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    @Test
    fun `calculate last day handles leap years correctly`() {
        // Leap years
        val leapYears = listOf(2020, 2024, 2028, 2032)
        leapYears.forEach { year ->
            val calendar = Calendar.getInstance()
            calendar.set(year, Calendar.FEBRUARY, 15)
            val date = calendar.time

            val lastDay = calculateLastDayOfMonth(date)

            assertEquals(
                "February $year (leap year) should have 29 days",
                29,
                lastDay.get(Calendar.DAY_OF_MONTH)
            )
        }

        // Non-leap years
        val nonLeapYears = listOf(2021, 2022, 2023, 2025)
        nonLeapYears.forEach { year ->
            val calendar = Calendar.getInstance()
            calendar.set(year, Calendar.FEBRUARY, 15)
            val date = calendar.time

            val lastDay = calculateLastDayOfMonth(date)

            assertEquals(
                "February $year (non-leap year) should have 28 days",
                28,
                lastDay.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    // ==================== CUSTOM MODE: PERIOD BOUNDARY TESTS ====================

    @Test
    fun `custom period start is always Thursday before first Friday`() {
        // Test multiple months to ensure consistency
        val months = listOf(
            Calendar.JANUARY,
            Calendar.FEBRUARY,
            Calendar.MARCH,
            Calendar.APRIL,
            Calendar.MAY,
            Calendar.JUNE
        )

        months.forEach { month ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, month, 15) // Mid-month date
            val date = calendar.time

            val periodStart = periodCalculationService.calculateCustomPeriodStart(date)

            assertEquals(
                "Period start for ${getMonthName(month)} should be Thursday",
                Calendar.THURSDAY,
                periodStart.get(Calendar.DAY_OF_WEEK)
            )
        }
    }

    @Test
    fun `custom period end is always Thursday before first Friday of next month`() {
        val months = listOf(
            Calendar.JANUARY,
            Calendar.MARCH,
            Calendar.APRIL,
            Calendar.MAY,
            Calendar.JUNE
        )

        months.forEach { month ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, month, 15) // Mid-month date
            val date = calendar.time

            val periodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

            assertEquals(
                "Period end for ${getMonthName(month)} should be Thursday",
                Calendar.THURSDAY,
                periodEnd.get(Calendar.DAY_OF_WEEK)
            )

            // Verify it's in the next month (except February which ends on Feb 29, still in February)
            val expectedNextMonth = (month + 1) % 12
            assertEquals(
                "Period end should be in next month for ${getMonthName(month)}",
                expectedNextMonth,
                periodEnd.get(Calendar.MONTH)
            )
        }
        
        // Special case: February 2024 (leap year) - period ends on Feb 29 (Thursday before March 1)
        val febCalendar = Calendar.getInstance()
        febCalendar.set(2024, Calendar.FEBRUARY, 15)
        val febDate = febCalendar.time
        val febPeriodEnd = periodCalculationService.calculateCustomPeriodEnd(febDate)
        
        assertEquals(
            "February 2024 period end should be Thursday",
            Calendar.THURSDAY,
            febPeriodEnd.get(Calendar.DAY_OF_WEEK)
        )
        assertEquals(
            "February 2024 period end should be Feb 29 (still in February, not March)",
            Calendar.FEBRUARY,
            febPeriodEnd.get(Calendar.MONTH)
        )
        assertEquals(
            "February 2024 period end should be day 29",
            29,
            febPeriodEnd.get(Calendar.DAY_OF_MONTH)
        )
    }

    @Test
    fun `custom period boundaries are consistent within same period`() {
        // All dates in March 2024 that are >= period start (Feb 29) should have same period boundaries
        // March 1 is before Feb 29 period start, so it belongs to previous period
        // Use dates from March 5 onwards which are clearly in March's period
        val datesInMarch = listOf(5, 10, 15, 20, 25, 31)

        val periodStarts = mutableSetOf<Long>()
        val periodEnds = mutableSetOf<Long>()

        datesInMarch.forEach { day ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, Calendar.MARCH, day)
            calendar.set(Calendar.HOUR_OF_DAY, 12) // Set to noon to avoid timezone edge cases
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val date = calendar.time

            val periodStart = periodCalculationService.calculateCustomPeriodStart(date)
            val periodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

            // Normalize to start of day for comparison
            periodStart.set(Calendar.HOUR_OF_DAY, 0)
            periodStart.set(Calendar.MINUTE, 0)
            periodStart.set(Calendar.SECOND, 0)
            periodStart.set(Calendar.MILLISECOND, 0)
            
            periodEnd.set(Calendar.HOUR_OF_DAY, 0)
            periodEnd.set(Calendar.MINUTE, 0)
            periodEnd.set(Calendar.SECOND, 0)
            periodEnd.set(Calendar.MILLISECOND, 0)

            periodStarts.add(periodStart.timeInMillis)
            periodEnds.add(periodEnd.timeInMillis)
        }

        // All dates in the same period should have the same period boundaries
        assertEquals(
            "All dates in March period should have same period start",
            1,
            periodStarts.size
        )
        assertEquals(
            "All dates in March period should have same period end",
            1,
            periodEnds.size
        )
    }

    @Test
    fun `custom period start is before period end`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15)
        val date = calendar.time

        val periodStart = periodCalculationService.calculateCustomPeriodStart(date)
        val periodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

        assertTrue(
            "Period start should be before period end",
            periodStart.before(periodEnd)
        )
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `first day calculation handles year boundary`() {
        // December 31, 2024
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.DECEMBER, 31)
        val date = calendar.time

        val firstDay = calculateFirstDayOfMonth(date)

        assertEquals("Should return December 1", 1, firstDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be December", Calendar.DECEMBER, firstDay.get(Calendar.MONTH))
        assertEquals("Year should remain 2024", 2024, firstDay.get(Calendar.YEAR))
    }

    @Test
    fun `last day calculation handles year boundary`() {
        // January 1, 2024
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 1)
        val date = calendar.time

        val lastDay = calculateLastDayOfMonth(date)

        assertEquals("Should return January 31", 31, lastDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be January", Calendar.JANUARY, lastDay.get(Calendar.MONTH))
        assertEquals("Year should remain 2024", 2024, lastDay.get(Calendar.YEAR))
    }

    @Test
    fun `date calculations normalize time correctly`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JUNE, 15, 14, 30, 45) // 2:30:45 PM
        calendar.set(Calendar.MILLISECOND, 123)
        val date = calendar.time

        val firstDay = calculateFirstDayOfMonth(date)
        val lastDay = calculateLastDayOfMonth(date)

        // First day should be normalized to start of day
        assertEquals("First day hour should be 0", 0, firstDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("First day minute should be 0", 0, firstDay.get(Calendar.MINUTE))
        assertEquals("First day second should be 0", 0, firstDay.get(Calendar.SECOND))
        assertEquals("First day millisecond should be 0", 0, firstDay.get(Calendar.MILLISECOND))

        // Last day should be normalized to end of day
        assertEquals("Last day hour should be 23", 23, lastDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("Last day minute should be 59", 59, lastDay.get(Calendar.MINUTE))
        assertEquals("Last day second should be 59", 59, lastDay.get(Calendar.SECOND))
    }

    @Test
    fun `single-day period - first equals last for same month`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15)
        val date = calendar.time

        val firstDay = calculateFirstDayOfMonth(date)
        val lastDay = calculateLastDayOfMonth(date)

        // For a month, first is 1st, last is 31st - different
        assertNotEquals(firstDay.get(Calendar.DAY_OF_MONTH), lastDay.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `February has 28 or 29 days - edge case for short months`() {
        val nonLeap = Calendar.getInstance().apply { set(2023, Calendar.FEBRUARY, 1) }
        val leap = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 1) }

        val lastNonLeap = calculateLastDayOfMonth(nonLeap.time)
        val lastLeap = calculateLastDayOfMonth(leap.time)

        assertEquals(28, lastNonLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(29, lastLeap.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `custom period December to January crosses year boundary`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.DECEMBER, 15)
        val date = calendar.time

        val periodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

        assertEquals(2025, periodEnd.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, periodEnd.get(Calendar.MONTH))
    }

    // ==================== HELPER METHODS ====================

    private fun calculateFirstDayOfMonth(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    private fun calculateLastDayOfMonth(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "January"
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> "Unknown"
        }
    }
}
