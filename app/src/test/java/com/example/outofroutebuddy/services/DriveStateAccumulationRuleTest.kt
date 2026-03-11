package com.example.outofroutebuddy.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the trip accumulation rule used by TripTrackingService:
 * distance is accumulated only when (1) not manually paused AND (2) drive state is DRIVING.
 * This encodes the auto drive-detect / walking exclusion contract.
 */
class DriveStateAccumulationRuleTest {

    /**
     * Mirror of TripTrackingService accumulation condition so we can test it in isolation.
     */
    private fun shouldAccumulateDistance(isPaused: Boolean, driveState: DriveState): Boolean {
        return !isPaused && driveState == DriveState.DRIVING
    }

    @Test
    fun whenDrivingAndNotPaused_shouldAccumulate() {
        assertTrue(shouldAccumulateDistance(isPaused = false, driveState = DriveState.DRIVING))
    }

    @Test
    fun whenPaused_shouldNotAccumulateEvenIfDriving() {
        assertFalse(shouldAccumulateDistance(isPaused = true, driveState = DriveState.DRIVING))
    }

    @Test
    fun whenWalkingOrStationary_shouldNotAccumulate() {
        assertFalse(shouldAccumulateDistance(isPaused = false, driveState = DriveState.WALKING_OR_STATIONARY))
    }

    @Test
    fun whenPausedAndWalking_shouldNotAccumulate() {
        assertFalse(shouldAccumulateDistance(isPaused = true, driveState = DriveState.WALKING_OR_STATIONARY))
    }
}
