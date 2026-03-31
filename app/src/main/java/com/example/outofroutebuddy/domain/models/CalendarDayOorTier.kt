package com.example.outofroutebuddy.domain.models

/**
 * Calendar cell styling for days that have saved trips, based on blended OOR% for that calendar day.
 * Thresholds: ≤10% green, 10–14% yellow outline, &gt;14% red fill.
 */
enum class CalendarDayOorTier {
    GREEN,
    YELLOW_OUTLINE,
    RED,
}
