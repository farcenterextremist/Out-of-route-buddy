package com.example.outofroutebuddy.services

import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Comprehensive tests for TripCalculationService to ensure all business logic works correctly.
 */
class TripCalculationServiceTest {
    private val service = TripCalculationService()

    @Test
    fun `calculateOORMiles with valid inputs returns correct value`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 160.0

        // When
        val oorMiles = service.calculateOORMiles(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(10.0, oorMiles, 0.01)
    }

    @Test
    fun `calculateOORMiles with negative OOR returns negative value`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 140.0 // Less than dispatched

        // When
        val oorMiles = service.calculateOORMiles(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(-10.0, oorMiles, 0.01)
    }

    @Test
    fun `calculateOORMiles with zero values returns correct value`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 0.0
        val actualMiles = 10.0

        // When
        val oorMiles = service.calculateOORMiles(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(10.0, oorMiles, 0.01)
    }

    @Test
    fun `calculateDispatchedMiles returns sum of loaded and bounce`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0

        // When
        val dispatchedMiles = service.calculateDispatchedMiles(loadedMiles, bounceMiles)

        // Then
        assertEquals(150.0, dispatchedMiles, 0.01)
    }

    @Test
    fun `calculateOORPercentage with valid inputs returns correct percentage`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 160.0

        // When
        val oorPercentage = service.calculateOORPercentage(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(6.67, oorPercentage, 0.01) // (10/150) * 100
    }

    @Test
    fun `calculateOORPercentage with zero dispatched miles returns zero`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 0.0
        val actualMiles = 10.0

        // When
        val oorPercentage = service.calculateOORPercentage(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(0.0, oorPercentage, 0.01)
    }

    @Test
    fun `calculateOORPercentage with negative OOR returns negative percentage`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 140.0

        // When
        val oorPercentage = service.calculateOORPercentage(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(-6.67, oorPercentage, 0.01) // (-10/150) * 100
    }

    @Test
    fun `calculateAverageOORPercentage with valid percentages returns correct average`() {
        // Given
        val percentages = listOf(10.0, 20.0, 30.0)

        // When
        val average = service.calculateAverageOORPercentage(percentages)

        // Then
        assertEquals(20.0, average, 0.01)
    }

    @Test
    fun `calculateAverageOORPercentage with empty list returns zero`() {
        // Given
        val percentages = emptyList<Double>()

        // When
        val average = service.calculateAverageOORPercentage(percentages)

        // Then
        assertEquals(0.0, average, 0.01)
    }

    @Test
    fun `calculateAverageOORPercentage with NaN values filters them out`() {
        // Given
        val percentages = listOf(10.0, Double.NaN, 30.0, Double.NaN)

        // When
        val average = service.calculateAverageOORPercentage(percentages)

        // Then
        assertEquals(20.0, average, 0.01) // (10 + 30) / 2
    }

    @Test
    fun `calculateAverageOORPercentage with all NaN values returns zero`() {
        // Given
        val percentages = listOf(Double.NaN, Double.NaN, Double.NaN)

        // When
        val average = service.calculateAverageOORPercentage(percentages)

        // Then
        assertEquals(0.0, average, 0.01)
    }

    @Test
    fun `validateTripInput with valid inputs returns success`() {
        // Given
        val loadedMiles = "100.0"
        val bounceMiles = "50.0"
        val actualMiles = "160.0"

        // When
        val result = service.validateTripInput(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertTrue(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateTripInput with invalid loaded miles returns error`() {
        // Given
        val loadedMiles = "invalid"
        val bounceMiles = "50.0"
        val actualMiles = "160.0"

        // When
        val result = service.validateTripInput(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertFalse(result.first)
        assertEquals("Invalid loaded miles", result.second)
    }

    @Test
    fun `validateTripInput with zero loaded miles returns error`() {
        // Given
        val loadedMiles = "0.0"
        val bounceMiles = "50.0"
        val actualMiles = "160.0"

        // When
        val result = service.validateTripInput(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertFalse(result.first)
        assertEquals("Loaded miles must be greater than 0", result.second)
    }

    @Test
    fun `validateTripInput with negative bounce miles returns error`() {
        // Given
        val loadedMiles = "100.0"
        val bounceMiles = "-10.0"
        val actualMiles = "160.0"

        // When
        val result = service.validateTripInput(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertFalse(result.first)
        assertEquals("Bounce miles cannot be negative", result.second)
    }

    @Test
    fun `validateTripInput with negative actual miles returns error`() {
        // Given
        val loadedMiles = "100.0"
        val bounceMiles = "50.0"
        val actualMiles = "-10.0"

        // When
        val result = service.validateTripInput(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertFalse(result.first)
        assertEquals("Actual miles cannot be negative", result.second)
    }

    @Test
    fun `calculateTotalMiles from trip list returns correct sum`() {
        // Given
        val trips =
            listOf(
                TripCalculationService.TripData(100.0, 50.0, 160.0),
                TripCalculationService.TripData(200.0, 100.0, 320.0),
            )

        // When
        val totalMiles = service.calculateTotalMiles(trips)

        // Then
        assertEquals(480.0, totalMiles, 0.01)
    }

    @Test
    fun `calculateTotalOORMiles from trip list returns correct sum`() {
        // Given
        val trips =
            listOf(
                TripCalculationService.TripData(100.0, 50.0, 160.0), // 10 OOR miles
                TripCalculationService.TripData(200.0, 100.0, 320.0), // 20 OOR miles
            )

        // When
        val totalOORMiles = service.calculateTotalOORMiles(trips)

        // Then
        assertEquals(30.0, totalOORMiles, 0.01)
    }

    @Test
    fun `calculateAverageOORPercentageFromTrips returns correct average`() {
        // Given
        val trips =
            listOf(
                TripCalculationService.TripData(100.0, 50.0, 160.0), // 6.67%
                TripCalculationService.TripData(200.0, 100.0, 320.0), // 6.67%
            )

        // When
        val average = service.calculateAverageOORPercentageFromTrips(trips)

        // Then
        assertEquals(6.67, average, 0.01)
    }

    @Test
    fun `calculateAverageOORPercentageFromTrips with empty list returns zero`() {
        // Given
        val trips = emptyList<TripCalculationService.TripData>()

        // When
        val average = service.calculateAverageOORPercentageFromTrips(trips)

        // Then
        assertEquals(0.0, average, 0.01)
    }

    @Test
    fun `edge case with very large numbers handles correctly`() {
        // Given
        val loadedMiles = Double.MAX_VALUE
        val bounceMiles = Double.MAX_VALUE
        val actualMiles = Double.MAX_VALUE * 2

        // When
        val oorMiles = service.calculateOORMiles(loadedMiles, bounceMiles, actualMiles)
        val oorPercentage = service.calculateOORPercentage(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertTrue(oorMiles.isInfinite() || oorMiles.isNaN())
        assertTrue(oorPercentage.isInfinite() || oorPercentage.isNaN())
    }

    @Test
    fun `edge case with very small numbers handles correctly`() {
        // Given
        val loadedMiles = Double.MIN_VALUE
        val bounceMiles = Double.MIN_VALUE
        val actualMiles = Double.MIN_VALUE * 2

        // When
        val oorMiles = service.calculateOORMiles(loadedMiles, bounceMiles, actualMiles)
        val oorPercentage = service.calculateOORPercentage(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(0.0, oorMiles, 0.01)
        assertEquals(0.0, oorPercentage, 0.01)
    }

    @Test
    fun `TripData creation with all parameters works correctly`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 160.0
        val date = Date()

        // When
        val tripData = TripCalculationService.TripData(loadedMiles, bounceMiles, actualMiles, date)

        // Then
        assertEquals(loadedMiles, tripData.loadedMiles, 0.01)
        assertEquals(bounceMiles, tripData.bounceMiles, 0.01)
        assertEquals(actualMiles, tripData.actualMiles, 0.01)
        assertEquals(date, tripData.date)
    }

    @Test
    fun `TripData creation with default date works correctly`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 160.0

        // When
        val tripData = TripCalculationService.TripData(loadedMiles, bounceMiles, actualMiles)

        // Then
        assertEquals(loadedMiles, tripData.loadedMiles, 0.01)
        assertEquals(bounceMiles, tripData.bounceMiles, 0.01)
        assertEquals(actualMiles, tripData.actualMiles, 0.01)
        assertNotNull(tripData.date)
    }
} 
