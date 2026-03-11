package com.example.outofroutebuddy.data.repository

import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.models.Trip
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for TripRepository to ensure proper business logic and data conversion.
 */
class TripRepositoryTest {
    private lateinit var tripDao: TripDao
    private lateinit var tripRepository: TripRepository

    @Before
    fun setUp() {
        tripDao = mockk()
        tripRepository = TripRepository(tripDao)
    }

    private fun isClose(
        a: Double,
        b: Double,
        delta: Double = 0.01,
    ): Boolean = kotlin.math.abs(a - b) < delta

    @Test
    fun `insertTrip converts Trip to TripEntity and returns id`() =
        runTest {
            // Given
            val trip =
                Trip(
                    id = 0L,
                    date = Date(),
                    loadedMiles = 100.0,
                    bounceMiles = 50.0,
                    actualMiles = 160.0,
                )
            val expectedId = 1L

            coEvery { tripDao.insertTrip(any()) } returns expectedId

            // When
            val resultId = tripRepository.insertTrip(trip)

            // Then
            assertEquals(expectedId, resultId)
            coVerify {
                tripDao.insertTrip(
                    match {
                        it.date == trip.date &&
                            it.loadedMiles == trip.loadedMiles &&
                            it.bounceMiles == trip.bounceMiles &&
                            it.actualMiles == trip.actualMiles &&
                            isClose(it.oorMiles, 10.0) &&
                            isClose(it.oorPercentage, 6.67)
                    },
                )
            }
        }

    @Test
    fun `insertTrip calculates OOR values correctly`() =
        runTest {
            // Given
            val date = Date()
            val loadedMiles = 100.0
            val bounceMiles = 50.0
            val actualMiles = 160.0
            val trip =
                Trip(
                    date = date,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            val expectedId = 1L

            coEvery { tripDao.insertTrip(any()) } returns expectedId

            // When
            val resultId = tripRepository.insertTrip(trip)

            // Then
            assertEquals(expectedId, resultId)
            coVerify {
                tripDao.insertTrip(
                    match {
                        it.date == date &&
                            it.loadedMiles == loadedMiles &&
                            it.bounceMiles == bounceMiles &&
                            it.actualMiles == actualMiles &&
                            isClose(it.oorMiles, 10.0) && // 160 - (100 + 50)
                            isClose(it.oorPercentage, 6.67) // (10 / 150) * 100
                    },
                )
            }
        }

    @Test
    fun `insertTrip handles zero dispatched miles`() =
        runTest {
            // Given
            val date = Date()
            val loadedMiles = 0.0
            val bounceMiles = 0.0
            val actualMiles = 10.0
            val trip =
                Trip(
                    date = date,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            val expectedId = 1L

            coEvery { tripDao.insertTrip(any()) } returns expectedId

            // When
            val resultId = tripRepository.insertTrip(trip)

            // Then
            assertEquals(expectedId, resultId)
            coVerify {
                tripDao.insertTrip(
                    match {
                        isClose(it.oorPercentage, 0.0) // Division by zero protection
                    },
                )
            }
        }

    @Test
    fun `getAllTrips converts entities to trips`() =
        runTest {
            // Given
            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = Date(1640995200000),
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                    TripEntity(
                        id = 2L,
                        date = Date(1641081600000),
                        loadedMiles = 200.0,
                        bounceMiles = 100.0,
                        actualMiles = 320.0,
                        oorMiles = 20.0,
                        oorPercentage = 6.67,
                    ),
                )

            every { tripDao.getAllTrips() } returns flowOf(entities)

            // When
            val trips = tripRepository.getAllTrips().first()

            // Then
            assertEquals(2, trips.size)
            assertEquals(1L, trips[0].id)
            assertEquals(Date(1640995200000), trips[0].date)
            assertEquals(100.0, trips[0].loadedMiles, 0.01)
            assertEquals(50.0, trips[0].bounceMiles, 0.01)
            assertEquals(160.0, trips[0].actualMiles, 0.01)

            assertEquals(2L, trips[1].id)
            assertEquals(Date(1641081600000), trips[1].date)
            assertEquals(200.0, trips[1].loadedMiles, 0.01)
            assertEquals(100.0, trips[1].bounceMiles, 0.01)
            assertEquals(320.0, trips[1].actualMiles, 0.01)
        }

    @Test
    fun `getTodayTrips returns trips for today`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = today,
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                )

            coEvery { tripDao.getTripsForDate(startOfDay, endOfDay) } returns entities

            // When
            val trips = tripRepository.getTodayTrips()

            // Then
            assertEquals(1, trips.size)
            assertEquals(1L, trips[0].id)
            assertEquals(today, trips[0].date)
        }

    @Test
    fun `getTripsForDateRange returns trips in range`() =
        runTest(timeout = 5.seconds) {
            // Given
            val startDate = Date(1640995200000) // 2022-01-01
            val endDate = Date(1641081600000) // 2022-01-02
            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = startDate,
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                )

            // TripDao.getTripsForDateRange is non-suspend (returns List); use every for mockk
            every { tripDao.getTripsForDateRange(startDate, endDate) } returns entities

            // When
            val trips = tripRepository.getTripsForDateRange(startDate, endDate)
            advanceUntilIdle()

            // Then
            assertEquals(1, trips.size)
            assertEquals(1L, trips[0].id)
            assertEquals(startDate, trips[0].date)
        }

    @Test
    fun `getTodayTotalMiles calculates total dispatched miles`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = today,
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                    TripEntity(
                        id = 2L,
                        date = today,
                        loadedMiles = 200.0,
                        bounceMiles = 100.0,
                        actualMiles = 320.0,
                        oorMiles = 20.0,
                        oorPercentage = 6.67,
                    ),
                )

            coEvery { tripDao.getTripsForDate(startOfDay, endOfDay) } returns entities

            // When
            val totalMiles = tripRepository.getTodayTotalMiles()

            // Then
            assertEquals(450.0, totalMiles, 0.01) // (100+50) + (200+100)
        }

    @Test
    fun `getTodayOorMiles calculates total OOR miles`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = today,
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                    TripEntity(
                        id = 2L,
                        date = today,
                        loadedMiles = 200.0,
                        bounceMiles = 100.0,
                        actualMiles = 320.0,
                        oorMiles = 20.0,
                        oorPercentage = 6.67,
                    ),
                )

            coEvery { tripDao.getTripsForDate(startOfDay, endOfDay) } returns entities

            // When
            val totalOorMiles = tripRepository.getTodayOorMiles()

            // Then
            assertEquals(30.0, totalOorMiles, 0.01) // 10 + 20
        }

    @Test
    fun `getTodayAvgOorPercentage calculates average correctly`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val entities =
                listOf(
                    TripEntity(
                        id = 1L,
                        date = today,
                        loadedMiles = 100.0,
                        bounceMiles = 50.0,
                        actualMiles = 160.0,
                        oorMiles = 10.0,
                        oorPercentage = 6.67,
                    ),
                    TripEntity(
                        id = 2L,
                        date = today,
                        loadedMiles = 200.0,
                        bounceMiles = 100.0,
                        actualMiles = 320.0,
                        oorMiles = 20.0,
                        oorPercentage = 6.67,
                    ),
                )

            coEvery { tripDao.getTripsForDate(startOfDay, endOfDay) } returns entities

            // When
            val avgPercentage = tripRepository.getTodayAvgOorPercentage()

            // Then
            assertEquals(6.67, avgPercentage, 0.01) // (6.67 + 6.67) / 2
        }

    @Test
    fun `getTodayAvgOorPercentage returns zero for empty list`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            coEvery { tripDao.getTripsForDate(startOfDay, endOfDay) } returns emptyList()

            // When
            val avgPercentage = tripRepository.getTodayAvgOorPercentage()

            // Then
            assertEquals(0.0, avgPercentage, 0.01)
        }

    @Test
    fun `clearAllTrips calls dao deleteAllTrips`() =
        runTest {
            // Given
            coEvery { tripDao.deleteAllTrips() } returns Unit

            // When
            tripRepository.clearAllTrips()

            // Then
            coVerify { tripDao.deleteAllTrips() }
        }

    @Test
    fun `getTodayTripCount returns correct count`() =
        runTest {
            // Given
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            coEvery { tripDao.getTripCountForDate(startOfDay, endOfDay) } returns 3

            // When
            val count = tripRepository.getTodayTripCount()

            // Then
            assertEquals(3, count)
        }

    @Test
    fun `insertTrip handles negative OOR miles`() =
        runTest {
            // Given
            val date = Date()
            val loadedMiles = 100.0
            val bounceMiles = 50.0
            val actualMiles = 140.0 // Less than dispatched
            val trip =
                Trip(
                    date = date,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            val expectedId = 1L

            coEvery { tripDao.insertTrip(any()) } returns expectedId

            // When
            val resultId = tripRepository.insertTrip(trip)

            // Then
            assertEquals(expectedId, resultId)
            coVerify {
                tripDao.insertTrip(
                    match {
                        isClose(it.oorMiles, -10.0) && // 140 - (100 + 50)
                            isClose(it.oorPercentage, -6.67) // (-10 / 150) * 100
                    },
                )
            }
        }

    @Test
    fun `insertTrip handles decimal precision`() =
        runTest {
            // Given
            val date = Date()
            val loadedMiles = 100.5
            val bounceMiles = 25.75
            val actualMiles = 130.0
            val trip =
                Trip(
                    date = date,
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            val expectedId = 1L

            coEvery { tripDao.insertTrip(any()) } returns expectedId

            // When
            val resultId = tripRepository.insertTrip(trip)

            // Then
            assertEquals(expectedId, resultId)
            coVerify {
                tripDao.insertTrip(
                    match {
                        isClose(it.oorMiles, 3.75) && // 130 - (100.5 + 25.75)
                            isClose(it.oorPercentage, 2.97) // (3.75 / 126.25) * 100
                    },
                )
            }
        }
} 
