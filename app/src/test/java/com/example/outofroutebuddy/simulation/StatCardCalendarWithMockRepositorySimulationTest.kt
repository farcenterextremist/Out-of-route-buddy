package com.example.outofroutebuddy.simulation

import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.services.*
import com.example.outofroutebuddy.utils.MockTripRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Simulation tests using MockTripRepository (lightweight mock) instead of MockK.
 *
 * Verifies the same Stat Card / Calendar wiring as StatCardCalendarWiringSimulationTest,
 * but uses the in-memory MockTripRepository for a simpler, no-MockK dependency.
 *
 * Demonstrates: lightweight mocks for simulation tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatCardCalendarWithMockRepositorySimulationTest {

    private lateinit var viewModel: TripInputViewModel
    private lateinit var mockRepository: MockTripRepository
    private lateinit var mockTripStatePersistence: TripStatePersistence
    private lateinit var mockUnifiedLocationService: UnifiedLocationService
    private lateinit var mockGpsFlow: MutableStateFlow<UnifiedLocationService.RealTimeGpsData>

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = MockTripRepository()
        val mockPreferencesManager = mockk<PreferencesManager>(relaxed = true)
        val mockTripStateManager = mockk<TripStateManager>(relaxed = true)
        mockTripStatePersistence = mockk<TripStatePersistence>(relaxed = true)
        mockUnifiedLocationService = mockk(relaxed = true)

        mockGpsFlow = MutableStateFlow(
            UnifiedLocationService.RealTimeGpsData(
                totalDistance = 0.0,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
        )
        every { mockUnifiedLocationService.realTimeGpsData } returns mockGpsFlow
        every { mockTripStateManager.tripState } returns MutableStateFlow(TripStateManager.TripState(isActive = false))
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.STANDARD

        coEvery { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) } returns 1L
        val mockUnifiedTripService = mockk<UnifiedTripService>(relaxed = true)
        coEvery { mockUnifiedTripService.calculateTrip(any(), any(), any()) } returns UnifiedTripService.CalculationResult(
            tripId = "sim-trip-1",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 125.0,
            oorMiles = 0.0,
            oorPercentage = 0.0,
            calculationTime = Date(),
            isValid = true
        )
        every { mockUnifiedTripService.getCurrentPeriodDates(any()) } returns Pair(Date(), Date())

        val mockApplication = mockk<OutOfRouteApplication>(relaxed = true)
        every { (mockApplication as OutOfRouteApplication).isHealthy() } returns true

        mockkObject(TripTrackingService.Companion)
        val mockTripMetricsFlow = MutableStateFlow(TripMetrics(0.0, 0.0))
        every { TripTrackingService.tripMetrics } returns mockTripMetricsFlow
        every { TripTrackingService.driveState } returns MutableStateFlow(DriveState.DRIVING)
        every { TripTrackingService.startService(any(), any(), any()) } just Runs
        every { TripTrackingService.stopService(any()) } just Runs
        every { TripTrackingService.canShowTripNotifications(any()) } returns true
        every { TripTrackingService.pauseService(any()) } just Runs
        every { TripTrackingService.resumeService(any()) } just Runs

        viewModel = TripInputViewModel(
            tripRepository = mockRepository,
            preferencesManager = mockPreferencesManager,
            tripStateManager = mockTripStateManager,
            tripStatePersistence = mockTripStatePersistence,
            backgroundSyncService = mockk(relaxed = true),
            optimizedGpsDataFlow = mockk(relaxed = true),
            validationFramework = mockk(relaxed = true),
            unifiedLocationService = mockUnifiedLocationService,
            unifiedTripService = mockUnifiedTripService,
            unifiedOfflineService = mockk(relaxed = true),
            crashRecoveryManager = mockk(relaxed = true),
            tripPersistenceManager = mockk(relaxed = true),
            tripBackupManager = mockk(relaxed = true),
            settingsManager = mockk(relaxed = true),
            application = mockApplication,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkObject(TripTrackingService.Companion)
        Dispatchers.resetMain()
    }

    private fun trip(
        id: String,
        loadedMiles: Double = 50.0,
        bounceMiles: Double = 10.0,
        actualMiles: Double = 60.0,
        startTime: Date,
        endTime: Date,
    ) = Trip(
        id = id,
        loadedMiles = loadedMiles,
        bounceMiles = bounceMiles,
        actualMiles = actualMiles,
        oorMiles = actualMiles - (loadedMiles + bounceMiles),
        oorPercentage = 0.0,
        startTime = startTime,
        endTime = endTime,
        status = TripStatus.COMPLETED
    )

    @Test
    fun `SIMULATION - MockTripRepository period selection populates stats row and calendar dots`() = runTest {
        println("\n=== SIMULATING STAT CARD / CALENDAR WITH MOCK REPOSITORY ===")

        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.MARCH, 10, 10, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val tripDate1 = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 20)
        val tripDate2 = cal.time

        val trip1 = trip("t1", 50.0, 10.0, 60.0, tripDate1, tripDate1)
        val trip2 = trip("t2", 80.0, 20.0, 100.0, tripDate2, tripDate2)

        mockRepository.setTrips(listOf(trip1, trip2))
        mockRepository.setPeriodStats(
            TripStatistics(
                totalTrips = 2,
                totalActualMiles = 160.0,
                totalOorMiles = 15.0,
                avgOorPercentage = 9.17
            )
        )
        mockRepository.setYearStats(
            TripStatistics(
                totalTrips = 10,
                totalActualMiles = 800.0,
                totalOorMiles = 40.0,
                avgOorPercentage = 5.0
            )
        )

        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        println("📋 STEP 1: User selects March 2025 in calendar...")
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        delay(150)

        val state = viewModel.uiState.value
        val summary = viewModel.mapPeriodToSummary(state.periodStatistics)

        println("   ✓ periodStatistics populated: ${state.periodStatistics != null}")
        println("   ✓ Stats row: totalTrips=${summary?.totalTrips}, totalMiles=${summary?.totalMiles}")
        println("   ✓ datesWithTripsInPeriod: ${state.datesWithTripsInPeriod.size} days")

        assertNotNull("periodStatistics should be set", state.periodStatistics)
        assertNotNull("mapPeriodToSummary should produce stats for row", summary)
        assertEquals("Stats row totalTrips", 2, summary!!.totalTrips)
        assertEquals("Stats row totalMiles", 160.0, summary.totalMiles, 0.01)
        assertEquals("Calendar should show 2 days with dots", 2, state.datesWithTripsInPeriod.size)

        println("\n✅ SIMULATION COMPLETE - MockTripRepository stat card wiring verified")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - MockTripRepository period and year stats both populated`() = runTest {
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        mockRepository.setTrips(emptyList())
        mockRepository.setPeriodStats(
            TripStatistics(
                totalTrips = 2,
                totalActualMiles = 160.0,
                totalOorMiles = 15.0,
                avgOorPercentage = 9.17
            )
        )
        mockRepository.setYearStats(
            TripStatistics(
                totalTrips = 10,
                totalActualMiles = 800.0,
                totalOorMiles = 40.0,
                avgOorPercentage = 5.0
            )
        )

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        delay(150)

        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be set", state.periodStatistics)
        assertNotNull("yearStatistics should be set", state.yearStatistics)

        val periodSummary = viewModel.mapPeriodToSummary(state.periodStatistics)
        val yearSummary = viewModel.mapPeriodToSummary(state.yearStatistics)
        assertEquals("Period stats totalTrips", 2, periodSummary!!.totalTrips)
        assertEquals("Year stats totalTrips", 10, yearSummary!!.totalTrips)
    }
}
