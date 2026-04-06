package com.example.outofroutebuddy.util

/**
 * Gate for address-based arrival (geocode + geofence). We only enable that path when the user
 * likely entered a street-level line, not "city, state", empty text, or ZIP-only locality.
 */
object AddressArrivalEligibility {

    private const val MIN_LENGTH = 12

    /**
     * @return true if the string is non-empty and the first address line (before the first comma)
     *         looks like a street-level description (contains a digit, e.g. house number).
     *         This filters typical "City, ST" / "City, State" entries that have no street number.
     */
    fun isStreetLevelEnough(address: String): Boolean {
        val t = address.trim()
        if (t.length < MIN_LENGTH) return false
        val firstLine = t.substringBefore(',').trim()
        if (firstLine.isEmpty()) return false
        if (!firstLine.any { it.isDigit() }) return false
        return true
    }
}
