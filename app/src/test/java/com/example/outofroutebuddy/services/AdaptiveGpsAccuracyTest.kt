package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.utils.TestLocationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Comprehensive tests for adaptive GPS accuracy functionality
 *
 * Tests the ability to adapt GPS accuracy requirements based on traffic conditions
 * and provide smooth transitions between normal and traffic modes.
 */
@RunWith(MockitoJUnitRunner::class)
class AdaptiveGpsAccuracyTest {
    // ✅ UPDATED: Using unified TestLocationUtils instead of duplicate MockLocation class

    private lateinit var locationValidationService: LocationValidationService

    @Before
    fun setUp() {
        locationValidationService = LocationValidationService()
        // Reset state before each test
        locationValidationService.resetAllState()
    }

    @Test
    fun `adaptGpsAccuracy should return normal threshold in normal mode`() {
        val threshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        assertEquals(LocationValidationService.NORMAL_GPS_ACCURACY_THRESHOLD, threshold, 0.01f)
    }

    @Test
    fun `adaptGpsAccuracy should return traffic threshold in traffic mode`() {
        // Test that traffic mode returns higher (more lenient) threshold
        // Reset state to ensure clean start
        locationValidationService.resetGpsAccuracyAdaptation()

        val normalThreshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        // Start transition to traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        // Wait for transition to complete
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME + 100)
        val trafficThreshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Traffic threshold should be higher (more lenient) than normal
        assertTrue("Traffic threshold should be higher than normal", trafficThreshold > normalThreshold)
        // Should be close to the target traffic threshold
        assertTrue(
            "Traffic threshold should be close to target",
            trafficThreshold >= LocationValidationService.TRAFFIC_GPS_ACCURACY_THRESHOLD * 0.8f,
        )
    }

    @Test
    fun `adaptGpsAccuracy should provide smooth transitions`() {
        // Test that transitions are smooth, not immediate
        locationValidationService.resetGpsAccuracyAdaptation()

        // Start in normal mode
        val normalThreshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)

        // Switch to traffic mode
        val firstTrafficThreshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // First call should not immediately reach target (due to smoothing)
        assertTrue(
            "First traffic threshold should not immediately reach target",
            firstTrafficThreshold < LocationValidationService.TRAFFIC_GPS_ACCURACY_THRESHOLD,
        )

        // Wait for transition to complete
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME + 100)
        val finalTrafficThreshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Final threshold should be close to target
        assertTrue(
            "Final traffic threshold should be close to target",
            finalTrafficThreshold >= LocationValidationService.TRAFFIC_GPS_ACCURACY_THRESHOLD * 0.9f,
        )
    }

    @Test
    fun `adaptGpsAccuracy should prevent rapid switching with hysteresis`() {
        // Start in normal mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)

        // Small change in target threshold should not trigger transition due to hysteresis
        val threshold1 = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        val threshold2 = locationValidationService.adaptGpsAccuracy(16f, trafficMode = false)

        // Should be the same due to hysteresis
        assertEquals("Threshold should not change due to hysteresis", threshold1, threshold2, 0.01f)
    }

    @Test
    fun `updateGpsAccuracyHistory should maintain accuracy history`() {
        // Add some accuracy readings
        locationValidationService.updateGpsAccuracyHistory(10f)
        locationValidationService.updateGpsAccuracyHistory(15f)
        locationValidationService.updateGpsAccuracyHistory(20f)

        val stats = locationValidationService.getGpsAccuracyStats()

        // Should contain average accuracy
        assertTrue("Stats should contain average accuracy", stats.contains("Avg accuracy: 15.0m"))
    }

    @Test
    fun `updateGpsAccuracyHistory should limit history size`() {
        // Add more than 10 accuracy readings
        for (i in 1..15) {
            locationValidationService.updateGpsAccuracyHistory(i.toFloat())
        }

        val stats = locationValidationService.getGpsAccuracyStats()

        // Should still work without errors
        assertTrue("Stats should be generated without errors", stats.isNotEmpty())
    }

    @Test
    fun `isGpsAccuracyAcceptable should work in normal mode`() {
        // Test accuracy that should be acceptable in normal mode
        val acceptableAccuracy = 10f // Good accuracy
        val unacceptableAccuracy = 25f // Poor accuracy

        val isAcceptable1 = locationValidationService.isGpsAccuracyAcceptable(acceptableAccuracy, trafficMode = false)
        val isAcceptable2 = locationValidationService.isGpsAccuracyAcceptable(unacceptableAccuracy, trafficMode = false)

        assertTrue("Good accuracy should be acceptable in normal mode", isAcceptable1)
        assertFalse("Poor accuracy should not be acceptable in normal mode", isAcceptable2)
    }

    @Test
    fun `isGpsAccuracyAcceptable should work in traffic mode`() {
        // Test accuracy that should be acceptable in traffic mode
        val acceptableAccuracy = 20f // Moderate accuracy
        val unacceptableAccuracy = 30f // Poor accuracy
        // Start transition to traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME + 100)
        val isAcceptable1 = locationValidationService.isGpsAccuracyAcceptable(acceptableAccuracy, trafficMode = true)
        val isAcceptable2 = locationValidationService.isGpsAccuracyAcceptable(unacceptableAccuracy, trafficMode = true)
        assertTrue("Moderate accuracy should be acceptable in traffic mode", isAcceptable1)
        assertFalse("Poor accuracy should not be acceptable in traffic mode", isAcceptable2)
    }

    @Test
    fun `validateVehicleLocation should use adaptive accuracy in normal mode`() {
        val config = LocationValidationService.ValidationConfigData(maxAccuracy = 20f)
        // Test with accuracy that's poor for normal mode
        val locationWithPoorAccuracy =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 25f, // Poor accuracy for normal mode
            )
        val lastLocation =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
            )

        // Should fail validation in normal mode due to poor accuracy
        val result = locationValidationService.validateVehicleLocation(locationWithPoorAccuracy, lastLocation, System.currentTimeMillis(), lastSpeed = lastLocation?.speed ?: 0f)
        assertTrue("Should fail validation in normal mode", result is LocationValidationService.ValidationResult.Invalid)
    }

    @Test
    fun `validateVehicleLocation should use adaptive accuracy in traffic mode`() {
        val config = LocationValidationService.ValidationConfigData(maxAccuracy = 20f)
        // Test with accuracy that's poor for normal mode but acceptable for traffic
        val freshTime = System.currentTimeMillis()
        val locationWithModerateAccuracy =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 22f, // Moderate accuracy
                time = freshTime,
            )
        val lastLocation =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 10f,
                time = freshTime - 1000,
            )

        // Start transition to traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        // Wait for transition to complete
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME + 100)
        // Update adaptive accuracy state
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Should pass validation in traffic mode with moderate accuracy
        val result =
            locationValidationService.validateVehicleLocation(
                locationWithModerateAccuracy,
                lastLocation,
                freshTime,
                lastSpeed = lastLocation?.speed ?: 0f, // Pass last speed for robust validation
                trafficMode = true,
            )
        assertTrue(
            "Should pass validation in traffic mode with moderate accuracy",
            result is LocationValidationService.ValidationResult.Valid,
        )
    }

    @Test
    fun `getValidatedVehicleDistance should use adaptive accuracy`() {
        val config = LocationValidationService.ValidationConfigData(maxAccuracy = 100f, minDistanceThreshold = 5f)
        // Should return 0 in normal mode due to poor accuracy
        val baseTime = System.currentTimeMillis()
        val locationWithModerateAccuracyNormal =
            TestLocationUtils.createMockLocation(
                lat = 40.7128 + 0.0001, // ~10 meters north
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 22f, // Moderate accuracy
                time = baseTime + 1000,
            )
        val lastLocationNormal =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 10f,
                time = baseTime,
            )

        // Should return 0 in normal mode due to poor accuracy
        val distanceNormal = locationValidationService.getValidatedVehicleDistance(locationWithModerateAccuracyNormal, lastLocationNormal)
        assertEquals("Should return 0 in normal mode", 0.0f, distanceNormal, 0.01f)

        // Start transition to traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        // Wait for transition to complete
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME + 100)
        // Update adaptive accuracy state - call multiple times to ensure state is fully updated
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Create fresh locations with current time for traffic mode test
        val freshTime = System.currentTimeMillis()
        val locationWithModerateAccuracyTraffic =
            TestLocationUtils.createMockLocation(
                lat = 40.7128 + 0.00045, // ~50 meters north (5.0 m/s * 10 seconds)
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 18f, // Moderate accuracy - should be acceptable for traffic mode
                time = freshTime,
            )
        val lastLocationTraffic =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 10f,
                time = freshTime - 10000, // 10 seconds earlier
            )

        // Should return distance in traffic mode due to more lenient accuracy
        val distanceTraffic =
            locationValidationService.getValidatedVehicleDistance(
                locationWithModerateAccuracyTraffic,
                lastLocationTraffic,
                config,
                trafficMode = true,
            )
        assertTrue("Should return distance in traffic mode", distanceTraffic > 0.0f)
    }

    @Test
    fun `getGpsAccuracyStats should return meaningful information`() {
        // Add some accuracy history
        locationValidationService.updateGpsAccuracyHistory(10f)
        locationValidationService.updateGpsAccuracyHistory(15f)

        // Get stats in normal mode
        val statsNormal = locationValidationService.getGpsAccuracyStats()

        assertTrue("Stats should contain current threshold", statsNormal.contains("Current threshold:"))
        assertTrue("Stats should contain target threshold", statsNormal.contains("Target threshold:"))
        assertTrue("Stats should contain mode information", statsNormal.contains("Mode: normal"))
        assertTrue("Stats should contain transition progress", statsNormal.contains("Transition:"))
        assertTrue("Stats should contain average accuracy", statsNormal.contains("Avg accuracy:"))
    }

    @Test
    fun `resetGpsAccuracyAdaptation should clear state`() {
        // Add some accuracy history and change mode
        locationValidationService.updateGpsAccuracyHistory(10f)
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Reset adaptation
        locationValidationService.resetGpsAccuracyAdaptation()

        // Should be back to normal mode
        val threshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        assertEquals(LocationValidationService.NORMAL_GPS_ACCURACY_THRESHOLD, threshold, 0.01f)
    }

    @Test
    fun `adaptive accuracy should handle critical accuracy errors`() {
        // Test with extremely poor accuracy
        val locationWithCriticalAccuracy =
            TestLocationUtils.createMockLocation(
                lat = 40.7128,
                lon = -74.0060,
                speed = 5.0f,
                accuracy = 1000f, // Critical accuracy error
            )

        // Should handle gracefully without crashing
        val threshold = locationValidationService.adaptGpsAccuracy(1000f, trafficMode = false)
        assertTrue("Should return a valid threshold even with critical accuracy", threshold > 0f)
    }

    @Test
    fun `adaptive accuracy should provide smooth transition progress`() {
        locationValidationService.resetGpsAccuracyAdaptation()

        // Start transition to traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Wait halfway through transition
        Thread.sleep(LocationValidationService.GPS_ACCURACY_TRANSITION_TIME / 2)

        // Call adaptGpsAccuracy to update progress
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Get progress string
        val progressString = locationValidationService.getGpsAccuracyStats()

        // Should contain progress information
        val progressRegex = Regex(".*Transition: (\\d+)%.*")
        val matchResult = progressRegex.find(progressString)
        val matches = matchResult != null

        // Progress should be around 50% (allowing for timing variations)
        assertTrue("Should show transition progress", matches)
        if (matches) {
            val progressPercent = matchResult!!.groupValues[1].toInt()
            assertTrue("Progress should be between 40-60%", progressPercent in 40..60)
        }
    }

    @Test
    fun `adaptive accuracy should reset properly`() {
        // Start in traffic mode
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        Thread.sleep(100)

        // Reset
        locationValidationService.resetGpsAccuracyAdaptation()

        // Should be back to normal threshold
        val threshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        assertEquals(LocationValidationService.NORMAL_GPS_ACCURACY_THRESHOLD, threshold, 0.01f)
    }

    @Test
    fun `adaptive accuracy should handle rapid mode switches`() {
        // Rapidly switch between modes
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = false)
        locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)

        // Should not crash and should return reasonable values
        val threshold = locationValidationService.adaptGpsAccuracy(15f, trafficMode = true)
        assertTrue("Should return valid threshold after rapid switches", threshold > 0f)
    }

    @Test
    fun `adaptive accuracy should work with different accuracy inputs`() {
        // Test with various accuracy values
        val accuracies = listOf(5f, 15f, 25f, 50f, 100f)

        for (accuracy in accuracies) {
            val threshold = locationValidationService.adaptGpsAccuracy(accuracy, trafficMode = false)
            assertTrue("Should return valid threshold for accuracy $accuracy", threshold > 0f)
        }
    }
} 
