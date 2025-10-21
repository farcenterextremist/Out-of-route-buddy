package com.example.outofroutebuddy.services

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Service responsible for all trip-related calculations and business logic.
 * This service is pure business logic and can be easily unit tested.
 */
class TripCalculationService {
    private val logger = LoggerFactory.getLogger(TripCalculationService::class.java)
    
    /**
     * Calculate OOR miles (Out of Route miles)
     * @param loadedMiles Miles from loaded route
     * @param bounceMiles Miles from bounce route
     * @param actualMiles Actual miles driven
     * @return OOR miles (can be negative if actual < dispatched)
     */
    fun calculateOORMiles(loadedMiles: Double, bounceMiles: Double, actualMiles: Double): Double {
        val dispatchedMiles = calculateDispatchedMiles(loadedMiles, bounceMiles)
        return actualMiles - dispatchedMiles
    }
    
    /**
     * Calculate dispatched miles (loaded + bounce)
     */
    fun calculateDispatchedMiles(loadedMiles: Double, bounceMiles: Double): Double {
        return loadedMiles + bounceMiles
    }
    
    /**
     * Calculate OOR percentage
     * @param loadedMiles Miles from loaded route
     * @param bounceMiles Miles from bounce route
     * @param actualMiles Actual miles driven
     * @return OOR percentage (can be negative)
     */
    fun calculateOORPercentage(loadedMiles: Double, bounceMiles: Double, actualMiles: Double): Double {
        val dispatchedMiles = calculateDispatchedMiles(loadedMiles, bounceMiles)
        val oorMiles = calculateOORMiles(loadedMiles, bounceMiles, actualMiles)
        
        return calculatePercentage(oorMiles, dispatchedMiles)
    }
    
    /**
     * Calculate percentage with proper error handling
     */
    private fun calculatePercentage(numerator: Double, denominator: Double): Double {
        // Handle NaN values
        if (numerator.isNaN() || denominator.isNaN()) {
            logger.warn("NaN value detected in percentage calculation: numerator=$numerator, denominator=$denominator")
            return Double.NaN
        }
        
        // Handle infinity values
        if (numerator.isInfinite() || denominator.isInfinite()) {
            logger.warn("Infinite value detected in percentage calculation: numerator=$numerator, denominator=$denominator")
            return Double.POSITIVE_INFINITY
        }
        
        // Handle zero denominator
        if (denominator == 0.0) {
            logger.warn("Zero denominator in percentage calculation")
            return 0.0
        }
        
        // Calculate percentage
        val percentage = (numerator / denominator) * 100
        
        // Handle NaN and infinity results
        return if (percentage.isNaN() || percentage.isInfinite()) {
            logger.warn("Invalid percentage result: $percentage")
            Double.NaN
        } else {
            percentage
        }
    }
    
    /**
     * Calculate average OOR percentage from a list of percentages
     */
    fun calculateAverageOORPercentage(percentages: List<Double>): Double {
        if (percentages.isEmpty()) {
            return 0.0
        }
        
        val validPercentages = percentages.filter { !it.isNaN() && !it.isInfinite() }
        
        return if (validPercentages.isEmpty()) {
            logger.warn("No valid percentages found in list")
            0.0
        } else {
            validPercentages.average()
        }
    }
    
    /**
     * Validate trip input data
     * @return Pair of isValid and error message
     */
    fun validateTripInput(loadedMiles: String, bounceMiles: String, actualMiles: String): Pair<Boolean, String?> {
        val loaded = loadedMiles.toDoubleOrNull()
        val bounce = bounceMiles.toDoubleOrNull()
        val actual = actualMiles.toDoubleOrNull()
        
        return when {
            loaded == null -> Pair(false, "Invalid loaded miles")
            loaded <= 0 -> Pair(false, "Loaded miles must be greater than 0")
            bounce == null -> Pair(false, "Invalid bounce miles")
            bounce < 0 -> Pair(false, "Bounce miles cannot be negative")
            actual == null -> Pair(false, "Invalid actual miles")
            actual < 0 -> Pair(false, "Actual miles cannot be negative")
            else -> Pair(true, null)
        }
    }
    
    /**
     * Calculate total miles from a list of trips
     */
    fun calculateTotalMiles(trips: List<TripData>): Double {
        return trips.sumOf { it.actualMiles }
    }
    
    /**
     * Calculate total OOR miles from a list of trips
     */
    fun calculateTotalOORMiles(trips: List<TripData>): Double {
        return trips.sumOf { trip ->
            calculateOORMiles(trip.loadedMiles, trip.bounceMiles, trip.actualMiles)
        }
    }
    
    /**
     * Calculate average OOR percentage from a list of trips
     */
    fun calculateAverageOORPercentageFromTrips(trips: List<TripData>): Double {
        if (trips.isEmpty()) {
            return 0.0
        }
        
        val percentages = trips.map { trip ->
            calculateOORPercentage(trip.loadedMiles, trip.bounceMiles, trip.actualMiles)
        }
        
        return calculateAverageOORPercentage(percentages)
    }
    
    /**
     * Data class for trip calculations
     */
    data class TripData(
        val loadedMiles: Double,
        val bounceMiles: Double,
        val actualMiles: Double,
        val date: Date = Date()
    )
} 
