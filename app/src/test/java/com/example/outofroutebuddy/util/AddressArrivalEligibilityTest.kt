package com.example.outofroutebuddy.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressArrivalEligibilityTest {

    @Test
    fun empty_or_short_rejected() {
        assertFalse(AddressArrivalEligibility.isStreetLevelEnough(""))
        assertFalse(AddressArrivalEligibility.isStreetLevelEnough("   "))
        assertFalse(AddressArrivalEligibility.isStreetLevelEnough("Denver, CO"))
    }

    @Test
    fun city_state_without_street_rejected() {
        assertFalse(AddressArrivalEligibility.isStreetLevelEnough("Springfield, Illinois"))
        assertFalse(AddressArrivalEligibility.isStreetLevelEnough("Los Angeles, CA 90001"))
    }

    @Test
    fun street_number_accepted() {
        assertTrue(AddressArrivalEligibility.isStreetLevelEnough("123 Main St, Springfield, IL"))
        assertTrue(AddressArrivalEligibility.isStreetLevelEnough("456 Oak Avenue, Denver, CO 80202"))
    }
}
