package com.example.outofroutebuddy.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.outofroutebuddy.domain.models.Trip
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Trip Exporter
 * 
 * Exports trips to CSV and PDF formats
 */
class TripExporter(private val context: Context) {
    
    companion object {
        private const val TAG = "TripExporter"
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }
    
    /**
     * Export trips to CSV format
     */
    fun exportToCSV(trips: List<Trip>): File {
        val fileName = "trips_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        file.bufferedWriter().use { writer ->
            // Write CSV header
            writer.write("Date,Loaded Miles,Bounce Miles,Actual Miles,Expected Miles,OOR Miles,OOR Percentage\n")
            
            // Write trip data
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
            for (trip in trips) {
                val tripDate = trip.endTime ?: trip.startTime ?: Date()
                val expectedMiles = trip.loadedMiles - trip.bounceMiles
                writer.write(
                    "${dateFormat.format(tripDate)}," +
                    "${trip.loadedMiles}," +
                    "${trip.bounceMiles}," +
                    "${trip.actualMiles}," +
                    "$expectedMiles," +
                    "${trip.oorMiles}," +
                    "${trip.oorPercentage}\n"
                )
            }
        }
        
        android.util.Log.d("TripExporter", "Exported ${trips.size} trips to CSV: ${file.absolutePath}")
        return file
    }
    
    /**
     * Export single trip to CSV
     */
    fun exportSingleTripToCSV(trip: Trip): File {
        return exportToCSV(listOf(trip))
    }
    
    /**
     * Generate PDF report (basic implementation)
     */
    fun exportToPDF(trips: List<Trip>): File {
        val fileName = "trips_report_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        
        file.bufferedWriter().use { writer ->
            writer.write("===========================================\n")
            writer.write("      TRIP HISTORY REPORT\n")
            writer.write("===========================================\n\n")
            
            writer.write("Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}\n")
            writer.write("Total Trips: ${trips.size}\n\n")
            
            // Summary statistics
            val totalMiles = trips.sumOf { it.actualMiles }
            val avgOOR = if (trips.isNotEmpty()) trips.map { it.oorMiles }.average() else 0.0
            
            writer.write("SUMMARY:\n")
            writer.write("  Total Miles: ${"%.1f".format(totalMiles)}\n")
            writer.write("  Average OOR: ${"%.1f".format(avgOOR)} miles\n")
            writer.write("  Average OOR %: ${"%.1f".format(if (trips.isNotEmpty()) trips.map { it.oorPercentage }.average() else 0.0)}%\n\n")
            
            writer.write("===========================================\n")
            writer.write("TRIP DETAILS:\n")
            writer.write("===========================================\n\n")
            
            // Trip details
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            for ((index, trip) in trips.withIndex()) {
                val tripDate = trip.endTime ?: trip.startTime ?: Date()
                val expectedMiles = trip.loadedMiles - trip.bounceMiles
                writer.write("Trip #${index + 1} - ${dateFormat.format(tripDate)}\n")
                writer.write("  Loaded:   ${"%.1f".format(trip.loadedMiles)} miles\n")
                writer.write("  Bounce:   ${"%.1f".format(trip.bounceMiles)} miles\n")
                writer.write("  Actual:   ${"%.1f".format(trip.actualMiles)} miles\n")
                writer.write("  Expected: ${"%.1f".format(expectedMiles)} miles\n")
                writer.write("  OOR:      ${"%.1f".format(trip.oorMiles)} miles (${"%.1f".format(trip.oorPercentage)}%)\n")
                writer.write("-------------------------------------------\n\n")
            }
        }
        
        android.util.Log.d("TripExporter", "Exported ${trips.size} trips to report: ${file.absolutePath}")
        return file
    }
    
    /**
     * Share exported file
     */
    fun shareFile(file: File, mimeType: String = "text/csv") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Trip Export - ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Trip Data"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error sharing file", e)
        }
    }
}

