package com.example.outofroutebuddy.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * ✅ NEW: Network State Manager for Offline Data Handling
 *
 * This class monitors network connectivity and manages offline state
 * to ensure the app works reliably even without internet connection.
 */
class NetworkStateManager(private val context: Context) {
    companion object {
        private const val TAG = "NetworkStateManager"
    }

    // ✅ NETWORK STATE: Current connectivity status
    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    // ✅ OFFLINE QUEUE: Pending operations when offline
    private val _offlineQueue = MutableStateFlow(OfflineQueue())
    val offlineQueue: StateFlow<OfflineQueue> = _offlineQueue.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // ✅ NEW: Network state data class
    data class NetworkState(
        val isConnected: Boolean = false,
        val networkType: NetworkType = NetworkType.NONE,
        val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN,
        val lastConnected: Date? = null,
        val lastDisconnected: Date? = null,
        val isMetered: Boolean = false,
        val isRoaming: Boolean = false,
    )

    // ✅ NEW: Network type enumeration
    enum class NetworkType {
        NONE,
        WIFI,
        CELLULAR,
        ETHERNET,
        VPN,
        UNKNOWN,
    }

    // ✅ NEW: Connection quality enumeration
    enum class ConnectionQuality {
        UNKNOWN,
        POOR,
        FAIR,
        GOOD,
        EXCELLENT,
    }

    // ✅ NEW: Offline queue for pending operations
    data class OfflineQueue(
        val pendingTrips: List<PendingTrip> = emptyList(),
        val pendingSyncs: List<PendingSync> = emptyList(),
        val pendingAnalytics: List<PendingAnalytics> = emptyList(),
        val lastSyncAttempt: Date? = null,
        val syncAttempts: Int = 0,
    )

    // ✅ NEW: Pending trip data
    data class PendingTrip(
        val id: String,
        val tripData: Map<String, Any>,
        val timestamp: Date,
        val retryCount: Int = 0,
    )

    // ✅ NEW: Pending sync data
    data class PendingSync(
        val type: String,
        val data: Map<String, Any>,
        val timestamp: Date,
        val priority: Int = 1,
    )

    // ✅ NEW: Pending analytics data
    data class PendingAnalytics(
        val event: String,
        val parameters: Map<String, Any>,
        val timestamp: Date,
    )

    init {
        // ✅ INITIALIZE: Set up network monitoring
        setupNetworkMonitoring()
        updateNetworkState()
    }

    /**
     * ✅ NEW: Set up network connectivity monitoring
     */
    private fun setupNetworkMonitoring() {
        try {
            val networkRequest =
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .build()

            networkCallback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "Network became available")
                        updateNetworkState()
                    }

                    override fun onLost(network: Network) {
                        Log.w(TAG, "Network connection lost")
                        updateNetworkState()
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        Log.d(TAG, "Network capabilities changed")
                        updateNetworkState()
                    }

                    override fun onUnavailable() {
                        Log.w(TAG, "Network unavailable")
                        updateNetworkState()
                    }
                }

            val callback = networkCallback ?: throw IllegalStateException("NetworkCallback was not initialized")
            connectivityManager.registerNetworkCallback(networkRequest, callback)
            Log.d(TAG, "Network monitoring initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up network monitoring", e)
        }
    }

    /**
     * ✅ NEW: Start network monitoring
     */
    fun startMonitoring() {
        try {
            if (networkCallback == null) {
                setupNetworkMonitoring()
            }
            updateNetworkState()
            Log.d(TAG, "Network monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start network monitoring", e)
        }
    }

    /**
     * ✅ NEW: Stop network monitoring
     */
    fun stopMonitoring() {
        try {
            networkCallback?.let { callback ->
                connectivityManager.unregisterNetworkCallback(callback)
                networkCallback = null
            }
            Log.d(TAG, "Network monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop network monitoring", e)
        }
    }

    /**
     * ✅ NEW: Update current network state
     */
    private fun updateNetworkState() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val networkType = getNetworkType(networkCapabilities)
            val connectionQuality = getConnectionQuality(networkCapabilities)
            val isMetered = connectivityManager.isActiveNetworkMetered
            val isRoaming = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) != true

            val currentState = _networkState.value
            val newState =
                NetworkState(
                    isConnected = isConnected,
                    networkType = networkType,
                    connectionQuality = connectionQuality,
                    lastConnected = if (isConnected && !currentState.isConnected) Date() else currentState.lastConnected,
                    lastDisconnected = if (!isConnected && currentState.isConnected) Date() else currentState.lastDisconnected,
                    isMetered = isMetered,
                    isRoaming = isRoaming,
                )

            _networkState.value = newState

            // ✅ HANDLE: Network state changes
            handleNetworkStateChange(currentState, newState)

            Log.d(TAG, "Network state updated: connected=$isConnected, type=$networkType, quality=$connectionQuality")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update network state", e)
        }
    }

    /**
     * ✅ NEW: Get network type from capabilities
     */
    private fun getNetworkType(capabilities: NetworkCapabilities?): NetworkType {
        return when {
            capabilities == null -> NetworkType.NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * ✅ NEW: Get connection quality from capabilities
     */
    private fun getConnectionQuality(capabilities: NetworkCapabilities?): ConnectionQuality {
        if (capabilities == null) return ConnectionQuality.UNKNOWN

        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED) &&
                capabilities.linkDownstreamBandwidthKbps > 10000 -> ConnectionQuality.EXCELLENT
            capabilities.linkDownstreamBandwidthKbps > 5000 -> ConnectionQuality.GOOD
            capabilities.linkDownstreamBandwidthKbps > 1000 -> ConnectionQuality.FAIR
            else -> ConnectionQuality.POOR
        }
    }

    /**
     * ✅ NEW: Handle network state changes
     */
    private fun handleNetworkStateChange(
        oldState: NetworkState,
        newState: NetworkState,
    ) {
        when {
            // Network became available
            !oldState.isConnected && newState.isConnected -> {
                Log.i(TAG, "Network restored - processing offline queue")
                processOfflineQueue()
            }
            // Network became unavailable
            oldState.isConnected && !newState.isConnected -> {
                Log.w(TAG, "Network lost - switching to offline mode")
                switchToOfflineMode()
            }
        }
    }

    /**
     * ✅ NEW: Switch to offline mode
     */
    private fun switchToOfflineMode() {
        Log.i(TAG, "Switching to offline mode")
        // The app will continue to work with local data
        // All operations will be queued for later sync
    }

    /**
     * ✅ NEW: Process offline queue when network is restored
     */
    private fun processOfflineQueue() {
        val queue = _offlineQueue.value

        if (queue.pendingTrips.isNotEmpty() || queue.pendingSyncs.isNotEmpty() || queue.pendingAnalytics.isNotEmpty()) {
            Log.i(
                TAG,
                "Processing offline queue: ${queue.pendingTrips.size} trips, ${queue.pendingSyncs.size} syncs, ${queue.pendingAnalytics.size} analytics",
            )

            // Process offline queue (implementation pending)
            // This would involve syncing trips, analytics, and other pending operations
        }
    }

    /**
     * ✅ NEW: Add trip to offline queue
     */
    fun addTripToOfflineQueue(tripData: Map<String, Any>): String {
        val tripId = "trip_${System.currentTimeMillis()}"
        val pendingTrip =
            PendingTrip(
                id = tripId,
                tripData = tripData,
                timestamp = Date(),
            )

        val currentQueue = _offlineQueue.value
        val newQueue =
            currentQueue.copy(
                pendingTrips = currentQueue.pendingTrips + pendingTrip,
            )

        _offlineQueue.value = newQueue
        Log.d(TAG, "Added trip to offline queue")

        return tripId
    }

    /**
     * ✅ NEW: Add sync operation to offline queue
     */
    fun addSyncToOfflineQueue(
        type: String,
        data: Map<String, Any>,
        priority: Int = 1,
    ) {
        val pendingSync =
            PendingSync(
                type = type,
                data = data,
                timestamp = Date(),
                priority = priority,
            )

        val currentQueue = _offlineQueue.value
        val newQueue =
            currentQueue.copy(
                pendingSyncs = currentQueue.pendingSyncs + pendingSync,
            )

        _offlineQueue.value = newQueue
        Log.d(TAG, "Added sync to offline queue: $type")
    }

    /**
     * ✅ NEW: Add analytics to offline queue
     */
    fun addAnalyticsToOfflineQueue(
        event: String,
        parameters: Map<String, Any>,
    ) {
        val pendingAnalytics =
            PendingAnalytics(
                event = event,
                parameters = parameters,
                timestamp = Date(),
            )

        val currentQueue = _offlineQueue.value
        val newQueue =
            currentQueue.copy(
                pendingAnalytics = currentQueue.pendingAnalytics + pendingAnalytics,
            )

        _offlineQueue.value = newQueue
        Log.d(TAG, "Added analytics to offline queue: $event")
    }

    /**
     * ✅ NEW: Check if currently offline
     */
    fun isOffline(): Boolean = !_networkState.value.isConnected

    /**
     * ✅ NEW: Check if network is available
     */
    fun isNetworkAvailable(): Boolean = _networkState.value.isConnected

    /**
     * ✅ NEW: Get current network type
     */
    fun getCurrentNetworkType(): NetworkType = _networkState.value.networkType

    /**
     * ✅ NEW: Get connection quality
     */
    fun getConnectionQuality(): ConnectionQuality = _networkState.value.connectionQuality

    /**
     * ✅ NEW: Get offline queue size
     */
    fun getOfflineQueueSize(): Int {
        val queue = _offlineQueue.value
        return queue.pendingTrips.size + queue.pendingSyncs.size + queue.pendingAnalytics.size
    }

    /**
     * ✅ NEW: Clear offline queue (use with caution)
     */
    fun clearOfflineQueue() {
        _offlineQueue.value = OfflineQueue()
        Log.w(TAG, "Offline queue cleared")
    }

    /**
     * ✅ NEW: Cleanup resources
     */
    fun cleanup() {
        try {
            networkCallback?.let { callback ->
                connectivityManager.unregisterNetworkCallback(callback)
            }
            Log.d(TAG, "Network monitoring cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up network monitoring", e)
        }
    }
} 
