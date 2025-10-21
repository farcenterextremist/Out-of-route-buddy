package com.example.outofroutebuddy.di

import android.content.Context
import com.example.outofroutebuddy.data.AppDatabase
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.data.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ✅ FIXED: Hilt Database Module
 *
 * This module provides database-related dependencies:
 * - AppDatabase instance
 * - TripDao for database operations
 * - PreferencesManager for app preferences
 * - StateCache for in-memory state
 *
 * Benefits:
 * - Clean separation of database concerns
 * - Singleton pattern for database instance
 * - Easy testing with mock implementations
 * - Proper lifecycle management
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * ✅ FIXED: Provides AppDatabase instance using correct method name
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * ✅ FIXED: Provides TripDao for database operations
     */
    @Provides
    @Singleton
    fun provideTripDao(database: AppDatabase): TripDao {
        return database.tripDao()
    }

    /**
     * ✅ NEW: Provides PreferencesManager for app preferences
     */
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    /**
     * ✅ NEW: Provides StateCache for in-memory state
     */
    @Provides
    @Singleton
    fun provideStateCache(): StateCache {
        return StateCache()
    }
} 
