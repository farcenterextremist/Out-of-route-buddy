package com.example.outofroutebuddy.presentation.ui.trip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.example.outofroutebuddy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Combined autocomplete adapter: local address history (clock icon) + Photon OSM geocoder (pin icon).
 * Debounces network calls (300ms) and respects Photon's ~1 req/sec fair-use limit.
 */
class AddressSuggestionAdapter(
    private val context: Context,
    private val historyStore: AddressHistoryStore,
    private val scope: CoroutineScope,
) : BaseAdapter(), Filterable {

    private var suggestions: List<SuggestionItem> = emptyList()
    private var photonJob: Job? = null
    private var lastPhotonRequestMs = 0L

    override fun getCount(): Int = suggestions.size
    override fun getItem(position: Int): SuggestionItem = suggestions[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun getAddress(position: Int): String = suggestions[position].address

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val item = suggestions[position]
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = item.address
        tv.textSize = 14f
        tv.maxLines = 2
        val iconRes = if (item.isLocal) android.R.drawable.ic_menu_recent_history
        else android.R.drawable.ic_menu_myplaces
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
        tv.compoundDrawablePadding = 16
        return view
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint?.toString().orEmpty().trim()
            val local = historyStore.matching(query).map { SuggestionItem(it, isLocal = true) }
            return FilterResults().apply {
                values = local
                count = local.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val local = (results?.values as? List<SuggestionItem>).orEmpty()
            suggestions = local
            notifyDataSetChanged()

            val query = constraint?.toString().orEmpty().trim()
            if (query.length >= 3) {
                photonJob?.cancel()
                photonJob = scope.launch {
                    delay(DEBOUNCE_MS)
                    val timeSinceLast = System.currentTimeMillis() - lastPhotonRequestMs
                    if (timeSinceLast < MIN_INTERVAL_MS) {
                        delay(MIN_INTERVAL_MS - timeSinceLast)
                    }
                    val remote = fetchPhoton(query)
                    lastPhotonRequestMs = System.currentTimeMillis()
                    if (remote.isNotEmpty()) {
                        val merged = local + remote.map { SuggestionItem(it, isLocal = false) }
                        withContext(Dispatchers.Main) {
                            suggestions = merged
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchPhoton(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("$PHOTON_BASE?q=$encoded&limit=$PHOTON_LIMIT")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "OutOfRouteBuddy-Android")
            try {
                if (conn.responseCode != 200) return@withContext emptyList()
                val body = conn.inputStream.bufferedReader().readText()
                parsePhotonResults(body)
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parsePhotonResults(json: String): List<String> {
        return try {
            val root = JSONObject(json)
            val features = root.optJSONArray("features") ?: return emptyList()
            val results = mutableListOf<String>()
            for (i in 0 until features.length()) {
                val props = features.getJSONObject(i).optJSONObject("properties") ?: continue
                val parts = mutableListOf<String>()
                props.optString("name", "").takeIf { it.isNotBlank() }?.let { parts.add(it) }
                props.optString("street", "").takeIf { it.isNotBlank() }?.let { parts.add(it) }
                props.optString("city", "").takeIf { it.isNotBlank() }?.let { parts.add(it) }
                props.optString("state", "").takeIf { it.isNotBlank() }?.let { parts.add(it) }
                props.optString("postcode", "").takeIf { it.isNotBlank() }?.let { parts.add(it) }
                if (parts.isNotEmpty()) results.add(parts.joinToString(", "))
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class SuggestionItem(val address: String, val isLocal: Boolean)

    companion object {
        private const val PHOTON_BASE = "https://photon.komoot.io/api/"
        private const val PHOTON_LIMIT = 5
        private const val DEBOUNCE_MS = 300L
        private const val MIN_INTERVAL_MS = 1100L
    }
}
