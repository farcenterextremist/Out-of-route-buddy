package com.example.outofroutebuddy.util

import java.util.Locale

/**
 * Utility class for consistent string formatting across the app
 * Fixes DefaultLocale warnings by explicitly using Locale.US
 */
object FormatUtils {
    fun formatDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)
    fun formatDecimalTwo(value: Double): String = String.format(Locale.US, "%.2f", value)
    fun formatPercentage(value: Double): String = String.format(Locale.US, "%.1f%%", value)
    fun formatPercentageTwo(value: Double): String = String.format(Locale.US, "%.2f%%", value)
    fun formatMiles(value: Double): String = String.format(Locale.US, "%.1f mi", value)
    fun formatMeters(value: Double): String = String.format(Locale.US, "%.2fm", value)
    fun formatSpeedMph(value: Double): String = String.format(Locale.US, "%.1f mph", value)
    fun formatAccuracyFeet(value: Double): String = String.format(Locale.US, "%.1f ft", value)
    fun formatAccuracyMeters(value: Double): String = String.format(Locale.US, "%.1fm", value)
    fun formatHours(value: Double): String = String.format(Locale.US, "%.1f hours", value)
    fun formatAcceleration(value: Double): String = String.format(Locale.US, "%.1f mph/s", value)
} 
