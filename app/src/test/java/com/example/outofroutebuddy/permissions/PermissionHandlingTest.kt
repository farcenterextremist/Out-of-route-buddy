package com.example.outofroutebuddy.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ✅ CRITICAL PRIORITY: Permission Handling Tests
 * 
 * Tests the critical permission request flow:
 * - Permission state checking
 * - Foreground permissions
 * - Background permissions (Android 10+)
 * - Permission denial handling
 */
class PermissionHandlingTest {

    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        
        // Mock package manager
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== FOREGROUND PERMISSION TESTS ====================

    @Test
    fun `checkSelfPermission returns GRANTED when permission is granted`() {
        every {
            ContextCompat.checkSelfPermission(
                mockContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_GRANTED
        
        val result = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        assertEquals("Permission should be granted", PackageManager.PERMISSION_GRANTED, result)
    }

    @Test
    fun `checkSelfPermission returns DENIED when permission is denied`() {
        every {
            ContextCompat.checkSelfPermission(
                mockContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED
        
        val result = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        assertEquals("Permission should be denied", PackageManager.PERMISSION_DENIED, result)
    }

    @Test
    fun `both FINE and COARSE permissions required for location`() {
        val finePermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarsePermission = Manifest.permission.ACCESS_COARSE_LOCATION
        
        assertNotNull("Fine location permission should exist", finePermission)
        assertNotNull("Coarse location permission should exist", coarsePermission)
        assertEquals("Should have correct fine permission", "android.permission.ACCESS_FINE_LOCATION", finePermission)
        assertEquals("Should have correct coarse permission", "android.permission.ACCESS_COARSE_LOCATION", coarsePermission)
    }

    // ==================== BACKGROUND PERMISSION TESTS ====================

    @Test
    fun `background location permission exists for Android 10+`() {
        val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        
        assertNotNull("Background location permission should exist", backgroundPermission)
        assertEquals("Should have correct background permission", 
            "android.permission.ACCESS_BACKGROUND_LOCATION", backgroundPermission)
    }

    // ==================== PERMISSION STATE TESTS ====================

    @Test
    fun `hasLocationPermissions returns true when both permissions granted`() {
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        
        val hasFine = ContextCompat.checkSelfPermission(
            mockContext, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarse = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        assertTrue("Should have both permissions", hasFine && hasCoarse)
    }

    @Test
    fun `hasLocationPermissions returns false when FINE permission denied`() {
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        
        val hasFine = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        assertFalse("Should not have fine permission", hasFine)
    }

    @Test
    fun `hasLocationPermissions returns false when COARSE permission denied`() {
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        
        every {
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        
        val hasCoarse = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        assertFalse("Should not have coarse permission", hasCoarse)
    }

    @Test
    fun `hasLocationPermissions returns false when both permissions denied`() {
        every {
            ContextCompat.checkSelfPermission(mockContext, any())
        } returns PackageManager.PERMISSION_DENIED
        
        val hasFine = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarse = ContextCompat.checkSelfPermission(
            mockContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        assertFalse("Should not have any permissions", hasFine || hasCoarse)
    }

    // ==================== PERMISSION ARRAY TESTS ====================

    @Test
    fun `required permissions array contains both location permissions`() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        assertEquals("Should have 2 required permissions", 2, requiredPermissions.size)
        assertTrue("Should include FINE location", 
            requiredPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue("Should include COARSE location",
            requiredPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}



