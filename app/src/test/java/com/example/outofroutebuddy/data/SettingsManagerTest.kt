package com.example.outofroutebuddy.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ✅ HIGH PRIORITY: Settings Manager Tests
 * 
 * Tests critical settings functionality that affects:
 * - GPS update frequency
 * - Distance unit conversion
 * - Battery optimization
 * - User preferences
 */
class SettingsManagerTest {

    private lateinit var settingsManager: SettingsManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        
        every { mockContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        settingsManager = SettingsManager(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== GPS SETTINGS TESTS ====================

    @Test
    fun `getGpsUpdateFrequency returns default 10 seconds`() {
        every { mockPrefs.getInt("gps_update_frequency", 10) } returns 10
        
        val frequency = settingsManager.getGpsUpdateFrequency()
        
        assertEquals(10, frequency)
    }

    @Test
    fun `setGpsUpdateFrequency saves value correctly`() {
        settingsManager.setGpsUpdateFrequency(15)
        
        verify { mockEditor.putInt("gps_update_frequency", 15) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `isHighAccuracyMode returns default true`() {
        every { mockPrefs.getBoolean("high_accuracy_mode", true) } returns true
        
        val isHighAccuracy = settingsManager.isHighAccuracyMode()
        
        assertTrue(isHighAccuracy)
    }

    // ==================== DISTANCE UNIT TESTS ====================

    @Test
    fun `getDistanceUnits returns default miles`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "miles"
        
        val units = settingsManager.getDistanceUnits()
        
        assertEquals("miles", units)
    }

    @Test
    fun `isKilometers returns false when units are miles`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "miles"
        
        val isKm = settingsManager.isKilometers()
        
        assertFalse(isKm)
    }

    @Test
    fun `isKilometers returns true when units are kilometers`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "kilometers"
        
        val isKm = settingsManager.isKilometers()
        
        assertTrue(isKm)
    }

    @Test
    fun `convertDistance keeps miles when units are miles`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "miles"
        
        val converted = settingsManager.convertDistance(100.0)
        
        assertEquals(100.0, converted, 0.01)
    }

    @Test
    fun `convertDistance converts to km when units are kilometers`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "kilometers"
        
        val converted = settingsManager.convertDistance(100.0)
        
        // 100 miles = 160.934 km
        assertEquals(160.934, converted, 0.01)
    }

    @Test
    fun `getDistanceUnitLabel returns correct label for miles`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "miles"
        
        val label = settingsManager.getDistanceUnitLabel()
        
        assertEquals("mi", label)
    }

    @Test
    fun `getDistanceUnitLabel returns correct label for kilometers`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "kilometers"
        
        val label = settingsManager.getDistanceUnitLabel()
        
        assertEquals("km", label)
    }

    // ==================== NOTIFICATION SETTINGS TESTS ====================

    @Test
    fun `areNotificationsEnabled returns default true`() {
        every { mockPrefs.getBoolean("notifications_enabled", true) } returns true
        
        val enabled = settingsManager.areNotificationsEnabled()
        
        assertTrue(enabled)
    }

    @Test
    fun `setNotificationsEnabled saves value correctly`() {
        settingsManager.setNotificationsEnabled(false)
        
        verify { mockEditor.putBoolean("notifications_enabled", false) }
        verify { mockEditor.apply() }
    }

    // ==================== BATTERY OPTIMIZATION TESTS ====================

    @Test
    fun `isBatteryOptimizationEnabled returns default true`() {
        every { mockPrefs.getBoolean("battery_optimization", true) } returns true
        
        val enabled = settingsManager.isBatteryOptimizationEnabled()
        
        assertTrue(enabled)
    }

    @Test
    fun `setBatteryOptimizationEnabled saves value correctly`() {
        settingsManager.setBatteryOptimizationEnabled(false)
        
        verify { mockEditor.putBoolean("battery_optimization", false) }
        verify { mockEditor.apply() }
    }

    // ==================== TRIP SETTINGS TESTS ====================

    @Test
    fun `isAutoStartTripEnabled returns default false`() {
        every { mockPrefs.getBoolean("auto_start_trip", false) } returns false
        
        val enabled = settingsManager.isAutoStartTripEnabled()
        
        assertFalse(enabled)
    }

    @Test
    fun `isAutoSaveTripEnabled returns default true`() {
        every { mockPrefs.getBoolean("auto_save_trip", true) } returns true
        
        val enabled = settingsManager.isAutoSaveTripEnabled()
        
        assertTrue(enabled)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `convertDistance handles zero correctly`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "kilometers"
        
        val converted = settingsManager.convertDistance(0.0)
        
        assertEquals(0.0, converted, 0.01)
    }

    @Test
    fun `convertDistance handles negative values`() {
        every { mockPrefs.getString("distance_units", "miles") } returns "kilometers"
        
        val converted = settingsManager.convertDistance(-50.0)
        
        assertEquals(-80.467, converted, 0.01)
    }

    @Test
    fun `getDistanceUnitLabel handles null preference gracefully`() {
        every { mockPrefs.getString("distance_units", "miles") } returns null
        
        val label = settingsManager.getDistanceUnitLabel()
        
        assertEquals("mi", label) // Should default to miles
    }
}











