package com.example.outofroutebuddy.presentation.ui.trip

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/**
 * Persists recently-entered pickup/dropoff addresses for autocomplete suggestions.
 * Stores up to [MAX_ENTRIES] unique addresses in a SharedPreferences-backed JSON array.
 */
class AddressHistoryStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRecent(): List<String> {
        val raw = prefs.getString(KEY_ADDRESSES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(address: String) {
        val trimmed = address.trim()
        if (trimmed.isBlank()) return
        val current = getRecent().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        if (current.size > MAX_ENTRIES) current.subList(MAX_ENTRIES, current.size).clear()
        val arr = JSONArray()
        current.forEach { arr.put(it) }
        prefs.edit().putString(KEY_ADDRESSES, arr.toString()).apply()
    }

    fun matching(query: String): List<String> {
        if (query.isBlank()) return getRecent()
        val q = query.lowercase()
        return getRecent().filter { it.lowercase().contains(q) }
    }

    companion object {
        private const val PREFS_NAME = "address_history"
        private const val KEY_ADDRESSES = "recent_addresses"
        private const val MAX_ENTRIES = 20
    }
}
