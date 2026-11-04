package ir.karensoft.karentunnel.di

import android.content.Context
import ir.karensoft.karentunnel.data.secure.FileSecureConfigStore
import ir.karensoft.karentunnel.data.secure.KeystoreCryptoEngine
import ir.karensoft.karentunnel.data.secure.SecureConfigStore
import ir.karensoft.karentunnel.diagnostics.ProbeEngine
import ir.karensoft.karentunnel.vpn.connection.HealthProbe
import ir.karensoft.karentunnel.vpn.controller.WireGuardTunnelController
import ir.karensoft.karentunnel.vpn.network.NetworkMonitor
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Explicit object graph. Constructed once in [ir.karensoft.karentunnel.KarenTunnelApp].
 *
 * Security-relevant choices visible here:
 *  * the only [SecureConfigStore] implementation is the encrypting one — there
 *    is no "plaintext fallback" constructor to accidentally wire in;
 *  * the tunnel controller is created with the application context, never an
 *    Activity, so a leaked Activity cannot keep the tunnel alive;
 *  * the scope is a SupervisorJob: one failing child (e.g. a probe) cannot
 *    cancel the connection manager.
 */
class AppContainer(
    context: Context,
    val crypto: KeystoreCryptoEngine,
) {
    val applicationContext: Context = context.applicationContext

    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val configStore: SecureConfigStore = FileSecureConfigStore(
        baseDir = File(applicationContext.filesDir, "tunnels"),
        crypto = crypto,
    )

    val tunnelController = WireGuardTunnelController(applicationContext)

    val networkMonitor = NetworkMonitor(applicationContext)

    val probeEngine = ProbeEngine()

    /**
     * Reports whether the system currently exposes a VPN network. We use the
     * platform's own view rather than trusting our state variable: if the OS says
     * there is no VPN network, our claim of "connected" would be a lie.
     */
    val healthProbe: HealthProbe = object : HealthProbe {
        override suspend fun systemVpnNetworkPresent(): Boolean =
            // Bounded, single emission: a hung connectivity callback must never
            // stall the connection state machine.
            withTimeoutOrNull(1_000) {
                runCatching { networkMonitor.snapshots().first().hasVpnNetwork }.getOrDefault(false)
            } ?: false
    }
}
