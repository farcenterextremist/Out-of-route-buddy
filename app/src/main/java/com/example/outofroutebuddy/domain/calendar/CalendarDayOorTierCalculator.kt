package com.example.outofroutebuddy.domain.calendar

import com.example.outofroutebuddy.domain.models.CalendarDayOorTier
import com.example.outofroutebuddy.domain.models.Trip
import java.util.Calendar

/**
 * Blends OOR per calendar day for trip history calendar markers.
 *
 * For each start-of-day that overlaps a trip (same midnight-spanning rules as trip dots),
 * sums [Trip.oorMiles] and [Trip.dispatchedMiles] across all trips touching that day, then:
 * - **≤10%** → [CalendarDayOorTier.GREEN]
 * - **>10% and ≤14%** → [CalendarDayOorTier.YELLOW_OUTLINE]
 * - **>14%** → [CalendarDayOorTier.RED]
 *
 * If total dispatched miles for the day is 0, tier is GREEN.
 */
object CalendarDayOorTierCalculator {

    fun forEachTripCalendarDayMillis(trip: Trip, block: (Long) -> Unit) {
        val start = trip.startTime ?: trip.endTime
        val end = trip.endTime ?: trip.startTime
        if (start == null) return
        val cal = Calendar.getInstance()
        cal.time = start
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startDayMillis = cal.timeInMillis
        if (end != null && end.time > start.time) {
            val endCal = Calendar.getInstance()
            endCal.time = end
            endCal.set(Calendar.HOUR_OF_DAY, 0)
            endCal.set(Calendar.MINUTE, 0)
            endCal.set(Calendar.SECOND, 0)
            endCal.set(Calendar.MILLISECOND, 0)
            val iter = Calendar.getInstance().apply { timeInMillis = startDayMillis }
            while (iter.timeInMillis <= endCal.timeInMillis) {
                block(iter.timeInMillis)
                iter.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            block(startDayMillis)
        }
    }

    /** All start-of-day millis that have at least one trip (sorted). */
    fun datesWithTripsMillis(trips: List<Trip>): List<Long> {
        val all = mutableSetOf<Long>()
        for (trip in trips) {
            forEachTripCalendarDayMillis(trip) { all.add(it) }
        }
        return all.sorted()
    }

    fun blendedOorTiersByDay(trips: List<Trip>): Map<Long, CalendarDayOorTier> {
        val oorSum = mutableMapOf<Long, Double>()
        val dispSum = mutableMapOf<Long, Double>()
        for (trip in trips) {
            forEachTripCalendarDayMillis(trip) { dayMillis ->
                oorSum[dayMillis] = (oorSum[dayMillis] ?: 0.0) + trip.oorMiles
                dispSum[dayMillis] = (dispSum[dayMillis] ?: 0.0) + trip.dispatchedMiles
            }
        }
        return oorSum.keys.associateWith { dayMillis ->
            tierForBlended(dayMillis, oorSum, dispSum)
        }
    }

    /**
     * Blended OOR totals → tier. Uses cross-multiplication so 10% / 14% boundaries are stable in double math.
     */
    fun tierForBlendedTotals(totalOor: Double, totalDispatched: Double): CalendarDayOorTier {
        if (totalDispatched <= 0.0) return CalendarDayOorTier.GREEN
        val oorTimes100 = totalOor * 100.0
        if (!oorTimes100.isFinite() || !totalDispatched.isFinite()) return CalendarDayOorTier.GREEN
        val atMost10Percent = oorTimes100 <= totalDispatched * 10.0
        val atMost14Percent = oorTimes100 <= totalDispatched * 14.0
        return when {
            atMost10Percent -> CalendarDayOorTier.GREEN
            atMost14Percent -> CalendarDayOorTier.YELLOW_OUTLINE
            else -> CalendarDayOorTier.RED
        }
    }

    private fun tierForBlended(
        dayMillis: Long,
        oorSum: Map<Long, Double>,
        dispSum: Map<Long, Double>,
    ): CalendarDayOorTier {
        val totalDisp = dispSum[dayMillis] ?: 0.0
        val totalOor = oorSum[dayMillis] ?: 0.0
        return tierForBlendedTotals(totalOor, totalDisp)
    }

    /** Single-trip OOR% tier (same thresholds as blended day logic). */
    fun oorTierForTrip(trip: Trip): CalendarDayOorTier =
        tierForBlendedTotals(trip.oorMiles, trip.dispatchedMiles)

    /** Blended OOR for a list of trips (e.g. all trips shown for one calendar day in history dialog). */
    fun blendedTierForTripList(trips: List<Trip>): CalendarDayOorTier {
        if (trips.isEmpty()) return CalendarDayOorTier.GREEN
        return tierForBlendedTotals(
            trips.sumOf { it.oorMiles },
            trips.sumOf { it.dispatchedMiles },
        )
    }
}
