package com.example.outofroutebuddy.data.entities

import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Tests for TripEntity to ensure proper entity behavior and calculations.
 */
class TripEntityTest {
    @Test
    fun `trip entity creation with all parameters`() {
        // Given
        val date = Date()
        val loadedMiles = 100.0
        val bounceMiles = 50.0
        val actualMiles = 160.0
        val oorMiles = 10.0
        val oorPercentage = 6.67

        // When
        val trip =
            TripEntity(
                id = 1L,
                date = date,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
            )

        // Then
        assertEquals(1L, trip.id)
        assertEquals(date, trip.date)
        assertEquals(loadedMiles, trip.loadedMiles, 0.01)
        assertEquals(bounceMiles, trip.bounceMiles, 0.01)
        assertEquals(actualMiles, trip.actualMiles, 0.01)
        assertEquals(oorMiles, trip.oorMiles, 0.01)
        assertEquals(oorPercentage, trip.oorPercentage, 0.01)
    }

    @Test
    fun `trip entity creation with default id`() {
        // Given
        val date = Date()

        // When
        val trip =
            TripEntity(
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // Then
        assertEquals(0L, trip.id) // Default value
        assertNotNull(trip.createdAt)
    }

    @Test
    fun `dispatchedMiles calculation is correct`() {
        // Given
        val loadedMiles = 100.0
        val bounceMiles = 50.0

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // Then
        assertEquals(150.0, trip.dispatchedMiles, 0.01)
    }

    @Test
    fun `dispatchedMiles with zero values`() {
        // Given
        val loadedMiles = 0.0
        val bounceMiles = 0.0

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = 0.0,
                oorMiles = 0.0,
                oorPercentage = 0.0,
            )

        // Then
        assertEquals(0.0, trip.dispatchedMiles, 0.01)
    }

    @Test
    fun `dispatchedMiles with decimal values`() {
        // Given
        val loadedMiles = 100.5
        val bounceMiles = 25.75

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = 130.0,
                oorMiles = 3.75,
                oorPercentage = 2.97,
            )

        // Then
        assertEquals(126.25, trip.dispatchedMiles, 0.01)
    }

    @Test
    fun `trip entity equality`() {
        // Given
        val date = Date()
        val trip1 =
            TripEntity(
                id = 1L,
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        val trip2 =
            TripEntity(
                id = 1L,
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // Then
        assertEquals("Trip entities should be equal", trip1, trip2)
        assertEquals("Hash codes should be equal", trip1.hashCode(), trip2.hashCode())
    }

    @Test
    fun `trip entity inequality with different id`() {
        // Given
        val date = Date()
        val trip1 =
            TripEntity(
                id = 1L,
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        val trip2 =
            TripEntity(
                id = 2L,
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // Then
        assertNotEquals(trip1, trip2)
    }

    @Test
    fun `trip entity with negative values`() {
        // Given
        val loadedMiles = -10.0
        val bounceMiles = -5.0

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = 0.0,
                oorMiles = 15.0,
                oorPercentage = -100.0,
            )

        // Then
        assertEquals(-15.0, trip.dispatchedMiles, 0.01)
        assertEquals(loadedMiles, trip.loadedMiles, 0.01)
        assertEquals(bounceMiles, trip.bounceMiles, 0.01)
    }

    @Test
    fun `trip entity with very large values`() {
        // Given
        val loadedMiles = Double.MAX_VALUE
        val bounceMiles = Double.MAX_VALUE

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = Double.MAX_VALUE,
                oorMiles = 0.0,
                oorPercentage = 0.0,
            )

        // Then
        assertTrue(trip.dispatchedMiles.isInfinite())
        assertEquals(loadedMiles, trip.loadedMiles, 0.01)
        assertEquals(bounceMiles, trip.bounceMiles, 0.01)
    }

    @Test
    fun `trip entity with NaN values`() {
        // Given
        val loadedMiles = Double.NaN
        val bounceMiles = Double.NaN

        // When
        val trip =
            TripEntity(
                date = Date(),
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = Double.NaN,
                oorMiles = Double.NaN,
                oorPercentage = Double.NaN,
            )

        // Then
        assertTrue(trip.dispatchedMiles.isNaN())
        assertTrue(trip.loadedMiles.isNaN())
        assertTrue(trip.bounceMiles.isNaN())
    }

    @Test
    fun `trip entity toString contains all fields`() {
        // Given
        val date = Date()
        val trip =
            TripEntity(
                id = 1L,
                date = date,
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // When
        val toString = trip.toString()

        // Then
        assertTrue(toString.contains("id=1"))
        assertTrue(toString.contains("loadedMiles=100.0"))
        assertTrue(toString.contains("bounceMiles=50.0"))
        assertTrue(toString.contains("actualMiles=160.0"))
        assertTrue(toString.contains("oorMiles=10.0"))
        assertTrue(toString.contains("oorPercentage=6.67"))
    }

    @Test
    fun `trip entity copy preserves values`() {
        // Given
        val originalTrip =
            TripEntity(
                id = 1L,
                date = Date(),
                loadedMiles = 100.0,
                bounceMiles = 50.0,
                actualMiles = 160.0,
                oorMiles = 10.0,
                oorPercentage = 6.67,
            )

        // When
        val copiedTrip = originalTrip.copy(id = 2L, loadedMiles = 200.0)

        // Then
        assertEquals(2L, copiedTrip.id)
        assertEquals(200.0, copiedTrip.loadedMiles, 0.01)
        assertEquals(originalTrip.bounceMiles, copiedTrip.bounceMiles, 0.01)
        assertEquals(originalTrip.actualMiles, copiedTrip.actualMiles, 0.01)
        assertEquals(originalTrip.oorMiles, copiedTrip.oorMiles, 0.01)
        assertEquals(originalTrip.oorPercentage, copiedTrip.oorPercentage, 0.01)
        assertEquals(originalTrip.date, copiedTrip.date)
    }
} 
