package com.example.outofroutebuddy.data

import android.content.Context
import android.content.SharedPreferences
import com.example.outofroutebuddy.domain.models.PeriodMode
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreferencesManagerTest {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockEditor.clear() } returns mockEditor

        preferencesManager = PreferencesManager(mockContext)
    }

    @Test
    fun `test period mode persistence`() {
        // Test default value when no preference is set
        every { mockSharedPreferences.getString("period_mode", PeriodMode.STANDARD.name) } returns PeriodMode.STANDARD.name
        assertEquals(PeriodMode.STANDARD, preferencesManager.getPeriodMode())

        // Test saving and loading CUSTOM mode
        preferencesManager.savePeriodMode(PeriodMode.CUSTOM)
        verify { mockEditor.putString("period_mode", PeriodMode.CUSTOM.name) }
        verify { mockEditor.apply() }

        // Test loading CUSTOM mode
        every { mockSharedPreferences.getString("period_mode", PeriodMode.STANDARD.name) } returns PeriodMode.CUSTOM.name
        assertEquals(PeriodMode.CUSTOM, preferencesManager.getPeriodMode())
    }

    @Test
    fun `test loaded miles persistence`() {
        // Test default value when no preference is set
        every { mockSharedPreferences.getString("last_loaded_miles", "") } returns ""
        assertEquals("", preferencesManager.getLastLoadedMiles())

        // Test saving and loading loaded miles
        preferencesManager.saveLastLoadedMiles("100.5")
        verify { mockEditor.putString("last_loaded_miles", "100.5") }
        verify { mockEditor.apply() }

        // Test loading saved miles
        every { mockSharedPreferences.getString("last_loaded_miles", "") } returns "100.5"
        assertEquals("100.5", preferencesManager.getLastLoadedMiles())
    }

    @Test
    fun `test bounce miles persistence`() {
        // Test default value when no preference is set
        every { mockSharedPreferences.getString("last_bounce_miles", "") } returns ""
        assertEquals("", preferencesManager.getLastBounceMiles())

        // Test saving and loading bounce miles
        preferencesManager.saveLastBounceMiles("25.0")
        verify { mockEditor.putString("last_bounce_miles", "25.0") }
        verify { mockEditor.apply() }

        // Test loading saved miles
        every { mockSharedPreferences.getString("last_bounce_miles", "") } returns "25.0"
        assertEquals("25.0", preferencesManager.getLastBounceMiles())
    }

    @Test
    fun `test clear all preferences`() {
        // Clear all preferences
        preferencesManager.clearAllPreferences()
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
} 
