package com.example.outofroutebuddy.util

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * ✅ NEW: Test class for ErrorHandler
 *
 * Tests the comprehensive error handling system to ensure:
 * - Error creation works correctly
 * - Error categorization is accurate
 * - Error severity levels are appropriate
 * - Error messages are user-friendly
 */
class ErrorHandlerTest {
    private val mockContext = mock(Context::class.java)

    @Test
    fun `createValidationError should create proper validation error`() {
        // When
        val error =
            ErrorHandler.createValidationError(
                field = "Loaded miles",
                value = "invalid",
                validationRule = "numeric",
                message = "Loaded miles must be a valid number",
            )

        // Then
        assertNotNull("Error should not be null", error)
        assertEquals("Field should match", "Loaded miles", error.field)
        assertEquals("Value should match", "invalid", error.value)
        assertEquals("Rule should match", "numeric", error.validationRule)
        assertEquals("Message should match", "Loaded miles must be a valid number", error.message)
        assertEquals("Severity should be LOW", ErrorHandler.ErrorSeverity.LOW, error.severity)
        assertEquals("Category should be VALIDATION", ErrorHandler.ErrorCategory.VALIDATION, error.category)
    }

    @Test
    fun `createDatabaseError should create proper database error`() {
        // When
        val cause = RuntimeException("Database connection failed")
        val error =
            ErrorHandler.createDatabaseError(
                operation = "insert",
                table = "trips",
                message = "Failed to insert trip data",
                cause = cause,
            )

        // Then
        assertNotNull("Error should not be null", error)
        assertEquals("Operation should match", "insert", error.operation)
        assertEquals("Table should match", "trips", error.table)
        assertEquals("Message should match", "Failed to insert trip data", error.message)
        assertEquals("Cause should match", cause, error.cause)
        assertEquals("Severity should be HIGH", ErrorHandler.ErrorSeverity.HIGH, error.severity)
        assertEquals("Category should be DATABASE", ErrorHandler.ErrorCategory.DATABASE, error.category)
    }

    @Test
    fun `createGpsError should create proper GPS error`() {
        // When
        val error =
            ErrorHandler.createGpsError(
                operation = "location_update",
                message = "GPS signal lost",
                accuracy = 50.0f,
                cause = null,
            )

        // Then
        assertNotNull("Error should not be null", error)
        assertEquals("Operation should match", "location_update", error.gpsOperation)
        assertEquals("Message should match", "GPS signal lost", error.message)
        assertEquals("Accuracy should match", 50.0f, error.accuracy)
        assertEquals("Severity should be MEDIUM", ErrorHandler.ErrorSeverity.MEDIUM, error.severity)
        assertEquals("Category should be GPS", ErrorHandler.ErrorCategory.GPS, error.category)
    }

    @Test
    fun `createServiceError should create proper service error`() {
        // When
        val error =
            ErrorHandler.createServiceError(
                serviceName = "TripTrackingService",
                operation = "start",
                message = "Service failed to start",
                cause = null,
            )

        // Then
        assertNotNull("Error should not be null", error)
        assertEquals("Service name should match", "TripTrackingService", error.serviceName)
        assertEquals("Operation should match", "start", error.serviceOperation)
        assertEquals("Message should match", "Service failed to start", error.message)
        assertEquals("Severity should be HIGH", ErrorHandler.ErrorSeverity.HIGH, error.severity)
        assertEquals("Category should be SERVICE", ErrorHandler.ErrorCategory.SERVICE, error.category)
    }

    @Test
    fun `createPermissionError should create proper permission error`() {
        // When
        val error =
            ErrorHandler.createPermissionError(
                permission = "ACCESS_FINE_LOCATION",
                operation = "start_trip",
                message = "Location permission required",
            )

        // Then
        assertNotNull("Error should not be null", error)
        assertEquals("Permission should match", "ACCESS_FINE_LOCATION", error.permission)
        assertEquals("Operation should match", "start_trip", error.operation)
        assertEquals("Message should match", "Location permission required", error.message)
        assertEquals("Severity should be HIGH", ErrorHandler.ErrorSeverity.HIGH, error.severity)
        assertEquals("Category should be PERMISSION", ErrorHandler.ErrorCategory.PERMISSION, error.category)
    }

    @Test
    fun `handleException should convert IllegalArgumentException to ValidationError`() {
        // Given
        val exception = IllegalArgumentException("Invalid input")

        // When
        val result =
            ErrorHandler.handleException(
                context = mockContext,
                exception = exception,
                operation = "validate_input",
                ErrorHandler.ErrorCategory.VALIDATION,
            )

        // Then
        assertTrue("Should handle the exception", result.wasHandled)
        assertNotNull("Should have recovery suggestion", result.recoverySuggestion)
        assertFalse("Should not retry validation errors", result.shouldRetry)
    }

    @Test
    fun `handleException should convert SecurityException to PermissionError`() {
        // Given
        val exception = SecurityException("Permission denied")

        // When
        val result =
            ErrorHandler.handleException(
                context = mockContext,
                exception = exception,
                operation = "access_location",
                ErrorHandler.ErrorCategory.PERMISSION,
            )

        // Then
        assertTrue("Should handle the exception", result.wasHandled)
        assertNotNull("Should have user message", result.userMessage)
        assertNotNull("Should have recovery suggestion", result.recoverySuggestion)
    }

    @Test
    fun `error severity levels should be correctly ordered`() {
        // Then
        assertTrue(
            "LOW should be less than MEDIUM",
            ErrorHandler.ErrorSeverity.LOW.ordinal < ErrorHandler.ErrorSeverity.MEDIUM.ordinal,
        )
        assertTrue(
            "MEDIUM should be less than HIGH",
            ErrorHandler.ErrorSeverity.MEDIUM.ordinal < ErrorHandler.ErrorSeverity.HIGH.ordinal,
        )
        assertTrue(
            "HIGH should be less than CRITICAL",
            ErrorHandler.ErrorSeverity.HIGH.ordinal < ErrorHandler.ErrorSeverity.CRITICAL.ordinal,
        )
    }

    @Test
    fun `error categories should be comprehensive`() {
        // Then
        val categories = ErrorHandler.ErrorCategory.values()
        assertTrue(
            "Should have validation category",
            categories.contains(ErrorHandler.ErrorCategory.VALIDATION),
        )
        assertTrue(
            "Should have database category",
            categories.contains(ErrorHandler.ErrorCategory.DATABASE),
        )
        assertTrue(
            "Should have network category",
            categories.contains(ErrorHandler.ErrorCategory.NETWORK),
        )
        assertTrue(
            "Should have GPS category",
            categories.contains(ErrorHandler.ErrorCategory.GPS),
        )
        assertTrue(
            "Should have service category",
            categories.contains(ErrorHandler.ErrorCategory.SERVICE),
        )
        assertTrue(
            "Should have permission category",
            categories.contains(ErrorHandler.ErrorCategory.PERMISSION),
        )
        assertTrue(
            "Should have unknown category",
            categories.contains(ErrorHandler.ErrorCategory.UNKNOWN),
        )
    }
} 
