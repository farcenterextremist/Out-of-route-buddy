package com.example.outofroutebuddy.data.archive

import com.example.outofroutebuddy.domain.data.SharedPoolExportReason
import com.example.outofroutebuddy.domain.models.Trip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class SharedPoolTripExportMetadata(
    val batchId: String,
    val exportType: String,
    val exportedAt: String,
    val sourceApp: String,
    val sourceTable: String,
    val schemaVersion: Int,
    val exportReason: String,
    val tripCount: Int,
    val tierSummary: Map<String, Int>,
)

data class SharedPoolTripExportRecord(
    val sourceRecordId: String,
    val humanOrigin: Boolean,
    val dataTier: String,
    val tripDate: String,
    val tripStartTime: String?,
    val tripEndTime: String?,
    val tripTimeZoneId: String?,
    val loadedMiles: Double,
    val bounceMiles: Double,
    val actualMiles: Double,
    val oorMiles: Double,
    val oorPercentage: Double,
    val totalGpsPoints: Int,
    val validGpsPoints: Int,
    val rejectedGpsPoints: Int,
    val avgGpsAccuracy: Double,
    val locationJumpsDetected: Int,
    val accuracyWarnings: Int,
    val speedAnomalies: Int,
    val interstatePercent: Double,
    val interstateMinutes: Int,
    val backRoadsPercent: Double,
    val backRoadsMinutes: Int,
    val truckStopsVisited: Int,
)

data class SharedPoolTripExportBundle(
    val metadata: SharedPoolTripExportMetadata,
    val trips: List<SharedPoolTripExportRecord>,
)

object SharedPoolTripExportBundleFactory {
    private const val SCHEMA_VERSION = 1
    private const val SOURCE_APP = "OutOfRouteBuddy"
    private const val SOURCE_TABLE = "trips"

    fun createHumanTripBundle(
        trips: List<Trip>,
        reason: SharedPoolExportReason,
    ): SharedPoolTripExportBundle {
        val exportedAt = formatIsoUtc(Date())
        val exportableTrips = trips.filter { it.dataTier.name == "GOLD" }
        val records = exportableTrips.map(::mapTripToRecord)
        val batchId = "oorb-gold-${UUID.randomUUID()}"
        val tierSummary = records.groupingBy { it.dataTier }.eachCount()

        return SharedPoolTripExportBundle(
            metadata = SharedPoolTripExportMetadata(
                batchId = batchId,
                exportType = "oorb_human_trip_export",
                exportedAt = exportedAt,
                sourceApp = SOURCE_APP,
                sourceTable = SOURCE_TABLE,
                schemaVersion = SCHEMA_VERSION,
                exportReason = reason.name,
                tripCount = records.size,
                tierSummary = tierSummary,
            ),
            trips = records,
        )
    }

    private fun mapTripToRecord(trip: Trip): SharedPoolTripExportRecord =
        SharedPoolTripExportRecord(
            sourceRecordId = trip.id,
            humanOrigin = true,
            dataTier = trip.dataTier.name,
            tripDate = formatIsoUtc(trip.startTime ?: Date()),
            tripStartTime = formatIsoUtcOrNull(trip.startTime),
            tripEndTime = formatIsoUtcOrNull(trip.endTime),
            tripTimeZoneId = trip.timeZoneId,
            loadedMiles = trip.loadedMiles,
            bounceMiles = trip.bounceMiles,
            actualMiles = trip.actualMiles,
            oorMiles = trip.oorMiles,
            oorPercentage = trip.oorPercentage,
            totalGpsPoints = trip.gpsMetadata.totalPoints,
            validGpsPoints = trip.gpsMetadata.validPoints,
            rejectedGpsPoints = trip.gpsMetadata.rejectedPoints,
            avgGpsAccuracy = trip.gpsMetadata.avgAccuracy,
            locationJumpsDetected = trip.gpsMetadata.locationJumps,
            accuracyWarnings = trip.gpsMetadata.accuracyWarnings,
            speedAnomalies = trip.gpsMetadata.speedAnomalies,
            interstatePercent = trip.gpsMetadata.interstatePercent,
            interstateMinutes = trip.gpsMetadata.interstateMinutes,
            backRoadsPercent = trip.gpsMetadata.backRoadsPercent,
            backRoadsMinutes = trip.gpsMetadata.backRoadsMinutes,
            truckStopsVisited = trip.gpsMetadata.truckStopsVisited,
        )

    private fun formatIsoUtcOrNull(date: Date?): String? = date?.let(::formatIsoUtc)

    private fun formatIsoUtc(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }
}
