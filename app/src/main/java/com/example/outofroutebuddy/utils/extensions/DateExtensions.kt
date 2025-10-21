package com.example.outofroutebuddy.utils.extensions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Date utility extensions
 * 
 * These extensions provide convenient methods for date operations
 * and formatting throughout the application.
 */

/**
 * Format date as string with default pattern
 */
fun Date.format(pattern: String = "MM/dd/yyyy HH:mm"): String {
    val formatter = SimpleDateFormat(pattern, Locale.US)
    return formatter.format(this)
}

/**
 * Format date as short date string
 */
fun Date.formatShort(): String {
    return format("MM/dd/yyyy")
}

/**
 * Format date as time string
 */
fun Date.formatTime(): String {
    return format("HH:mm")
}

/**
 * Format date as date and time string
 */
fun Date.formatDateTime(): String {
    return format("MM/dd/yyyy HH:mm")
}

/**
 * Get relative time string (e.g., "2 minutes ago")
 */
fun Date.getRelativeTimeString(): String {
    val now = Date()
    val diffInMillis = now.time - this.time
    val diffInMinutes = diffInMillis / (1000 * 60)
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24
    
    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "$diffInMinutes minute${if (diffInMinutes != 1L) "s" else ""} ago"
        diffInHours < 24 -> "$diffInHours hour${if (diffInHours != 1L) "s" else ""} ago"
        diffInDays < 7 -> "$diffInDays day${if (diffInDays != 1L) "s" else ""} ago"
        else -> formatShort()
    }
}

/**
 * Check if date is today
 */
fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val thisDate = Calendar.getInstance().apply { time = this@isToday }
    
    return today.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
}

/**
 * Check if date is yesterday
 */
fun Date.isYesterday(): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val thisDate = Calendar.getInstance().apply { time = this@isYesterday }
    
    return yesterday.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
           yesterday.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
}

/**
 * Get start of day
 */
fun Date.startOfDay(): Date {
    val calendar = Calendar.getInstance().apply { time = this@startOfDay }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Get end of day
 */
fun Date.endOfDay(): Date {
    val calendar = Calendar.getInstance().apply { time = this@endOfDay }
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

/**
 * Get start of week (Monday)
 */
fun Date.startOfWeek(): Date {
    val calendar = Calendar.getInstance().apply { time = this@startOfWeek }
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Get end of week (Sunday)
 */
fun Date.endOfWeek(): Date {
    val calendar = Calendar.getInstance().apply { time = this@endOfWeek }
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

/**
 * Get start of month
 */
fun Date.startOfMonth(): Date {
    val calendar = Calendar.getInstance().apply { time = this@startOfMonth }
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Get end of month
 */
fun Date.endOfMonth(): Date {
    val calendar = Calendar.getInstance().apply { time = this@endOfMonth }
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

/**
 * Add days to date
 */
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance().apply { time = this@addDays }
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

/**
 * Subtract days from date
 */
fun Date.subtractDays(days: Int): Date {
    return addDays(-days)
}

/**
 * Get days difference between two dates
 */
fun Date.daysDifference(other: Date): Long {
    val diffInMillis = this.time - other.time
    return diffInMillis / (1000 * 60 * 60 * 24)
}

/**
 * Get minutes difference between two dates
 */
fun Date.minutesDifference(other: Date): Long {
    val diffInMillis = this.time - other.time
    return diffInMillis / (1000 * 60)
} 
