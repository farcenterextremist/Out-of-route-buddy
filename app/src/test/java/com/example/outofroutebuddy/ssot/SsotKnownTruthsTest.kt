package com.example.outofroutebuddy.ssot

import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.services.UnifiedLocationService
import com.example.outofroutebuddy.services.UnifiedTripService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * SSOT Known Truths verification tests.
 *
 * Verifies behavior documented in docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md.
 * Prevents regressions when someone changes persistence, recovery, or calendar logic.
 *
 * Scenarios per docs/qa/SSOT_TEST_SCENARIOS.md:
 * - Only End trip writes to Room; Clear trip never inserts
 * - Recovery precedence: (1) Application.recoveredTripState (2) TripPersistenceManager (3) inactive
 * - TripTrackingService is source for live miles (verified by TripTrackingServiceRobolectricTest)
 * - Monthly stats from Room only (verified by repository tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SsotKnownTruthsTest {

    private lateinit var viewModel: TripInputViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SSOT - Clear trip never inserts into repository`() = runTest {
        // Known Truth: "Only End trip writes to the trip store. Clear trip never inserts."
        // Ref: docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md
        val mockRepo = mockk<TripRepository>(relaxed = true)
        coEvery { mockRepo.insertTrip(any()) } returns "would-be-id"

        viewModel = createViewModel(mockRepository = mockRepo)

        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)

        viewModel.clearTrip()
        delay(150)

        coVerify(exactly = 0) { mockRepo.insertTrip(any()) }
        assertFalse("Trip should be inactive after clear", viewModel.uiState.value.isTripActive)
    }

    @Test
    fun `SSOT - End trip inserts into repository`() = runTest(testDispatcher) {
        // Known Truth: "Only End trip writes to the trip store." ViewModel persists via TripStatePersistence.saveCompletedTrip.
        val mockRepo = mockk<TripRepository>(relaxed = true)
        coEvery { mockRepo.getMonthlyTripStatistics() } returns TripStatistics(totalTrips = 1, totalActualMiles = 125.0, totalOorMiles = 0.0, avgOorPercentage = 0.0)
        coEvery { mockRepo.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepo.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        val mockPersistence = mockk<TripStatePersistence>(relaxed = true)
        coEvery { mockPersistence.saveCompletedTrip(any(), any(), any(), any(), any()) } returns 123L

        viewModel = createViewModel(mockRepository = mockRepo, mockTripStatePersistence = mockPersistence)

        viewModel.calculateTrip(100.0, 25.0, 125.0)
        advanceUntilIdle()

        viewModel.endTrip()
        advanceUntilIdle()
        coVerify(atLeast = 1) { mockPersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
    }

    // Recovery precedence (1) recoveredTripState (2) TripPersistenceManager (3) inactive is verified by:
    // - TripPersistenceRecoveryTest (persistence load/save)
    // - TripRecoveryDialogRobolectricTest, TripRecoveryResumeRobolectricTest (crash recovery UI flow)

    private fun createViewModel(
        mockRepository: TripRepository = mockk(relaxed = true),
        mockTripStatePersistence: TripStatePersistence = mockk(relaxed = true),
        mockTripPersistenceManager: TripPersistenceManager = mockk(relaxed = true) {
            every { loadSavedTripState() } returns null
        },
        mockTripStateManager: TripStateManager = mockk(relaxed = true) {
            every { tripState } returns MutableStateFlow(TripStateManager.TripState(isActive = false))
        }
    ): TripInputViewModel {
        val mockPrefs = mockk<PreferencesManager>(relaxed = true)
        every { mockPrefs.getPeriodMode() } returns PeriodMode.STANDARD

        val mockUnifiedLocation = mockk<UnifiedLocationService>(relaxed = true)
        every { mockUnifiedLocation.realTimeGpsData } returns MutableStateFlow(
            UnifiedLocationService.RealTimeGpsData(totalDistance = 0.0, accuracy = 0.0, lastUpdate = System.currentTimeMillis())
        )

        val mockUnifiedTrip = mockk<UnifiedTripService>(relaxed = true)
        every { mockUnifiedTrip.getCurrentPeriodDates(any()) } returns Pair(Date(), Date())
        coEvery { mockUnifiedTrip.calculateTrip(any(), any(), any()) } returns UnifiedTripService.CalculationResult(
            tripId = "test",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 125.0,
            oorMiles = 0.0,
            oorPercentage = 0.0,
            calculationTime = Date(),
            isValid = true
        )

        val mockApplication = mockk<android.app.Application>(relaxed = true)

        return TripInputViewModel(
            tripRepository = mockRepository,
            preferencesManager = mockPrefs,
            tripStateManager = mockTripStateManager,
            tripStatePersistence = mockTripStatePersistence,
            tripPersistenceManager = mockTripPersistenceManager,
            backgroundSyncService = mockk(relaxed = true),
            optimizedGpsDataFlow = mockk(relaxed = true),
            validationFramework = mockk(relaxed = true),
            unifiedLocationService = mockUnifiedLocation,
            unifiedTripService = mockUnifiedTrip,
            unifiedOfflineService = mockk(relaxed = true),
            crashRecoveryManager = mockk(relaxed = true),
            application = mockApplication,
            ioDispatcher = testDispatcher
        )
    }
}
