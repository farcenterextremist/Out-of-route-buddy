package com.example.outofroutebuddy.ui

import org.junit.Test
import java.util.Locale

/**
 * OOR Display Formatting Test
 * 
 * This test demonstrates the new user-friendly OOR display format
 * and prints out actual before/after examples so you can see the difference.
 */
class OORDisplayFormattingTest {

    /**
     * Format OOR miles for user-friendly display
     * - Negative values show as "X.X under" instead of "-X.X"
     * - Positive values show as "X.X over" instead of "+X.X"
     * - Zero shows as "0.0 (on route)"
     */
    private fun formatOORMiles(oorMiles: Double): String {
        return when {
            oorMiles > 0.01 -> String.format(Locale.US, "%.1f over", oorMiles)
            oorMiles < -0.01 -> String.format(Locale.US, "%.1f under", Math.abs(oorMiles))
            else -> "0.0 (on route)"
        }
    }

    /**
     * Format OOR percentage for user-friendly display
     * - Negative values show as "X.X% under" instead of "-X.X%"
     * - Positive values show as "X.X% over" instead of "+X.X%"
     * - Zero shows as "0.0% (perfect)"
     */
    private fun formatOORPercentage(oorPercentage: Double): String {
        return when {
            oorPercentage > 0.01 -> String.format(Locale.US, "%.1f%% over", oorPercentage)
            oorPercentage < -0.01 -> String.format(Locale.US, "%.1f%% under", Math.abs(oorPercentage))
            else -> "0.0% (perfect)"
        }
    }

    /**
     * OLD formatting (what was shown before)
     */
    private fun formatOldStyle(oorMiles: Double, oorPercentage: Double): Pair<String, String> {
        return Pair(
            String.format(Locale.US, "%.1f", oorMiles),
            String.format(Locale.US, "%.1f%%", oorPercentage)
        )
    }

    @Test
    fun `demonstrate OOR display formatting - all scenarios`() {
        println("\n" + "=".repeat(80))
        println("OOR DISPLAY FORMATTING - BEFORE vs AFTER")
        println("=".repeat(80))

        val scenarios = listOf(
            Triple(100.0, 25.0, 125.0) to "Perfect Route Adherence",
            Triple(200.0, 50.0, 275.0) to "10% Over Route (Positive OOR)",
            Triple(150.0, 30.0, 140.0) to "Under Route (Negative OOR)",
            Triple(100.0, 0.0, 105.0) to "5% Over Route",
            Triple(100.0, 0.0, 150.0) to "50% Over Route (EXTREME)",
            Triple(200.0, 50.0, 150.0) to "40% Under Route (Large Negative)"
        )

        scenarios.forEachIndexed { index, (params, description) ->
            val (loaded, bounce, actual) = params
            val dispatched = loaded + bounce
            val oorMiles = actual - dispatched
            val oorPercent = (oorMiles / dispatched) * 100

            println("\n" + "-".repeat(80))
            println("SCENARIO ${index + 1}: $description")
            println("-".repeat(80))
            println("INPUT:")
            println("  Loaded: $loaded miles")
            println("  Bounce: $bounce miles")
            println("  Dispatched: $dispatched miles")
            println("  Actual: $actual miles")
            println()
            println("CALCULATION:")
            println("  OOR Miles = $actual - $dispatched = $oorMiles")
            println("  OOR % = ($oorMiles / $dispatched) × 100 = ${"%.2f".format(oorPercent)}%")
            println()

            val (oldMiles, oldPercent) = formatOldStyle(oorMiles, oorPercent)
            val newMiles = formatOORMiles(oorMiles)
            val newPercent = formatOORPercentage(oorPercent)

            println("❌ OLD DISPLAY (Confusing):")
            println("   OOR Miles: $oldMiles")
            println("   OOR %: $oldPercent")
            println()
            println("✅ NEW DISPLAY (Clear!):")
            println("   OOR Miles: $newMiles")
            println("   OOR %: $newPercent")
            println()

            // Show what it means
            when {
                oorMiles > 0 -> println("   Meaning: Driver went ${Math.abs(oorMiles)} miles OVER the route")
                oorMiles < 0 -> println("   Meaning: Driver came back ${Math.abs(oorMiles)} miles UNDER the route")
                else -> println("   Meaning: Driver followed route PERFECTLY!")
            }
        }

        println("\n" + "=".repeat(80))
        println("SUMMARY:")
        println("=".repeat(80))
        println("✅ Positive OOR: Shows \"X.X over\" - driver went beyond route")
        println("✅ Negative OOR: Shows \"X.X under\" - driver came back early (NO MINUS!)")
        println("✅ Zero OOR: Shows \"on route\" and \"perfect\" - encouraging feedback")
        println()
        println("The NEW format eliminates confusion and makes it crystal clear!")
        println("=".repeat(80))
    }

    @Test
    fun `show specific problematic negative OOR example`() {
        println("\n" + "=".repeat(80))
        println("SPECIFIC EXAMPLE: The Confusing Negative OOR Case")
        println("=".repeat(80))

        val loaded = 75.0
        val bounce = 25.0
        val actual = 80.0
        val dispatched = loaded + bounce
        val oorMiles = actual - dispatched
        val oorPercent = (oorMiles / dispatched) * 100

        println("\nREAL-WORLD SCENARIO:")
        println("  A driver is dispatched for a 100-mile route (75 loaded + 25 bounce)")
        println("  They only drive 80 miles and come back early")
        println()
        println("CALCULATION:")
        println("  Dispatched = 75 + 25 = 100 miles")
        println("  Actual = 80 miles")
        println("  OOR Miles = 80 - 100 = -20 miles")
        println("  OOR % = (-20 / 100) × 100 = -20%")
        println()

        val (oldMiles, oldPercent) = formatOldStyle(oorMiles, oorPercent)
        val newMiles = formatOORMiles(oorMiles)
        val newPercent = formatOORPercentage(oorPercent)

        println("❌ OLD DISPLAY (What users saw before):")
        println("   ┌─────────────────────────────────┐")
        println("   │ OOR Miles: $oldMiles             │")
        println("   │ OOR %: $oldPercent              │")
        println("   └─────────────────────────────────┘")
        println()
        println("   User reaction: 😕 \"What? Negative? Is this an error?\"")
        println("   User confusion: \"Did the calculation break?\"")
        println("   User question: \"What does -20.0 mean?\"")
        println()

        println("✅ NEW DISPLAY (What users see now):")
        println("   ┌─────────────────────────────────┐")
        println("   │ OOR Miles: $newMiles      │")
        println("   │ OOR %: $newPercent     │")
        println("   └─────────────────────────────────┘")
        println()
        println("   User reaction: ✅ \"Oh! I drove 20 miles under the route\"")
        println("   User understanding: \"I came back early - makes sense!\"")
        println("   User clarity: \"20% under means I didn't complete full route\"")
        println()

        println("THIS IS THE FIX!")
        println("  • No more confusing minus signs")
        println("  • Clear 'under' label explains what happened")
        println("  • Users immediately understand the meaning")
        println("=".repeat(80))
    }

    @Test
    fun `show toast message formatting`() {
        println("\n" + "=".repeat(80))
        println("TOAST MESSAGE FORMATTING")
        println("=".repeat(80))

        val scenarios = listOf(
            Triple(100.0, 25.0, 125.0),  // Perfect
            Triple(200.0, 50.0, 275.0),  // 10% over
            Triple(150.0, 30.0, 140.0)   // Under route
        )

        scenarios.forEach { (loaded, bounce, actual) ->
            val dispatched = loaded + bounce
            val oorMiles = actual - dispatched
            val oorPercent = (oorMiles / dispatched) * 100

            val (oldMiles, oldPercent) = formatOldStyle(oorMiles, oorPercent)
            val newMiles = formatOORMiles(oorMiles)
            val newPercent = formatOORPercentage(oorPercent)

            println("\nScenario: Loaded=$loaded, Bounce=$bounce, Actual=$actual")
            println()
            println("  ❌ OLD: Trip calculated! OOR: $oldMiles miles, $oldPercent")
            println("  ✅ NEW: Trip calculated! OOR: $newMiles, $newPercent")
            println()
        }

        println("=".repeat(80))
    }
}









