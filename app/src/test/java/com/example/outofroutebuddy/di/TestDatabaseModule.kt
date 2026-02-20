package com.example.outofroutebuddy.di

import android.content.Context
import androidx.room.Room
import com.example.outofroutebuddy.data.AppDatabase
import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.data.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test replacement for production DatabaseModule.
 * Uses an in-memory Room database to speed up and stabilize tests.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDb(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideStateCache(): StateCache = StateCache()
}


