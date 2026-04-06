package com.example.outofroutebuddy.di

import com.example.outofroutebuddy.data.SettingsManager
import com.example.outofroutebuddy.services.BatteryOptimizationService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hilt entry point so [com.example.outofroutebuddy.services.TripTrackingService] can read settings without @AndroidEntryPoint. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TripTrackingServiceEntryPoint {
    fun settingsManager(): SettingsManager
    fun batteryOptimizationService(): BatteryOptimizationService
}
