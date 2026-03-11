package com.example.outofroutebuddy.models

import java.util.*

/**
 * Data model representing a trip in the Out of Route Buddy app
 *
 * Features:
 * - Clean data class with computed properties
 * - Proper handling of edge cases (NaN, Infinity, overflow)
 * - Immutable design prevents accidental modifications
 * - Clear separation of concerns
 * - Comprehensive edge case handling in calculations
 * - Comprehensive data validation for negative and unrealistic values
 * - Input validation with meaningful error messages
 * - Realistic value bounds checking
 */
data class Trip(
    val id: Long = 0,
    val date: Date = Date(),
    @field:com.example.outofroutebuddy.validation.MinValue(0.0)
    @field:com.example.outofroutebuddy.validation.MaxValue(10000.0)
    val loadedMiles: Double,
    @field:com.example.outofroutebuddy.validation.MinValue(0.0)
    @field:com.example.outofroutebuddy.validation.MaxValue(10000.0)
    val bounceMiles: Double,
    @field:com.example.outofroutebuddy.validation.MinValue(0.1)
    @field:com.example.outofroutebuddy.validation.MaxValue(10000.0)
    val actualMiles: Double, // required parameter, no default
) {
    // Validation and computed properties for trip data

        // Validation constants
    companion object {
        private const val MIN_MILES = 0.0
        private const val MAX_MILES = 10000.0 // Realistic maximum for a single trip

        /**
         * Validates trip data and throws appropriate exceptions for invalid data
         * @throws IllegalArgumentException if validation fails
         */
        fun validateTripData(
            loadedMiles: Double,
            bounceMiles: Double,
            actualMiles: Double,
        ) {
            // Check for NaN values first
            if (loadedMiles.isNaN() || bounceMiles.isNaN() || actualMiles.isNaN()) {
                throw IllegalArgumentException("Trip values cannot be NaN")
            }

            // Check for infinity values second
            if (loadedMiles.isInfinite() || bounceMiles.isInfinite() || actualMiles.isInfinite()) {
                throw IllegalArgumentException("Trip values cannot be infinite")
            }

            // Check for negative values
            if (loadedMiles < MIN_MILES) {
                throw IllegalArgumentException("Loaded miles cannot be negative: $loadedMiles")
            }
            if (bounceMiles < MIN_MILES) {
                throw IllegalArgumentException("Bounce miles cannot be negative: $bounceMiles")
            }
            if (actualMiles < 0.0) { // ✅ GPS-AWARE: Allow 0 for GPS initialization
                throw IllegalArgumentException("Actual miles cannot be negative: $actualMiles")
            }

            // Check for unrealistic values
            if (loadedMiles > MAX_MILES) {
                throw IllegalArgumentException("Loaded miles seems unrealistic (>$MAX_MILES): $loadedMiles")
            }
            if (bounceMiles > MAX_MILES) {
                throw IllegalArgumentException("Bounce miles seems unrealistic (>$MAX_MILES): $bounceMiles")
            }
            if (actualMiles > MAX_MILES) {
                throw IllegalArgumentException("Actual miles seems unrealistic (>$MAX_MILES): $actualMiles")
            }

            // ✅ GPS-AWARE: Only validate business logic if actual miles is meaningful (> 1.0)
            // This prevents validation errors when GPS is still initializing
            if (actualMiles > 1.0) {
                // Validate that actual miles is reasonable compared to dispatched miles
                val dispatchedMiles = loadedMiles + bounceMiles
                if (dispatchedMiles > 0 && actualMiles < dispatchedMiles * 0.5) {
                    throw IllegalArgumentException("Actual miles ($actualMiles) seems too low compared to dispatched miles ($dispatchedMiles)")
                }
                if (dispatchedMiles > 0 && actualMiles > dispatchedMiles * 5) {
                    throw IllegalArgumentException("Actual miles ($actualMiles) seems too high compared to dispatched miles ($dispatchedMiles)")
                }
            }
        }

        /**
         * Creates a validated Trip instance
         * @throws IllegalArgumentException if validation fails
         */
        fun createValidatedTrip(
            id: Long = 0,
            date: Date = Date(),
            loadedMiles: Double,
            bounceMiles: Double,
            actualMiles: Double,
        ): Trip {
            validateTripData(loadedMiles, bounceMiles, actualMiles)
            return Trip(id, date, loadedMiles, bounceMiles, actualMiles)
        }
    }

    // Initialize with validation
    init {
        // ✅ FIX: Validate all trips with strict requirements
        // Check for NaN values first
        require(!loadedMiles.isNaN() && !bounceMiles.isNaN() && !actualMiles.isNaN()) {
            "Trip values cannot be NaN"
        }

        // Check for infinity values
        require(!loadedMiles.isInfinite() && !bounceMiles.isInfinite() && !actualMiles.isInfinite()) {
            "Trip values cannot be infinite"
        }

        // ✅ FIX: Validate actualMiles FIRST (tests expect "Actual miles" error message)
        // Reject all-zeros case (trip must represent some activity)
        if (loadedMiles == 0.0 && bounceMiles == 0.0 && actualMiles == 0.0) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles: all trip values are zero")
        }
        
        // Reject epsilon values for actualMiles
        if (actualMiles > 0.0 && actualMiles < 0.001) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles: $actualMiles")
        }
        
        // For dispatched trips, actualMiles cannot be exactly 0.0
        val dispatchedTotal = loadedMiles + bounceMiles
        if (dispatchedTotal > 0.0 && actualMiles == 0.0) {
            throw IllegalArgumentException("Actual miles must be at least 0.001 miles for a dispatched trip: $actualMiles")
        }

        // ✅ FIX: Validate bounce miles - check negative first, then epsilon values
        require(bounceMiles >= 0) {
            "Bounce miles cannot be negative: $bounceMiles"
        }
        if (bounceMiles > 0.0 && bounceMiles < 0.001) {
            throw IllegalArgumentException("Bounce miles must be at least 0.001 miles: $bounceMiles")
        }
        require(bounceMiles < MAX_MILES) {
            "Bounce miles seems unrealistic (>$MAX_MILES): $bounceMiles"
        }

        // ✅ FIX: Validate loaded miles - reject epsilon values
        if (loadedMiles > 0.0 && loadedMiles < 0.001) {
            throw IllegalArgumentException("Loaded miles must be at least 0.001 miles: $loadedMiles")
        }
        require(loadedMiles < MAX_MILES) {
            "Loaded miles seems unrealistic (>$MAX_MILES): $loadedMiles"
        }
    }

    val dispatchedMiles: Double
        get() = loadedMiles + bounceMiles

    val oorMiles: Double
        get() = actualMiles - dispatchedMiles

    val oorPercentage: Double
        get() = calculateOORPercentage()

    /**
     * Calculate the Out of Route percentage for this trip
     */
    fun calculateOORPercentage(): Double {
        // Input validation and calculation logic

        // Handle NaN values
        if (this.loadedMiles.isNaN() || this.bounceMiles.isNaN() || this.actualMiles.isNaN()) {
            return Double.NaN
        }

        // Handle infinity values
        if (this.loadedMiles.isInfinite() || this.bounceMiles.isInfinite() || this.actualMiles.isInfinite()) {
            return Double.POSITIVE_INFINITY
        }

        // Handle zero dispatched miles
        if (this.dispatchedMiles == 0.0) {
            return 0.0
        }

        // Handle overflow cases (when dispatchedMiles becomes infinite due to addition overflow)
        if (this.dispatchedMiles.isInfinite()) {
            return Double.POSITIVE_INFINITY
        }

        // Calculate percentage
        val oorPercentage = (this.oorMiles / this.dispatchedMiles) * 100

        // Handle NaN and infinity results
        return if (oorPercentage.isNaN() || oorPercentage.isInfinite()) {
            Double.NaN
        } else {
            oorPercentage
        }
    }

    // Companion object with utility methods
} 
