package com.example.outofroutebuddy.utils

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.repository.TripStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Unit tests for MockTripRepository.
 *
 * Verifies the lightweight mock behaves correctly for simulation and unit tests.
 * No database, no instrumented tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTripRepositoryTest {

    private lateinit var mock: MockTripRepository

    @Before
    fun setUp() {
        mock = MockTripRepository()
    }

    private fun trip(
        id: String = "t1",
        loadedMiles: Double = 50.0,
        bounceMiles: Double = 10.0,
        actualMiles: Double = 60.0,
        startTime: Date? = null,
        endTime: Date? = null,
        dataTier: DataTier = DataTier.GOLD,
    ) = Trip(
        id = id,
        loadedMiles = loadedMiles,
        bounceMiles = bounceMiles,
        actualMiles = actualMiles,
        oorMiles = actualMiles - (loadedMiles + bounceMiles),
        oorPercentage = 0.0,
        startTime = startTime,
        endTime = endTime,
        status = TripStatus.COMPLETED,
        dataTier = dataTier,
    )

    @Test
    fun `setTrips and getAllTrips returns configured trips`() = runTest {
        val t1 = trip("t1")
        val t2 = trip("t2", actualMiles = 70.0)
        mock.setTrips(listOf(t1, t2))

        val result = mock.getAllTrips().first()
        assertEquals(2, result.size)
        assertEquals("t1", result[0].id)
        assertEquals("t2", result[1].id)
    }

    @Test
    fun `getTripById returns trip when found`() = runTest {
        val t1 = trip("t1")
        mock.setTrips(listOf(t1))

        val result = mock.getTripById("t1").first()
        assertNotNull(result)
        assertEquals("t1", result!!.id)
    }

    @Test
    fun `getTripById returns null when not found`() = runTest {
        mock.setTrips(emptyList())
        val result = mock.getTripById("nonexistent").first()
        assertNull(result)
    }

    @Test
    fun `insertTrip adds trip and returns id`() = runTest {
        val t1 = trip("t1")
        val id = mock.insertTrip(t1)
        assertEquals("t1", id)

        val all = mock.getAllTrips().first()
        assertEquals(1, all.size)
        assertEquals("t1", all[0].id)
    }

    @Test
    fun `addTrip appends to existing trips`() = runTest {
        val t1 = trip("t1")
        val t2 = trip("t2")
        mock.setTrips(listOf(t1))
        mock.addTrip(t2)

        val result = mock.getAllTrips().first()
        assertEquals(2, result.size)
    }

    @Test
    fun `updateTrip replaces existing trip`() = runTest {
        val t1 = trip("t1", actualMiles = 60.0)
        mock.setTrips(listOf(t1))

        val updated = t1.copy(actualMiles = 65.0)
        val success = mock.updateTrip(updated)
        assertTrue(success)

        val result = mock.getTripById("t1").first()
        assertNotNull(result)
        assertEquals(65.0, result!!.actualMiles, 0.01)
    }

    @Test
    fun `deleteTrip removes trip`() = runTest {
        val t1 = trip("t1")
        mock.setTrips(listOf(t1))
        val success = mock.deleteTrip(t1)
        assertTrue(success)

        val result = mock.getAllTrips().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteTripById removes trip by id`() = runTest {
        val t1 = trip("t1")
        mock.setTrips(listOf(t1))
        val success = mock.deleteTripById("t1")
        assertTrue(success)

        val result = mock.getAllTrips().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTripsByDateRange filters by endTime`() = runTest {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.MARCH, 10, 10, 0, 0)
        val mar10 = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 20)
        val mar20 = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 25)
        val mar25 = cal.time

        val t1 = trip("t1", endTime = mar10)
        val t2 = trip("t2", endTime = mar20)
        val t3 = trip("t3", endTime = mar25)
        mock.setTrips(listOf(t1, t2, t3))

        val start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 15, 0, 0, 0)
        }.time
        val end = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 22, 23, 59, 59)
        }.time

        val result = mock.getTripsByDateRange(start, end).first()
        assertEquals(1, result.size)
        assertEquals("t2", result[0].id)
    }

    @Test
    fun `getTripsOverlappingDay includes midnight-spanning trips`() = runTest {
        val mar10Late = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 23, 0, 0)
        }.time
        val mar11Early = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 1, 0, 0)
        }.time
        val t1 = trip("t1", startTime = mar10Late, endTime = mar11Early)
        mock.setTrips(listOf(t1))

        val startOfMar11 = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 0, 0, 0)
        }.time
        val endOfMar11 = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 12, 0, 0, 0)
        }.time

        val result = mock.getTripsOverlappingDay(startOfMar11, endOfMar11).first()
        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `getTripsByStatus filters by status`() = runTest {
        val t1 = trip("t1").withStatus(TripStatus.COMPLETED)
        val t2 = trip("t2").withStatus(TripStatus.CANCELLED)
        mock.setTrips(listOf(t1, t2))

        val completed = mock.getTripsByStatus(TripStatus.COMPLETED).first()
        assertEquals(1, completed.size)
        assertEquals("t1", completed[0].id)
    }

    @Test
    fun `setMonthlyStats and getMonthlyTripStatistics return configured stats`() = runTest {
        val stats = TripStatistics(
            totalTrips = 5,
            totalActualMiles = 500.0,
            totalOorMiles = 25.0,
            avgOorPercentage = 5.0
        )
        mock.setMonthlyStats(stats)

        val result = mock.getMonthlyTripStatistics()
        assertEquals(5, result.totalTrips)
        assertEquals(500.0, result.totalActualMiles, 0.01)
        assertEquals(25.0, result.totalOorMiles, 0.01)
    }

    @Test
    fun `setTodayStats and getTodayTripStatistics return configured stats`() = runTest {
        val stats = TripStatistics(
            totalTrips = 2,
            totalActualMiles = 200.0,
            totalOorMiles = 10.0,
            avgOorPercentage = 5.0
        )
        mock.setTodayStats(stats)

        val result = mock.getTodayTripStatistics()
        assertEquals(2, result.totalTrips)
    }

    @Test
    fun `clearAllTrips empties the list`() = runTest {
        mock.setTrips(listOf(trip("t1")))
        mock.clearAllTrips()

        val result = mock.getAllTrips().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteTripsOlderThan removes trips with endTime before cutoff`() = runTest {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.JANUARY, 5, 10, 0, 0)
        val t1 = trip("t1", endTime = cal.time)
        cal.set(2025, Calendar.MARCH, 10, 10, 0, 0)
        val t2 = trip("t2", endTime = cal.time)
        mock.setTrips(listOf(t1, t2))

        val cutoff = Calendar.getInstance().apply {
            set(2025, Calendar.FEBRUARY, 1, 0, 0, 0)
        }.time
        mock.deleteTripsOlderThan(cutoff)

        val result = mock.getAllTrips().first()
        assertEquals(1, result.size)
        assertEquals("t2", result[0].id)
    }

    @Test
    fun `setShouldFail causes insertTrip to throw`() = runTest {
        mock.setShouldFail(true)
        try {
            mock.insertTrip(trip("t1"))
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertTrue(e.message!!.contains("insert failed"))
        }
    }

    @Test
    fun `setShouldFail causes updateTrip to return false`() = runTest {
        mock.setTrips(listOf(trip("t1")))
        mock.setShouldFail(true)
        val success = mock.updateTrip(trip("t1", actualMiles = 70.0))
        assertFalse(success)
    }

    @Test
    fun `setShouldFail causes deleteTrip to return false`() = runTest {
        mock.setTrips(listOf(trip("t1")))
        mock.setShouldFail(true)
        val success = mock.deleteTrip(trip("t1"))
        assertFalse(success)
    }

    // ---------- Data loop: getTripsByTier, setTripTier (tier separation) ----------

    @Test
    fun `getTripsByTier returns only trips of given tier`() = runTest {
        val t1 = trip("t1", dataTier = DataTier.GOLD)
        val t2 = trip("t2", dataTier = DataTier.PLATINUM)
        val t3 = trip("t3", dataTier = DataTier.PLATINUM)
        val t4 = trip("t4", dataTier = DataTier.SILVER)
        mock.setTrips(listOf(t1, t2, t3, t4))

        val gold = mock.getTripsByTier(DataTier.GOLD).first()
        val platinum = mock.getTripsByTier(DataTier.PLATINUM).first()
        val silver = mock.getTripsByTier(DataTier.SILVER).first()

        assertEquals(1, gold.size)
        assertEquals("t1", gold[0].id)
        assertEquals(2, platinum.size)
        assertTrue(platinum.map { it.id }.toSet() == setOf("t2", "t3"))
        assertEquals(1, silver.size)
        assertEquals("t4", silver[0].id)
    }

    @Test
    fun `setTripTier updates tier and getTripsByTier reflects it`() = runTest {
        val t1 = trip("t1", dataTier = DataTier.PLATINUM)
        mock.setTrips(listOf(t1))

        val success = mock.setTripTier("t1", DataTier.GOLD)
        assertTrue(success)

        val gold = mock.getTripsByTier(DataTier.GOLD).first()
        val platinum = mock.getTripsByTier(DataTier.PLATINUM).first()
        assertEquals(1, gold.size)
        assertEquals("t1", gold[0].id)
        assertEquals(DataTier.GOLD, gold[0].dataTier)
        assertTrue(platinum.isEmpty())
    }

    @Test
    fun `setTripTier returns false for unknown id`() = runTest {
        mock.setTrips(listOf(trip("t1")))
        val success = mock.setTripTier("nonexistent", DataTier.GOLD)
        assertFalse(success)
    }

    @Test
    fun `setShouldFail causes setTripTier to return false`() = runTest {
        mock.setTrips(listOf(trip("t1")))
        mock.setShouldFail(true)
        val success = mock.setTripTier("t1", DataTier.PLATINUM)
        assertFalse(success)
    }
}
