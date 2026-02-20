package com.example.outofroutebuddy.util

import org.junit.Test
import org.junit.Assert.*

/**
 * InputValidator Tests
 * 
 * Unit tests for input validation and sanitization.
 * 
 * Priority: MEDIUM
 * Coverage Target: 90%
 * 
 * Created: December 2024
 */
class InputValidatorTest {
    
    // ==================== MILES SANITIZATION TESTS ====================
    
    @Test
    fun `sanitize miles with valid input should return double`() {
        val result = InputValidator.sanitizeMiles("100.5")
        assertNotNull("Should return valid double", result)
        assertEquals(100.5, result!!, 0.001)
    }
    
    @Test
    fun `sanitize miles with blank input should return null`() {
        val result = InputValidator.sanitizeMiles("")
        assertNull("Should return null for blank", result)
    }
    
    @Test
    fun `sanitize miles with whitespace only should return null`() {
        val result = InputValidator.sanitizeMiles("   ")
        assertNull("Should return null for whitespace only", result)
    }
    
    @Test
    fun `sanitize miles with invalid format should return null`() {
        val result = InputValidator.sanitizeMiles("not a number")
        assertNull("Should return null for invalid format", result)
    }
    
    @Test
    fun `sanitize miles with negative value should return null`() {
        val result = InputValidator.sanitizeMiles("-10")
        assertNull("Should return null for negative value", result)
    }
    
    @Test
    fun `sanitize miles with value too high should return null`() {
        val result = InputValidator.sanitizeMiles("20000")
        assertNull("Should return null for value > 10000", result)
    }
    
    @Test
    fun `sanitize miles with zero should return zero`() {
        val result = InputValidator.sanitizeMiles("0")
        assertEquals(0.0, result!!, 0.001)
    }
    
    @Test
    fun `sanitize miles with maximum value should return value`() {
        val result = InputValidator.sanitizeMiles("10000")
        assertNotNull("Should return value at max", result)
        assertEquals(10000.0, result!!, 0.001)
    }
    
    @Test
    fun `sanitize miles should trim whitespace`() {
        val result = InputValidator.sanitizeMiles("  123.5  ")
        assertNotNull("Should handle trimmed input", result)
        assertEquals(123.5, result!!, 0.001)
    }
    
    @Test
    fun `sanitize miles with NaN string should handle gracefully`() {
        val result = InputValidator.sanitizeMiles("NaN")
        assertNull("Should return null for NaN", result)
    }
    
    // ==================== FILE PATH SANITIZATION TESTS ====================
    
    @Test
    fun `sanitize file path with valid path should return path`() {
        val result = InputValidator.sanitizeFilePath("trip_data.csv")
        assertNotNull("Should return valid path", result)
        assertEquals("trip_data.csv", result)
    }
    
    @Test
    fun `sanitize file path with directory traversal should return null`() {
        val result = InputValidator.sanitizeFilePath("../../etc/passwd")
        assertNull("Should reject directory traversal", result)
    }
    
    @Test
    fun `sanitize file path with home directory should return null`() {
        val result = InputValidator.sanitizeFilePath("~/secret.txt")
        assertNull("Should reject home directory access", result)
    }
    
    @Test
    fun `sanitize file path with absolute path should return null`() {
        val result = InputValidator.sanitizeFilePath("/etc/passwd")
        assertNull("Should reject absolute path", result)
    }
    
    @Test
    fun `sanitize file path with Windows absolute path should return null`() {
        val result = InputValidator.sanitizeFilePath("C:\\Windows\\system32")
        assertNull("Should reject absolute Windows path", result)
    }
    
    @Test
    fun `sanitize file path with invalid extension should return null`() {
        val result = InputValidator.sanitizeFilePath("data.exe")
        assertNull("Should reject invalid extension", result)
    }
    
    @Test
    fun `sanitize file path with valid extensions should pass`() {
        listOf(".csv", ".json", ".txt", ".log").forEach { ext ->
            val result = InputValidator.sanitizeFilePath("data$ext")
            assertNotNull("Should accept $ext extension", result)
        }
    }
    
    @Test
    fun `sanitize file path with invalid characters should return null`() {
        val result = InputValidator.sanitizeFilePath("data<script>.csv")
        assertNull("Should reject invalid characters", result)
    }
    
    @Test
    fun `sanitize file path should trim whitespace`() {
        val result = InputValidator.sanitizeFilePath("  data.csv  ")
        assertNotNull("Should handle trimmed path", result)
        assertEquals("data.csv", result)
    }
    
    @Test
    fun `sanitize file path with subdirectory should allow`() {
        val result = InputValidator.sanitizeFilePath("trips/2024/data.csv")
        assertNotNull("Should allow subdirectories", result)
    }
    
    // ==================== STRING SANITIZATION TESTS ====================
    
    @Test
    fun `sanitize string with valid input should return trimmed string`() {
        val result = InputValidator.sanitizeString("  hello  ")
        assertEquals("hello", result)
    }
    
    @Test
    fun `sanitize string with long input should truncate`() {
        val longString = "a".repeat(2000)
        val result = InputValidator.sanitizeString(longString)
        assertEquals("String should be truncated to 1000 chars", 1000, result.length)
    }
    
    @Test
    fun `sanitize string with custom max length should respect limit`() {
        val longString = "a".repeat(500)
        val result = InputValidator.sanitizeString(longString, maxLength = 100)
        assertEquals("String should be truncated to custom limit", 100, result.length)
    }
    
    @Test
    fun `sanitize string with valid length should not truncate`() {
        val result = InputValidator.sanitizeString("Short string")
        assertEquals("Short string", result)
    }
    
    @Test
    fun `sanitize string with empty input should return empty`() {
        val result = InputValidator.sanitizeString("")
        assertEquals("", result)
    }
    
    // ==================== RANGE VALIDATION TESTS ====================
    
    @Test
    fun `validate range with value in range should return true`() {
        val result = InputValidator.validateRange(5.0, 0.0, 10.0)
        assertTrue("Should return true for value in range", result)
    }
    
    @Test
    fun `validate range with value below min should return false`() {
        val result = InputValidator.validateRange(-5.0, 0.0, 10.0)
        assertFalse("Should return false for value below min", result)
    }
    
    @Test
    fun `validate range with value above max should return false`() {
        val result = InputValidator.validateRange(15.0, 0.0, 10.0)
        assertFalse("Should return false for value above max", result)
    }
    
    @Test
    fun `validate range with value at min should return true`() {
        val result = InputValidator.validateRange(0.0, 0.0, 10.0)
        assertTrue("Should return true for value at min", result)
    }
    
    @Test
    fun `validate range with value at max should return true`() {
        val result = InputValidator.validateRange(10.0, 0.0, 10.0)
        assertTrue("Should return true for value at max", result)
    }
    
    // ==================== PERCENTAGE VALIDATION TESTS ====================
    
    @Test
    fun `validate percentage with valid percentage should return true`() {
        val result = InputValidator.validatePercentage(50.0)
        assertTrue("Should return true for valid percentage", result)
    }
    
    @Test
    fun `validate percentage with zero should return true`() {
        val result = InputValidator.validatePercentage(0.0)
        assertTrue("Should return true for zero", result)
    }
    
    @Test
    fun `validate percentage with one hundred should return true`() {
        val result = InputValidator.validatePercentage(100.0)
        assertTrue("Should return true for 100", result)
    }
    
    @Test
    fun `validate percentage with negative should return false`() {
        val result = InputValidator.validatePercentage(-5.0)
        assertFalse("Should return false for negative", result)
    }
    
    @Test
    fun `validate percentage above one hundred should return false`() {
        val result = InputValidator.validatePercentage(150.0)
        assertFalse("Should return false for value > 100", result)
    }
    
    // ==================== POSITIVE VALIDATION TESTS ====================
    
    @Test
    fun `validate positive with positive value should return true`() {
        val result = InputValidator.validatePositive(5.0)
        assertTrue("Should return true for positive", result)
    }
    
    @Test
    fun `validate positive with zero should return true`() {
        val result = InputValidator.validatePositive(0.0)
        assertTrue("Should return true for zero", result)
    }
    
    @Test
    fun `validate positive with negative should return false`() {
        val result = InputValidator.validatePositive(-5.0)
        assertFalse("Should return false for negative", result)
    }
    
    @Test
    fun `validate positive with NaN should return false`() {
        val result = InputValidator.validatePositive(Double.NaN)
        assertFalse("Should return false for NaN", result)
    }
    
    @Test
    fun `validate positive with infinite should return false`() {
        val result = InputValidator.validatePositive(Double.POSITIVE_INFINITY)
        assertFalse("Should return false for infinite", result)
    }
    
    // ==================== FILE NAME SANITIZATION TESTS ====================
    
    @Test
    fun `sanitize file name with valid name should return name`() {
        val result = InputValidator.sanitizeFileName("trip_data.csv")
        assertNotNull("Should return valid filename", result)
        assertEquals("trip_data.csv", result)
    }
    
    @Test
    fun `sanitize file name with forward slash should return null`() {
        val result = InputValidator.sanitizeFileName("path/to/file.csv")
        assertNull("Should reject path separators", result)
    }
    
    @Test
    fun `sanitize file name with backslash should return null`() {
        val result = InputValidator.sanitizeFileName("path\\to\\file.csv")
        assertNull("Should reject backslashes", result)
    }
    
    @Test
    fun `sanitize file name with invalid characters should return null`() {
        val result = InputValidator.sanitizeFileName("file<script>.csv")
        assertNull("Should reject invalid characters", result)
    }
    
    @Test
    fun `sanitize file name with valid special characters should allow`() {
        val result = InputValidator.sanitizeFileName("file_name-2024.csv")
        assertNotNull("Should allow valid special characters", result)
    }
    
    @Test
    fun `sanitize file name with too long name should return null`() {
        val longName = "a".repeat(300) + ".csv"
        val result = InputValidator.sanitizeFileName(longName)
        assertNull("Should reject filename > 255 chars", result)
    }
    
    // ==================== COMPREHENSIVE VALIDATION TESTS ====================
    
    @Test
    fun `validate miles input with valid input should return success result`() {
        val result = InputValidator.validateMilesInput("100.5")
        assertTrue("Should be valid", result.isValid)
        assertEquals(100.5, result.sanitizedValue as Double, 0.001)
        assertNull("Should have no error message", result.errorMessage)
    }
    
    @Test
    fun `validate miles input with invalid input should return failure result`() {
        val result = InputValidator.validateMilesInput("invalid")
        assertFalse("Should be invalid", result.isValid)
        assertNull("Should have no sanitized value", result.sanitizedValue)
        assertNotNull("Should have error message", result.errorMessage)
    }
    
    @Test
    fun `validate miles input with out of range should return failure`() {
        val result = InputValidator.validateMilesInput("20000")
        assertFalse("Should be invalid", result.isValid)
    }
}

