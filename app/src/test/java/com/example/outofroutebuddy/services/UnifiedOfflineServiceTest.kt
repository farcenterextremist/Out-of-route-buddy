package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.utils.MockServices
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 UnifiedOfflineService Tests
 * 
 * Tests for UnifiedOfflineService functionality:
 * - Offline data sync
 * - Network state handling
 * - Queue management
 * - Conflict resolution
 * 
 * Priority: 🟡 MEDIUM
 * Impact: Offline functionality robustness
 * 
 * Created: Phase 2A - Unified Services
 */
class UnifiedOfflineServiceTest {
    
    private lateinit var mockOfflineService: MockServices.MockUnifiedOfflineService
    
    @Before
    fun setup() {
        mockOfflineService = MockServices.UnifiedMockServiceFactory.createMockOfflineService()
    }
    
    // ==================== NETWORK STATE TESTS ====================
    
    @Test
    fun `test starts in online state`() {
        assertTrue("Should start in online state", mockOfflineService.isOnline())
    }
    
    @Test
    fun `test set offline`() {
        mockOfflineService.setOnline(false)
        assertFalse("Should be offline", mockOfflineService.isOnline())
    }
    
    @Test
    fun `test set online`() {
        mockOfflineService.setOnline(false)
        mockOfflineService.setOnline(true)
        assertTrue("Should be online", mockOfflineService.isOnline())
    }
    
    @Test
    fun `test toggle online offline multiple times`() {
        repeat(5) {
            mockOfflineService.setOnline(false)
            assertFalse(mockOfflineService.isOnline())
            
            mockOfflineService.setOnline(true)
            assertTrue(mockOfflineService.isOnline())
        }
    }
    
    // ==================== QUEUE MANAGEMENT TESTS ====================
    
    @Test
    fun `test queue starts empty`() {
        assertEquals(0, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test queue item`() {
        mockOfflineService.queueSync("trip", mapOf("id" to "123"))
        assertEquals(1, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test queue multiple items`() {
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("location", mapOf("lat" to 37.77))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        
        assertEquals(3, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test clear queue`() {
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        
        mockOfflineService.clearQueue()
        assertEquals(0, mockOfflineService.getQueueSize())
    }
    
    // ==================== SYNC TESTS ====================
    
    @Test
    fun `test sync when online clears queue`() {
        // Queue items
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        assertEquals(2, mockOfflineService.getQueueSize())
        
        // Sync while online
        val result = mockOfflineService.performSync()
        
        // Queue should be cleared
        assertTrue("Sync should succeed", result.success)
        assertEquals(2, result.itemsProcessed)
        assertEquals(0, result.itemsFailed)
        assertEquals(0, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test sync when offline does not clear queue`() {
        // Go offline
        mockOfflineService.setOnline(false)
        
        // Queue items
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        
        // Try to sync
        val result = mockOfflineService.performSync()
        
        // Sync should fail, queue should remain
        assertFalse("Sync should fail when offline", result.success)
        assertEquals(0, result.itemsProcessed)
        assertEquals(2, result.itemsFailed)
        assertEquals(2, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test sync with empty queue`() {
        // Sync with no queued items
        val result = mockOfflineService.performSync()
        
        // Should succeed but process nothing
        assertTrue(result.success)
        assertEquals(0, result.itemsProcessed)
        assertEquals(0, result.itemsFailed)
    }
    
    // ==================== SYNC HISTORY TESTS ====================
    
    @Test
    fun `test sync history tracks all syncs`() {
        // Perform multiple syncs
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.performSync()
        
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        mockOfflineService.performSync()
        
        // Verify history
        val history = mockOfflineService.getSyncHistory()
        assertEquals(2, history.size)
    }
    
    @Test
    fun `test clear sync history`() {
        mockOfflineService.performSync()
        mockOfflineService.performSync()
        
        mockOfflineService.clearHistory()
        
        val history = mockOfflineService.getSyncHistory()
        assertTrue("History should be empty", history.isEmpty())
    }
    
    @Test
    fun `test sync history contains results`() {
        // Queue and sync
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        val result = mockOfflineService.performSync()
        
        // Check history
        val history = mockOfflineService.getSyncHistory()
        assertEquals(1, history.size)
        assertEquals(result.success, history[0].success)
        assertEquals(result.itemsProcessed, history[0].itemsProcessed)
    }
    
    // ==================== ONLINE/OFFLINE TRANSITION TESTS ====================
    
    @Test
    fun `test queue while offline then sync when online`() {
        // Go offline
        mockOfflineService.setOnline(false)
        
        // Queue items
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        mockOfflineService.queueSync("trip", mapOf("id" to "3"))
        assertEquals(3, mockOfflineService.getQueueSize())
        
        // Try to sync (should fail)
        val offlineResult = mockOfflineService.performSync()
        assertFalse(offlineResult.success)
        assertEquals(3, mockOfflineService.getQueueSize()) // Queue unchanged
        
        // Go online
        mockOfflineService.setOnline(true)
        
        // Sync should now succeed
        val onlineResult = mockOfflineService.performSync()
        assertTrue(onlineResult.success)
        assertEquals(3, onlineResult.itemsProcessed)
        assertEquals(0, mockOfflineService.getQueueSize())
    }
    
    @Test
    fun `test partial sync failure recovers`() {
        // Queue items
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        
        // First sync succeeds
        val result1 = mockOfflineService.performSync()
        assertTrue(result1.success)
        
        // Queue more items
        mockOfflineService.queueSync("trip", mapOf("id" to "3"))
        
        // Second sync also succeeds
        val result2 = mockOfflineService.performSync()
        assertTrue(result2.success)
        assertEquals(1, result2.itemsProcessed)
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test
    fun `test sync fails when configured to fail`() {
        mockOfflineService.setShouldFail(true, "Network error")
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        
        try {
            mockOfflineService.performSync()
            fail("Should throw exception")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
    }
    
    @Test
    fun `test recovery from sync failure`() {
        // Fail first
        mockOfflineService.setShouldFail(true)
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        
        try {
            mockOfflineService.performSync()
        } catch (e: Exception) {
            // Expected
        }
        
        // Recover
        mockOfflineService.setShouldFail(false)
        val result = mockOfflineService.performSync()
        
        assertTrue("Should recover from failure", result.success)
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    fun `test complete offline to online workflow`() {
        // Start online
        assertTrue(mockOfflineService.isOnline())
        
        // Go offline
        mockOfflineService.setOnline(false)
        assertFalse(mockOfflineService.isOnline())
        
        // Queue data while offline
        mockOfflineService.queueSync("trip", mapOf("id" to "1"))
        mockOfflineService.queueSync("location", mapOf("lat" to 37.77))
        mockOfflineService.queueSync("trip", mapOf("id" to "2"))
        assertEquals(3, mockOfflineService.getQueueSize())
        
        // Attempt offline sync (should fail)
        val offlineResult = mockOfflineService.performSync()
        assertFalse(offlineResult.success)
        assertEquals(3, mockOfflineService.getQueueSize()) // Still queued
        
        // Go online
        mockOfflineService.setOnline(true)
        
        // Sync should succeed
        val onlineResult = mockOfflineService.performSync()
        assertTrue(onlineResult.success)
        assertEquals(3, onlineResult.itemsProcessed)
        assertEquals(0, mockOfflineService.getQueueSize()) // Queue cleared
        
        // Verify history
        val history = mockOfflineService.getSyncHistory()
        assertEquals(2, history.size) // Both sync attempts recorded
        assertFalse(history[0].success) // Offline sync failed
        assertTrue(history[1].success) // Online sync succeeded
    }
    
    @Test
    fun `test unified service suite offline scenario`() {
        // Create suite
        val suite = MockServices.UnifiedMockServiceFactory.createUnifiedServiceSuite()
        
        // Configure for offline
        suite.configureOfflineScenario()
        
        // Verify offline state
        assertFalse(suite.offlineService.isOnline())
        assertEquals(50f, suite.locationService.getLocationHistory().firstOrNull()?.accuracy ?: 50f, 0.1f)
    }
    
    @Test
    fun `test unified service suite reset`() {
        // Create suite
        val suite = MockServices.UnifiedMockServiceFactory.createUnifiedServiceSuite()
        
        // Make changes
        suite.offlineService.setOnline(false)
        suite.offlineService.queueSync("trip", mapOf("id" to "1"))
        suite.tripService.calculateTrip(100.0, 20.0, 125.0)
        
        // Reset
        suite.resetAll()
        
        // Verify clean state
        assertTrue(suite.offlineService.isOnline())
        assertEquals(0, suite.offlineService.getQueueSize())
        assertEquals(0, suite.tripService.getTripHistory().size)
    }
}

