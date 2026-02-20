package com.example.outofroutebuddy.workers

import android.content.Context
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Lightweight metrics hook for WorkManager jobs. Optional and non-UI.
 */
interface WorkMetricsLogger {
    fun onWorkStart(type: String)
    fun onWorkSuccess(type: String, durationMs: Long)
    fun onWorkFailure(type: String, attempt: Int, error: Throwable?)
}

/**
 * Default no-op logger that logs to Logcat. Can be swapped with a real metrics sink.
 */
object DefaultWorkMetricsLogger : WorkMetricsLogger {
    private const val TAG = "WorkMetrics"

    override fun onWorkStart(type: String) {
        Log.d(TAG, "start type=$type")
    }

    override fun onWorkSuccess(type: String, durationMs: Long) {
        Log.i(TAG, "success type=$type durationMs=$durationMs")
    }

    override fun onWorkFailure(type: String, attempt: Int, error: Throwable?) {
        Log.w(TAG, "failure type=$type attempt=$attempt error=${error?.message}", error)
    }
}

/**
 * Simple provider; later can be backed by DI.
 */
object WorkMetricsLoggerProvider {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkMetricsLoggerEntryPoint {
        fun logger(): WorkMetricsLogger
    }

    fun get(context: Context): WorkMetricsLogger = try {
        EntryPointAccessors.fromApplication(context, WorkMetricsLoggerEntryPoint::class.java).logger()
    } catch (t: Throwable) {
        // Hilt not initialized or no binding; fallback to default
        DefaultWorkMetricsLogger
    }
}


