package com.example.outofroutebuddy.utils

import android.location.Location
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import java.util.*

/**
 * 🔧 Test Data Builders
 * 
 * Builder pattern implementations for creating test data objects.
 * Provides fluent API for building complex test scenarios.
 * 
 * Created: Phase 0 - Infrastructure
 * Purpose: Simplify test data creation
 */
object TestDataBuilders {
    
    // ==================== TRIP BUILDER ====================
    
    /**
     * Builder for creating Trip objects in tests
     */
    class TripBuilder {
        private var id: String = UUID.randomUUID().toString()
        private var loadedMiles: Double = 100.0
        private var bounceMiles: Double = 20.0
        private var actualMiles: Double = 125.0
        private var oorMiles: Double = 5.0
        private var oorPercentage: Double = 4.17
        private var startTime: Date? = Date()
        private var endTime: Date? = Date()
        
        fun withId(id: String) = apply { this.id = id }
        fun withLoadedMiles(miles: Double) = apply { this.loadedMiles = miles }
        fun withBounceMiles(miles: Double) = apply { this.bounceMiles = miles }
        fun withActualMiles(miles: Double) = apply { this.actualMiles = miles }
        fun withOorMiles(miles: Double) = apply { this.oorMiles = miles }
        fun withOorPercentage(percentage: Double) = apply { this.oorPercentage = percentage }
        fun withStartTime(time: Date?) = apply { this.startTime = time }
        fun withEndTime(time: Date?) = apply { this.endTime = time }
        
        /**
         * Build with automatic OOR calculation
         */
        fun buildWithCalculation(): Trip {
            val dispatched = loadedMiles + bounceMiles
            oorMiles = actualMiles - dispatched
            oorPercentage = if (dispatched > 0) (oorMiles / dispatched) * 100 else 0.0
            return build()
        }
        
        fun build(): Trip {
            return Trip(
                id = id,
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                startTime = startTime,
                endTime = endTime
            )
        }
        
        companion object {
            fun aTrip() = TripBuilder()
            
            fun aStandardTrip() = TripBuilder()
                .withLoadedMiles(100.0)
                .withBounceMiles(20.0)
                .withActualMiles(125.0)
                .buildWithCalculation()
            
            fun aCustomPeriodTrip() = TripBuilder()
                .withLoadedMiles(100.0)
                .withBounceMiles(20.0)
                .withActualMiles(125.0)
                .buildWithCalculation()
            
            fun aZeroOorTrip() = TripBuilder()
                .withLoadedMiles(100.0)
                .withBounceMiles(20.0)
                .withActualMiles(120.0)
                .buildWithCalculation()
            
            fun aNegativeOorTrip() = TripBuilder()
                .withLoadedMiles(100.0)
                .withBounceMiles(20.0)
                .withActualMiles(110.0)
                .buildWithCalculation()
        }
    }
    
    // ==================== LOCATION BUILDER ====================
    
    /**
     * Builder for creating Location objects in tests
     */
    class LocationBuilder {
        private var latitude: Double = 37.7749
        private var longitude: Double = -122.4194
        private var accuracy: Float = 10f
        private var speed: Float = 25f
        private var bearing: Float = 0f
        private var altitude: Double = 0.0
        private var time: Long = System.currentTimeMillis()
        
        fun withLatitude(lat: Double) = apply { this.latitude = lat }
        fun withLongitude(lon: Double) = apply { this.longitude = lon }
        fun withAccuracy(acc: Float) = apply { this.accuracy = acc }
        fun withSpeed(spd: Float) = apply { this.speed = spd }
        fun withBearing(brg: Float) = apply { this.bearing = brg }
        fun withAltitude(alt: Double) = apply { this.altitude = alt }
        fun withTime(t: Long) = apply { this.time = t }
        
        fun build(): Location {
            return Location("test").apply {
                latitude = this@LocationBuilder.latitude
                longitude = this@LocationBuilder.longitude
                accuracy = this@LocationBuilder.accuracy
                speed = this@LocationBuilder.speed
                bearing = this@LocationBuilder.bearing
                altitude = this@LocationBuilder.altitude
                time = this@LocationBuilder.time
            }
        }
        
        companion object {
            fun aLocation() = LocationBuilder()
            
            fun aGoodLocation() = LocationBuilder()
                .withAccuracy(5f)
                .withSpeed(25f)
                .build()
            
            fun aPoorLocation() = LocationBuilder()
                .withAccuracy(50f)
                .build()
            
            fun aHighSpeedLocation() = LocationBuilder()
                .withSpeed(75f)
                .build()
        }
    }
    
    // ==================== THEME STATE BUILDER ====================
    
    /**
     * Builder for creating theme state test scenarios
     */
    class ThemeStateBuilder {
        private var themePreference: String = "light"
        private var systemDarkMode: Boolean = false
        private var activityRecreated: Boolean = false
        
        fun withThemePreference(theme: String) = apply { this.themePreference = theme }
        fun withSystemDarkMode(isDark: Boolean) = apply { this.systemDarkMode = isDark }
        fun withActivityRecreated(recreated: Boolean) = apply { this.activityRecreated = recreated }
        
        fun build(): ThemeState {
            return ThemeState(themePreference, systemDarkMode, activityRecreated)
        }
        
        companion object {
            fun aThemeState() = ThemeStateBuilder()
            fun lightTheme() = ThemeStateBuilder().withThemePreference("light").build()
            fun darkTheme() = ThemeStateBuilder().withThemePreference("dark").build()
            fun systemTheme() = ThemeStateBuilder().withThemePreference("system").build()
        }
    }
    
    data class ThemeState(
        val themePreference: String,
        val systemDarkMode: Boolean,
        val activityRecreated: Boolean
    )
    
    // ==================== PERMISSION STATE BUILDER ====================
    
    /**
     * Builder for creating permission state test scenarios
     */
    class PermissionStateBuilder {
        private var fineLocationGranted: Boolean = false
        private var coarseLocationGranted: Boolean = false
        private var backgroundLocationGranted: Boolean = false
        private var rationaleShown: Boolean = false
        private var permanentlyDenied: Boolean = false
        
        fun withFineLocation(granted: Boolean) = apply { this.fineLocationGranted = granted }
        fun withCoarseLocation(granted: Boolean) = apply { this.coarseLocationGranted = granted }
        fun withBackgroundLocation(granted: Boolean) = apply { this.backgroundLocationGranted = granted }
        fun withRationaleShown(shown: Boolean) = apply { this.rationaleShown = shown }
        fun withPermanentlyDenied(denied: Boolean) = apply { this.permanentlyDenied = denied }
        
        fun build(): PermissionState {
            return PermissionState(
                fineLocationGranted,
                coarseLocationGranted,
                backgroundLocationGranted,
                rationaleShown,
                permanentlyDenied
            )
        }
        
        companion object {
            fun aPermissionState() = PermissionStateBuilder()
            
            fun allGranted() = PermissionStateBuilder()
                .withFineLocation(true)
                .withCoarseLocation(true)
                .withBackgroundLocation(true)
                .build()
            
            fun allDenied() = PermissionStateBuilder()
                .withFineLocation(false)
                .withCoarseLocation(false)
                .withBackgroundLocation(false)
                .build()
            
            fun foregroundOnly() = PermissionStateBuilder()
                .withFineLocation(true)
                .withCoarseLocation(true)
                .withBackgroundLocation(false)
                .build()
            
            fun permanentlyDenied() = PermissionStateBuilder()
                .withFineLocation(false)
                .withPermanentlyDenied(true)
                .build()
        }
    }
    
    data class PermissionState(
        val fineLocationGranted: Boolean,
        val coarseLocationGranted: Boolean,
        val backgroundLocationGranted: Boolean,
        val rationaleShown: Boolean,
        val permanentlyDenied: Boolean
    ) {
        fun hasAnyLocationPermission() = fineLocationGranted || coarseLocationGranted
        fun hasAllPermissions() = fineLocationGranted && coarseLocationGranted && backgroundLocationGranted
    }
    
    // ==================== PREFERENCES BUILDER ====================
    
    /**
     * Builder for creating app preference test states
     */
    class PreferencesBuilder {
        private var gpsUpdateFrequency: Int = 10
        private var distanceUnits: String = "miles"
        private var theme: String = "light"
        private var notificationsEnabled: Boolean = true
        private var autoStartTrip: Boolean = false
        private var periodMode: String = "STANDARD"
        
        fun withGpsFrequency(seconds: Int) = apply { this.gpsUpdateFrequency = seconds }
        fun withDistanceUnits(units: String) = apply { this.distanceUnits = units }
        fun withTheme(theme: String) = apply { this.theme = theme }
        fun withNotifications(enabled: Boolean) = apply { this.notificationsEnabled = enabled }
        fun withAutoStartTrip(enabled: Boolean) = apply { this.autoStartTrip = enabled }
        fun withPeriodMode(mode: String) = apply { this.periodMode = mode }
        
        fun build(): PreferencesState {
            return PreferencesState(
                gpsUpdateFrequency,
                distanceUnits,
                theme,
                notificationsEnabled,
                autoStartTrip,
                periodMode
            )
        }
        
        companion object {
            fun aPreferencesState() = PreferencesBuilder()
            
            fun defaultPreferences() = PreferencesBuilder().build()
            
            fun kilometersPreferences() = PreferencesBuilder()
                .withDistanceUnits("kilometers")
                .build()
            
            fun fastGpsPreferences() = PreferencesBuilder()
                .withGpsFrequency(5)
                .build()
        }
    }
    
    data class PreferencesState(
        val gpsUpdateFrequency: Int,
        val distanceUnits: String,
        val theme: String,
        val notificationsEnabled: Boolean,
        val autoStartTrip: Boolean,
        val periodMode: String
    )
}

