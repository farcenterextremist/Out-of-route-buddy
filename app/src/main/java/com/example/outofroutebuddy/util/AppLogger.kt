package com.example.outofroutebuddy.util

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
     * Verbose-level log. No-op in release. Same as d() for PII safety.
     */
    @JvmStatic
    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
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
