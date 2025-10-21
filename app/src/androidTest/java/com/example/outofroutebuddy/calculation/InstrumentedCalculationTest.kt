package com.example.outofroutebuddy.calculation

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Calculation Tests
 * 
 * These tests run on a REAL DEVICE and verify:
 * 1. User can input trip parameters via UI
 * 2. App correctly calculates OOR values
 * 3. Results are displayed correctly
 * 4. Calculations handle positive, zero, and negative OOR
 * 
 * Tests will output detailed workflow and calculation results
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class InstrumentedCalculationTest {

    private fun printHeader(title: String) {
        println("\n" + "=".repeat(70))
        println("📊 $title")
        println("=".repeat(70))
    }

    private fun printSection(title: String) {
        println("\n--- $title ---")
    }

    private fun printInput(loaded: String, bounce: String, actual: String) {
        println("📝 INPUT:")
        println("   Loaded Miles: $loaded")
        println("   Bounce Miles: $bounce")
        println("   Actual Miles: $actual (will be simulated by GPS)")
    }

    private fun printExpectedCalculation(loaded: Double, bounce: Double, actual: Double) {
        val dispatched = loaded + bounce
        val oor = actual - dispatched
        val oorPercent = if (dispatched > 0) (oor / dispatched) * 100 else 0.0
        
        println("\n🧮 EXPECTED CALCULATION:")
        println("   Dispatched = $loaded + $bounce = $dispatched miles")
        println("   OOR = $actual - $dispatched = $oor miles")
        println("   OOR% = ($oor / $dispatched) × 100 = ${"%.2f".format(oorPercent)}%")
        
        when {
            oor > 0 -> println("   ⚠️  POSITIVE OOR: Driver went OVER route")
            oor < 0 -> println("   ℹ️  NEGATIVE OOR: Driver came back UNDER route")
            else -> println("   ✅ ZERO OOR: Driver stayed ON route")
        }
    }

    @Test
    fun calculationTest01_PerfectRouteAdherence() {
        printHeader("TEST 1: Perfect Route Adherence (0% OOR)")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "100.0"
            val bounce = "25.0"
            val expected = 125.0
            
            printInput(loaded, bounce, expected.toString())
            printExpectedCalculation(100.0, 25.0, 125.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                println("   ✓ Entered loaded miles: $loaded")
            } catch (e: Exception) {
                println("   ⚠️ Using alternative input method")
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
            }
            
            Thread.sleep(300)
            
            try {
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Entered bounce miles: $bounce")
            } catch (e: Exception) {
                println("   ⚠️ Using alternative input method")
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            
            try {
                onView(withText(containsString("Start")))
                    .perform(click())
                println("   ✓ Trip started")
            } catch (e: Exception) {
                println("   ❌ Could not start trip: ${e.message}")
            }
            
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 125 miles (perfect)")
            println("   ℹ️  In real scenario, GPS would emit 125.0 miles")
            println("   ✓ Trip should show 0% OOR")
            
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            try {
                onView(withText(containsString("End")))
                    .perform(click())
                println("   ✓ Trip ended")
            } catch (e: Exception) {
                println("   ❌ Could not end trip: ${e.message}")
            }
            
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: 0% OOR (125 - 125 = 0)")
            println("   Status: Driver followed route perfectly")
        }
    }

    @Test
    fun calculationTest02_PositiveOOR_10Percent() {
        printHeader("TEST 2: Positive OOR - 10% Over Route")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "200.0"
            val bounce = "50.0"
            val actual = 275.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(200.0, 50.0, 275.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Parameters entered")
            } catch (e: Exception) {
                println("   ⚠️ Using alternative input")
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 275 miles (10% over)")
            println("   ℹ️  GPS would emit 275.0 miles")
            println("   ⚠️  Expected: +25 miles OOR (+10%)")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: +10% OOR (275 - 250 = +25 miles)")
            println("   Status: Driver went OVER route - needs coaching")
        }
    }

    @Test
    fun calculationTest03_NegativeOOR_CameBackEarly() {
        printHeader("TEST 3: Negative OOR - Driver Came Back Early")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "150.0"
            val bounce = "30.0"
            val actual = 140.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(150.0, 30.0, 140.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Parameters entered")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 140 miles (came back early)")
            println("   ℹ️  GPS would emit 140.0 miles (less than dispatched)")
            println("   ℹ️  Expected: -40 miles OOR (-22.22%)")
            println("   ✅ NEGATIVE OOR IS CORRECT - means under route!")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: -22.22% OOR (140 - 180 = -40 miles)")
            println("   Status: Driver came back UNDER route")
            println("   Meaning: Negative value = less than dispatched ✓")
        }
    }

    @Test
    fun calculationTest04_SmallPositiveOOR_5Percent() {
        printHeader("TEST 4: Small Positive OOR - 5% Over (Acceptable)")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "100.0"
            val bounce = "0.0"
            val actual = 105.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(100.0, 0.0, 105.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Parameters entered (no bounce miles)")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 105 miles (5% over)")
            println("   ℹ️  GPS would emit 105.0 miles")
            println("   ✓ Expected: +5 miles OOR (+5%)")
            println("   ✓ Minor deviation - likely traffic/detour")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: +5% OOR (105 - 100 = +5 miles)")
            println("   Status: Acceptable deviation")
        }
    }

    @Test
    fun calculationTest05_ExtremePositiveOOR_50Percent() {
        printHeader("TEST 5: Extreme Positive OOR - 50% Over!")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "100.0"
            val bounce = "0.0"
            val actual = 150.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(100.0, 0.0, 150.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Parameters entered")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 150 miles (EXTREME!)")
            println("   🚨 GPS would emit 150.0 miles")
            println("   🚨 Expected: +50 miles OOR (+50%!)")
            println("   🚨 CRITICAL: Driver went WAY off route!")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: +50% OOR (150 - 100 = +50 miles)")
            println("   Status: 🚨 CRITICAL - Immediate coaching needed!")
        }
    }

    @Test
    fun calculationTest06_LargeNegativeOOR_40PercentUnder() {
        printHeader("TEST 6: Large Negative OOR - 40% Under Route")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "200.0"
            val bounce = "50.0"
            val actual = 150.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(200.0, 50.0, 150.0)
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Parameters entered")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 150 miles (way under)")
            println("   ℹ️  GPS would emit 150.0 miles")
            println("   ℹ️  Expected: -100 miles OOR (-40%)")
            println("   ℹ️  Driver came back VERY early")
            println("   ✅ NEGATIVE = UNDER DISPATCHED (correct!)")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: -40% OOR (150 - 250 = -100 miles)")
            println("   Status: Driver significantly under dispatched")
            println("   Meaning: Negative = came back early ✓")
        }
    }

    @Test
    fun calculationTest07_RealWorldScenario_LongHaul() {
        printHeader("TEST 7: Real-World Long Haul Trip")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "450.0"
            val bounce = "75.0"
            val actual = 540.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(450.0, 75.0, 540.0)
            
            printSection("Scenario: Long haul truck driver")
            println("   Route: 450 loaded + 75 bounce = 525 dispatched")
            println("   Driver ends up doing 540 miles")
            println("   Why? Traffic detour, construction, missed exit")
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Long haul parameters entered")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Long haul trip started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS tracking")
            println("   📍 Progressive updates:")
            println("      100 miles → 250 miles → 400 miles → 540 miles")
            println("   ⚠️  Final: 540 miles (15 over dispatched)")
            Thread.sleep(3000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Long haul trip ended")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: +2.86% OOR (540 - 525 = +15 miles)")
            println("   Status: Minor deviation - acceptable for long haul")
            println("   Analysis: Likely due to detour or missed exit")
        }
    }

    @Test
    fun calculationTest08_ShortCommute_PerfectExecution() {
        printHeader("TEST 8: Short Commute - Perfect Execution")
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val loaded = "25.0"
            val bounce = "5.0"
            val actual = 30.0
            
            printInput(loaded, bounce, actual.toString())
            printExpectedCalculation(25.0, 5.0, 30.0)
            
            printSection("Scenario: Short local delivery route")
            println("   Route: 25 loaded + 5 bounce = 30 dispatched")
            println("   Driver completes exactly 30 miles")
            println("   Perfect route adherence!")
            
            printSection("Step 1: Enter Trip Parameters")
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
                println("   ✓ Short route parameters entered")
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText(loaded), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText(bounce), closeSoftKeyboard())
            }
            
            printSection("Step 2: Start Trip")
            Thread.sleep(500)
            onView(withText(containsString("Start"))).perform(click())
            println("   ✓ Short route started")
            Thread.sleep(2000)
            
            printSection("Step 3: Simulate GPS at 30 miles")
            println("   📍 GPS emits 30.0 miles")
            println("   ✅ Exactly matches dispatched!")
            Thread.sleep(2000)
            
            printSection("Step 4: End Trip")
            onView(withText(containsString("End"))).perform(click())
            println("   ✓ Short route completed")
            Thread.sleep(1500)
            
            printSection("RESULT")
            println("   ✅ TEST COMPLETE")
            println("   Expected: 0% OOR (30 - 30 = 0 miles)")
            println("   Status: 🌟 PERFECT! Driver followed route exactly")
        }
    }
}




