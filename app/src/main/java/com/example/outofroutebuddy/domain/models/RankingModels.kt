package com.example.outofroutebuddy.domain.models

/**
 * Experience-based cohort so drivers are ranked against peers with similar
 * trip counts. A brand-new user with 3 trips never competes against a
 * veteran with 90 trips — they each see a leaderboard of similarly-active drivers.
 *
 * @property displayName user-facing label
 * @property subtitle motivational tagline shown under the cohort name
 * @property tripRange inclusive range of completed trips that map to this cohort
 * @property volumeFullMarks trip count that earns 100% on the Volume score *within* this cohort
 */
enum class RankingCohort(
    val displayName: String,
    val subtitle: String,
    val tripRange: IntRange,
    val volumeFullMarks: Int,
) {
    NEWCOMER(
        displayName = "Newcomer",
        subtitle = "Just Getting Started",
        tripRange = 1..10,
        volumeFullMarks = 10,
    ),
    REGULAR(
        displayName = "Regular",
        subtitle = "Finding Your Groove",
        tripRange = 11..30,
        volumeFullMarks = 30,
    ),
    EXPERIENCED(
        displayName = "Experienced",
        subtitle = "Road Tested",
        tripRange = 31..75,
        volumeFullMarks = 75,
    ),
    LEGEND(
        displayName = "Legend",
        subtitle = "Fleet Legend",
        tripRange = 76..Int.MAX_VALUE,
        volumeFullMarks = 120,
    ),
    ;

    companion object {
        fun fromTripCount(tripCount: Int): RankingCohort =
            entries.firstOrNull { tripCount in it.tripRange } ?: NEWCOMER
    }
}

/**
 * Performance tier for the rankings system.
 * Tiers are assigned based on percentile rank within the driver's cohort.
 *
 * Calibrated against fleet scoring research:
 * - Dispatch Driver Score tiers
 * - CSA safety measurement percentiles
 * - Industry gamification best practice (explainable in 30 seconds)
 */
enum class RankingTier(
    val displayName: String,
    val minPercentile: Double,
    val colorHex: String,
) {
    DIAMOND("Diamond", 95.0, "#B9F2FF"),
    PLATINUM("Platinum", 80.0, "#E5E4E2"),
    GOLD("Gold", 60.0, "#FFD700"),
    SILVER("Silver", 40.0, "#C0C0C0"),
    BRONZE("Bronze", 0.0, "#CD7F32"),
    ;

    companion object {
        fun fromPercentile(percentile: Double): RankingTier = when {
            percentile >= DIAMOND.minPercentile -> DIAMOND
            percentile >= PLATINUM.minPercentile -> PLATINUM
            percentile >= GOLD.minPercentile -> GOLD
            percentile >= SILVER.minPercentile -> SILVER
            else -> BRONZE
        }
    }
}

/**
 * Breakdown of the individual scoring components.
 * Each factor is scored 0–100 independently before weighting.
 */
data class RankingBreakdown(
    val oorEfficiencyScore: Double,
    val consistencyScore: Double,
    val tripVolumeScore: Double,
    val gpsQualityScore: Double,
    val routeDisciplineScore: Double,
) {
    companion object {
        const val WEIGHT_OOR_EFFICIENCY = 0.40
        const val WEIGHT_CONSISTENCY = 0.25
        const val WEIGHT_TRIP_VOLUME = 0.15
        const val WEIGHT_GPS_QUALITY = 0.10
        const val WEIGHT_ROUTE_DISCIPLINE = 0.10
    }
}

/**
 * Complete ranking result for a single driver (real or virtual).
 *
 * @property driverId identifier ("user" for the real user, persona id for virtual)
 * @property driverName display name
 * @property archetype null for real user, archetype enum for virtual drivers
 * @property overallScore weighted composite 0–100
 * @property breakdown per-factor scores before weighting
 * @property cohort the experience cohort this driver belongs to
 * @property tier assigned tier based on percentile rank *within the cohort*
 * @property rank 1-based position in the cohort leaderboard (1 = best)
 * @property percentile 0–100 percentile within the cohort (100 = best)
 * @property totalTrips how many trips contributed to this score
 * @property avgOorPercent average OOR% across all scored trips
 * @property isVirtual true for synthetic fleet drivers
 */
data class RankingScore(
    val driverId: String,
    val driverName: String,
    val archetype: DriverArchetype? = null,
    val overallScore: Double,
    val breakdown: RankingBreakdown,
    val cohort: RankingCohort,
    val tier: RankingTier,
    val rank: Int,
    val percentile: Double,
    val totalTrips: Int,
    val avgOorPercent: Double,
    val isVirtual: Boolean,
)

/**
 * Full leaderboard snapshot scoped to the user's cohort.
 *
 * @property cohort the cohort this leaderboard represents
 * @property rankings all drivers in this cohort, sorted by score descending
 * @property userRanking the user's entry (null if user has no completed trips)
 * @property cohortSize number of drivers in this cohort
 * @property nextCohort the next cohort the user will graduate into (null if already Legend)
 * @property tripsUntilNextCohort how many more trips until promotion (0 if already Legend)
 */
data class FleetLeaderboard(
    val cohort: RankingCohort,
    val rankings: List<RankingScore>,
    val userRanking: RankingScore?,
    val cohortSize: Int,
    val nextCohort: RankingCohort?,
    val tripsUntilNextCohort: Int,
    val generatedAt: Long = System.currentTimeMillis(),
) {
    val userRank: Int?
        get() = userRanking?.rank

    val userTier: RankingTier?
        get() = userRanking?.tier

    val userPercentile: Double?
        get() = userRanking?.percentile

    @Deprecated("Use cohortSize", ReplaceWith("cohortSize"))
    val fleetSize: Int
        get() = cohortSize
}
