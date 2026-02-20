package com.example.outofroutebuddy.data.util

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * DateConverter Tests
 * 
 * Unit tests for Room date type converters.
 * 
 * Priority: LOW
 * Coverage Target: 100%
 * 
 * Created: December 2024
 */
class DateConverterTest {
    
    private lateinit var dateConverter: DateConverter
    
    @Before
    fun setup() {
        dateConverter = DateConverter()
    }
    
    // ==================== TIMESTAMP TO DATE TESTS ====================
    
    @Test
    fun `fromTimestamp with valid timestamp should return date`() {
        val timestamp = 1609459200000L // 2021-01-01 00:00:00 UTC
        val result = dateConverter.fromTimestamp(timestamp)
        
        assertNotNull("Should return date", result)
        assertEquals(timestamp, result?.time)
    }
    
    @Test
    fun `fromTimestamp with zero should return zero date`() {
        val timestamp = 0L
        val result = dateConverter.fromTimestamp(timestamp)
        
        assertNotNull("Should return date", result)
        assertEquals(timestamp, result?.time)
    }
    
    @Test
    fun `fromTimestamp with null should return null`() {
        val result = dateConverter.fromTimestamp(null)
        
        assertNull("Should return null for null input", result)
    }
    
    @Test
    fun `fromTimestamp with future timestamp should return future date`() {
        val futureTimestamp = System.currentTimeMillis() + 86400000L // +1 day
        val result = dateConverter.fromTimestamp(futureTimestamp)
        
        assertNotNull("Should return future date", result)
        assertTrue("Date should be in future", result!!.after(Date()))
    }
    
    @Test
    fun `fromTimestamp with past timestamp should return past date`() {
        val pastTimestamp = System.currentTimeMillis() - 86400000L // -1 day
        val result = dateConverter.fromTimestamp(pastTimestamp)
        
        assertNotNull("Should return past date", result)
        assertTrue("Date should be in past", result!!.before(Date()))
    }
    
    // ==================== DATE TO TIMESTAMP TESTS ====================
    
    @Test
    fun `dateToTimestamp with valid date should return timestamp`() {
        val date = Date(1609459200000L) // 2021-01-01 00:00:00 UTC
        val result = dateConverter.dateToTimestamp(date)
        
        assertNotNull("Should return timestamp", result)
        assertEquals(1609459200000L, result)
    }
    
    @Test
    fun `dateToTimestamp with current date should return current timestamp`() {
        val now = Date()
        val result = dateConverter.dateToTimestamp(now)
        
        assertNotNull("Should return timestamp", result)
        assertEquals(now.time, result)
    }
    
    @Test
    fun `dateToTimestamp with null should return null`() {
        val result = dateConverter.dateToTimestamp(null)
        
        assertNull("Should return null for null input", result)
    }
    
    @Test
    fun `dateToTimestamp with epoch should return zero`() {
        val epoch = Date(0L)
        val result = dateConverter.dateToTimestamp(epoch)
        
        assertNotNull("Should return timestamp", result)
        assertEquals(0L, result)
    }
    
    // ==================== ROUND TRIP TESTS ====================
    
    @Test
    fun `round trip conversion should preserve value`() {
        val originalDate = Date()
        
        val timestamp = dateConverter.dateToTimestamp(originalDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)
        
        assertNotNull("Converted date should not be null", convertedDate)
        assertEquals("Date should be preserved", originalDate.time, convertedDate?.time)
    }
    
    @Test
    fun `round trip with null should handle correctly`() {
        val timestamp = dateConverter.dateToTimestamp(null)
        val convertedDate = dateConverter.fromTimestamp(timestamp)
        
        assertNull("Should handle null round trip", convertedDate)
    }
    
    @Test
    fun `round trip with specific date should preserve value`() {
        val specificDate = Date(1609459200000L)
        
        val timestamp = dateConverter.dateToTimestamp(specificDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)
        
        assertNotNull("Converted date should not be null", convertedDate)
        assertEquals("Specific date should be preserved", 1609459200000L, convertedDate?.time)
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun `conversion should handle millisecond precision`() {
        val originalDate = Date(1609459200123L) // With milliseconds
        val timestamp = dateConverter.dateToTimestamp(originalDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)
        
        assertEquals("Should preserve milliseconds", originalDate.time, convertedDate?.time)
    }
    
    @Test
    fun `conversion should handle large timestamps`() {
        val largeTimestamp = 9999999999999L
        val result = dateConverter.fromTimestamp(largeTimestamp)
        
        assertNotNull("Should handle large timestamp", result)
        assertEquals(largeTimestamp, result?.time)
    }
}





