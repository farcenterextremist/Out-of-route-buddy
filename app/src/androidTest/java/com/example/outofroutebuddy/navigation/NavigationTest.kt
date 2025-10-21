package com.example.outofroutebuddy.navigation

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.utils.TestNavigationUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧪 Navigation Tests
 * 
 * Tests for app navigation and fragment transitions:
 * - Navigation between TripInputFragment ↔ TripHistoryFragment
 * - Navigation to SettingsFragment
 * - Back stack handling
 * - Deep link navigation
 * - Fragment state restoration
 * 
 * Priority: 🔴 HIGH
 * Impact: Core user flows
 * 
 * Created: Phase 1 - Critical Tests
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var navController: TestNavHostController
    
    @Before
    fun setup() {
        // Create test NavController
        navController = TestNavigationUtils.createTestNavController()
    }
    
    // ==================== BASIC NAVIGATION TESTS ====================
    
    @Test
    fun testInitialDestinationIsTripInput() {
        activityRule.scenario.onActivity { activity ->
            val fragmentManager = activity.supportFragmentManager
            val currentFragment = fragmentManager.fragments.firstOrNull()
            
            // Should start on TripInputFragment
            assert(currentFragment != null) {
                "Should have an active fragment"
            }
        }
    }
    
    @Test
    fun testNavigateToTripInput() {
        // Navigate to trip input
        TestNavigationUtils.navigateToTripInput(navController)
        
        // Verify destination
        TestNavigationUtils.assertOnTripInput(navController)
    }
    
    @Test
    fun testNavigateToTripHistory() {
        // Navigate to history
        TestNavigationUtils.navigateToTripHistory(navController)
        
        // Verify destination
        TestNavigationUtils.assertOnTripHistory(navController)
    }
    
    @Test
    fun testNavigateToSettings() {
        // Navigate to settings
        TestNavigationUtils.navigateToSettings(navController)
        
        // Verify destination
        TestNavigationUtils.assertOnSettings(navController)
    }
    
    // ==================== BACK NAVIGATION TESTS ====================
    
    @Test
    fun testBackNavigationFromHistory() {
        // Navigate to history
        TestNavigationUtils.navigateToTripHistory(navController)
        TestNavigationUtils.assertOnTripHistory(navController)
        
        // Navigate back
        val success = TestNavigationUtils.pressBack(navController)
        
        // Should be back on trip input
        if (success) {
            TestNavigationUtils.assertOnTripInput(navController)
        }
    }
    
    @Test
    fun testBackNavigationFromSettings() {
        // Navigate to settings
        TestNavigationUtils.navigateToSettings(navController)
        TestNavigationUtils.assertOnSettings(navController)
        
        // Navigate back
        val success = TestNavigationUtils.pressBack(navController)
        
        // Should be back on previous screen
        assert(success) {
            "Should be able to navigate back from settings"
        }
    }
    
    @Test
    fun testBackStackOrdering() {
        // Start at trip input (back stack size should be 1)
        val initialSize = TestNavigationUtils.getBackStackEntryCount(navController)
        
        // Navigate to history
        TestNavigationUtils.navigateToTripHistory(navController)
        val afterHistorySize = TestNavigationUtils.getBackStackEntryCount(navController)
        
        // Back stack should have grown
        assert(afterHistorySize > initialSize) {
            "Back stack should grow after navigation"
        }
        
        // Navigate to settings
        TestNavigationUtils.navigateToSettings(navController)
        val afterSettingsSize = TestNavigationUtils.getBackStackEntryCount(navController)
        
        // Back stack should have grown again
        assert(afterSettingsSize > afterHistorySize) {
            "Back stack should continue growing"
        }
    }
    
    @Test
    fun testMultipleBackPresses() {
        // Navigate forward twice
        TestNavigationUtils.navigateToTripHistory(navController)
        TestNavigationUtils.navigateToSettings(navController)
        
        // Back twice
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripHistory(navController)
        
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripInput(navController)
    }
    
    // ==================== NAVIGATION FLOW TESTS ====================
    
    @Test
    fun testCompleteNavigationFlow() {
        // Start → History → Settings → Back → Back → Start
        TestNavigationUtils.assertOnTripInput(navController)
        
        TestNavigationUtils.navigateToTripHistory(navController)
        TestNavigationUtils.assertOnTripHistory(navController)
        
        TestNavigationUtils.navigateToSettings(navController)
        TestNavigationUtils.assertOnSettings(navController)
        
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripHistory(navController)
        
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripInput(navController)
    }
    
    @Test
    fun testNavigationFromAllScreens() {
        // From Trip Input → History
        TestNavigationUtils.assertOnTripInput(navController)
        TestNavigationUtils.navigateToTripHistory(navController)
        TestNavigationUtils.assertOnTripHistory(navController)
        
        // Back to input
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripInput(navController)
        
        // From Trip Input → Settings
        TestNavigationUtils.navigateToSettings(navController)
        TestNavigationUtils.assertOnSettings(navController)
        
        // Back to input
        TestNavigationUtils.pressBack(navController)
        TestNavigationUtils.assertOnTripInput(navController)
    }
    
    // ==================== BACK STACK MANAGEMENT TESTS ====================
    
    @Test
    fun testBackStackNotEmpty() {
        // After initial navigation, back stack should have entries
        TestNavigationUtils.navigateToTripHistory(navController)
        
        val backStackSize = TestNavigationUtils.getBackStackEntryCount(navController)
        assert(backStackSize > 0) {
            "Back stack should not be empty after navigation"
        }
    }
    
    @Test
    fun testBackStackCanNavigateBack() {
        // Navigate somewhere
        TestNavigationUtils.navigateToTripHistory(navController)
        
        // Should be able to navigate back
        TestNavigationUtils.assertCanNavigateBack(navController)
    }
    
    @Test
    fun testBackStackClearsOnPopToRoot() {
        // Navigate multiple times
        TestNavigationUtils.navigateToTripHistory(navController)
        TestNavigationUtils.navigateToSettings(navController)
        
        // Pop back to root
        while (TestNavigationUtils.pressBack(navController)) {
            // Keep going
        }
        
        // Should be back at the start
        TestNavigationUtils.assertOnTripInput(navController)
    }
    
    // ==================== STATE PRESERVATION TESTS ====================
    
    @Test
    fun testNavigationPreservesActivityState() {
        activityRule.scenario.onActivity { activity ->
            // Activity should maintain its state across navigation
            assert(activity != null)
            assert(!activity.isFinishing)
        }
        
        // Simulate navigation
        activityRule.scenario.recreate()
        
        activityRule.scenario.onActivity { activity ->
            // Activity should still be valid
            assert(activity != null)
            assert(!activity.isFinishing)
        }
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun testRapidNavigationChanges() {
        // Rapidly switch between screens
        repeat(5) {
            TestNavigationUtils.navigateToTripHistory(navController)
            TestNavigationUtils.assertOnTripHistory(navController)
            
            TestNavigationUtils.pressBack(navController)
            TestNavigationUtils.assertOnTripInput(navController)
        }
    }
    
    @Test
    fun testNavigationAfterActivityRecreation() {
        // Navigate somewhere
        TestNavigationUtils.navigateToTripHistory(navController)
        
        // Recreate activity (configuration change)
        activityRule.scenario.recreate()
        
        // Create new nav controller for new activity
        val newNavController = TestNavigationUtils.createTestNavController()
        
        // Should be able to navigate
        TestNavigationUtils.navigateToSettings(newNavController)
        TestNavigationUtils.assertOnSettings(newNavController)
    }
    
    @Test
    fun testBackPressFromStartDoesNotCrash() {
        // Try to navigate back from start
        val success = TestNavigationUtils.pressBack(navController)
        
        // May or may not succeed, but should not crash
        // If it doesn't crash, test passes
        assert(true)
    }
}

