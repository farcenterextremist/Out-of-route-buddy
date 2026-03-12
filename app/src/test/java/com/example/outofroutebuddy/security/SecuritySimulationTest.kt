package com.example.outofroutebuddy.security

import com.example.outofroutebuddy.util.InputValidator
import com.example.outofroutebuddy.validation.ValidationFramework
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Security Simulation Tests — Cyber Security Loop
 *
 * Single entry point for automated attack simulations. Maps to playbooks in
 * run_purple_simulations.py and ATTACK_LIBRARY. These tests verify that
 * ValidationFramework and InputValidator reject malicious inputs.
 *
 * Playbooks covered:
 * - trip_validation_rejects_nan
 * - trip_validation_rejects_negative
 * - trip_validation_rejects_out_of_range
 * - input_validator_rejects_path_traversal
 *
 * Best practice: Controlled attack scenarios with explicit expected outcomes.
 * See docs/automation/CYBER_SECURITY_LOOP_ROUTINE.md.
 */
class SecuritySimulationTest {

    // ==================== trip_validation_rejects_nan ====================

    @Test
    fun `trip validation rejects NaN string`() {
        val result = InputValidator.sanitizeMiles("NaN")
        assertNull("InputValidator must reject NaN string", result)
    }

    @Test
    fun `trip validation rejects NaN via ValidationFramework`() {
        val result = ValidationFramework.UnifiedValidation.validateTripInput(
            loadedMilesText = "NaN",
            bounceMilesText = "25.0",
            isStartingTrip = true,
        )
        assertFalse("ValidationFramework must reject NaN in loaded miles", result.isValid)
    }

    // ==================== trip_validation_rejects_negative ====================

    @Test
    fun `trip validation rejects negative miles`() {
        val result = InputValidator.sanitizeMiles("-10")
        assertNull("InputValidator must reject negative miles", result)
    }

    @Test
    fun `trip validation rejects negative via ValidationFramework`() {
        val result = ValidationFramework.UnifiedValidation.validateTripInput(
            loadedMilesText = "100.0",
            bounceMilesText = "-5.0",
            isStartingTrip = true,
        )
        assertFalse("ValidationFramework must reject negative bounce miles", result.isValid)
    }

    // ==================== trip_validation_rejects_out_of_range ====================

    @Test
    fun `trip validation rejects out of range miles`() {
        val result = InputValidator.sanitizeMiles("20000")
        assertNull("InputValidator must reject miles > 10000", result)
    }

    @Test
    fun `trip validation rejects out of range via ValidationFramework`() {
        val result = ValidationFramework.UnifiedValidation.validateTripInput(
            loadedMilesText = "15000",
            bounceMilesText = "25.0",
            isStartingTrip = true,
        )
        assertFalse("ValidationFramework must reject loaded miles > 10000", result.isValid)
    }

    // ==================== input_validator_rejects_path_traversal ====================

    @Test
    fun `input validator rejects path traversal parent directory`() {
        val result = InputValidator.sanitizeFilePath("../../etc/passwd")
        assertNull("InputValidator must reject directory traversal", result)
    }

    @Test
    fun `input validator rejects path traversal home directory`() {
        val result = InputValidator.sanitizeFilePath("~/secret.txt")
        assertNull("InputValidator must reject home directory access", result)
    }

    @Test
    fun `input validator rejects absolute path`() {
        val result = InputValidator.sanitizeFilePath("/etc/passwd")
        assertNull("InputValidator must reject absolute path", result)
    }
}
