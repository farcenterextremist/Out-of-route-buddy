package com.example.outofroutebuddy.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [WorkManagerInitializer] (Phase 4 coverage).
 * Verifies initialize() and cancelAll() run without throwing; work is enqueued then cleared.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkManagerInitializerTest {

    private lateinit var context: Context
    private lateinit var initializer: WorkManagerInitializer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        } catch (e: IllegalStateException) {
            // Already initialized; proceed
        }
        initializer = WorkManagerInitializer(context)
    }

    @Test
    fun initialize_doesNotThrow() {
        initializer.initialize()
        // No exception; SyncWorkerRobolectricTest covers work enqueue details
    }

    @Test
    fun cancelAll_doesNotThrow() {
        initializer.initialize()
        initializer.cancelAll()
        // No exception
    }

    @Test
    fun getConfiguration_returnsNonNull() {
        val config = initializer.getConfiguration()
        assertThat(config).isNotNull()
    }
}
