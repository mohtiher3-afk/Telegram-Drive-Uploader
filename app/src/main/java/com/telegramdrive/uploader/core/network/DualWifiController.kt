package com.telegramdrive.uploader.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Safely observes Wi-Fi networks without making Settings depend on optional
 * vendor-specific concurrent-Wi-Fi APIs. A missing service or registration
 * failure degrades to an empty list instead of crashing the app.
 */
data class WifiNetworkInfo(
    val key: String,
    val network: Network,
    val hasInternet: Boolean,
    val isValidated: Boolean
)

@Singleton
class DualWifiController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)
    private val wifiManager: WifiManager? =
        context.getSystemService(WifiManager::class.java)

    val availableWifiNetworks: Flow<List<WifiNetworkInfo>> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val current = linkedMapOf<String, WifiNetworkInfo>()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        var registered = false
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                publish(network, current)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                publish(network, current, capabilities)
            }

            override fun onLost(network: Network) {
                current.remove(networkKey(network))
                trySend(current.values.toList())
            }

            private fun publish(
                network: Network,
                map: MutableMap<String, WifiNetworkInfo>,
                capabilities: NetworkCapabilities? = runCatching {
                    manager.getNetworkCapabilities(network)
                }.getOrNull()
            ) {
                val caps = capabilities ?: return
                val key = networkKey(network)
                map[key] = WifiNetworkInfo(
                    key = key,
                    network = network,
                    hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                    isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                )
                trySend(map.values.toList())
            }
        }

        try {
            manager.registerNetworkCallback(request, callback)
            registered = true
            trySend(emptyList())
        } catch (_: SecurityException) {
            trySend(emptyList())
            close()
        } catch (_: RuntimeException) {
            // Some vendor builds reject concurrent callbacks or unsupported
            // requests. Settings should remain usable in that case.
            trySend(emptyList())
            close()
        }

        awaitClose {
            if (registered) {
                runCatching { manager.unregisterNetworkCallback(callback) }
            }
        }
    }.distinctUntilChanged()

    fun supportsConcurrentWifi(): Boolean = runCatching {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (wifiManager?.isStaConcurrencyForLocalOnlyConnectionsSupported == true)
    }.getOrDefault(false)

    fun bindToNetwork(network: Network?): Boolean = runCatching {
        connectivityManager?.bindProcessToNetwork(network) ?: false
    }.getOrDefault(false)

    fun clearBinding(): Boolean = bindToNetwork(null)

    private fun networkKey(network: Network): String = network.networkHandle.toString()
}
