package com.example.outofroutebuddy.presentation.ui.history

import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus

/**
 * Heuristics for highlighting calendar stat cards that may need a second look.
 * Not a judgment of correctness — only "unusual" patterns (very short/long duration,
 * missing times, validation issues, thin GPS coverage).
 */
object TripStatCardReviewFlags {

    private const val ABNORMALLY_SHORT_MAX_MINUTES = 9
    private const val ABNORMALLY_LONG_MINUTES = 14 * 60 // 14 hours
    private const val MIN_POINTS_FOR_GPS_RATIO = 10
    private const val MIN_VALID_POINT_RATIO_PERCENT = 30

    fun shouldFlagForReview(trip: Trip): Boolean {
        if (trip.validationIssues.isNotEmpty()) return true

        if (trip.status == TripStatus.COMPLETED) {
            if (trip.startTime == null || trip.endTime == null) return true
        }

        val durationMin = effectiveDurationMinutes(trip)
        if (trip.status == TripStatus.COMPLETED && trip.startTime != null && trip.endTime != null) {
            if (durationMin == 0) return true
            if (durationMin in 1..ABNORMALLY_SHORT_MAX_MINUTES) return true
            if (durationMin >= ABNORMALLY_LONG_MINUTES) return true
        }

        val gps = trip.gpsMetadata
        if (gps.totalPoints >= MIN_POINTS_FOR_GPS_RATIO) {
            val ratioPercent = (gps.validPoints * 100.0) / gps.totalPoints
            if (ratioPercent < MIN_VALID_POINT_RATIO_PERCENT) return true
        }
        if (gps.totalPoints >= 5 && gps.gpsQualityPercentage > 0 && gps.gpsQualityPercentage < 40.0) {
            return true
        }

        return false
    }

    private fun effectiveDurationMinutes(trip: Trip): Int {
        val fromGps = trip.gpsMetadata.tripDurationMinutes
        if (fromGps > 0) return fromGps
        return trip.durationMinutes
    }
}
