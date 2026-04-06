package com.example.outofroutebuddy.domain.calendar

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import java.util.Date
import kotlin.math.max
import kotlin.math.min

/**
 * Returns a copy of this trip with loaded/bounce/actual/OOR miles scaled by the fraction of
 * trip **clock time** overlapping `[dayStart, dayEndExclusive)` (half-open day window).
 *
 * Preserves [Trip.id], [Trip.startTime], and [Trip.endTime] so trip details and delete still
 * refer to the full stored trip. Split is **time-proportional** (uniform speed assumption), not
 * per GPS segment.
 */
fun Trip.scaledToCalendarDay(dayStart: Date, dayEndExclusive: Date): Trip {
    val tripStart = startTime ?: return this
    val tripEnd = endTime

    val (ratio, overlapMillis) =
        if (tripEnd == null || tripEnd.time <= tripStart.time) {
            val inDay = tripStart.time in dayStart.time until dayEndExclusive.time
            Pair(if (inDay) 1.0 else 0.0, 0L)
        } else {
            val overlapStart = max(tripStart.time, dayStart.time)
            val overlapEnd = min(tripEnd.time, dayEndExclusive.time)
            val oMillis = (overlapEnd - overlapStart).coerceAtLeast(0L)
            val tripDurationMillis = (tripEnd.time - tripStart.time).coerceAtLeast(1L)
            val r = (oMillis.toDouble() / tripDurationMillis.toDouble()).coerceIn(0.0, 1.0)
            Pair(r, oMillis)
        }

    if (ratio >= 1.0 - 1e-9) return this
    if (ratio <= 0.0) return this

    return copy(
        loadedMiles = loadedMiles * ratio,
        bounceMiles = bounceMiles * ratio,
        actualMiles = actualMiles * ratio,
        oorMiles = oorMiles * ratio,
        oorPercentage = oorPercentage,
        gpsMetadata =
            gpsMetadata.copy(
                tripDurationMinutes = (overlapMillis / 60_000L).toInt().coerceAtLeast(0),
                // Full-trip GPS path stats stay unchanged for the slice (same underlying trip).
                interstatePercent = gpsMetadata.interstatePercent * ratio,
                interstateMinutes = (gpsMetadata.interstateMinutes.toDouble() * ratio).toInt(),
                backRoadsPercent = gpsMetadata.backRoadsPercent * ratio,
                backRoadsMinutes = (gpsMetadata.backRoadsMinutes.toDouble() * ratio).toInt(),
            ),
        isProportionalDaySlice = true,
        status = TripStatus.COMPLETED,
    )
}
