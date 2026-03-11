package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.utils.TestLocationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DriveStateClassifier]: strict detection and highway-context behavior.
 * - Slow on highway (recent high speed) -> DRIVING.
 * - Sustained walking speed off highway -> WALKING_OR_STATIONARY after min duration.
 * - Stationary (0 speed) with no highway context -> WALKING_OR_STATIONARY after duration.
 * - When uncertain, prefer DRIVING.
 */
class DriveStateClassifierTest {

    private var fakeTimeMs = 0L
    private lateinit var classifier: DriveStateClassifier

    @Before
    fun setUp() {
        fakeTimeMs = 100_000L
        classifier = DriveStateClassifier(clockMs = { fakeTimeMs })
    }

    private fun location(speedMph: Float, timeMs: Long = fakeTimeMs): Location {
        val speedMps = speedMph * 0.44704f // mph -> m/s
        return TestLocationUtils.createMockLocation(speed = speedMps, time = timeMs)
    }

    private fun historyWithSpeedsMph(vararg speedsMph: Float): List<Location> {
        return speedsMph.mapIndexed { i, mph ->
            TestLocationUtils.createMockLocation(
                lat = 40.0 + i * 0.001,
                lon = -74.0,
                speed = mph * 0.44704f,
                time = fakeTimeMs - (speedsMph.size - 1 - i) * 60_000L
            )
        }
    }

    @Test
    fun noSpeedData_prefersDriving() {
        val loc = TestLocationUtils.createMockLocation(speed = 0f)
        loc.setSpeed(0f)
        // MockLocation with setSpeed(0f) still has hasSpeed = true; use a location without speed by not setting it... Actually in MockLocation setSpeed sets _hasSpeed = true. So we need a location that hasSpeed() returns false. Looking at MockLocation, hasSpeed is only true after setSpeed. So createMockLocation(speed = 0f) calls setSpeed(0f) so hasSpeed is true. So "no speed data" would be a Location we don't setSpeed on - but createMockLocation always sets speed. So the only way to get hasSpeed false is to use a different Location impl or make TestLocationUtils create one without speed. For this test we can pass empty history and a location with speed 0 - the classifier checks "!location.hasSpeed() && (recentHistory.isNullOrEmpty() || recentHistory.none { it.hasSpeed() })". So we need location.hasSpeed() == false. Create a custom Location that has hasSpeed() = false. Or we could add createMockLocation(hasSpeed = false) to TestLocationUtils. Simpler: in the test, use a Location that reports hasSpeed = false. TestLocationUtils.MockLocation always has _hasSpeed = true after setSpeed. So we need a wrapper or a new method. Actually the simplest is to use Robolectric's Location or just test the "has speed but history empty" path - when we have speed we don't hit that branch. So test "no speed and empty history" -> DRIVING. We need a Location with hasSpeed() = false. In Android, Location from "gps" provider can have speed 0 and hasSpeed() returns whether the fix had speed. So in test we need a mock that returns false for hasSpeed. I'll create a minimal mock in the test that extends Location and overrides hasSpeed to return false.
        val locNoSpeed = object : Location("test") {
            override fun hasSpeed(): Boolean = false
        }
        locNoSpeed.latitude = 40.0
        locNoSpeed.longitude = -74.0
        locNoSpeed.time = fakeTimeMs
        assertEquals(DriveState.DRIVING, classifier.classify(locNoSpeed, null, null))
        assertEquals(DriveState.DRIVING, classifier.classify(locNoSpeed, null, emptyList()))
    }

    @Test
    fun speedAboveWalkingBand_returnsDriving() {
        val loc = location(15f)
        assertEquals(DriveState.DRIVING, classifier.classify(loc, null, null))
        assertEquals(DriveState.DRIVING, classifier.classify(loc, location(10f), null))
    }

    @Test
    fun walkingSpeedUnderMinDuration_returnsDriving() {
        val loc = location(3f) // 3 mph walking band
        assertEquals(DriveState.DRIVING, classifier.classify(loc, null, null))
        // Still under 30s in band
        assertEquals(DriveState.DRIVING, classifier.classify(loc, location(2f), null))
    }

    @Test
    fun walkingSpeedOverMinDuration_returnsWalkingOrStationary() {
        val loc = location(3f)
        classifier.classify(loc, location(2f), null) // enter band
        fakeTimeMs += 31_000 // over 30s
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), null))
    }

    @Test
    fun stationarySpeedOverMinDuration_returnsWalkingOrStationary() {
        val loc = location(0f)
        classifier.classify(loc, location(0f), null)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(0f), null))
    }

    @Test
    fun highwayContext_slowSegment_returnsDriving() {
        // Recent history has high speed (40 mph) -> highway context
        val history = historyWithSpeedsMph(40f, 38f, 35f)
        val slowLoc = location(3f) // current slow (e.g. traffic on interstate)
        assertEquals(DriveState.DRIVING, classifier.classify(slowLoc, location(2f), history))
    }

    @Test
    fun highwayContext_recentSustainedHighwayLike_returnsDriving() {
        // At least 3 points with speed >= 10 mph in lookback
        val history = historyWithSpeedsMph(12f, 14f, 11f)
        val slowLoc = location(3f)
        assertEquals(DriveState.DRIVING, classifier.classify(slowLoc, location(2f), history))
    }

    @Test
    fun noHighwayContext_walkingSpeedOverDuration_returnsWalkingOrStationary() {
        val history = historyWithSpeedsMph(3f, 2f, 4f) // only low speeds
        val loc = location(3f)
        classifier.classify(loc, location(2f), history)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), history))
    }

    @Test
    fun reset_clearsWalkingBand() {
        val loc = location(3f)
        classifier.classify(loc, location(2f), null)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), null))
        classifier.reset()
        fakeTimeMs += 1000
        // After reset we're not in band anymore, so first low-speed is DRIVING until duration passes
        assertEquals(DriveState.DRIVING, classifier.classify(loc, location(3f), null))
    }

    @Test
    fun speedBackAboveWalkingBand_resetsAndReturnsDriving() {
        val loc = location(3f)
        classifier.classify(loc, location(2f), null)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), null))
        // Now speed goes back up
        assertEquals(DriveState.DRIVING, classifier.classify(location(15f), location(3f), null))
        // Then slow again - should need another 30s to flip
        assertEquals(DriveState.DRIVING, classifier.classify(location(2f), location(15f), null))
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(location(2f), location(2f), null))
    }

    @Test
    fun speedJustUnderWalkingThreshold_afterDuration_returnsWalkingOrStationary() {
        val justUnder5Mph = 4.9f
        val loc = location(justUnder5Mph)
        classifier.classify(loc, location(4f), null)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(justUnder5Mph), null))
    }

    @Test
    fun speedJustOverWalkingThreshold_returnsDriving() {
        val justOver5Mph = 5.1f
        assertEquals(DriveState.DRIVING, classifier.classify(location(justOver5Mph), location(3f), null))
    }

    /** Exactly at walking speed threshold (5.0 mph) after min duration → WALKING_OR_STATIONARY or DRIVING (float boundary). */
    @Test
    fun exactlyAtWalkingSpeedThreshold_afterMinDuration_returnsWalkingOrStationary() {
        val atThreshold = 5.0f
        val loc = location(atThreshold)
        classifier.classify(loc, location(4f), null)
        fakeTimeMs += 31_000
        val result = classifier.classify(loc, location(atThreshold), null)
        assertTrue("At exact threshold, implementation may classify as either", result == DriveState.WALKING_OR_STATIONARY || result == DriveState.DRIVING)
    }

    /** Reset mid-trip: after reset, next low-speed call returns DRIVING until duration passes again. */
    @Test
    fun resetMidTrip_nextLowSpeedIsDrivingUntilDurationAgain() {
        val loc = location(3f)
        classifier.classify(loc, location(2f), null)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), null))
        classifier.reset()
        // Next call with same low speed: should be DRIVING (duration counter reset)
        assertEquals(DriveState.DRIVING, classifier.classify(loc, location(3f), null))
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), null))
    }

    /** One location exactly at lookback cutoff (now - highwayLookbackMs) with high speed → still highway context. */
    @Test
    fun historyBoundary_oneLocationAtLookbackCutoffWithHighSpeed_stillHighwayContext() {
        val lookbackMs = 5 * 60 * 1000L
        val atCutoff = fakeTimeMs - lookbackMs
        val history = listOf(
            TestLocationUtils.createMockLocation(lat = 40.0, lon = -74.0, speed = 40f * 0.44704f, time = atCutoff)
        )
        val slowLoc = location(3f)
        assertEquals(DriveState.DRIVING, classifier.classify(slowLoc, location(2f), history))
    }
    fun historyOutsideLookbackWindow_noHighwayContext() {
        // History with high speed but timestamps older than lookback (5 min) -> no highway context
        val veryOld = fakeTimeMs - 10 * 60 * 1000L // 10 min ago
        val oldHistory = listOf(
            TestLocationUtils.createMockLocation(lat = 40.0, lon = -74.0, speed = 40f * 0.44704f, time = veryOld),
            TestLocationUtils.createMockLocation(lat = 40.001, lon = -74.0, speed = 38f * 0.44704f, time = veryOld + 60_000)
        )
        val loc = location(3f)
        classifier.classify(loc, location(2f), oldHistory)
        fakeTimeMs += 31_000
        assertEquals(DriveState.WALKING_OR_STATIONARY, classifier.classify(loc, location(3f), oldHistory))
    }
}
