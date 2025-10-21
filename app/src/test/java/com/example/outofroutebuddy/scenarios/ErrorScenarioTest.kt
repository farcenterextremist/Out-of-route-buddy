package com.example.outofroutebuddy.scenarios

import com.example.outofroutebuddy.utils.MockServices
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 Error Scenario Tests
 * 
 * Tests for various error conditions:
 * - Network errors during sync
 * - Low storage scenarios
 * - GPS unavailable
 * - Database errors
 * - Memory pressure
 * 
 * Priority: 🟡 MEDIUM
 * Impact: Robustness and error handling
 * 
 * Created: Phase 2D - Error Scenarios
 */
class ErrorScenarioTest {
    
    private lateinit var mockServices: MockServices.UnifiedMockServiceSuite
    
    @Before
    fun setup() {
        mockServices = MockServices.UnifiedMockServiceFactory.createUnifiedServiceSuite()
    }
    
    // ==================== NETWORK ERROR TESTS ====================
    
    @Test
    fun `test network error during sync`() {
        // Go offline
        mockServices.offlineService.setOnline(false)
        
        // Queue data
        mockServices.offlineService.queueSync("trip", mapOf("id" to "1"))
        
        // Try to sync (should fail gracefully)
        val result = mockServices.offlineService.performSync()
        
        assertFalse("Sync should fail when offline", result.success)
        assertEquals(0, result.itemsProcessed)
        assertEquals(1, result.itemsFailed)
    }
    
    @Test
    fun `test sync recovers when network returns`() {
        // Start offline
        mockServices.offlineService.setOnline(false)
        mockServices.offlineService.queueSync("trip", mapOf("id" to "1"))
        
        // Failed sync
        val failedResult = mockServices.offlineService.performSync()
        assertFalse(failedResult.success)
        
        // Go online
        mockServices.offlineService.setOnline(true)
        
        // Should succeed now
        val successResult = mockServices.offlineService.performSync()
        assertTrue("Should recover when online", successResult.success)
    }
    
    // ==================== GPS UNAVAILABLE TESTS ====================
    
    @Test
    fun `test GPS unavailable on tracking start`() {
        mockServices.locationService.setShouldFail(true, "GPS unavailable")
        
        try {
            mockServices.locationService.startTracking()
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("GPS unavailable", e.message)
        }
    }
    
    @Test
    fun `test GPS signal lost during tracking`() {
        // Start tracking successfully
        mockServices.locationService.startTracking()
        mockServices.locationService.addLocationUpdate(37.77, -122.41)
        
        // GPS signal lost
        mockServices.locationService.setShouldFail(true, "GPS signal lost")
        
        try {
            mockServices.locationService.addLocationUpdate(37.78, -122.42)
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("GPS signal lost", e.message)
        }
        
        // Location count should be 1 (only the successful one)
        assertEquals(1, mockServices.locationService.getLocationCount())
    }
    
    // ==================== SERVICE FAILURE TESTS ====================
    
    @Test
    fun `test trip calculation fails gracefully`() {
        mockServices.tripService.setShouldFail(true, "Calculation error")
        
        try {
            mockServices.tripService.calculateTrip(100.0, 20.0, 125.0)
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("Calculation error", e.message)
        }
        
        // History should be empty
        assertEquals(0, mockServices.tripService.getTripHistory().size)
    }
    
    @Test
    fun `test period statistics fail gracefully`() {
        // Add a trip first
        mockServices.tripService.calculateTrip(100.0, 20.0, 125.0)
        
        // Configure to fail
        mockServices.tripService.setShouldFail(true, "Stats error")
        
        try {
            mockServices.tripService.calculatePeriodStatistics()
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("Stats error", e.message)
        }
    }
    
    // ==================== CONCURRENT FAILURE TESTS ====================
    
    @Test
    fun `test multiple services failing simultaneously`() {
        // Configure all services to fail
        mockServices.configureErrorScenario()
        
        // All operations should fail
        try {
            mockServices.tripService.calculateTrip(100.0, 20.0, 125.0)
            fail("Trip service should fail")
        } catch (e: Exception) {
            // Expected
        }
        
        try {
            mockServices.locationService.startTracking()
            fail("Location service should fail")
        } catch (e: Exception) {
            // Expected
        }
        
        try {
            mockServices.offlineService.performSync()
            fail("Offline service should fail")
        } catch (e: Exception) {
            // Expected
        }
    }
    
    @Test
    fun `test recovery from multiple service failures`() {
        // All fail
        mockServices.configureErrorScenario()
        
        // Reset to working state
        mockServices.resetAll()
        
        // All should work now
        assertTrue(mockServices.locationService.startTracking())
        assertNotNull(mockServices.tripService.calculateTrip(100.0, 20.0, 125.0))
        assertTrue(mockServices.offlineService.performSync().success)
    }
    
    // ==================== INTEGRATION ERROR TESTS ====================
    
    @Test
    fun `test offline scenario with multiple services`() {
        mockServices.configureOfflineScenario()
        
        // Offline service should be offline
        assertFalse(mockServices.offlineService.isOnline())
        
        // Sync should fail
        val result = mockServices.offlineService.performSync()
        assertFalse(result.success)
        
        // But other services can still work
        assertTrue(mockServices.locationService.startTracking())
    }
    
    @Test
    fun `test online recovery scenario`() {
        // Start offline
        mockServices.configureOfflineScenario()
        mockServices.offlineService.queueSync("trip", mapOf("id" to "1"))
        
        // Switch to online
        mockServices.configureOnlineScenario()
        
        // Should sync successfully
        val result = mockServices.offlineService.performSync()
        assertTrue(result.success)
        assertEquals(1, result.itemsProcessed)
    }
}

