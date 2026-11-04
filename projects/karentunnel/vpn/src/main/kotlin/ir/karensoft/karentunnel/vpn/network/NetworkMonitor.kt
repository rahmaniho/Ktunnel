package ir.karensoft.karentunnel.vpn.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Snapshot of the physical connectivity situation, ignoring our own VPN. */
data class NetworkSnapshot(
    val hasInternet: Boolean,
    val transports: Set<Transport>,
    /** True when the system reports an active VPN network (ours or another app's). */
    val hasVpnNetwork: Boolean,
) {
    enum class Transport { WIFI, CELLULAR, ETHERNET, OTHER, NONE }

    val isUsable: Boolean get() = hasInternet && transports != setOf(Transport.NONE)
}

/**
 * Watches connectivity and emits a snapshot on every meaningful change.
 *
 * We deliberately observe *capabilities* rather than just availability: a
 * Wi-Fi network with no internet is not a usable handover target, and treating
 * it as one is a common source of reconnect churn in the wild.
 *
 * The flow never throws; a missing ConnectivityManager yields a single
 * "no internet" snapshot so callers can keep their state machine simple.
 */
class NetworkMonitor(context: Context) {

    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun snapshots(): Flow<NetworkSnapshot> = callbackFlow {
        val cm = manager
        if (cm == null) {
            trySend(NetworkSnapshot(false, setOf(NetworkSnapshot.Transport.NONE), false))
            awaitClose { }
            return@callbackFlow
        }

        fun current(): NetworkSnapshot {
            val active = cm.activeNetwork
            val caps = active?.let(cm::getNetworkCapabilities)
            val transports = buildSet {
                when {
                    caps == null -> add(NetworkSnapshot.Transport.NONE)
                    else -> {
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            add(NetworkSnapshot.Transport.WIFI)
                        }
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            add(NetworkSnapshot.Transport.CELLULAR)
                        }
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            add(NetworkSnapshot.Transport.ETHERNET)
                        }
                        if (isEmpty()) {
                            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                                add(NetworkSnapshot.Transport.OTHER)
                            } else {
                                add(NetworkSnapshot.Transport.NONE)
                            }
                        }
                    }
                }
            }
            val internet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            return NetworkSnapshot(
                hasInternet = internet,
                transports = transports,
                hasVpnNetwork = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            )
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(current())
            }

            override fun onLost(network: Network) {
                trySend(current())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(current())
            }
        }

        // Match only non-VPN transports: a VPN network showing up must not be
        // mistaken for the physical link being healthy.
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (e: SecurityException) {
            trySend(current())
            awaitClose { }
            return@callbackFlow
        }

        trySend(current())
        awaitClose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }.distinctUntilChanged()
}
