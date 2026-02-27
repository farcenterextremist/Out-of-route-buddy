package com.example.outofroutebuddy.data.dao

import androidx.room.*
import com.example.outofroutebuddy.data.entities.TripEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for trip operations in the Room database.
 * Provides methods for inserting, querying, and managing trip data.
 */
@Dao
interface TripDao {
    /**
     * Insert a new trip into the database
     */
    @Insert
    fun insertTrip(trip: TripEntity): Long

    /**
     * Insert multiple trips into the database
     */
    @Insert
    fun insertTrips(trips: List<TripEntity>)

    /**
     * Get all trips from the database
     */
    @Query("SELECT * FROM trips ORDER BY date DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    /**
     * Get a specific trip by ID
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripById(tripId: Long): TripEntity?

    /**
     * Update an existing trip
     */
    @Update
    fun updateTrip(trip: TripEntity)

    /**
     * Delete a trip by ID
     */
    @Query("DELETE FROM trips WHERE id = :tripId")
    fun deleteTripById(tripId: Long)

    /**
     * Get trips from a specific date onwards
     */
    @Query("SELECT * FROM trips WHERE date >= :startDate ORDER BY date DESC")
    fun getTripsFromDate(startDate: Date): List<TripEntity>

    /**
     * Get trips for a specific date
     */
    @Query("SELECT * FROM trips WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getTripsForDate(
        startOfDay: Date,
        endOfDay: Date,
    ): List<TripEntity>

    /**
     * Get trips for a date range
     */
    @Query("SELECT * FROM trips WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTripsForDateRange(
        startDate: Date,
        endDate: Date,
    ): List<TripEntity>

    /**
     * Delete a specific trip
     */
    @Delete
    fun deleteTrip(trip: TripEntity)

    /**
     * Delete all trips
     */
    @Query("DELETE FROM trips")
    fun deleteAllTrips()

    /**
     * Delete trips with date strictly before the given date (for "delete old data from device").
     */
    @Query("DELETE FROM trips WHERE date < :beforeDate")
    fun deleteTripsBefore(beforeDate: Date)

    /**
     * Get the total count of trips
     */
    @Query("SELECT COUNT(*) FROM trips")
    fun getTripCount(): Int

    /**
     * Get the total count of trips for a specific date
     */
    @Query("SELECT COUNT(*) FROM trips WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTripCountForDate(
        startOfDay: Date,
        endOfDay: Date,
    ): Int
} 
