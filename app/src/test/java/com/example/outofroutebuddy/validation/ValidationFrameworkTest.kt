package com.example.outofroutebuddy.validation

import com.example.outofroutebuddy.data.entities.TripEntity
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

/**
 * ✅ NEW: Comprehensive tests for the unified validation framework
 *
 * These tests verify that all validation logic works correctly and catches edge cases.
 * This ensures data integrity and prevents runtime errors.
 */
class ValidationFrameworkTest {
    @Test
    fun `validateTripInput with valid data should pass`() {
        // Given
        val loadedMiles = "100.5"
        val bounceMiles = "25.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertTrue("Validation should pass for valid data", result.isValid)
        assertFalse("Should have no errors", result.hasErrors)
        assertFalse("Should have no warnings", result.hasWarnings)
        assertNotNull("Should return sanitized values", result.sanitizedValue)
    }

    @Test
    fun `validateTripInput with empty loaded miles should fail`() {
        // Given
        val loadedMiles = ""
        val bounceMiles = "25.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertFalse("Validation should fail for empty loaded miles", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have required field error", "required", result.firstError?.rule)
    }

    @Test
    fun `validateTripInput with negative bounce miles should fail`() {
        // Given
        val loadedMiles = "100.0"
        val bounceMiles = "-5.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertFalse("Validation should fail for negative bounce miles", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have min_value error", "min_value", result.firstError?.rule)
    }

    @Test
    fun `validateTripInput with zero loaded miles should fail`() {
        // Given
        val loadedMiles = "0.0"
        val bounceMiles = "25.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertFalse("Validation should fail for zero loaded miles", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have min_value error", "min_value", result.firstError?.rule)
    }

    @Test
    fun `validateTripInput with both zero values should fail business logic`() {
        // Given
        val loadedMiles = "0.0"
        val bounceMiles = "0.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertFalse("Validation should fail for both zero values", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have business_logic error", "business_logic", result.firstError?.rule)
    }

    @Test
    fun `validateTripInput with malicious input should be sanitized`() {
        // Given
        val loadedMiles = "100<script>alert('xss')</script>"
        val bounceMiles = "25.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertTrue("Validation should pass after sanitization", result.isValid)
        assertNotNull("Should return sanitized values", result.sanitizedValue)
        assertTrue("Should contain sanitized loaded value", result.sanitizedValue!!.contains("\"loaded\":\"100\""))
    }

    @Test
    fun `validateTripInput with extremely large values should show warnings`() {
        // Given
        val loadedMiles = "9999.0"
        val bounceMiles = "25.0"

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                isStartingTrip = true,
            )

        // Then
        assertTrue("Validation should pass for large values", result.isValid)
        assertFalse("Should have no errors", result.hasErrors)
        assertTrue("Should have warnings for large values", result.hasWarnings)
        assertEquals("Should have unrealistic_value warning", "unrealistic_value", result.firstWarning?.rule)
    }

    @Test
    fun `validateTripInput with actual miles validation should work`() {
        // Given
        val loadedMiles = "100.0"
        val bounceMiles = "25.0"
        val actualMiles = "50.0" // Too low compared to dispatched miles

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateTripInput(
                loadedMilesText = loadedMiles,
                bounceMilesText = bounceMiles,
                actualMilesText = actualMiles,
                isStartingTrip = false,
            )

        // Then
        assertFalse("Validation should fail for unrealistic actual miles", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have business_logic error", "business_logic", result.firstError?.rule)
    }

    @Test
    fun `validateGpsDataForTrip with good GPS data should pass`() {
        // Given
        val gpsAccuracy = 15f // Good accuracy
        val gpsAge = 5000L // 5 seconds old
        val totalGpsPoints = 100
        val validGpsPoints = 95

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertTrue("Validation should pass for good GPS data", result.isValid)
        assertFalse("Should have no errors", result.hasErrors)
        assertFalse("Should have no warnings", result.hasWarnings)
    }

    @Test
    fun `validateGpsDataForTrip with poor accuracy should show warnings`() {
        // Given
        val gpsAccuracy = 50f // Poor accuracy
        val gpsAge = 5000L
        val totalGpsPoints = 100
        val validGpsPoints = 95

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertTrue("Validation should pass but with warnings", result.isValid)
        assertFalse("Should have no errors", result.hasErrors)
        assertTrue("Should have warnings for poor accuracy", result.hasWarnings)
        assertEquals("Should have gps_quality warning", "gps_quality", result.firstWarning?.rule)
    }

    @Test
    fun `validateGpsDataForTrip with old data should fail`() {
        // Given
        val gpsAccuracy = 15f
        val gpsAge = 60000L // 1 minute old (too old)
        val totalGpsPoints = 100
        val validGpsPoints = 95

        // When
        val result =
            ValidationFramework.UnifiedValidation.validateGpsDataForTrip(
                gpsAccuracy = gpsAccuracy,
                gpsAge = gpsAge,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
            )

        // Then
        assertFalse("Validation should fail for old GPS data", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have gps_freshness error", "gps_freshness", result.firstError?.rule)
    }

    @Test
    fun `validateTripEntity with valid entity should pass`() {
        // Given
        val tripEntity =
            TripEntity(
                id = 1,
                date = Date(),
                loadedMiles = 100.0,
                bounceMiles = 25.0,
                actualMiles = 150.0,
                oorMiles = 25.0,
                oorPercentage = 25.0,
            )

        // When
        val result = ValidationFramework.UnifiedValidation.validateTripEntity(tripEntity)

        // Then
        assertTrue("Validation should pass for valid entity", result.isValid)
        assertFalse("Should have no errors", result.hasErrors)
    }

    @Test
    fun `validateTripEntity with invalid data should fail`() {
        // Given
        val tripEntity =
            TripEntity(
                id = 1,
                date = Date(),
                loadedMiles = -10.0, // Invalid negative value
                bounceMiles = 25.0,
                actualMiles = 150.0,
                oorMiles = 25.0,
                oorPercentage = 25.0,
            )

        // When
        val result = ValidationFramework.UnifiedValidation.validateTripEntity(tripEntity)

        // Then
        assertFalse("Validation should fail for invalid data", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have min_value error", "min_value", result.firstError?.rule)
    }

    @Test
    fun `validateTripEntity with inconsistent OOR calculation should fail`() {
        // Given
        val tripEntity =
            TripEntity(
                id = 1,
                date = Date(),
                loadedMiles = 100.0,
                bounceMiles = 25.0,
                actualMiles = 150.0,
                oorMiles = 50.0, // Incorrect: should be 25.0
                oorPercentage = 25.0,
            )

        // When
        val result = ValidationFramework.UnifiedValidation.validateTripEntity(tripEntity)

        // Then
        assertFalse("Validation should fail for inconsistent OOR calculation", result.isValid)
        assertTrue("Should have errors", result.hasErrors)
        assertEquals("Should have calculation_consistency error", "calculation_consistency", result.firstError?.rule)
    }

    @Test
    fun `getUserFriendlyMessage should return appropriate messages`() {
        // Given
        val validationResult =
            ValidationFramework.ValidationResult(
                isValid = false,
                errors =
                    listOf(
                        ValidationFramework.ValidationError(
                            field = "Loaded miles",
                            value = "0",
                            rule = "required",
                            message = "Loaded miles is required",
                        ),
                    ),
            )

        // When
        val message = ValidationFramework.UnifiedValidation.getUserFriendlyMessage(validationResult)

        // Then
        assertEquals("Should return user-friendly message", "Please fill in all required fields", message)
    }

    @Test
    fun `getUserFriendlyMessage with warnings should include warning text`() {
        // Given
        val validationResult =
            ValidationFramework.ValidationResult(
                isValid = true,
                warnings =
                    listOf(
                        ValidationFramework.ValidationWarning(
                            field = "GPS Accuracy",
                            value = "50m",
                            rule = "gps_quality",
                            message = "GPS accuracy is lower than recommended",
                        ),
                    ),
            )

        // When
        val message = ValidationFramework.UnifiedValidation.getUserFriendlyMessage(validationResult)

        // Then
        assertTrue("Should include warning text", message.startsWith("Warning:"))
        assertTrue("Should include warning message", message.contains("GPS accuracy"))
    }
} 
