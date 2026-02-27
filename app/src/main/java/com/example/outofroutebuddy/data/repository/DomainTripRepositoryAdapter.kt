package com.example.outofroutebuddy.data.repository

import android.util.Log
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository as DomainTripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.Locale
import com.example.outofroutebuddy.data.repository.TripRepository as DataTripRepository
import com.example.outofroutebuddy.utils.extensions.endOfDay
import com.example.outofroutebuddy.utils.extensions.endOfMonth
import com.example.outofroutebuddy.utils.extensions.startOfDay
import com.example.outofroutebuddy.utils.extensions.startOfMonth

/**
 * ✅ ADAPTER: Domain TripRepository implementation using data layer
 * 
 * This adapter bridges the gap between the domain layer interface
 * and the data layer implementation, converting between different
 * data models and handling any necessary transformations.
 */
class DomainTripRepositoryAdapter(
    private val dataRepository: DataTripRepository,
) : DomainTripRepository {

    companion object {
        private const val TAG = "DomainTripRepositoryAdapter"
    }
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
        }
    }

    override fun getTripById(id: String): Flow<Trip?> {
        return kotlinx.coroutines.flow.flow {
            val tripId = id.toLongOrNull()
            if (tripId != null) {
                val trip = dataRepository.getTripById(tripId)
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
            emit(null)
        }
    }

    override fun getTripsByDateRange(
        startDate: Date,
        endDate: Date,
    ): Flow<List<Trip>> {
        return kotlinx.coroutines.flow.flow {
            val trips = dataRepository.getTripsForDateRange(startDate, endDate)
            val domainTrips = trips.map { dataTrip ->
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
            Log.w(TAG, "getTripsByDateRange failed", e)
            emit(emptyList())
        }
    }

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
            emit(emptyList())
        }
    }

    override suspend fun getTripStatistics(startDate: Date, endDate: Date): TripStatistics {
        return try {
            val trips = dataRepository.getTripsForDateRange(startDate, endDate)
            
            val totalTrips = trips.size
            val totalLoadedMiles = trips.sumOf { it.loadedMiles }
            val totalBounceMiles = trips.sumOf { it.bounceMiles }
            val totalActualMiles = trips.sumOf { it.actualMiles }
            val totalOorMiles = trips.sumOf { it.oorMiles }
            val avgOorPercentage = if (totalTrips > 0) trips.map { it.oorPercentage }.average() else 0.0
            
            TripStatistics(
                totalTrips = totalTrips,
                totalLoadedMiles = totalLoadedMiles,
                totalBounceMiles = totalBounceMiles,
                totalActualMiles = totalActualMiles,
                totalOorMiles = totalOorMiles,
                avgOorPercentage = avgOorPercentage
            )
        } catch (e: Exception) {
            TripStatistics(
                totalTrips = 0,
                totalLoadedMiles = 0.0,
                totalBounceMiles = 0.0,
                totalActualMiles = 0.0,
                totalOorMiles = 0.0,
                avgOorPercentage = 0.0
            )
        }
    }

    // 1. Implement the missing insertTrip method
    override suspend fun insertTrip(trip: Trip): String {
        val dataTrip = com.example.outofroutebuddy.models.Trip(
            id = trip.id.toLongOrNull() ?: 0L,
            date = trip.startTime ?: java.util.Date(),
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles
        )
        val id = dataRepository.insertTrip(dataTrip)
        return id.toString()
    }

    override suspend fun updateTrip(trip: Trip): Boolean {
        val dataTrip = com.example.outofroutebuddy.models.Trip(
            id = trip.id.toLongOrNull() ?: 0L,
            date = trip.startTime ?: java.util.Date(),
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles
        )
        return dataRepository.updateTrip(dataTrip)
    }

    // 3. Implement other missing methods
    override suspend fun deleteTrip(trip: Trip): Boolean {
        val tripId = trip.id.toLongOrNull()
        if (tripId == null || tripId <= 0L) return false
        return dataRepository.deleteTripById(tripId)
    }

    override suspend fun deleteTripById(id: String): Boolean {
        val tripId = id.toLongOrNull()
        if (tripId == null || tripId <= 0L) return false
        return dataRepository.deleteTripById(tripId)
    }

    override suspend fun getTodayTripStatistics(): TripStatistics {
        val today = Date()
        val start = today.startOfDay()
        val end = today.endOfDay()
        return getTripStatistics(start, end)
    }

    override suspend fun getMonthlyTripStatistics(): TripStatistics {
        val today = Date()
        val start = today.startOfMonth()
        val end = today.endOfMonth()
        return getTripStatistics(start, end)
    }

    override suspend fun clearAllTrips() {
        dataRepository.clearAllTrips()
    }

    override suspend fun deleteTripsOlderThan(cutoffDate: Date) {
        dataRepository.deleteTripsOlderThan(cutoffDate)
    }

    override suspend fun exportTripData(startDate: Date, endDate: Date): String {
        val trips = dataRepository.getTripsForDateRange(startDate, endDate)
        return com.google.gson.Gson().toJson(trips)
    }
} 
