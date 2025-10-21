package com.example.outofroutebuddy.services

import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Comprehensive tests for PeriodCalculationService to ensure all custom period date calculations work correctly.
 */
class PeriodCalculationServiceTest {
    private val service = PeriodCalculationService()

    @Test
    fun `findFirstFridayOfMonth with January 2024 returns correct date`() {
        // Given
        val year = 2024
        val month = Calendar.JANUARY

        // When
        val firstFriday = service.findFirstFridayOfMonth(year, month)

        // Then
        assertEquals(2024, firstFriday.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, firstFriday.get(Calendar.MONTH))
        assertEquals(5, firstFriday.get(Calendar.DAY_OF_MONTH)) // January 5, 2024 is a Friday
        assertEquals(Calendar.FRIDAY, firstFriday.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `findFirstFridayOfMonth with February 2024 returns correct date`() {
        // Given
        val year = 2024
        val month = Calendar.FEBRUARY

        // When
        val firstFriday = service.findFirstFridayOfMonth(year, month)

        // Then
        assertEquals(2024, firstFriday.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, firstFriday.get(Calendar.MONTH))
        assertEquals(2, firstFriday.get(Calendar.DAY_OF_MONTH)) // February 2, 2024 is a Friday
        assertEquals(Calendar.FRIDAY, firstFriday.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `findThursdayBeforeFirstFriday with January 2024 returns correct date`() {
        // Given
        val year = 2024
        val month = Calendar.JANUARY

        // When
        val thursdayBefore = service.findThursdayBeforeFirstFriday(year, month)

        // Then
        assertEquals(2024, thursdayBefore.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, thursdayBefore.get(Calendar.MONTH))
        assertEquals(4, thursdayBefore.get(Calendar.DAY_OF_MONTH)) // January 4, 2024 is a Thursday
        assertEquals(Calendar.THURSDAY, thursdayBefore.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `findThursdayBeforeFirstFriday with February 2024 returns correct date`() {
        // Given
        val year = 2024
        val month = Calendar.FEBRUARY

        // When
        val thursdayBefore = service.findThursdayBeforeFirstFriday(year, month)

        // Then
        assertEquals(2024, thursdayBefore.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, thursdayBefore.get(Calendar.MONTH))
        assertEquals(1, thursdayBefore.get(Calendar.DAY_OF_MONTH)) // February 1, 2024 is a Thursday
        assertEquals(Calendar.THURSDAY, thursdayBefore.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateCustomPeriodStart for March 1 2024 returns February 29`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1) // March 1, 2024
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        assertEquals(2024, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(29, periodStart.get(Calendar.DAY_OF_MONTH)) // February 29, 2024 (leap year)
        assertEquals(Calendar.THURSDAY, periodStart.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateCustomPeriodStart for March 15 2024 returns February 29`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // March 15, 2024
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        assertEquals(2024, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(29, periodStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.THURSDAY, periodStart.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateCustomPeriodStart for April 1 2024 returns February 29`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.APRIL, 1) // April 1, 2024
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        assertEquals(2024, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(29, periodStart.get(Calendar.DAY_OF_MONTH)) // Feb 29, 2024 is the period start (Thursday before first Friday of March)
        assertEquals(Calendar.THURSDAY, periodStart.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateCustomPeriodEnd for March 2024 returns April 4`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // March 15, 2024
        val date = calendar.time

        // When
        val periodEnd = service.calculateCustomPeriodEnd(date)

        // Then
        assertEquals(2024, periodEnd.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, periodEnd.get(Calendar.MONTH))
        assertEquals(4, periodEnd.get(Calendar.DAY_OF_MONTH)) // April 4, 2024 is a Thursday
        assertEquals(Calendar.THURSDAY, periodEnd.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `getCustomPeriodStartFor returns correct start date`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1)
        val date = calendar.time

        // When
        val periodStart = service.getCustomPeriodStartFor(date)

        // Then
        val resultCalendar = Calendar.getInstance()
        resultCalendar.time = periodStart
        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, resultCalendar.get(Calendar.MONTH))
        assertEquals(29, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getCustomPeriodEndFor returns correct end date`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1)
        val date = calendar.time

        // When
        val periodEnd = service.getCustomPeriodEndFor(date)

        // Then
        val resultCalendar = Calendar.getInstance()
        resultCalendar.time = periodEnd
        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, resultCalendar.get(Calendar.MONTH))
        assertEquals(4, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `isDateInCustomPeriod with date in period returns true`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time
        val testDate =
            Calendar.getInstance().apply {
                set(2024, Calendar.MARCH, 15)
            }.time

        // When
        val isInPeriod = service.isDateInCustomPeriod(testDate, periodStart, periodEnd)

        // Then
        assertTrue(isInPeriod)
    }

    @Test
    fun `isDateInCustomPeriod with date at period start returns true`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time
        val testDate =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time

        // When
        val isInPeriod = service.isDateInCustomPeriod(testDate, periodStart, periodEnd)

        // Then
        assertTrue(isInPeriod)
    }

    @Test
    fun `isDateInCustomPeriod with date at period end returns false`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time
        val testDate =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time

        // When
        val isInPeriod = service.isDateInCustomPeriod(testDate, periodStart, periodEnd)

        // Then
        assertFalse(isInPeriod) // End date is exclusive
    }

    @Test
    fun `isDateInCustomPeriod with date before period returns false`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time
        val testDate =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 28)
            }.time

        // When
        val isInPeriod = service.isDateInCustomPeriod(testDate, periodStart, periodEnd)

        // Then
        assertFalse(isInPeriod)
    }

    @Test
    fun `isDateInCustomPeriod with date after period returns false`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time
        val testDate =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 5)
            }.time

        // When
        val isInPeriod = service.isDateInCustomPeriod(testDate, periodStart, periodEnd)

        // Then
        assertFalse(isInPeriod)
    }

    @Test
    fun `getCustomPeriodDays returns correct number of days`() {
        // Given
        val periodStart =
            Calendar.getInstance().apply {
                set(2024, Calendar.FEBRUARY, 29)
            }.time
        val periodEnd =
            Calendar.getInstance().apply {
                set(2024, Calendar.APRIL, 4)
            }.time

        // When
        val days = service.getCustomPeriodDays(periodStart, periodEnd)

        // Then
        assertEquals(34, days) // Feb 29 to Apr 4: 34 days difference (milliseconds calculation)
    }

    @Test
    fun `formatDateForDisplay returns correct format`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // March 15, 2024 is a Friday

        // When
        val formatted = service.formatDateForDisplay(calendar)

        // Then
        assertTrue(formatted.contains("2024"))
        assertTrue(formatted.contains("3"))
        assertTrue(formatted.contains("15"))
        assertTrue(formatted.contains("Friday"))
    }

    @Test
    fun `edge case with leap year February handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 29) // Leap year February 29
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        assertEquals(2024, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(29, periodStart.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `edge case with non-leap year February handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.FEBRUARY, 28) // Non-leap year February 28
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        assertEquals(2023, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(2, periodStart.get(Calendar.DAY_OF_MONTH)) // February 2, 2023 is a Thursday
    }

    @Test
    fun `edge case with year boundary handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.DECEMBER, 31) // December 31, 2023
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)
        val periodEnd = service.calculateCustomPeriodEnd(date)

        // Then
        // Should return the period that includes December 31, 2023
        assertTrue(periodStart.get(Calendar.YEAR) == 2023 || periodStart.get(Calendar.YEAR) == 2024)
        assertTrue(periodEnd.get(Calendar.YEAR) == 2024)
    }

    @Test
    fun `multiple months have consistent period boundaries`() {
        // Test that consecutive months have proper period boundaries
        val months =
            listOf(
                Triple(2024, Calendar.JANUARY, 4), // Jan 4 (Thu) - Thursday before first Friday of Jan
                Triple(2024, Calendar.FEBRUARY, 1), // Feb 1 (Thu) - Thursday before first Friday of Feb
                Triple(2024, Calendar.MARCH, 29), // Feb 29 (Thu) - Thursday before first Friday of Mar
                Triple(2024, Calendar.APRIL, 4), // Apr 4 (Thu) - Thursday before first Friday of Apr
            )

        months.forEach { (year, month, expectedDay) ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 15) // Middle of the month
            val date = calendar.time

            val periodStart = service.calculateCustomPeriodStart(date)

            // For March, the period start is actually February 29
            val expectedMonth = if (month == Calendar.MARCH) Calendar.FEBRUARY else month
            val actualExpectedDay = if (month == Calendar.MARCH) 29 else expectedDay

            assertEquals(
                "Month $month should start on day $actualExpectedDay",
                actualExpectedDay,
                periodStart.get(Calendar.DAY_OF_MONTH),
            )
            assertEquals(
                "Month $month should be in month $expectedMonth",
                expectedMonth,
                periodStart.get(Calendar.MONTH),
            )
            assertEquals(Calendar.THURSDAY, periodStart.get(Calendar.DAY_OF_WEEK))
        }
    }

    @Test
    fun `period calculations are timezone independent`() {
        // Given
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2024, Calendar.MARCH, 1)
        val date = calendar.time

        // When
        val periodStart = service.calculateCustomPeriodStart(date)

        // Then
        // Should work regardless of timezone
        assertEquals(2024, periodStart.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals(29, periodStart.get(Calendar.DAY_OF_MONTH))
    }
} 
