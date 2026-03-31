package com.example.outofroutebuddy.utils

import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * Lightweight mock TripRepository for simulation and unit tests.
 *
 * Configurable in-memory trips. No database. Use for:
 * - StatCardCalendarWiringSimulationTest
 * - TripHistoryByDateViewModelTest
 * - Any test needing trip data without Room
 *
 * Example:
 *   val mock = MockTripRepository()
 *   mock.setTrips(listOf(trip1, trip2))
 *   mock.setMonthlyStats(TripStatistics(...))
 */
class MockTripRepository : TripRepository {

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    private var _monthlyStats = TripStatistics()
    private var _periodStats = TripStatistics()
    private var _yearStats = TripStatistics()
    private var _todayStats = TripStatistics()
    private var shouldFail = false

    /** Set trips to return from getAllTrips, getTripsByDateRange, etc. */
    fun setTrips(trips: List<Trip>) {
        _trips.value = trips
    }

    /** Add a trip to the mock list */
    fun addTrip(trip: Trip) {
        _trips.value = _trips.value + trip
    }

    /** Set monthly statistics for getMonthlyTripStatistics */
    fun setMonthlyStats(stats: TripStatistics) {
        _monthlyStats = stats
        _periodStats = stats
    }

    /** Set period statistics for getTripStatistics(short range, e.g. month) */
    fun setPeriodStats(stats: TripStatistics) {
        _periodStats = stats
    }

    /** Set year statistics for getTripStatistics(long range, e.g. year) */
    fun setYearStats(stats: TripStatistics) {
        _yearStats = stats
    }

    /** Set today's statistics for getTodayTripStatistics */
    fun setTodayStats(stats: TripStatistics) {
        _todayStats = stats
    }

    /** When true, methods throw or return empty */
    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }

    override fun getAllTrips(): Flow<List<Trip>> = _trips

    override fun getTripById(id: String): Flow<Trip?> =
        flowOf(_trips.value.find { it.id == id })

    override fun getTripsByDateRange(startDate: Date, endDate: Date): Flow<List<Trip>> =
        flowOf(_trips.value.filter { it.endTime?.let { e -> !e.before(startDate) && !e.after(endDate) } == true })

    override fun getTripsOverlappingDay(startOfDay: Date, endOfDay: Date): Flow<List<Trip>> =
        flowOf(_trips.value.filter { trip ->
            val start = trip.startTime ?: return@filter false
            val end = trip.endTime ?: return@filter false
            start.before(endOfDay) && end.after(startOfDay)
        })

    override fun getTripsByStatus(status: TripStatus): Flow<List<Trip>> =
        flowOf(_trips.value.filter { it.status == status })

    override fun getTripsByTier(tier: DataTier): Flow<List<Trip>> =
        flowOf(_trips.value.filter { it.dataTier == tier })

    override suspend fun insertTrip(trip: Trip): String {
        if (shouldFail) throw RuntimeException("MockTripRepository: insert failed")
        addTrip(trip)
        return trip.id
    }

    override suspend fun updateTrip(trip: Trip): Boolean {
        if (shouldFail) return false
        _trips.value = _trips.value.map { if (it.id == trip.id) trip else it }
        return true
    }

    override suspend fun deleteTrip(trip: Trip): Boolean {
        if (shouldFail) return false
        _trips.value = _trips.value.filter { it.id != trip.id }
        return true
    }

    override suspend fun deleteTripById(id: String): Boolean {
        if (shouldFail) return false
        _trips.value = _trips.value.filter { it.id != id }
        return true
    }

    override suspend fun setTripTier(tripId: String, tier: DataTier): Boolean {
        if (shouldFail) return false
        val index = _trips.value.indexOfFirst { it.id == tripId }
        if (index < 0) return false
        val list = _trips.value.toMutableList()
        list[index] = list[index].copy(dataTier = tier)
        _trips.value = list
        return true
    }

    override suspend fun getTripStatistics(startDate: Date, endDate: Date): TripStatistics {
        val rangeDays = (endDate.time - startDate.time) / (24 * 60 * 60 * 1000)
        return if (rangeDays > 200) _yearStats else _periodStats
    }

    override suspend fun getTodayTripStatistics(): TripStatistics = _todayStats

    override suspend fun getMonthlyTripStatistics(): TripStatistics = _monthlyStats

    override suspend fun clearAllTrips() {
        if (!shouldFail) _trips.value = emptyList()
    }

    override suspend fun deleteTripsOlderThan(cutoffDate: Date, maxTier: DataTier?) {
        if (!shouldFail) {
            _trips.value = _trips.value.filter { it.endTime != null && !it.endTime!!.before(cutoffDate) }
        }
    }

    override suspend fun exportTripData(startDate: Date, endDate: Date): String =
        "" // Stub for tests that don't need export

    override suspend fun exportSharedPoolTripData(startDate: Date, endDate: Date, tier: DataTier): String =
        "" // Stub for tests that don't need shared-pool export
}
