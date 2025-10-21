package com.example.outofroutebuddy.visual

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Read Actual UI Values Test
 * 
 * This test READS the REAL values displayed in the UI
 * and prints them so you can see exactly what users see.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ReadActualUIValuesTest {

    private var capturedText = ""

    private fun captureText(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "Capture text from TextView"
            }

            override fun perform(uiController: UiController?, view: View?) {
                val textView = view as? TextView
                capturedText = textView?.text?.toString() ?: "null"
            }
        }
    }

    private fun readField(id: Int, name: String): String {
        capturedText = ""
        try {
            onView(withId(id)).perform(captureText())
            return capturedText
        } catch (e: Exception) {
            return "[Field not accessible: ${e.message}]"
        }
    }

    @Test
    fun readUIValues_DuringTripLifecycle() {
        println("\n" + "═".repeat(90))
        println("📺 READING ACTUAL UI VALUES FROM SCREEN")
        println("═".repeat(90))
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            
            Thread.sleep(2000)
            
            println("\n📋 STEP 1: Reading UI Before Trip")
            println("─".repeat(90))
            
            var totalMiles = readField(R.id.total_miles_output, "Total Miles")
            var oorMiles = readField(R.id.oor_miles_output, "OOR Miles")
            var oorPercent = readField(R.id.oor_percentage_output, "OOR %")
            
            println("┌────────────────────────────────────────────────────────────┐")
            println("│ TODAY'S INFO - Before Trip:                                │")
            println("│   Total Miles: $totalMiles")
            println("│   OOR Miles: $oorMiles")
            println("│   OOR %: $oorPercent")
            println("└────────────────────────────────────────────────────────────┘")
            
            println("\n📝 STEP 2: Entering Trip Parameters")
            println("   Loaded: 100.0, Bounce: 25.0 (Dispatched: 125.0)")
            
            Thread.sleep(1000)
            
            try {
                onView(withId(R.id.loaded_miles_input))
                    .perform(clearText(), typeText("100.0"), closeSoftKeyboard())
                Thread.sleep(500)
                onView(withId(R.id.bounce_miles_input))
                    .perform(clearText(), typeText("25.0"), closeSoftKeyboard())
                println("   ✓ Parameters entered")
            } catch (e: Exception) {
                println("   ⚠️ Could not enter parameters: ${e.message}")
            }
            
            Thread.sleep(1000)
            
            println("\n🚀 STEP 3: Starting Trip")
            
            try {
                onView(withId(R.id.start_trip_button))
                    .perform(click())
                println("   ✓ Clicked Start Trip button")
            } catch (e: Exception) {
                try {
                    onView(withText(containsString("Start")))
                        .perform(click())
                    println("   ✓ Clicked Start button via text")
                } catch (e2: Exception) {
                    println("   ❌ Could not start trip: ${e2.message}")
                }
            }
            
            Thread.sleep(3000)
            
            println("\n📊 STEP 4: Reading UI Right After Start")
            println("─".repeat(90))
            
            totalMiles = readField(R.id.total_miles_output, "Total Miles")
            oorMiles = readField(R.id.oor_miles_output, "OOR Miles")
            oorPercent = readField(R.id.oor_percentage_output, "OOR %")
            
            println("┌────────────────────────────────────────────────────────────┐")
            println("│ TODAY'S INFO - After Start:                                │")
            println("│   Total Miles: $totalMiles ← Should be 0.0 initially")
            println("│   OOR Miles: $oorMiles ← Should be 0.0 or '0.0 (on route)'")
            println("│   OOR %: $oorPercent ← Should be 0.0% or '0.0% (perfect)'")
            println("└────────────────────────────────────────────────────────────┘")
            
            println("\n📍 STEP 5: Monitoring GPS Updates During Active Trip")
            println("   (Watching for 10 seconds to see if Total Miles updates)")
            println("─".repeat(90))
            
            for (i in 1..10) {
                Thread.sleep(1000)
                
                totalMiles = readField(R.id.total_miles_output, "Total Miles")
                oorMiles = readField(R.id.oor_miles_output, "OOR Miles")
                oorPercent = readField(R.id.oor_percentage_output, "OOR %")
                
                println("\n   Second $i:")
                println("   ┌──────────────────────────────────────────────────┐")
                println("   │ Total Miles: $totalMiles")
                println("   │ OOR Miles: $oorMiles")
                println("   │ OOR %: $oorPercent")
                println("   └──────────────────────────────────────────────────┘")
                
                if (i == 1) {
                    println("   💡 Baseline reading")
                } else if (totalMiles != "0.0" && !totalMiles.contains("Field not accessible")) {
                    println("   ✅ Total Miles IS updating from GPS!")
                } else {
                    println("   ⚠️ Total Miles not updating yet (GPS might be initializing)")
                }
            }
            
            Thread.sleep(2000)
            
            println("\n🏁 STEP 6: Ending Trip")
            
            try {
                onView(withId(R.id.start_trip_button))
                    .perform(click())
                println("   ✓ Clicked End Trip button")
            } catch (e: Exception) {
                try {
                    onView(withText(containsString("End")))
                        .perform(click())
                    println("   ✓ Clicked End button via text")
                } catch (e2: Exception) {
                    println("   ❌ Could not end trip: ${e2.message}")
                }
            }
            
            Thread.sleep(4000)
            
            println("\n📊 STEP 7: Reading UI After End Trip")
            println("─".repeat(90))
            
            totalMiles = readField(R.id.total_miles_output, "Total Miles")
            oorMiles = readField(R.id.oor_miles_output, "OOR Miles")
            oorPercent = readField(R.id.oor_percentage_output, "OOR %")
            
            println("┌────────────────────────────────────────────────────────────┐")
            println("│ TODAY'S INFO - After End Trip:                             │")
            println("│   Total Miles: $totalMiles ← Final GPS value")
            println("│   OOR Miles: $oorMiles ← NOW CALCULATED")
            println("│   OOR %: $oorPercent ← NOW CALCULATED")
            println("└────────────────────────────────────────────────────────────┘")
            
            println("\n" + "═".repeat(90))
            println("📊 ANALYSIS")
            println("═".repeat(90))
            
            if (oorMiles.contains("over") || oorMiles.contains("under") || oorMiles.contains("on route")) {
                println("✅ OOR Miles uses NEW formatting (over/under/on route)")
            } else {
                println("⚠️ OOR Miles: $oorMiles (check if formatted)")
            }
            
            if (oorPercent.contains("over") || oorPercent.contains("under") || oorPercent.contains("perfect")) {
                println("✅ OOR % uses NEW formatting (over/under/perfect)")
            } else {
                println("⚠️ OOR %: $oorPercent (check if formatted)")
            }
            
            if (!oorMiles.contains("-") && !oorPercent.contains("-")) {
                println("✅ NO MINUS SIGNS in OOR display!")
            } else {
                println("❌ Still showing minus signs: OOR Miles=$oorMiles, OOR %=$oorPercent")
            }
            
            println("═".repeat(90))
        }
    }
}




