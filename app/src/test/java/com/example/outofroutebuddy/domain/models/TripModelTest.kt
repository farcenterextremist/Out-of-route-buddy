package com.example.outofroutebuddy.domain.models

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Trip Domain Model Tests
 * 
 * Unit tests for Trip domain model validation and business logic.
 * 
 * Priority: MEDIUM
 * Coverage Target: 80%
 * 
 * Created: December 2024
 */
class TripModelTest {
    
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    
    @Before
    fun setup() {
        startDate = Date()
        endDate = Date(startDate.time + 3600000) // 1 hour later
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Test
    fun `trip with valid data should be created successfully`() {
        // Given: Valid trip data
        val trip = Trip(
            id = "test-123",
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            oorMiles = -10.0,
            oorPercentage = -8.33,
            startTime = startDate,
            endTime = endDate,
            status = TripStatus.COMPLETED
        )
        
        // Then: Trip should be created
        assertNotNull("Trip should be created", trip)
        assertEquals(100.0, trip.loadedMiles, 0.001)
        assertEquals(20.0, trip.bounceMiles, 0.001)
    }
    
    @Test
    fun `trip should reject NaN values`() {
        // Given: Trip with NaN values
        try {
            Trip(
                loadedMiles = Double.NaN,
                bounceMiles = 20.0,
                actualMiles = 110.0
            )
            fail("Should throw IllegalArgumentException for NaN")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention NaN", e.message?.contains("NaN") == true)
        }
    }
    
    @Test
    fun `trip should reject infinite values`() {
        // Given: Trip with infinite values
        try {
            Trip(
                loadedMiles = Double.POSITIVE_INFINITY,
                bounceMiles = 20.0,
                actualMiles = 110.0
            )
            fail("Should throw IllegalArgumentException for infinite")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention infinite", e.message?.contains("infinite", ignoreCase = true) == true)
        }
    }
    
    @Test
    fun `trip should reject epsilon values for loaded miles`() {
        // Given: Trip with epsilon loaded miles
        try {
            Trip(
                loadedMiles = 0.0005,
                bounceMiles = 20.0,
                actualMiles = 110.0
            )
            fail("Should throw IllegalArgumentException for epsilon loaded miles")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention loaded miles", e.message?.contains("Loaded miles") == true)
        }
    }
    
    @Test
    fun `trip should reject epsilon values for bounce miles`() {
        // Given: Trip with epsilon bounce miles
        try {
            Trip(
                loadedMiles = 100.0,
                bounceMiles = 0.0005,
                actualMiles = 110.0
            )
            fail("Should throw IllegalArgumentException for epsilon bounce miles")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention bounce miles", e.message?.contains("Bounce miles") == true)
        }
    }
    
    @Test
    fun `trip should reject epsilon values for actual miles`() {
        // Given: Trip with epsilon actual miles
        try {
            Trip(
                loadedMiles = 100.0,
                bounceMiles = 20.0,
                actualMiles = 0.0005
            )
            fail("Should throw IllegalArgumentException for epsilon actual miles")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention actual miles", e.message?.contains("Actual miles") == true)
        }
    }
    
    @Test
    fun `trip should allow zero miles for active trips`() {
        // Given: Active trip with zero miles
        val trip = Trip(
            loadedMiles = 0.0,
            bounceMiles = 0.0,
            actualMiles = 0.0,
            status = TripStatus.ACTIVE
        )
        
        // Then: Trip should be created
        assertNotNull("Active trip should be created with zero miles", trip)
        assertEquals(TripStatus.ACTIVE, trip.status)
    }
    
    @Test
    fun `trip should enforce positive miles for completed trips`() {
        // Given: Completed trip with zero actual miles
        try {
            Trip(
                loadedMiles = 100.0,
                bounceMiles = 20.0,
                actualMiles = 0.0,
                status = TripStatus.COMPLETED
            )
            fail("Should throw IllegalArgumentException for completed trip with zero miles")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention completed trip", e.message?.contains("completed", ignoreCase = true) == true)
        }
    }
    
    @Test
    fun `trip should reject negative bounce miles`() {
        // Given: Trip with negative bounce miles
        try {
            Trip(
                loadedMiles = 100.0,
                bounceMiles = -5.0,
                actualMiles = 110.0
            )
            fail("Should throw IllegalArgumentException for negative bounce miles")
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue("Error should mention negative", e.message?.contains("negative", ignoreCase = true) == true)
        }
    }
    
    // ==================== CALCULATED PROPERTIES TESTS ====================
    
    @Test
    fun `dispatched miles should be sum of loaded and bounce`() {
        // Given: Trip
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0
        )
        
        // Then: Dispatched miles should be correct
        assertEquals(120.0, trip.dispatchedMiles, 0.001)
    }
    
    @Test
    fun `isValid should return true for valid trip`() {
        // Given: Valid trip
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0
        )
        
        // Then: Should be valid
        assertTrue("Trip should be valid", trip.isValid)
    }
    
    @Test
    fun `isValid should return false for invalid trip`() {
        // Given: Invalid trip
        val trip = Trip(
            loadedMiles = 0.0,
            bounceMiles = 20.0,
            actualMiles = 110.0
        )
        
        // Then: Should not be valid
        assertFalse("Trip should not be valid with zero loaded miles", trip.isValid)
    }
    
    @Test
    fun `isActive should return true for active trip`() {
        // Given: Active trip
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            status = TripStatus.ACTIVE
        )
        
        // Then: Should be active
        assertTrue("Trip should be active", trip.isActive)
    }
    
    @Test
    fun `isActive should return false for inactive trip`() {
        // Given: Completed trip
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            status = TripStatus.COMPLETED
        )
        
        // Then: Should not be active
        assertFalse("Trip should not be active when completed", trip.isActive)
    }
    
    @Test
    fun `duration minutes should calculate correctly`() {
        // Given: Trip with start and end time
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            startTime = startDate,
            endTime = endDate
        )
        
        // Then: Duration should be 60 minutes (1 hour)
        assertEquals(60, trip.durationMinutes)
    }
    
    @Test
    fun `duration minutes should return zero when times are null`() {
        // Given: Trip without times
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0
        )
        
        // Then: Duration should be zero
        assertEquals(0, trip.durationMinutes)
    }
    
    // ==================== WITH METHODS TESTS ====================
    
    @Test
    fun `withUpdatedOorCalculations should update OOR metrics`() {
        // Given: Trip with initial OOR
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            oorMiles = 0.0,
            oorPercentage = 0.0
        )
        
        // When: Update OOR calculations
        val updated = trip.withUpdatedOorCalculations()
        
        // Then: OOR should be updated
        assertEquals(-10.0, updated.oorMiles, 0.01)
        assertEquals(-8.33, updated.oorPercentage, 0.1)
    }
    
    @Test
    fun `withUpdatedGpsMetadata should update metadata`() {
        // Given: Trip with initial metadata
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0
        )
        
        // When: Update GPS metadata
        val newMetadata = GpsMetadata(
            totalPoints = 100,
            validPoints = 95,
            rejectedPoints = 5,
            avgAccuracy = 15.5
        )
        val updated = trip.withUpdatedGpsMetadata(newMetadata)
        
        // Then: Metadata should be updated
        assertEquals(100, updated.gpsMetadata.totalPoints)
        assertEquals(95, updated.gpsMetadata.validPoints)
        assertEquals(15.5, updated.gpsMetadata.avgAccuracy, 0.01)
    }
    
    @Test
    fun `withStatus should update status`() {
        // Given: Trip with initial status
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            status = TripStatus.PENDING
        )
        
        // When: Update status
        val updated = trip.withStatus(TripStatus.ACTIVE)
        
        // Then: Status should be updated
        assertEquals(TripStatus.ACTIVE, updated.status)
        assertEquals(TripStatus.PENDING, trip.status) // Original unchanged
    }
    
    @Test
    fun `withEndTime should update end time`() {
        // Given: Trip without end time
        val trip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            startTime = startDate
        )
        
        // When: Set end time
        val updated = trip.withEndTime(endDate)
        
        // Then: End time should be set
        assertEquals(endDate, updated.endTime)
        assertNull(trip.endTime) // Original unchanged
    }

    @Test
    fun `copyForEdit returns copy with same dataTier`() {
        val trip = Trip(
            id = "1",
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            dataTier = DataTier.GOLD
        )
        val copy = trip.copyForEdit()
        assertNotSame(trip, copy)
        assertEquals(trip.dataTier, copy.dataTier)
        assertEquals(DataTier.GOLD, copy.dataTier)
        assertEquals("1", copy.id)
    }

    @Test
    fun `copyForEdit with newId uses new id`() {
        val trip = Trip(
            id = "1",
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            dataTier = DataTier.PLATINUM
        )
        val copy = trip.copyForEdit(newId = "2")
        assertEquals("2", copy.id)
        assertEquals(DataTier.PLATINUM, copy.dataTier)
    }
    
    // ==================== GPS METADATA TESTS ====================
    
    @Test
    fun `gps quality percentage should calculate correctly`() {
        // Given: GPS metadata
        val metadata = GpsMetadata(
            totalPoints = 100,
            validPoints = 80,
            rejectedPoints = 20
        )
        
        // Then: Quality should be 80%
        assertEquals(80.0, metadata.calculatedGpsQuality, 0.01)
    }
    
    @Test
    fun `gps quality percentage with zero total points should return zero`() {
        // Given: GPS metadata with no points
        val metadata = GpsMetadata(
            totalPoints = 0,
            validPoints = 0,
            rejectedPoints = 0
        )
        
        // Then: Quality should be zero
        assertEquals(0.0, metadata.calculatedGpsQuality, 0.01)
    }
    
    @Test
    fun `gps metadata should be reliable with good accuracy and quality`() {
        // Given: Good GPS metadata with high quality percentage
        val metadata = GpsMetadata(
            totalPoints = 100,
            validPoints = 95,
            rejectedPoints = 5,
            avgAccuracy = 15.0,
            gpsQualityPercentage = 95.0
        )
        
        // Then: Should be reliable
        assertTrue("GPS should be reliable", metadata.isReliable)
    }
    
    @Test
    fun `gps metadata should not be reliable with poor quality`() {
        // Given: Poor quality metadata
        val metadata = GpsMetadata(
            totalPoints = 100,
            validPoints = 50,
            rejectedPoints = 50,
            avgAccuracy = 15.0
        )
        
        // Then: Should not be reliable
        assertFalse("GPS should not be reliable with poor quality", metadata.isReliable)
    }
    
    @Test
    fun `gps metadata should not be reliable with poor accuracy`() {
        // Given: Poor accuracy metadata
        val metadata = GpsMetadata(
            totalPoints = 100,
            validPoints = 95,
            rejectedPoints = 5,
            avgAccuracy = 30.0
        )
        
        // Then: Should not be reliable
        assertFalse("GPS should not be reliable with poor accuracy", metadata.isReliable)
    }
}

