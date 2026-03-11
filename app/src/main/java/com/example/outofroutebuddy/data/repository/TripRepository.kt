package com.example.outofroutebuddy.data.repository

import android.util.Log
import androidx.room.*
import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.models.Trip
import com.example.outofroutebuddy.util.TimeoutManager
import com.example.outofroutebuddy.validation.ValidationFramework
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

/**
 * Repository for managing trip data operations.
 * Provides a clean API for the ViewModel to interact with the database.
 */
class TripRepository(private val tripDao: TripDao) {
    companion object {
        private const val TAG = "TripRepository"
    }

    /**
     * Insert a new trip into the database with retry logic and enhanced error handling
     */
    suspend fun insertTrip(
        trip: Trip,
        gpsMetadata: Map<String, Any>? = null,
    ): Long {
        return withContext(Dispatchers.IO) {
            insertTripWithRetry(trip, gpsMetadata, maxRetries = 3)
        }
    }

    /**
     * ✅ NEW: Insert trip with retry logic and exponential backoff
     * ✅ NEW (#8): Added timeout protection for database writes
     */
    private suspend fun insertTripWithRetry(
        trip: Trip,
        gpsMetadata: Map<String, Any>? = null,
        maxRetries: Int = 3,
    ): Long {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                // ✅ NEW (#8): Wrap database write with timeout (3 seconds)
                val result = TimeoutManager.withDatabaseWriteTimeout("insertTrip") {
                    val tripEntity =
                        TripEntity(
                            date = trip.date,
                            loadedMiles = trip.loadedMiles,
                            bounceMiles = trip.bounceMiles,
                            actualMiles = trip.actualMiles,
                            oorMiles = trip.oorMiles,
                            oorPercentage = trip.oorPercentage,
                            // ✅ NEW: GPS metadata fields
                            avgGpsAccuracy = gpsMetadata?.get("avgGpsAccuracy") as? Double ?: 0.0,
                            minGpsAccuracy = gpsMetadata?.get("minGpsAccuracy") as? Double ?: 0.0,
                            maxGpsAccuracy = gpsMetadata?.get("maxGpsAccuracy") as? Double ?: 0.0,
                        totalGpsPoints = gpsMetadata?.get("totalGpsPoints") as? Int ?: 0,
                        validGpsPoints = gpsMetadata?.get("validGpsPoints") as? Int ?: 0,
                        rejectedGpsPoints = gpsMetadata?.get("rejectedGpsPoints") as? Int ?: 0,
                        tripDurationMinutes = gpsMetadata?.get("tripDurationMinutes") as? Int ?: 0,
                        avgSpeedMph = gpsMetadata?.get("avgSpeedMph") as? Double ?: 0.0,
                        maxSpeedMph = gpsMetadata?.get("maxSpeedMph") as? Double ?: 0.0,
                        locationJumpsDetected = gpsMetadata?.get("locationJumpsDetected") as? Int ?: 0,
                        accuracyWarnings = gpsMetadata?.get("accuracyWarnings") as? Int ?: 0,
                        speedAnomalies = gpsMetadata?.get("speedAnomalies") as? Int ?: 0,
                        tripStartTime = gpsMetadata?.get("tripStartTime") as? Date,
                        tripEndTime = gpsMetadata?.get("tripEndTime") as? Date,
                        tripTimeZoneId = gpsMetadata?.get("tripTimeZoneId") as? String,
                        wasInterrupted = gpsMetadata?.get("wasInterrupted") as? Boolean ?: false,
                        interruptionCount = gpsMetadata?.get("interruptionCount") as? Int ?: 0,
                        lastLocationLat = gpsMetadata?.get("lastLocationLat") as? Double ?: 0.0,
                        lastLocationLng = gpsMetadata?.get("lastLocationLng") as? Double ?: 0.0,
                        lastLocationTime = gpsMetadata?.get("lastLocationTime") as? Date,
                        interstatePercent = gpsMetadata?.get("interstatePercent") as? Double ?: 0.0,
                        interstateMinutes = gpsMetadata?.get("interstateMinutes") as? Int ?: 0,
                        backRoadsPercent = gpsMetadata?.get("backRoadsPercent") as? Double ?: 0.0,
                        backRoadsMinutes = gpsMetadata?.get("backRoadsMinutes") as? Int ?: 0,
                        truckStopsVisited = gpsMetadata?.get("truckStopsVisited") as? Int ?: 0,
                    )

                    // ✅ NEW: Validate entity before database insertion
                    val entityValidation = ValidationFramework.UnifiedValidation.validateTripEntity(tripEntity)
                    if (!entityValidation.isValid) {
                        val errorMessage = "Database validation failed: ${entityValidation.firstError?.message}"
                        Log.e(TAG, errorMessage)
                        throw RuntimeException(errorMessage)
                    }

                    tripDao.insertTrip(tripEntity)
                }
                
                // Handle timeout result
                if (result.isSuccess) {
                    val tripId = result.getOrThrow()
                    // Audit: filterable tag for detection of bulk/anomalous inserts (Purple Team 2025-02-20)
                    Log.w("TripInsertAudit", "trip_inserted result=true")
                    Log.d(TAG, "Successfully inserted trip (attempt ${attempt + 1})")
                    return tripId
                } else {
                    throw result.exceptionOrNull() ?: Exception("Database write failed")
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Failed to insert trip (attempt ${attempt + 1}/$maxRetries): ${e.message}")

                if (attempt < maxRetries - 1) {
                    val delayMs = 1000L * (attempt + 1) // Exponential backoff: 1s, 2s, 3s
                    Log.i(TAG, "Retrying in ${delayMs}ms...")
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }

        // All retries failed
        val errorMessage = "Failed to save trip data after $maxRetries attempts: ${lastException?.message}"
        Log.e(TAG, errorMessage, lastException)
        throw RuntimeException(errorMessage, lastException)
    }

    /**
     * Get all trips as a Flow with enhanced error handling
     */
    fun getAllTrips(): Flow<List<Trip>> {
        return try {
            tripDao.getAllTrips().map { entities ->
                try {
                    entities.map { entity ->
                        Trip(
                            id = entity.id,
                            date = entity.date,
                            loadedMiles = entity.loadedMiles,
                            bounceMiles = entity.bounceMiles,
                            actualMiles = entity.actualMiles,
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping trip entities: ${e.message}", e)
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all trips: ${e.message}", e)
            // Return empty flow instead of throwing to prevent app crashes
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    /**
     * ✅ NEW: Get a specific trip by ID
     */
    suspend fun getTripById(tripId: Long): Trip? {
        return try {
            val entity = tripDao.getTripById(tripId)
            entity?.let {
                Trip(
                    id = it.id,
                    date = it.date,
                    loadedMiles = it.loadedMiles,
                    bounceMiles = it.bounceMiles,
                    actualMiles = it.actualMiles,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trip by ID: ${e.message}", e)
            null
        }
    }

    /**
     * ✅ NEW: Update an existing trip
     * Preserves tripTimeZoneId, tripStartTime, tripEndTime from existing entity when not provided by caller.
     */
    suspend fun updateTrip(trip: Trip): Boolean {
        return try {
            val existing = tripDao.getTripById(trip.id) ?: return false
            val entity =
                TripEntity(
                    id = trip.id,
                    date = trip.date,
                    loadedMiles = trip.loadedMiles,
                    bounceMiles = trip.bounceMiles,
                    actualMiles = trip.actualMiles,
                    oorMiles = trip.oorMiles,
                    oorPercentage = trip.oorPercentage,
                    createdAt = existing.createdAt,
                    tripTimeZoneId = existing.tripTimeZoneId,
                    tripStartTime = existing.tripStartTime,
                    tripEndTime = existing.tripEndTime,
                    avgGpsAccuracy = existing.avgGpsAccuracy,
                    minGpsAccuracy = existing.minGpsAccuracy,
                    maxGpsAccuracy = existing.maxGpsAccuracy,
                    totalGpsPoints = existing.totalGpsPoints,
                    validGpsPoints = existing.validGpsPoints,
                    rejectedGpsPoints = existing.rejectedGpsPoints,
                    tripDurationMinutes = existing.tripDurationMinutes,
                    avgSpeedMph = existing.avgSpeedMph,
                    maxSpeedMph = existing.maxSpeedMph,
                    locationJumpsDetected = existing.locationJumpsDetected,
                    accuracyWarnings = existing.accuracyWarnings,
                    speedAnomalies = existing.speedAnomalies,
                    wasInterrupted = existing.wasInterrupted,
                    interruptionCount = existing.interruptionCount,
                    lastLocationLat = existing.lastLocationLat,
                    lastLocationLng = existing.lastLocationLng,
                    lastLocationTime = existing.lastLocationTime,
                    interstatePercent = existing.interstatePercent,
                    interstateMinutes = existing.interstateMinutes,
                    backRoadsPercent = existing.backRoadsPercent,
                    backRoadsMinutes = existing.backRoadsMinutes,
                    truckStopsVisited = existing.truckStopsVisited,
                )
            tripDao.updateTrip(entity)
            Log.d(TAG, "Successfully updated trip")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip: ${e.message}", e)
            false
        }
    }

    /**
     * ✅ NEW: Delete a trip by ID
     */
    suspend fun deleteTripById(tripId: Long): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                tripDao.deleteTripById(tripId)
            }
            Log.d(TAG, "Successfully deleted trip")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete trip: ${e.message}", e)
            false
        }
    }

    /**
     * ✅ NEW: Get TripEntity objects with GPS metadata for advanced info
     */
    suspend fun getTripEntitiesWithGpsMetadata(limit: Int = 10): List<TripEntity> {
        return try {
            // Get the TripEntity objects directly from the DAO
            val allEntities = tripDao.getAllTrips().first()
            val limitedEntities = allEntities.take(limit)
            Log.d(TAG, "Successfully retrieved ${limitedEntities.size} trip entities with GPS metadata")
            limitedEntities
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trip entities with GPS metadata: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get trips for today
     */
    suspend fun getTodayTrips(): List<Trip> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val trips =
                tripDao.getTripsForDate(startOfDay, endOfDay).map { entity ->
                    Trip(
                        id = entity.id,
                        date = entity.date,
                        loadedMiles = entity.loadedMiles,
                        bounceMiles = entity.bounceMiles,
                        actualMiles = entity.actualMiles,
                    )
                }
            Log.d(TAG, "Successfully retrieved ${trips.size} trips for today")
            trips
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's trips: ${e.message}", e)
            throw RuntimeException("Failed to load today's trip data: ${e.message}", e)
        }
    }

    /**
     * Get trips for a specific date range
     */
    suspend fun getTripsForDateRange(
        startDate: Date,
        endDate: Date,
    ): List<Trip> {
        return try {
            val trips =
                tripDao.getTripsForDateRange(startDate, endDate).map { entity ->
                    Trip(
                        id = entity.id,
                        date = entity.date,
                        loadedMiles = entity.loadedMiles,
                        bounceMiles = entity.bounceMiles,
                        actualMiles = entity.actualMiles,
                    )
                }
            Log.d(TAG, "Successfully retrieved ${trips.size} trips for date range")
            trips
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trips for date range: ${e.message}", e)
            throw RuntimeException("Failed to load trip data for date range: ${e.message}", e)
        }
    }

    /**
     * Get TripEntity list for a date range (for domain layer mapping with full metadata).
     */
    suspend fun getTripEntitiesForDateRange(
        startDate: Date,
        endDate: Date,
    ): List<TripEntity> {
        return try {
            val entities = withContext(Dispatchers.IO) {
                tripDao.getTripsForDateRange(startDate, endDate)
            }
            Log.d(TAG, "Successfully retrieved ${entities.size} trip entities for date range")
            entities
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trip entities for date range: ${e.message}", e)
            throw RuntimeException("Failed to load trip entities for date range: ${e.message}", e)
        }
    }

    /**
     * Get trips overlapping a single day (supports midnight-spanning trips).
     * Returns raw TripEntity for domain layer mapping with full metadata.
     */
    suspend fun getTripEntitiesOverlappingDay(
        startOfDay: Date,
        endOfDay: Date,
    ): List<TripEntity> {
        return try {
            val entities = withContext(Dispatchers.IO) {
                tripDao.getTripsOverlappingDay(startOfDay, endOfDay)
            }
            Log.d(TAG, "Successfully retrieved ${entities.size} trip entities overlapping day")
            entities
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trip entities overlapping day: ${e.message}", e)
            throw RuntimeException("Failed to load trip entities overlapping day: ${e.message}", e)
        }
    }

    /**
     * Get trips overlapping an arbitrary range [rangeStart, rangeEnd).
     * Returns raw TripEntity for domain-layer aggregation and metadata mapping.
     */
    suspend fun getTripEntitiesOverlappingRange(
        rangeStart: Date,
        rangeEnd: Date,
    ): List<TripEntity> {
        return try {
            val entities = withContext(Dispatchers.IO) {
                tripDao.getTripsOverlappingRange(rangeStart, rangeEnd)
            }
            Log.d(TAG, "Successfully retrieved ${entities.size} trip entities overlapping range")
            entities
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trip entities overlapping range: ${e.message}", e)
            throw RuntimeException("Failed to load trip entities overlapping range: ${e.message}", e)
        }
    }

    /**
     * Get total miles for today
     */
    suspend fun getTodayTotalMiles(): Double {
        return try {
            val todayTrips = getTodayTrips()
            val totalMiles = todayTrips.sumOf { it.dispatchedMiles }
            Log.d(TAG, "Today's total miles: $totalMiles")
            totalMiles
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's total miles: ${e.message}", e)
            0.0
        }
    }

    /**
     * Get total OOR miles for today
     */
    suspend fun getTodayOorMiles(): Double {
        return try {
            val todayTrips = getTodayTrips()
            val totalOorMiles = todayTrips.sumOf { it.oorMiles }
            Log.d(TAG, "Today's total OOR miles: $totalOorMiles")
            totalOorMiles
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's OOR miles: ${e.message}", e)
            0.0
        }
    }

    /**
     * Get average OOR percentage for today (weighted by dispatched miles)
     */
    suspend fun getTodayAvgOorPercentage(): Double {
        return try {
            val todayTrips = getTodayTrips()
            val avgPercentage =
                if (todayTrips.isNotEmpty()) {
                    val totalOorMiles = todayTrips.sumOf { it.oorMiles }
                    val totalDispatchedMiles = todayTrips.sumOf { it.dispatchedMiles }
                    if (totalDispatchedMiles > 0) {
                        (totalOorMiles / totalDispatchedMiles) * 100
                    } else {
                        0.0
                    }
                } else {
                    0.0
                }
            Log.d(TAG, "Today's average OOR percentage: $avgPercentage%")
            avgPercentage
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's average OOR percentage: ${e.message}", e)
            0.0
        }
    }

    /**
     * Clear all trips (for testing or data reset)
     */
    suspend fun clearAllTrips() {
        try {
            tripDao.deleteAllTrips()
            Log.d(TAG, "Successfully cleared all trips")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all trips: ${e.message}", e)
            throw RuntimeException("Failed to clear trip data: ${e.message}", e)
        }
    }

    /**
     * Delete trips with date strictly before [cutoffDate] (for "delete old data from device").
     */
    suspend fun deleteTripsOlderThan(cutoffDate: Date) {
        try {
            tripDao.deleteTripsBefore(cutoffDate)
            Log.d(TAG, "Successfully deleted trips older than $cutoffDate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete trips older than $cutoffDate: ${e.message}", e)
            throw RuntimeException("Failed to delete old trip data: ${e.message}", e)
        }
    }

    /**
     * Get trip count for today
     */
    suspend fun getTodayTripCount(): Int {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val count = tripDao.getTripCountForDate(startOfDay, endOfDay)
            Log.d(TAG, "Today's trip count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's trip count: ${e.message}", e)
            0
        }
    }
} 
