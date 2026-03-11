package com.example.outofroutebuddy.services

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Service responsible for custom period date calculations and business logic.
 * This service is pure business logic and can be easily unit tested.
 * 
 * Custom period definition:
 * - Each custom period starts on the Thursday before the first Friday of the month.
 * - The period includes all days from that Thursday (inclusive) up to, but not including, the next such Thursday.
 * - Example for 2024:
 *   - January: Starts Jan 4 (Thu, before Jan 5), ends Feb 1 (Thu, before Feb 2)
 *   - February: Starts Feb 1 (Thu, before Feb 2), ends Feb 29 (Thu, before Mar 1)
 *   - March: Starts Feb 29 (Thu, before Mar 1), ends Apr 4 (Thu, before Apr 5)
 *   - July: Starts Jul 4 (Thu, before Jul 5), ends Aug 1 (Thu, before Aug 2)
 */
class PeriodCalculationService {
    private val logger = LoggerFactory.getLogger(PeriodCalculationService::class.java)
    
    /**
     * Find the first Friday of a given month
     */
    fun findFirstFridayOfMonth(year: Int, month: Int): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(year, month, 1) // Set to first day of month
        
        // Find the first Friday
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar
    }
    
    /**
     * Find the Thursday before the first Friday of a given month
     */
    fun findThursdayBeforeFirstFriday(year: Int, month: Int): Calendar {
        val firstFriday = findFirstFridayOfMonth(year, month)
        val thursdayBefore = firstFriday.clone() as Calendar
        thursdayBefore.add(Calendar.DAY_OF_MONTH, -1)
        return thursdayBefore
    }
    
    /**
     * Calculate the start date of the custom period for a given date
     *
     * For any given date, find which month's custom period it belongs to.
     * A date belongs to the custom period that starts on or before that date, and before the next such period start.
     *
     * Example: For March 1, 2024, the period start is Feb 29, 2024 (Thursday before first Friday of March).
     */
    fun calculateCustomPeriodStart(forDate: Date): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.time = forDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        // Find the Thursday before the first Friday of the current month
        val currentPeriodStart = findThursdayBeforeFirstFriday(year, month)
        
        // Find the Thursday before the first Friday of the next month
        val nextMonthCal = Calendar.getInstance(TimeZone.getDefault())
        nextMonthCal.set(year, month, 1)
        nextMonthCal.add(Calendar.MONTH, 1)
        val nextPeriodStart = findThursdayBeforeFirstFriday(nextMonthCal.get(Calendar.YEAR), nextMonthCal.get(Calendar.MONTH))
        
        // Find the Thursday before the first Friday of the previous month
        val prevMonthCal = Calendar.getInstance(TimeZone.getDefault())
        prevMonthCal.set(year, month, 1)
        prevMonthCal.add(Calendar.MONTH, -1)
        val prevPeriodStart = findThursdayBeforeFirstFriday(prevMonthCal.get(Calendar.YEAR), prevMonthCal.get(Calendar.MONTH))

        // Clear time fields for consistent comparison
        val inputCal = Calendar.getInstance(TimeZone.getDefault())
        inputCal.time = forDate
        inputCal.set(Calendar.HOUR_OF_DAY, 0)
        inputCal.set(Calendar.MINUTE, 0)
        inputCal.set(Calendar.SECOND, 0)
        inputCal.set(Calendar.MILLISECOND, 0)
        
        currentPeriodStart.set(Calendar.HOUR_OF_DAY, 0)
        currentPeriodStart.set(Calendar.MINUTE, 0)
        currentPeriodStart.set(Calendar.SECOND, 0)
        currentPeriodStart.set(Calendar.MILLISECOND, 0)
        
        nextPeriodStart.set(Calendar.HOUR_OF_DAY, 0)
        nextPeriodStart.set(Calendar.MINUTE, 0)
        nextPeriodStart.set(Calendar.SECOND, 0)
        nextPeriodStart.set(Calendar.MILLISECOND, 0)
        
        prevPeriodStart.set(Calendar.HOUR_OF_DAY, 0)
        prevPeriodStart.set(Calendar.MINUTE, 0)
        prevPeriodStart.set(Calendar.SECOND, 0)
        prevPeriodStart.set(Calendar.MILLISECOND, 0)

        // Determine which period the date belongs to
        return when {
            inputCal.compareTo(nextPeriodStart) >= 0 -> {
                // Date is >= next period start, so it belongs to the next period
                nextPeriodStart
            }
            inputCal.compareTo(currentPeriodStart) >= 0 -> {
                // Date is >= current period start but < next period start, so it belongs to current period
                currentPeriodStart
            }
            else -> {
                // Date is < current period start, so it belongs to previous period
                prevPeriodStart
            }
        }
    }
    
    /**
     * Calculate the end date of the custom period for a given date.
     * The end of the period containing forDate is the start of the next period
     * (Thursday before first Friday of some later month). We derive it from the
     * period start so that when the calendar month flips (e.g. Feb → Mar), we
     * still show the current period (e.g. Feb 5–Mar 5) instead of jumping to the
     * next period's end (e.g. Apr). If "month after period start" yields a
     * boundary that is not after period start (e.g. Feb 29 in 2024 for both Feb
     * and Mar), we use the following month.
     */
    fun calculateCustomPeriodEnd(forDate: Date): Calendar {
        val periodStart = calculateCustomPeriodStart(forDate)
        val year = periodStart.get(Calendar.YEAR)
        val month = periodStart.get(Calendar.MONTH)
        var nextMonthCal = Calendar.getInstance(TimeZone.getDefault())
        nextMonthCal.set(year, month, 1)
        nextMonthCal.add(Calendar.MONTH, 1)
        var nextYear = nextMonthCal.get(Calendar.YEAR)
        var nextMonth = nextMonthCal.get(Calendar.MONTH)
        var endCal = findThursdayBeforeFirstFriday(nextYear, nextMonth)
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        periodStart.set(Calendar.HOUR_OF_DAY, 0)
        periodStart.set(Calendar.MINUTE, 0)
        periodStart.set(Calendar.SECOND, 0)
        periodStart.set(Calendar.MILLISECOND, 0)
        // If this "end" is not after period start (e.g. Feb 29 for both Feb and Mar in 2024), use next month
        if (endCal.compareTo(periodStart) <= 0) {
            nextMonthCal = Calendar.getInstance(TimeZone.getDefault())
            nextMonthCal.set(year, month, 1)
            nextMonthCal.add(Calendar.MONTH, 2)
            nextYear = nextMonthCal.get(Calendar.YEAR)
            nextMonth = nextMonthCal.get(Calendar.MONTH)
            endCal = findThursdayBeforeFirstFriday(nextYear, nextMonth)
            endCal.set(Calendar.HOUR_OF_DAY, 0)
            endCal.set(Calendar.MINUTE, 0)
            endCal.set(Calendar.SECOND, 0)
            endCal.set(Calendar.MILLISECOND, 0)
        }
        // End of period is inclusive (last day of period at end-of-day) so "Current Period" includes the end Thursday
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        return endCal
    }
    
    /**
     * Get the custom period start date for the current date
     */
    fun getCurrentCustomPeriodStart(): Date {
        val currentCal = getCurrentCalendar()
        return calculateCustomPeriodStart(currentCal.time).time
    }
    
    /**
     * Get the custom period end date for the current date
     */
    fun getCurrentCustomPeriodEnd(): Date {
        val currentCal = getCurrentCalendar()
        return calculateCustomPeriodEnd(currentCal.time).time
    }
    
    /**
     * Get the custom period start date for a specific date (for testing and external use)
     * @param date The date to calculate the custom period start for
     * @return The start date of the custom period containing the given date
     */
    fun getCustomPeriodStartFor(date: Date): Date {
        return calculateCustomPeriodStart(date).time
    }
    
    /**
     * Get the custom period end date for a specific date (for testing and external use)
     * @param date The date to calculate the custom period end for
     * @return The end date of the custom period containing the given date
     */
    fun getCustomPeriodEndFor(date: Date): Date {
        return calculateCustomPeriodEnd(date).time
    }
    
    /**
     * Check if a date falls within a custom period
     */
    fun isDateInCustomPeriod(date: Date, periodStart: Date, periodEnd: Date): Boolean {
        val dateCal = Calendar.getInstance(TimeZone.getDefault())
        dateCal.time = date
        dateCal.set(Calendar.HOUR_OF_DAY, 0)
        dateCal.set(Calendar.MINUTE, 0)
        dateCal.set(Calendar.SECOND, 0)
        dateCal.set(Calendar.MILLISECOND, 0)
        
        val startCal = Calendar.getInstance(TimeZone.getDefault())
        startCal.time = periodStart
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        
        val endCal = Calendar.getInstance(TimeZone.getDefault())
        endCal.time = periodEnd
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        
        return dateCal.compareTo(startCal) >= 0 && dateCal.compareTo(endCal) <= 0
    }
    
    /**
     * Get the number of days in a custom period
     */
    fun getCustomPeriodDays(periodStart: Date, periodEnd: Date): Int {
        val startCal = Calendar.getInstance(TimeZone.getDefault())
        startCal.time = periodStart
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        
        val endCal = Calendar.getInstance(TimeZone.getDefault())
        endCal.time = periodEnd
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        
        val diffInMillis = endCal.timeInMillis - startCal.timeInMillis
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
    
    /**
     * Get current calendar instance
     */
    private fun getCurrentCalendar(): Calendar {
        return Calendar.getInstance(TimeZone.getDefault())
    }
    
    /**
     * Format a calendar date for display
     */
    fun formatDateForDisplay(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK))
        
        return "$year-$month-$day ($dayOfWeek)"
    }
    
    /**
     * Get day of week name
     */
    private fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
} 
