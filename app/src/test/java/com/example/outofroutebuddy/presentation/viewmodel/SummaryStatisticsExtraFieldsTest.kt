package com.example.outofroutebuddy.presentation.viewmodel

import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel.SummaryStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SummaryStatistics] extraFields.
 * Ensures stat cards remain extensible via extraFields for future metrics.
 */
class SummaryStatisticsExtraFieldsTest {

    @Test
    fun summaryStatistics_hasDefaultEmptyExtraFields() {
        val stats = SummaryStatistics(
            totalTrips = 1,
            totalMiles = 10.0,
            oorMiles = 2.0,
            oorPercentage = 20.0,
        )
        assertTrue(stats.extraFields.isEmpty())
    }

    @Test
    fun summaryStatistics_acceptsExtraFields() {
        val extra = mapOf("Avg trip (hr)" to "1.5", "Fuel cost" to "25.00")
        val stats = SummaryStatistics(
            totalTrips = 2,
            totalMiles = 100.0,
            oorMiles = 10.0,
            oorPercentage = 10.0,
            extraFields = extra,
        )
        assertEquals(extra, stats.extraFields)
    }
}
