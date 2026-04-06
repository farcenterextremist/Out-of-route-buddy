package com.example.outofroutebuddy.data.dao

import androidx.room.*
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.domain.models.DataTier
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
    @Query("SELECT * FROM trips ORDER BY date ASC, id ASC")
    fun getAllTrips(): Flow<List<TripEntity>>

    /**
     * Find an existing trip matching the given business-key fields.
     * Uses a +/-60 second window for date, tripStartTime and tripEndTime so that
     * near-duplicate inserts (e.g. double endTrip with a new Date() each call) are caught.
     */
    @Query("""
        SELECT * FROM trips 
        WHERE ABS(date - :date) < 60000
        AND loadedMiles = :loadedMiles 
        AND bounceMiles = :bounceMiles 
        AND actualMiles = :actualMiles
        AND ((:tripStartTime IS NULL AND tripStartTime IS NULL) 
             OR ABS(tripStartTime - :tripStartTime) < 60000)
        AND ((:tripEndTime IS NULL AND tripEndTime IS NULL) 
             OR ABS(tripEndTime - :tripEndTime) < 60000)
        ORDER BY id DESC LIMIT 1
    """)
    fun findDuplicate(
        date: Date,
        loadedMiles: Double,
        bounceMiles: Double,
        actualMiles: Double,
        tripStartTime: Date?,
        tripEndTime: Date?,
    ): TripEntity?

    /**
     * Get a specific trip by ID (suspend to avoid blocking main thread).
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): TripEntity?

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
    @Query("SELECT * FROM trips WHERE date >= :startDate ORDER BY date ASC, id ASC")
    fun getTripsFromDate(startDate: Date): List<TripEntity>

    /**
     * Get trips for a specific date
     */
    @Query("SELECT * FROM trips WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date ASC, id ASC")
    fun getTripsForDate(
        startOfDay: Date,
        endOfDay: Date,
    ): List<TripEntity>

    /**
     * Get trips for a date range
     */
    @Query("SELECT * FROM trips WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    fun getTripsForDateRange(
        startDate: Date,
        endDate: Date,
    ): List<TripEntity>

    /**
     * Get trips overlapping a single day (supports midnight-spanning trips).
     * - Trips with tripStartTime and tripEndTime: included if day overlaps [start, end)
     * - Trips with null tripStartTime/tripEndTime: included if date falls in day (backward compat)
     */
    @Query("""
        SELECT * FROM trips WHERE
        ((tripStartTime IS NOT NULL AND tripEndTime IS NOT NULL)
         AND (tripStartTime < :endOfDay AND tripEndTime > :startOfDay))
        OR
        ((tripStartTime IS NULL OR tripEndTime IS NULL)
         AND (date >= :startOfDay AND date < :endOfDay))
        ORDER BY date ASC, id ASC
    """)
    fun getTripsOverlappingDay(
        startOfDay: Date,
        endOfDay: Date,
    ): List<TripEntity>

    /**
     * Get trips overlapping an arbitrary range [rangeStart, rangeEnd) (supports midnight-spanning trips).
     * Uses half-open end semantics to avoid double-including exact boundary starts in adjacent ranges.
     */
    @Query("""
        SELECT * FROM trips WHERE
        ((tripStartTime IS NOT NULL AND tripEndTime IS NOT NULL)
         AND (tripStartTime < :rangeEnd AND tripEndTime > :rangeStart))
        OR
        ((tripStartTime IS NULL OR tripEndTime IS NULL)
         AND (date >= :rangeStart AND date < :rangeEnd))
        ORDER BY date ASC, id ASC
    """)
    fun getTripsOverlappingRange(
        rangeStart: Date,
        rangeEnd: Date,
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
     * Delete trips with date strictly before [beforeDate] and dataTier in [tiers].
     * Used for tier-scoped retention (e.g. delete only SILVER, or SILVER+PLATINUM).
     */
    @Query("DELETE FROM trips WHERE date < :beforeDate AND dataTier IN (:tiers)")
    fun deleteTripsBeforeWithTiers(beforeDate: Date, tiers: List<String>)

    /**
     * Get the total count of trips
     */
    @Query("SELECT COUNT(*) FROM trips")
    fun getTripCount(): Int

    /** Main-safe count for debug exports and diagnostics (no full table load). */
    @Query("SELECT COUNT(*) FROM trips")
    suspend fun countTripsSuspend(): Int

    /**
     * Get the total count of trips for a specific date
     */
    @Query("SELECT COUNT(*) FROM trips WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTripCountForDate(
        startOfDay: Date,
        endOfDay: Date,
    ): Int

    /**
     * Get trips by data tier (SILVER, PLATINUM, GOLD).
     */
    @Query("SELECT * FROM trips WHERE dataTier = :tier ORDER BY date ASC, id ASC")
    fun getTripsByTier(tier: DataTier): Flow<List<TripEntity>>

    /**
     * Get trip IDs by data tier (for bulk ops / counts).
     */
    @Query("SELECT id FROM trips WHERE dataTier = :tier")
    suspend fun getTripIdsByTier(tier: DataTier): List<Long>
} 
