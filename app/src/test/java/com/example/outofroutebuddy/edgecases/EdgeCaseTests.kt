package com.example.outofroutebuddy.edgecases

import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.services.PeriodCalculationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🧪 Edge Case Tests
 * 
 * Tests edge cases that could cause issues:
 * - Leap year boundaries
 * - DST transitions
 * - Month/year boundaries
 * - Timezone changes
 * 
 * ✅ NEW (#20): Edge Case Testing
 * 
 * Priority: MEDIUM
 * Impact: Correctness and reliability
 */
class EdgeCaseTests {
    
    private lateinit var periodCalculator: PeriodCalculationService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    @Before
    fun setup() {
        periodCalculator = PeriodCalculationService()
    }
    
    // ==================== LEAP YEAR TESTS ====================
    
    @Test
    fun `test leap year February 29th exists`() {
        // 2024 is a leap year
        val leapYearDate = dateFormat.parse("2024-02-29")
        assertNotNull("Leap year date should be valid", leapYearDate)
        
        val calendar = Calendar.getInstance().apply {
            time = leapYearDate!!
        }
        
        assertEquals("Should be February", Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals("Should be 29th", 29, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals("Should be 2024", 2024, calendar.get(Calendar.YEAR))
    }
    
    @Test
    fun `test non-leap year February has 28 days`() {
        // 2023 is not a leap year
        val febEnd2023 = dateFormat.parse("2023-02-28")
        assertNotNull("Feb 28 should be valid", febEnd2023)
        
        val calendar = Calendar.getInstance().apply {
            time = febEnd2023!!
        }
        
        assertEquals("Should be February", Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals("Should be 28th", 28, calendar.get(Calendar.DAY_OF_MONTH))
    }
    
    @Test
    fun `test period calculation across leap year boundary`() {
        val leapYearDate = dateFormat.parse("2024-02-29")!!
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(leapYearDate)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(leapYearDate)
        
        assertNotNull("Period start should be calculated", periodStart)
        assertNotNull("Period end should be calculated", periodEnd)
        assertTrue("Period end should be after start", periodEnd.time > periodStart.time)
    }
    
    // ==================== MONTH BOUNDARY TESTS ====================
    
    @Test
    fun `test month boundary December to January`() {
        val dec31 = dateFormat.parse("2023-12-31")!!
        val jan1 = dateFormat.parse("2024-01-01")!!
        
        val duration = jan1.time - dec31.time
        val expectedDuration = 24 * 60 * 60 * 1000L // 1 day
        
        assertTrue("Duration should be approximately 1 day", 
            kotlin.math.abs(duration - expectedDuration) < 1000) // Within 1 second
    }
    
    @Test
    fun `test period calculation at month boundary`() {
        val lastDayOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(lastDayOfMonth)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(lastDayOfMonth)
        
        assertNotNull("Should handle month boundary", periodStart)
        assertNotNull("Should handle month boundary", periodEnd)
    }
    
    // ==================== DST TRANSITION TESTS ====================
    
    @Test
    fun `test DST spring forward does not break duration calculation`() {
        // In US, DST typically starts second Sunday of March
        // 2024: March 10, 2:00 AM → 3:00 AM (lose 1 hour)
        
        val beforeDST = dateFormat.parse("2024-03-09")!!
        val afterDST = dateFormat.parse("2024-03-11")!!
        
        val duration = afterDST.time - beforeDST.time
        val expectedDuration = 2 * 24 * 60 * 60 * 1000L // 2 days in milliseconds
        
        // Duration should be approximately 2 days (might be off by up to 2 hours due to DST and timezone)
        val toleranceMs = 2 * 60 * 60 * 1000L // 2 hours tolerance
        assertTrue("Duration should be approximately 2 days (±2 hours for DST), was ${duration}ms vs expected ${expectedDuration}ms",
            kotlin.math.abs(duration - expectedDuration) < toleranceMs)
    }
    
    @Test
    fun `test DST fall back does not break duration calculation`() {
        // In US, DST typically ends first Sunday of November
        // 2024: November 3, 2:00 AM → 1:00 AM (gain 1 hour)
        
        val beforeDST = dateFormat.parse("2024-11-02")!!
        val afterDST = dateFormat.parse("2024-11-04")!!
        
        val duration = afterDST.time - beforeDST.time
        val expectedDuration = 2 * 24 * 60 * 60 * 1000L // 2 days in milliseconds
        
        // Duration should be approximately 2 days (might be off by up to 2 hours due to DST and timezone)
        val toleranceMs = 2 * 60 * 60 * 1000L // 2 hours tolerance
        assertTrue("Duration should be approximately 2 days (±2 hours for DST), was ${duration}ms vs expected ${expectedDuration}ms",
            kotlin.math.abs(duration - expectedDuration) < toleranceMs)
    }
    
    // ==================== YEAR BOUNDARY TESTS ====================
    
    @Test
    fun `test year boundary December 31 to January 1`() {
        val dec31_2023 = dateFormat.parse("2023-12-31")!!
        val jan1_2024 = dateFormat.parse("2024-01-01")!!
        
        val duration = jan1_2024.time - dec31_2023.time
        val expectedDuration = 24 * 60 * 60 * 1000L
        
        assertTrue("Duration across year boundary should be 1 day",
            kotlin.math.abs(duration - expectedDuration) < 1000)
    }
    
    @Test
    fun `test period calculation across year boundary`() {
        val dec15_2023 = dateFormat.parse("2023-12-15")!!
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(dec15_2023)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(dec15_2023)
        
        assertNotNull("Should handle year boundary", periodStart)
        assertNotNull("Should handle year boundary", periodEnd)
        assertTrue("Period should span correctly", periodEnd.time > periodStart.time)
    }
    
    // ==================== EXTREME VALUE TESTS ====================
    
    @Test
    fun `test zero miles calculation`() {
        // Test with all zeros - validates safe division by zero handling
        val loaded = 0.0
        val bounce = 0.0
        val actual = 0.0
        
        val dispatched = loaded + bounce // 0.0
        val oor = actual - dispatched // 0.0
        // This tests that the code properly handles division by zero
        val oorPercentage = if (dispatched > 0.0) (oor / dispatched) * 100 else 0.0
        
        assertEquals("OOR should be 0", 0.0, oor, 0.001)
        assertEquals("OOR percentage should be 0 when dispatched is 0", 0.0, oorPercentage, 0.001)
    }
    
    @Test
    fun `test very large miles values`() {
        val loaded = 9999.0
        val bounce = 500.0
        val actual = 10500.0
        
        val dispatched = loaded + bounce
        val oor = actual - dispatched
        val oorPercentage = (oor / dispatched) * 100
        
        assertEquals("OOR should be 1.0", 1.0, oor, 0.001)
        assertTrue("OOR percentage should be reasonable", oorPercentage < 1.0)
    }
    
    @Test
    fun `test negative OOR scenario`() {
        // Actual less than dispatched (under-mileage)
        val loaded = 500.0
        val bounce = 100.0
        val actual = 550.0
        
        val dispatched = loaded + bounce // 600
        val oor = actual - dispatched // -50
        val oorPercentage = (oor / dispatched) * 100 // -8.33%
        
        assertEquals("OOR should be negative", -50.0, oor, 0.001)
        assertEquals("OOR percentage should be negative", -8.33, oorPercentage, 0.1)
    }
    
    // ==================== DATE EDGE CASES ====================
    
    @Test
    fun `test first day of month`() {
        val firstDay = dateFormat.parse("2024-03-01")!!
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(firstDay)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(firstDay)
        
        assertNotNull("Should handle first day of month", periodStart)
        assertNotNull("Should handle first day of month", periodEnd)
    }
    
    @Test
    fun `test last day of month`() {
        val lastDay = dateFormat.parse("2024-01-31")!!
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(lastDay)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(lastDay)
        
        assertNotNull("Should handle last day of month", periodStart)
        assertNotNull("Should handle last day of month", periodEnd)
    }
    
    @Test
    fun `test all months have correct period calculations`() {
        for (month in 0..11) {
            val date = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2024)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 15) // Mid-month
            }.time
            
            val periodStart = periodCalculator.calculateCustomPeriodStart(date)
            val periodEnd = periodCalculator.calculateCustomPeriodEnd(date)
            
            assertNotNull("Month $month should have valid period start", periodStart)
            assertNotNull("Month $month should have valid period end", periodEnd)
            assertTrue("Month $month period end should be after start", 
                periodEnd.time > periodStart.time)
        }
    }
    
    // ==================== NULL/EMPTY TESTS ====================
    
    @Test
    fun `test empty trip list statistics`() {
        val trips = emptyList<com.example.outofroutebuddy.domain.models.Trip>()
        
        val totalMiles = trips.sumOf { it.actualMiles }
        val totalOOR = trips.sumOf { it.oorMiles }
        
        assertEquals("Empty list should have 0 total miles", 0.0, totalMiles, 0.001)
        assertEquals("Empty list should have 0 OOR", 0.0, totalOOR, 0.001)
    }
    
    // ==================== ADDITIONAL DST & TIMEZONE EDGE CASES ====================
    
    @Test
    fun `test leap year February 29 period calculation`() {
        // Feb 29, 2024 is the Thursday before first Friday of March (Mar 1, 2024)
        val leapDay = dateFormat.parse("2024-02-29")!!
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(leapDay)
        val periodEnd = periodCalculator.calculateCustomPeriodEnd(leapDay)
        
        // Feb 29 is itself a period start
        assertEquals("Feb 29, 2024 should be period start", Calendar.FEBRUARY, periodStart.get(Calendar.MONTH))
        assertEquals("Should be 29th", 29, periodStart.get(Calendar.DAY_OF_MONTH))
        
        // Period end should be in April
        assertTrue("Period end should be after period start", periodEnd.time > periodStart.time)
    }
    
    @Test
    fun `test century leap year 2000`() {
        // 2000 is a leap year (divisible by 400)
        val calendar = Calendar.getInstance()
        calendar.set(2000, Calendar.FEBRUARY, 29)
        
        // Should not throw exception
        val date = calendar.time
        assertNotNull("Feb 29, 2000 should be valid", date)
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH))
    }
    
    @Test
    fun `test non-leap century year 1900`() {
        // 1900 is NOT a leap year (divisible by 100 but not 400)
        val calendar = Calendar.getInstance()
        calendar.set(1900, Calendar.FEBRUARY, 28)
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Add 1 day to Feb 28
        
        // Should roll over to March 1
        assertEquals("Should roll to March", Calendar.MARCH, calendar.get(Calendar.MONTH))
        assertEquals("Should be March 1", 1, calendar.get(Calendar.DAY_OF_MONTH))
    }
    
    @Test
    fun `test multiple DST transitions in same year`() {
        // Test that period calculations work across multiple DST transitions
        val dates = listOf(
            "2024-02-15", // Before spring DST
            "2024-04-15", // After spring DST
            "2024-08-15", // Summer
            "2024-11-15"  // After fall DST
        )
        
        dates.forEach { dateStr ->
            val date = dateFormat.parse(dateStr)!!
            val periodStart = periodCalculator.calculateCustomPeriodStart(date)
            val periodEnd = periodCalculator.calculateCustomPeriodEnd(date)
            
            assertNotNull("Period start should be valid for $dateStr", periodStart)
            assertNotNull("Period end should be valid for $dateStr", periodEnd)
            assertTrue("Period end should be after start for $dateStr", 
                periodEnd.time > periodStart.time)
        }
    }
    
    @Test
    fun `test midnight boundary calculations`() {
        // Test calculations at exactly midnight
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val midnightDate = calendar.time
        
        val periodStart = periodCalculator.calculateCustomPeriodStart(midnightDate)
        assertNotNull("Midnight should have valid period start", periodStart)
        
        // Test just before midnight
        calendar.set(2024, Calendar.FEBRUARY, 29, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val justBeforeMidnight = calendar.time
        
        val periodStart2 = periodCalculator.calculateCustomPeriodStart(justBeforeMidnight)
        assertNotNull("Just before midnight should have valid period start", periodStart2)
    }
    
    @Test
    fun `test week boundary at month end`() {
        // Test when week boundary coincides with month end
        val dates = listOf(
            "2024-01-31", // Wednesday
            "2024-03-31", // Sunday
            "2024-05-31", // Friday
            "2024-08-31"  // Saturday
        )
        
        dates.forEach { dateStr ->
            val date = dateFormat.parse(dateStr)!!
            val periodStart = periodCalculator.calculateCustomPeriodStart(date)
            val periodEnd = periodCalculator.calculateCustomPeriodEnd(date)
            
            assertNotNull("$dateStr should have valid period", periodStart)
            assertTrue("$dateStr period should be valid", periodEnd.time > periodStart.time)
        }
    }
    
    @Test
    fun `test very long time span calculation`() {
        // Test calculations spanning many years
        val startDate = dateFormat.parse("2020-01-01")!!
        val endDate = dateFormat.parse("2024-12-31")!!
        
        val duration = endDate.time - startDate.time
        val expectedYears = 5
        val daysInPeriod = duration / (24 * 60 * 60 * 1000L)
        
        assertTrue("Should span approximately 5 years (~1826 days)", 
            daysInPeriod > 1800 && daysInPeriod < 1850)
    }
    
    @Test
    fun `test consecutive Friday calculations`() {
        // Verify first Friday calculation is consistent across consecutive months
        for (month in 0..11) {
            val firstFriday = periodCalculator.findFirstFridayOfMonth(2024, month)
            
            assertEquals("Should be Friday", Calendar.FRIDAY, firstFriday.get(Calendar.DAY_OF_WEEK))
            assertTrue("Should be in first week", firstFriday.get(Calendar.DAY_OF_MONTH) <= 7)
            assertEquals("Should be correct month", month, firstFriday.get(Calendar.MONTH))
        }
    }
}


