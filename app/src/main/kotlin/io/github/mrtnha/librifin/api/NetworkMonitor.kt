package io.github.mrtnha.librifin.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The phone's network: whether it has one at all, and when it changes, e.g. from Wi-Fi to mobile data.
 * Says nothing about whether the server can be reached on it.
 */
class NetworkMonitor(context: Context) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    /** The network the phone uses right now, or null if it has none. */
    val current: Network? get() = connectivity.activeNetwork

    /**
     * The network the phone uses, first the current one and then each time it changes; null while it has none.
     * Android is only asked to report changes while this is collected.
     */
    fun defaultNetwork(): Flow<Network?> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            // A new network replaces the old one, e.g. mobile data after Wi-Fi was turned off.
            override fun onAvailable(network: Network) {
                trySend(network)
            }

            // Only called for this kind of callback when no other network takes over: the phone has none.
            override fun onLost(network: Network) {
                trySend(null)
            }
        }
        trySend(connectivity.activeNetwork)
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
