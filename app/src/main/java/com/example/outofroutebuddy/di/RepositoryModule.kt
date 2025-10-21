package com.example.outofroutebuddy.di

import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.data.repository.DomainTripRepositoryAdapter
import com.example.outofroutebuddy.data.repository.TripRepository as DataTripRepository
import com.example.outofroutebuddy.domain.repository.TripRepository as DomainTripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * ✅ SIMPLIFIED: Repository Module for Dependency Injection
 * 
 * This module provides repository implementations for the OutOfRouteBuddy app.
 * 
 * 🔧 CHANGES MADE:
 * - Simple and direct dependency injection
 * - No complex adapters
 * - Singleton pattern for shared state
 * - Easy testing with mock implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    /**
     * ✅ SIMPLIFIED: Provides data layer TripRepository
     */
    @Provides
    @Singleton
    fun provideDataTripRepository(tripDao: TripDao): DataTripRepository {
        return DataTripRepository(tripDao)
    }

    /**
     * ✅ FIXED: Provides domain layer TripRepository with proper adapter
     */
    @Provides
    @Singleton
    fun provideDomainTripRepository(dataRepository: DataTripRepository): DomainTripRepository {
        return DomainTripRepositoryAdapter(dataRepository)
    }

    /**
     * ✅ FIXED: Provides TripStateManager with correct constructor
     */
    @Provides
    @Singleton
    fun provideTripStateManager(preferencesManager: PreferencesManager): TripStateManager {
        return TripStateManager(preferencesManager)
    }

    /**
     * ✅ Provides TripStatePersistence with correct dependencies
     */
    @Provides
    @Singleton
    fun provideTripStatePersistence(
        repository: DataTripRepository,
        tripStateManager: TripStateManager,
        coroutineScope: CoroutineScope,
    ): TripStatePersistence = TripStatePersistence(repository, tripStateManager, coroutineScope)

    /**
     * ✅ NEW: Provides CoroutineScope for background operations
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
} 
