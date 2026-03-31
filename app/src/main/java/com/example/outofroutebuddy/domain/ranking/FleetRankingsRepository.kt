package com.example.outofroutebuddy.domain.ranking

import com.example.outofroutebuddy.domain.models.DriverArchetype
import com.example.outofroutebuddy.domain.models.FleetLeaderboard
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.services.CohortDriverConfig
import com.example.outofroutebuddy.services.VirtualFleetDataGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates between the real user's trip data, the virtual fleet generator,
 * and the ranking engine to produce a cohort-scoped leaderboard.
 *
 * This is the single entry point for the rankings feature. It:
 * 1. Loads the user's real GOLD-tier completed trips
 * 2. Generates a cohort-aware virtual fleet (drivers spread across all cohorts)
 * 3. Feeds both into [RankingsEngine] which filters to the user's cohort
 *
 * Virtual fleet data is generated in-memory and never persisted to Room,
 * preserving the synthetic/human data separation contract.
 */
@Singleton
class FleetRankingsRepository @Inject constructor(
    private val tripRepository: TripRepository,
    private val fleetGenerator: VirtualFleetDataGenerator,
    private val rankingsEngine: RankingsEngine,
) {
    @Volatile
    private var cachedFleet: Map<String, List<Trip>>? = null

    @Volatile
    private var cachedBundles: Map<String, VirtualDriverTripBundle>? = null

    /**
     * Build a fresh leaderboard from the user's current trips + virtual fleet.
     * The leaderboard is scoped to the user's experience cohort.
     * Emits a single [FleetLeaderboard] value.
     */
    fun getLeaderboard(): Flow<FleetLeaderboard> = flow {
        val userTrips = tripRepository.getAllTrips().first()
        val fleet = getOrGenerateFleet()
        val bundles = getOrBuildBundles(fleet)

        val leaderboard = rankingsEngine.buildLeaderboard(
            userTrips = userTrips,
            virtualFleetTrips = bundles,
        )

        emit(leaderboard)
    }

    /**
     * Force-refresh the virtual fleet (new seed).
     */
    fun refreshFleet(seed: Long = System.currentTimeMillis()) {
        cachedFleet = fleetGenerator.generateCohortFleet(seed = seed)
        cachedBundles = null
    }

    /**
     * Get the virtual fleet trip bundles for external use (e.g. export, display).
     */
    fun getVirtualFleetBundles(): Map<String, VirtualDriverTripBundle> {
        return getOrBuildBundles(getOrGenerateFleet())
    }

    /**
     * Score just the user against the current fleet without rebuilding everything.
     */
    suspend fun getUserRankingQuick(): FleetLeaderboard {
        val userTrips = tripRepository.getAllTrips().first()
        val fleet = getOrGenerateFleet()
        val bundles = getOrBuildBundles(fleet)
        return rankingsEngine.buildLeaderboard(userTrips, bundles)
    }

    private fun getOrGenerateFleet(): Map<String, List<Trip>> {
        return cachedFleet ?: fleetGenerator.generateCohortFleet().also { cachedFleet = it }
    }

    private fun getOrBuildBundles(fleet: Map<String, List<Trip>>): Map<String, VirtualDriverTripBundle> {
        return cachedBundles ?: buildBundles(fleet).also { cachedBundles = it }
    }

    private fun buildBundles(fleet: Map<String, List<Trip>>): Map<String, VirtualDriverTripBundle> {
        val configs = VirtualFleetDataGenerator.COHORT_DRIVER_CONFIGS
        return fleet.mapValues { (driverId, trips) ->
            val config = configFromDriverId(driverId, configs)
            VirtualDriverTripBundle(
                displayName = "${config.archetype.displayName} (${config.displaySuffix})",
                archetype = config.archetype,
                trips = trips,
            )
        }
    }

    private fun configFromDriverId(
        driverId: String,
        configs: List<CohortDriverConfig>,
    ): CohortDriverConfig {
        val body = driverId.removePrefix("virtual-")
        val parts = body.split("-")
        val archetypeName = parts.dropLast(1).joinToString("_").uppercase()
        val daysSuffix = parts.lastOrNull()?.removeSuffix("d")?.toIntOrNull()

        val archetype = DriverArchetype.entries.find { it.name == archetypeName }
            ?: DriverArchetype.VETERAN

        return configs.find { it.archetype == archetype && it.days == daysSuffix }
            ?: CohortDriverConfig(archetype, daysSuffix ?: 30, "")
    }
}
