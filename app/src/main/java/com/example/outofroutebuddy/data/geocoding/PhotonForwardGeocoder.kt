package com.example.outofroutebuddy.data.geocoding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Forward-geocodes a free-text address to WGS84 coordinates using the same Photon endpoint as
 * [com.example.outofroutebuddy.presentation.ui.trip.AddressSuggestionAdapter].
 */
object PhotonForwardGeocoder {

    private const val PHOTON_BASE = "https://photon.komoot.io/api/"
    private const val LIMIT = 1

    /**
     * @return first result's (latitude, longitude), or null on failure / empty results.
     */
    suspend fun geocodeFirstLatLng(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val query = address.trim()
        if (query.isEmpty()) return@withContext null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("${PHOTON_BASE}q=$encoded&limit=$LIMIT")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("User-Agent", "OutOfRouteBuddy-Android")
            try {
                if (conn.responseCode != 200) return@withContext null
                val body = conn.inputStream.bufferedReader().readText()
                parseFirstCoordinates(body)
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFirstCoordinates(json: String): Pair<Double, Double>? {
        return try {
            val root = JSONObject(json)
            val features = root.optJSONArray("features") ?: return null
            if (features.length() == 0) return null
            val geometry = features.getJSONObject(0).optJSONObject("geometry") ?: return null
            val coords = geometry.optJSONArray("coordinates") ?: return null
            if (coords.length() < 2) return null
            val lon = coords.getDouble(0)
            val lat = coords.getDouble(1)
            if (!lat.isFinite() || !lon.isFinite()) return null
            Pair(lat, lon)
        } catch (_: Exception) {
            null
        }
    }
}
