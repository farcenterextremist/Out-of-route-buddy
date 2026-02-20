package com.example.outofroutebuddy.presentation.ui.trip

import com.example.outofroutebuddy.services.PeriodCalculationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * ✅ NEW: Unit tests for calendar date highlighting functionality
 * 
 * These tests verify:
 * - First and last day of month calculations for STANDARD mode
 * - Custom period boundary calculations for CUSTOM mode
 * - Date highlighting logic correctness
 * 
 * Priority: HIGH
 * Coverage Target: 95%
 * 
 * Created: December 2024
 */
class TripInputFragmentCalendarHighlightingTest {

    private lateinit var periodCalculationService: PeriodCalculationService

    @Before
    fun setUp() {
        periodCalculationService = PeriodCalculationService()
    }

    // ==================== STANDARD MODE: FIRST/LAST DAY OF MONTH TESTS ====================

    @Test
    fun `getFirstDayOfMonth with January 2024 returns January 1`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15) // Mid-month date
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val firstDay = fragment.getFirstDayOfMonth(date)

        // Then
        assertEquals("Year should be 2024", 2024, firstDay.get(Calendar.YEAR))
        assertEquals("Month should be January", Calendar.JANUARY, firstDay.get(Calendar.MONTH))
        assertEquals("Day should be 1", 1, firstDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Hour should be 0", 0, firstDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 0", 0, firstDay.get(Calendar.MINUTE))
        assertEquals("Second should be 0", 0, firstDay.get(Calendar.SECOND))
        assertEquals("Millisecond should be 0", 0, firstDay.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getLastDayOfMonth with January 2024 returns January 31`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15) // Mid-month date
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val lastDay = fragment.getLastDayOfMonth(date)

        // Then
        assertEquals("Year should be 2024", 2024, lastDay.get(Calendar.YEAR))
        assertEquals("Month should be January", Calendar.JANUARY, lastDay.get(Calendar.MONTH))
        assertEquals("Day should be 31", 31, lastDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Hour should be 23", 23, lastDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 59", 59, lastDay.get(Calendar.MINUTE))
    }

    @Test
    fun `getLastDayOfMonth with February 2024 returns February 29 (leap year)`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 15) // Mid-month date
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val lastDay = fragment.getLastDayOfMonth(date)

        // Then
        assertEquals("Year should be 2024", 2024, lastDay.get(Calendar.YEAR))
        assertEquals("Month should be February", Calendar.FEBRUARY, lastDay.get(Calendar.MONTH))
        assertEquals("Day should be 29 (leap year)", 29, lastDay.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getLastDayOfMonth with February 2023 returns February 28 (non-leap year)`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.FEBRUARY, 15) // Mid-month date
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val lastDay = fragment.getLastDayOfMonth(date)

        // Then
        assertEquals("Year should be 2023", 2023, lastDay.get(Calendar.YEAR))
        assertEquals("Month should be February", Calendar.FEBRUARY, lastDay.get(Calendar.MONTH))
        assertEquals("Day should be 28 (non-leap year)", 28, lastDay.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getFirstAndLastDayOfMonth returns correct pair`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // Mid-month date
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val (firstDay, lastDay) = fragment.getFirstAndLastDayOfMonth(date)

        // Then
        assertEquals("First day should be March 1", 1, firstDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Last day should be March 31", 31, lastDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Both should be in March", Calendar.MARCH, firstDay.get(Calendar.MONTH))
        assertEquals("Both should be in March", Calendar.MARCH, lastDay.get(Calendar.MONTH))
        assertTrue("First day should be before last day", firstDay.before(lastDay))
    }

    @Test
    fun `getFirstDayOfMonth handles month boundaries correctly`() {
        // Given - Test with last day of month
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 31) // Last day of January
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val firstDay = fragment.getFirstDayOfMonth(date)

        // Then
        assertEquals("Should return first day of same month", 1, firstDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be January", Calendar.JANUARY, firstDay.get(Calendar.MONTH))
    }

    @Test
    fun `getLastDayOfMonth handles different month lengths`() {
        // Test months with different lengths
        val testCases = listOf(
            Triple(2024, Calendar.APRIL, 30), // April has 30 days
            Triple(2024, Calendar.MAY, 31),   // May has 31 days
            Triple(2024, Calendar.JUNE, 30),  // June has 30 days
            Triple(2024, Calendar.SEPTEMBER, 30), // September has 30 days
            Triple(2024, Calendar.NOVEMBER, 30)    // November has 30 days
        )

        testCases.forEach { (year, month, expectedLastDay) ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 15) // Mid-month date
            val date = calendar.time

            val fragment = createTestFragment()
            val lastDay = fragment.getLastDayOfMonth(date)

            assertEquals(
                "Last day of ${getMonthName(month)} $year should be $expectedLastDay",
                expectedLastDay,
                lastDay.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    // ==================== CUSTOM MODE: PERIOD BOUNDARY TESTS ====================

    @Test
    fun `custom period start calculation matches PeriodCalculationService`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // Mid-month date
        val date = calendar.time

        // When - Use PeriodCalculationService directly
        val customPeriodStart = periodCalculationService.calculateCustomPeriodStart(date)

        // Then - Should be Thursday before first Friday of March (Feb 29, 2024)
        assertEquals("Year should be 2024", 2024, customPeriodStart.get(Calendar.YEAR))
        assertEquals("Month should be February", Calendar.FEBRUARY, customPeriodStart.get(Calendar.MONTH))
        assertEquals("Day should be 29", 29, customPeriodStart.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be Thursday", Calendar.THURSDAY, customPeriodStart.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `custom period end calculation matches PeriodCalculationService`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 15) // Mid-month date
        val date = calendar.time

        // When - Use PeriodCalculationService directly
        val customPeriodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

        // Then - Should be Thursday before first Friday of April (Apr 4, 2024)
        assertEquals("Year should be 2024", 2024, customPeriodEnd.get(Calendar.YEAR))
        assertEquals("Month should be April", Calendar.APRIL, customPeriodEnd.get(Calendar.MONTH))
        assertEquals("Day should be 4", 4, customPeriodEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be Thursday", Calendar.THURSDAY, customPeriodEnd.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `custom period boundaries are consistent across month`() {
        // Given - Test multiple dates in the same month
        val datesInMarch = listOf(1, 10, 15, 20, 31)

        datesInMarch.forEach { day ->
            val calendar = Calendar.getInstance()
            calendar.set(2024, Calendar.MARCH, day)
            val date = calendar.time

            val periodStart = periodCalculationService.calculateCustomPeriodStart(date)
            val periodEnd = periodCalculationService.calculateCustomPeriodEnd(date)

            // All dates in March should have the same period boundaries
            assertEquals(
                "Period start for March $day should be Feb 29",
                29,
                periodStart.get(Calendar.DAY_OF_MONTH)
            )
            assertEquals(
                "Period end for March $day should be Apr 4",
                4,
                periodEnd.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `getFirstDayOfMonth handles year boundary`() {
        // Given - December 31
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.DECEMBER, 31)
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val firstDay = fragment.getFirstDayOfMonth(date)

        // Then
        assertEquals("Should return December 1", 1, firstDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be December", Calendar.DECEMBER, firstDay.get(Calendar.MONTH))
        assertEquals("Year should remain 2024", 2024, firstDay.get(Calendar.YEAR))
    }

    @Test
    fun `getLastDayOfMonth handles year boundary`() {
        // Given - January 1
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 1)
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val lastDay = fragment.getLastDayOfMonth(date)

        // Then
        assertEquals("Should return January 31", 31, lastDay.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be January", Calendar.JANUARY, lastDay.get(Calendar.MONTH))
        assertEquals("Year should remain 2024", 2024, lastDay.get(Calendar.YEAR))
    }

    @Test
    fun `date calculations handle timezone correctly`() {
        // Given - Date with specific time
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JUNE, 15, 14, 30, 45) // 2:30:45 PM
        calendar.set(Calendar.MILLISECOND, 123)
        val date = calendar.time

        // When
        val fragment = createTestFragment()
        val firstDay = fragment.getFirstDayOfMonth(date)
        val lastDay = fragment.getLastDayOfMonth(date)

        // Then - Time should be normalized
        assertEquals("First day hour should be 0", 0, firstDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("First day minute should be 0", 0, firstDay.get(Calendar.MINUTE))
        assertEquals("First day second should be 0", 0, firstDay.get(Calendar.SECOND))
        assertEquals("First day millisecond should be 0", 0, firstDay.get(Calendar.MILLISECOND))

        assertEquals("Last day hour should be 23", 23, lastDay.get(Calendar.HOUR_OF_DAY))
        assertEquals("Last day minute should be 59", 59, lastDay.get(Calendar.MINUTE))
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a test fragment instance using reflection to access private methods
     * Note: In a real implementation, these methods might need to be made package-private
     * or we might need to use a different testing approach
     */
    private fun createTestFragment(): TestTripInputFragment {
        return TestTripInputFragment()
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

    /**
     * Test helper class that accesses private methods via reflection
     */
    private class TestTripInputFragment {
        fun getFirstDayOfMonth(date: Date): Calendar {
            // Access via reflection since method is private
            val method = TripInputFragment::class.java.getDeclaredMethod(
                "getFirstDayOfMonth",
                Date::class.java
            )
            method.isAccessible = true
            // Create a fragment instance for testing (won't be fully initialized, but method should work)
            val fragment = TripInputFragment()
            return method.invoke(fragment, date) as Calendar
        }

        fun getLastDayOfMonth(date: Date): Calendar {
            val method = TripInputFragment::class.java.getDeclaredMethod(
                "getLastDayOfMonth",
                Date::class.java
            )
            method.isAccessible = true
            val fragment = TripInputFragment()
            return method.invoke(fragment, date) as Calendar
        }

        fun getFirstAndLastDayOfMonth(date: Date): Pair<Calendar, Calendar> {
            val method = TripInputFragment::class.java.getDeclaredMethod(
                "getFirstAndLastDayOfMonth",
                Date::class.java
            )
            method.isAccessible = true
            val fragment = TripInputFragment()
            // The method returns androidx.core.util.Pair, convert to Kotlin Pair
            val result = method.invoke(fragment, date) as androidx.core.util.Pair<Calendar, Calendar>
            return Pair(result.first, result.second)
        }
    }
}
