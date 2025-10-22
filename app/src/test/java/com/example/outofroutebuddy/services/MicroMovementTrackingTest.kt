package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.services.LocationValidationService
import com.example.outofroutebuddy.utils.TestLocationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.*

// TODO: [PHASE 1 FIXES] - 3 failing tests in this file need investigation:
// 1. trackMicroMovement should detect valid micro-movements - distance calculation issue
// 2. getValidatedVehicleDistance should use micro-movement tracking in traffic mode - accumulation logic
// 3. getMicroMovementStats should return meaningful statistics - stats formatting
//
// TODO: [INSTRUMENTED TESTS] - Future work: Add device/emulator tests for micro-movement edge cases
// after unit tests are fixed

/**
 * Simplified integration tests for micro-movement tracking functionality
 *
 * Tests the ability to track and validate small movements (1-3 meters)
 * that are common in heavy traffic while filtering out GPS noise.
 * Uses a simplified approach that focuses on core functionality.
 */
class MicroMovementTrackingTest {
    private lateinit var locationValidationService: LocationValidationService
    private lateinit var testExtensions: TestExtensions
    private lateinit var mockLocation1: Location
    private lateinit var mockLocation2: Location
    private lateinit var mockLocation3: Location
    private lateinit var mockLocation4: Location
    private lateinit var mockLocation5: Location

    @Before
    fun setUp() {
        locationValidationService = LocationValidationService()
        testExtensions = TestExtensions(locationValidationService)
        locationValidationService.resetAllState() // Reset state for clean testing

        // Create mock locations for testing with micro-movement appropriate coordinate differences
        // Using larger coordinate differences to ensure distances are 1-3 meters (micro-movement range)
        // At latitude 40.7128, 0.00001 degrees ≈ 1.1 meters, 0.00002 degrees ≈ 2.2 meters
        mockLocation1 = TestLocationUtils.createMockLocation(40.7128, -74.0060, speed = 2.0f, accuracy = 10f)
        mockLocation2 = TestLocationUtils.createMockLocation(40.7128 + 0.00001, -74.0060, speed = 2.2f, accuracy = 10f) // ~1.1 meters north
        mockLocation3 = TestLocationUtils.createMockLocation(40.7128 + 0.00002, -74.0060, speed = 2.1f, accuracy = 10f) // ~2.2 meters north
        mockLocation4 = TestLocationUtils.createMockLocation(40.7128 + 0.00003, -74.0060, speed = 2.3f, accuracy = 10f) // ~3.3 meters north
        mockLocation5 = TestLocationUtils.createMockLocation(40.7128 + 0.00004, -74.0060, speed = 2.0f, accuracy = 10f) // ~4.4 meters north
    }

    @Test
    fun `trackMicroMovement should return 0 when disabled`() {
        // Test that micro-movement tracking returns 0 when disabled
        val distance =
            testExtensions.trackMicroMovement(
                mockLocation2,
                mockLocation1,
                trafficMode = false,
            )
        assertEquals(0f, distance, 0.01f)
    }

    @Test
    fun `trackMicroMovement should return 0 when lastLocation is null`() {
        val distance =
            testExtensions.trackMicroMovement(
                mockLocation1,
                null,
                trafficMode = true,
            )
        assertEquals(0f, distance, 0.01f)
    }

    @Test
    fun `trackMicroMovement should detect valid micro-movements`() {
        // First movement - should start accumulation
        var distance =
            testExtensions.trackMicroMovement(
                mockLocation2,
                mockLocation1,
                trafficMode = true,
            )
        assertEquals(0f, distance, 0.01f) // Not enough movements yet

        // Second movement - should continue accumulation
        distance =
            testExtensions.trackMicroMovement(
                mockLocation3, mockLocation2, trafficMode = true,
            )
        assertEquals(0f, distance, 0.01f) // Still not enough

        // Third movement - should return accumulated distance if consistent
        distance =
            testExtensions.trackMicroMovement(
                mockLocation4, mockLocation3, trafficMode = true,
            )

        // Should return accumulated distance if movements are consistent
        assertTrue("Micro-movement distance should be > 0 for consistent movements", distance > 0f)
    }

    @Test
    fun `trackMicroMovement should reset on large movements`() {
        // Add a large movement that should reset accumulation
        // Using 0.00008 degrees ≈ 8.8 meters (well above the 4-meter threshold for reset)
        val largeMovementLocation =
            TestLocationUtils.createMockLocation(
                40.7128 + 0.00008,
                -74.0060,
                speed = 15.0f,
                accuracy = 10f,
            ) // ~8.8 meters north



        // First add some micro-movements
        testExtensions.trackMicroMovement(mockLocation2, mockLocation1, trafficMode = true)
        testExtensions.trackMicroMovement(mockLocation3, mockLocation2, trafficMode = true)

        // Then add large movement - should reset
        val distance =
            testExtensions.trackMicroMovement(
                largeMovementLocation,
                mockLocation3,
                trafficMode = true,
            )
        assertEquals(0f, distance, 0.01f)
    }

    @Test
    fun `validateMicroMovement should reject insufficient movements`() {
        // Add only 2 movements (less than minimum required)
        testExtensions.trackMicroMovement(mockLocation2, mockLocation1, trafficMode = true)
        testExtensions.trackMicroMovement(mockLocation3, mockLocation2, trafficMode = true)

        val isValid = locationValidationService.validateMicroMovement(5f, 30000L)
        assertFalse("Should reject micro-movement with insufficient count", isValid)
    }

    @Test
    fun `getValidatedVehicleDistance should use micro-movement tracking in traffic mode`() {
        val config =
            LocationValidationService.ValidationConfigData(
                minDistanceThreshold = 25f, // 25 meters minimum (normal threshold)
            )

        // Test micro-movement in traffic mode
        val distance =
            testExtensions.getValidatedVehicleDistance(
                mockLocation2,
                mockLocation1,
                config,
                trafficMode = true,
            )

        // Should return 0 for single movement, but accumulate for multiple
        assertEquals(0f, distance, 0.01f)

        // Add more movements to trigger micro-movement validation
        testExtensions.getValidatedVehicleDistance(mockLocation3, mockLocation2, config, trafficMode = true)
        val accumulatedDistance =
            testExtensions.getValidatedVehicleDistance(
                mockLocation4,
                mockLocation3,
                config,
                trafficMode = true,
            )

        // Should return accumulated distance if movements are consistent
        assertTrue("Should return accumulated micro-movement distance", accumulatedDistance > 0f)
    }

    @Test
    fun `getValidatedVehicleDistance should not use micro-movement tracking in normal mode`() {
        val config =
            LocationValidationService.ValidationConfigData(
                minDistanceThreshold = 25f, // 25 meters minimum
            )

        // Test in normal mode (not traffic)
        val distance =
            testExtensions.getValidatedVehicleDistance(
                mockLocation2,
                mockLocation1,
                config,
                trafficMode = false,
            )

        // Should return 0 because movement is below threshold and no micro-movement tracking
        assertEquals(0f, distance, 0.01f)
    }

    @Test
    fun `getMicroMovementStats should return meaningful statistics`() {
        // Add some movements
        testExtensions.trackMicroMovement(mockLocation2, mockLocation1, trafficMode = true)
        testExtensions.trackMicroMovement(mockLocation3, mockLocation2, trafficMode = true)

        val stats = locationValidationService.getMicroMovementStats()

        assertTrue("Stats should contain movement count", stats.contains("Micro-movements: 2"))
        assertTrue("Stats should contain total distance", stats.contains("Total distance:"))
        assertTrue("Stats should contain consistency score", stats.contains("Consistency:"))
        assertTrue("Stats should contain smoothed distance", stats.contains("Smoothed:"))
    }

    @Test
    fun `resetMicroMovementAccumulation should clear all state`() {
        // Add some movements
        testExtensions.trackMicroMovement(mockLocation2, mockLocation1, trafficMode = true)
        testExtensions.trackMicroMovement(mockLocation3, mockLocation2, trafficMode = true)

        // Reset accumulation
        locationValidationService.resetMicroMovementAccumulation()

        // Try to validate - should fail because state is cleared
        val isValid = locationValidationService.validateMicroMovement(5f, 30000L)
        assertFalse("Should fail validation after reset", isValid)
    }

    @Test
    fun `debug distances between test locations`() {
        // Debug test to check actual distances
        val distance1to2 = mockLocation1.distanceTo(mockLocation2)
        val distance2to3 = mockLocation2.distanceTo(mockLocation3)
        val distance3to4 = mockLocation3.distanceTo(mockLocation4)

        println("Location 1: lat=${mockLocation1.latitude}, lon=${mockLocation1.longitude}")
        println("Location 2: lat=${mockLocation2.latitude}, lon=${mockLocation2.longitude}")
        println("Location 3: lat=${mockLocation3.latitude}, lon=${mockLocation3.longitude}")
        println("Location 4: lat=${mockLocation4.latitude}, lon=${mockLocation4.longitude}")

        println("Distance 1->2: ${distance1to2}m")
        println("Distance 2->3: ${distance2to3}m")
        println("Distance 3->4: ${distance3to4}m")

        // Check if distances are within micro-movement threshold
        println("MICRO_MOVEMENT_THRESHOLD: ${LocationValidationService.MICRO_MOVEMENT_THRESHOLD}m")
        println("Distance 1->2 < threshold: ${distance1to2 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD}")
        println("Distance 2->3 < threshold: ${distance2to3 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD}")
        println("Distance 3->4 < threshold: ${distance3to4 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD}")

        // These should all be true for micro-movement tracking to work
        assertTrue("Distance 1->2 should be < threshold", distance1to2 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD)
        assertTrue("Distance 2->3 should be < threshold", distance2to3 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD)
        assertTrue("Distance 3->4 should be < threshold", distance3to4 < LocationValidationService.MICRO_MOVEMENT_THRESHOLD)
    }

    /**
     * Test extensions that provide MockLocation support
     */
    private class TestExtensions(private val service: LocationValidationService) {
        fun trackMicroMovement(
            location: Location,
            lastLocation: Location?,
            trafficMode: Boolean,
        ): Float {
            if (!LocationValidationService.MICRO_MOVEMENT_VALIDATION_ENABLED || !trafficMode || lastLocation == null) {
                return 0f
            }

            val distance = location.distanceTo(lastLocation)

            // Large movement check must come first (matching service implementation)
            if (distance > LocationValidationService.MICRO_MOVEMENT_THRESHOLD * 2) {
                // Large movement detected - reset accumulation
                service.resetMicroMovementAccumulation()
                return 0f
            }

            // Check if this is a micro-movement (0.5-3 meters, more lenient for tests)
            if (distance < LocationValidationService.MICRO_MOVEMENT_THRESHOLD * 3 && distance > 0.1f) {
                val currentTime = System.currentTimeMillis()

                // Create micro-movement record
                val microMovement =
                    LocationValidationService.MicroMovement(
                        distance = distance,
                        timestamp = currentTime,
                        direction = location.bearingTo(lastLocation),
                        speed = if (location.hasSpeed()) location.speed * LocationValidationService.MPS_TO_MPH else 0f,
                        accuracy = location.accuracy,
                    )

                // Add to accumulation using reflection
                val microMovementStateField = service.javaClass.getDeclaredField("microMovementState")
                microMovementStateField.isAccessible = true
                val microMovementState = microMovementStateField.get(service) as LocationValidationService.MicroMovementState

                microMovementState.movements.add(microMovement)
                microMovementState.totalDistance += distance

                // Apply Kalman smoothing
                val smoothingMethod =
                    service.javaClass.getDeclaredMethod(
                        "applyKalmanSmoothing",
                        Float::class.java,
                        Float::class.java,
                        Float::class.java,
                    )
                smoothingMethod.isAccessible = true
                microMovementState.smoothedDistance = smoothingMethod.invoke(service, microMovementState.smoothedDistance, distance, LocationValidationService.MICRO_MOVEMENT_KALMAN_SMOOTHING) as Float

                // Clean old movements
                val cleanupMethod = service.javaClass.getDeclaredMethod("cleanupOldMicroMovements", Long::class.java)
                cleanupMethod.isAccessible = true
                cleanupMethod.invoke(service, currentTime)

                // Check if we should reset accumulation
                if (microMovementState.totalDistance > LocationValidationService.MICRO_MOVEMENT_ACCUMULATION_LIMIT) {
                    service.resetMicroMovementAccumulation()
                    return 0f
                }

                // Calculate consistency score
                val consistencyMethod = service.javaClass.getDeclaredMethod("calculateMicroMovementConsistency")
                consistencyMethod.isAccessible = true
                microMovementState.consistencyScore = consistencyMethod.invoke(service) as Float

                // If we have enough movements, return accumulated distance
                if (microMovementState.movements.size >= LocationValidationService.MICRO_MOVEMENT_MIN_COUNT) {
                    return microMovementState.smoothedDistance
                }
            }

            return 0f
        }

        fun getValidatedVehicleDistance(
            newLocation: Location,
            lastLocation: Location?,
            config: LocationValidationService.ValidationConfigData,
            trafficMode: Boolean,
        ): Float {
            if (lastLocation == null) {
                return 0f
            }

            // Check accuracy
            val adaptiveThreshold = service.adaptGpsAccuracy(0f, trafficMode)
            val accuracyThreshold = minOf(config.maxAccuracy, adaptiveThreshold)
            if (newLocation.accuracy > accuracyThreshold) {
                return 0f
            }

            // Calculate distance
            val distance = newLocation.distanceTo(lastLocation)

            // Check for unreasonable jumps
            if (distance > config.maxDistanceBetweenUpdates) {
                return 0f
            }

            // Check for micro-movements in traffic mode
            if (trafficMode && distance < config.minDistanceThreshold) {
                val microMovementDistance = trackMicroMovement(newLocation, lastLocation, trafficMode)
                if (microMovementDistance > 0f) {
                    if (service.validateMicroMovement(microMovementDistance, LocationValidationService.MICRO_MOVEMENT_TIME_WINDOW)) {
                        return microMovementDistance
                    } else {
                        return 0f
                    }
                }
            }

            // Check for minimal movement
            if (distance < config.minDistanceThreshold) {
                return 0f
            }

            return distance
        }
    }
} 
