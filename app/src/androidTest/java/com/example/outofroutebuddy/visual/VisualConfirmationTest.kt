package com.example.outofroutebuddy.visual

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import android.view.View
import android.widget.TextView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher

/**
 * Visual Confirmation Test
 * 
 * This test READS actual UI values from the screen and prints them
 * so you can see exactly what numbers are displayed to users.
 * 
 * It will show:
 * - What's in Total Miles field
 * - What's in OOR Miles field
 * - What's in OOR % field
 * 
 * At each stage of the trip workflow.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VisualConfirmationTest {

    /**
     * Custom ViewAction to read text from a TextView
     */
    private fun getText(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "Get text from TextView"
            }

            override fun perform(uiController: UiController, view: View) {
                val textView = view as TextView
                textView.tag = textView.text.toString()
            }
        }
    }

    private fun readUIValue(resourceId: Int, fieldName: String): String {
        return try {
            var text = ""
            onView(withId(resourceId)).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = isAssignableFrom(TextView::class.java)
                override fun getDescription(): String = "Get text"
                override fun perform(uiController: UiController, view: View) {
                    text = (view as TextView).text.toString()
                }
            })
            text
        } catch (e: Exception) {
            "N/A (field not found)"
        }
    }

    private fun printUISnapshot(phase: String) {
        println("\n┌─────────────────────────────────────────────────────┐")
        println("│ 📱 UI SNAPSHOT: $phase")
        println("├─────────────────────────────────────────────────────┤")
        
        Thread.sleep(500)
        
        val totalMiles = readUIValue(R.id.total_miles_output, "Total Miles")
        val oorMiles = readUIValue(R.id.oor_miles_output, "OOR Miles")
        val oorPercent = readUIValue(R.id.oor_percentage_output, "OOR %")
        
        println("│ TODAY'S INFO:                                       │")
        println("│   Total Miles: $totalMiles")
        println("│   OOR Miles: $oorMiles")
        println("│   OOR %: $oorPercent")
        println("└─────────────────────────────────────────────────────┘")
    }

    @Test
    fun visualTest01_WatchTotalMilesUpdate_OORStaysZero() {
        println("\n" + "═".repeat(80))
        println("🎬 VISUAL CONFIRMATION TEST: GPS → Total Miles Flow")
        println("═".repeat(80))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            println("\n📝 PHASE 1: Enter Trip Parameters")
            println("   Loaded: 100.0 miles")
            println("   Bounce: 25.0 miles")
            println("   Dispatched: 125.0 miles")
            
            Thread.sleep(2000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("100.0"), closeSoftKeyboard())
                println("   ✓ Entered loaded miles")
                
                Thread.sleep(300)
                
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("25.0"), closeSoftKeyboard())
                println("   ✓ Entered bounce miles")
            } catch (e: Exception) {
                println("   ⚠️ Using alternative input")
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText("100.0"), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText("25.0"), closeSoftKeyboard())
            }
            
            printUISnapshot("Before Starting Trip")
            
            println("\n🚀 PHASE 2: Start Trip")
            Thread.sleep(1000)
            
            try {
                onView(withText(containsString("Start")))
                    .perform(click())
                println("   ✓ Trip started")
            } catch (e: Exception) {
                println("   ⚠️ Could not click start")
            }
            
            Thread.sleep(2000)
            printUISnapshot("Right After Start (Initial State)")
            
            println("\n📍 PHASE 3: Simulating GPS Updates")
            println("   (Waiting for GPS to update Total Miles...)")
            
            // Monitor for several seconds to see updates
            for (i in 1..8) {
                Thread.sleep(1500)
                println("\n   ⏱️  Time Elapsed: ${i * 1.5} seconds")
                printUISnapshot("During Active Trip - Update $i")
                
                if (i == 4) {
                    println("\n   📌 CHECKPOINT: Halfway through monitoring")
                    println("   ✅ If Total Miles is updating → GPS is working")
                    println("   ✅ If OOR is still 0.0 → Workflow is correct")
                }
            }
            
            println("\n🏁 PHASE 4: End Trip")
            Thread.sleep(1000)
            
            try {
                onView(withText(containsString("End")))
                    .perform(click())
                println("   ✓ Trip ended")
            } catch (e: Exception) {
                println("   ⚠️ Could not click end")
            }
            
            Thread.sleep(2500)
            printUISnapshot("After End Trip (Final Calculations)")
            
            println("\n" + "═".repeat(80))
            println("📊 VISUAL CONFIRMATION SUMMARY")
            println("═".repeat(80))
            println("✅ Total Miles field shows actual UI values")
            println("✅ OOR Miles field shows actual UI values with formatting")
            println("✅ OOR % field shows actual UI values with formatting")
            println()
            println("👆 Check the UI snapshots above to see actual displayed values!")
            println("═".repeat(80))
        }
    }

    @Test
    fun visualTest02_ShowPositiveOOR_OverRouteScenario() {
        println("\n" + "═".repeat(80))
        println("🎬 VISUAL TEST: Positive OOR (Driver Goes Over Route)")
        println("═".repeat(80))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            println("\n📝 SETUP: 100 mile route, driver will go 120 miles")
            Thread.sleep(2000)
            
            // Enter parameters
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("80.0"), closeSoftKeyboard())
                Thread.sleep(300)
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("20.0"), closeSoftKeyboard())
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText("80.0"), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText("20.0"), closeSoftKeyboard())
            }
            
            printUISnapshot("Before Trip")
            
            println("\n🚀 Starting Trip...")
            Thread.sleep(1000)
            onView(withText(containsString("Start"))).perform(click())
            Thread.sleep(2000)
            
            printUISnapshot("Trip Started - Initial")
            
            println("\n📍 Simulating GPS (Driver Going Over)...")
            
            for (i in 1..6) {
                Thread.sleep(1800)
                println("\n   Update $i:")
                printUISnapshot("Active Trip - GPS Update $i")
                
                if (i >= 5) {
                    println("   ⚠️  Driver is going beyond dispatched 100 miles")
                    println("   ⚠️  But OOR should still show 0.0 (not calculated yet)")
                }
            }
            
            println("\n🏁 Ending Trip...")
            Thread.sleep(1500)
            onView(withText(containsString("End"))).perform(click())
            Thread.sleep(2500)
            
            printUISnapshot("AFTER END - Final OOR Display")
            
            println("\n📊 EXPECTED FINAL VALUES:")
            println("   Total Miles: ~120.0")
            println("   OOR Miles: 20.0 over ← Should show 'over' label")
            println("   OOR %: 20.0% over ← Should show 'over' label")
            
            println("\n" + "═".repeat(80))
        }
    }

    @Test
    fun visualTest03_ShowNegativeOOR_UnderRouteScenario() {
        println("\n" + "═".repeat(80))
        println("🎬 VISUAL TEST: Negative OOR (Driver Comes Back Under)")
        println("═".repeat(80))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            println("\n📝 SETUP: 100 mile route, driver will only go 75 miles")
            Thread.sleep(2000)
            
            // Enter parameters
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("80.0"), closeSoftKeyboard())
                Thread.sleep(300)
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("20.0"), closeSoftKeyboard())
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText("80.0"), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText("20.0"), closeSoftKeyboard())
            }
            
            printUISnapshot("Before Trip")
            
            println("\n🚀 Starting Trip...")
            Thread.sleep(1000)
            onView(withText(containsString("Start"))).perform(click())
            Thread.sleep(2000)
            
            printUISnapshot("Trip Started - Initial")
            
            println("\n📍 Simulating GPS (Driver Coming Back Early)...")
            
            for (i in 1..5) {
                Thread.sleep(1600)
                println("\n   Update $i:")
                printUISnapshot("Active Trip - GPS Update $i")
                
                if (i == 5) {
                    println("   ℹ️  Driver stopped at ~75 miles (25 under dispatched)")
                    println("   ℹ️  But OOR should still show 0.0 (not calculated yet)")
                }
            }
            
            println("\n🏁 Ending Trip...")
            Thread.sleep(1500)
            onView(withText(containsString("End"))).perform(click())
            Thread.sleep(2500)
            
            printUISnapshot("AFTER END - Final OOR Display")
            
            println("\n📊 EXPECTED FINAL VALUES:")
            println("   Total Miles: ~75.0")
            println("   OOR Miles: 25.0 under ← Should show 'under' (NO MINUS!)")
            println("   OOR %: 25.0% under ← Should show 'under' (NO MINUS!)")
            println()
            println("   ✅ Key Point: 'under' label instead of '-25.0'")
            
            println("\n" + "═".repeat(80))
        }
    }

    @Test
    fun visualTest04_CompareBeforeDuringAfter_AllStages() {
        println("\n" + "═".repeat(80))
        println("🎬 VISUAL TEST: Complete Trip Lifecycle - All Stages")
        println("═".repeat(80))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            println("\n📝 SETUP: Dispatched 60 miles (50L + 10B), will go 70 miles")
            Thread.sleep(2000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("50.0"), closeSoftKeyboard())
                Thread.sleep(300)
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("10.0"), closeSoftKeyboard())
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText("50.0"), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText("10.0"), closeSoftKeyboard())
            }
            
            println("\n" + "─".repeat(80))
            println("STAGE 1: BEFORE TRIP STARTS")
            println("─".repeat(80))
            printUISnapshot("Before Trip")
            println("\n💡 All fields should be at default/zero state")
            
            Thread.sleep(1500)
            
            println("\n🚀 >>> STARTING TRIP <<<")
            onView(withText(containsString("Start"))).perform(click())
            Thread.sleep(2500)
            
            println("\n" + "─".repeat(80))
            println("STAGE 2: RIGHT AFTER START (Initial State)")
            println("─".repeat(80))
            printUISnapshot("Just After Start")
            println("\n💡 Expected: Total=0.0, OOR=0.0, OOR%=0.0")
            
            Thread.sleep(2000)
            
            println("\n" + "─".repeat(80))
            println("STAGE 3: DURING TRIP (GPS Updating)")
            println("─".repeat(80))
            
            for (update in 1..5) {
                Thread.sleep(1800)
                println("\n   🛰️  GPS Update #$update")
                printUISnapshot("During Trip - Update $update")
                println("\n   💡 Expected: Total Miles increasing, OOR staying at 0")
            }
            
            Thread.sleep(2000)
            
            println("\n" + "─".repeat(80))
            println("STAGE 4: BEFORE ENDING (Final Active State)")
            println("─".repeat(80))
            printUISnapshot("Before End Trip")
            println("\n💡 Expected: Total Miles has updated, OOR still 0")
            
            Thread.sleep(1500)
            
            println("\n🏁 >>> ENDING TRIP <<<")
            onView(withText(containsString("End"))).perform(click())
            Thread.sleep(3000)
            
            println("\n" + "─".repeat(80))
            println("STAGE 5: AFTER TRIP ENDS (Final Calculations)")
            println("─".repeat(80))
            printUISnapshot("After Trip Ended")
            println("\n💡 Expected: Total Miles final value, OOR NOW calculated")
            println("💡 If went 70 miles: OOR should show '10.0 over' and '16.7% over'")
            
            println("\n" + "═".repeat(80))
            println("📊 COMPLETE LIFECYCLE DEMONSTRATED")
            println("═".repeat(80))
            println("✅ Check all snapshots above to see actual UI values!")
            println("✅ Compare 'During Trip' vs 'After End' to see the difference")
            println("═".repeat(80))
        }
    }

    @Test
    fun visualTest05_FocusOnTotalMilesField_ShowGPSFlow() {
        println("\n" + "═".repeat(80))
        println("🎬 FOCUSED TEST: Total Miles Field GPS Updates")
        println("═".repeat(80))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            println("\n📝 This test focuses ONLY on Total Miles field")
            println("   Goal: Confirm GPS data flows to Total Miles")
            
            Thread.sleep(2000)
            
            // Quick setup
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("100.0"), closeSoftKeyboard())
                Thread.sleep(200)
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("0.0"), closeSoftKeyboard())
            } catch (e: Exception) {
                onView(withHint(containsString("Loaded")))
                    .perform(clearText(), typeText("100.0"), closeSoftKeyboard())
                onView(withHint(containsString("Bounce")))
                    .perform(clearText(), typeText("0.0"), closeSoftKeyboard())
            }
            
            Thread.sleep(1000)
            println("   ✓ Setup complete")
            
            println("\n🚀 Starting Trip...")
            onView(withText(containsString("Start"))).perform(click())
            Thread.sleep(2000)
            
            println("\n📊 MONITORING TOTAL MILES FIELD:")
            println("   (Reading actual value from UI every 1.5 seconds)")
            println()
            
            for (reading in 1..10) {
                Thread.sleep(1500)
                
                val totalMilesValue = readUIValue(R.id.total_miles_output, "Total Miles")
                val oorMilesValue = readUIValue(R.id.oor_miles_output, "OOR Miles")
                
                println("   Reading $reading:")
                println("      Total Miles: $totalMilesValue ← Should change")
                println("      OOR Miles: $oorMilesValue ← Should stay 0.0")
                println()
                
                if (reading == 1) {
                    println("      💡 First reading - baseline")
                } else if (reading == 10) {
                    println("      💡 Last reading before end")
                }
            }
            
            println("\n🏁 Ending Trip...")
            Thread.sleep(1000)
            onView(withText(containsString("End"))).perform(click())
            Thread.sleep(3000)
            
            println("\n📊 FINAL VALUES AFTER END:")
            val finalTotal = readUIValue(R.id.total_miles_output, "Total Miles")
            val finalOOR = readUIValue(R.id.oor_miles_output, "OOR Miles")
            val finalPercent = readUIValue(R.id.oor_percentage_output, "OOR %")
            
            println("   ┌─────────────────────────────────────┐")
            println("   │ Total Miles: $finalTotal              │")
            println("   │ OOR Miles: $finalOOR                  │")
            println("   │ OOR %: $finalPercent                  │")
            println("   └─────────────────────────────────────┘")
            
            println("\n✅ TEST COMPLETE")
            println("   • Total Miles should show final GPS value")
            println("   • OOR should now be calculated and displayed")
            println("   • Check if format is user-friendly ('over' or 'on route')")
            
            println("\n" + "═".repeat(80))
        }
    }
}




