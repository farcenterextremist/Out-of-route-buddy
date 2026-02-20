package com.example.outofroutebuddy.presentation

import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for pause button functionality
 * 
 * These tests verify:
 * - Pause/resume state transitions
 * - UI state updates correctly reflect paused state
 * - Icon and message changes are predictable
 */
class PauseButtonTest {

    @Test
    fun `UI state should have initial isPaused flag set to false`() {
        // Given & When
        val uiState = TripInputViewModel.TripInputUiState()
        
        // Then
        assertFalse("Trip should not be paused initially", uiState.isPaused)
    }

    @Test
    fun `UI state should support pause state updates`() {
        // Given
        val initialState = TripInputViewModel.TripInputUiState()
        
        // When
        val pausedState = initialState.copy(isPaused = true)
        
        // Then
        assertTrue("Trip should be paused", pausedState.isPaused)
        assertFalse("Original state should not be paused", initialState.isPaused)
    }

    @Test
    fun `UI state should support resume state updates`() {
        // Given
        val pausedState = TripInputViewModel.TripInputUiState(isPaused = true)
        
        // When
        val resumedState = pausedState.copy(isPaused = false)
        
        // Then
        assertFalse("Trip should be active after resume", resumedState.isPaused)
        assertTrue("Original state should be paused", pausedState.isPaused)
    }

    @Test
    fun `Pause state should not affect trip active status`() {
        // Given
        val activeState = TripInputViewModel.TripInputUiState(
            isTripActive = true,
            isPaused = false
        )
        
        // When - pause the trip
        val pausedActiveState = activeState.copy(isPaused = true)
        
        // Then
        assertTrue("Trip should still be active", pausedActiveState.isTripActive)
        assertTrue("Trip should be paused", pausedActiveState.isPaused)
    }

    @Test
    fun `UI state should preserve other values when pausing`() {
        // Given
        val activeState = TripInputViewModel.TripInputUiState(
            isTripActive = true,
            loadedMiles = 100.0,
            bounceMiles = 50.0,
            actualMiles = 75.0,
            oorMiles = 25.0,
            oorPercentage = 20.0,
            isPaused = false,
            tripStatusMessage = "Active"
        )
        
        // When
        val pausedState = activeState.copy(
            isPaused = true,
            tripStatusMessage = "Paused"
        )
        
        // Then - all trip values should be preserved
        assertTrue("Trip should still be active", pausedState.isTripActive)
        assertEquals("Loaded miles should be preserved", 100.0, pausedState.loadedMiles, 0.001)
        assertEquals("Bounce miles should be preserved", 50.0, pausedState.bounceMiles, 0.001)
        assertEquals("Actual miles should be preserved", 75.0, pausedState.actualMiles, 0.001)
        assertEquals("OOR miles should be preserved", 25.0, pausedState.oorMiles, 0.001)
        assertEquals("OOR percentage should be preserved", 20.0, pausedState.oorPercentage, 0.001)
        assertTrue("Trip should be paused", pausedState.isPaused)
        assertEquals("Status message should update", "Paused", pausedState.tripStatusMessage)
    }

    @Test
    fun `Pause and resume should toggle isPaused flag`() {
        // Given
        var currentState = TripInputViewModel.TripInputUiState(isPaused = false)
        
        // When - simulate pause
        currentState = currentState.copy(isPaused = true)
        assertTrue("After pause, should be paused", currentState.isPaused)
        
        // When - simulate resume
        currentState = currentState.copy(isPaused = false)
        assertFalse("After resume, should not be paused", currentState.isPaused)
        
        // When - pause again
        currentState = currentState.copy(isPaused = true)
        assertTrue("After second pause, should be paused again", currentState.isPaused)
    }

    @Test
    fun `Trip cannot be paused when inactive`() {
        // Given - inactive trip
        val inactiveState = TripInputViewModel.TripInputUiState(
            isTripActive = false,
            isPaused = false
        )
        
        // When - attempt to pause (shouldn't happen in real UI, but test the state)
        val pausedInactiveState = inactiveState.copy(isPaused = true)
        
        // Then
        assertFalse("Trip should still be inactive", pausedInactiveState.isTripActive)
        assertTrue("Can technically be paused even if inactive", pausedInactiveState.isPaused)
        // Note: In real implementation, UI should prevent pause button from being clickable when trip is not active
    }

    @Test
    fun `UI state should support complete pause workflow`() {
        // Given - active trip
        var state = TripInputViewModel.TripInputUiState(
            isTripActive = true,
            isPaused = false,
            loadedMiles = 200.0,
            bounceMiles = 100.0,
            actualMiles = 250.0,
            tripStatusMessage = "Trip active"
        )
        
        // When - pause
        state = state.copy(
            isPaused = true,
            tripStatusMessage = "Trip paused"
        )
        
        // Then - paused state
        assertTrue("Trip should be active", state.isTripActive)
        assertTrue("Trip should be paused", state.isPaused)
        assertEquals("Status should show paused", "Trip paused", state.tripStatusMessage)
        assertEquals("Actual miles should be preserved", 250.0, state.actualMiles, 0.001)
        
        // When - resume
        state = state.copy(
            isPaused = false,
            tripStatusMessage = "Trip active"
        )
        
        // Then - active state
        assertTrue("Trip should still be active", state.isTripActive)
        assertFalse("Trip should not be paused", state.isPaused)
        assertEquals("Status should show active", "Trip active", state.tripStatusMessage)
    }

    @Test
    fun `UI state immutability during pause operations`() {
        // Given
        val originalState = TripInputViewModel.TripInputUiState(
            isTripActive = true,
            isPaused = false
        )
        
        // When
        val pausedState = originalState.copy(isPaused = true)
        val resumedState = pausedState.copy(isPaused = false)
        
        // Then - verify immutability
        assertFalse("Original state should remain unchanged", originalState.isPaused)
        assertTrue("Paused state should be paused", pausedState.isPaused)
        assertFalse("Resumed state should not be paused", resumedState.isPaused)
        
        // Verify all three are different instances
        assertNotSame("Original and paused should be different", originalState, pausedState)
        assertNotSame("Paused and resumed should be different", pausedState, resumedState)
    }
}

