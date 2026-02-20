package com.example.outofroutebuddy.domain.usecase

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * CalculateTripOorUseCase Tests - Week 4
 * 
 * Unit tests for OOR calculation business logic.
 * 
 * Priority: HIGH
 * Coverage Target: 80%
 * 
 * Created: December 2024
 */
class CalculateTripOorUseCaseTest {
    
    private lateinit var useCase: CalculateTripOorUseCase
    
    @Before
    fun setup() {
        useCase = CalculateTripOorUseCase()
    }
    
    // ==================== TASK 4.1: BASIC CALCULATION TESTS ====================
    
    @Test
    fun `calculate OOR with positive result`() {
        // Given: Standard trip with actual > dispatched
        val loaded = 100.0
        val bounce = 20.0
        val actual = 125.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: OOR should be positive
        assertEquals(5.0, trip.oorMiles, 0.01)
        assertEquals(4.17, trip.oorPercentage, 0.1)
    }
    
    @Test
    fun `calculate OOR with negative result`() {
        // Given: Trip where actual < dispatched
        val loaded = 100.0
        val bounce = 20.0
        val actual = 110.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: OOR should be negative
        assertEquals(-10.0, trip.oorMiles, 0.01)
        assertEquals(-8.33, trip.oorPercentage, 0.1)
    }
    
    @Test
    fun `calculate OOR with zero result`() {
        // Given: Trip where actual = dispatched
        val loaded = 100.0
        val bounce = 20.0
        val actual = 120.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: OOR should be zero
        assertEquals(0.0, trip.oorMiles, 0.01)
        assertEquals(0.0, trip.oorPercentage, 0.01)
    }
    
    @Test
    fun `calculate OOR with zero bounce miles`() {
        // Given: Trip with only loaded miles
        val loaded = 100.0
        val bounce = 0.0
        val actual = 105.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: OOR should be calculated correctly
        assertEquals(5.0, trip.oorMiles, 0.01)
        assertEquals(5.0, trip.oorPercentage, 0.01)
    }
    
    @Test
    fun `calculate OOR with division by zero protection`() {
        // Given: Trip with zero dispatched miles
        val loaded = 0.0
        val bounce = 0.0
        val actual = 50.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: Percentage should be zero to avoid division by zero
        assertEquals(50.0, trip.oorMiles, 0.01)
        assertEquals(0.0, trip.oorPercentage, 0.01)
    }
    
    @Test
    fun `execute with existing trip should calculate OOR`() {
        // Given: Existing trip
        val existingTrip = Trip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 110.0,
            status = TripStatus.ACTIVE
        )
        
        // When: Execute calculation
        val updatedTrip = useCase.execute(existingTrip)
        
        // Then: OOR should be calculated
        assertEquals(-10.0, updatedTrip.oorMiles, 0.01)
        assertEquals(-8.33, updatedTrip.oorPercentage, 0.1)
        assertEquals(TripStatus.PENDING, updatedTrip.status) // Should be reset to pending
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Test
    fun `validate input with valid data should return empty list`() {
        // Given: Valid input
        val loaded = 100.0
        val bounce = 20.0
        val actual = 120.0
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: No issues
        assertTrue("Should have no validation issues", issues.isEmpty())
    }
    
    @Test
    fun `validate input with zero loaded miles should return error`() {
        // Given: Zero loaded miles
        val loaded = 0.0
        val bounce = 20.0
        val actual = 120.0
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: Should have error
        assertTrue("Should have validation issue", issues.isNotEmpty())
        assertTrue("Should mention loaded miles", issues.any { it.contains("Loaded miles", ignoreCase = true) })
    }
    
    @Test
    fun `validate input with negative bounce miles should return error`() {
        // Given: Negative bounce miles
        val loaded = 100.0
        val bounce = -5.0
        val actual = 120.0
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: Should have error
        assertTrue("Should have validation issue", issues.isNotEmpty())
        assertTrue("Should mention bounce miles", issues.any { it.contains("bounce", ignoreCase = true) })
    }
    
    @Test
    fun `validate input with zero actual miles should return error`() {
        // Given: Zero actual miles
        val loaded = 100.0
        val bounce = 20.0
        val actual = 0.0
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: Should have error
        assertTrue("Should have validation issue", issues.isNotEmpty())
        assertTrue("Should mention actual miles", issues.any { it.contains("Actual miles", ignoreCase = true) })
    }
    
    @Test
    fun `validate input with dispatched exceeding actual should return error`() {
        // Given: Dispatched miles exceed actual miles
        val loaded = 100.0
        val bounce = 20.0
        val actual = 50.0
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: Should have error
        assertTrue("Should have validation issue", issues.isNotEmpty())
        assertTrue("Should mention dispatched miles", issues.any { it.contains("dispatched", ignoreCase = true) })
    }
    
    // ==================== EFFICIENCY RATING TESTS ====================
    
    @Test
    fun `get efficiency rating for excellent should return Excellent`() {
        // Given: Low OOR percentage
        val oorPercentage = 3.0
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Excellent
        assertEquals("Excellent", rating)
    }
    
    @Test
    fun `get efficiency rating for good should return Good`() {
        // Given: Good OOR percentage
        val oorPercentage = 8.0
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Good
        assertEquals("Good", rating)
    }
    
    @Test
    fun `get efficiency rating for fair should return Fair`() {
        // Given: Fair OOR percentage
        val oorPercentage = 12.0
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Fair
        assertEquals("Fair", rating)
    }
    
    @Test
    fun `get efficiency rating for poor should return Poor`() {
        // Given: Poor OOR percentage
        val oorPercentage = 20.0
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Poor
        assertEquals("Poor", rating)
    }
    
    @Test
    fun `get efficiency rating for very poor should return Very Poor`() {
        // Given: Very poor OOR percentage
        val oorPercentage = 30.0
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Very Poor
        assertEquals("Very Poor", rating)
    }
    
    @Test
    fun `get efficiency rating at boundary 5 percent should return Good`() {
        // Given: Exact boundary
        val oorPercentage = 5.01
        
        // When: Get rating
        val rating = useCase.getEfficiencyRating(oorPercentage)
        
        // Then: Should be Good (not Excellent)
        assertEquals("Good", rating)
    }
    
    // ==================== COST IMPACT TESTS ====================
    
    @Test
    fun `calculate cost impact with default rate should return correct value`() {
        // Given: OOR miles
        val oorMiles = 10.0
        
        // When: Calculate cost
        val cost = useCase.calculateCostImpact(oorMiles)
        
        // Then: Should be $5.00 (10 * 0.50)
        assertEquals(5.0, cost, 0.01)
    }
    
    @Test
    fun `calculate cost impact with custom rate should return correct value`() {
        // Given: OOR miles and custom rate
        val oorMiles = 20.0
        val customRate = 0.75
        
        // When: Calculate cost
        val cost = useCase.calculateCostImpact(oorMiles, customRate)
        
        // Then: Should be $15.00 (20 * 0.75)
        assertEquals(15.0, cost, 0.01)
    }
    
    @Test
    fun `calculate cost impact with negative OOR should return negative cost`() {
        // Given: Negative OOR miles
        val oorMiles = -5.0
        
        // When: Calculate cost
        val cost = useCase.calculateCostImpact(oorMiles)
        
        // Then: Should be negative (saving money)
        assertEquals(-2.5, cost, 0.01)
    }
    
    @Test
    fun `calculate cost impact with zero OOR should return zero cost`() {
        // Given: Zero OOR miles
        val oorMiles = 0.0
        
        // When: Calculate cost
        val cost = useCase.calculateCostImpact(oorMiles)
        
        // Then: Should be zero
        assertEquals(0.0, cost, 0.01)
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun `calculate OOR with very large values should work correctly`() {
        // Given: Very large trip
        val loaded = 5000.0
        val bounce = 200.0
        val actual = 5500.0
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: Should calculate correctly
        assertEquals(300.0, trip.oorMiles, 0.01)
        assertEquals(5.77, trip.oorPercentage, 0.1)
    }
    
    @Test
    fun `calculate OOR with decimal precision should be accurate`() {
        // Given: Decimal values
        val loaded = 100.5
        val bounce = 20.3
        val actual = 125.8
        
        // When: Calculate OOR
        val trip = useCase.execute(loaded, bounce, actual)
        
        // Then: Should preserve precision
        assertEquals(5.0, trip.oorMiles, 0.01)
        assertEquals(4.14, trip.oorPercentage, 0.01)
    }
    
    @Test
    fun `validate multiple issues should return all issues`() {
        // Given: Multiple invalid inputs
        val loaded = -10.0  // Negative loaded
        val bounce = -5.0    // Negative bounce
        val actual = 0.0     // Zero actual
        
        // When: Validate
        val issues = useCase.validateInput(loaded, bounce, actual)
        
        // Then: Should have multiple issues
        assertTrue("Should have multiple issues", issues.size > 1)
    }
}





