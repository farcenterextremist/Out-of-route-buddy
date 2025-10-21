package com.example.outofroutebuddy.util

import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 🌍 Timezone Handler
 * 
 * Ensures correct timezone handling across the app.
 * 
 * ✅ NEW (#22): Timezone Validation
 * 
 * Rules:
 * - ALWAYS store dates in UTC internally
 * - ALWAYS convert to local timezone for display only
 * - Handle DST transitions correctly
 * - Test timezone changes mid-trip
 * 
 * Priority: MEDIUM
 * Impact: Data correctness, international support
 */
object TimezoneHandler {
    
    private const val TAG = "TimezoneHandler"
    
    /**
     * Convert local time to UTC for storage
     * 
     * @param localDate Date in local timezone
     * @return Date in UTC
     */
    fun toUTC(localDate: Date): Date {
        return try {
            // Convert to UTC
            val instant = localDate.toInstant()
            val utcZoned = instant.atZone(ZoneId.of("UTC"))
            Date.from(utcZoned.toInstant())
        } catch (e: Exception) {
            Log.e(TAG, "Error converting to UTC", e)
            localDate // Fallback to original
        }
    }
    
    /**
     * Convert UTC time to local timezone for display
     * 
     * @param utcDate Date in UTC
     * @return Date in local timezone
     */
    fun toLocal(utcDate: Date): Date {
        return try {
            // Convert to local timezone
            val instant = utcDate.toInstant()
            val localZoned = instant.atZone(ZoneId.systemDefault())
            Date.from(localZoned.toInstant())
        } catch (e: Exception) {
            Log.e(TAG, "Error converting to local", e)
            utcDate // Fallback to original
        }
    }
    
    /**
     * Format UTC date for display in local timezone
     * 
     * @param utcDate Date in UTC
     * @param pattern Format pattern (e.g., "MMM dd, yyyy HH:mm")
     * @return Formatted string in local timezone
     */
    fun formatForDisplay(utcDate: Date, pattern: String = "MMM dd, yyyy HH:mm"): String {
        return try {
            val instant = utcDate.toInstant()
            val localZoned = instant.atZone(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            localZoned.format(formatter)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date", e)
            SimpleDateFormat(pattern, Locale.getDefault()).format(utcDate)
        }
    }
    
    /**
     * Get current UTC time
     */
    fun nowUTC(): Date {
        return Date.from(Instant.now().atZone(ZoneId.of("UTC")).toInstant())
    }
    
    /**
     * Check if DST is currently active in local timezone
     */
    fun isDSTActive(): Boolean {
        return try {
            val zone = ZoneId.systemDefault()
            val zoned = ZonedDateTime.now(zone)
            zone.rules.isDaylightSavings(zoned.toInstant())
        } catch (e: Exception) {
            Log.e(TAG, "Error checking DST", e)
            false
        }
    }
    
    /**
     * Get timezone offset in hours from UTC
     */
    fun getTimezoneOffsetHours(): Int {
        return try {
            val zone = ZoneId.systemDefault()
            val offset = zone.rules.getOffset(Instant.now())
            offset.totalSeconds / 3600
        } catch (e: Exception) {
            Log.e(TAG, "Error getting timezone offset", e)
            0
        }
    }
    
    /**
     * Calculate trip duration accounting for DST transitions
     * 
     * @param startTime Trip start (UTC)
     * @param endTime Trip end (UTC)
     * @return Duration in milliseconds (accurate across DST)
     */
    fun calculateDuration(startTime: Date, endTime: Date): Long {
        return try {
            // Duration in UTC is always correct regardless of DST
            endTime.time - startTime.time
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating duration", e)
            0L
        }
    }
    
    /**
     * Validate that a date is in UTC
     * 
     * This is a best-effort check - we can't guarantee timezone without metadata
     */
    fun looksLikeUTC(date: Date): Boolean {
        // If the date's time matches UTC time closely, it's likely UTC
        val utcNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val dateCalendar = Calendar.getInstance().apply { time = date }
        
        // Check if hour difference is small (within expected timezone offset)
        val hourDiff = kotlin.math.abs(utcNow.get(Calendar.HOUR_OF_DAY) - dateCalendar.get(Calendar.HOUR_OF_DAY))
        
        // This is not foolproof, but helps catch obvious errors
        return hourDiff <= 12 // Reasonable timezone range
    }
    
    /**
     * Get timezone information for logging
     */
    fun getTimezoneInfo(): TimezoneInfo {
        return try {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            val offset = zone.rules.getOffset(now.toInstant())
            
            TimezoneInfo(
                timezoneId = zone.id,
                displayName = zone.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
                offsetHours = offset.totalSeconds / 3600,
                isDSTActive = zone.rules.isDaylightSavings(now.toInstant()),
                currentTime = Date.from(now.toInstant())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting timezone info", e)
            TimezoneInfo(
                timezoneId = "Unknown",
                displayName = "Unknown",
                offsetHours = 0,
                isDSTActive = false,
                currentTime = Date()
            )
        }
    }
    
    /**
     * Timezone information
     */
    data class TimezoneInfo(
        val timezoneId: String,
        val displayName: String,
        val offsetHours: Int,
        val isDSTActive: Boolean,
        val currentTime: Date
    )
}

