package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.util.FormatUtils
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * ✅ NEW: Unified validation framework to eliminate repetitive code
 * This consolidates all validation logic into reusable components
 * 
 * BENEFITS:
 * - Eliminates code duplication between validateLocation and validateVehicleLocation
 * - Makes validation rules easily testable and composable
 * - Provides consistent validation behavior across the application
 * - Enables easy addition of new validation rules
 */
class LocationValidationFramework {
    
    companion object {
        // Logger accessible to all validation rules
        private val logger = LoggerFactory.getLogger(LocationValidationFramework::class.java)
        
        // ✅ CENTRALIZED: Using ValidationConfig for all constants
        const val MPS_TO_MPH = ValidationConfig.MPS_TO_MPH
        const val VEHICLE_MAX_SPEED_MPH = ValidationConfig.VEHICLE_MAX_SPEED_MPH
        const val VEHICLE_MAX_ACCELERATION_MPH_PER_SEC = ValidationConfig.VEHICLE_MAX_ACCELERATION_MPH_PER_SEC
        const val VEHICLE_MIN_ACCURACY = ValidationConfig.VEHICLE_MIN_ACCURACY
        const val DEFAULT_MAX_ACCURACY = ValidationConfig.MAX_ACCURACY
        const val TRAFFIC_ACCURACY_THRESHOLD = ValidationConfig.TRAFFIC_ACCURACY_THRESHOLD
        
        /**
         * ✅ NEW: Self-contained distance calculation method
         */
        private fun calculateDistance(location1: Location, location2: Location): Float {
            val results = FloatArray(1)
            Location.distanceBetween(
                location1.latitude, location1.longitude,
                location2.latitude, location2.longitude,
                results
            )
            return results[0]
        }
    }
    
    /**
     * ✅ NEW: Validation context containing all necessary data for validation
     */
    data class ValidationContext(
        val location: Location,
        val lastLocation: Location?,
        val lastUpdateTime: Long,
        val lastSpeed: Float,
        val config: LocationValidationService.ValidationConfigData,
        val trafficMode: Boolean = false,
        val currentTime: Long = System.currentTimeMillis(),
        val distanceCalculator: ((Location, Location) -> Float)? = null,
        val timeCalculator: ((Location) -> Long)? = null,
        val speedCalculator: ((Location) -> Float)? = null
    )
    
    /**
     * ✅ NEW: Individual validation rules for reusability
     */
    sealed class ValidationRule {
        abstract fun validate(context: ValidationContext): LocationValidationService.ValidationResult
        abstract val ruleName: String
    }
    
    /**
     * ✅ NEW: Location age validation rule
     */
    object LocationAgeRule : ValidationRule() {
        override val ruleName = "LocationAge"
        
        override fun validate(context: ValidationContext): LocationValidationService.ValidationResult {
            val locationTime = context.timeCalculator?.invoke(context.location) ?: context.location.time
            val timeSinceLastUpdate = context.currentTime - locationTime
            if (timeSinceLastUpdate > context.config.maxLocationAge) {
                logger.warn("$ruleName: Location too old: ${timeSinceLastUpdate}ms")
                return LocationValidationService.ValidationResult.Invalid(
                    "Location too old: ${timeSinceLastUpdate}ms",
                    LocationValidationService.ValidationSeverity.ERROR
                )
            }
            return LocationValidationService.ValidationResult.Valid
        }
    }
    
    /**
     * ✅ NEW: GPS accuracy validation rule with adaptive thresholds
     */
    object AccuracyRule : ValidationRule() {
        override val ruleName = "Accuracy"
        
        override fun validate(context: ValidationContext): LocationValidationService.ValidationResult {
            val adaptedThreshold = adaptGpsAccuracy(
                context.config.maxAccuracy, 
                context.trafficMode
            )
            
            logger.debug("$ruleName: Checking accuracy ${context.location.accuracy}m against threshold ${adaptedThreshold}m")
            
            when {
                context.location.accuracy > adaptedThreshold * 2f -> {
                    logger.error("$ruleName: GPS accuracy critically poor: ${context.location.accuracy}m")
                    return LocationValidationService.ValidationResult.Invalid(
                        "Poor vehicle GPS accuracy: ${context.location.accuracy}m",
                        LocationValidationService.ValidationSeverity.ERROR
                    )
                }
                context.location.accuracy > adaptedThreshold -> {
                    val mode = if (context.trafficMode) "traffic" else "normal"
                    logger.warn("$ruleName: GPS accuracy below ${mode} threshold: ${context.location.accuracy}m (threshold: ${adaptedThreshold}m)")
                    logger.debug("$ruleName: Returning Invalid result")
                    return LocationValidationService.ValidationResult.Invalid(
                        "Poor vehicle GPS accuracy: ${context.location.accuracy}m",
                        LocationValidationService.ValidationSeverity.WARNING
                    )
                }
            }
            logger.debug("$ruleName: Accuracy is acceptable")
            return LocationValidationService.ValidationResult.Valid
        }
        
        private fun adaptGpsAccuracy(baseAccuracy: Float, trafficMode: Boolean): Float {
            return if (trafficMode) {
                baseAccuracy * 1.5f // More lenient in traffic
            } else {
                baseAccuracy
            }
        }
    }
    
    /**
     * ✅ NEW: Speed validation rule with acceleration checking
     */
    object SpeedRule : ValidationRule() {
        override val ruleName = "Speed"
        
        override fun validate(context: ValidationContext): LocationValidationService.ValidationResult {
            val speed = context.speedCalculator?.invoke(context.location) ?: context.location.speed
            val speedMph = speed * 2.23694f // m/s to mph
            // ✅ FIXED: lastSpeed is already in mph from the service call, don't convert again
            val lastSpeedMph = context.lastSpeed // Already in mph
            logger.debug("SpeedRule: speedMph=$speedMph, lastSpeedMph=$lastSpeedMph, maxSpeedChange=${context.config.maxSpeedChange}")
            
            // Check for unrealistic speeds
            when {
                speedMph > VEHICLE_MAX_SPEED_MPH -> {
                    logger.warn("$ruleName: Speed unrealistic: $speedMph mph")
                    return LocationValidationService.ValidationResult.Invalid(
                        "Unrealistic vehicle speed: $speedMph mph",
                        LocationValidationService.ValidationSeverity.WARNING
                    )
                }
                // Only check speed change if we have a previous location
                context.lastLocation != null && abs(speedMph - lastSpeedMph) > context.config.maxSpeedChange -> {
                    logger.warn("$ruleName: Speed change too large: ${abs(speedMph - lastSpeedMph)}mph")
                    return LocationValidationService.ValidationResult.Invalid(
                        "Speed change too large: ${abs(speedMph - lastSpeedMph)}mph",
                        LocationValidationService.ValidationSeverity.WARNING
                    )
                }
            }
            
            // Check acceleration if we have a previous location
            if (context.lastLocation != null && context.lastLocation.hasSpeed()) {
                val lastSpeedMphFromLocation = context.lastLocation.speed * MPS_TO_MPH
                val timeDiffSeconds = (context.location.time - context.lastLocation.time) / 1000.0
                logger.debug("SpeedRule: lastSpeedMphFromLocation=$lastSpeedMphFromLocation, timeDiffSeconds=$timeDiffSeconds")
                if (timeDiffSeconds > 1.0) { // Only check acceleration if time diff is reasonable
                    val accelerationMphPerSec = abs(speedMph - lastSpeedMphFromLocation) / timeDiffSeconds.toFloat()
                    logger.debug("SpeedRule: accelerationMphPerSec=$accelerationMphPerSec, VEHICLE_MAX_ACCELERATION_MPH_PER_SEC=$VEHICLE_MAX_ACCELERATION_MPH_PER_SEC")
                    if (accelerationMphPerSec > VEHICLE_MAX_ACCELERATION_MPH_PER_SEC) {
                        logger.warn("$ruleName: Acceleration unrealistic: ${accelerationMphPerSec}mph/s")
                        return LocationValidationService.ValidationResult.Invalid(
                            "Unrealistic vehicle acceleration: ${accelerationMphPerSec}mph/s",
                            LocationValidationService.ValidationSeverity.WARNING
                        )
                    }
                } else {
                    logger.debug("SpeedRule: Skipping acceleration check due to small timeDiffSeconds=$timeDiffSeconds")
                }
            }
            
            return LocationValidationService.ValidationResult.Valid
        }
    }
    
    /**
     * ✅ NEW: Stationary detection rule
     */
    object StationaryRule : ValidationRule() {
        override val ruleName = "Stationary"
        
        override fun validate(context: ValidationContext): LocationValidationService.ValidationResult {
            if (context.lastLocation == null) return LocationValidationService.ValidationResult.Valid
            
            val distance = context.distanceCalculator?.invoke(context.lastLocation, context.location)
                ?: calculateDistance(context.lastLocation, context.location)
            val timeSinceLastLocation = context.currentTime - context.lastUpdateTime
            
            if (distance < context.config.minDistanceThreshold && 
                timeSinceLastLocation > context.config.maxStationaryTime) {
                logger.warn("$ruleName: Location appears stationary for too long: Distance: ${FormatUtils.formatMeters(distance.toDouble())}, Time: ${timeSinceLastLocation}ms")
                return LocationValidationService.ValidationResult.Invalid(
                    "Location appears stationary for too long",
                    LocationValidationService.ValidationSeverity.WARNING
                )
            }
            
            return LocationValidationService.ValidationResult.Valid
        }
    }
    
    /**
     * ✅ NEW: Location jump detection rule
     */
    object LocationJumpRule : ValidationRule() {
        override val ruleName = "LocationJump"
        
        override fun validate(context: ValidationContext): LocationValidationService.ValidationResult {
            if (context.lastLocation == null) return LocationValidationService.ValidationResult.Valid
            
            val distance = context.distanceCalculator?.invoke(context.lastLocation, context.location)
                ?: calculateDistance(context.lastLocation, context.location)
            logger.warn("$ruleName: Calculated distance: ${FormatUtils.formatMeters(distance.toDouble())}, threshold: ${context.config.maxDistanceBetweenUpdates}m")
            if (distance > context.config.maxDistanceBetweenUpdates) {
                logger.error("$ruleName: Location jump detected: ${FormatUtils.formatMeters(distance.toDouble())}")
                return LocationValidationService.ValidationResult.Invalid(
                    "Large position change detected: ${FormatUtils.formatMeters(distance.toDouble())}",
                    LocationValidationService.ValidationSeverity.CRITICAL
                )
            }
            
            return LocationValidationService.ValidationResult.Valid
        }
    }
    
    /**
     * ✅ NEW: Unified validation orchestrator
     * This replaces the repetitive validation logic in validateLocation and validateVehicleLocation
     */
    fun validateLocation(
        location: Location,
        lastLocation: Location?,
        lastUpdateTime: Long,
        lastSpeed: Float,
        config: LocationValidationService.ValidationConfigData,
        trafficMode: Boolean = false,
        rules: List<ValidationRule> = listOf(
            LocationAgeRule,
            AccuracyRule,
            SpeedRule,
            StationaryRule,
            LocationJumpRule
        ),
        currentTime: Long = System.currentTimeMillis(),
        distanceCalculator: ((Location, Location) -> Float)? = null,
        timeCalculator: ((Location) -> Long)? = null,
        speedCalculator: ((Location) -> Float)? = null
    ): LocationValidationService.ValidationResult {
        val context = ValidationContext(
            location = location,
            lastLocation = lastLocation,
            lastUpdateTime = lastUpdateTime,
            lastSpeed = lastSpeed,
            config = config,
            trafficMode = trafficMode,
            currentTime = currentTime,
            distanceCalculator = distanceCalculator,
            timeCalculator = timeCalculator,
            speedCalculator = speedCalculator
        )
        
        for (rule in rules) {
            val result = rule.validate(context)
            logger.warn("Rule ${rule.ruleName} returned: $result")
            if (result is LocationValidationService.ValidationResult.Invalid) {
                logger.warn("Validation failed at rule: ${rule.ruleName}")
                return result
            }
        }
        
        logger.debug("All validation rules passed")
        return LocationValidationService.ValidationResult.Valid
    }
    
    /**
     * ✅ NEW: Vehicle-specific validation with enhanced rules
     */
    fun validateVehicleLocation(
        location: Location,
        lastLocation: Location?,
        lastUpdateTime: Long,
        lastSpeed: Float, // <-- new parameter
        config: LocationValidationService.ValidationConfigData,
        trafficMode: Boolean = false,
        currentTime: Long = System.currentTimeMillis(),
        distanceCalculator: ((Location, Location) -> Float)? = null,
        timeCalculator: ((Location) -> Long)? = null,
        speedCalculator: ((Location) -> Float)? = null
    ): LocationValidationService.ValidationResult {
        // Create vehicle-specific context with vehicle thresholds
        val vehicleConfig = config.copy(
            maxAccuracy = VEHICLE_MIN_ACCURACY // Use vehicle-specific accuracy threshold
        )
        // Use the same framework but with vehicle-specific context
        return validateLocation(
            location = location,
            lastLocation = lastLocation,
            lastUpdateTime = lastUpdateTime,
            lastSpeed = lastSpeed, // <-- pass through
            config = vehicleConfig,
            trafficMode = trafficMode,
            rules = listOf(
                LocationAgeRule,
                AccuracyRule,
                LocationJumpRule,
                SpeedRule
            ),
            currentTime = currentTime,
            distanceCalculator = distanceCalculator,
            timeCalculator = timeCalculator,
            speedCalculator = speedCalculator
        )
    }
} 
