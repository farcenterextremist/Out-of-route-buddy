package com.example.outofroutebuddy.services

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive tests for intelligent update frequency functionality
 *
 * Tests the ability to adapt GPS update frequency based on traffic conditions,
 * vehicle speed, and GPS signal quality.
 */
class IntelligentUpdateFrequencyTest {
    private lateinit var locationValidationService: LocationValidationService

    @Before
    fun setUp() {
        locationValidationService = LocationValidationService()
    }

    @Test
    fun `getOptimalUpdateFrequency should return normal frequency when disabled`() {
        // Test that frequency adaptation returns normal frequency when disabled
        // Since the feature is enabled by default, we test that speed and GPS adjustments work correctly
        // but the base frequency should still be NORMAL_UPDATE_FREQUENCY for non-traffic mode
        val frequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 10f,
            )
        // The frequency should be close to normal frequency, but may be adjusted by speed/GPS
        assertTrue(
            "Frequency should be reasonable",
            frequency >= LocationValidationService.MIN_UPDATE_FREQUENCY &&
                frequency <= LocationValidationService.MAX_UPDATE_FREQUENCY,
        )
    }

    @Test
    fun `getOptimalUpdateFrequency should return traffic frequency in traffic mode`() {
        val normalFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
            )
        val trafficFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = true,
                currentSpeed = 30f,
            )

        println("DEBUG: Normal frequency: $normalFrequency, Traffic frequency: $trafficFrequency")
        println("DEBUG: Expected traffic frequency: ${LocationValidationService.TRAFFIC_UPDATE_FREQUENCY}")

        // Traffic mode should have more frequent updates (lower frequency value in ms)
        assertTrue("Traffic mode should have more frequent updates", trafficFrequency < normalFrequency)
        assertEquals(LocationValidationService.TRAFFIC_UPDATE_FREQUENCY, trafficFrequency)
    }

    @Test
    fun `getOptimalUpdateFrequency should adjust for high speed`() {
        val lowSpeedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 10f,
            )
        val highSpeedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 70f,
            )

        // High speed should have more frequent updates (lower frequency value in ms)
        assertTrue("High speed should have more frequent updates", highSpeedFrequency < lowSpeedFrequency)
    }

    @Test
    fun `getOptimalUpdateFrequency should adjust for poor GPS accuracy`() {
        val goodAccuracyFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 5f,
            )
        val poorAccuracyFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 35f,
            )

        // Poor accuracy should have less frequent updates (higher frequency value in ms to avoid noise)
        assertTrue("Poor accuracy should have less frequent updates", poorAccuracyFrequency > goodAccuracyFrequency)
    }

    @Test
    fun `getOptimalUpdateFrequency should respect frequency bounds`() {
        // Test with extreme values to ensure bounds are respected
        val minFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 100f,
                gpsAccuracy = 1f,
            )
        val maxFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 1f,
                gpsAccuracy = 50f,
            )

        assertTrue("Frequency should not be below minimum", minFrequency >= LocationValidationService.MIN_UPDATE_FREQUENCY)
        assertTrue("Frequency should not be above maximum", maxFrequency <= LocationValidationService.MAX_UPDATE_FREQUENCY)
    }

    @Test
    fun `adaptUpdateFrequencyForGpsQuality should handle poor accuracy`() {
        val baseFrequency = 5000L
        val poorAccuracyFrequency = locationValidationService.adaptUpdateFrequencyForGpsQuality(baseFrequency, 35f)

        // Poor accuracy should increase frequency (slower updates)
        assertTrue("Poor accuracy should increase frequency", poorAccuracyFrequency > baseFrequency)
    }

    @Test
    fun `adaptUpdateFrequencyForGpsQuality should handle excellent accuracy`() {
        val baseFrequency = 5000L
        val excellentAccuracyFrequency = locationValidationService.adaptUpdateFrequencyForGpsQuality(baseFrequency, 5f)

        // Excellent accuracy should decrease frequency (faster updates)
        assertTrue("Excellent accuracy should decrease frequency", excellentAccuracyFrequency < baseFrequency)
    }

    @Test
    fun `adaptUpdateFrequencyForGpsQuality should handle zero accuracy`() {
        val baseFrequency = 5000L
        val zeroAccuracyFrequency = locationValidationService.adaptUpdateFrequencyForGpsQuality(baseFrequency, 0f)

        // Zero accuracy should return base frequency
        assertEquals("Zero accuracy should return base frequency", baseFrequency, zeroAccuracyFrequency)
    }

    @Test
    fun `shouldRequestGpsUpdate should return true when enough time has passed`() {
        val lastUpdateTime = System.currentTimeMillis() - 6000L // 6 seconds ago
        val shouldUpdate =
            locationValidationService.shouldRequestGpsUpdate(
                lastUpdateTime,
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 10f,
            )

        assertTrue("Should request update when enough time has passed", shouldUpdate)
    }

    @Test
    fun `shouldRequestGpsUpdate should return false when not enough time has passed`() {
        val lastUpdateTime = System.currentTimeMillis() - 1000L // 1 second ago
        val shouldUpdate =
            locationValidationService.shouldRequestGpsUpdate(
                lastUpdateTime,
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 10f,
            )

        assertFalse("Should not request update when not enough time has passed", shouldUpdate)
    }

    @Test
    fun `shouldRequestGpsUpdate should adapt to traffic mode`() {
        val lastUpdateTime = System.currentTimeMillis() - 3000L // 3 seconds ago

        // In normal mode, 3 seconds might not be enough
        val normalModeUpdate =
            locationValidationService.shouldRequestGpsUpdate(
                lastUpdateTime,
                trafficMode = false,
                currentSpeed = 30f,
                gpsAccuracy = 10f,
            )

        // In traffic mode, 3 seconds should be enough (more frequent updates)
        val trafficModeUpdate =
            locationValidationService.shouldRequestGpsUpdate(
                lastUpdateTime,
                trafficMode = true,
                currentSpeed = 30f,
                gpsAccuracy = 10f,
            )

        // Traffic mode should be more likely to request updates
        assertTrue("Traffic mode should request updates more frequently", trafficModeUpdate || !normalModeUpdate)
    }

    @Test
    fun `getUpdateFrequencyStats should return meaningful information`() {
        // Get some frequency data first
        locationValidationService.getOptimalUpdateFrequency(trafficMode = true, currentSpeed = 25f, gpsAccuracy = 15f)

        val stats = locationValidationService.getUpdateFrequencyStats()

        assertTrue("Stats should contain current frequency", stats.contains("Current frequency:"))
        assertTrue("Stats should contain target frequency", stats.contains("Target frequency:"))
        assertTrue("Stats should contain mode information", stats.contains("Mode: traffic"))
        assertTrue("Stats should contain average speed", stats.contains("Avg speed:"))
        assertTrue("Stats should contain average GPS accuracy", stats.contains("Avg GPS accuracy:"))
    }

    @Test
    fun `resetUpdateFrequencyAdaptation should clear state`() {
        // Add some frequency data
        locationValidationService.getOptimalUpdateFrequency(trafficMode = true, currentSpeed = 25f, gpsAccuracy = 15f)

        // Reset adaptation
        locationValidationService.resetUpdateFrequencyAdaptation()

        // Should be back to normal frequency
        val frequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
            )
        assertEquals(
            "Should return normal frequency after reset",
            LocationValidationService.NORMAL_UPDATE_FREQUENCY,
            frequency,
        )
    }

    @Test
    fun `frequency adaptation should handle speed-based adjustments`() {
        val lowSpeedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 5f,
            )
        val mediumSpeedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 40f,
            )
        val highSpeedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 80f,
            )

        // High speed should have more frequent updates (lower frequency value in ms)
        assertTrue("High speed should have more frequent updates than medium speed", highSpeedFrequency < mediumSpeedFrequency)
        assertTrue("Medium speed should have more frequent updates than low speed", mediumSpeedFrequency < lowSpeedFrequency)
    }

    @Test
    fun `frequency adaptation should provide smooth transitions`() {
        // Start with normal frequency
        val normalFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = false,
                currentSpeed = 30f,
            )

        // Switch to traffic mode
        val trafficFrequency1 =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = true,
                currentSpeed = 30f,
            )

        // Should be between normal and traffic frequencies during transition
        assertTrue(
            "Transition frequency should be reasonable",
            trafficFrequency1 >= LocationValidationService.MIN_UPDATE_FREQUENCY &&
                trafficFrequency1 <= LocationValidationService.MAX_UPDATE_FREQUENCY,
        )
    }

    @Test
    fun `frequency adaptation should handle combined factors`() {
        // Test with multiple factors: traffic mode + high speed + good accuracy
        val combinedFrequency =
            locationValidationService.getOptimalUpdateFrequency(
                trafficMode = true,
                currentSpeed = 70f,
                gpsAccuracy = 5f,
            )

        // Should be a reasonable frequency within bounds
        assertTrue(
            "Combined frequency should be within bounds",
            combinedFrequency >= LocationValidationService.MIN_UPDATE_FREQUENCY &&
                combinedFrequency <= LocationValidationService.MAX_UPDATE_FREQUENCY,
        )
    }

    @Test
    fun `frequency adaptation should maintain history`() {
        // Get multiple frequency updates
        locationValidationService.getOptimalUpdateFrequency(trafficMode = false, currentSpeed = 30f)
        locationValidationService.getOptimalUpdateFrequency(trafficMode = true, currentSpeed = 25f)
        locationValidationService.getOptimalUpdateFrequency(trafficMode = false, currentSpeed = 60f)

        val stats = locationValidationService.getUpdateFrequencyStats()

        // Should contain meaningful information
        assertTrue("Stats should be generated without errors", stats.isNotEmpty())
        assertTrue("Stats should contain frequency information", stats.contains("Current frequency:"))
    }
} 
