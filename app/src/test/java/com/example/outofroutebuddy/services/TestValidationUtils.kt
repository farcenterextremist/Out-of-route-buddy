package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.util.FormatUtils
import org.slf4j.LoggerFactory

/**
 * ✅ NEW: Shared test utilities for validation tests
 * This eliminates code duplication across validation test files
 *
 * BENEFITS:
 * - Eliminates duplicate test setup code
 * - Provides consistent test data creation
 * - Makes tests more readable and maintainable
 * - Enables easy addition of new test scenarios
 */
object TestValidationUtils {
    private val logger = LoggerFactory.getLogger(TestValidationUtils::class.java)

    // Constants for test scenarios - now using centralized config
    const val TEST_GOOD_ACCURACY = ValidationConfig.TEST_GOOD_ACCURACY
    const val TEST_POOR_ACCURACY = ValidationConfig.TEST_POOR_ACCURACY
    const val TEST_CRITICAL_ACCURACY = ValidationConfig.TEST_CRITICAL_ACCURACY
    const val TEST_NORMAL_SPEED_MPH = ValidationConfig.TEST_NORMAL_SPEED_MPH
    const val TEST_HIGH_SPEED_MPH = ValidationConfig.TEST_HIGH_SPEED_MPH
    const val TEST_UNREALISTIC_SPEED_MPH = ValidationConfig.TEST_UNREALISTIC_SPEED_MPH
    const val TEST_TRAFFIC_SPEED_MPH = ValidationConfig.TEST_TRAFFIC_SPEED_MPH

    /**
     * ✅ NEW: Create a mock location with specified parameters
     */
    fun createMockLocation(
        latitude: Double = 40.7128,
        longitude: Double = -74.0060,
        accuracy: Float = TEST_GOOD_ACCURACY,
        speed: Float = TEST_NORMAL_SPEED_MPH / 2.23694f, // Convert mph to m/s
        time: Long = System.currentTimeMillis(),
    ): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
            this.accuracy = accuracy
            this.speed = speed
            this.time = time
        }
    }

    /**
     * ✅ NEW: Create a mock location for poor accuracy testing
     */
    fun createPoorAccuracyLocation(): Location {
        return createMockLocation(accuracy = TEST_POOR_ACCURACY)
    }

    /**
     * ✅ NEW: Create a mock location for critical accuracy testing
     */
    fun createCriticalAccuracyLocation(): Location {
        return createMockLocation(accuracy = TEST_CRITICAL_ACCURACY)
    }

    /**
     * ✅ NEW: Create a mock location for unrealistic speed testing
     */
    fun createUnrealisticSpeedLocation(): Location {
        return createMockLocation(speed = TEST_UNREALISTIC_SPEED_MPH / 2.23694f)
    }

    /**
     * ✅ NEW: Create a mock location for traffic speed testing
     */
    fun createTrafficSpeedLocation(): Location {
        return createMockLocation(speed = TEST_TRAFFIC_SPEED_MPH / 2.23694f)
    }

    /**
     * ✅ NEW: Create a mock location for high acceleration testing
     */
    fun createHighAccelerationLocation(
        previousLocation: Location,
        timeDiffSeconds: Long = 2L,
    ): Location {
        val newLocation = createMockLocation()
        newLocation.time = previousLocation.time + (timeDiffSeconds * 1000)
        newLocation.speed = previousLocation.speed * 3f // 3x speed for high acceleration
        return newLocation
    }

    /**
     * ✅ NEW: Create a mock location for location jump testing
     */
    fun createLocationJumpLocation(
        previousLocation: Location,
        distanceMeters: Float = 1000f,
    ): Location {
        val newLocation = createMockLocation()
        // Move north by the specified distance
        val latOffset = distanceMeters / 111320.0 // Approximate meters per degree latitude
        newLocation.latitude = previousLocation.latitude + latOffset
        newLocation.time = previousLocation.time + 1000 // 1 second later
        return newLocation
    }

    /**
     * ✅ NEW: Create a mock location for stationary testing
     */
    fun createStationaryLocation(
        previousLocation: Location,
        timeDiffSeconds: Long = 300L, // 5 minutes
    ): Location {
        val newLocation =
            createMockLocation(
                latitude = previousLocation.latitude,
                longitude = previousLocation.longitude,
            )
        newLocation.time = previousLocation.time + (timeDiffSeconds * 1000)
        newLocation.speed = 0f
        return newLocation
    }

    /**
     * ✅ NEW: Create a validation config for testing
     */
    fun createTestValidationConfig(
        maxAccuracy: Float = LocationValidationService.DEFAULT_MAX_ACCURACY,
        maxSpeedChange: Float = LocationValidationService.DEFAULT_MAX_SPEED_CHANGE,
        minDistanceThreshold: Float = LocationValidationService.DEFAULT_MIN_DISTANCE_THRESHOLD,
        maxStationaryTime: Long = ValidationConfig.MAX_STATIONARY_TIME,
        maxDistanceBetweenUpdates: Float = LocationValidationService.DEFAULT_MAX_DISTANCE_BETWEEN_UPDATES,
    ): LocationValidationService.ValidationConfigData {
        return LocationValidationService.ValidationConfigData(
            maxAccuracy = maxAccuracy,
            maxSpeedChange = maxSpeedChange,
            minDistanceThreshold = minDistanceThreshold,
            maxStationaryTime = maxStationaryTime,
            maxDistanceBetweenUpdates = maxDistanceBetweenUpdates,
        )
    }

    /**
     * ✅ NEW: Create a vehicle-specific validation config for testing
     */
    fun createVehicleTestConfig(): LocationValidationService.ValidationConfigData {
        return createTestValidationConfig(
            maxAccuracy = LocationValidationService.VEHICLE_MIN_ACCURACY,
            minDistanceThreshold = 25f, // Vehicle-specific distance threshold
        )
    }

    /**
     * ✅ NEW: Create a traffic-aware validation config for testing
     * Note: Traffic validation now uses centralized ValidationConfig constants
     */
    fun createTrafficTestConfig(): LocationValidationService.ValidationConfigData {
        return LocationValidationService.ValidationConfigData(
            maxAccuracy = ValidationConfig.TRAFFIC_ACCURACY_THRESHOLD,
            minDistanceThreshold = ValidationConfig.MICRO_MOVEMENT_THRESHOLD * 2.5f, // 2.5x micro-movement threshold for traffic
            maxStationaryTime = ValidationConfig.MICRO_MOVEMENT_TIME_WINDOW * 2L, // 2x micro-movement time window for traffic
        )
    }

    /**
     * ✅ NEW: Assert validation result with detailed error information
     */
    fun assertValidationFailed(
        result: LocationValidationService.ValidationResult,
        expectedReasonContains: String,
        testName: String = "Validation test",
    ) {
        if (result !is LocationValidationService.ValidationResult.Invalid) {
            throw AssertionError("$testName: Expected validation to fail, but it passed")
        }

        if (!result.reason.contains(expectedReasonContains)) {
            throw AssertionError("$testName: Expected reason to contain '$expectedReasonContains', but got '${result.reason}'")
        }

        logger.debug("$testName: Validation correctly failed with reason: ${result.reason}")
    }

    /**
     * ✅ NEW: Assert validation passed
     */
    fun assertValidationPassed(
        result: LocationValidationService.ValidationResult,
        testName: String = "Validation test",
    ) {
        if (result !is LocationValidationService.ValidationResult.Valid) {
            throw AssertionError(
                "$testName: Expected validation to pass, but it failed with reason: ${(result as LocationValidationService.ValidationResult.Invalid).reason}",
            )
        }

        logger.debug("$testName: Validation correctly passed")
    }

    /**
     * ✅ NEW: Calculate expected distance between two locations
     */
    fun calculateExpectedDistance(
        location1: Location,
        location2: Location,
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location1.latitude,
            location1.longitude,
            location2.latitude,
            location2.longitude,
            results,
        )
        return results[0]
    }

    /**
     * ✅ NEW: Log test scenario details for debugging
     */
    fun logTestScenario(
        scenario: String,
        location: Location,
        lastLocation: Location? = null,
        config: LocationValidationService.ValidationConfigData? = null,
    ) {
        logger.debug("Test Scenario: $scenario")
        logger.debug(
            "  Location: lat=${location.latitude}, lon=${location.longitude}, accuracy=${location.accuracy}m, speed=${location.speed * 2.23694f}mph",
        )
        if (lastLocation != null) {
            logger.debug(
                "  Last Location: lat=${lastLocation.latitude}, lon=${lastLocation.longitude}, accuracy=${lastLocation.accuracy}m, speed=${lastLocation.speed * 2.23694f}mph",
            )
            val distance = calculateExpectedDistance(lastLocation, location)
            logger.debug("  Distance: ${FormatUtils.formatMeters(distance.toDouble())}")
        }
        if (config != null) {
            logger.debug("  Config: maxAccuracy=${config.maxAccuracy}m, maxSpeedChange=${config.maxSpeedChange}mph")
        }
    }
} 
