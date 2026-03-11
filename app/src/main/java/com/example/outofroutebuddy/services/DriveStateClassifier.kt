package com.example.outofroutebuddy.services

import android.location.Location
import com.example.outofroutebuddy.core.config.DriveDetectConfig
import com.example.outofroutebuddy.core.config.ValidationConfig

/**
 * Result of drive-state classification: whether the user is driving (count miles)
 * or walking/stationary (do not count miles). Used to auto-exclude non-driving segments
 * so the app proactively ignores walking and micro-movement miles without requiring
 * the user to tap Pause.
 */
enum class DriveState {
    /** User is driving; accumulate distance. */
    DRIVING,

    /** User is walking or stationary; do not accumulate distance (same effect as Pause). */
    WALKING_OR_STATIONARY
}

/**
 * Classifies location updates as DRIVING vs WALKING_OR_STATIONARY for auto-excluding
 * non-driving miles. Uses strict rules and highway context: slow on interstate is
 * still DRIVING; only exclude when confident the segment is walking/stationary.
 *
 * Thresholds are in [ValidationConfig] (DRIVE_DETECT_*), or overridden by optional [config].
 *
 * @param clockMs Optional time source in ms (for tests); default is System.currentTimeMillis().
 * @param config Optional overrides from Settings (e.g. [DriveDetectConfig.from]); when null, [ValidationConfig] is used.
 */
class DriveStateClassifier(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    config: DriveDetectConfig? = null
) {
    private val walkingSpeedMph = config?.walkingSpeedMph ?: ValidationConfig.DRIVE_DETECT_WALKING_SPEED_MPH
    private val walkingMinDurationMs = config?.walkingMinDurationMs ?: ValidationConfig.DRIVE_DETECT_WALKING_MIN_DURATION_MS
    private val highwaySpeedMph = ValidationConfig.DRIVE_DETECT_HIGHWAY_SPEED_MPH
    private val highwayLookbackMs = config?.highwayLookbackMs ?: ValidationConfig.DRIVE_DETECT_HIGHWAY_LOOKBACK_MS
    private val highwayLikeMinSpeedMph = ValidationConfig.DRIVE_DETECT_HIGHWAY_LIKE_MIN_SPEED_MPH

    /** When we first entered walking-speed band (or 0 if not in band). */
    private var walkingBandEnteredAtMs: Long = 0L

    /**
     * Classifies the current location update.
     *
     * @param location Current location (with optional speed).
     * @param _lastLocation Previous location (can be null on first update). Reserved for future bearing/consistency checks.
     * @param recentHistory Optional list of recent locations (newest last) for highway context; can be null.
     * @return DRIVING to count miles, WALKING_OR_STATIONARY to exclude.
     */
    @Suppress("UNUSED_PARAMETER")
    fun classify(
        location: Location,
        _lastLocation: Location?,
        recentHistory: List<Location>?
    ): DriveState {
        val speedMph = if (location.hasSpeed()) location.speed * ValidationConfig.MPS_TO_MPH else 0f

        // When speed is unavailable and recent history is empty or has no speed, we prefer DRIVING to avoid
        // excluding real driving; no explicit UNKNOWN state is exposed to the UI.
        if (!location.hasSpeed() && (recentHistory.isNullOrEmpty() || recentHistory.none { it.hasSpeed() })) {
            return DriveState.DRIVING
        }

        val nowMs = clockMs()

        // Highway context: if we had sustained highway-like speed recently, treat current slow as driving.
        if (hasHighwayContext(recentHistory, nowMs)) {
            walkingBandEnteredAtMs = 0L
            return DriveState.DRIVING
        }

        // Walking/stationary: speed at or below walking band for long enough.
        if (speedMph <= walkingSpeedMph) {
            if (walkingBandEnteredAtMs == 0L) {
                walkingBandEnteredAtMs = nowMs
            }
            val durationInBand = nowMs - walkingBandEnteredAtMs
            if (durationInBand >= walkingMinDurationMs) {
                return DriveState.WALKING_OR_STATIONARY
            }
            // Still in band but not long enough; treat as driving to be strict.
            return DriveState.DRIVING
        }

        // Speed above walking band -> driving; reset walking band.
        walkingBandEnteredAtMs = 0L
        return DriveState.DRIVING
    }

    /**
     * Returns true if in the lookback window we have seen speed above [highwaySpeedMph]
     * or sustained speed above [highwayLikeMinSpeedMph] (highway-like context).
     */
    private fun hasHighwayContext(history: List<Location>?, nowMs: Long): Boolean {
        if (history.isNullOrEmpty()) return false
        val cutoffMs = nowMs - highwayLookbackMs
        var seenHighwaySpeed = false
        var recentSustainedCount = 0
        for (loc in history) {
            if (loc.time < cutoffMs) continue
            if (loc.hasSpeed()) {
                val mph = loc.speed * ValidationConfig.MPS_TO_MPH
                if (mph >= highwaySpeedMph) seenHighwaySpeed = true
                if (mph >= highwayLikeMinSpeedMph) recentSustainedCount++ else recentSustainedCount = 0
            } else {
                recentSustainedCount = 0
            }
        }
        return seenHighwaySpeed || recentSustainedCount >= 3
    }

    /** Call when trip ends or service resets so next trip starts with clean state. */
    fun reset() {
        walkingBandEnteredAtMs = 0L
    }
}
