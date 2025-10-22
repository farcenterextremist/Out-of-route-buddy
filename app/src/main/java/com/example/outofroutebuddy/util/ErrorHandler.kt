package com.example.outofroutebuddy.util

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.OutOfRouteApplication
import java.util.Date

/**
 * ✅ NEW: Comprehensive Error Handling System
 * 
 * This class provides centralized error handling with:
 * - Sealed classes for different error types
 * - User-friendly error messages
 * - Error reporting to Firebase Crashlytics
 * - Error categorization and prioritization
 * - Error recovery suggestions
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * ✅ NEW: Sealed class hierarchy for different error types
     */
    sealed class AppError(
        open val message: String,
        open val cause: Throwable? = null,
        val timestamp: Date = Date(),
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        val category: ErrorCategory = ErrorCategory.UNKNOWN,
        open val userMessage: String? = null,
        open val recoverySuggestion: String? = null
    ) {
        
        // ✅ VALIDATION ERRORS: Input validation failures
        data class ValidationError(
            val field: String,
            val value: String?,
            val validationRule: String,
            override val message: String,
            override val userMessage: String = "Please check your input and try again",
            override val recoverySuggestion: String = "Verify the entered values are correct"
        ) : AppError(
            message = message,
            severity = ErrorSeverity.LOW,
            category = ErrorCategory.VALIDATION,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ DATABASE ERRORS: Database operation failures
        data class DatabaseError(
            val operation: String,
            val table: String? = null,
            override val message: String,
            override val cause: Throwable? = null,
            override val userMessage: String = "Unable to save or load data",
            override val recoverySuggestion: String = "Try restarting the app or check your device storage"
        ) : AppError(
            message = message,
            cause = cause,
            severity = ErrorSeverity.HIGH,
            category = ErrorCategory.DATABASE,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ NETWORK ERRORS: Network connectivity issues
        data class NetworkError(
            val endpoint: String? = null,
            val statusCode: Int? = null,
            override val message: String,
            override val cause: Throwable? = null,
            override val userMessage: String = "Network connection issue",
            override val recoverySuggestion: String = "Check your internet connection and try again"
        ) : AppError(
            message = message,
            cause = cause,
            severity = ErrorSeverity.MEDIUM,
            category = ErrorCategory.NETWORK,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ GPS ERRORS: Location service failures
        data class GpsError(
            val gpsOperation: String,
            val accuracy: Float? = null,
            override val message: String,
            override val cause: Throwable? = null,
            override val userMessage: String = "GPS location issue",
            override val recoverySuggestion: String = "Check GPS settings and try again"
        ) : AppError(
            message = message,
            cause = cause,
            severity = ErrorSeverity.MEDIUM,
            category = ErrorCategory.GPS,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ SERVICE ERRORS: Background service failures
        data class ServiceError(
            val serviceName: String,
            val serviceOperation: String,
            override val message: String,
            override val cause: Throwable? = null,
            override val userMessage: String = "Service temporarily unavailable",
            override val recoverySuggestion: String = "Try restarting the app"
        ) : AppError(
            message = message,
            cause = cause,
            severity = ErrorSeverity.HIGH,
            category = ErrorCategory.SERVICE,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ PERMISSION ERRORS: Permission-related failures
        data class PermissionError(
            val permission: String,
            val operation: String,
            override val message: String,
            override val userMessage: String = "Permission required",
            override val recoverySuggestion: String = "Grant the required permission in settings"
        ) : AppError(
            message = message,
            severity = ErrorSeverity.HIGH,
            category = ErrorCategory.PERMISSION,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
        
        // ✅ UNKNOWN ERRORS: Unclassified errors
        data class UnknownError(
            override val message: String,
            override val cause: Throwable? = null,
            override val userMessage: String = "An unexpected error occurred",
            override val recoverySuggestion: String = "Try restarting the app"
        ) : AppError(
            message = message,
            cause = cause,
            severity = ErrorSeverity.MEDIUM,
            category = ErrorCategory.UNKNOWN,
            userMessage = userMessage,
            recoverySuggestion = recoverySuggestion
        )
    }
    
    /**
     * ✅ NEW: Error severity levels
     */
    enum class ErrorSeverity {
        LOW,      // Non-critical, user can continue
        MEDIUM,   // May affect functionality, user should be aware
        HIGH,     // Critical, affects core functionality
        CRITICAL  // App-breaking, requires immediate attention
    }
    
    /**
     * ✅ NEW: Error categories for better organization
     */
    enum class ErrorCategory {
        VALIDATION,   // Input validation errors
        DATABASE,     // Database operation errors
        NETWORK,      // Network connectivity errors
        GPS,          // GPS/location errors
        SERVICE,      // Background service errors
        PERMISSION,   // Permission-related errors
        UNKNOWN       // Unclassified errors
    }
    
    /**
     * ✅ NEW: Error handling result
     */
    data class ErrorHandlingResult(
        val wasHandled: Boolean,
        val userMessage: String?,
        val recoverySuggestion: String?,
        val shouldRetry: Boolean = false,
        val retryDelayMs: Long = 0L
    )
    
    /**
     * ✅ NEW: Handle an error with comprehensive logging and reporting
     */
    fun handleError(
        context: Context,
        error: AppError,
        showUserMessage: Boolean = true
    ): ErrorHandlingResult {
        
        try {
            // Log the error with appropriate level
            logError(error)
            
            // Report to Firebase Crashlytics
            reportErrorToCrashlytics(context, error)
            
            // Determine if user should be notified
            val shouldNotifyUser = shouldNotifyUser(error)
            
            // Show user message if requested and appropriate
            if (showUserMessage && shouldNotifyUser) {
                showUserError(context, error)
            }
            
            // Return handling result
            return ErrorHandlingResult(
                wasHandled = true,
                userMessage = if (shouldNotifyUser) error.userMessage else null,
                recoverySuggestion = error.recoverySuggestion,
                shouldRetry = shouldRetry(error),
                retryDelayMs = getRetryDelay(error)
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle error: ${error.message}", e)
            return ErrorHandlingResult(
                wasHandled = false,
                userMessage = "Error handling failed",
                recoverySuggestion = "Please restart the app"
            )
        }
    }
    
    /**
     * ✅ NEW: Handle a generic exception by converting it to AppError
     */
    fun handleException(
        context: Context,
        exception: Throwable,
        operation: String,
        _category: ErrorCategory = ErrorCategory.UNKNOWN
    ): ErrorHandlingResult {
        
        val appError = when (exception) {
            is IllegalArgumentException -> AppError.ValidationError(
                field = "unknown",
                value = null,
                validationRule = "validation",
                message = "Validation error during $operation: ${exception.message}"
            )
            is SecurityException -> AppError.PermissionError(
                permission = "unknown",
                operation = operation,
                message = "Permission error during $operation: ${exception.message}"
            )
            is OutOfMemoryError -> AppError.UnknownError(
                message = "Memory error during $operation: ${exception.message}",
                cause = exception
            )
            else -> AppError.UnknownError(
                message = "Unexpected error during $operation: ${exception.message}",
                cause = exception
            )
        }
        
        return handleError(context, appError)
    }
    
    /**
     * ✅ NEW: Log error with appropriate level based on severity
     */
    private fun logError(error: AppError) {
        val logMessage = buildString {
            append("${error.category.name} ERROR: ${error.message}")
            error.cause?.let { append(" (Cause: ${it.message})") }
            append(" [Severity: ${error.severity}]")
        }
        
        when (error.severity) {
            ErrorSeverity.LOW -> Log.d(TAG, logMessage)
            ErrorSeverity.MEDIUM -> Log.w(TAG, logMessage)
            ErrorSeverity.HIGH -> Log.e(TAG, logMessage)
            ErrorSeverity.CRITICAL -> Log.e(TAG, "CRITICAL: $logMessage")
        }
    }
    
    /**
     * ✅ NEW: Report error to Firebase Crashlytics
     */
    private fun reportErrorToCrashlytics(context: Context, error: AppError) {
        try {
            val app = context.applicationContext as? OutOfRouteApplication
            app?.reportErrorToCrashlytics(
                "${error.category.name}: ${error.message}",
                error.cause
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report error to Crashlytics", e)
        }
    }
    
    /**
     * ✅ NEW: Determine if user should be notified about this error
     */
    private fun shouldNotifyUser(error: AppError): Boolean {
        return when (error.severity) {
            ErrorSeverity.LOW -> false
            ErrorSeverity.MEDIUM -> true
            ErrorSeverity.HIGH -> true
            ErrorSeverity.CRITICAL -> true
        }
    }
    
    /**
     * ✅ NEW: Show user-friendly error message
     */
    private fun showUserError(context: Context, error: AppError) {
        try {
            // Error messages are handled by UI state
            // Will implement Snackbar later if needed
            val message = error.userMessage ?: "An error occurred"
            Log.w(TAG, "User error: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log user error message", e)
        }
    }
    
    /**
     * ✅ NEW: Determine if operation should be retried
     */
    private fun shouldRetry(error: AppError): Boolean {
        return when (error.category) {
            ErrorCategory.NETWORK -> true
            ErrorCategory.GPS -> true
            ErrorCategory.SERVICE -> true
            else -> false
        }
    }
    
    /**
     * ✅ NEW: Get retry delay based on error type
     */
    private fun getRetryDelay(error: AppError): Long {
        return when (error.category) {
            ErrorCategory.NETWORK -> 5000L // 5 seconds
            ErrorCategory.GPS -> 3000L     // 3 seconds
            ErrorCategory.SERVICE -> 2000L // 2 seconds
            else -> 0L
        }
    }
    
    /**
     * ✅ NEW: Create validation error for input fields
     */
    fun createValidationError(
        field: String,
        value: String?,
        validationRule: String,
        message: String
    ): AppError.ValidationError {
        return AppError.ValidationError(
            field = field,
            value = value,
            validationRule = validationRule,
            message = message
        )
    }
    
    /**
     * ✅ NEW: Create database error
     */
    fun createDatabaseError(
        operation: String,
        table: String? = null,
        message: String,
        cause: Throwable? = null
    ): AppError.DatabaseError {
        return AppError.DatabaseError(
            operation = operation,
            table = table,
            message = message,
            cause = cause
        )
    }
    
    /**
     * ✅ NEW: Create GPS error
     */
    fun createGpsError(
        operation: String,
        message: String,
        accuracy: Float? = null,
        cause: Throwable? = null
    ): AppError.GpsError {
        return AppError.GpsError(
            gpsOperation = operation,
            accuracy = accuracy,
            message = message,
            cause = cause
        )
    }
    
    /**
     * ✅ NEW: Create service error
     */
    fun createServiceError(
        serviceName: String,
        operation: String,
        message: String,
        cause: Throwable? = null
    ): AppError.ServiceError {
        return AppError.ServiceError(
            serviceName = serviceName,
            serviceOperation = operation,
            message = message,
            cause = cause
        )
    }
    
    /**
     * ✅ NEW: Create permission error
     */
    fun createPermissionError(
        permission: String,
        operation: String,
        message: String
    ): AppError.PermissionError {
        return AppError.PermissionError(
            permission = permission,
            operation = operation,
            message = message
        )
    }
} 
