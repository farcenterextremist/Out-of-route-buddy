package com.example.outofroutebuddy.simulation

import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.services.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Simulation tests for Monthly Statistics / Stat Card on Calendar wiring.
 *
 * Verifies the data flow:
 * 1. User selects calendar period → periodStatistics populated
 * 2. Stats row displays period data via mapPeriodToSummary(periodStatistics)
 * 3. datesWithTripsInPeriod populated for calendar dots
 * 4. End trip → refreshStatisticsAfterSave → period + dates updated
 *
 * No instrument tests; pure unit/simulation tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatCardCalendarWiringSimulationTest {

    private lateinit var viewModel: TripInputViewModel
    private lateinit var mockRepository: TripRepository
    private lateinit var mockTripStatePersistence: TripStatePersistence
    private lateinit var mockUnifiedLocationService: UnifiedLocationService
    private lateinit var mockGpsFlow: MutableStateFlow<UnifiedLocationService.RealTimeGpsData>

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mockk(relaxed = true)
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

    @Test
    fun `SIMULATION - Period selection populates stats row and calendar dots`() = runTest {
        println("\n=== SIMULATING STAT CARD / CALENDAR WIRING ===")

        // Given: Repository has trips in March 2025
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.MARCH, 10, 10, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val tripDate1 = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 20)
        val tripDate2 = cal.time

        val trip1 = Trip(
            id = "t1",
            loadedMiles = 50.0,
            bounceMiles = 10.0,
            actualMiles = 60.0,
            oorMiles = 5.0,
            oorPercentage = 8.33,
            startTime = tripDate1,
            endTime = tripDate1,
            status = TripStatus.COMPLETED
        )
        val trip2 = Trip(
            id = "t2",
            loadedMiles = 80.0,
            bounceMiles = 20.0,
            actualMiles = 100.0,
            oorMiles = 10.0,
            oorPercentage = 10.0,
            startTime = tripDate2,
            endTime = tripDate2,
            status = TripStatus.COMPLETED
        )

        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 160.0,
            totalOorMiles = 15.0,
            avgOorPercentage = 9.17
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip1, trip2))

        println("📋 STEP 1: User selects March 2025 in calendar...")
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(periodStart, periodEnd) }
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(periodStart, periodEnd) }
        delay(100)

        val state = viewModel.uiState.value
        val summary = viewModel.mapPeriodToSummary(state.periodStatistics)

        println("   ✓ periodStatistics populated: ${state.periodStatistics != null}")
        println("   ✓ Stats row (mapPeriodToSummary): totalTrips=${summary?.totalTrips}, totalMiles=${summary?.totalMiles}, oorMiles=${summary?.oorMiles}")
        println("   ✓ datesWithTripsInPeriod: ${state.datesWithTripsInPeriod.size} days")

        assertNotNull("periodStatistics should be set", state.periodStatistics)
        assertNotNull("mapPeriodToSummary should produce stats for row", summary)
        assertEquals("Stats row totalTrips", 2, summary!!.totalTrips)
        assertEquals("Stats row totalMiles", 160.0, summary.totalMiles, 0.01)
        assertEquals("Stats row oorMiles", 15.0, summary.oorMiles, 0.01)
        assertEquals("Calendar should show 2 days with dots", 2, state.datesWithTripsInPeriod.size)

        println("\n✅ SIMULATION COMPLETE - Stat card and calendar wiring verified")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Period selection sets both period and year statistics for stats rows`() = runTest {
        // After period selection (or refreshStatisticsAfterSave), both monthly and yearly stats rows
        // must be populated so neither shows placeholder while the other has data.
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val yearStart = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val yearEnd = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(periodStart, periodEnd) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 160.0,
            totalOorMiles = 15.0,
            avgOorPercentage = 9.17
        )
        coEvery { mockRepository.getTripStatistics(yearStart, yearEnd) } returns TripStatistics(
            totalTrips = 10,
            totalActualMiles = 800.0,
            totalOorMiles = 40.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList<Trip>())

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        delay(150)

        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be set after period selection", state.periodStatistics)
        assertNotNull("yearStatistics should be set together with periodStatistics", state.yearStatistics)

        val periodSummary = viewModel.mapPeriodToSummary(state.periodStatistics)
        val yearSummary = viewModel.mapPeriodToSummary(state.yearStatistics)
        assertNotNull("mapPeriodToSummary(periodStatistics) for monthly row", periodSummary)
        assertNotNull("mapPeriodToSummary(yearStatistics) for yearly row", yearSummary)

        assertEquals("Period stats totalTrips", 2, periodSummary!!.totalTrips)
        assertEquals("Year stats totalTrips", 10, yearSummary!!.totalTrips)
        assertEquals("Period stats totalMiles", 160.0, periodSummary.totalMiles, 0.01)
        assertEquals("Year stats totalMiles", 800.0, yearSummary.totalMiles, 0.01)
    }

    @Test
    fun `SIMULATION - datesWithTripsInPeriod includes both days for midnight-spanning trip in period`() = runTest {
        // Period March 1-31; one trip March 10 23:00 -> March 11 01:00.
        // datesWithTripsInPeriod (chips / calendar dots) should list both March 10 and March 11.
        val mar10Late = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 23, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val mar11Early = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 1, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val midnightSpanningTrip = Trip(
            id = "ms-1",
            loadedMiles = 40.0,
            bounceMiles = 0.0,
            actualMiles = 40.0,
            oorMiles = 2.0,
            oorPercentage = 5.0,
            startTime = mar10Late,
            endTime = mar11Early,
            status = TripStatus.COMPLETED
        )
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 40.0,
            totalOorMiles = 2.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(midnightSpanningTrip))

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        delay(150)

        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be set", state.periodStatistics)
        assertEquals(
            "datesWithTripsInPeriod should include both days for midnight-spanning trip",
            2,
            state.datesWithTripsInPeriod.size
        )
        val dayMillis = state.datesWithTripsInPeriod.map { it.time }.toSet()
        val mar10Start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val mar11Start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 11, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertTrue("March 10 should be in datesWithTripsInPeriod", dayMillis.contains(mar10Start))
        assertTrue("March 11 should be in datesWithTripsInPeriod", dayMillis.contains(mar11Start))
    }

    @Test
    fun `SIMULATION - End trip refreshes period stats and calendar when period includes today`() = runTest {
        println("\n=== SIMULATING END TRIP → STAT REFRESH → CALENDAR UPDATE ===")

        val now = Calendar.getInstance()
        val startOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        val savedTrip = Trip(
            id = "saved-1",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 125.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(savedTrip))
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )

        println("📋 STEP 1: User selects current month...")
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startOfMonth, endOfMonth)
        delay(50)

        println("📋 STEP 2: User starts and ends a trip...")
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        viewModel.endTrip()
        delay(200)
        coVerify(atLeast = 1) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        coVerify(atLeast = 1) { mockRepository.getMonthlyTripStatistics() }
        coVerify(atLeast = 1) { mockRepository.getTripsByDateRange(any(), any()) }
        delay(100)

        val state = viewModel.uiState.value
        val summary = viewModel.mapPeriodToSummary(state.periodStatistics)

        println("   ✓ periodStatistics refreshed: ${state.periodStatistics != null}")
        println("   ✓ datesWithTripsInPeriod: ${state.datesWithTripsInPeriod.size} days")
        println("   ✓ Stats row: totalTrips=${summary?.totalTrips}")

        assertNotNull("periodStatistics should be refreshed after endTrip", state.periodStatistics)
        assertTrue("datesWithTripsInPeriod should include today", state.datesWithTripsInPeriod.isNotEmpty())
        assertNotNull("Stats row should show period data", summary)
        assertEquals("Stats row totalTrips", 1, summary!!.totalTrips)

        assertNotNull("yearStatistics should also be set after endTrip (both rows populated)", state.yearStatistics)
        val yearSummary = viewModel.mapPeriodToSummary(state.yearStatistics)
        assertNotNull("Year row should have summary", yearSummary)
        assertEquals("Year row totalTrips", 1, yearSummary!!.totalTrips)

        println("\n✅ SIMULATION COMPLETE - End trip → stat refresh → calendar update verified")
        println("=".repeat(50))
    }
}
