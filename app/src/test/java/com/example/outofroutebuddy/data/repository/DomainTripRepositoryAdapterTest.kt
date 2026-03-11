package com.example.outofroutebuddy.data.repository

import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.models.Trip as DataTrip
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * Unit tests for DomainTripRepositoryAdapter: data → domain mapping and one-shot flows.
 */
class DomainTripRepositoryAdapterTest {

    private lateinit var dataRepository: TripRepository
    private lateinit var adapter: DomainTripRepositoryAdapter

    @Before
    fun setUp() {
        dataRepository = mockk(relaxed = true)
        adapter = DomainTripRepositoryAdapter(dataRepository, null)
    }

    @Test
    fun `getTripsByDateRange maps data trips to domain and emits once`() = runTest {
        val start = Date()
        val end = Date()
        val entities = listOf(
            TripEntity(id = 1L, date = start, loadedMiles = 10.0, bounceMiles = 2.0, actualMiles = 12.0, oorMiles = 0.0, oorPercentage = 0.0),
        )
        coEvery { dataRepository.getTripEntitiesOverlappingRange(start, any()) } returns entities

        val result = adapter.getTripsByDateRange(start, end).first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals(10.0, result[0].loadedMiles, 0.0)
        assertEquals(12.0, result[0].actualMiles, 0.0)
        assertEquals(start, result[0].startTime)
        assertEquals(TripStatus.COMPLETED, result[0].status)
    }

    @Test
    fun `getTripsByDateRange emits empty list when data returns empty`() = runTest {
        coEvery { dataRepository.getTripEntitiesOverlappingRange(any(), any()) } returns emptyList()

        val result = adapter.getTripsByDateRange(Date(), Date()).first()

        assertEquals(0, result.size)
    }

    @Test
    fun `getTripsByStatus uses first snapshot and maps to domain`() = runTest {
        val dataTrips = listOf(
            DataTrip(id = 2L, date = Date(), loadedMiles = 5.0, bounceMiles = 0.0, actualMiles = 5.0),
        )
        every { dataRepository.getAllTrips() } returns flowOf(dataTrips)

        val result = adapter.getTripsByStatus(TripStatus.COMPLETED).first()

        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
        assertEquals(5.0, result[0].actualMiles, 0.0)
    }

    @Test
    fun `getTripById returns null for invalid id`() = runTest {
        val result = adapter.getTripById("not-a-number").first()
        assertNull(result)
    }

    @Test
    fun `deleteTrip returns false for invalid id`() = runTest {
        val tripWithInvalidId = com.example.outofroutebuddy.domain.models.Trip(
            id = "invalid",
            loadedMiles = 10.0,
            bounceMiles = 0.0,
            actualMiles = 10.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED,
        )
        val deleted = adapter.deleteTrip(tripWithInvalidId)
        assertEquals(false, deleted)
    }

    @Test
    fun `deleteTrip returns false for zero id`() = runTest {
        val tripWithZeroId = com.example.outofroutebuddy.domain.models.Trip(
            id = "0",
            loadedMiles = 10.0,
            bounceMiles = 0.0,
            actualMiles = 10.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED,
        )
        val deleted = adapter.deleteTrip(tripWithZeroId)
        assertEquals(false, deleted)
    }

    @Test
    fun `deleteTrip returns data layer result`() = runTest {
        val trip = com.example.outofroutebuddy.domain.models.Trip(
            id = "1",
            loadedMiles = 10.0,
            bounceMiles = 0.0,
            actualMiles = 10.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED,
        )
        coEvery { dataRepository.deleteTripById(1L) } returns true

        val deleted = adapter.deleteTrip(trip)

        assertEquals(true, deleted)
        coVerify { dataRepository.deleteTripById(1L) }
    }

    @Test
    fun `deleteTripsOlderThan delegates to data repository`() = runTest {
        val cutoff = Date()
        coEvery { dataRepository.deleteTripsOlderThan(cutoff) } returns Unit

        adapter.deleteTripsOlderThan(cutoff)

        coVerify { dataRepository.deleteTripsOlderThan(cutoff) }
    }

    // ==================== Phase 3: getTripStatistics aggregation tests ====================

    @Test
    fun `getTripStatistics returns correct totals and average OOR percent for multiple trips`() = runTest {
        val start = Date()
        val end = Date()
        val dataTrips = listOf(
            DataTrip(id = 1L, date = start, loadedMiles = 100.0, bounceMiles = 25.0, actualMiles = 125.0),
            DataTrip(id = 2L, date = start, loadedMiles = 80.0, bounceMiles = 20.0, actualMiles = 110.0),
            DataTrip(id = 3L, date = start, loadedMiles = 60.0, bounceMiles = 15.0, actualMiles = 90.0),
        )
        val entities = dataTrips.map {
            TripEntity(
                id = it.id,
                date = it.date,
                loadedMiles = it.loadedMiles,
                bounceMiles = it.bounceMiles,
                actualMiles = it.actualMiles,
                oorMiles = it.oorMiles,
                oorPercentage = it.oorPercentage,
            )
        }
        coEvery { dataRepository.getTripEntitiesOverlappingRange(start, any()) } returns entities

        val result = adapter.getTripStatistics(start, end)

        assertEquals(3, result.totalTrips)
        assertEquals(325.0, result.totalActualMiles, 0.01)
        assertEquals(25.0, result.totalOorMiles, 0.01)
        assertEquals(8.33, result.avgOorPercentage, 0.01)
    }

    @Test
    fun `getTripStatistics returns zeros for empty range`() = runTest {
        coEvery { dataRepository.getTripEntitiesOverlappingRange(any(), any()) } returns emptyList()

        val result = adapter.getTripStatistics(Date(), Date())

        assertEquals(0, result.totalTrips)
        assertEquals(0.0, result.totalActualMiles, 0.01)
        assertEquals(0.0, result.totalOorMiles, 0.01)
        assertEquals(0.0, result.avgOorPercentage, 0.01)
    }

    @Test
    fun `getTripStatistics proportionally splits crossing-boundary trip`() = runTest {
        val rangeStart = dateAt(2026, Calendar.MARCH, 1, 0, 0, 0)
        val rangeEnd = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val tripStart = dateAt(2026, Calendar.MARCH, 31, 23, 30, 0)
        val tripEnd = dateAt(2026, Calendar.APRIL, 1, 0, 30, 0)
        val entity = TripEntity(
            id = 99L,
            date = tripEnd,
            loadedMiles = 120.0,
            bounceMiles = 0.0,
            actualMiles = 150.0,
            oorMiles = 30.0,
            oorPercentage = 25.0,
            tripStartTime = tripStart,
            tripEndTime = tripEnd,
        )
        coEvery { dataRepository.getTripEntitiesOverlappingRange(rangeStart, any()) } returns listOf(entity)

        val result = adapter.getTripStatistics(rangeStart, rangeEnd)

        // Exactly half of a 60-minute trip overlaps March window (23:30-00:00).
        assertEquals(1, result.totalTrips)
        assertEquals(75.0, result.totalActualMiles, 0.01)
        assertEquals(15.0, result.totalOorMiles, 0.01)
        assertEquals(25.0, result.avgOorPercentage, 0.01)
    }

    @Test
    fun `getTripStatistics fully counts legacy point trip in range`() = runTest {
        val rangeStart = dateAt(2026, Calendar.MARCH, 1, 0, 0, 0)
        val rangeEnd = dateAt(2026, Calendar.MARCH, 31, 23, 59, 59)
        val pointDate = dateAt(2026, Calendar.MARCH, 15, 12, 0, 0)
        val entity = TripEntity(
            id = 100L,
            date = pointDate,
            loadedMiles = 80.0,
            bounceMiles = 20.0,
            actualMiles = 100.0,
            oorMiles = 10.0,
            oorPercentage = 10.0,
            tripStartTime = null,
            tripEndTime = null,
        )
        coEvery { dataRepository.getTripEntitiesOverlappingRange(rangeStart, any()) } returns listOf(entity)

        val result = adapter.getTripStatistics(rangeStart, rangeEnd)

        assertEquals(1, result.totalTrips)
        assertEquals(100.0, result.totalActualMiles, 0.01)
        assertEquals(10.0, result.totalOorMiles, 0.01)
        assertEquals(10.0, result.avgOorPercentage, 0.01)
    }

    // ==================== Phase 2 (T5/D1): Error paths and edge cases ====================

    @Test
    fun `getTripsByDateRange when data throws emits empty list`() = runTest {
        coEvery { dataRepository.getTripEntitiesOverlappingRange(any(), any()) } throws RuntimeException("DB error")

        val result = adapter.getTripsByDateRange(Date(), Date()).first()

        assertEquals(0, result.size)
    }

    @Test
    fun `getTripById when data throws emits null`() = runTest {
        coEvery { dataRepository.getTripById(1L) } throws RuntimeException("DB error")

        val result = adapter.getTripById("1").first()

        assertNull(result)
    }

    @Test
    fun `getTripsByStatus when data throws emits empty list`() = runTest {
        every { dataRepository.getAllTrips() } returns flow {
            throw RuntimeException("DB error")
        }

        val result = adapter.getTripsByStatus(TripStatus.COMPLETED).first()

        assertEquals(0, result.size)
    }

    @Test
    fun `getTripStatistics when data throws returns zeros`() = runTest {
        coEvery { dataRepository.getTripEntitiesOverlappingRange(any(), any()) } throws RuntimeException("DB error")

        val result = adapter.getTripStatistics(Date(), Date())

        assertEquals(0, result.totalTrips)
        assertEquals(0.0, result.totalActualMiles, 0.01)
        assertEquals(0.0, result.totalOorMiles, 0.01)
    }

    @Test
    fun `deleteTripById returns false for invalid id`() = runTest {
        val deleted = adapter.deleteTripById("invalid")
        assertEquals(false, deleted)
    }

    @Test
    fun `deleteTripById returns false for zero id`() = runTest {
        val deleted = adapter.deleteTripById("0")
        assertEquals(false, deleted)
    }

    private fun dateAt(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
    ): Date {
        return Calendar.getInstance().apply {
            set(year, month, day, hour, minute, second)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}
