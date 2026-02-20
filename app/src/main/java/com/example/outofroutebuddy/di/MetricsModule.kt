package com.example.outofroutebuddy.di

import com.example.outofroutebuddy.workers.DefaultWorkMetricsLogger
import com.example.outofroutebuddy.workers.WorkMetricsLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MetricsModule {

    @Provides
    @Singleton
    fun provideWorkMetricsLogger(): WorkMetricsLogger = DefaultWorkMetricsLogger
}


