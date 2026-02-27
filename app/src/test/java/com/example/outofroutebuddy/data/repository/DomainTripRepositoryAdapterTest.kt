package com.example.outofroutebuddy.data.repository

import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.models.Trip as DataTrip
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
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
        adapter = DomainTripRepositoryAdapter(dataRepository)
    }

    @Test
    fun `getTripsByDateRange maps data trips to domain and emits once`() = runTest {
        val start = Date()
        val end = Date()
        val dataTrips = listOf(
            DataTrip(id = 1L, date = start, loadedMiles = 10.0, bounceMiles = 2.0, actualMiles = 12.0),
        )
        coEvery { dataRepository.getTripsForDateRange(start, end) } returns dataTrips

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
        coEvery { dataRepository.getTripsForDateRange(any(), any()) } returns emptyList()

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
}
