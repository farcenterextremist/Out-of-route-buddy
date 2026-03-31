package com.example.outofroutebuddy.services

import android.content.Context
import com.example.outofroutebuddy.data.archive.SharedPoolExportStorage
import com.example.outofroutebuddy.di.IoDispatcher
import com.example.outofroutebuddy.domain.models.DataTier
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

data class VirtualFleetGenerationRequest(
    val managerCount: Int = 2,
    val driverCount: Int = 6,
    val tripsPerDriver: Int = 4,
    val seed: Long = 42L,
    val runLabel: String = "default",
    val referenceDatasets: List<String> = emptyList(),
)

data class VirtualFleetExportReceipt(
    val batchId: String,
    val filePath: String,
    val tripCount: Int,
)

data class VirtualFleetRunBundle(
    val metadata: VirtualFleetRunMetadata,
    val trips: List<VirtualFleetTripRecord>,
)

data class VirtualFleetRunMetadata(
    val batchId: String,
    val exportType: String,
    val exportedAt: String,
    val sourceApp: String,
    val schemaVersion: Int,
    val generatorVersion: String,
    val runLabel: String,
    val fleetManagerCount: Int,
    val driverCount: Int,
    val tripCount: Int,
    val defaultDataTier: String,
    val contaminationCheck: String,
    val referenceDatasets: List<String>,
)

data class VirtualFleetTripRecord(
    val sourceRecordId: String,
    val personaId: String,
    val personaRole: String,
    val routeLabel: String,
    val dataTier: String,
    val plannedMiles: Double,
    val actualMiles: Double,
    val oorMiles: Double,
    val oorPercentage: Double,
    val tripStartTime: String,
    val tripEndTime: String,
    val scenarioTags: List<String>,
)

@Singleton
class VirtualFleetSandboxService @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val fleetDataGenerator: VirtualFleetDataGenerator,
) {

    suspend fun generateAndExport(
        request: VirtualFleetGenerationRequest = VirtualFleetGenerationRequest(),
    ): Result<VirtualFleetExportReceipt> =
        withContext(ioDispatcher) {
            runCatching {
                val bundle = generateBundle(request)
                val exportDir = SharedPoolExportStorage.resolveExportDirectory(context)
                val exportFile = File(exportDir, "virtual_fleet_${bundle.metadata.batchId}.json")
                exportFile.writeText(Gson().toJson(bundle), Charsets.UTF_8)
                VirtualFleetExportReceipt(
                    batchId = bundle.metadata.batchId,
                    filePath = exportFile.absolutePath,
                    tripCount = bundle.trips.size,
                )
            }
        }

    fun generateBundle(
        request: VirtualFleetGenerationRequest = VirtualFleetGenerationRequest(),
    ): VirtualFleetRunBundle {
        val random = Random(request.seed)
        val batchId = "vf-${UUID.randomUUID()}"
        val exportedAt = formatIsoUtc(Date())
        val trips = mutableListOf<VirtualFleetTripRecord>()

        repeat(request.driverCount) { driverIndex ->
            val personaId = "driver-${driverIndex + 1}"
            repeat(request.tripsPerDriver) { tripIndex ->
                val plannedMiles = randomMiles(random, min = 180.0, max = 740.0)
                val deviationRatio = 0.02 + random.nextDouble() * 0.22
                val oorMiles = (plannedMiles * deviationRatio * 100.0).roundToInt() / 100.0
                val actualMiles = ((plannedMiles + oorMiles) * 100.0).roundToInt() / 100.0
                val tripStartTime = Date(System.currentTimeMillis() + ((driverIndex * 10L + tripIndex) * 3_600_000L))
                val tripEndTime = Date(tripStartTime.time + ((plannedMiles / 55.0) * 3_600_000L).toLong())
                val tier = if ((driverIndex + tripIndex) % 3 == 0) DataTier.SILVER else DataTier.PLATINUM

                trips +=
                    VirtualFleetTripRecord(
                        sourceRecordId = "$personaId-trip-${tripIndex + 1}",
                        personaId = personaId,
                        personaRole = "driver",
                        routeLabel = "synthetic-route-${driverIndex + 1}-${tripIndex + 1}",
                        dataTier = tier.name,
                        plannedMiles = plannedMiles,
                        actualMiles = actualMiles,
                        oorMiles = oorMiles,
                        oorPercentage = ((oorMiles / plannedMiles) * 100.0 * 100.0).roundToInt() / 100.0,
                        tripStartTime = formatIsoUtc(tripStartTime),
                        tripEndTime = formatIsoUtc(tripEndTime),
                        scenarioTags = buildScenarioTags(driverIndex, tripIndex),
                    )
            }
        }

        return VirtualFleetRunBundle(
            metadata = VirtualFleetRunMetadata(
                batchId = batchId,
                exportType = "virtual_fleet_trip_batch",
                exportedAt = exportedAt,
                sourceApp = "OutOfRouteBuddy",
                schemaVersion = 1,
                generatorVersion = "1",
                runLabel = request.runLabel,
                fleetManagerCount = request.managerCount,
                driverCount = request.driverCount,
                tripCount = trips.size,
                defaultDataTier = DataTier.PLATINUM.name,
                contaminationCheck = "pass: synthetic batches exported only; production Room trips untouched",
                referenceDatasets = request.referenceDatasets,
            ),
            trips = trips,
        )
    }

    /**
     * Generate and export an archetype-based fleet bundle with realistic driver personas.
     * Uses [VirtualFleetDataGenerator] for statistically grounded trip data.
     */
    suspend fun generateArchetypeFleetExport(
        days: Int = 30,
        seed: Long = 42L,
    ): Result<VirtualFleetExportReceipt> =
        withContext(ioDispatcher) {
            runCatching {
                val fleet = fleetDataGenerator.generateFleet(days = days, seed = seed)
                val trips = fleet.flatMap { (driverId, driverTrips) ->
                    driverTrips.map { trip ->
                        VirtualFleetTripRecord(
                            sourceRecordId = trip.id,
                            personaId = driverId,
                            personaRole = "driver",
                            routeLabel = "${driverId}-${trip.startTime?.time ?: 0}",
                            dataTier = trip.dataTier.name,
                            plannedMiles = trip.loadedMiles + trip.bounceMiles,
                            actualMiles = trip.actualMiles,
                            oorMiles = trip.oorMiles,
                            oorPercentage = trip.oorPercentage,
                            tripStartTime = formatIsoUtc(trip.startTime ?: Date()),
                            tripEndTime = formatIsoUtc(trip.endTime ?: Date()),
                            scenarioTags = listOf("synthetic", "archetype_fleet", driverId.substringAfter("virtual-")),
                        )
                    }
                }
                val batchId = "vf-archetype-${UUID.randomUUID()}"
                val bundle = VirtualFleetRunBundle(
                    metadata = VirtualFleetRunMetadata(
                        batchId = batchId,
                        exportType = "virtual_fleet_archetype_batch",
                        exportedAt = formatIsoUtc(Date()),
                        sourceApp = "OutOfRouteBuddy",
                        schemaVersion = 2,
                        generatorVersion = "2-archetype",
                        runLabel = "archetype-$days-days",
                        fleetManagerCount = 1,
                        driverCount = fleet.size,
                        tripCount = trips.size,
                        defaultDataTier = DataTier.PLATINUM.name,
                        contaminationCheck = "pass: archetype synthetic batches exported only; production Room trips untouched",
                        referenceDatasets = listOf("ATRI-2026", "DSP-route-data", "gig-platform-stats"),
                    ),
                    trips = trips,
                )
                val exportDir = SharedPoolExportStorage.resolveExportDirectory(context)
                val exportFile = File(exportDir, "virtual_fleet_archetype_${batchId}.json")
                exportFile.writeText(Gson().toJson(bundle), Charsets.UTF_8)
                VirtualFleetExportReceipt(
                    batchId = batchId,
                    filePath = exportFile.absolutePath,
                    tripCount = trips.size,
                )
            }
        }

    private fun randomMiles(random: Random, min: Double, max: Double): Double {
        val value = min + (random.nextDouble() * (max - min))
        return (value * 100.0).roundToInt() / 100.0
    }

    private fun buildScenarioTags(driverIndex: Int, tripIndex: Int): List<String> {
        val tags = mutableListOf("synthetic", "virtual_fleet")
        if (driverIndex % 2 == 0) tags += "experienced_driver"
        if (tripIndex % 2 == 1) tags += "night_run"
        if ((driverIndex + tripIndex) % 3 == 0) tags += "weather_reference_candidate"
        return tags
    }

    private fun formatIsoUtc(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }
}
