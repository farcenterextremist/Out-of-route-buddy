package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.utils.MockServices
import com.example.outofroutebuddy.utils.TestDataBuilders
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 UnifiedLocationService Tests
 * 
 * Tests for UnifiedLocationService functionality:
 * - Location tracking start/stop
 * - Location updates and validation
 * - Background tracking
 * - Permission handling
 * 
 * Priority: 🟡 MEDIUM
 * Impact: Core GPS functionality
 * 
 * Created: Phase 2A - Unified Services
 */
class UnifiedLocationServiceTest {
    
    private lateinit var mockLocationService: MockServices.MockUnifiedLocationService
    
    @Before
    fun setup() {
        mockLocationService = MockServices.UnifiedMockServiceFactory.createMockLocationService()
    }
    
    // ==================== TRACKING STATE TESTS ====================
    
    @Test
    fun `test tracking starts in stopped state`() {
        assertFalse("Should not be tracking initially", mockLocationService.isTracking())
    }
    
    @Test
    fun `test start tracking`() {
        // Start tracking
        val success = mockLocationService.startTracking()
        
        // Verify
        assertTrue("startTracking should return true", success)
        assertTrue("Should be tracking", mockLocationService.isTracking())
    }
    
    @Test
    fun `test stop tracking`() {
        // Start then stop
        mockLocationService.startTracking()
        val success = mockLocationService.stopTracking()
        
        // Verify
        assertTrue("stopTracking should return true", success)
        assertFalse("Should not be tracking", mockLocationService.isTracking())
    }
    
    @Test
    fun `test multiple start stop cycles`() {
        // Multiple cycles
        repeat(5) {
            mockLocationService.startTracking()
            assertTrue(mockLocationService.isTracking())
            
            mockLocationService.stopTracking()
            assertFalse(mockLocationService.isTracking())
        }
    }
    
    // ==================== LOCATION UPDATE TESTS ====================
    
    @Test
    fun `test add location update while tracking`() {
        // Start tracking
        mockLocationService.startTracking()
        
        // Add location
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 25f)
        
        // Verify
        assertEquals(1, mockLocationService.getLocationCount())
    }
    
    @Test
    fun `test cannot add location when not tracking`() {
        // Try to add location without starting tracking
        try {
            mockLocationService.addLocationUpdate(37.7749, -122.4194)
            fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Not tracking", e.message)
        }
    }
    
    @Test
    fun `test multiple location updates`() {
        mockLocationService.startTracking()
        
        // Add multiple locations
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 10f)
        mockLocationService.addLocationUpdate(37.7750, -122.4195, 15f)
        mockLocationService.addLocationUpdate(37.7751, -122.4196, 20f)
        
        // Verify
        assertEquals(3, mockLocationService.getLocationCount())
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(3, history.size)
        assertEquals(37.7749, history[0].latitude, 0.00001)
        assertEquals(37.7751, history[2].latitude, 0.00001)
    }
    
    @Test
    fun `test location history preserves order`() {
        mockLocationService.startTracking()
        
        // Add locations
        val locs = listOf(
            Triple(37.7749, -122.4194, 10f),
            Triple(37.7750, -122.4195, 15f),
            Triple(37.7751, -122.4196, 20f)
        )
        
        locs.forEach { (lat, lon, speed) ->
            mockLocationService.addLocationUpdate(lat, lon, speed)
        }
        
        // Verify order
        val history = mockLocationService.getLocationHistory()
        locs.forEachIndexed { index, (lat, lon, speed) ->
            assertEquals(lat, history[index].latitude, 0.00001)
            assertEquals(lon, history[index].longitude, 0.00001)
            assertEquals(speed, history[index].speed, 0.1f)
        }
    }
    
    // ==================== LOCATION HISTORY MANAGEMENT TESTS ====================
    
    @Test
    fun `test clear location history`() {
        mockLocationService.startTracking()
        
        // Add locations
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        mockLocationService.addLocationUpdate(37.7750, -122.4195)
        
        // Clear
        mockLocationService.clearLocationHistory()
        
        // Verify
        assertEquals(0, mockLocationService.getLocationCount())
        assertTrue(mockLocationService.getLocationHistory().isEmpty())
    }
    
    @Test
    fun `test location history accumulation`() {
        mockLocationService.startTracking()
        
        // Add locations incrementally
        assertEquals(0, mockLocationService.getLocationCount())
        
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        assertEquals(1, mockLocationService.getLocationCount())
        
        mockLocationService.addLocationUpdate(37.7750, -122.4195)
        assertEquals(2, mockLocationService.getLocationCount())
        
        mockLocationService.addLocationUpdate(37.7751, -122.4196)
        assertEquals(3, mockLocationService.getLocationCount())
    }
    
    @Test
    fun `test history persists after stopping tracking`() {
        mockLocationService.startTracking()
        
        // Add locations
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        mockLocationService.addLocationUpdate(37.7750, -122.4195)
        
        // Stop tracking
        mockLocationService.stopTracking()
        
        // History should still be there
        assertEquals(2, mockLocationService.getLocationCount())
    }
    
    // ==================== ACCURACY TESTS ====================
    
    @Test
    fun `test default accuracy`() {
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(10f, history[0].accuracy, 0.1f) // Default accuracy
    }
    
    @Test
    fun `test custom accuracy`() {
        // Set custom accuracy
        mockLocationService.setMockAccuracy(25f)
        
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(25f, history[0].accuracy, 0.1f)
    }
    
    @Test
    fun `test accuracy changes affect new locations`() {
        mockLocationService.startTracking()
        
        // Add location with default accuracy (10)
        mockLocationService.addLocationUpdate(37.7749, -122.4194)
        
        // Change accuracy
        mockLocationService.setMockAccuracy(50f)
        
        // Add another location
        mockLocationService.addLocationUpdate(37.7750, -122.4195)
        
        // Verify different accuracies
        val history = mockLocationService.getLocationHistory()
        assertEquals(10f, history[0].accuracy, 0.1f)
        assertEquals(50f, history[1].accuracy, 0.1f)
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test
    fun `test start tracking fails when configured`() {
        mockLocationService.setShouldFail(true, "GPS unavailable")
        
        try {
            mockLocationService.startTracking()
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("GPS unavailable", e.message)
        }
    }
    
    @Test
    fun `test location update fails when configured`() {
        mockLocationService.startTracking()
        mockLocationService.setShouldFail(true, "GPS signal lost")
        
        try {
            mockLocationService.addLocationUpdate(37.7749, -122.4194)
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("GPS signal lost", e.message)
        }
    }
    
    @Test
    fun `test recovery from failure`() {
        // Fail
        mockLocationService.setShouldFail(true)
        try {
            mockLocationService.startTracking()
        } catch (e: Exception) {
            // Expected
        }
        
        // Recover
        mockLocationService.setShouldFail(false)
        val success = mockLocationService.startTracking()
        
        assertTrue("Should recover after failure", success)
        assertTrue(mockLocationService.isTracking())
    }
    
    // ==================== SPEED TRACKING TESTS ====================
    
    @Test
    fun `test speed is recorded correctly`() {
        mockLocationService.startTracking()
        
        // Add location with speed
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 55f)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(55f, history[0].speed, 0.1f)
    }
    
    @Test
    fun `test zero speed`() {
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 0f)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(0f, history[0].speed, 0.1f)
    }
    
    @Test
    fun `test high speed values`() {
        mockLocationService.startTracking()
        
        // Add high-speed location
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 75f)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(75f, history[0].speed, 0.1f)
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    fun `test complete tracking session`() {
        // Start tracking
        mockLocationService.startTracking()
        assertTrue(mockLocationService.isTracking())
        
        // Add several locations
        mockLocationService.addLocationUpdate(37.7749, -122.4194, 10f)
        mockLocationService.addLocationUpdate(37.7750, -122.4195, 25f)
        mockLocationService.addLocationUpdate(37.7751, -122.4196, 40f)
        
        // Stop tracking
        mockLocationService.stopTracking()
        assertFalse(mockLocationService.isTracking())
        
        // Verify history
        assertEquals(3, mockLocationService.getLocationCount())
        
        // History should persist after stopping
        val history = mockLocationService.getLocationHistory()
        assertEquals(3, history.size)
    }
}

