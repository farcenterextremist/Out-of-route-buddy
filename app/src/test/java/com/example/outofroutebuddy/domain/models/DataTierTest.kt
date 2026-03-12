package com.example.outofroutebuddy.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for [DataTier] enum.
 * Verifies tier order (used by retention: GOLD last so never auto-terminated) and values.
 */
class DataTierTest {

    @Test
    fun values_hasExactlyThreeTiers() {
        val values = DataTier.entries
        assertEquals(3, values.size)
        assertEquals(DataTier.SILVER, values[0])
        assertEquals(DataTier.PLATINUM, values[1])
        assertEquals(DataTier.GOLD, values[2])
    }

    @Test
    fun gold_isLastSoNeverAutoTerminated() {
        val last = DataTier.entries.last()
        assertSame(DataTier.GOLD, last)
    }

    @Test
    fun names_matchExpected() {
        assertEquals("SILVER", DataTier.SILVER.name)
        assertEquals("PLATINUM", DataTier.PLATINUM.name)
        assertEquals("GOLD", DataTier.GOLD.name)
    }
}
