package com.example.outofroutebuddy.data.repository

import android.util.Log
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository as DomainTripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import com.example.outofroutebuddy.data.repository.TripRepository as DataTripRepository
import com.example.outofroutebuddy.utils.extensions.endOfDay
import com.example.outofroutebuddy.utils.extensions.endOfMonth
import com.example.outofroutebuddy.utils.extensions.startOfDay
import com.example.outofroutebuddy.utils.extensions.startOfMonth

/**
 * Adapter: domain TripRepository implementation using the data layer.
 * Converts between domain and data models; emits load errors via [loadErrors]. Main-safe; call from any dispatcher.
 */
class DomainTripRepositoryAdapter(
    private val dataRepository: DataTripRepository,
    private val stateCache: StateCache?,
) : DomainTripRepository {

    companion object {
        private const val TAG = "DomainTripRepositoryAdapter"
    }

    /** Emits when a load (getTripById, getTripsByDateRange, getTripsOverlappingDay) fails. UI can collect and show. */
    private val _loadErrors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val loadErrors: SharedFlow<String> = _loadErrors.asSharedFlow()

    /** Returns all trips as domain [Trip] list. Maps from data layer; errors emitted to [loadErrors] (D1). */
    override fun getAllTrips(): Flow<List<Trip>> {
        return dataRepository.getAllTrips().map { trips ->
            trips.map { dataTrip ->
                Trip(
                    id = dataTrip.id.toString(),
                    loadedMiles = dataTrip.loadedMiles,
                    bounceMiles = dataTrip.bounceMiles,
                    actualMiles = dataTrip.actualMiles,
                    oorMiles = dataTrip.oorMiles,
                    oorPercentage = dataTrip.oorPercentage,
                    startTime = dataTrip.date,
                    status = TripStatus.COMPLETED,
                )
            }
        }.catch { e ->
            Log.w(TAG, "getAllTrips failed", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            emit(emptyList())
        }
    }

    /** Returns a single trip by id, or null if not found/invalid id. Errors emitted to [loadErrors]. DB3: call DAO on IO. */
    override fun getTripById(id: String): Flow<Trip?> {
        return kotlinx.coroutines.flow.flow {
            val tripId = id.toLongOrNull()
            if (tripId != null) {
                val trip = withContext(Dispatchers.IO) { dataRepository.getTripById(tripId) }
                emit(
                    trip?.let { dataTrip ->
                        Trip(
                            id = dataTrip.id.toString(),
                            loadedMiles = dataTrip.loadedMiles,
                            bounceMiles = dataTrip.bounceMiles,
                            actualMiles = dataTrip.actualMiles,
                            oorMiles = dataTrip.oorMiles,
                            oorPercentage = dataTrip.oorPercentage,
                            startTime = dataTrip.date,
                            status = TripStatus.COMPLETED,
                        )
                    },
                )
            } else {
                emit(null)
            }
        }.catch { e ->
            Log.w(TAG, "getTripById failed for id=$id", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            emit(null)
        }
    }

    /** Returns trips in [startDate, endDate] as domain list. Errors emitted to [loadErrors]. */
    override fun getTripsByDateRange(
        startDate: Date,
        endDate: Date,
    ): Flow<List<Trip>> {
        return flow {
            val rangeEndExclusive = toExclusiveEnd(endDate)
            val entities = dataRepository.getTripEntitiesOverlappingRange(startDate, rangeEndExclusive)
            val domainTrips = entities.map { mapTripEntityToDomain(it) }
            emit(domainTrips)
        }.catch { e ->
            Log.w(TAG, "getTripsByDateRange failed", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            emit(emptyList())
        }
    }

    /** Returns trips overlapping the day [startOfDay, endOfDay] with full metadata. Errors emitted to [loadErrors]. */
    override fun getTripsOverlappingDay(
        startOfDay: Date,
        endOfDay: Date,
    ): Flow<List<Trip>> {
        return flow {
            val entities = dataRepository.getTripEntitiesOverlappingDay(startOfDay, endOfDay)
            val domainTrips = entities.map { mapTripEntityToDomain(it) }
                .sortedBy { it.startTime?.time ?: it.endTime?.time ?: 0L }
            emit(domainTrips)
        }.catch { e ->
            Log.w(TAG, "getTripsOverlappingDay failed", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            emit(emptyList())
        }
    }

    /** Maps TripEntity to domain Trip with full metadata (startTime, endTime, gpsMetadata). */
    private fun mapTripEntityToDomain(entity: TripEntity): Trip {
        return Trip(
            id = entity.id.toString(),
            loadedMiles = entity.loadedMiles,
            bounceMiles = entity.bounceMiles,
            actualMiles = entity.actualMiles,
            oorMiles = entity.oorMiles,
            oorPercentage = entity.oorPercentage,
            startTime = entity.tripStartTime ?: entity.date,
            endTime = entity.tripEndTime,
            timeZoneId = entity.tripTimeZoneId,
            status = TripStatus.COMPLETED,
            gpsMetadata = GpsMetadata(
                totalPoints = entity.totalGpsPoints,
                validPoints = entity.validGpsPoints,
                rejectedPoints = entity.rejectedGpsPoints,
                avgAccuracy = entity.avgGpsAccuracy,
                maxSpeed = entity.maxSpeedMph,
                locationJumps = entity.locationJumpsDetected,
                accuracyWarnings = entity.accuracyWarnings,
                speedAnomalies = entity.speedAnomalies,
                tripDurationMinutes = entity.tripDurationMinutes,
                satelliteCount = 0,
                gpsQualityPercentage = entity.gpsQualityPercentage,
                interstatePercent = entity.interstatePercent,
                interstateMinutes = entity.interstateMinutes,
                backRoadsPercent = entity.backRoadsPercent,
                backRoadsMinutes = entity.backRoadsMinutes,
                truckStopsVisited = entity.truckStopsVisited,
            ),
        )
    }

    /** Returns one-shot snapshot of trips with [status]. Uses first() to avoid hanging; errors emitted to [loadErrors]. */
    override fun getTripsByStatus(status: TripStatus): Flow<List<Trip>> {
        return flow {
            // One-shot snapshot: getAllTrips() is an infinite Flow, so use first() to avoid hanging
            val allTrips = dataRepository.getAllTrips().first()
            val domainTrips = allTrips.map { dataTrip ->
                Trip(
                    id = dataTrip.id.toString(),
                    loadedMiles = dataTrip.loadedMiles,
                    bounceMiles = dataTrip.bounceMiles,
                    actualMiles = dataTrip.actualMiles,
                    oorMiles = dataTrip.oorMiles,
                    oorPercentage = dataTrip.oorPercentage,
                    startTime = dataTrip.date,
                    status = TripStatus.COMPLETED,
                )
            }
            emit(domainTrips)
        }.catch { e ->
            Log.w(TAG, "getTripsByStatus failed", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            emit(emptyList())
        }
    }

    /** Aggregates trip statistics for [startDate, endDate]. Returns empty stats on error. */
    override suspend fun getTripStatistics(startDate: Date, endDate: Date): TripStatistics {
        return try {
            val rangeEndExclusive = toExclusiveEnd(endDate)
            val trips = dataRepository.getTripEntitiesOverlappingRange(startDate, rangeEndExclusive)

            val totalTrips = trips.size
            val weightedParts = trips.map { entity ->
                val ratio = overlapRatio(entity, startDate, rangeEndExclusive)
                WeightedPart(
                    loadedMiles = entity.loadedMiles * ratio,
                    bounceMiles = entity.bounceMiles * ratio,
                    actualMiles = entity.actualMiles * ratio,
                    oorMiles = entity.oorMiles * ratio,
                )
            }
            val totalLoadedMiles = weightedParts.sumOf { it.loadedMiles }
            val totalBounceMiles = weightedParts.sumOf { it.bounceMiles }
            val totalActualMiles = weightedParts.sumOf { it.actualMiles }
            val totalOorMiles = weightedParts.sumOf { it.oorMiles }
            val totalDispatchedMiles = totalLoadedMiles + totalBounceMiles
            val avgOorPercentage =
                if (totalDispatchedMiles > 0.0) {
                    (totalOorMiles / totalDispatchedMiles) * 100.0
                } else {
                    0.0
                }

            TripStatistics(
                totalTrips = totalTrips,
                totalLoadedMiles = totalLoadedMiles,
                totalBounceMiles = totalBounceMiles,
                totalActualMiles = totalActualMiles,
                totalOorMiles = totalOorMiles,
                avgOorPercentage = avgOorPercentage
            )
        } catch (e: Exception) {
            Log.w(TAG, "getTripStatistics failed", e)
            _loadErrors.tryEmit(e.message ?: "Load failed")
            return TripStatistics(
                totalTrips = 0,
                totalLoadedMiles = 0.0,
                totalBounceMiles = 0.0,
                totalActualMiles = 0.0,
                totalOorMiles = 0.0,
                avgOorPercentage = 0.0
            )
        }
    }

    private data class WeightedPart(
        val loadedMiles: Double,
        val bounceMiles: Double,
        val actualMiles: Double,
        val oorMiles: Double,
    )

    /**
     * Calculates how much of a trip belongs to [rangeStart, rangeEndExclusive).
     * - Timed trips are split proportionally by overlap duration.
     * - Legacy trips without explicit start/end are treated as point-in-time entries.
     */
    private fun overlapRatio(
        trip: TripEntity,
        rangeStart: Date,
        rangeEndExclusive: Date,
    ): Double {
        val tripStart = trip.tripStartTime ?: trip.date
        val tripEnd = trip.tripEndTime

        // Legacy/point trip: all miles belong to the single bucket it falls in.
        if (tripEnd == null || tripEnd.time <= tripStart.time) {
            return if (tripStart.time in rangeStart.time until rangeEndExclusive.time) 1.0 else 0.0
        }

        val overlapStart = max(tripStart.time, rangeStart.time)
        val overlapEnd = min(tripEnd.time, rangeEndExclusive.time)
        val overlapMillis = (overlapEnd - overlapStart).coerceAtLeast(0L)
        val tripDurationMillis = (tripEnd.time - tripStart.time).coerceAtLeast(1L)
        return (overlapMillis.toDouble() / tripDurationMillis.toDouble()).coerceIn(0.0, 1.0)
    }

    /**
     * Trip statistics accept an inclusive end date in callers; overlap SQL uses [start, end).
     * Convert safely to exclusive-end semantics with +1ms when possible.
     */
    private fun toExclusiveEnd(inclusiveEnd: Date): Date {
        val endMillis = inclusiveEnd.time
        return if (endMillis == Long.MAX_VALUE) inclusiveEnd else Date(endMillis + 1L)
    }

    /** Inserts [trip] into the data layer; returns assigned id. Passes through start/end for calendar overlap queries. */
    override suspend fun insertTrip(trip: Trip): String {
        val dataTrip = com.example.outofroutebuddy.models.Trip(
            id = trip.id.toLongOrNull() ?: 0L,
            date = trip.startTime ?: java.util.Date(),
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles
        )
        // Pass start/end times so TripEntity has tripStartTime/tripEndTime for calendar and getTripsOverlappingDay
        val gpsMetadata = mutableMapOf<String, Any>()
        trip.startTime?.let { gpsMetadata["tripStartTime"] = it }
        trip.endTime?.let { gpsMetadata["tripEndTime"] = it }
        trip.timeZoneId?.let { gpsMetadata["tripTimeZoneId"] = it }
        val id = dataRepository.insertTrip(dataTrip, if (gpsMetadata.isEmpty()) null else gpsMetadata)
        stateCache?.invalidateAll()
        return id.toString()
    }

    /** Updates existing trip in data layer. Returns true if updated, false if not found. */
    override suspend fun updateTrip(trip: Trip): Boolean {
        val dataTrip = com.example.outofroutebuddy.models.Trip(
            id = trip.id.toLongOrNull() ?: 0L,
            date = trip.startTime ?: java.util.Date(),
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles
        )
        return dataRepository.updateTrip(dataTrip)
            .also { if (it) stateCache?.invalidateAll() }
    }

    /** Deletes trip; returns false if id invalid or not found. */
    override suspend fun deleteTrip(trip: Trip): Boolean {
        val tripId = extractNumericTripId(trip.id)
        if (tripId == null || tripId <= 0L) return false
        return dataRepository.deleteTripById(tripId)
            .also { if (it) stateCache?.invalidateAll() }
    }

    /** Deletes trip by id; returns false if id invalid or not found. */
    override suspend fun deleteTripById(id: String): Boolean {
        val tripId = extractNumericTripId(id)
        if (tripId == null || tripId <= 0L) return false
        return dataRepository.deleteTripById(tripId)
            .also { if (it) stateCache?.invalidateAll() }
    }

    private fun extractNumericTripId(id: String): Long? {
        id.toLongOrNull()?.let { return it }
        val digits = id.takeLastWhile { it.isDigit() }
        return digits.toLongOrNull()
    }

    /** Today's aggregate statistics (start of day to end of day). */
    override suspend fun getTodayTripStatistics(): TripStatistics {
        val today = Date()
        val start = today.startOfDay()
        val end = today.endOfDay()
        return getTripStatistics(start, end)
    }

    /** Current month's aggregate statistics. */
    override suspend fun getMonthlyTripStatistics(): TripStatistics {
        val today = Date()
        val start = today.startOfMonth()
        val end = today.endOfMonth()
        return getTripStatistics(start, end)
    }

    /** Clears all trips in the data layer. */
    override suspend fun clearAllTrips() {
        dataRepository.clearAllTrips()
        stateCache?.invalidateAll()
    }

    /** Deletes trips with date strictly before [cutoffDate]. */
    override suspend fun deleteTripsOlderThan(cutoffDate: Date) {
        dataRepository.deleteTripsOlderThan(cutoffDate)
        stateCache?.invalidateAll()
    }

    /** Exports trip data for [startDate, endDate] as JSON string. */
    override suspend fun exportTripData(startDate: Date, endDate: Date): String {
        val trips = dataRepository.getTripsForDateRange(startDate, endDate)
        return com.google.gson.Gson().toJson(trips)
    }
} 
