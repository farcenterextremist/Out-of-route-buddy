package com.example.outofroutebuddy.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class TripTest {
    private val delta = 0.001 // Delta value for floating-point comparisons

    @Test
    fun `trip with valid inputs calculates correct values`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0 // GPS miles

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(150.0, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(350.0, trip.oorMiles, delta)
        assertEquals(233.33333333333334, trip.oorPercentage, delta)
    }

    @Test
    fun `trip with zero loaded miles has zero oor percentage`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 50.0
        val actualMiles = 50.0

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(50.0, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(0.0, trip.oorMiles, delta)
        assertEquals(0.0, trip.oorPercentage, delta)
    }

    @Test
    fun `trip with custom date uses provided date`() {
        // Given
        val customDate = Date(1234567890000) // 2009-02-13 23:31:30 UTC
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                date = customDate,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(customDate, trip.date)
    }

    @Test
    fun `trip with custom id uses provided id`() {
        // Given
        val customId = 123L
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                id = customId,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(customId, trip.id)
    }

    @Test
    fun `trip with negative bounce miles throws exception`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = -20.0
        val actualMiles = 500.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Bounce miles cannot be negative") == true)
    }

    @Test
    fun `trip with very large numbers throws exception`() {
        // Given
        val loadedMiles = 999999.99
        val bounceMiles = 999999.99
        val actualMiles = 2000000.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("seems unrealistic") == true)
    }

    @Test
    fun `trip with decimal precision handles correctly`() {
        // Given
        val loadedMiles = 100.123456
        val bounceMiles = 50.123456
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(150.246912, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(349.753088, trip.oorMiles, delta)
        assertEquals(232.78554170883723, trip.oorPercentage, 0.01)
    }

    @Test
    fun `trip with equal dispatched and actual miles has zero oor percentage`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 150.0

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(150.0, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(0.0, trip.oorMiles, delta)
        assertEquals(0.0, trip.oorPercentage, delta)
    }

    @Test
    fun `trip with actual miles less than dispatched miles has negative oor percentage`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 100.0

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(150.0, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(-50.0, trip.oorMiles, delta)
        assertEquals(-33.333333333333336, trip.oorPercentage, delta)
    }

    @Test
    fun `trip with zero actual miles throws exception`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 0.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with default date uses current date`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0
        val beforeCreation = Date()

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )
        val afterCreation = Date()

        // Then
        assertNotEquals(null, trip.date)
        assert(trip.date.time in beforeCreation.time..afterCreation.time)
    }

    @Test
    fun `trip with maximum double values throws exception`() {
        // Given
        val loadedMiles = Double.MAX_VALUE
        val bounceMiles = Double.MAX_VALUE
        val actualMiles = Double.MAX_VALUE

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("seems unrealistic") == true)
    }

    @Test
    fun `trip with minimum double values throws exception`() {
        // Given
        val loadedMiles = Double.MIN_VALUE
        val bounceMiles = Double.MIN_VALUE
        val actualMiles = Double.MIN_VALUE

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with NaN values throws exception`() {
        // Given
        val loadedMiles = Double.NaN
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("cannot be NaN") == true)
    }

    @Test
    fun `trip with infinity values throws exception`() {
        // Given
        val loadedMiles = Double.POSITIVE_INFINITY
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Trip values cannot be infinite") == true)
    }

    @Test
    fun `trip with date at start of month handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        // When
        val trip =
            Trip(
                date = startOfMonth,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(startOfMonth, trip.date)
    }

    @Test
    fun `trip with date at end of month handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        // When
        val trip =
            Trip(
                date = endOfMonth,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(endOfMonth, trip.date)
    }

    @Test
    fun `trip with leap year date handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 29) // 2024 is a leap year
        val leapYearDate = calendar.time

        // When
        val trip =
            Trip(
                date = leapYearDate,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(leapYearDate, trip.date)
    }

    @Test
    fun `trip with negative id handles correctly`() {
        // Given
        val negativeId = -123L
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                id = negativeId,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(negativeId, trip.id)
    }

    @Test
    fun `trip with maximum long id handles correctly`() {
        // Given
        val maxId = Long.MAX_VALUE
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                id = maxId,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(maxId, trip.id)
    }

    @Test
    fun `trip with minimum long id handles correctly`() {
        // Given
        val minId = Long.MIN_VALUE
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                id = minId,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(minId, trip.id)
    }

    @Test
    fun `trip with zero id handles correctly`() {
        // Given
        val zeroId = 0L
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When
        val trip =
            Trip(
                id = zeroId,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(zeroId, trip.id)
    }

    @Test
    fun `trip with negative infinity values throws exception`() {
        // Given
        val loadedMiles = Double.NEGATIVE_INFINITY
        val bounceMiles = 50.0
        val actualMiles = 500.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Trip values cannot be infinite") == true)
    }

    @Test
    fun `trip with epsilon values throws exception`() {
        // Given
        val loadedMiles = Double.MIN_VALUE
        val bounceMiles = Double.MIN_VALUE
        val actualMiles = Double.MIN_VALUE * 2

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with date in different time zones handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC"),
                TimeZone.getTimeZone("America/New_York"),
                TimeZone.getTimeZone("Asia/Tokyo"),
                TimeZone.getTimeZone("Australia/Sydney"),
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            val date = calendar.time

            // When
            val trip =
                Trip(
                    date = date,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(date, trip.date)
        }
    }

    @Test
    fun `trip with date during daylight saving time transition handles correctly`() {
        // Given
        val timeZone = TimeZone.getTimeZone("America/New_York")
        val calendar = Calendar.getInstance(timeZone)

        // Set to DST transition date (second Sunday in March)
        calendar.set(2024, Calendar.MARCH, 10, 1, 59, 59)
        val beforeDST = calendar.time

        calendar.add(Calendar.SECOND, 1)
        val duringDST = calendar.time

        // When
        val tripBefore =
            Trip(
                date = beforeDST,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripDuring =
            Trip(
                date = duringDST,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(beforeDST, tripBefore.date)
        assertEquals(duringDST, tripDuring.date)
    }

    @Test
    fun `trip with date at year boundaries handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()

        // Test year start
        calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val yearStart = calendar.time

        // Test year end
        calendar.set(2024, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val yearEnd = calendar.time

        // When
        val tripStart =
            Trip(
                date = yearStart,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripEnd =
            Trip(
                date = yearEnd,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(yearStart, tripStart.date)
        assertEquals(yearEnd, tripEnd.date)
    }

    @Test
    fun `trip with date at century boundaries handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()

        // Test century start
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val centuryStart = calendar.time

        // Test century end
        calendar.set(2099, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val centuryEnd = calendar.time

        // When
        val tripStart =
            Trip(
                date = centuryStart,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripEnd =
            Trip(
                date = centuryEnd,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(centuryStart, tripStart.date)
        assertEquals(centuryEnd, tripEnd.date)
    }

    @Test
    fun `trip with date at millennium boundaries handles correctly`() {
        // Given
        val calendar = Calendar.getInstance()

        // Test millennium start
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val millenniumStart = calendar.time

        // Test millennium end
        calendar.set(2999, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val millenniumEnd = calendar.time

        // When
        val tripStart =
            Trip(
                date = millenniumStart,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripEnd =
            Trip(
                date = millenniumEnd,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(millenniumStart, tripStart.date)
        assertEquals(millenniumEnd, tripEnd.date)
    }

    @Test
    fun `trip with date at time zone offset boundaries handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC+14:00"), // Maximum positive offset
                TimeZone.getTimeZone("UTC-12:00"), // Maximum negative offset
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            val date = calendar.time

            // When
            val trip =
                Trip(
                    date = date,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(date, trip.date)
        }
    }

    @Test
    fun `trip with date at leap second handles correctly`() {
        // Given
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2023, Calendar.DECEMBER, 31, 23, 59, 60) // Leap second
        val leapSecondDate = calendar.time

        // When
        val trip =
            Trip(
                date = leapSecondDate,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(leapSecondDate, trip.date)
    }

    @Test
    fun `trip with date at epoch boundaries handles correctly`() {
        // Given
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        // Test Unix epoch start
        calendar.set(1970, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val epochStart = calendar.time

        // Test far future date
        calendar.set(9999, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val farFuture = calendar.time

        // When
        val tripStart =
            Trip(
                date = epochStart,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripFuture =
            Trip(
                date = farFuture,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(epochStart, tripStart.date)
        assertEquals(farFuture, tripFuture.date)
    }

    @Test
    fun `trip with zero dispatched miles and non-zero actual miles handles correctly`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 0.0
        val actualMiles = 100.0

        // When
        val trip =
            Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
            )

        // Then
        assertEquals(0.0, trip.dispatchedMiles, delta)
        assertEquals(actualMiles, trip.actualMiles, delta)
        assertEquals(100.0, trip.oorMiles, delta)
        assertEquals(0.0, trip.oorPercentage, delta) // Should be 0% when dispatched miles is 0
    }

    @Test
    fun `trip with zero actual miles and non-zero dispatched miles throws exception`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 0.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with zero actual miles and zero dispatched miles throws exception`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 0.0
        val actualMiles = 0.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with very small decimal values throws exception`() {
        // Given
        val loadedMiles = 0.000001
        val bounceMiles = 0.000001
        val actualMiles = 0.000003

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("Actual miles must be at least") == true)
    }

    @Test
    fun `trip with very large decimal values throws exception`() {
        // Given
        val loadedMiles = 999999.999999
        val bounceMiles = 999999.999999
        val actualMiles = 2000000.0

        // When & Then
        val exception =
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                Trip(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        org.junit.Assert.assertTrue(exception.message?.contains("seems unrealistic") == true)
    }

    @Test
    fun `trip with date at time zone transition handles correctly`() {
        // Given
        val timeZone = TimeZone.getTimeZone("America/New_York")
        val calendar = Calendar.getInstance(timeZone)

        // Set to time zone transition date (first Sunday in November)
        calendar.set(2024, Calendar.NOVEMBER, 3, 1, 59, 59)
        val beforeTransition = calendar.time

        calendar.add(Calendar.SECOND, 1)
        val duringTransition = calendar.time

        // When
        val tripBefore =
            Trip(
                date = beforeTransition,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        val tripDuring =
            Trip(
                date = duringTransition,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 500.0,
            )

        // Then
        assertEquals(beforeTransition, tripBefore.date)
        assertEquals(duringTransition, tripDuring.date)
    }

    @Test
    fun `trip with date at international date line handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("Pacific/Auckland"), // UTC+12
                TimeZone.getTimeZone("Pacific/Honolulu"), // UTC-10
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            val date = calendar.time

            // When
            val trip =
                Trip(
                    date = date,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(date, trip.date)
        }
    }

    @Test
    fun `trip with date at year end in different time zones handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC"),
                TimeZone.getTimeZone("America/New_York"),
                TimeZone.getTimeZone("Asia/Tokyo"),
                TimeZone.getTimeZone("Australia/Sydney"),
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            calendar.set(2024, Calendar.DECEMBER, 31, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val yearEnd = calendar.time

            // When
            val trip =
                Trip(
                    date = yearEnd,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(yearEnd, trip.date)
        }
    }

    @Test
    fun `trip with date at year start in different time zones handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC"),
                TimeZone.getTimeZone("America/New_York"),
                TimeZone.getTimeZone("Asia/Tokyo"),
                TimeZone.getTimeZone("Australia/Sydney"),
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val yearStart = calendar.time

            // When
            val trip =
                Trip(
                    date = yearStart,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(yearStart, trip.date)
        }
    }

    @Test
    fun `trip with date at month end in different time zones handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC"),
                TimeZone.getTimeZone("America/New_York"),
                TimeZone.getTimeZone("Asia/Tokyo"),
                TimeZone.getTimeZone("Australia/Sydney"),
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            calendar.set(2024, Calendar.JANUARY, 31, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val monthEnd = calendar.time

            // When
            val trip =
                Trip(
                    date = monthEnd,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(monthEnd, trip.date)
        }
    }

    @Test
    fun `trip with date at month start in different time zones handles correctly`() {
        // Given
        val timeZones =
            listOf(
                TimeZone.getTimeZone("UTC"),
                TimeZone.getTimeZone("America/New_York"),
                TimeZone.getTimeZone("Asia/Tokyo"),
                TimeZone.getTimeZone("Australia/Sydney"),
            )

        timeZones.forEach { timeZone ->
            val calendar = Calendar.getInstance(timeZone)
            calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.time

            // When
            val trip =
                Trip(
                    date = monthStart,
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 500.0,
                )

            // Then
            assertEquals(monthStart, trip.date)
        }
    }
} 
