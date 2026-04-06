package com.example.outofroutebuddy.util

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.BuildConfig

/**
 * L1: Structured logging facade with build-type guard.
 * - Debug: all levels (d, w, e) are logged.
 * - Release: only w and e are logged; d is no-op to reduce noise and avoid leaking PII in verbose logs.
 * Callers must not include PII (trip IDs, location coords, user identifiers) in messages in release.
 * See docs/security/SECURITY_PLAN.md for PII guidance.
 */
object AppLogger {

    private const val PREFS_APP_SETTINGS = "app_settings"
    private const val KEY_VERBOSE_LOGGING = "verbose_logging"

    @Volatile
    private var appContext: Context? = null

    /** Call from [android.app.Application.onCreate] so verbose logging can read Settings. */
    @JvmStatic
    fun initApplicationContext(context: Context) {
        appContext = context.applicationContext
    }

    private fun isVerboseLoggingEnabled(): Boolean {
        val ctx = appContext ?: return false
        return ctx.getSharedPreferences(PREFS_APP_SETTINGS, Context.MODE_PRIVATE)
            .getBoolean(KEY_VERBOSE_LOGGING, false)
    }

    /**
     * Debug-level log. No-op in release to avoid PII leakage in verbose paths.
     */
    @JvmStatic
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    /**
     * Verbose-level log. In debug builds, only emitted when **Verbose logging** is on in Settings
     * (reduces Logcat noise). No-op in release.
     */
    @JvmStatic
    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG && isVerboseLoggingEnabled()) {
            Log.v(tag, message)
        }
    }

    /**
     * Warn-level log. Logged in all build types. Do not include PII in [message].
     */
    @JvmStatic
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, sanitizeForRelease(message), throwable)
        } else {
            Log.w(tag, sanitizeForRelease(message))
        }
    }

    /**
     * Error-level log. Logged in all build types. Do not include PII in [message].
     */
    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, sanitizeForRelease(message), throwable)
        } else {
            Log.e(tag, sanitizeForRelease(message))
        }
    }

    /**
     * In release, redact obvious numeric IDs to reduce PII in logs. In debug, return as-is.
     */
    @JvmStatic
    private fun sanitizeForRelease(message: String): String {
        return if (BuildConfig.DEBUG) message
        else message.replace(Regex("""(trip_?id|id)\s*=\s*\d+""", RegexOption.IGNORE_CASE), "$1=***")
    }
}
