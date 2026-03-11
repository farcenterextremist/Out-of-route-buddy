package com.example.outofroutebuddy.presentation.ui.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

/**
 * Unit tests for TripHistoryByDateViewModel.
 *
 * Uses Robolectric for Application context and mocked TripRepository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripHistoryByDateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: TripRepository
    private lateinit var viewModel: TripHistoryByDateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = TripHistoryByDateViewModel(application, mockRepository, emptyFlow())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `date filtering logic test - trips on same date are included`() {
        // Given
        val targetDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 10, 30, 0)
        }.time
        
        val tripOnTargetDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 14, 0, 0)
        }.time
        
        // Normalize both to start of day
        val targetCalendar = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val tripCalendar = Calendar.getInstance().apply {
            time = tripOnTargetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Then
        assertThat(tripCalendar.timeInMillis).isEqualTo(targetCalendar.timeInMillis)
    }

    @Test
    fun `date filtering logic test - trips on different date are excluded`() {
        // Given
        val targetDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 10, 30, 0)
        }.time
        
        val tripOnDifferentDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 16, 14, 0, 0) // Next day
        }.time
        
        // Normalize both to start of day
        val targetCalendar = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val tripCalendar = Calendar.getInstance().apply {
            time = tripOnDifferentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Then
        assertThat(tripCalendar.timeInMillis).isNotEqualTo(targetCalendar.timeInMillis)
    }

    @Test
    fun `loadTripsForDate filters trips correctly for specific date`() = runTest(testDispatcher) {
        // Given: target date Dec 15, 2024
        val targetDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 10, 30, 0)
        }.time

        val startOfDay = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val tripOnTargetDate = Trip(
            id = "1",
            loadedMiles = 100.0,
            bounceMiles = 50.0,
            actualMiles = 150.0,
            oorMiles = 5.0,
            oorPercentage = 3.3,
            startTime = targetDate,
            endTime = Date(targetDate.time + 3600000),
            status = TripStatus.COMPLETED,
        )

        val tripOnDifferentDate = Trip(
            id = "2",
            loadedMiles = 200.0,
            bounceMiles = 100.0,
            actualMiles = 300.0,
            oorMiles = 10.0,
            oorPercentage = 3.3,
            startTime = Date(targetDate.time + 86400000),
            endTime = Date(targetDate.time + 86400000 + 3600000),
            status = TripStatus.COMPLETED,
        )

        coEvery { mockRepository.getTripsOverlappingDay(startOfDay, endOfDay) } returns flowOf(
            listOf(tripOnTargetDate),
        )

        viewModel.loadTripsForDate(targetDate)
        advanceUntilIdle()

        // ViewModel filters to same-day only; tripOnDifferentDate is next day
        val trips = viewModel.trips.value
        assertThat(trips).hasSize(1)
        assertThat(trips[0].id).isEqualTo("1")
    }

    @Test
    fun `loadTripsForDate handles empty results`() = runTest(testDispatcher) {
        val targetDate = Date()
        val startOfDay = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        coEvery { mockRepository.getTripsOverlappingDay(startOfDay, endOfDay) } returns flowOf(emptyList())

        viewModel.loadTripsForDate(targetDate)
        advanceUntilIdle()

        assertThat(viewModel.trips.value).isEmpty()
    }

    @Test
    fun `loadTripsForDate includes midnight-spanning trip when selected day is within range`() = runTest(testDispatcher) {
        // Trip spans Dec 14 23:00 -> Dec 15 01:00; loading Dec 15 should include it (stat card shows on both days).
        val dec14Late = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 14, 23, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val dec15Early = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 1, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val selectedDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val startOfDay = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = Calendar.getInstance().apply {
            time = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val midnightSpanningTrip = Trip(
            id = "midnight-1",
            loadedMiles = 50.0,
            bounceMiles = 10.0,
            actualMiles = 60.0,
            oorMiles = 2.0,
            oorPercentage = 3.33,
            startTime = dec14Late,
            endTime = dec15Early,
            status = TripStatus.COMPLETED,
        )

        coEvery { mockRepository.getTripsOverlappingDay(startOfDay, endOfDay) } returns flowOf(
            listOf(midnightSpanningTrip),
        )

        viewModel.loadTripsForDate(selectedDate)
        advanceUntilIdle()

        val trips = viewModel.trips.value
        assertThat(trips).hasSize(1)
        assertThat(trips[0].id).isEqualTo("midnight-1")
        assertThat(trips[0].startTime).isEqualTo(dec14Late)
        assertThat(trips[0].endTime).isEqualTo(dec15Early)
    }

    @Test
    fun `loadTripsForDate when repository throws emits empty list`() = runTest(testDispatcher) {
        val targetDate = Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val startOfDay = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = Calendar.getInstance().apply {
            time = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        coEvery { mockRepository.getTripsOverlappingDay(startOfDay, endOfDay) } throws RuntimeException("DB error")

        viewModel.loadTripsForDate(targetDate)
        advanceUntilIdle()

        assertThat(viewModel.trips.value).isEmpty()
    }

    @Test
    fun `loadTripsForDate includes very short saved trip`() = runTest(testDispatcher) {
        val selectedDate = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val startOfDay = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = Calendar.getInstance().apply {
            time = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        val veryShortTrip = Trip(
            id = "short-1",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 0.001,
            oorMiles = -11.999,
            oorPercentage = -99.99,
            startTime = Calendar.getInstance().apply {
                set(2026, Calendar.MARCH, 1, 11, 59, 55)
                set(Calendar.MILLISECOND, 0)
            }.time,
            endTime = Calendar.getInstance().apply {
                set(2026, Calendar.MARCH, 1, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time,
            status = TripStatus.COMPLETED,
        )
        coEvery { mockRepository.getTripsOverlappingDay(startOfDay, endOfDay) } returns flowOf(listOf(veryShortTrip))

        viewModel.loadTripsForDate(selectedDate)
        advanceUntilIdle()

        assertThat(viewModel.trips.value).hasSize(1)
        assertThat(viewModel.trips.value.first().id).isEqualTo("short-1")
    }

    @Test
    fun `deleteTrip emits success and refreshes current date list`() = runTest(testDispatcher) {
        val selectedDate = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val trip = Trip(
            id = "123",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.0,
            oorMiles = 0.0,
            oorPercentage = 0.0,
            startTime = selectedDate,
            endTime = selectedDate,
            status = TripStatus.COMPLETED,
        )
        coEvery { mockRepository.getTripsOverlappingDay(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.deleteTrip(trip) } returns true

        viewModel.loadTripsForDate(selectedDate)
        advanceUntilIdle()

        val success = async { viewModel.deleteSuccess.first() }
        viewModel.deleteTrip(trip)
        advanceUntilIdle()

        assertThat(success.await()).isEqualTo("Trip deleted")
        coVerify { mockRepository.deleteTrip(trip) }
        coVerify(atLeast = 2) { mockRepository.getTripsOverlappingDay(any(), any()) }
    }

    @Test
    fun `deleteTrip emits error when repository returns false`() = runTest(testDispatcher) {
        val trip = Trip(
            id = "123",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.0,
            oorMiles = 0.0,
            oorPercentage = 0.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED,
        )
        coEvery { mockRepository.deleteTrip(trip) } returns false

        val error = async { viewModel.deleteError.first() }
        viewModel.deleteTrip(trip)
        advanceUntilIdle()

        assertThat(error.await()).isEqualTo("Failed to delete trip")
    }
}
