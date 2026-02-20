package com.example.outofroutebuddy.data.repository

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository as DomainTripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.outofroutebuddy.data.repository.TripRepository as DataTripRepository

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
            try {
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
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    override fun getTripsByDateRange(
        startDate: Date,
        endDate: Date,
    ): Flow<List<Trip>> {
        return kotlinx.coroutines.flow.flow {
            try {
                val trips = dataRepository.getTripsForDateRange(startDate, endDate)
                val domainTrips =
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
                emit(domainTrips)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }

    override fun getTripsByStatus(status: TripStatus): Flow<List<Trip>> {
        return kotlinx.coroutines.flow.flow {
            try {
                // For now, return all trips as completed since we don't have status tracking yet
                val allTrips = mutableListOf<com.example.outofroutebuddy.models.Trip>()
                dataRepository.getAllTrips().collect { trips ->
                    allTrips.clear()
                    allTrips.addAll(trips)
                }
                val domainTrips =
                    allTrips.map { dataTrip ->
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
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }

    override fun getCurrentActiveTrip(): Flow<Trip?> {
        return kotlinx.coroutines.flow.flow {
            try {
                // For now, return null since we don't have active trip tracking yet
                emit(null)
            } catch (e: Exception) {
                emit(null)
            }
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

    // 2. Implement the missing updateTrip method
    override suspend fun updateTrip(trip: Trip) {
        val dataTrip = com.example.outofroutebuddy.models.Trip(
            id = trip.id.toLongOrNull() ?: 0L,
            date = trip.startTime ?: java.util.Date(),
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles
        )
        dataRepository.updateTrip(dataTrip)
    }

    // 3. Implement other missing methods
    override suspend fun deleteTrip(trip: Trip) {
        val tripId = trip.id.toLongOrNull() ?: 0L
        dataRepository.deleteTripById(tripId)
    }

    override suspend fun deleteTripById(id: String) {
        dataRepository.deleteTripById(id.toLongOrNull() ?: 0L)
    }

    override suspend fun getTodayTripStatistics(): TripStatistics {
        val today = java.util.Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time
        
        return getTripStatistics(startOfDay, endOfDay)
    }

    override suspend fun getMonthlyTripStatistics(): TripStatistics {
        val today = java.util.Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = today
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time
        
        calendar.add(java.util.Calendar.MONTH, 1)
        val endOfMonth = calendar.time
        
        return getTripStatistics(startOfMonth, endOfMonth)
    }

    override suspend fun clearAllTrips() {
        dataRepository.clearAllTrips()
    }

    override suspend fun exportTripData(startDate: Date, endDate: Date): String {
        val trips = dataRepository.getTripsForDateRange(startDate, endDate)
        return com.google.gson.Gson().toJson(trips)
    }
} 
