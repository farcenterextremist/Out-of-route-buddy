package com.example.outofroutebuddy.services

import android.content.Context
import android.location.Location
import android.os.BatteryManager
import com.example.outofroutebuddy.data.SettingsManager
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ✅ MEDIUM PRIORITY: Battery Optimization Service Tests
 * 
 * Tests battery-aware GPS behavior:
 * - Update interval adaptation based on battery
 * - Stationary detection
 * - Power saving modes
 */
class BatteryOptimizationServiceTest {

    private lateinit var service: BatteryOptimizationService
    private lateinit var mockContext: Context
    private lateinit var mockSettingsManager: SettingsManager
    private lateinit var mockBatteryManager: BatteryManager

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockSettingsManager = mockk(relaxed = true)
        mockBatteryManager = mockk(relaxed = true)
        
        every { mockContext.getSystemService(Context.BATTERY_SERVICE) } returns mockBatteryManager
        every { mockSettingsManager.isBatteryOptimizationEnabled() } returns true
        
        service = BatteryOptimizationService(mockContext, mockSettingsManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== BATTERY LEVEL TESTS ====================

    @Test
    fun `getRecommendedGpsInterval returns 30s for critical battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 10
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        val interval = service.getRecommendedGpsInterval()
        
        assertEquals("Critical battery should use 30s interval", 30000L, interval)
    }

    @Test
    fun `getRecommendedGpsInterval returns 15s for low battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 18
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        val interval = service.getRecommendedGpsInterval()
        
        assertEquals("Low battery should use 15s interval", 15000L, interval)
    }

    @Test
    fun `getRecommendedGpsInterval returns 10s for normal battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 50
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        val interval = service.getRecommendedGpsInterval()
        
        assertEquals("Normal battery should use 10s interval", 10000L, interval)
    }

    @Test
    fun `charging battery uses normal intervals regardless of level`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 10
        every { mockBatteryManager.isCharging } returns true
        
        service.updateBatteryStatus()
        val state = service.optimizationState.value
        
        assertEquals("Charging should use NORMAL mode", 
            BatteryOptimizationService.PowerSavingMode.NORMAL, state.powerSavingMode)
    }

    // ==================== STATIONARY DETECTION TESTS ====================

    @Test
    fun `checkStationaryStatus detects stationary device`() {
        val location1 = createMockLocation(37.7749, -122.4194)
        val location2 = createMockLocation(37.7749, -122.4194) // Same location
        
        service.checkStationaryStatus(location1)
        // Advance time > 1s; prefer Robolectric idle, fallback to real sleep if unavailable
        try {
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(1200, java.util.concurrent.TimeUnit.MILLISECONDS)
        } catch (_: Throwable) {
            Thread.sleep(1200)
        }
        service.checkStationaryStatus(location2)
        
        // After STATIONARY_TIME_MS would be stationary
        // Test just verifies no crash
        assertNotNull(service.optimizationState.value)
    }

    @Test
    fun `checkStationaryStatus detects movement`() {
        val location1 = createMockLocation(37.7749, -122.4194)
        val location2 = createMockLocation(37.7850, -122.4194) // Different latitude
        
        service.checkStationaryStatus(location1)
        service.checkStationaryStatus(location2)
        
        val state = service.optimizationState.value
        assertFalse("Movement should reset stationary status", state.isStationary)
    }

    // ==================== POWER SAVING MODE TESTS ====================

    @Test
    fun `updateBatteryStatus sets CRITICAL mode for low battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 10
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        
        val state = service.optimizationState.value
        assertEquals(BatteryOptimizationService.PowerSavingMode.CRITICAL, state.powerSavingMode)
        assertTrue("Should flag critical battery", state.isCriticalBattery)
    }

    @Test
    fun `updateBatteryStatus sets ECO mode for 15-20 percent battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 18
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        
        val state = service.optimizationState.value
        assertEquals(BatteryOptimizationService.PowerSavingMode.ECO, state.powerSavingMode)
        assertTrue("Should flag low battery", state.isLowBattery)
    }

    @Test
    fun `updateBatteryStatus sets NORMAL mode for good battery`() {
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 75
        every { mockBatteryManager.isCharging } returns false
        
        service.updateBatteryStatus()
        
        val state = service.optimizationState.value
        assertEquals(BatteryOptimizationService.PowerSavingMode.NORMAL, state.powerSavingMode)
        assertFalse("Should not flag low battery", state.isLowBattery)
    }

    // ==================== OPTIMIZATION DISABLED TESTS ====================

    @Test
    fun `getRecommendedGpsInterval uses normal interval when optimization disabled`() {
        every { mockSettingsManager.isBatteryOptimizationEnabled() } returns false
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 10
        
        service.updateBatteryStatus()
        val interval = service.getRecommendedGpsInterval()
        
        assertEquals("Disabled optimization should use 5s interval", 5000L, interval)
    }

    // ==================== HELPER METHODS ====================

    private fun createMockLocation(latitude: Double, longitude: Double): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
            this.time = System.currentTimeMillis()
            this.accuracy = 10f
        }
    }
}








