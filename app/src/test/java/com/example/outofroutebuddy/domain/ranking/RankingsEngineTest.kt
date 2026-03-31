package com.example.outofroutebuddy.domain.ranking

import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.models.DriverArchetype
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.RankingCohort
import com.example.outofroutebuddy.domain.models.RankingTier
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class RankingsEngineTest {

    private lateinit var engine: RankingsEngine

    @Before
    fun setUp() {
        engine = RankingsEngine()
    }

    // ── Helpers ─────────────────────────────────────────

    private fun makeTrip(
        oorPercent: Double,
        actualMiles: Double = 100.0,
        gpsQuality: Double = 90.0,
    ): Trip {
        val dispatched = actualMiles / (1 + oorPercent / 100.0)
        val loaded = dispatched * 0.6
        val bounce = dispatched * 0.4
        val oorMiles = actualMiles - dispatched
        return Trip(
            id = "test-${System.nanoTime()}",
            loadedMiles = loaded,
            bounceMiles = bounce,
            actualMiles = actualMiles,
            oorMiles = oorMiles,
            oorPercentage = oorPercent,
            startTime = Date(System.currentTimeMillis() - 3_600_000),
            endTime = Date(),
            status = TripStatus.COMPLETED,
            gpsMetadata = GpsMetadata(
                totalPoints = 100,
                validPoints = (100 * gpsQuality / 100).toInt(),
                gpsQualityPercentage = gpsQuality,
            ),
            dataTier = DataTier.GOLD,
        )
    }

    private fun makeTrips(count: Int, oorPercent: Double, gpsQuality: Double = 90.0): List<Trip> =
        (1..count).map { makeTrip(oorPercent = oorPercent, gpsQuality = gpsQuality) }

    // ── Cohort assignment ────────────────────────────────

    @Test
    fun `cohort fromTripCount assigns correct brackets`() {
        assertEquals(RankingCohort.NEWCOMER, RankingCohort.fromTripCount(1))
        assertEquals(RankingCohort.NEWCOMER, RankingCohort.fromTripCount(10))
        assertEquals(RankingCohort.REGULAR, RankingCohort.fromTripCount(11))
        assertEquals(RankingCohort.REGULAR, RankingCohort.fromTripCount(30))
        assertEquals(RankingCohort.EXPERIENCED, RankingCohort.fromTripCount(31))
        assertEquals(RankingCohort.EXPERIENCED, RankingCohort.fromTripCount(75))
        assertEquals(RankingCohort.LEGEND, RankingCohort.fromTripCount(76))
        assertEquals(RankingCohort.LEGEND, RankingCohort.fromTripCount(500))
    }

    @Test
    fun `zero trips maps to NEWCOMER`() {
        assertEquals(RankingCohort.NEWCOMER, RankingCohort.fromTripCount(0))
    }

    // ── scoreDriver tests ───────────────────────────────

    @Test
    fun `low OOR driver scores higher than high OOR driver`() {
        val lowOorTrips = makeTrips(10, oorPercent = 2.0)
        val highOorTrips = makeTrips(10, oorPercent = 15.0)

        val lowScore = engine.scoreDriver("low", "Low OOR", lowOorTrips, null, false)
        val highScore = engine.scoreDriver("high", "High OOR", highOorTrips, null, false)

        assertTrue(
            "Low OOR (${lowScore.overallScore}) should beat high OOR (${highScore.overallScore})",
            lowScore.overallScore > highScore.overallScore,
        )
    }

    @Test
    fun `consistent driver scores higher than inconsistent driver`() {
        val consistentTrips = makeTrips(10, oorPercent = 5.0)
        val inconsistentTrips = (1..10).map { i ->
            makeTrip(oorPercent = if (i % 2 == 0) 1.0 else 15.0)
        }

        val consistentScore = engine.scoreDriver("c", "Consistent", consistentTrips, null, false)
        val inconsistentScore = engine.scoreDriver("i", "Inconsistent", inconsistentTrips, null, false)

        assertTrue(
            "Consistent (${consistentScore.breakdown.consistencyScore}) > " +
                "Inconsistent (${inconsistentScore.breakdown.consistencyScore})",
            consistentScore.breakdown.consistencyScore > inconsistentScore.breakdown.consistencyScore,
        )
    }

    @Test
    fun `trip volume score is relative to cohort ceiling`() {
        val newcomerTrips = makeTrips(8, oorPercent = 5.0)
        val legendTrips = makeTrips(80, oorPercent = 5.0)

        val newcomerScore = engine.scoreDriver("n", "Newcomer", newcomerTrips, null, false)
        val legendScore = engine.scoreDriver("l", "Legend", legendTrips, null, false)

        assertTrue(
            "Newcomer with 8/10 (${newcomerScore.breakdown.tripVolumeScore}) should have higher " +
                "volume score than Legend with 80/120 (${legendScore.breakdown.tripVolumeScore})",
            newcomerScore.breakdown.tripVolumeScore > legendScore.breakdown.tripVolumeScore,
        )
    }

    @Test
    fun `score is between 0 and 100`() {
        val trips = makeTrips(10, oorPercent = 5.0)
        val score = engine.scoreDriver("test", "Test", trips, null, false)

        assertTrue("Score should be >= 0", score.overallScore >= 0.0)
        assertTrue("Score should be <= 100", score.overallScore <= 100.0)
    }

    @Test
    fun `perfect driver gets near-perfect score`() {
        val perfectTrips = makeTrips(10, oorPercent = 0.5, gpsQuality = 98.0)
        val score = engine.scoreDriver("perfect", "Perfect", perfectTrips, null, false)

        assertTrue(
            "Near-perfect OOR with full volume should score > 90, got ${score.overallScore}",
            score.overallScore > 90.0,
        )
    }

    @Test
    fun `scoreDriver assigns correct cohort`() {
        val score5 = engine.scoreDriver("a", "A", makeTrips(5, 3.0), null, false)
        val score20 = engine.scoreDriver("b", "B", makeTrips(20, 3.0), null, false)
        val score50 = engine.scoreDriver("c", "C", makeTrips(50, 3.0), null, false)
        val score100 = engine.scoreDriver("d", "D", makeTrips(100, 3.0), null, false)

        assertEquals(RankingCohort.NEWCOMER, score5.cohort)
        assertEquals(RankingCohort.REGULAR, score20.cohort)
        assertEquals(RankingCohort.EXPERIENCED, score50.cohort)
        assertEquals(RankingCohort.LEGEND, score100.cohort)
    }

    // ── buildLeaderboard cohort-scoped tests ────────────

    @Test
    fun `leaderboard only contains drivers from user cohort`() {
        val userTrips = makeTrips(5, oorPercent = 5.0)

        val virtualFleet = mapOf(
            "same-cohort" to VirtualDriverTripBundle(
                "Same Cohort",
                DriverArchetype.ROOKIE,
                makeTrips(7, oorPercent = 8.0),
            ),
            "different-cohort" to VirtualDriverTripBundle(
                "Different Cohort",
                DriverArchetype.VETERAN,
                makeTrips(50, oorPercent = 2.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(userTrips, virtualFleet)

        assertEquals(RankingCohort.NEWCOMER, leaderboard.cohort)
        assertEquals(
            "Leaderboard should only contain user + same-cohort driver",
            2,
            leaderboard.cohortSize,
        )
        assertTrue(
            "All rankings should be NEWCOMER",
            leaderboard.rankings.all { it.cohort == RankingCohort.NEWCOMER },
        )
    }

    @Test
    fun `leaderboard ranks drivers by score descending within cohort`() {
        val userTrips = makeTrips(8, oorPercent = 5.0)
        val virtualFleet = mapOf(
            "v-good" to VirtualDriverTripBundle(
                "Good",
                DriverArchetype.EFFICIENT,
                makeTrips(6, oorPercent = 1.0),
            ),
            "v-bad" to VirtualDriverTripBundle(
                "Bad",
                DriverArchetype.DETOUR_KING,
                makeTrips(9, oorPercent = 18.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(userTrips, virtualFleet)

        assertEquals(3, leaderboard.cohortSize)
        assertEquals(1, leaderboard.rankings[0].rank)
        assertEquals(3, leaderboard.rankings[2].rank)
        assertTrue(
            "Rank 1 score >= Rank 2 score",
            leaderboard.rankings[0].overallScore >= leaderboard.rankings[1].overallScore,
        )
    }

    @Test
    fun `user ranking is found in leaderboard`() {
        val userTrips = makeTrips(8, oorPercent = 3.0)
        val virtualFleet = mapOf(
            "v1" to VirtualDriverTripBundle(
                "Virtual",
                DriverArchetype.VETERAN,
                makeTrips(6, oorPercent = 2.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(userTrips, virtualFleet)

        assertNotNull("User should be in leaderboard", leaderboard.userRanking)
        assertEquals("user", leaderboard.userRanking!!.driverId)
        assertEquals(RankingCohort.NEWCOMER, leaderboard.userRanking!!.cohort)
    }

    @Test
    fun `empty user trips produces leaderboard without user`() {
        val virtualFleet = mapOf(
            "v1" to VirtualDriverTripBundle(
                "Virtual",
                DriverArchetype.VETERAN,
                makeTrips(5, oorPercent = 2.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(emptyList(), virtualFleet)

        assertNull("No user trips = no user in leaderboard", leaderboard.userRanking)
        assertEquals(RankingCohort.NEWCOMER, leaderboard.cohort)
    }

    @Test
    fun `tier assignment follows percentile rules`() {
        assertEquals(RankingTier.DIAMOND, RankingTier.fromPercentile(95.0))
        assertEquals(RankingTier.DIAMOND, RankingTier.fromPercentile(100.0))
        assertEquals(RankingTier.PLATINUM, RankingTier.fromPercentile(80.0))
        assertEquals(RankingTier.PLATINUM, RankingTier.fromPercentile(94.9))
        assertEquals(RankingTier.GOLD, RankingTier.fromPercentile(60.0))
        assertEquals(RankingTier.SILVER, RankingTier.fromPercentile(40.0))
        assertEquals(RankingTier.BRONZE, RankingTier.fromPercentile(0.0))
        assertEquals(RankingTier.BRONZE, RankingTier.fromPercentile(39.9))
    }

    @Test
    fun `non-completed trips are filtered before scoring`() {
        val mixedTrips = listOf(
            makeTrip(oorPercent = 2.0),
            makeTrip(oorPercent = 2.0).copy(status = TripStatus.ACTIVE),
            makeTrip(oorPercent = 2.0).copy(status = TripStatus.CANCELLED),
        )

        val virtualFleet = mapOf(
            "v1" to VirtualDriverTripBundle(
                "Virtual",
                DriverArchetype.VETERAN,
                makeTrips(5, oorPercent = 2.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(mixedTrips, virtualFleet)
        val userRank = leaderboard.userRanking

        assertNotNull(userRank)
        assertEquals("Only 1 completed trip counted", 1, userRank!!.totalTrips)
    }

    @Test
    fun `leaderboard reports next cohort and trips until promotion`() {
        val userTrips = makeTrips(8, oorPercent = 5.0)
        val virtualFleet = mapOf(
            "v1" to VirtualDriverTripBundle(
                "Virtual",
                DriverArchetype.ROOKIE,
                makeTrips(6, oorPercent = 8.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(userTrips, virtualFleet)

        assertEquals(RankingCohort.NEWCOMER, leaderboard.cohort)
        assertEquals(RankingCohort.REGULAR, leaderboard.nextCohort)
        assertEquals(3, leaderboard.tripsUntilNextCohort)
    }

    @Test
    fun `legend cohort has no next cohort`() {
        val userTrips = makeTrips(80, oorPercent = 3.0)
        val virtualFleet = mapOf(
            "v1" to VirtualDriverTripBundle(
                "Virtual",
                DriverArchetype.EFFICIENT,
                makeTrips(90, oorPercent = 1.0),
            ),
        )

        val leaderboard = engine.buildLeaderboard(userTrips, virtualFleet)

        assertEquals(RankingCohort.LEGEND, leaderboard.cohort)
        assertNull(leaderboard.nextCohort)
        assertEquals(0, leaderboard.tripsUntilNextCohort)
    }
}
