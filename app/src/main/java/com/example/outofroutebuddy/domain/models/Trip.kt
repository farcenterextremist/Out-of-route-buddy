package com.example.outofroutebuddy.domain.models

import java.util.Date

/**
 * Domain model representing a trip
 *
 * This is the core business entity that represents a trip in the domain layer.
 * It contains only the essential business logic and validation rules.
 */
data class Trip(
    val id: String = "",
    val loadedMiles: Double = 0.0,
    val bounceMiles: Double = 0.0,
    val actualMiles: Double = 0.0,
    val oorMiles: Double = 0.0,
    val oorPercentage: Double = 0.0,
    val startTime: Date? = null,
    val endTime: Date? = null,
    val status: TripStatus = TripStatus.PENDING,
    val gpsMetadata: GpsMetadata = GpsMetadata(),
    val validationIssues: List<ValidationIssue> = emptyList(),
    val gpsDistance: Double = 0.0,
    val gpsAccuracy: Double = 0.0,
    val gpsQuality: Double = 0.0,
) {
    init {
        // Validate trip values in constructor
        require(!loadedMiles.isNaN() && !bounceMiles.isNaN() && !actualMiles.isNaN()) {
            "Trip values cannot be NaN"
        }
        require(!loadedMiles.isInfinite() && !bounceMiles.isInfinite() && !actualMiles.isInfinite()) {
            "Trip values cannot be infinite"
        }

        // ✅ FIX: Validate actualMiles FIRST (tests expect "Actual miles" error message)
        val dispatchedTotal = loadedMiles + bounceMiles
        
        // Reject all-zeros case (trip must represent some activity)
        if (loadedMiles == 0.0 && bounceMiles == 0.0 && actualMiles == 0.0) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles: all trip values are zero")
        }
        
        // Reject epsilon values for actualMiles
        if (actualMiles > 0.0 && actualMiles < 0.001) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles: $actualMiles")
        }
        
        // ✅ FIX: For COMPLETED trips only, require meaningful actualMiles
        // Allow 0.0 for ACTIVE/PENDING trips (GPS is still updating)
        if (status == TripStatus.COMPLETED && dispatchedTotal > 0.0 && actualMiles == 0.0) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles for a completed trip: $actualMiles")
        }

        // ✅ FIX: Validate bounce miles - check negative first, then epsilon values
        require(bounceMiles >= 0) {
            "Bounce miles cannot be negative: $bounceMiles"
        }
        if (bounceMiles > 0.0 && bounceMiles < 0.001) {
            throw IllegalArgumentException("Bounce miles must be at least 0.001 miles: $bounceMiles")
        }
        require(bounceMiles < 10000.0) {
            "Bounce miles seems unrealistic (>10000.0): $bounceMiles"
        }
        
        // ✅ FIX: Validate loaded miles - reject epsilon values
        if (loadedMiles > 0.0 && loadedMiles < 0.001) {
            throw IllegalArgumentException("Loaded miles must be at least 0.001 miles: $loadedMiles")
        }
        require(loadedMiles < 10000.0) {
            "Loaded miles seems unrealistic (>10000.0): $loadedMiles"
        }
    }
    
    /**
     * Calculate the total dispatched miles (loaded + bounce)
     */
    val dispatchedMiles: Double
        get() = loadedMiles + bounceMiles

    /**
     * Check if the trip has valid data for calculation
     */
    val isValid: Boolean
        get() = loadedMiles > 0 && actualMiles > 0

    /**
     * Calculate trip duration in minutes
     */
    val durationMinutes: Int
        get() =
            if (startTime != null && endTime != null) {
                ((endTime.time - startTime.time) / (1000 * 60)).toInt()
            } else {
                0
            }

    /**
     * Check if trip is currently active
     */
    val isActive: Boolean
        get() = status == TripStatus.ACTIVE

    /**
     * Validate trip data and return validation issues
     */
    fun validate(): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        if (loadedMiles <= 0) {
            issues.add(ValidationIssue.INVALID_LOADED_MILES)
        }

        if (bounceMiles < 0) {
            issues.add(ValidationIssue.INVALID_BOUNCE_MILES)
        }

        if (actualMiles <= 0) {
            issues.add(ValidationIssue.INVALID_ACTUAL_MILES)
        }

        if (oorMiles < 0) {
            issues.add(ValidationIssue.INVALID_OOR_MILES)
        }

        if (oorPercentage < 0 || oorPercentage > 100) {
            issues.add(ValidationIssue.INVALID_OOR_PERCENTAGE)
        }

        return issues
    }

    /**
     * Create a copy with updated OOR calculations
     */
    fun withUpdatedOorCalculations(): Trip {
        // ✅ FIXED: Allow negative OOR miles (correct business logic)
        // OOR can be negative when actual miles < dispatched miles
        val newOorMiles = actualMiles - dispatchedMiles
        val newOorPercentage =
            if (dispatchedMiles > 0) {
                (newOorMiles / dispatchedMiles) * 100
            } else {
                0.0
            }

        return copy(
            oorMiles = newOorMiles,
            oorPercentage = newOorPercentage,
        )
    }

    /**
     * Create a copy with updated GPS metadata
     */
    fun withUpdatedGpsMetadata(metadata: GpsMetadata): Trip {
        return copy(gpsMetadata = metadata)
    }

    /**
     * Create a copy with updated status
     */
    fun withStatus(newStatus: TripStatus): Trip {
        return copy(status = newStatus)
    }

    /**
     * Create a copy with end time
     */
    fun withEndTime(endTime: Date): Trip {
        return copy(endTime = endTime)
    }
}

/**
 * Trip status enumeration
 */
enum class TripStatus {
    PENDING, // Trip is created but not started
    ACTIVE, // Trip is currently being tracked
    COMPLETED, // Trip has been completed
    CANCELLED, // Trip was cancelled
    PAUSED, // Trip is temporarily paused
}

/**
 * GPS metadata for a trip
 */
data class GpsMetadata(
    val totalPoints: Int = 0,
    val validPoints: Int = 0,
    val rejectedPoints: Int = 0,
    val avgAccuracy: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val locationJumps: Int = 0,
    val accuracyWarnings: Int = 0,
    val speedAnomalies: Int = 0,
    val tripDurationMinutes: Int = 0,
    val satelliteCount: Int = 0,
    val gpsQualityPercentage: Double = 0.0,
) {
    /**
     * Calculate GPS quality percentage
     */
    val calculatedGpsQuality: Double
        get() =
            if (totalPoints > 0) {
                (validPoints.toDouble() / totalPoints) * 100
            } else {
                0.0
            }

    /**
     * Check if GPS data is reliable
     */
    val isReliable: Boolean
        get() = gpsQualityPercentage >= 80.0 && avgAccuracy <= 20.0
}

/**
 * Validation issues that can occur with trip data
 */
enum class ValidationIssue {
    INVALID_LOADED_MILES,
    INVALID_BOUNCE_MILES,
    INVALID_ACTUAL_MILES,
    INVALID_OOR_MILES,
    INVALID_OOR_PERCENTAGE,
    INSUFFICIENT_GPS_DATA,
    POOR_GPS_ACCURACY,
    EXCESSIVE_LOCATION_JUMPS,
} 
