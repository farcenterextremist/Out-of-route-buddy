package com.example.outofroutebuddy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.outofroutebuddy.data.util.DateConverter
import java.util.Date

/**
 * Entity for storing trip data in the Room database.
 * This ensures trip data persists across app restarts and configuration changes.
 */
@Entity(tableName = "trips")
@TypeConverters(DateConverter::class)
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val loadedMiles: Double,
    val bounceMiles: Double,
    val actualMiles: Double,
    val oorMiles: Double,
    val oorPercentage: Double,
    val createdAt: Date = Date(),
    // ✅ NEW: GPS metadata for accuracy auditing
    val avgGpsAccuracy: Double = 0.0, // Average GPS accuracy in meters
    val minGpsAccuracy: Double = 0.0, // Minimum GPS accuracy in meters
    val maxGpsAccuracy: Double = 0.0, // Maximum GPS accuracy in meters
    val totalGpsPoints: Int = 0, // Total GPS points received
    val validGpsPoints: Int = 0, // Valid GPS points used for distance calculation
    val rejectedGpsPoints: Int = 0, // GPS points rejected by validation
    val tripDurationMinutes: Int = 0, // Trip duration in minutes
    val avgSpeedMph: Double = 0.0, // Average speed during trip
    val maxSpeedMph: Double = 0.0, // Maximum speed during trip
    val locationJumpsDetected: Int = 0, // Number of location jumps detected
    val accuracyWarnings: Int = 0, // Number of accuracy warnings
    val speedAnomalies: Int = 0, // Number of speed anomalies detected
    // ✅ NEW: Trip state tracking
    val tripStartTime: Date? = null, // When the trip actually started
    val tripEndTime: Date? = null, // When the trip actually ended
    val wasInterrupted: Boolean = false, // Whether the trip was interrupted
    val interruptionCount: Int = 0, // Number of times the trip was interrupted
    val lastLocationLat: Double = 0.0, // Last known latitude
    val lastLocationLng: Double = 0.0, // Last known longitude
    val lastLocationTime: Date? = null, // Last location timestamp
) {
    val dispatchedMiles: Double
        get() = loadedMiles + bounceMiles

    // ✅ NEW: GPS quality metrics
    val gpsQualityPercentage: Double
        get() = if (totalGpsPoints > 0) (validGpsPoints.toDouble() / totalGpsPoints) * 100 else 0.0

    val avgAccuracyFeet: Double
        get() = avgGpsAccuracy * 3.28084 // Convert meters to feet

    val tripDurationHours: Double
        get() = tripDurationMinutes / 60.0
} 
