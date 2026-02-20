package com.example.outofroutebuddy.util

import android.util.Log
import java.io.File

/**
 * 🛡️ Input Validator
 * 
 * Validates and sanitizes all user input to prevent security issues and crashes.
 * 
 * ✅ NEW (#1): Input Sanitization
 * 
 * Features:
 * - Numeric input validation
 * - File path sanitization
 * - String sanitization
 * - Bounds checking
 * 
 * Prevents:
 * - SQL injection (though Room already protects)
 * - Path traversal attacks
 * - Invalid numeric inputs
 * - Crash-causing values
 * 
 * Priority: MEDIUM
 * Impact: Security and crash prevention
 */
object InputValidator {
    
    private const val TAG = "InputValidator"
    
    // Validation constants
    private const val MAX_MILES = 10000.0
    private const val MIN_MILES = 0.0
    private const val MAX_STRING_LENGTH = 1000
    
    /**
     * Sanitize miles input
     * 
     * @param input String input from user
     * @return Validated double or null if invalid
     */
    fun sanitizeMiles(input: String): Double? {
        return try {
            if (input.isBlank()) {
                return null
            }
            
            val value = input.trim().toDoubleOrNull()
            
            when {
                value == null -> {
                    Log.w(TAG, "Invalid miles format: $input")
                    null
                }
                value < MIN_MILES -> {
                    Log.w(TAG, "Miles too low: $value")
                    null
                }
                value > MAX_MILES -> {
                    Log.w(TAG, "Miles too high: $value")
                    null
                }
                value.isNaN() || value.isInfinite() -> {
                    Log.w(TAG, "Invalid miles value: $value")
                    null
                }
                else -> value
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing miles", e)
            null
        }
    }
    
    /**
     * Sanitize file path to prevent directory traversal
     * 
     * @param path File path from user
     * @return Sanitized path or null if suspicious
     */
    fun sanitizeFilePath(path: String): String? {
        return try {
            val trimmed = path.trim()
            
            // Check for directory traversal
            if (trimmed.contains("..")) {
                Log.w(TAG, "Directory traversal detected: $path")
                return null
            }
            
            // Check for home directory access
            if (trimmed.contains("~")) {
                Log.w(TAG, "Home directory access attempt: $path")
                return null
            }
            
            // Check for absolute paths (should be relative)
            if (trimmed.startsWith("/") || trimmed.matches(Regex("^[A-Za-z]:"))) {
                Log.w(TAG, "Absolute path detected: $path")
                return null
            }
            
            // Validate extension
            val validExtensions = listOf(".csv", ".json", ".txt", ".log")
            if (!validExtensions.any { trimmed.endsWith(it) }) {
                Log.w(TAG, "Invalid file extension: $path")
                return null
            }
            
            // Validate characters (alphanumeric, underscore, dash, dot, slash only)
            if (!trimmed.matches(Regex("^[a-zA-Z0-9_./\\-]+$"))) {
                Log.w(TAG, "Invalid characters in path: $path")
                return null
            }
            
            trimmed
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing path", e)
            null
        }
    }
    
    /**
     * Sanitize string input (prevent injection, limit length)
     * 
     * @param input String from user
     * @param maxLength Maximum allowed length
     * @return Sanitized string
     */
    fun sanitizeString(input: String, maxLength: Int = MAX_STRING_LENGTH): String {
        return try {
            val trimmed = input.trim()
            
            if (trimmed.length > maxLength) {
                Log.w(TAG, "String too long (${trimmed.length} > $maxLength), truncating")
                trimmed.take(maxLength)
            } else {
                trimmed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing string", e)
            ""
        }
    }
    
    /**
     * Validate numeric range
     * 
     * @param value Value to validate
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return true if in range, false otherwise
     */
    fun validateRange(value: Double, min: Double, max: Double): Boolean {
        return value in min..max
    }
    
    /**
     * Validate percentage (0-100)
     */
    fun validatePercentage(value: Double): Boolean {
        return value in 0.0..100.0
    }
    
    /**
     * Validate positive number
     */
    fun validatePositive(value: Double): Boolean {
        return value >= 0.0 && !value.isNaN() && !value.isInfinite()
    }
    
    /**
     * Sanitize file name (no path components)
     */
    fun sanitizeFileName(fileName: String): String? {
        return try {
            val trimmed = fileName.trim()
            
            // No path separators
            if (trimmed.contains("/") || trimmed.contains("\\")) {
                Log.w(TAG, "Path separators in filename: $fileName")
                return null
            }
            
            // Valid characters only
            if (!trimmed.matches(Regex("^[a-zA-Z0-9_.-]+$"))) {
                Log.w(TAG, "Invalid characters in filename: $fileName")
                return null
            }
            
            // Reasonable length
            if (trimmed.length > 255) {
                Log.w(TAG, "Filename too long: ${trimmed.length}")
                return null
            }
            
            trimmed
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing filename", e)
            null
        }
    }
    
    /**
     * Validation result
     */
    data class ValidationResult(
        val isValid: Boolean,
        val sanitizedValue: Any?,
        val errorMessage: String? = null
    )
    
    /**
     * Comprehensive validation for miles input
     */
    fun validateMilesInput(input: String): ValidationResult {
        val sanitized = sanitizeMiles(input)
        
        return if (sanitized != null) {
            ValidationResult(
                isValid = true,
                sanitizedValue = sanitized
            )
        } else {
            ValidationResult(
                isValid = false,
                sanitizedValue = null,
                errorMessage = "Invalid miles: must be 0-$MAX_MILES"
            )
        }
    }
}










