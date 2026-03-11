package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.data.TripStateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Scenario tests for the trip-ended overlay alert logic.
 * Exercises TripEndedDetector.shouldShowBubble for:
 * - Under/over allotted miles (percent and tolerance)
 * - Min trip duration and min actual miles
 * - Cooldown after "No, continue trip"
 * - Inactive trip
 */
class TripEndedOverlayScenarioTest {

    private val now = System.currentTimeMillis()
    private val fiveMinutesMs = 5 * 60 * 1000L
    private val fifteenMinutesMs = 15 * 60 * 1000L

    private fun state(
        isActive: Boolean = true,
        loadedMiles: String = "10",
        bounceMiles: String = "2",
        startTime: Long = now - fiveMinutesMs - 60_000,
    ): TripStateManager.TripState = TripStateManager.TripState(
        isActive = isActive,
        loadedMiles = loadedMiles,
        bounceMiles = bounceMiles,
        startTime = Date(startTime),
    )

    private fun metrics(totalMiles: Double, oorMiles: Double = 0.0) = TripMetrics(totalMiles = totalMiles, oorMiles = oorMiles)

    @Test
    fun scenario_inactiveTrip_doesNotShowBubble() {
        val s = state(isActive = false, loadedMiles = "10", bounceMiles = "2")
        val m = metrics(12.0) // over allotted
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_underMinActualMiles_doesNotShowBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(0.3) // below default minActualMiles 0.5
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_underMinTripDuration_doesNotShowBubble() {
        val startTime = now - 60_000 // 1 minute ago
        val s = state(loadedMiles = "10", bounceMiles = "2", startTime = startTime)
        val m = metrics(12.0)
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L) // default minTripDurationMs = 5 min
        )
    }

    @Test
    fun scenario_zeroAllotted_doesNotShowBubble() {
        val s = state(loadedMiles = "0", bounceMiles = "0")
        val m = metrics(5.0)
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_noStartTime_doesNotShowBubble() {
        val s = TripStateManager.TripState(
            isActive = true,
            loadedMiles = "10",
            bounceMiles = "2",
            startTime = null,
        )
        val m = metrics(12.0)
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_at98PercentOfAllotted_showsBubble() {
        // allotted = 12, 98% = 11.76; total 11.8 meets percent threshold
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(11.8)
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_atToleranceUnderAllotted_showsBubble() {
        // allotted = 12, tolerance 0.3 => 11.7 is ok
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(11.7)
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_overAllotted_showsBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(12.5)
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_cooldownAfterUserChoseNo_doesNotShowBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(12.0)
        val userChoseNoAt = now - 60_000 // 1 min ago; cooldown default 15 min
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, userChoseNoAt)
        )
    }

    @Test
    fun scenario_afterCooldownExpired_showsBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(12.0)
        val userChoseNoAt = now - (sixteenMinutesMs())
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, userChoseNoAt)
        )
    }

    @Test
    fun scenario_exactlyAtCooldownBoundary_showsBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(12.0)
        val userChoseNoAt = now - fifteenMinutesMs
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, userChoseNoAt)
        )
    }

    @Test
    fun scenario_nonNumericMiles_doesNotShowBubble() {
        val s = state(loadedMiles = "abc", bounceMiles = "xyz")
        val m = metrics(12.0)
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_belowPercentAndTolerance_doesNotShowBubble() {
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(11.4) // below both 98% and 0.3 tolerance threshold
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L)
        )
    }

    @Test
    fun scenario_customConfig_stricterMinDuration_respectsIt() {
        val startTime = now - 3 * 60 * 1000 // 3 min ago
        val s = state(loadedMiles = "10", bounceMiles = "2", startTime = startTime)
        val m = metrics(12.0)
        val config = TripEndedDetector.Config(minTripDurationMs = 5 * 60 * 1000L)
        assertFalse(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L, config)
        )
    }

    @Test
    fun scenario_customConfig_relaxedMinMiles_showsWhenOverRelaxedMin() {
        // With minActualMiles = 0.1, 11.8 miles (98%+ of 12) should show
        val s = state(loadedMiles = "10", bounceMiles = "2")
        val m = metrics(11.8)
        val config = TripEndedDetector.Config(minActualMiles = 0.1)
        assertTrue(
            TripEndedDetector.shouldShowBubble(s, m, now, 0L, config)
        )
    }

    private fun sixteenMinutesMs() = 16 * 60 * 1000L
}
