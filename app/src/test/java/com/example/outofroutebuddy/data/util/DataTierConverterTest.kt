package com.example.outofroutebuddy.data.util

import com.example.outofroutebuddy.domain.models.DataTier
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DataTierConverter].
 * Ensures Room persistence of DataTier enum is correct and null/unknown default to GOLD (per DATA_TIERS).
 */
class DataTierConverterTest {

    private val converter = DataTierConverter()

    @Test
    fun fromString_SILVER_returnsSilver() {
        assertEquals(DataTier.SILVER, converter.fromString("SILVER"))
    }

    @Test
    fun fromString_PLATINUM_returnsPlatinum() {
        assertEquals(DataTier.PLATINUM, converter.fromString("PLATINUM"))
    }

    @Test
    fun fromString_GOLD_returnsGold() {
        assertEquals(DataTier.GOLD, converter.fromString("GOLD"))
    }

    @Test
    fun fromString_null_returnsGoldDefault() {
        assertEquals(DataTier.GOLD, converter.fromString(null))
    }

    @Test
    fun fromString_empty_returnsGoldDefault() {
        assertEquals(DataTier.GOLD, converter.fromString(""))
    }

    @Test
    fun fromString_unknown_returnsGoldDefault() {
        assertEquals(DataTier.GOLD, converter.fromString("UNKNOWN"))
    }

    @Test
    fun toString_silver_returnsSILVER() {
        assertEquals("SILVER", converter.toString(DataTier.SILVER))
    }

    @Test
    fun toString_platinum_returnsPLATINUM() {
        assertEquals("PLATINUM", converter.toString(DataTier.PLATINUM))
    }

    @Test
    fun toString_gold_returnsGOLD() {
        assertEquals("GOLD", converter.toString(DataTier.GOLD))
    }

    @Test
    fun roundTrip_preservesTier() {
        DataTier.entries.forEach { tier ->
            assertEquals(tier, converter.fromString(converter.toString(tier)))
        }
    }
}
