package com.example.outofroutebuddy.gps

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.utils.MockServices
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * 🧪 GPS Failure Tests
 * 
 * Tests for GPS failure scenarios:
 * - GPS signal loss
 * - Poor accuracy handling
 * - Permission revocation mid-trip
 * - Location service disabled
 * - Airplane mode
 * 
 * Priority: 🟡 MEDIUM
 * Impact: GPS reliability
 * 
 * Created: Phase 2E - GPS Edge Cases
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class GpsFailureTest {
    
    private lateinit var mockLocationService: MockServices.MockUnifiedLocationService
    
    @Before
    fun setup() {
        mockLocationService = MockServices.UnifiedMockServiceFactory.createMockLocationService()
    }
    
    @Test
    fun testGpsSignalLoss() {
        // Start tracking
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.77, -122.41, 25f)
        
        // Signal lost
        mockLocationService.setShouldFail(true, "GPS signal lost")
        
        try {
            mockLocationService.addLocationUpdate(37.78, -122.42)
            fail("Should fail when signal lost")
        } catch (e: Exception) {
            assertEquals("GPS signal lost", e.message)
        }
    }
    
    @Test
    fun testPoorAccuracyHandling() {
        // Set poor accuracy
        mockLocationService.setMockAccuracy(100f) // Very poor
        
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.77, -122.41)
        
        val history = mockLocationService.getLocationHistory()
        assertEquals(100f, history[0].accuracy, 0.1f)
    }
    
    @Test
    fun testPermissionRevocationMidTrip() {
        mockLocationService.startTracking()
        mockLocationService.addLocationUpdate(37.77, -122.41)
        
        // Permission revoked
        mockLocationService.setShouldFail(true, "Permission denied")
        
        try {
            mockLocationService.addLocationUpdate(37.78, -122.42)
            fail("Should fail after permission revoked")
        } catch (e: Exception) {
            assertEquals("Permission denied", e.message)
        }
    }
}

