package com.example.outofroutebuddy.domain.ranking

import com.example.outofroutebuddy.domain.models.DriverArchetype
import com.example.outofroutebuddy.domain.models.FleetLeaderboard
import com.example.outofroutebuddy.domain.models.RankingBreakdown
import com.example.outofroutebuddy.domain.models.RankingCohort
import com.example.outofroutebuddy.domain.models.RankingScore
import com.example.outofroutebuddy.domain.models.RankingTier
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Core ranking engine that calculates driver performance scores.
 *
 * Drivers are ranked **within their experience cohort** so a new user with
 * 5 trips competes against other Newcomers, not against Legends with 100+ trips.
 *
 * Scoring formula (weighted composite 0–100):
 * - OOR Efficiency   40%  — lower average OOR% = higher score
 * - Consistency      25%  — lower std deviation of OOR% across trips = higher score
 * - Trip Volume      15%  — engagement relative to cohort ceiling
 * - GPS Quality      10%  — higher average GPS quality = more trustworthy data
 * - Route Discipline 10%  — % of trips under the OOR target threshold
 *
 * The algorithm emphasizes habitual patterns over isolated incidents,
 * consistent with S.A.F.E. fleet scoring research.
 */
@Singleton
class RankingsEngine @Inject constructor() {

    companion object {
        const val OOR_CEILING_PERCENT = 25.0
        const val OOR_DISCIPLINE_THRESHOLD = 5.0
    }

    /**
     * Build a cohort-scoped leaderboard. The user is placed into a cohort based on
     * their completed trip count, and only virtual drivers in that same cohort appear
     * on the leaderboard. This prevents new users from feeling permanently behind.
     */
    fun buildLeaderboard(
        userTrips: List<Trip>,
        virtualFleetTrips: Map<String, VirtualDriverTripBundle>,
    ): FleetLeaderboard {
        val userCompletedTrips = userTrips.filter { it.status == TripStatus.COMPLETED }
        val userCohort = RankingCohort.fromTripCount(userCompletedTrips.size)

        val allScored = mutableListOf<RankingScore>()

        if (userCompletedTrips.isNotEmpty()) {
            allScored += scoreDriver(
                driverId = "user",
                driverName = "You",
                trips = userCompletedTrips,
                archetype = null,
                isVirtual = false,
            )
        }

        for ((driverId, bundle) in virtualFleetTrips) {
            if (bundle.trips.isNotEmpty()) {
                allScored += scoreDriver(
                    driverId = driverId,
                    driverName = bundle.displayName,
                    trips = bundle.trips,
                    archetype = bundle.archetype,
                    isVirtual = true,
                )
            }
        }

        val cohortScores = allScored.filter { it.cohort == userCohort }
        val ranked = assignRanks(cohortScores)

        val nextCohort = RankingCohort.entries
            .firstOrNull { it.tripRange.first > userCohort.tripRange.last }
        val tripsUntilNext = if (nextCohort != null) {
            max(0, nextCohort.tripRange.first - userCompletedTrips.size)
        } else {
            0
        }

        return FleetLeaderboard(
            cohort = userCohort,
            rankings = ranked,
            userRanking = ranked.find { it.driverId == "user" },
            cohortSize = ranked.size,
            nextCohort = nextCohort,
            tripsUntilNextCohort = tripsUntilNext,
        )
    }

    /**
     * Score a single driver from their completed trip history.
     * Trip Volume is scaled relative to the driver's cohort ceiling.
     * Rank/percentile/tier are placeholder values until [assignRanks] runs.
     */
    fun scoreDriver(
        driverId: String,
        driverName: String,
        trips: List<Trip>,
        archetype: DriverArchetype?,
        isVirtual: Boolean,
    ): RankingScore {
        val cohort = RankingCohort.fromTripCount(trips.size)
        val oorPercents = trips.map { it.oorPercentage.coerceAtLeast(0.0) }
        val avgOor = if (oorPercents.isNotEmpty()) oorPercents.average() else 0.0

        val breakdown = RankingBreakdown(
            oorEfficiencyScore = calculateOorEfficiency(avgOor),
            consistencyScore = calculateConsistency(oorPercents),
            tripVolumeScore = calculateTripVolume(trips.size, cohort),
            gpsQualityScore = calculateGpsQuality(trips),
            routeDisciplineScore = calculateRouteDiscipline(oorPercents),
        )

        val overallScore = (
            breakdown.oorEfficiencyScore * RankingBreakdown.WEIGHT_OOR_EFFICIENCY +
                breakdown.consistencyScore * RankingBreakdown.WEIGHT_CONSISTENCY +
                breakdown.tripVolumeScore * RankingBreakdown.WEIGHT_TRIP_VOLUME +
                breakdown.gpsQualityScore * RankingBreakdown.WEIGHT_GPS_QUALITY +
                breakdown.routeDisciplineScore * RankingBreakdown.WEIGHT_ROUTE_DISCIPLINE
            ).coerceIn(0.0, 100.0)

        return RankingScore(
            driverId = driverId,
            driverName = driverName,
            archetype = archetype,
            overallScore = overallScore,
            breakdown = breakdown,
            cohort = cohort,
            tier = RankingTier.BRONZE,
            rank = 0,
            percentile = 0.0,
            totalTrips = trips.size,
            avgOorPercent = avgOor,
            isVirtual = isVirtual,
        )
    }

    private fun assignRanks(scores: List<RankingScore>): List<RankingScore> {
        val sorted = scores.sortedByDescending { it.overallScore }
        val size = sorted.size

        return sorted.mapIndexed { index, score ->
            val rank = index + 1
            val percentile = if (size > 1) {
                ((size - rank).toDouble() / (size - 1)) * 100.0
            } else {
                100.0
            }
            score.copy(
                rank = rank,
                percentile = percentile,
                tier = RankingTier.fromPercentile(percentile),
            )
        }
    }

    /**
     * OOR Efficiency: 0% OOR → 100, [OOR_CEILING_PERCENT]% OOR → 0.
     */
    private fun calculateOorEfficiency(avgOorPercent: Double): Double {
        return max(0.0, 100.0 - (avgOorPercent / OOR_CEILING_PERCENT * 100.0))
    }

    /**
     * Consistency: low standard deviation of OOR% → high score.
     * StdDev of 0 → 100, StdDev of 10+ → 0.
     */
    private fun calculateConsistency(oorPercents: List<Double>): Double {
        if (oorPercents.size < 2) return 50.0
        val mean = oorPercents.average()
        val variance = oorPercents.sumOf { (it - mean) * (it - mean) } / oorPercents.size
        val stdDev = sqrt(variance)
        return max(0.0, 100.0 - (stdDev * 10.0))
    }

    /**
     * Trip Volume: engagement score **scaled to the driver's cohort**.
     * A Newcomer with 10 trips gets full marks; a Legend needs 120.
     */
    private fun calculateTripVolume(tripCount: Int, cohort: RankingCohort): Double {
        return min(100.0, (tripCount.toDouble() / cohort.volumeFullMarks) * 100.0)
    }

    /**
     * GPS Quality: average GPS quality percentage across all trips.
     * Trips without GPS data contribute 50 (neutral).
     */
    private fun calculateGpsQuality(trips: List<Trip>): Double {
        if (trips.isEmpty()) return 0.0
        val qualities = trips.map { trip ->
            val q = trip.gpsMetadata.gpsQualityPercentage
            if (q > 0.0) q else 50.0
        }
        return qualities.average().coerceIn(0.0, 100.0)
    }

    /**
     * Route Discipline: % of trips where OOR% is under [OOR_DISCIPLINE_THRESHOLD].
     */
    private fun calculateRouteDiscipline(oorPercents: List<Double>): Double {
        if (oorPercents.isEmpty()) return 0.0
        val underThreshold = oorPercents.count { it <= OOR_DISCIPLINE_THRESHOLD }
        return (underThreshold.toDouble() / oorPercents.size) * 100.0
    }
}

/**
 * Bundle of trips for a single virtual driver, with display metadata.
 */
data class VirtualDriverTripBundle(
    val displayName: String,
    val archetype: DriverArchetype,
    val trips: List<Trip>,
)
