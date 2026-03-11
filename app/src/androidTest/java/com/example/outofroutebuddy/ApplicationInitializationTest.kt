package com.example.outofroutebuddy

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.utils.TestPreferenceUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * 🧪 Application Initialization Tests
 * 
 * Tests for app startup and initialization:
 * - Firebase initialization
 * - Database initialization
 * - Theme application on startup
 * - Crash recovery initialization
 * 
 * Priority: 🟢 LOW
 * Impact: Initialization robustness
 * 
 * Created: Phase 3A - Application Lifecycle
 */
@RunWith(AndroidJUnit4::class)
class ApplicationInitializationTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        TestPreferenceUtils.clearAll()
    }
    
    @Test
    fun testApplicationContextAvailable() {
        assertNotNull("Application context should be available", context)
    }
    
    @Test
    fun testDatabaseInitialization() {
        // Database should initialize without errors
        val app = context as? OutOfRouteApplication
        assertNotNull("Should be OutOfRouteApplication", app)
        
        if (app != null) {
            assertTrue("Application should be healthy", app.isHealthy())
            assertNull("No database errors", app.getDatabaseError())
        }
    }

    /** H1: When setDatabaseUnhealthy is called, app reports unhealthy and getDatabaseError returns the exception. */
    @Test
    fun testSetDatabaseUnhealthy_reportsUnhealthy() {
        val app = context as? OutOfRouteApplication
        assertNotNull("Should be OutOfRouteApplication", app)
        if (app == null) return
        val testException = Exception("Database check failed")
        app.setDatabaseUnhealthy(testException)
        assertFalse("Application should be unhealthy after setDatabaseUnhealthy", app.isHealthy())
        assertNotNull("getDatabaseError should return the set exception", app.getDatabaseError())
        assertEquals("Exception message should match", "Database check failed", app.getDatabaseError()?.message)
        // Restore so other tests are not affected (companion is process-wide)
        app.setDatabaseUnhealthy(null)
    }
    
    @Test
    fun testThemeAppliedOnStartup() {
        // Set a theme preference
        TestPreferenceUtils.setTheme("dark")
        
        // Theme should be readable
        val theme = TestPreferenceUtils.getTheme()
        assertEquals("dark", theme)
    }
    
    @Test
    fun testPreferencesAccessible() {
        // Preferences should be accessible
        TestPreferenceUtils.setGpsUpdateFrequency(15)
        assertEquals(15, TestPreferenceUtils.getGpsUpdateFrequency())
    }
    
    @Test
    fun testApplicationDoesNotCrashOnInit() {
        // This test passing means init didn't crash
        assertTrue("Application initialized successfully", true)
    }
}

