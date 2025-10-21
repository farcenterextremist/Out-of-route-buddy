package com.example.outofroutebuddy.validation

import com.example.outofroutebuddy.util.ErrorHandler
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.services.LocationValidationService
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * ✅ NEW: Comprehensive Validation Framework
 * 
 * This framework provides:
 * - Input validation for all data types
 * - Custom validation rules
 * - Detailed error messages
 * - Integration with ErrorHandler
 * - Validation result caching
 */
object ValidationFramework {
    
    private const val TAG = "ValidationFramework"
    
    /**
     * ✅ NEW: Validation result with detailed information
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError> = emptyList(),
        val warnings: List<ValidationWarning> = emptyList(),
        val sanitizedValue: String? = null
    ) {
        val hasErrors: Boolean get() = errors.isNotEmpty()
        val hasWarnings: Boolean get() = warnings.isNotEmpty()
        val firstError: ValidationError? get() = errors.firstOrNull()
        val firstWarning: ValidationWarning? get() = warnings.firstOrNull()
        val errorMessage: String? get() = firstError?.message
    }
    
    /**
     * ✅ NEW: Validation error with field-specific information
     */
    data class ValidationError(
        val field: String,
        val value: String?,
        val rule: String,
        val message: String,
        val severity: ErrorHandler.ErrorSeverity = ErrorHandler.ErrorSeverity.MEDIUM
    )
    
    /**
     * ✅ NEW: Validation warning for non-critical issues
     */
    data class ValidationWarning(
        val field: String,
        val value: String?,
        val rule: String,
        val message: String
    )
    
    /**
     * ✅ NEW: Validation rules for different field types
     */
    object ValidationRules {
        
        // ✅ MILES VALIDATION: Trip distance validation
        const val MIN_MILES = 0.0
        const val MAX_MILES = 10000.0
        const val MIN_ACTUAL_MILES = 0.1
        
        // ✅ TEXT VALIDATION: General text validation
        const val MIN_TEXT_LENGTH = 1
        const val MAX_TEXT_LENGTH = 1000
        
        // ✅ EMAIL VALIDATION: Email format validation
        private val EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        
        // ✅ PHONE VALIDATION: Phone number validation
        private val PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
        )
        
        // ✅ DECIMAL VALIDATION: Decimal number validation
        private val DECIMAL_PATTERN = Pattern.compile(
            "^[0-9]+(\\.[0-9]+)?$"
        )
        
        /**
         * ✅ ENHANCED: Validate miles input with comprehensive rules
         */
        fun validateMiles(
            field: String,
            value: String?,
            allowZero: Boolean = false,
            minValue: Double = MIN_MILES,
            maxValue: Double = MAX_MILES
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()
            
            // Check for null or empty
            if (value.isNullOrBlank()) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "required",
                    message = "$field is required",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
                return ValidationResult(false, errors, warnings)
            }
            
            // Check for valid decimal format (allow negative numbers only if minValue < 0)
            val decimalPattern = if (minValue < 0) {
                Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$")
            } else {
                // Allow negative numbers to be caught by value validation, not format validation
                Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$")
            }
            
            if (!decimalPattern.matcher(value).matches()) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "decimal_format",
                    message = "$field must be a valid number",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
                return ValidationResult(false, errors, warnings)
            }
            
            // Convert to double and validate
            val doubleValue = value.toDoubleOrNull()
            if (doubleValue == null) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "numeric",
                    message = "$field must be a valid number",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
                return ValidationResult(false, errors, warnings)
            }
            
            // Check for NaN or Infinity
            if (doubleValue.isNaN() || doubleValue.isInfinite()) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "finite_number",
                    message = "$field must be a finite number",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
                return ValidationResult(false, errors, warnings)
            }
            
            // Check minimum value (including zero check and negative check)
            if (doubleValue < minValue || (!allowZero && doubleValue == 0.0)) {
                val message = when {
                    !allowZero && doubleValue == 0.0 -> "$field cannot be zero"
                    doubleValue < 0 -> "$field cannot be negative"
                    else -> "$field must be at least $minValue"
                }
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "min_value",
                    message = message,
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            // Check maximum value
            if (doubleValue > maxValue) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "max_value",
                    message = "$field cannot exceed $maxValue",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            // Check for unrealistic values (warnings)
            if (doubleValue > maxValue * 0.8) {
                warnings.add(ValidationWarning(
                    field = field,
                    value = value,
                    rule = "unrealistic_value",
                    message = "$field seems unusually high"
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                warnings = warnings,
                sanitizedValue = if (errors.isEmpty()) value else null
            )
        }
        
        /**
         * ✅ NEW: Validate trip data with business logic
         */
        fun validateTripData(
            loadedMiles: String?,
            bounceMiles: String?,
            actualMiles: String?
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()
            
            // Validate individual fields
            val loadedValidation = validateMiles("Loaded miles", loadedMiles, allowZero = true)
            val bounceValidation = validateMiles("Bounce miles", bounceMiles, allowZero = true)
            // ✅ GPS-AWARE: Allow actual miles to start at 0 for GPS initialization
            val actualValidation = validateMiles("Actual miles", actualMiles, allowZero = true, minValue = 0.0)
            
            errors.addAll(loadedValidation.errors)
            errors.addAll(bounceValidation.errors)
            errors.addAll(actualValidation.errors)
            warnings.addAll(loadedValidation.warnings)
            warnings.addAll(bounceValidation.warnings)
            warnings.addAll(actualValidation.warnings)
            
            // If individual validations failed, return early
            if (errors.isNotEmpty()) {
                return ValidationResult(false, errors, warnings)
            }
            
            // Validate business logic relationships - only if we have meaningful actual miles
            val loaded = loadedValidation.sanitizedValue?.toDouble() ?: 0.0
            val bounce = bounceValidation.sanitizedValue?.toDouble() ?: 0.0
            val actual = actualValidation.sanitizedValue?.toDouble() ?: 0.0
            
            val dispatchedMiles = loaded + bounce
            
            // ✅ GPS-AWARE: Only validate business logic if actual miles is meaningful (> 1.0)
            // This prevents validation errors when GPS is still initializing at 0.0 miles
            if (dispatchedMiles > 0 && actual > 1.0) {
                val ratio = actual / dispatchedMiles
                
                if (ratio < 0.5) {
                    errors.add(ValidationError(
                        field = "Actual miles",
                        value = actualMiles,
                        rule = "business_logic",
                        message = "Actual miles seems too low compared to dispatched miles",
                        severity = ErrorHandler.ErrorSeverity.MEDIUM
                    ))
                } else if (ratio > 5.0) {
                    errors.add(ValidationError(
                        field = "Actual miles",
                        value = actualMiles,
                        rule = "business_logic",
                        message = "Actual miles seems too high compared to dispatched miles",
                        severity = ErrorHandler.ErrorSeverity.MEDIUM
                    ))
                } else if (ratio < 0.8) {
                    warnings.add(ValidationWarning(
                        field = "Actual miles",
                        value = actualMiles,
                        rule = "business_logic",
                        message = "Actual miles is significantly lower than dispatched miles"
                    ))
                } else if (ratio > 2.0) {
                    warnings.add(ValidationWarning(
                        field = "Actual miles",
                        value = actualMiles,
                        rule = "business_logic",
                        message = "Actual miles is significantly higher than dispatched miles"
                    ))
                }
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                warnings = warnings
            )
        }
        
        /**
         * ✅ NEW: Validate text input
         */
        fun validateText(
            field: String,
            value: String?,
            required: Boolean = false,
            minLength: Int = MIN_TEXT_LENGTH,
            maxLength: Int = MAX_TEXT_LENGTH,
            allowEmpty: Boolean = false
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()
            
            // Check for null
            if (value == null) {
                if (required) {
                    errors.add(ValidationError(
                        field = field,
                        value = null,
                        rule = "required",
                        message = "$field is required",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                }
                return ValidationResult(false, errors, warnings)
            }
            
            // Check for empty string
            if (value.isBlank()) {
                if (required || !allowEmpty) {
                    errors.add(ValidationError(
                        field = field,
                        value = value,
                        rule = "required",
                        message = "$field cannot be empty",
                        severity = ErrorHandler.ErrorSeverity.MEDIUM
                    ))
                }
                return ValidationResult(false, errors, warnings)
            }
            
            // Check minimum length
            if (value.length < minLength) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "min_length",
                    message = "$field must be at least $minLength characters",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            // Check maximum length
            if (value.length > maxLength) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "max_length",
                    message = "$field cannot exceed $maxLength characters",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                warnings = warnings,
                sanitizedValue = if (errors.isEmpty()) value.trim() else null
            )
        }
        
        /**
         * ✅ NEW: Validate email format
         */
        fun validateEmail(
            field: String,
            value: String?,
            required: Boolean = false
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            
            // Check for null or empty
            if (value.isNullOrBlank()) {
                if (required) {
                    errors.add(ValidationError(
                        field = field,
                        value = value,
                        rule = "required",
                        message = "$field is required",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                }
                return ValidationResult(false, errors)
            }
            
            // Check email format
            if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "email_format",
                    message = "$field must be a valid email address",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                sanitizedValue = if (errors.isEmpty()) value.trim() else null
            )
        }
        
        /**
         * ✅ NEW: Validate phone number
         */
        fun validatePhone(
            field: String,
            value: String?,
            required: Boolean = false
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            
            // Check for null or empty
            if (value.isNullOrBlank()) {
                if (required) {
                    errors.add(ValidationError(
                        field = field,
                        value = value,
                        rule = "required",
                        message = "$field is required",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                }
                return ValidationResult(false, errors)
            }
            
            // Remove non-digit characters for validation
            val digitsOnly = value.replace(Regex("[^0-9+]"), "")
            
            // Check phone format
            if (!PHONE_PATTERN.matcher(digitsOnly).matches()) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "phone_format",
                    message = "$field must be a valid phone number",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                sanitizedValue = if (errors.isEmpty()) digitsOnly else null
            )
        }
        
        /**
         * ✅ NEW: Validate date input
         */
        fun validateDate(
            field: String,
            value: String?,
            required: Boolean = false,
            format: String = "yyyy-MM-dd"
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            
            // Check for null or empty
            if (value.isNullOrBlank()) {
                if (required) {
                    errors.add(ValidationError(
                        field = field,
                        value = value,
                        rule = "required",
                        message = "$field is required",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                }
                return ValidationResult(false, errors)
            }
            
            // Try to parse the date
            try {
                val formatter = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                formatter.isLenient = false
                formatter.parse(value)
            } catch (e: Exception) {
                errors.add(ValidationError(
                    field = field,
                    value = value,
                    rule = "date_format",
                    message = "$field must be a valid date in format $format",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                sanitizedValue = if (errors.isEmpty()) value else null
            )
        }
    }
    
    /**
     * ✅ NEW: Convert validation result to ErrorHandler.AppError
     */
    fun convertToAppError(
        validationResult: ValidationResult,
        operation: String
    ): List<ErrorHandler.AppError> {
        
        return validationResult.errors.map { error ->
            ErrorHandler.createValidationError(
                field = error.field,
                value = error.value,
                validationRule = error.rule,
                message = "${error.message} during $operation"
            )
        }
    }
    
    /**
     * ✅ NEW: Validate and sanitize input with error handling
     */
    fun validateAndSanitize(
        validationResult: ValidationResult,
        onError: (List<ErrorHandler.AppError>) -> Unit = {},
        onWarning: (List<ValidationWarning>) -> Unit = {}
    ): String? {
        
        if (validationResult.hasErrors) {
            val appErrors = convertToAppError(validationResult, "validation")
            onError(appErrors)
            return null
        }
        
        if (validationResult.hasWarnings) {
            onWarning(validationResult.warnings)
        }
        
        return validationResult.sanitizedValue
    }
    
    // ==================== NEW: VIEWMODEL-SPECIFIC VALIDATION METHODS ====================
    
    /**
     * ✅ NEW: Validate loaded miles for ViewModel
     */
    fun validateLoadedMiles(loadedMiles: Double): ValidationResult {
        return ValidationRules.validateMiles(
            field = "Loaded miles",
            value = loadedMiles.toString(),
            allowZero = true,
            minValue = ValidationRules.MIN_MILES,
            maxValue = ValidationRules.MAX_MILES
        )
    }
    
    /**
     * ✅ NEW: Validate bounce miles for ViewModel
     */
    fun validateBounceMiles(bounceMiles: Double): ValidationResult {
        return ValidationRules.validateMiles(
            field = "Bounce miles",
            value = bounceMiles.toString(),
            allowZero = true,
            minValue = 0.0,
            maxValue = ValidationRules.MAX_MILES
        )
    }
    
    /**
     * ✅ NEW: Validate trip start parameters for ViewModel
     */
    fun validateTripStart(loadedMiles: Double, bounceMiles: Double): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        // Validate individual fields
        val loadedValidation = validateLoadedMiles(loadedMiles)
        val bounceValidation = validateBounceMiles(bounceMiles)
        
        errors.addAll(loadedValidation.errors)
        errors.addAll(bounceValidation.errors)
        warnings.addAll(loadedValidation.warnings)
        warnings.addAll(bounceValidation.warnings)
        
        // Check if at least one mile value is provided
        if (loadedMiles <= 0 && bounceMiles <= 0) {
            errors.add(ValidationError(
                field = "Trip start",
                value = "loaded=$loadedMiles, bounce=$bounceMiles",
                rule = "business_logic",
                message = "At least one mile value must be greater than 0",
                severity = ErrorHandler.ErrorSeverity.HIGH
            ))
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * ✅ NEW: Validate actual miles for ViewModel
     */
    fun validateActualMiles(actualMiles: Double): ValidationResult {
        return ValidationRules.validateMiles(
            field = "Actual miles",
            value = actualMiles.toString(),
            allowZero = true, // ✅ GPS-AWARE: Allow zero for GPS initialization
            minValue = 0.0,   // ✅ GPS-AWARE: Start from 0, not MIN_ACTUAL_MILES
            maxValue = ValidationRules.MAX_MILES
        )
    }
    
    /**
     * ✅ NEW: Get error message from validation result
     */
    fun getErrorMessage(validationResult: ValidationResult): String? {
        return validationResult.firstError?.message
    }

    /**
     * ✅ NEW: Unified validation strategy for the entire app
     * This consolidates all validation logic and provides consistent error handling
     */
    object UnifiedValidation {
        
        /**
         * ✅ ENHANCED: Validate and sanitize trip input from UI
         * This is the single entry point for all trip validation
         */
        fun validateTripInput(
            loadedMilesText: String?,
            bounceMilesText: String?,
            actualMilesText: String? = null,
            isStartingTrip: Boolean = true
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()
            
            // Sanitize input strings (allow negative sign for numeric validation)
            val sanitizedLoaded = loadedMilesText?.trim()?.replace(Regex("[^0-9.-]"), "")
            val sanitizedBounce = bounceMilesText?.trim()?.replace(Regex("[^0-9.-]"), "")
            val sanitizedActual = actualMilesText?.trim()?.replace(Regex("[^0-9.-]"), "")
            
            // ✅ NEW: Check business logic for both zero values first
            if (isStartingTrip) {
                val loadedValue = sanitizedLoaded?.toDoubleOrNull() ?: 0.0
                val bounceValue = sanitizedBounce?.toDoubleOrNull() ?: 0.0
                
                if (loadedValue <= 0 && bounceValue <= 0) {
                    errors.add(ValidationError(
                        field = "Trip start",
                        value = "loaded=$loadedValue, bounce=$bounceValue",
                        rule = "business_logic",
                        message = "At least one mile value must be greater than 0",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                    return ValidationResult(false, errors, warnings)
                }
            }
            
            // Validate loaded miles
            val loadedValidation = ValidationRules.validateMiles(
                field = "Loaded miles",
                value = sanitizedLoaded,
                allowZero = false, // Must have loaded miles to start trip
                minValue = ValidationRules.MIN_MILES,
                maxValue = ValidationRules.MAX_MILES
            )
            
            // Validate bounce miles
            val bounceValidation = ValidationRules.validateMiles(
                field = "Bounce miles", 
                value = sanitizedBounce,
                allowZero = true, // Bounce miles can be zero
                minValue = 0.0, // But cannot be negative
                maxValue = ValidationRules.MAX_MILES
            )
            
            errors.addAll(loadedValidation.errors)
            errors.addAll(bounceValidation.errors)
            warnings.addAll(loadedValidation.warnings)
            warnings.addAll(bounceValidation.warnings)
            
            // Validate actual miles if provided (for trip completion)
            var actualValidation: ValidationResult? = null
            if (!isStartingTrip && sanitizedActual != null) {
                actualValidation = ValidationRules.validateMiles(
                    field = "Actual miles",
                    value = sanitizedActual,
                    allowZero = false,
                    minValue = ValidationRules.MIN_ACTUAL_MILES,
                    maxValue = ValidationRules.MAX_MILES
                )
                errors.addAll(actualValidation.errors)
                warnings.addAll(actualValidation.warnings)
            }
            
            // Business logic validation for trip completion - only if we have meaningful actual miles
            if (errors.isEmpty() && !isStartingTrip && sanitizedActual != null && actualValidation != null) {
                val loaded = loadedValidation.sanitizedValue?.toDouble() ?: 0.0
                val bounce = bounceValidation.sanitizedValue?.toDouble() ?: 0.0
                val actual = actualValidation.sanitizedValue?.toDouble() ?: 0.0
                val dispatchedMiles = loaded + bounce
                
                // ✅ GPS-AWARE: Only validate business logic if actual miles is meaningful (> 1.0)
                // This prevents validation errors when GPS is still initializing
                if (dispatchedMiles > 0 && actual > 1.0) {
                    val ratio = actual / dispatchedMiles
                    
                    if (ratio < 0.5) {
                        errors.add(ValidationError(
                            field = "Actual miles",
                            value = sanitizedActual,
                            rule = "business_logic",
                            message = "Actual miles seems too low compared to dispatched miles",
                            severity = ErrorHandler.ErrorSeverity.MEDIUM
                        ))
                    } else if (ratio > 5.0) {
                        errors.add(ValidationError(
                            field = "Actual miles",
                            value = sanitizedActual,
                            rule = "business_logic",
                            message = "Actual miles seems too high compared to dispatched miles",
                            severity = ErrorHandler.ErrorSeverity.MEDIUM
                        ))
                    }
                }
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                warnings = warnings,
                sanitizedValue = if (errors.isEmpty()) {
                    // Return sanitized values as JSON string for easy parsing
                    val actualValue = actualValidation?.sanitizedValue ?: ""
                    "{\"loaded\":\"${loadedValidation.sanitizedValue}\",\"bounce\":\"${bounceValidation.sanitizedValue}\",\"actual\":\"$actualValue\"}"
                } else null
            )
        }
        
        /**
         * ✅ ENHANCED: Comprehensive GPS data validation for vehicle tracking
         */
        fun validateGpsDataForTrip(
            gpsAccuracy: Float,
            gpsAge: Long,
            totalGpsPoints: Int,
            validGpsPoints: Int
        ): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()
            
            // ✅ CRITICAL: Check GPS accuracy for vehicle tracking
            if (gpsAccuracy > LocationValidationService.VEHICLE_MIN_ACCURACY) {
                if (gpsAccuracy > 50f) {
                    // Very poor accuracy - critical error
                    errors.add(ValidationError(
                        field = "GPS Accuracy",
                        value = "${gpsAccuracy}m",
                        rule = "gps_accuracy_critical",
                        message = "GPS accuracy is critically poor for vehicle tracking",
                        severity = ErrorHandler.ErrorSeverity.HIGH
                    ))
                } else {
                    // Poor accuracy - warning
                    warnings.add(ValidationWarning(
                        field = "GPS Accuracy",
                        value = "${gpsAccuracy}m",
                        rule = "gps_quality",
                        message = "GPS accuracy is lower than recommended for vehicle tracking"
                    ))
                }
            }
            
            // ✅ CRITICAL: Check GPS data freshness
            if (gpsAge > LocationValidationService.DEFAULT_MAX_LOCATION_AGE) {
                errors.add(ValidationError(
                    field = "GPS Data Age",
                    value = "${gpsAge}ms",
                    rule = "gps_freshness",
                    message = "GPS data is too old for accurate calculations",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            } else if (gpsAge > 15000) { // 15 seconds - warning threshold
                warnings.add(ValidationWarning(
                    field = "GPS Data Age",
                    value = "${gpsAge}ms",
                    rule = "gps_freshness_warning",
                    message = "GPS data is getting stale"
                ))
            }
            
            // ✅ ENHANCED: Check GPS point quality and quantity
            if (totalGpsPoints > 0) {
                val qualityPercentage = (validGpsPoints.toDouble() / totalGpsPoints) * 100
                
                // Check quality percentage
                when {
                    qualityPercentage < 50 -> {
                        errors.add(ValidationError(
                            field = "GPS Quality",
                            value = "${qualityPercentage}%",
                            rule = "gps_quality_critical",
                            message = "GPS data quality is critically poor",
                            severity = ErrorHandler.ErrorSeverity.HIGH
                        ))
                    }
                    qualityPercentage < 70 -> {
                        warnings.add(ValidationWarning(
                            field = "GPS Quality",
                            value = "${qualityPercentage}%",
                            rule = "gps_quality_warning",
                            message = "GPS data quality is below recommended threshold"
                        ))
                    }
                }
                
                // Check if we have enough data points for reliable calculations
                if (totalGpsPoints < 5) {
                    warnings.add(ValidationWarning(
                        field = "GPS Data Points",
                        value = "$totalGpsPoints",
                        rule = "gps_data_quantity",
                        message = "Limited GPS data available for accurate calculations"
                    ))
                }
            } else {
                // No GPS data at all
                errors.add(ValidationError(
                    field = "GPS Data",
                    value = "0",
                    rule = "gps_no_data",
                    message = "No GPS data available for trip calculations",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
                warnings = warnings
            )
        }
        
        /**
         * ✅ NEW: Validate database entity before insertion
         */
        fun validateTripEntity(tripEntity: TripEntity): ValidationResult {
            
            val errors = mutableListOf<ValidationError>()
            
            // Validate required fields
            if (tripEntity.date == null) {
                errors.add(ValidationError(
                    field = "Trip Date",
                    value = null,
                    rule = "required",
                    message = "Trip date is required",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
            }
            
            // Validate mile values
            if (tripEntity.loadedMiles < 0) {
                errors.add(ValidationError(
                    field = "Loaded Miles",
                    value = tripEntity.loadedMiles.toString(),
                    rule = "min_value",
                    message = "Loaded miles cannot be negative",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
            }
            
            if (tripEntity.bounceMiles < 0) {
                errors.add(ValidationError(
                    field = "Bounce Miles",
                    value = tripEntity.bounceMiles.toString(),
                    rule = "min_value",
                    message = "Bounce miles cannot be negative",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
            }
            
            if (tripEntity.actualMiles < 0) { // ✅ GPS-AWARE: Allow 0 for GPS initialization
                errors.add(ValidationError(
                    field = "Actual Miles",
                    value = tripEntity.actualMiles.toString(),
                    rule = "min_value",
                    message = "Actual miles cannot be negative",
                    severity = ErrorHandler.ErrorSeverity.HIGH
                ))
            }
            
            // Validate calculated fields consistency
            val expectedOorMiles = tripEntity.actualMiles - tripEntity.loadedMiles - tripEntity.bounceMiles
            if (abs(tripEntity.oorMiles - expectedOorMiles) > 0.01) {
                errors.add(ValidationError(
                    field = "OOR Miles Calculation",
                    value = "expected=${expectedOorMiles}, actual=${tripEntity.oorMiles}",
                    rule = "calculation_consistency",
                    message = "OOR miles calculation is inconsistent",
                    severity = ErrorHandler.ErrorSeverity.MEDIUM
                ))
            }
            
            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors
            )
        }
        
        /**
         * ✅ NEW: Get user-friendly validation message
         */
        fun getUserFriendlyMessage(validationResult: ValidationResult): String {
            return when {
                validationResult.hasErrors -> {
                    val firstError = validationResult.firstError
                    when (firstError?.rule) {
                        "required" -> "Please fill in all required fields"
                        "decimal_format" -> "Please enter a valid number"
                        "min_value" -> "Please enter a valid value"
                        "max_value" -> "Value is too high"
                        "business_logic" -> "Please check your input values"
                        "gps_accuracy_critical" -> "GPS accuracy is critically poor for vehicle tracking"
                        "gps_freshness" -> "GPS data is too old for accurate calculations"
                        "gps_quality_critical" -> "GPS data quality is critically poor"
                        else -> "Please check your input and try again"
                    }
                }
                validationResult.hasWarnings -> {
                    val firstWarning = validationResult.firstWarning
                    "Warning: ${firstWarning?.message ?: "Please verify your input"}"
                }
                else -> "Validation passed"
            }
        }
    }
} 
