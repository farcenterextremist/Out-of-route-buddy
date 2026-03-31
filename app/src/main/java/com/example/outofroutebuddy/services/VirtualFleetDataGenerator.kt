package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.models.DriverArchetype
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Config for a single virtual driver within the cohort fleet.
 *
 * @property archetype the behavioral archetype
 * @property days how many days of trip history to generate (controls trip count)
 * @property displaySuffix human-readable suffix like "Week 1" or "Full Month"
 */
data class CohortDriverConfig(
    val archetype: DriverArchetype,
    val days: Int,
    val displaySuffix: String,
)

/**
 * Generates realistic synthetic trip data for virtual fleet drivers.
 *
 * Each driver archetype produces trips with statistically distinct patterns:
 * - Mile ranges, OOR distributions, GPS quality, trip timing
 * - Day-of-week variation (weekday vs weekend), time-of-day patterns
 * - Improvement trends for learning drivers (Rookie, Gig Runner)
 * - Natural noise so no two drivers of the same archetype are identical
 *
 * All generated trips are marked [DataTier.PLATINUM] to maintain
 * verifiable separation from human GOLD data.
 */
@Singleton
class VirtualFleetDataGenerator @Inject constructor() {

    companion object {
        const val DEFAULT_DAYS = 30
        const val DEFAULT_SEED = 42L
        private val US_TIME_ZONES = listOf(
            "America/New_York",
            "America/Chicago",
            "America/Denver",
            "America/Los_Angeles",
        )

        /**
         * Pre-defined driver configs that spread virtual drivers across all four
         * cohorts. Each entry is (archetype, days of history, display-name suffix).
         * The days value combined with the archetype's avgTripsPerDay produces a
         * trip count that lands in the target cohort.
         *
         * Cohort targets:
         * - Newcomer  (1-10 trips):  short histories, low-volume archetypes
         * - Regular   (11-30 trips): moderate histories
         * - Experienced (31-75):     full-month histories, higher-volume archetypes
         * - Legend     (76+):        extended histories or high-volume archetypes
         */
        val COHORT_DRIVER_CONFIGS: List<CohortDriverConfig> = listOf(
            // ── Newcomer cohort ─────────────────────────────
            CohortDriverConfig(DriverArchetype.ROOKIE, 5, "Week 1"),
            CohortDriverConfig(DriverArchetype.LOCAL, 2, "Day 2"),
            CohortDriverConfig(DriverArchetype.GIG_RUNNER, 1, "First Day"),

            // ── Regular cohort ──────────────────────────────
            CohortDriverConfig(DriverArchetype.ROOKIE, 15, "2 Weeks In"),
            CohortDriverConfig(DriverArchetype.NIGHT_OWL, 15, "Settling In"),
            CohortDriverConfig(DriverArchetype.HIGHWAY_WARRIOR, 30, "Month 1"),
            CohortDriverConfig(DriverArchetype.DETOUR_KING, 20, "3 Weeks"),

            // ── Experienced cohort ──────────────────────────
            CohortDriverConfig(DriverArchetype.VETERAN, 30, "Month 1"),
            CohortDriverConfig(DriverArchetype.EFFICIENT, 30, "Month 1"),
            CohortDriverConfig(DriverArchetype.LOCAL, 14, "2 Weeks"),
            CohortDriverConfig(DriverArchetype.GIG_RUNNER, 10, "10 Days"),

            // ── Legend cohort ────────────────────────────────
            CohortDriverConfig(DriverArchetype.GIG_RUNNER, 20, "3 Weeks"),
            CohortDriverConfig(DriverArchetype.LOCAL, 30, "Full Month"),
            CohortDriverConfig(DriverArchetype.EFFICIENT, 60, "2 Months"),
        )
    }

    /**
     * Generate a full virtual fleet: one driver per archetype, each with
     * [days] worth of realistic trip history.
     *
     * @return map of driverId → list of domain [Trip] objects
     */
    fun generateFleet(
        days: Int = DEFAULT_DAYS,
        seed: Long = DEFAULT_SEED,
    ): Map<String, List<Trip>> {
        val fleet = mutableMapOf<String, List<Trip>>()

        DriverArchetype.entries.forEachIndexed { index, archetype ->
            val driverId = "virtual-${archetype.name.lowercase()}"
            val driverSeed = seed + index * 1000L
            fleet[driverId] = generateDriverTrips(
                archetype = archetype,
                driverId = driverId,
                days = days,
                random = Random(driverSeed),
                timeZone = US_TIME_ZONES[index % US_TIME_ZONES.size],
            )
        }

        return fleet
    }

    /**
     * Generate a cohort-aware fleet with virtual drivers spread across all four
     * experience cohorts (Newcomer, Regular, Experienced, Legend).
     *
     * This is the preferred method for the rankings system — it ensures every
     * user, regardless of trip count, has meaningful competitors nearby.
     *
     * @return map of driverId → list of domain [Trip] objects
     */
    fun generateCohortFleet(
        seed: Long = DEFAULT_SEED,
    ): Map<String, List<Trip>> {
        val fleet = mutableMapOf<String, List<Trip>>()

        COHORT_DRIVER_CONFIGS.forEachIndexed { index, config ->
            val suffix = config.archetype.name.lowercase()
            val driverId = "virtual-${suffix}-${config.days}d"
            val driverSeed = seed + index * 1337L
            fleet[driverId] = generateDriverTrips(
                archetype = config.archetype,
                driverId = driverId,
                days = config.days,
                random = Random(driverSeed),
                timeZone = US_TIME_ZONES[index % US_TIME_ZONES.size],
            )
        }

        return fleet
    }

    /**
     * Generate [days] worth of trips for a single driver archetype.
     */
    fun generateDriverTrips(
        archetype: DriverArchetype,
        driverId: String,
        days: Int = DEFAULT_DAYS,
        random: Random = Random(),
        timeZone: String = "America/Chicago",
    ): List<Trip> {
        val trips = mutableListOf<Trip>()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        calendar.add(Calendar.DAY_OF_YEAR, -days)

        for (dayOffset in 0 until days) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val isWeekend = calendar.get(Calendar.DAY_OF_WEEK) in listOf(
                Calendar.SATURDAY,
                Calendar.SUNDAY,
            )

            val tripsToday = decideTripsForDay(archetype, isWeekend, random)

            for (tripIndex in 0 until tripsToday) {
                val dayProgress = dayOffset.toDouble() / days
                val trip = generateSingleTrip(
                    archetype = archetype,
                    driverId = driverId,
                    calendar = calendar.clone() as Calendar,
                    tripIndex = tripIndex,
                    dayProgress = dayProgress,
                    random = random,
                    timeZone = timeZone,
                    isNightRun = random.nextDouble() < archetype.nightRunProbability,
                )
                trips += trip
            }
        }

        return trips
    }

    private fun decideTripsForDay(
        archetype: DriverArchetype,
        isWeekend: Boolean,
        random: Random,
    ): Int {
        val base = archetype.avgTripsPerDay
        val weekendFactor = if (isWeekend) 0.6 else 1.0
        val noise = 0.7 + random.nextDouble() * 0.6

        val expected = base * weekendFactor * noise

        var count = expected.toInt()
        if (random.nextDouble() < (expected - count)) count++

        if (random.nextDouble() < 0.08) return 0

        return max(0, count)
    }

    private fun generateSingleTrip(
        archetype: DriverArchetype,
        driverId: String,
        calendar: Calendar,
        tripIndex: Int,
        dayProgress: Double,
        random: Random,
        timeZone: String,
        isNightRun: Boolean,
    ): Trip {
        val plannedMiles = randomInRange(
            random,
            archetype.minPlannedMiles,
            archetype.maxPlannedMiles,
        )

        val loadedMiles = plannedMiles * (0.55 + random.nextDouble() * 0.35)
        val bounceMiles = plannedMiles - loadedMiles

        val baseOorPercent = randomGaussian(
            random,
            mean = archetype.midOorPercent,
            stdDev = archetype.oorStdDev,
        ).coerceIn(archetype.minOorPercent, archetype.maxOorPercent)

        val improvementAdjustment = archetype.improvementTrend * dayProgress * 30.0
        val oorPercent = max(0.3, baseOorPercent + improvementAdjustment)

        val oorMiles = plannedMiles * (oorPercent / 100.0)
        val actualMiles = plannedMiles + oorMiles

        val startHour = if (isNightRun) {
            20 + random.nextInt(4)
        } else {
            6 + random.nextInt(10) + tripIndex * 3
        }
        calendar.set(Calendar.HOUR_OF_DAY, startHour.coerceIn(0, 23))
        calendar.set(Calendar.MINUTE, random.nextInt(60))
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.time

        val drivingHours = actualMiles / archetype.avgSpeedMph
        val breakMinutes = (random.nextInt(30) + 10).toLong()
        val durationMs = ((drivingHours * 60 + breakMinutes) * 60 * 1000).toLong()
        val endTime = Date(startTime.time + durationMs)
        val durationMinutes = (durationMs / 60_000).toInt()

        val gpsQuality = randomInRange(
            random,
            archetype.gpsQualityRange.start,
            archetype.gpsQualityRange.endInclusive,
        )
        val totalGpsPoints = (durationMinutes * 6 * (0.8 + random.nextDouble() * 0.4)).toInt()
        val validPoints = (totalGpsPoints * gpsQuality / 100.0).roundToInt()

        val gpsMetadata = GpsMetadata(
            totalPoints = totalGpsPoints,
            validPoints = validPoints,
            rejectedPoints = totalGpsPoints - validPoints,
            avgAccuracy = randomInRange(random, 3.0, 25.0),
            maxSpeed = archetype.avgSpeedMph + random.nextDouble() * 20.0,
            locationJumps = random.nextInt(5),
            accuracyWarnings = random.nextInt(3),
            speedAnomalies = random.nextInt(2),
            tripDurationMinutes = durationMinutes,
            satelliteCount = 6 + random.nextInt(10),
            gpsQualityPercentage = gpsQuality,
            interstatePercent = if (archetype == DriverArchetype.HIGHWAY_WARRIOR) {
                randomInRange(random, 60.0, 90.0)
            } else {
                randomInRange(random, 10.0, 50.0)
            },
            interstateMinutes = (durationMinutes * 0.4).toInt(),
            backRoadsPercent = if (archetype == DriverArchetype.LOCAL) {
                randomInRange(random, 30.0, 60.0)
            } else {
                randomInRange(random, 5.0, 25.0)
            },
            backRoadsMinutes = (durationMinutes * 0.15).toInt(),
            truckStopsVisited = if (archetype == DriverArchetype.DETOUR_KING) {
                1 + random.nextInt(3)
            } else {
                random.nextInt(2)
            },
        )

        val tripId = "$driverId-day${(dayProgress * 30).toInt()}-trip$tripIndex"

        return Trip(
            id = tripId,
            loadedMiles = roundTo2(loadedMiles),
            bounceMiles = roundTo2(bounceMiles),
            actualMiles = roundTo2(actualMiles),
            oorMiles = roundTo2(oorMiles),
            oorPercentage = roundTo2(oorPercent),
            startTime = startTime,
            endTime = endTime,
            timeZoneId = timeZone,
            status = TripStatus.COMPLETED,
            gpsMetadata = gpsMetadata,
            dataTier = DataTier.PLATINUM,
        )
    }

    private fun randomInRange(random: Random, min: Double, max: Double): Double {
        return min + random.nextDouble() * (max - min)
    }

    private fun randomGaussian(random: Random, mean: Double, stdDev: Double): Double {
        return mean + random.nextGaussian() * stdDev
    }

    private fun roundTo2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
