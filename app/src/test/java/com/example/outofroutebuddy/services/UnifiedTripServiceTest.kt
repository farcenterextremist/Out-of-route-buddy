package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.utils.TestDataBuilders
import com.example.outofroutebuddy.utils.MockServices
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 UnifiedTripService Tests
 * 
 * Tests for UnifiedTripService functionality:
 * - Period calculations
 * - Trip statistics
 * - Period mode switching
 * - State management
 * 
 * Priority: 🟡 MEDIUM
 * Impact: Core business logic
 * 
 * Created: Phase 2A - Unified Services
 */
class UnifiedTripServiceTest {
    
    private lateinit var mockTripService: MockServices.MockUnifiedTripService
    
    @Before
    fun setup() {
        mockTripService = MockServices.UnifiedMockServiceFactory.createMockTripService()
    }
    
    // ==================== TRIP CALCULATION TESTS ====================
    
    @Test
    fun `test calculate trip with positive OOR`() {
        // Given
        val loaded = 100.0
        val bounce = 20.0
        val actual = 125.0
        
        // When
        val trip = mockTripService.calculateTrip(loaded, bounce, actual)
        
        // Then
        assertEquals(100.0, trip.loadedMiles, 0.001)
        assertEquals(20.0, trip.bounceMiles, 0.001)
        assertEquals(125.0, trip.actualMiles, 0.001)
        assertEquals(5.0, trip.oorMiles, 0.001) // 125 - (100 + 20) = 5
        assertEquals(4.17, trip.oorPercentage, 0.1) // (5 / 120) * 100 = 4.17%
    }
    
    @Test
    fun `test calculate trip with zero OOR`() {
        // Given: actual matches dispatched
        val trip = mockTripService.calculateTrip(100.0, 20.0, 120.0)
        
        // Then
        assertEquals(0.0, trip.oorMiles, 0.001)
        assertEquals(0.0, trip.oorPercentage, 0.001)
    }
    
    @Test
    fun `test calculate trip with negative OOR`() {
        // Given: actual less than dispatched
        val trip = mockTripService.calculateTrip(100.0, 20.0, 110.0)
        
        // Then
        assertEquals(-10.0, trip.oorMiles, 0.001)
        assertTrue("OOR percentage should be negative", trip.oorPercentage < 0)
    }
    
    @Test
    fun `test calculate trip handles zero dispatched miles`() {
        // Given: zero loaded and bounce
        val trip = mockTripService.calculateTrip(0.0, 0.0, 50.0)
        
        // Then
        assertEquals(50.0, trip.oorMiles, 0.001)
        assertEquals(0.0, trip.oorPercentage, 0.001) // Should handle division by zero
    }
    
    // ==================== PERIOD STATISTICS TESTS ====================
    
    @Test
    fun `test period statistics with no trips`() {
        // When: no trips recorded
        val stats = mockTripService.calculatePeriodStatistics()
        
        // Then
        assertEquals(0, stats.totalTrips)
        assertEquals(0.0, stats.totalDistance, 0.001)
        assertEquals(0.0, stats.totalOor, 0.001)
        assertEquals(0.0, stats.averageOorPercentage, 0.001)
    }
    
    @Test
    fun `test period statistics with single trip`() {
        // Given
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        
        // When
        val stats = mockTripService.calculatePeriodStatistics()
        
        // Then
        assertEquals(1, stats.totalTrips)
        assertEquals(125.0, stats.totalDistance, 0.001)
        assertEquals(5.0, stats.totalOor, 0.001)
        assertEquals(4.17, stats.averageOorPercentage, 0.1)
    }
    
    @Test
    fun `test period statistics with multiple trips`() {
        // Given
        mockTripService.calculateTrip(100.0, 20.0, 125.0) // 5 OOR
        mockTripService.calculateTrip(200.0, 30.0, 240.0) // 10 OOR
        mockTripService.calculateTrip(150.0, 25.0, 180.0) // 5 OOR
        
        // When
        val stats = mockTripService.calculatePeriodStatistics()
        
        // Then
        assertEquals(3, stats.totalTrips)
        assertEquals(545.0, stats.totalDistance, 0.001) // 125 + 240 + 180
        assertEquals(20.0, stats.totalOor, 0.001) // 5 + 10 + 5
    }
    
    @Test
    fun `test period statistics average calculation`() {
        // Given: trips with different OOR percentages
        mockTripService.calculateTrip(100.0, 0.0, 110.0) // 10%
        mockTripService.calculateTrip(100.0, 0.0, 120.0) // 20%
        mockTripService.calculateTrip(100.0, 0.0, 105.0) // 5%
        
        // When
        val stats = mockTripService.calculatePeriodStatistics()
        
        // Then
        assertEquals(3, stats.totalTrips)
        // Average should be around (10 + 20 + 5) / 3 = 11.67%
        assertEquals(11.67, stats.averageOorPercentage, 0.5)
    }
    
    // ==================== PERIOD MODE TESTS ====================
    
    @Test
    fun `test default period mode is STANDARD`() {
        val mode = mockTripService.getPeriodMode()
        assertEquals("STANDARD", mode)
    }
    
    @Test
    fun `test set period mode to CUSTOM`() {
        mockTripService.setPeriodMode("CUSTOM")
        assertEquals("CUSTOM", mockTripService.getPeriodMode())
    }
    
    @Test
    fun `test period mode switching`() {
        // Start with STANDARD
        assertEquals("STANDARD", mockTripService.getPeriodMode())
        
        // Switch to CUSTOM
        mockTripService.setPeriodMode("CUSTOM")
        assertEquals("CUSTOM", mockTripService.getPeriodMode())
        
        // Switch back to STANDARD
        mockTripService.setPeriodMode("STANDARD")
        assertEquals("STANDARD", mockTripService.getPeriodMode())
    }
    
    @Test
    fun `test period mode persists across calculations`() {
        // Set mode
        mockTripService.setPeriodMode("CUSTOM")
        
        // Calculate some trips
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        mockTripService.calculateTrip(150.0, 25.0, 180.0)
        
        // Mode should still be CUSTOM
        assertEquals("CUSTOM", mockTripService.getPeriodMode())
    }
    
    // ==================== TRIP HISTORY TESTS ====================
    
    @Test
    fun `test trip history starts empty`() {
        val history = mockTripService.getTripHistory()
        assertTrue("History should start empty", history.isEmpty())
    }
    
    @Test
    fun `test trip history accumulates trips`() {
        // Add trips
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        mockTripService.calculateTrip(150.0, 25.0, 180.0)
        
        // Verify history
        val history = mockTripService.getTripHistory()
        assertEquals(2, history.size)
    }
    
    @Test
    fun `test clear trip history`() {
        // Add trips
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        mockTripService.calculateTrip(150.0, 25.0, 180.0)
        
        // Clear
        mockTripService.clearHistory()
        
        // Verify empty
        val history = mockTripService.getTripHistory()
        assertTrue("History should be empty after clear", history.isEmpty())
    }
    
    @Test
    fun `test trip history order`() {
        // Add trips in order
        val trip1 = mockTripService.calculateTrip(100.0, 20.0, 125.0)
        Thread.sleep(10) // Ensure different timestamps
        val trip2 = mockTripService.calculateTrip(150.0, 25.0, 180.0)
        Thread.sleep(10)
        val trip3 = mockTripService.calculateTrip(200.0, 30.0, 240.0)
        
        // Get history
        val history = mockTripService.getTripHistory()
        
        // Verify order (should be chronological)
        assertEquals(3, history.size)
        assertTrue("Trips should be in order", history[0].timestamp <= history[1].timestamp)
        assertTrue("Trips should be in order", history[1].timestamp <= history[2].timestamp)
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test
    fun `test calculation fails when service is configured to fail`() {
        // Configure service to fail
        mockTripService.setShouldFail(true, "Test failure")
        
        // Should throw exception
        try {
            mockTripService.calculateTrip(100.0, 20.0, 125.0)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Test failure", e.message)
        }
    }
    
    @Test
    fun `test period statistics fails when service is configured to fail`() {
        // Add a trip first
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        
        // Configure to fail
        mockTripService.setShouldFail(true, "Stats failure")
        
        // Should throw exception
        try {
            mockTripService.calculatePeriodStatistics()
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Stats failure", e.message)
        }
    }
    
    @Test
    fun `test recovery after failure`() {
        // Fail once
        mockTripService.setShouldFail(true)
        try {
            mockTripService.calculateTrip(100.0, 20.0, 125.0)
        } catch (e: Exception) {
            // Expected
        }
        
        // Recover
        mockTripService.setShouldFail(false)
        
        // Should work now
        val trip = mockTripService.calculateTrip(100.0, 20.0, 125.0)
        assertNotNull(trip)
        assertEquals(5.0, trip.oorMiles, 0.001)
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    fun `test complete workflow with period statistics`() {
        // Set period mode
        mockTripService.setPeriodMode("CUSTOM")
        
        // Add multiple trips
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        mockTripService.calculateTrip(150.0, 25.0, 180.0)
        mockTripService.calculateTrip(200.0, 30.0, 240.0)
        
        // Calculate statistics
        val stats = mockTripService.calculatePeriodStatistics()
        
        // Verify everything
        assertEquals("CUSTOM", mockTripService.getPeriodMode())
        assertEquals(3, stats.totalTrips)
        assertEquals(545.0, stats.totalDistance, 0.001)
        assertEquals(20.0, stats.totalOor, 0.001)
        
        // Verify history
        val history = mockTripService.getTripHistory()
        assertEquals(3, history.size)
    }
    
    @Test
    fun `test statistics reset after clear history`() {
        // Add trips
        mockTripService.calculateTrip(100.0, 20.0, 125.0)
        mockTripService.calculateTrip(150.0, 25.0, 180.0)
        
        // Clear history
        mockTripService.clearHistory()
        
        // Statistics should reflect empty state
        val stats = mockTripService.calculatePeriodStatistics()
        assertEquals(0, stats.totalTrips)
        assertEquals(0.0, stats.totalDistance, 0.001)
        assertEquals(0.0, stats.totalOor, 0.001)
    }
}

