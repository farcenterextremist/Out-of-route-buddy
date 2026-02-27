package com.example.outofroutebuddy.presentation.ui.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
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
        viewModel = TripHistoryByDateViewModel(application, mockRepository)
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

        coEvery { mockRepository.getTripsByDateRange(startOfDay, endOfDay) } returns flowOf(
            listOf(tripOnTargetDate, tripOnDifferentDate),
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

        coEvery { mockRepository.getTripsByDateRange(startOfDay, endOfDay) } returns flowOf(emptyList())

        viewModel.loadTripsForDate(targetDate)
        advanceUntilIdle()

        assertThat(viewModel.trips.value).isEmpty()
    }
}
