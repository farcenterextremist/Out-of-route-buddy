package com.example.outofroutebuddy.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.R

/**
 * 🔧 Test Utilities for Navigation Testing
 * 
 * Helper utilities to manipulate and verify navigation state in tests.
 * Provides navigation actions and back stack assertions.
 * 
 * Created: Phase 0 - Infrastructure
 * Purpose: Enable navigation testing
 */
object TestNavigationUtils {
    
    /**
     * Create a test NavController with the app's navigation graph
     */
    fun createTestNavController(): TestNavHostController {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.nav_graph)
        return navController
    }
    
    /**
     * Get NavController from a fragment
     */
    fun getNavController(fragment: Fragment): NavController? {
        return try {
            Navigation.findNavController(fragment.requireView())
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Navigate to trip input fragment
     */
    fun navigateToTripInput(navController: NavController) {
        navController.navigate(R.id.tripInputFragment)
    }
    
    /**
     * Navigate to trip history fragment
     */
    fun navigateToTripHistory(navController: NavController) {
        navController.navigate(R.id.tripHistoryFragment)
    }
    
    /**
     * Navigate to settings fragment
     */
    fun navigateToSettings(navController: NavController) {
        navController.navigate(R.id.settingsFragment)
    }
    
    /**
     * Simulate back press
     */
    fun pressBack(navController: NavController): Boolean {
        return navController.popBackStack()
    }
    
    /**
     * Get current destination ID
     */
    fun getCurrentDestinationId(navController: NavController): Int? {
        return navController.currentDestination?.id
    }
    
    /**
     * Get back stack entry count
     */
    fun getBackStackEntryCount(navController: NavController): Int {
        return navController.backQueue.size
    }
    
    /**
     * Assert current destination is trip input
     */
    fun assertOnTripInput(navController: NavController) {
        val currentId = getCurrentDestinationId(navController)
        assert(currentId == R.id.tripInputFragment) {
            "Expected to be on TripInputFragment but was on $currentId"
        }
    }
    
    /**
     * Assert current destination is trip history
     */
    fun assertOnTripHistory(navController: NavController) {
        val currentId = getCurrentDestinationId(navController)
        assert(currentId == R.id.tripHistoryFragment) {
            "Expected to be on TripHistoryFragment but was on $currentId"
        }
    }
    
    /**
     * Assert current destination is settings
     */
    fun assertOnSettings(navController: NavController) {
        val currentId = getCurrentDestinationId(navController)
        assert(currentId == R.id.settingsFragment) {
            "Expected to be on SettingsFragment but was on $currentId"
        }
    }
    
    /**
     * Assert back stack has expected size
     */
    fun assertBackStackSize(navController: NavController, expectedSize: Int) {
        val actualSize = getBackStackEntryCount(navController)
        assert(actualSize == expectedSize) {
            "Expected back stack size $expectedSize but was $actualSize"
        }
    }
    
    /**
     * Assert can navigate back
     */
    fun assertCanNavigateBack(navController: NavController) {
        assert(getBackStackEntryCount(navController) > 1) {
            "Cannot navigate back - back stack is empty"
        }
    }
    
    /**
     * Get fragment from activity
     */
    fun <T : Fragment> getFragment(activity: FragmentActivity, fragmentClass: Class<T>): T? {
        return activity.supportFragmentManager.fragments
            .filterIsInstance(fragmentClass)
            .firstOrNull()
    }
}

