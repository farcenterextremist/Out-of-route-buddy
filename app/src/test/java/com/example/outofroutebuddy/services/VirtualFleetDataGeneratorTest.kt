package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.models.DriverArchetype
import com.example.outofroutebuddy.domain.models.RankingCohort
import com.example.outofroutebuddy.domain.models.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VirtualFleetDataGeneratorTest {

    private lateinit var generator: VirtualFleetDataGenerator

    @Before
    fun setUp() {
        generator = VirtualFleetDataGenerator()
    }

    @Test
    fun `generateFleet produces one driver per archetype`() {
        val fleet = generator.generateFleet()

        assertEquals(
            "Should have one driver per archetype",
            DriverArchetype.entries.size,
            fleet.size,
        )
    }

    @Test
    fun `all generated trips are PLATINUM tier`() {
        val fleet = generator.generateFleet(days = 7)

        fleet.values.flatten().forEach { trip ->
            assertEquals(
                "Synthetic trips must be PLATINUM, not ${trip.dataTier}",
                DataTier.PLATINUM,
                trip.dataTier,
            )
        }
    }

    @Test
    fun `all generated trips are COMPLETED status`() {
        val fleet = generator.generateFleet(days = 7)

        fleet.values.flatten().forEach { trip ->
            assertEquals(TripStatus.COMPLETED, trip.status)
        }
    }

    @Test
    fun `deterministic seed produces identical output`() {
        val fleet1 = generator.generateFleet(days = 7, seed = 123L)
        val fleet2 = generator.generateFleet(days = 7, seed = 123L)

        assertEquals("Same seed should produce same fleet size", fleet1.size, fleet2.size)

        fleet1.forEach { (driverId, trips1) ->
            val trips2 = fleet2[driverId]!!
            assertEquals("Trip count should match for $driverId", trips1.size, trips2.size)
            trips1.zip(trips2).forEach { (t1, t2) ->
                assertEquals("Actual miles should match", t1.actualMiles, t2.actualMiles, 0.001)
                assertEquals("OOR% should match", t1.oorPercentage, t2.oorPercentage, 0.001)
            }
        }
    }

    @Test
    fun `different seeds produce different output`() {
        val fleet1 = generator.generateFleet(days = 7, seed = 1L)
        val fleet2 = generator.generateFleet(days = 7, seed = 999L)

        val allMiles1 = fleet1.values.flatten().map { it.actualMiles }
        val allMiles2 = fleet2.values.flatten().map { it.actualMiles }

        assertTrue(
            "Different seeds should produce different mile values",
            allMiles1 != allMiles2,
        )
    }

    @Test
    fun `veteran driver has low OOR percentage`() {
        val trips = generator.generateDriverTrips(
            archetype = DriverArchetype.VETERAN,
            driverId = "test-veteran",
            days = 30,
        )

        assertTrue("Veteran should have trips", trips.isNotEmpty())

        val avgOor = trips.map { it.oorPercentage }.average()
        assertTrue(
            "Veteran avg OOR ($avgOor) should be < 5%",
            avgOor < 5.0,
        )
    }

    @Test
    fun `detour king has high OOR percentage`() {
        val trips = generator.generateDriverTrips(
            archetype = DriverArchetype.DETOUR_KING,
            driverId = "test-detour",
            days = 30,
        )

        assertTrue("Detour King should have trips", trips.isNotEmpty())

        val avgOor = trips.map { it.oorPercentage }.average()
        assertTrue(
            "Detour King avg OOR ($avgOor) should be > 5%",
            avgOor > 5.0,
        )
    }

    @Test
    fun `gig runner has more trips per day than highway warrior`() {
        val gigTrips = generator.generateDriverTrips(
            archetype = DriverArchetype.GIG_RUNNER,
            driverId = "test-gig",
            days = 30,
        )
        val hwTrips = generator.generateDriverTrips(
            archetype = DriverArchetype.HIGHWAY_WARRIOR,
            driverId = "test-hw",
            days = 30,
        )

        assertTrue(
            "Gig Runner (${gigTrips.size} trips) should have more trips than Highway Warrior (${hwTrips.size})",
            gigTrips.size > hwTrips.size,
        )
    }

    @Test
    fun `highway warrior has longer planned miles than local`() {
        val hwTrips = generator.generateDriverTrips(
            archetype = DriverArchetype.HIGHWAY_WARRIOR,
            driverId = "test-hw",
            days = 14,
        )
        val localTrips = generator.generateDriverTrips(
            archetype = DriverArchetype.LOCAL,
            driverId = "test-local",
            days = 14,
        )

        val hwAvgMiles = hwTrips.map { it.actualMiles }.average()
        val localAvgMiles = localTrips.map { it.actualMiles }.average()

        assertTrue(
            "Highway Warrior avg miles ($hwAvgMiles) > Local avg miles ($localAvgMiles)",
            hwAvgMiles > localAvgMiles,
        )
    }

    @Test
    fun `generated trips have valid GPS metadata`() {
        val trips = generator.generateDriverTrips(
            archetype = DriverArchetype.EFFICIENT,
            driverId = "test-gps",
            days = 7,
        )

        trips.forEach { trip ->
            assertTrue("GPS total points > 0", trip.gpsMetadata.totalPoints > 0)
            assertTrue(
                "Valid points <= total points",
                trip.gpsMetadata.validPoints <= trip.gpsMetadata.totalPoints,
            )
            assertTrue("GPS quality >= 0", trip.gpsMetadata.gpsQualityPercentage >= 0.0)
            assertTrue("GPS quality <= 100", trip.gpsMetadata.gpsQualityPercentage <= 100.0)
        }
    }

    @Test
    fun `trips have start and end times with end after start`() {
        val fleet = generator.generateFleet(days = 7)

        fleet.values.flatten().forEach { trip ->
            assertTrue("Trip should have startTime", trip.startTime != null)
            assertTrue("Trip should have endTime", trip.endTime != null)
            assertTrue(
                "endTime should be after startTime",
                trip.endTime!!.after(trip.startTime),
            )
        }
    }

    @Test
    fun `trip OOR miles are positive`() {
        val fleet = generator.generateFleet(days = 14)

        fleet.values.flatten().forEach { trip ->
            assertTrue(
                "OOR miles (${trip.oorMiles}) should be >= 0",
                trip.oorMiles >= 0.0,
            )
        }
    }

    @Test
    fun `rookie shows improvement trend over 30 days`() {
        val trips = generator.generateDriverTrips(
            archetype = DriverArchetype.ROOKIE,
            driverId = "test-rookie",
            days = 30,
        )

        if (trips.size >= 10) {
            val firstHalf = trips.take(trips.size / 2).map { it.oorPercentage }.average()
            val secondHalf = trips.drop(trips.size / 2).map { it.oorPercentage }.average()

            assertTrue(
                "Rookie should improve: first half avg ($firstHalf) > second half avg ($secondHalf)",
                firstHalf > secondHalf,
            )
        }
    }

    // ── Cohort fleet tests ──────────────────────────────

    @Test
    fun `generateCohortFleet produces drivers for all configs`() {
        val fleet = generator.generateCohortFleet()

        assertEquals(
            "Should have one entry per cohort config",
            VirtualFleetDataGenerator.COHORT_DRIVER_CONFIGS.size,
            fleet.size,
        )
    }

    @Test
    fun `cohort fleet spans all four cohorts`() {
        val fleet = generator.generateCohortFleet()

        val cohorts = fleet.values.map { trips ->
            RankingCohort.fromTripCount(trips.size)
        }.toSet()

        assertTrue(
            "Fleet should cover NEWCOMER cohort, found: $cohorts",
            cohorts.contains(RankingCohort.NEWCOMER),
        )
        assertTrue(
            "Fleet should cover REGULAR cohort, found: $cohorts",
            cohorts.contains(RankingCohort.REGULAR),
        )
        assertTrue(
            "Fleet should cover EXPERIENCED cohort, found: $cohorts",
            cohorts.contains(RankingCohort.EXPERIENCED),
        )
        assertTrue(
            "Fleet should cover LEGEND cohort, found: $cohorts",
            cohorts.contains(RankingCohort.LEGEND),
        )
    }

    @Test
    fun `cohort fleet has all PLATINUM tier trips`() {
        val fleet = generator.generateCohortFleet()

        fleet.values.flatten().forEach { trip ->
            assertEquals(
                "Cohort fleet trips must be PLATINUM",
                DataTier.PLATINUM,
                trip.dataTier,
            )
        }
    }

    @Test
    fun `cohort fleet is deterministic with same seed`() {
        val fleet1 = generator.generateCohortFleet(seed = 77L)
        val fleet2 = generator.generateCohortFleet(seed = 77L)

        assertEquals(fleet1.size, fleet2.size)
        fleet1.forEach { (driverId, trips1) ->
            val trips2 = fleet2[driverId]!!
            assertEquals(
                "Trip count should match for $driverId",
                trips1.size,
                trips2.size,
            )
        }
    }
}
