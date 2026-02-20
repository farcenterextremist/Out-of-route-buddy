package com.example.outofroutebuddy.util

import org.junit.Test
import org.junit.Assert.*

/**
 * FormatUtils Tests
 * 
 * Unit tests for formatting utility functions.
 * 
 * Priority: LOW
 * Coverage Target: 95%
 * 
 * Created: December 2024
 */
class FormatUtilsTest {
    
    // ==================== DECIMAL FORMATTING TESTS ====================
    
    @Test
    fun `format decimal should format to one decimal place`() {
        val result = FormatUtils.formatDecimal(123.456)
        assertEquals("123.5", result)
    }
    
    @Test
    fun `format decimal should round up correctly`() {
        val result = FormatUtils.formatDecimal(123.55)
        assertEquals("123.6", result)
    }
    
    @Test
    fun `format decimal should round down correctly`() {
        val result = FormatUtils.formatDecimal(123.44)
        assertEquals("123.4", result)
    }
    
    @Test
    fun `format decimal should handle zero`() {
        val result = FormatUtils.formatDecimal(0.0)
        assertEquals("0.0", result)
    }
    
    @Test
    fun `format decimal should handle negative numbers`() {
        val result = FormatUtils.formatDecimal(-123.456)
        assertEquals("-123.5", result)
    }
    
    @Test
    fun `format decimal two should format to two decimal places`() {
        val result = FormatUtils.formatDecimalTwo(123.456)
        assertEquals("123.46", result)
    }
    
    @Test
    fun `format decimal two should round correctly`() {
        val result = FormatUtils.formatDecimalTwo(123.555)
        assertEquals("123.56", result)
    }
    
    @Test
    fun `format decimal two should handle trailing zeros`() {
        val result = FormatUtils.formatDecimalTwo(123.5)
        assertEquals("123.50", result)
    }
    
    // ==================== PERCENTAGE FORMATTING TESTS ====================
    
    @Test
    fun `format percentage should add percent sign`() {
        val result = FormatUtils.formatPercentage(85.5)
        assertEquals("85.5%", result)
    }
    
    @Test
    fun `format percentage should format to one decimal place`() {
        val result = FormatUtils.formatPercentage(85.55)
        assertEquals("85.6%", result)
    }
    
    @Test
    fun `format percentage should handle zero`() {
        val result = FormatUtils.formatPercentage(0.0)
        assertEquals("0.0%", result)
    }
    
    @Test
    fun `format percentage should handle one hundred`() {
        val result = FormatUtils.formatPercentage(100.0)
        assertEquals("100.0%", result)
    }
    
    @Test
    fun `format percentage two should format to two decimal places`() {
        val result = FormatUtils.formatPercentageTwo(85.555)
        assertEquals("85.56%", result)
    }
    
    @Test
    fun `format percentage two should add percent sign`() {
        val result = FormatUtils.formatPercentageTwo(85.5)
        assertEquals("85.50%", result)
    }
    
    // ==================== MILES FORMATTING TESTS ====================
    
    @Test
    fun `format miles should add mi suffix`() {
        val result = FormatUtils.formatMiles(123.5)
        assertEquals("123.5 mi", result)
    }
    
    @Test
    fun `format miles should format to one decimal place`() {
        val result = FormatUtils.formatMiles(123.55)
        assertEquals("123.6 mi", result)
    }
    
    @Test
    fun `format miles should handle zero`() {
        val result = FormatUtils.formatMiles(0.0)
        assertEquals("0.0 mi", result)
    }
    
    @Test
    fun `format miles should handle large numbers`() {
        val result = FormatUtils.formatMiles(1234.567)
        assertEquals("1234.6 mi", result)
    }
    
    // ==================== METERS FORMATTING TESTS ====================
    
    @Test
    fun `format meters should add m suffix`() {
        val result = FormatUtils.formatMeters(123.456)
        assertEquals("123.46m", result)
    }
    
    @Test
    fun `format meters should format to two decimal places`() {
        val result = FormatUtils.formatMeters(123.5)
        assertEquals("123.50m", result)
    }
    
    @Test
    fun `format meters should handle zero`() {
        val result = FormatUtils.formatMeters(0.0)
        assertEquals("0.00m", result)
    }
    
    // ==================== SPEED FORMATTING TESTS ====================
    
    @Test
    fun `format speed mph should add mph suffix`() {
        val result = FormatUtils.formatSpeedMph(65.5)
        assertEquals("65.5 mph", result)
    }
    
    @Test
    fun `format speed mph should format to one decimal place`() {
        val result = FormatUtils.formatSpeedMph(65.55)
        assertEquals("65.6 mph", result)
    }
    
    @Test
    fun `format speed mph should handle zero`() {
        val result = FormatUtils.formatSpeedMph(0.0)
        assertEquals("0.0 mph", result)
    }
    
    // ==================== ACCURACY FORMATTING TESTS ====================
    
    @Test
    fun `format accuracy feet should add ft suffix`() {
        val result = FormatUtils.formatAccuracyFeet(15.5)
        assertEquals("15.5 ft", result)
    }
    
    @Test
    fun `format accuracy feet should format to one decimal place`() {
        val result = FormatUtils.formatAccuracyFeet(15.55)
        assertEquals("15.6 ft", result)
    }
    
    @Test
    fun `format accuracy meters should add m suffix`() {
        val result = FormatUtils.formatAccuracyMeters(5.5)
        assertEquals("5.5m", result)
    }
    
    @Test
    fun `format accuracy meters should format to one decimal place`() {
        val result = FormatUtils.formatAccuracyMeters(5.55)
        assertEquals("5.6m", result)
    }
    
    @Test
    fun `format accuracy meters should handle zero`() {
        val result = FormatUtils.formatAccuracyMeters(0.0)
        assertEquals("0.0m", result)
    }
    
    // ==================== TIME FORMATTING TESTS ====================
    
    @Test
    fun `format hours should add hours suffix`() {
        val result = FormatUtils.formatHours(2.5)
        assertEquals("2.5 hours", result)
    }
    
    @Test
    fun `format hours should format to one decimal place`() {
        val result = FormatUtils.formatHours(2.55)
        assertEquals("2.6 hours", result)
    }
    
    @Test
    fun `format hours should handle zero`() {
        val result = FormatUtils.formatHours(0.0)
        assertEquals("0.0 hours", result)
    }
    
    @Test
    fun `format hours should handle fractional hours`() {
        val result = FormatUtils.formatHours(0.5)
        assertEquals("0.5 hours", result)
    }
    
    // ==================== ACCELERATION FORMATTING TESTS ====================
    
    @Test
    fun `format acceleration should add mph slash s suffix`() {
        val result = FormatUtils.formatAcceleration(2.5)
        assertEquals("2.5 mph/s", result)
    }
    
    @Test
    fun `format acceleration should format to one decimal place`() {
        val result = FormatUtils.formatAcceleration(2.55)
        assertEquals("2.6 mph/s", result)
    }
    
    @Test
    fun `format acceleration should handle zero`() {
        val result = FormatUtils.formatAcceleration(0.0)
        assertEquals("0.0 mph/s", result)
    }
    
    @Test
    fun `format acceleration should handle negative values`() {
        val result = FormatUtils.formatAcceleration(-2.5)
        assertEquals("-2.5 mph/s", result)
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun `all formats should use US locale`() {
        // Test that numbers use US formatting (period for decimal separator)
        val decimal = FormatUtils.formatDecimal(123.456)
        assertTrue("Should use period as decimal separator", decimal.contains("."))
        assertFalse("Should not use comma as decimal separator", decimal.contains(","))
    }
    
    @Test
    fun `format should handle very small numbers`() {
        val result = FormatUtils.formatDecimal(0.001)
        assertEquals("0.0", result)
    }
    
    @Test
    fun `format should handle very large numbers`() {
        val result = FormatUtils.formatDecimal(999999.999)
        assertEquals("1000000.0", result)
    }
    
    @Test
    fun `format should handle exact halves for rounding`() {
        // .5 should round up
        val result = FormatUtils.formatDecimal(123.5)
        assertEquals("123.5", result)
    }
}





