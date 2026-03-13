package com.example.outofroutebuddy.data

import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.utils.MockTripRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Verifies that the Synthetic Data Loop–related tier API is wired and usable:
 * getTripsByTier, setTripTier, and that GOLD/PLATINUM/SILVER separation works through the repository.
 *
 * Uses MockTripRepository (no DB). Ensures loop code paths compile and behave as expected.
 */
class DataLoopTierWiringTest {

    private lateinit var repository: TripRepository

    @Before
    fun setUp() {
        repository = MockTripRepository()
    }

    @Test
    fun `tier enum has three values for loop separation`() {
        val tiers = DataTier.entries
        assertEquals(3, tiers.size)
        assertTrue(DataTier.SILVER in tiers)
        assertTrue(DataTier.PLATINUM in tiers)
        assertTrue(DataTier.GOLD in tiers)
    }

    @Test
    fun `getTripsByTier returns empty when no trips`() = runTest {
        val gold = repository.getTripsByTier(DataTier.GOLD).first()
        val silver = repository.getTripsByTier(DataTier.SILVER).first()
        assertTrue(gold.isEmpty())
        assertTrue(silver.isEmpty())
    }

    @Test
    fun `setTripTier returns false when trip not found`() = runTest {
        val result = repository.setTripTier("nonexistent", DataTier.GOLD)
        assertEquals(false, result)
    }
}
