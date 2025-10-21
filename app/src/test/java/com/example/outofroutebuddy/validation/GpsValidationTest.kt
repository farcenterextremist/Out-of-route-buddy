package com.example.outofroutebuddy.validation

import android.location.Location
import com.example.outofroutebuddy.services.LocationValidationService
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Note: This test file contains validation tests for GPS functionality

/**
 * ✅ NEW: Comprehensive GPS validation tests
 *
 * These tests verify that GPS validation logic works correctly for vehicle tracking
 * and catches various edge cases that could affect trip accuracy.
 */
class GpsValidationTest {
    private lateinit var locationValidationService: LocationValidationService
    private lateinit var validationFramework: ValidationFramework

    @Before
    fun setUp() {
        locationValidationService = LocationValidationService()
    }

    @Test
    fun `validateGpsDataForTrip with excellent GPS data should pass`() {
        // Given: Excellent GPS conditions
        val gpsAccuracy = 5.0f // Excellent accuracy
        val gpsAge = 5000L // 5 seconds old
        val totalGpsPoints = 50
        val validGpsPoints = 48 // 96% quality

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertTrue("GPS validation should pass with excellent data", result.isValid)
        assertTrue("Should have no errors", result.errors.isEmpty())
        assertTrue("Should have no warnings", result.warnings.isEmpty())
    }

    @Test
    fun `validateGpsDataForTrip with poor accuracy should generate warning`() {
        // Given: Poor GPS accuracy
        val gpsAccuracy = 35.0f // Poor accuracy
        val gpsAge = 5000L
        val totalGpsPoints = 20
        val validGpsPoints = 15

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertTrue("GPS validation should pass with warnings", result.isValid)
        assertTrue("Should have no errors", result.errors.isEmpty())
        assertTrue("Should have warnings for poor accuracy", result.warnings.isNotEmpty())
        assertTrue("Warning should mention accuracy", result.warnings.any { it.field == "GPS Accuracy" })
    }

    @Test
    fun `validateGpsDataForTrip with critically poor accuracy should fail`() {
        // Given: Critically poor GPS accuracy
        val gpsAccuracy = 75.0f // Critically poor accuracy
        val gpsAge = 5000L
        val totalGpsPoints = 10
        val validGpsPoints = 5

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertFalse("GPS validation should fail with critically poor accuracy", result.isValid)
        assertTrue("Should have errors", result.errors.isNotEmpty())
        assertTrue(
            "Error should mention critically poor accuracy",
            result.errors.any { it.message.contains("critically poor") },
        )
    }

    @Test
    fun `validateGpsDataForTrip with stale data should fail`() {
        // Given: Stale GPS data
        val gpsAccuracy = 10.0f
        val gpsAge = 60000L // 60 seconds old (too old)
        val totalGpsPoints = 30
        val validGpsPoints = 25

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertFalse("GPS validation should fail with stale data", result.isValid)
        assertTrue("Should have errors", result.errors.isNotEmpty())
        assertTrue(
            "Error should mention data age",
            result.errors.any { it.field == "GPS Data Age" },
        )
    }

    @Test
    fun `validateGpsDataForTrip with poor quality percentage should generate warning`() {
        // Given: Poor GPS quality percentage (50% - should be a warning, not error)
        val gpsAccuracy = 15.0f
        val gpsAge = 5000L
        val totalGpsPoints = 20
        val validGpsPoints = 10 // 50% quality

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then - 50% quality should be a warning, not an error
        assertTrue("GPS validation should pass with warnings for 50% quality", result.isValid)
        assertTrue("Should have warnings for poor quality", result.warnings.isNotEmpty())
        assertTrue(
            "Warning should mention quality threshold",
            result.warnings.any { it.field == "GPS Quality" },
        )
    }

    @Test
    fun `validateGpsDataForTrip with insufficient data points should generate warning`() {
        // Given: Insufficient GPS data points
        val gpsAccuracy = 10.0f
        val gpsAge = 5000L
        val totalGpsPoints = 3 // Very few points
        val validGpsPoints = 3

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertTrue("GPS validation should pass with warnings", result.isValid)
        assertTrue("Should have warnings for insufficient data", result.warnings.isNotEmpty())
        assertTrue(
            "Warning should mention limited GPS data",
            result.warnings.any { it.field == "GPS Data Points" },
        )
    }

    @Test
    fun `validateGpsDataForTrip with no GPS data should fail`() {
        // Given: No GPS data
        val gpsAccuracy = 0.0f
        val gpsAge = 0L
        val totalGpsPoints = 0
        val validGpsPoints = 0

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertFalse("GPS validation should fail with no data", result.isValid)
        assertTrue("Should have errors", result.errors.isNotEmpty())
        assertTrue(
            "Error should mention no GPS data",
            result.errors.any { it.message.contains("No GPS data") },
        )
    }

    @Test
    fun `validateVehicleLocation with valid location should pass`() {
        // Given: Valid vehicle location with recent timestamp
        val currentTime = System.currentTimeMillis()
        val location =
            createMockLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                accuracy = 10.0f,
                speed = 15.0f, // 15 m/s = ~33 mph
                time = currentTime,
            )

        // When
        val result =
            locationValidationService.validateVehicleLocation(
                location = location,
                lastLocation = null,
                lastUpdateTime = currentTime,
                lastSpeed = 0f, // No previous location, so lastSpeed is 0f
                config = com.example.outofroutebuddy.services.LocationValidationService.ValidationConfigData(),
                trafficMode = false,
                currentTime = currentTime,
                distanceCalculator = null,
                timeCalculator = null,
                speedCalculator = null
            )

        // Then
        assertTrue("Vehicle location validation should pass", result is com.example.outofroutebuddy.services.LocationValidationService.ValidationResult.Valid)
    }

    @Test
    fun `validateVehicleLocation with poor accuracy should fail`() {
        // Given: Location with poor accuracy (45m is above VEHICLE_MIN_ACCURACY of 20m)
        val currentTime = System.currentTimeMillis()
        val location =
            createMockLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                accuracy = 45.0f, // Poor accuracy - above 20m threshold
                speed = 15.0f,
                time = currentTime,
            )

        // When
        val result =
            locationValidationService.validateVehicleLocation(
                location = location,
                lastLocation = null,
                lastUpdateTime = currentTime,
                lastSpeed = 0f, // No previous location, so lastSpeed is 0f
                config = com.example.outofroutebuddy.services.LocationValidationService.ValidationConfigData(),
                trafficMode = false,
                currentTime = currentTime,
                distanceCalculator = null,
                timeCalculator = null,
                speedCalculator = null
            )

        assertFalse(
            "Vehicle location validation should fail with poor accuracy",
            result is LocationValidationService.ValidationResult.Valid,
        )
        if (result is LocationValidationService.ValidationResult.Invalid) {
            assertTrue(
                "Should mention accuracy issue",
                (result as? LocationValidationService.ValidationResult.Invalid)?.reason?.contains("accuracy") == true,
            )
        }
    }

    @Test
    fun `validateVehicleLocation with unrealistic speed should fail`() {
        // Given: Location with unrealistic speed (50 m/s = ~112 mph, above 80 mph limit)
        val currentTime = System.currentTimeMillis()
        val location =
            createMockLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                accuracy = 10.0f,
                speed = 50.0f, // 50 m/s = ~112 mph (above VEHICLE_MAX_SPEED_MPH of 80)
                time = currentTime,
            )

        // When
        val result =
            locationValidationService.validateVehicleLocation(
                location = location,
                lastLocation = null,
                lastUpdateTime = currentTime,
                lastSpeed = 0f, // No previous location, so lastSpeed is 0f
                config = com.example.outofroutebuddy.services.LocationValidationService.ValidationConfigData(),
                trafficMode = false,
                currentTime = currentTime,
                distanceCalculator = null,
                timeCalculator = null,
                speedCalculator = { 50.0f }
            )

        assertFalse(
            "Vehicle location validation should fail with unrealistic speed",
            result is LocationValidationService.ValidationResult.Valid,
        )
        if (result is LocationValidationService.ValidationResult.Invalid) {
            assertTrue(
                "Should mention speed issue",
                (result as? LocationValidationService.ValidationResult.Invalid)?.reason?.contains("speed") == true,
            )
        }
    }

    @Test
    fun `validateVehicleLocation with location jump should fail`() {
        // Given: Two locations with unrealistic movement (6.9 miles in 1 second = ~24,800 mph)
        val now = System.currentTimeMillis() // Use current time to avoid "too old" issue
        val lastLocation =
            android.location.Location("test").apply {
                latitude = 40.7128
                longitude = -74.0060
                accuracy = 10.0f
                speed = 15.0f
                time = now - 1000 // 1 second ago
            }
        val currentLocation =
            android.location.Location("test").apply {
                latitude = 40.8128 // ~6.9 miles north
                longitude = -74.0060
                accuracy = 10.0f
                speed = 15.0f
                time = now // Use current time
            }
        // When
        val result =
            locationValidationService.validateVehicleLocation(
                location = currentLocation,
                lastLocation = lastLocation,
                lastUpdateTime = now - 1000,
                lastSpeed = lastLocation.speed, // Use lastLocation's speed
                currentTime = now,
                distanceCalculator = { _, _ -> 11000.0f },
                timeCalculator = { location ->
                    when (location.latitude) {
                        40.8128 -> now // currentLocation
                        40.7128 -> now - 1000 // lastLocation
                        else -> now
                    }
                },
                config = com.example.outofroutebuddy.services.LocationValidationService.ValidationConfigData(),
                trafficMode = false
            )
        assertFalse(
            "Vehicle location validation should fail with location jump",
            result is LocationValidationService.ValidationResult.Valid,
        )
        if (result is LocationValidationService.ValidationResult.Invalid) {
            assertTrue(
                "Should mention movement issue",
                (result as? LocationValidationService.ValidationResult.Invalid)?.reason?.contains("position change") == true,
            )
        }
    }

    /**
     * Helper method to create mock Location objects for testing
     */
    private fun createMockLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float,
        time: Long,
    ): Location {
        return mockk<Location>(relaxed = true).apply {
            every { this@apply.latitude } returns latitude
            every { this@apply.longitude } returns longitude
            every { this@apply.accuracy } returns accuracy
            every { this@apply.speed } returns speed
            every { this@apply.time } returns time
            every { hasSpeed() } returns (speed > 0f)
            // Ensure the mock properly returns the latitude and longitude values
            every { this@apply.latitude } returns latitude
            every { this@apply.longitude } returns longitude
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
            kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }
} 
