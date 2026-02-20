package com.example.outofroutebuddy.di

import android.content.Context
import com.example.outofroutebuddy.data.NetworkStateManager
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.services.BackgroundSyncService
import com.example.outofroutebuddy.services.HealthCheckManager
import com.example.outofroutebuddy.services.OptimizedGpsDataFlow
import com.example.outofroutebuddy.services.PeriodCalculationService
import com.example.outofroutebuddy.services.TripCrashRecoveryManager
import com.example.outofroutebuddy.services.UnifiedLocationService
import com.example.outofroutebuddy.services.UnifiedOfflineService
import com.example.outofroutebuddy.services.UnifiedTripService
import com.example.outofroutebuddy.validation.ValidationFramework
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ✅ SIMPLIFIED: Hilt Service Module - Using Unified Services
 * 
 * This module provides the new unified services that combine multiple
 * related services into single, focused classes for better maintainability.
 * 
 * Benefits:
 * - Reduced number of dependencies
 * - Simplified dependency injection
 * - Easier to test and mock
 * - Better separation of concerns
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    /**
     * ✅ UNIFIED: Provides UnifiedLocationService (replaces multiple location services)
     */
    @Provides
    @Singleton
    fun provideUnifiedLocationService(
        @ApplicationContext context: Context,
        tripStateManager: TripStateManager,
    ): UnifiedLocationService = UnifiedLocationService(context, tripStateManager)

    /**
     * ✅ UNIFIED: Provides UnifiedTripService (replaces multiple trip services)
     */
    @Provides
    @Singleton
    fun provideUnifiedTripService(
        @ApplicationContext context: Context,
        tripStateManager: TripStateManager,
        periodCalculationService: PeriodCalculationService,
    ): UnifiedTripService = UnifiedTripService(context, tripStateManager, periodCalculationService)

    /**
     * ✅ UNIFIED: Provides UnifiedOfflineService (replaces multiple offline services)
     */
    @Provides
    @Singleton
    fun provideUnifiedOfflineService(
        @ApplicationContext context: Context,
        networkStateManager: NetworkStateManager,
        preferencesManager: PreferencesManager,
    ): UnifiedOfflineService = UnifiedOfflineService(context, networkStateManager, preferencesManager)

    /**
     * ✅ MINIMAL: Provides OptimizedGpsDataFlow (no dependencies)
     */
    @Provides
    @Singleton
    fun provideOptimizedGpsDataFlow(): OptimizedGpsDataFlow = OptimizedGpsDataFlow()

    /**
     * ✅ MINIMAL: Provides BackgroundSyncService (Android Service - no constructor args)
     */
    @Provides
    @Singleton
    fun provideBackgroundSyncService(): BackgroundSyncService = BackgroundSyncService()

    /**
     * ✅ MINIMAL: Provides ValidationFramework (object - no constructor needed)
     */
    @Provides
    @Singleton
    fun provideValidationFramework(): ValidationFramework = ValidationFramework

    /**
     * ✅ NEW: Provides NetworkStateManager for network connectivity monitoring
     */
    @Provides
    @Singleton
    fun provideNetworkStateManager(
        @ApplicationContext context: Context,
    ): NetworkStateManager = NetworkStateManager(context)

    @Provides
    @Singleton
    fun providePeriodCalculationService(): PeriodCalculationService = PeriodCalculationService()

    /**
     * ✅ NEW: Provides TripCrashRecoveryManager for automatic crash recovery
     */
    @Provides
    @Singleton
    fun provideTripCrashRecoveryManager(
        @ApplicationContext context: Context
    ): TripCrashRecoveryManager = TripCrashRecoveryManager(context)

    /**
     * ✅ NEW (#21): Provides HealthCheckManager for service monitoring
     */
    @Provides
    @Singleton
    fun provideHealthCheckManager(
        @ApplicationContext context: Context
    ): HealthCheckManager = HealthCheckManager(context)

    /**
     * ✅ NEW: Provides TripPersistenceManager for trip recovery across app restarts
     */
    @Provides
    @Singleton
    fun provideTripPersistenceManager(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): TripPersistenceManager = TripPersistenceManager(context, preferencesManager)
} 
