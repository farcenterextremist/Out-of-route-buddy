package com.example.outofroutebuddy.utils.formatters

import java.util.Locale

/**
 * Distance formatting utilities
 * 
 * This class provides consistent formatting for distance values
 * throughout the application.
 */
object DistanceFormatter {
    
    /**
     * Format distance in miles with one decimal place
     * 
     * @param miles The distance in miles
     * @return Formatted distance string
     */
    fun formatMiles(miles: Double): String {
        return String.format(Locale.US, "%.1f", miles)
    }
    
    /**
     * Format distance in miles with two decimal places
     * 
     * @param miles The distance in miles
     * @return Formatted distance string
     */
    fun formatMilesDetailed(miles: Double): String {
        return String.format(Locale.US, "%.2f", miles)
    }
    
    /**
     * Format distance in miles with units
     * 
     * @param miles The distance in miles
     * @return Formatted distance string with units
     */
    fun formatMilesWithUnits(miles: Double): String {
        return "${formatMiles(miles)} mi"
    }
    
    /**
     * Format distance in meters
     * 
     * @param meters The distance in meters
     * @return Formatted distance string
     */
    fun formatMeters(meters: Double): String {
        return String.format(Locale.US, "%.1f", meters)
    }
    
    /**
     * Format distance in meters with units
     * 
     * @param meters The distance in meters
     * @return Formatted distance string with units
     */
    fun formatMetersWithUnits(meters: Double): String {
        return "${formatMeters(meters)}m"
    }
    
    /**
     * Format distance in feet
     * 
     * @param feet The distance in feet
     * @return Formatted distance string
     */
    fun formatFeet(feet: Double): String {
        return String.format(Locale.US, "%.1f", feet)
    }
    
    /**
     * Format distance in feet with units
     * 
     * @param feet The distance in feet
     * @return Formatted distance string with units
     */
    fun formatFeetWithUnits(feet: Double): String {
        return "${formatFeet(feet)} ft"
    }
    
    /**
     * Convert meters to feet and format
     * 
     * @param meters The distance in meters
     * @return Formatted distance string in feet
     */
    fun formatMetersToFeet(meters: Double): String {
        val feet = meters * 3.28084
        return formatFeetWithUnits(feet)
    }
    
    /**
     * Format percentage with one decimal place
     * 
     * @param percentage The percentage value
     * @return Formatted percentage string
     */
    fun formatPercentage(percentage: Double): String {
        return String.format(Locale.US, "%.1f%%", percentage)
    }
    
    /**
     * Format percentage with two decimal places
     * 
     * @param percentage The percentage value
     * @return Formatted percentage string
     */
    fun formatPercentageDetailed(percentage: Double): String {
        return String.format(Locale.US, "%.2f%%", percentage)
    }
    
    /**
     * Format speed in miles per hour
     * 
     * @param mph The speed in miles per hour
     * @return Formatted speed string
     */
    fun formatSpeedMph(mph: Double): String {
        return String.format(Locale.US, "%.1f mph", mph)
    }
    
    /**
     * Format speed in miles per hour (no units)
     * 
     * @param mph The speed in miles per hour
     * @return Formatted speed string without units
     */
    fun formatSpeedMphNoUnits(mph: Double): String {
        return String.format(Locale.US, "%.1f", mph)
    }
    
    /**
     * Format acceleration in mph per second
     * 
     * @param acceleration The acceleration in mph/s
     * @return Formatted acceleration string
     */
    fun formatAcceleration(acceleration: Double): String {
        return String.format(Locale.US, "%.1f mph/s", acceleration)
    }
    
    /**
     * Format large numbers with comma separators
     * 
     * @param number The number to format
     * @return Formatted number string
     */
    fun formatLargeNumber(number: Int): String {
        return String.format(Locale.US, "%,d", number)
    }
    
    /**
     * Format large numbers with comma separators
     * 
     * @param number The number to format
     * @return Formatted number string
     */
    fun formatLargeNumber(number: Long): String {
        return String.format(Locale.US, "%,d", number)
    }
    
    /**
     * Format distance range (e.g., "5.0 - 10.0 mi")
     * 
     * @param min The minimum distance
     * @param max The maximum distance
     * @return Formatted range string
     */
    fun formatRange(min: Double, max: Double): String {
        return "${formatMiles(min)} - ${formatMiles(max)} mi"
    }
    
    /**
     * Format distance with appropriate units based on magnitude
     * 
     * @param meters The distance in meters
     * @return Formatted distance string with appropriate units
     */
    fun formatDistanceSmart(meters: Double): String {
        return when {
            meters < 1000 -> formatMetersWithUnits(meters)
            else -> formatMilesWithUnits(meters * 0.000621371) // Convert to miles
        }
    }
    
    /**
     * Format trip duration in hours and minutes
     * 
     * @param minutes The duration in minutes
     * @return Formatted duration string
     */
    fun formatTripDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}m"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes == 0) "${hours}h" else "${hours}h ${remainingMinutes}m"
            }
        }
    }
    
    /**
     * Format trip duration in hours and minutes from milliseconds
     * 
     * @param milliseconds The duration in milliseconds
     * @return Formatted duration string
     */
    fun formatTripDurationFromMillis(milliseconds: Long): String {
        val minutes = (milliseconds / (1000 * 60)).toInt()
        return formatTripDuration(minutes)
    }
    
    /**
     * ✅ NEW: Generic format method for distances (defaults to miles with units)
     * 
     * @param distance The distance value
     * @return Formatted distance string with units
     */
    fun format(distance: Double): String {
        return formatMilesWithUnits(distance)
    }
    
    /**
     * ✅ NEW: Format distance with custom precision
     * 
     * @param distance The distance value
     * @param precision Number of decimal places
     * @return Formatted distance string
     */
    fun format(distance: Double, precision: Int): String {
        return String.format(Locale.US, "%.${precision}f", distance)
    }
} 
