package com.example.outofroutebuddy.domain.repository

import com.example.outofroutebuddy.domain.models.Trip
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for trip data operations
 *
 * This interface defines the contract for trip data operations in the domain layer.
 * It follows the Repository pattern to abstract data access from business logic.
 */
interface TripRepository {
    /**
     * Get all trips
     *
     * @return Flow of all trips
     */
    fun getAllTrips(): Flow<List<Trip>>

    /**
     * Get trip by ID
     *
     * @param id The trip ID
     * @return Flow of the trip, or null if not found
     */
    fun getTripById(id: String): Flow<Trip?>

    /**
     * Get trips for a specific date range
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return Flow of trips in the date range
     */
    fun getTripsByDateRange(
        startDate: Date,
        endDate: Date,
    ): Flow<List<Trip>>

    /**
     * Get trips by status
     *
     * @param status The trip status
     * @return Flow of trips with the specified status
     */
    fun getTripsByStatus(status: com.example.outofroutebuddy.domain.models.TripStatus): Flow<List<Trip>>

    /**
     * Get current active trip
     *
     * @return Flow of the current active trip, or null if none
     */
    fun getCurrentActiveTrip(): Flow<Trip?>

    /**
     * Insert a new trip
     *
     * @param trip The trip to insert
     * @return The ID of the inserted trip
     */
    suspend fun insertTrip(trip: Trip): String

    /**
     * Update an existing trip
     *
     * @param trip The trip to update
     */
    suspend fun updateTrip(trip: Trip)

    /**
     * Delete a trip
     *
     * @param trip The trip to delete
     */
    suspend fun deleteTrip(trip: Trip)

    /**
     * Delete trip by ID
     *
     * @param id The trip ID to delete
     */
    suspend fun deleteTripById(id: String)

    /**
     * Get trip statistics for a date range
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return Trip statistics
     */
    suspend fun getTripStatistics(
        startDate: Date,
        endDate: Date,
    ): TripStatistics

    /**
     * Get today's trip statistics
     *
     * @return Today's trip statistics
     */
    suspend fun getTodayTripStatistics(): TripStatistics

    /**
     * Get weekly trip statistics
     *
     * @return Weekly trip statistics
     */
    suspend fun getWeeklyTripStatistics(): TripStatistics

    /**
     * Get monthly trip statistics
     *
     * @return Monthly trip statistics
     */
    suspend fun getMonthlyTripStatistics(): TripStatistics

    /**
     * Get yearly trip statistics
     *
     * @return Yearly trip statistics
     */
    suspend fun getYearlyTripStatistics(): TripStatistics

    /**
     * Clear all trip data
     */
    suspend fun clearAllTrips()

    /**
     * Export trip data
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return Exported trip data as string
     */
    suspend fun exportTripData(
        startDate: Date,
        endDate: Date,
    ): String
}

/**
 * Trip statistics data class
 */
data class TripStatistics(
    val totalTrips: Int = 0,
    val totalLoadedMiles: Double = 0.0,
    val totalBounceMiles: Double = 0.0,
    val totalActualMiles: Double = 0.0,
    val totalOorMiles: Double = 0.0,
    val avgOorPercentage: Double = 0.0,
    val totalTripDuration: Int = 0,
    val avgTripDuration: Int = 0,
    val totalGpsPoints: Int = 0,
    val validGpsPoints: Int = 0,
    val rejectedGpsPoints: Int = 0,
    val avgGpsAccuracy: Double = 0.0,
    val locationJumpsDetected: Int = 0,
    val accuracyWarnings: Int = 0,
    val speedAnomalies: Int = 0,
) {
    /**
     * Calculate total dispatched miles
     */
    val totalDispatchedMiles: Double
        get() = totalLoadedMiles + totalBounceMiles

    /**
     * Calculate GPS quality percentage
     */
    val gpsQualityPercentage: Double
        get() =
            if (totalGpsPoints > 0) {
                (validGpsPoints.toDouble() / totalGpsPoints) * 100
            } else {
                0.0
            }

    /**
     * Calculate average trip duration in hours
     */
    val avgTripDurationHours: Double
        get() =
            if (totalTrips > 0) {
                avgTripDuration / 60.0
            } else {
                0.0
            }
} 
