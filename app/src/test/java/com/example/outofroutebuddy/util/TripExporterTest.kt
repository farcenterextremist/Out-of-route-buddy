package com.example.outofroutebuddy.util

import android.content.Context
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.io.path.createTempDirectory

/**
 * ✅ HIGH PRIORITY: Trip Exporter Tests
 * 
 * Tests critical export functionality for:
 * - CSV generation (data integrity)
 * - PDF report generation
 * - File sharing capability
 */
class TripExporterTest {

    private lateinit var tripExporter: TripExporter
    private lateinit var mockContext: Context
    private lateinit var testCacheDir: File

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        
        // Create temporary directory for test files
        testCacheDir = createTempDirectory("test_export").toFile()
        every { mockContext.cacheDir } returns testCacheDir
        every { mockContext.packageName } returns "com.example.outofroutebuddy"
        
        tripExporter = TripExporter(mockContext)
    }

    @After
    fun tearDown() {
        // Clean up test files
        testCacheDir.deleteRecursively()
        unmockkAll()
    }

    // ==================== CSV EXPORT TESTS ====================

    @Test
    fun `exportToCSV creates valid CSV file`() {
        val trips = listOf(
            createTestTrip(loadedMiles = 100.0, bounceMiles = 20.0, actualMiles = 85.0),
            createTestTrip(loadedMiles = 150.0, bounceMiles = 30.0, actualMiles = 125.0)
        )
        
        val csvFile = tripExporter.exportToCSV(trips)
        
        assertTrue("CSV file should exist", csvFile.exists())
        assertTrue("CSV file should not be empty", csvFile.length() > 0)
    }

    @Test
    fun `exportToCSV includes CSV header`() {
        val trips = listOf(createTestTrip())
        
        val csvFile = tripExporter.exportToCSV(trips)
        val content = csvFile.readText()
        
        assertTrue("CSV should have header row", content.contains("Date,Loaded Miles,Bounce Miles,Actual Miles"))
        assertTrue("CSV should include OOR columns", content.contains("OOR Miles,OOR Percentage"))
    }

    @Test
    fun `exportToCSV formats trip data correctly`() {
        val trip = createTestTrip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 85.0
        )
        
        val csvFile = tripExporter.exportToCSV(listOf(trip))
        val lines = csvFile.readLines()
        
        assertTrue("CSV should have at least 2 lines (header + data)", lines.size >= 2)
        
        val dataLine = lines[1]
        assertTrue("Data line should contain loaded miles", dataLine.contains("100.0"))
        assertTrue("Data line should contain bounce miles", dataLine.contains("20.0"))
        assertTrue("Data line should contain actual miles", dataLine.contains("85.0"))
    }

    @Test
    fun `exportToCSV calculates expected miles correctly`() {
        val trip = createTestTrip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 85.0
        )
        
        val csvFile = tripExporter.exportToCSV(listOf(trip))
        val content = csvFile.readText()
        
        // Expected miles = Loaded - Bounce = 100 - 20 = 80
        assertTrue("CSV should calculate expected miles (80.0)", content.contains("80.0"))
    }

    @Test
    fun `exportToCSV handles empty trip list`() {
        val csvFile = tripExporter.exportToCSV(emptyList())
        val lines = csvFile.readLines()
        
        assertEquals("Empty list should only have header", 1, lines.size)
        assertTrue("Should have header row", lines[0].contains("Date"))
    }

    @Test
    fun `exportToCSV handles multiple trips`() {
        val trips = (1..10).map { i ->
            createTestTrip(
                loadedMiles = 100.0 + i,
                actualMiles = 80.0 + i
            )
        }
        
        val csvFile = tripExporter.exportToCSV(trips)
        val lines = csvFile.readLines()
        
        assertEquals("Should have header + 10 data rows", 11, lines.size)
    }

    // ==================== PDF EXPORT TESTS ====================

    @Test
    fun `exportToPDF creates valid report file`() {
        val trips = listOf(createTestTrip())
        
        val pdfFile = tripExporter.exportToPDF(trips)
        
        assertTrue("Report file should exist", pdfFile.exists())
        assertTrue("Report file should not be empty", pdfFile.length() > 0)
    }

    @Test
    fun `exportToPDF includes header and summary`() {
        val trips = listOf(
            createTestTrip(actualMiles = 100.0),
            createTestTrip(actualMiles = 150.0)
        )
        
        val pdfFile = tripExporter.exportToPDF(trips)
        val content = pdfFile.readText()
        
        assertTrue("Report should have title", content.contains("TRIP HISTORY REPORT"))
        assertTrue("Report should have summary section", content.contains("SUMMARY:"))
        assertTrue("Report should show total trips", content.contains("Total Trips: 2"))
    }

    @Test
    fun `exportToPDF calculates summary statistics correctly`() {
        val trips = listOf(
            createTestTrip(actualMiles = 100.0, oorMiles = 5.0),
            createTestTrip(actualMiles = 200.0, oorMiles = -10.0)
        )
        
        val pdfFile = tripExporter.exportToPDF(trips)
        val content = pdfFile.readText()
        
        // Total miles: 100 + 200 = 300
        assertTrue("Should show total miles", content.contains("300.0"))
        
        // Average OOR: (5 + (-10)) / 2 = -2.5
        assertTrue("Should calculate average OOR", content.contains("-2.5"))
    }

    @Test
    fun `exportToPDF includes trip details`() {
        val trip = createTestTrip(
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 85.0,
            oorMiles = 5.0,
            oorPercentage = 6.25
        )
        
        val pdfFile = tripExporter.exportToPDF(listOf(trip))
        val content = pdfFile.readText()
        
        assertTrue("Should show loaded miles", content.contains("100.0"))
        assertTrue("Should show bounce miles", content.contains("20.0"))
        assertTrue("Should show actual miles", content.contains("85.0"))
        assertTrue("Should show OOR miles", content.contains("5.0"))
    }

    @Test
    fun `exportToPDF handles empty trip list`() {
        val pdfFile = tripExporter.exportToPDF(emptyList())
        val content = pdfFile.readText()
        
        assertTrue("Should show 0 trips", content.contains("Total Trips: 0"))
        assertTrue("Should have header", content.contains("TRIP HISTORY REPORT"))
    }

    // ==================== SINGLE TRIP EXPORT TESTS ====================

    @Test
    fun `exportSingleTripToCSV works correctly`() {
        val trip = createTestTrip(actualMiles = 125.0)
        
        val csvFile = tripExporter.exportSingleTripToCSV(trip)
        val lines = csvFile.readLines()
        
        assertEquals("Should have header + 1 data row", 2, lines.size)
        assertTrue("Should contain trip data", lines[1].contains("125.0"))
    }

    // ==================== HELPER METHODS ====================

    private fun createTestTrip(
        loadedMiles: Double = 100.0,
        bounceMiles: Double = 20.0,
        actualMiles: Double = 80.0,
        oorMiles: Double = 0.0,
        oorPercentage: Double = 0.0
    ): Trip {
        return Trip(
            id = UUID.randomUUID().toString(),
            loadedMiles = loadedMiles,
            bounceMiles = bounceMiles,
            actualMiles = actualMiles,
            oorMiles = oorMiles,
            oorPercentage = oorPercentage,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED,
            gpsMetadata = GpsMetadata()
        )
    }
}


