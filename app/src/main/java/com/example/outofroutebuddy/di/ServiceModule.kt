package com.example.outofroutebuddy.di

import android.content.Context
import com.example.outofroutebuddy.data.NetworkStateManager
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
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

    // --- LEGACY SERVICES (COMMENTED OUT) ---
    // These services have been replaced by the unified services above
    // They are kept for reference but should be removed once migration is complete
    
    // @Provides
    // @Singleton
    // fun provideGpsSynchronizationService(
    //     tripStateManager: TripStateManager,
    //     @ApplicationContext context: Context,
    // ): GpsSynchronizationService = GpsSynchronizationService(tripStateManager, context)
    //
    // @Provides
    // @Singleton
    // fun provideTripTrackingCoordinator(
    //     @ApplicationContext context: Context,
    //     tripStateManager: TripStateManager,
    //     gpsSynchronizationService: GpsSynchronizationService,
    // ): TripTrackingCoordinator = TripTrackingCoordinator(context, tripStateManager, gpsSynchronizationService)
    //
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
    // @Provides
    // @Singleton
    // fun provideOfflineSyncService(): OfflineSyncService = OfflineSyncService()
    //
    // @Provides
    // @Singleton
    // fun provideSimpleOfflineService(@ApplicationContext context: Context): SimpleOfflineService = SimpleOfflineService(context)
    //
    // @Provides
    // @Singleton
    // fun provideStandaloneOfflineService(@ApplicationContext context: Context): StandaloneOfflineService = StandaloneOfflineService.getInstance(context)
    //
    // @Provides
    // @Singleton
    // fun provideOfflineServiceCoordinator(
    //     @ApplicationContext context: Context,
    //     networkStateManager: NetworkStateManager,
    //     offlineDataManager: OfflineDataManager,
    //     preferencesManager: PreferencesManager,
    //     offlineSyncService: OfflineSyncService,
    //     simpleOfflineService: SimpleOfflineService,
    //     standaloneOfflineService: StandaloneOfflineService
    // ): OfflineServiceCoordinator = OfflineServiceCoordinator(context, networkStateManager, offlineDataManager, preferencesManager, offlineSyncService, simpleOfflineService, standaloneOfflineService)
} 
