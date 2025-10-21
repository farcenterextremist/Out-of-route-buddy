package com.example.outofroutebuddy.calculation

import org.junit.Test
import org.junit.Assert.*

/**
 * OOR Calculation Verification Test
 * 
 * This test comprehensively verifies the Out-of-Route (OOR) calculation logic
 * including positive, negative, and zero OOR scenarios.
 * 
 * FORMULA:
 * - Dispatched Miles = Loaded Miles + Bounce Miles
 * - OOR Miles = Actual Miles - Dispatched Miles
 * - OOR Percentage = (OOR Miles / Dispatched Miles) × 100
 * 
 * SCENARIOS:
 * 1. Positive OOR: Driver goes OVER the route (needs coaching)
 * 2. Zero OOR: Driver follows route exactly (perfect!)
 * 3. Negative OOR: Driver comes back UNDER the route (came back early)
 */
class OORCalculationVerificationTest {

    private fun calculateOOR(loadedMiles: Double, bounceMiles: Double, actualMiles: Double): Triple<Double, Double, Double> {
        val dispatchedMiles = loadedMiles + bounceMiles
        val oorMiles = actualMiles - dispatchedMiles
        val oorPercentage = if (dispatchedMiles > 0) (oorMiles / dispatchedMiles) * 100 else 0.0
        
        return Triple(dispatchedMiles, oorMiles, oorPercentage)
    }

    @Test
    fun `verify calculation - perfect route adherence (0 percent OOR)`() {
        println("\n=== TEST 1: Perfect Route Adherence ===")
        
        val loadedMiles = 40.0
        val bounceMiles = 10.0
        val actualMiles = 50.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: ✅ Driver stayed exactly on route!")
        
        assertEquals(50.0, dispatched, 0.01)
        assertEquals(0.0, oor, 0.01)
        assertEquals(0.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - positive OOR (driver went over route)`() {
        println("\n=== TEST 2: Positive OOR (10% Over Route) ===")
        
        val loadedMiles = 400.0
        val bounceMiles = 100.0
        val actualMiles = 550.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: ⚠️ Driver went 50 miles over route (needs coaching)")
        
        assertEquals(500.0, dispatched, 0.01)
        assertEquals(50.0, oor, 0.01)
        assertEquals(10.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - negative OOR (driver came back early)`() {
        println("\n=== TEST 3: Negative OOR (Driver Under Route) ===")
        
        val loadedMiles = 75.0
        val bounceMiles = 25.0
        val actualMiles = 80.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: ℹ️ Driver came back 20 miles early (under route)")
        println("         Negative OOR is CORRECT - means less than dispatched!")
        
        assertEquals(100.0, dispatched, 0.01)
        assertEquals(-20.0, oor, 0.01)
        assertEquals(-20.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - extreme positive OOR (100 percent over)`() {
        println("\n=== TEST 4: Extreme Positive OOR (100% Over!) ===")
        
        val loadedMiles = 75.0
        val bounceMiles = 25.0
        val actualMiles = 200.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: 🚨 CRITICAL! Driver doubled the route distance!")
        
        assertEquals(100.0, dispatched, 0.01)
        assertEquals(100.0, oor, 0.01)
        assertEquals(100.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - small positive OOR (5 percent over)`() {
        println("\n=== TEST 5: Small Positive OOR (5% Over) ===")
        
        val loadedMiles = 100.0
        val bounceMiles = 0.0
        val actualMiles = 105.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles (no bounce)")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: ⚠️ Minor deviation, within acceptable range")
        
        assertEquals(100.0, dispatched, 0.01)
        assertEquals(5.0, oor, 0.01)
        assertEquals(5.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - large negative OOR (50 percent under)`() {
        println("\n=== TEST 6: Large Negative OOR (50% Under) ===")
        
        val loadedMiles = 200.0
        val bounceMiles = 0.0
        val actualMiles = 100.0
        
        val (dispatched, oor, oorPercent) = calculateOOR(loadedMiles, bounceMiles, actualMiles)
        
        println("Input:")
        println("  Loaded: $loadedMiles miles")
        println("  Bounce: $bounceMiles miles")
        println("  Actual: $actualMiles miles")
        println("\nCalculations:")
        println("  Dispatched = $loadedMiles + $bounceMiles = $dispatched miles")
        println("  OOR = $actualMiles - $dispatched = $oor miles")
        println("  OOR% = ($oor / $dispatched) × 100 = $oorPercent%")
        println("\nResult: ℹ️ Driver came back VERY early (50% under route)")
        println("         Negative value is CORRECT!")
        
        assertEquals(200.0, dispatched, 0.01)
        assertEquals(-100.0, oor, 0.01)
        assertEquals(-50.0, oorPercent, 0.01)
    }

    @Test
    fun `verify calculation - multiple scenarios edge cases`() {
        println("\n=== TEST 7: Edge Cases ===")
        
        val scenarios = listOf(
            // (loaded, bounce, actual, expected oor, expected %)
            Triple(10.0, 5.0, 15.0) to Pair(0.0, 0.0),           // Perfect
            Triple(50.0, 50.0, 110.0) to Pair(10.0, 10.0),       // 10% over
            Triple(100.0, 20.0, 60.0) to Pair(-60.0, -50.0),     // 50% under
            Triple(1.0, 1.0, 3.0) to Pair(1.0, 50.0),            // 50% over
            Triple(500.0, 100.0, 500.0) to Pair(-100.0, -16.67)  // ~17% under
        )
        
        scenarios.forEachIndexed { index, (input, expected) ->
            val (loaded, bounce, actual) = input
            val (expectedOor, expectedPercent) = expected
            val (dispatched, oor, oorPercent) = calculateOOR(loaded, bounce, actual)
            
            println("\nScenario ${index + 1}:")
            println("  L=$loaded, B=$bounce, A=$actual → D=$dispatched")
            println("  OOR=$oor miles (expected $expectedOor)")
            println("  OOR%=${"%.2f".format(oorPercent)}% (expected ${"%.2f".format(expectedPercent)}%)")
            
            assertEquals("Scenario $index OOR miles", expectedOor, oor, 0.01)
            assertEquals("Scenario $index OOR percent", expectedPercent, oorPercent, 0.5)
        }
    }

    @Test
    fun `explain negative OOR meaning`() {
        println("\n" + "=".repeat(70))
        println("📚 UNDERSTANDING NEGATIVE OOR VALUES")
        println("=".repeat(70))
        println()
        println("❓ What does NEGATIVE OOR mean?")
        println("   → The driver drove LESS than the dispatched route distance")
        println("   → This happens when drivers come back early")
        println("   → It's a NORMAL and EXPECTED calculation")
        println()
        println("✅ Is negative OOR an error?")
        println("   → NO! It's mathematically correct")
        println("   → Formula: OOR = Actual - Dispatched")
        println("   → If Actual < Dispatched, then OOR is negative")
        println()
        println("📊 Example:")
        println("   Dispatched: 100 miles (route distance)")
        println("   Actual: 80 miles (driver came back at 80)")
        println("   OOR = 80 - 100 = -20 miles")
        println("   OOR% = (-20 / 100) × 100 = -20%")
        println()
        println("💡 What does -20% mean?")
        println("   → Driver drove 20% LESS than the route")
        println("   → Came back 20 miles early")
        println("   → Under-delivered on the route")
        println()
        println("🎯 When to worry:")
        println("   ✅ Negative OOR (under route): Usually OK")
        println("   ⚠️ Positive OOR (over route): May need coaching")
        println("   🚨 Large positive OOR (>15%): Definitely needs review")
        println()
        println("=".repeat(70))
    }
}




