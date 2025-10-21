package com.example.outofroutebuddy.domain.usecase

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import javax.inject.Inject

/**
 * Use case for calculating Out of Route (OOR) metrics for a trip
 *
 * This use case encapsulates the business logic for calculating OOR miles
 * and percentages, ensuring consistency across the application.
 */
class CalculateTripOorUseCase
    @Inject
    constructor() {
        /**
         * Calculate OOR metrics for a trip
         *
         * @param loadedMiles The loaded miles for the trip
         * @param bounceMiles The bounce miles for the trip
         * @param actualMiles The actual miles traveled
         * @return Trip with calculated OOR metrics
         */
        fun execute(
            loadedMiles: Double,
            bounceMiles: Double,
            actualMiles: Double,
        ): Trip {
            val dispatchedMiles = loadedMiles + bounceMiles
            // ✅ FIXED: Allow negative OOR miles (correct business logic)
            // OOR can be negative when actual miles < dispatched miles
            val oorMiles = actualMiles - dispatchedMiles
            val oorPercentage =
                if (dispatchedMiles > 0) {
                    (oorMiles / dispatchedMiles) * 100
                } else {
                    0.0
                }

            return Trip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                status = TripStatus.PENDING,
            )
        }

        /**
         * Calculate OOR metrics for an existing trip
         *
         * @param trip The trip to calculate OOR metrics for
         * @return Updated trip with calculated OOR metrics
         */
        fun execute(trip: Trip): Trip {
            return execute(
                loadedMiles = trip.loadedMiles,
                bounceMiles = trip.bounceMiles,
                actualMiles = trip.actualMiles,
            )
        }

        /**
         * Validate trip data for OOR calculation
         *
         * @param loadedMiles The loaded miles
         * @param bounceMiles The bounce miles
         * @param actualMiles The actual miles
         * @return List of validation issues, empty if valid
         */
        fun validateInput(
            loadedMiles: Double,
            bounceMiles: Double,
            actualMiles: Double,
        ): List<String> {
            val issues = mutableListOf<String>()

            if (loadedMiles <= 0) {
                issues.add("Loaded miles must be greater than 0")
            }

            if (bounceMiles < 0) {
                issues.add("Bounce miles cannot be negative")
            }

            if (actualMiles <= 0) {
                issues.add("Actual miles must be greater than 0")
            }

            if (loadedMiles + bounceMiles > actualMiles) {
                issues.add("Dispatched miles cannot exceed actual miles")
            }

            return issues
        }

        /**
         * Get OOR efficiency rating based on percentage
         *
         * @param oorPercentage The OOR percentage
         * @return Efficiency rating string
         */
        fun getEfficiencyRating(oorPercentage: Double): String {
            return when {
                oorPercentage <= 5.0 -> "Excellent"
                oorPercentage <= 10.0 -> "Good"
                oorPercentage <= 15.0 -> "Fair"
                oorPercentage <= 25.0 -> "Poor"
                else -> "Very Poor"
            }
        }

        /**
         * Calculate cost impact of OOR miles
         *
         * @param oorMiles The OOR miles
         * @param costPerMile The cost per mile (default $0.50)
         * @return The cost impact
         */
        fun calculateCostImpact(
            oorMiles: Double,
            costPerMile: Double = 0.50,
        ): Double {
            return oorMiles * costPerMile
        }
    } 
