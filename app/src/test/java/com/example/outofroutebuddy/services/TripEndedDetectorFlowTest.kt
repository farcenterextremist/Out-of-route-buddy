package com.example.outofroutebuddy.services

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TripEndedDetectorFlowTest {

    @Test
    fun computeEndConfidence_isHigh_whenStillNearOriginAndLegacyTriggered() {
        val score = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 1.0,
                speedMph = 0.8,
                rollingDistanceMeters = 30.0,
                headingVarianceDeg = 3.0,
                sustainedStopMillis = 180_000L,
                lowMotionSampleCount = 5,
                isNearOrigin = true,
                originDwellMillis = 180_000L,
                isWalkingOrStill = true,
                stillConfidence = 90,
                inVehicleConfidence = 5,
                gpsAccuracyMeters = 12f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = true,
            ),
        )

        assertThat(score).isAtLeast(0.8)
    }

    @Test
    fun computeEndConfidence_isLow_whenDrivingSignalStrong() {
        val score = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 1.0,
                speedMph = 35.0,
                rollingDistanceMeters = 700.0,
                headingVarianceDeg = 55.0,
                sustainedStopMillis = 0L,
                lowMotionSampleCount = 0,
                isNearOrigin = false,
                originDwellMillis = 0L,
                isWalkingOrStill = false,
                stillConfidence = 0,
                inVehicleConfidence = 90,
                gpsAccuracyMeters = 10f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = true,
            ),
        )

        assertThat(score).isEqualTo(0.0)
    }

    @Test
    fun computeEndConfidence_penalizesPoorGpsQuality() {
        val good = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 0.98,
                speedMph = 2.0,
                rollingDistanceMeters = 40.0,
                headingVarianceDeg = 8.0,
                sustainedStopMillis = 90_000L,
                lowMotionSampleCount = 4,
                isNearOrigin = true,
                originDwellMillis = 120_000L,
                isWalkingOrStill = true,
                stillConfidence = 80,
                inVehicleConfidence = 0,
                gpsAccuracyMeters = 15f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = false,
            ),
        )
        val bad = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 0.98,
                speedMph = 2.0,
                rollingDistanceMeters = 40.0,
                headingVarianceDeg = 8.0,
                sustainedStopMillis = 90_000L,
                lowMotionSampleCount = 4,
                isNearOrigin = true,
                originDwellMillis = 120_000L,
                isWalkingOrStill = true,
                stillConfidence = 80,
                inVehicleConfidence = 0,
                gpsAccuracyMeters = 120f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = false,
            ),
        )

        assertThat(good).isGreaterThan(bad)
    }

    @Test
    fun computeEndConfidence_isBlockedWhenNotCloseToAllottedMiles() {
        val config = TripEndedDetector.Config(minCompletionRatioForEnding = 0.90)
        val score = TripEndedDetector.shouldShowBubble(
            state = com.example.outofroutebuddy.data.TripStateManager.TripState(
                isActive = true,
                loadedMiles = "100",
                bounceMiles = "0",
                startTime = java.util.Date(System.currentTimeMillis() - 10 * 60 * 1000L),
            ),
            metrics = TripMetrics(totalMiles = 50.0, oorMiles = 0.0),
            now = System.currentTimeMillis(),
            lastUserChoseNoAtMillis = 0L,
            config = config,
        )

        // Legacy gate should be false when not close enough.
        assertThat(score).isFalse()
    }

    @Test
    fun computeEndConfidence_supportsArrivalPrompt_whenParkedBelowTargetMiles() {
        val score = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 0.78,
                speedMph = 1.2,
                rollingDistanceMeters = 28.0,
                headingVarianceDeg = 6.0,
                sustainedStopMillis = 120_000L,
                lowMotionSampleCount = 4,
                isNearOrigin = false,
                originDwellMillis = 0L,
                isWalkingOrStill = true,
                stillConfidence = 82,
                inVehicleConfidence = 10,
                gpsAccuracyMeters = 18f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = false,
            ),
        )

        assertThat(score).isAtLeast(0.6)
    }

    @Test
    fun computeEndConfidence_penalizesEstimatedZeroSpeed_whenMovementStillHigh() {
        val directSpeed = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 0.92,
                speedMph = 2.0,
                rollingDistanceMeters = 90.0,
                headingVarianceDeg = 10.0,
                sustainedStopMillis = 30_000L,
                lowMotionSampleCount = 2,
                isNearOrigin = false,
                originDwellMillis = 0L,
                isWalkingOrStill = false,
                stillConfidence = 25,
                inVehicleConfidence = 35,
                gpsAccuracyMeters = 22f,
                hasDirectSpeed = true,
                legacyBubbleTriggered = false,
            ),
        )
        val estimatedOnly = TripEndedDetector.computeEndConfidence(
            TripEndedDetector.Companion.ConfidenceInput(
                ratio = 0.92,
                speedMph = 2.0,
                rollingDistanceMeters = 180.0,
                headingVarianceDeg = 10.0,
                sustainedStopMillis = 30_000L,
                lowMotionSampleCount = 2,
                isNearOrigin = false,
                originDwellMillis = 0L,
                isWalkingOrStill = false,
                stillConfidence = 25,
                inVehicleConfidence = 35,
                gpsAccuracyMeters = 22f,
                hasDirectSpeed = false,
                legacyBubbleTriggered = false,
            ),
        )

        assertThat(directSpeed).isGreaterThan(estimatedOnly)
    }
}
