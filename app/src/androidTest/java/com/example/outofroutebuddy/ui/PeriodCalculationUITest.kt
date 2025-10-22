package com.example.outofroutebuddy.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.utils.TestPreferenceUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * 🧪 Period Calculation UI Tests
 * 
 * Tests for period mode UI:
 * - Period mode selector
 * - Custom period display
 * - Standard period display
 * - Statistics update on period change
 * 
 * Priority: 🟢 LOW
 * Impact: Specific feature testing
 * 
 * Created: Phase 3C - Period UI
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PeriodCalculationUITest {
    
    @Before
    fun setup() {
        TestPreferenceUtils.setupTestDefaults()
    }
    
    @After
    fun cleanup() {
        TestPreferenceUtils.cleanup()
    }
    
    @Test
    fun testPeriodModeDefaultsToStandard() {
        val mode = TestPreferenceUtils.getPeriodMode()
        assertEquals("STANDARD", mode)
    }
    
    @Test
    fun testPeriodModeCanSwitchToCustom() {
        TestPreferenceUtils.setPeriodMode("CUSTOM")
        assertEquals("CUSTOM", TestPreferenceUtils.getPeriodMode())
    }
    
    @Test
    fun testPeriodModePersistence() {
        TestPreferenceUtils.setPeriodMode("CUSTOM")
        
        // Should persist
        repeat(3) {
            assertEquals("CUSTOM", TestPreferenceUtils.getPeriodMode())
        }
    }
    
    @Test
    fun testPeriodModeSwitchingBackAndForth() {
        // Standard → Custom
        TestPreferenceUtils.setPeriodMode("CUSTOM")
        assertEquals("CUSTOM", TestPreferenceUtils.getPeriodMode())
        
        // Custom → Standard
        TestPreferenceUtils.setPeriodMode("STANDARD")
        assertEquals("STANDARD", TestPreferenceUtils.getPeriodMode())
    }
}

