package com.example.outofroutebuddy.data

import com.example.outofroutebuddy.data.util.DateConverter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for DateConverter to ensure proper date serialization/deserialization
 * in the Room database.
 */
class DateConverterTest {
    private lateinit var dateConverter: DateConverter

    @Before
    fun setUp() {
        dateConverter = DateConverter()
    }

    @Test
    fun `dateToTimestamp converts valid date to timestamp`() {
        // Given
        val date = Date(1640995200000) // 2022-01-01 00:00:00 UTC

        // When
        val timestamp = dateConverter.dateToTimestamp(date)

        // Then
        assertEquals(1640995200000, timestamp)
    }

    @Test
    fun `fromTimestamp converts valid timestamp to date`() {
        // Given
        val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC

        // When
        val date = dateConverter.fromTimestamp(timestamp)

        // Then
        assertNotNull(date)
        assertEquals(1640995200000, date!!.time)
    }

    @Test
    fun `dateToTimestamp with null returns null`() {
        // When
        val timestamp = dateConverter.dateToTimestamp(null)

        // Then
        assertNull(timestamp)
    }

    @Test
    fun `fromTimestamp with null returns null`() {
        // When
        val date = dateConverter.fromTimestamp(null)

        // Then
        assertNull(date)
    }

    @Test
    fun `round trip conversion preserves date`() {
        // Given
        val originalDate = Date()

        // When
        val timestamp = dateConverter.dateToTimestamp(originalDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)

        // Then
        assertNotNull(convertedDate)
        assertEquals(originalDate.time, convertedDate!!.time)
    }

    @Test
    fun `handles epoch date correctly`() {
        // Given
        val epochDate = Date(0) // 1970-01-01 00:00:00 UTC

        // When
        val timestamp = dateConverter.dateToTimestamp(epochDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)

        // Then
        assertEquals(0L, timestamp)
        assertEquals(epochDate.time, convertedDate!!.time)
    }

    @Test
    fun `handles future date correctly`() {
        // Given
        val futureDate = Date(4102444800000) // 2100-01-01 00:00:00 UTC

        // When
        val timestamp = dateConverter.dateToTimestamp(futureDate)
        val convertedDate = dateConverter.fromTimestamp(timestamp)

        // Then
        assertEquals(4102444800000, timestamp)
        assertEquals(futureDate.time, convertedDate!!.time)
    }

    @Test
    fun `handles date with milliseconds precision`() {
        // Given
        val dateWithMillis = Date(1640995200123) // 2022-01-01 00:00:00.123 UTC

        // When
        val timestamp = dateConverter.dateToTimestamp(dateWithMillis)
        val convertedDate = dateConverter.fromTimestamp(timestamp)

        // Then
        assertEquals(1640995200123, timestamp)
        assertEquals(dateWithMillis.time, convertedDate!!.time)
    }

    @Test
    fun `multiple conversions maintain consistency`() {
        // Given
        val originalDate = Date()

        // When
        val timestamp1 = dateConverter.dateToTimestamp(originalDate)
        val date1 = dateConverter.fromTimestamp(timestamp1)
        val timestamp2 = dateConverter.dateToTimestamp(date1)
        val date2 = dateConverter.fromTimestamp(timestamp2)

        // Then
        assertEquals(timestamp1, timestamp2)
        assertEquals(date1!!.time, date2!!.time)
        assertEquals(originalDate.time, date2.time)
    }
} 
