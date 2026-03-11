package com.example.outofroutebuddy.presentation.viewmodel

import com.example.outofroutebuddy.domain.data.TripArchiveService
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DataManagementViewModel]: delete-from-device (keep on server) flow.
 * Verifies export-before-delete and result emission.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataManagementViewModelTest {

    private lateinit var viewModel: DataManagementViewModel
    private lateinit var mockRepository: TripRepository
    private lateinit var mockArchiveService: TripArchiveService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockArchiveService = mockk(relaxed = true)

        viewModel = DataManagementViewModel(
            tripRepository = mockRepository,
            tripArchiveService = mockArchiveService,
            ioDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun deleteOldDataFromDevice_exportsThenDeletesAndEmitsSuccess() = runTest(testDispatcher) {
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockArchiveService.exportBeforeLocalDelete(any()) } returns Result.success(Unit)

        val results = mutableListOf<DataManagementViewModel.DataManagementResult>()
        val collectJob = launch {
            viewModel.results.collect { results.add(it) }
        }

        viewModel.deleteOldDataFromDevice(12)
        advanceUntilIdle()

        coVerify { mockRepository.getTripsByDateRange(any(), any()) }
        coVerify { mockArchiveService.exportBeforeLocalDelete(any()) }
        coVerify { mockRepository.deleteTripsOlderThan(any()) }
        assertTrue(results.size >= 1)
        assertTrue(results.last() is DataManagementViewModel.DataManagementResult.Success)
        collectJob.cancel()
        advanceUntilIdle()
    }

    @Test
    fun deleteOldDataFromDevice_whenExportFails_doesNotDeleteAndEmitsError() = runTest(testDispatcher) {
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockArchiveService.exportBeforeLocalDelete(any()) } returns Result.failure(RuntimeException("Network error"))

        val results = mutableListOf<DataManagementViewModel.DataManagementResult>()
        val collectJob = launch {
            viewModel.results.collect { results.add(it) }
        }
        try {
            viewModel.deleteOldDataFromDevice(12)
            advanceUntilIdle()

            coVerify(exactly = 0) { mockRepository.deleteTripsOlderThan(any()) }
            assertTrue(results.any { it is DataManagementViewModel.DataManagementResult.Error })
            val errorResult = results.lastOrNull() as? DataManagementViewModel.DataManagementResult.Error
            assertTrue(errorResult != null && (errorResult.message.contains("Network error") || errorResult.message.contains("Export failed")))
        } finally {
            collectJob.cancel()
            advanceUntilIdle()
        }
    }

    @Test
    fun clearAllDataFromDevice_exportsThenClearsAllAndEmitsSuccess() = runTest(testDispatcher) {
        coEvery { mockRepository.getAllTrips() } returns flowOf(emptyList())
        coEvery { mockArchiveService.exportBeforeLocalDelete(any()) } returns Result.success(Unit)

        val results = mutableListOf<DataManagementViewModel.DataManagementResult>()
        val collectJob = launch {
            viewModel.results.collect { results.add(it) }
        }

        viewModel.clearAllDataFromDevice()
        advanceUntilIdle()

        coVerify { mockRepository.getAllTrips() }
        coVerify { mockArchiveService.exportBeforeLocalDelete(any()) }
        coVerify { mockRepository.clearAllTrips() }
        assertTrue(results.any { it is DataManagementViewModel.DataManagementResult.Success })
        collectJob.cancel()
    }

    @Test
    fun clearAllDataFromDevice_whenExportFails_doesNotClearAndEmitsError() = runTest(testDispatcher) {
        coEvery { mockRepository.getAllTrips() } returns flowOf(emptyList())
        coEvery { mockArchiveService.exportBeforeLocalDelete(any()) } returns Result.failure(RuntimeException("Upload failed"))

        val results = mutableListOf<DataManagementViewModel.DataManagementResult>()
        val collectJob = launch {
            viewModel.results.collect { results.add(it) }
        }

        viewModel.clearAllDataFromDevice()
        advanceUntilIdle()

        coVerify(exactly = 0) { mockRepository.clearAllTrips() }
        assertTrue(results.any { it is DataManagementViewModel.DataManagementResult.Error })
        collectJob.cancel()
        advanceUntilIdle()
    }
}
