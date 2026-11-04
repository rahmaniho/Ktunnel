package ir.karensoft.karentunnel.vpn.controller

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.BackendException
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.core.model.Ipv6Policy
import ir.karensoft.karentunnel.core.model.TunnelConfig
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.UnknownHostException

/**
 * The real tunnel, on the official WireGuard library. This class is the only
 * place in the app allowed to talk to `com.wireguard.android`.
 *
 * Design notes that matter (see docs/TECHNICAL-FEASIBILITY.md):
 *
 * * F1 — we never page the library's `allowFamily` behaviour in from our side;
 *   safety is achieved by the *content* of the config we hand over. The guard in
 *   [buildWgQuickString] asserts that an IPv6-excluded config emits no v6 route,
 *   and the ConfigNormalizer already refuses partial-v6 configs.
 * * F2 — the caller (ConnectionManager) must start our foreground service
 *   *before* calling [up]; GoBackend starts its own VpnService internally, and
 *   without an FGS the process can be frozen while the tunnel still exists.
 * * F3 — [status] returning Up is necessary but not sufficient; the caller must
 *   also see a fresh handshake before claiming a connection.
 *
 * Sensitive material: the private key is handed to the library as a string
 * because that is the library's only ingestion path (`Config.parse`). It is
 * never logged, never placed in an exception message, and the intermediate
 * string is not retained by this class.
 */
class WireGuardTunnelController(
    context: Context,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) : TunnelController {

    private val appContext = context.applicationContext
    private val backend: Backend by lazy { GoBackend(appContext) }

    /** One library Tunnel instance is reused; it carries no config of its own. */
    private val tunnel: Tunnel = object : Tunnel {
        override fun getName(): String = TUNNEL_NAME
        override fun onStateChange(newState: Tunnel.State) {
            Log.i(TAG, "backend state -> $newState")
        }
    }

    @Volatile
    private var active: Boolean = false

    @Volatile
    private var upSince: Long? = null

    override fun isActive(): Boolean = active

    override suspend fun backendVersion(): String =
        try {
            backend.version ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

    override suspend fun up(config: TunnelConfig): Result<Unit> {
        if (active) {
            // Idempotent: bringing up an already-up tunnel is a no-op, but we
            // make it visible instead of silently returning success.
            Log.i(TAG, "up() called while already active; ignoring")
            return Result.success(Unit)
        }

        // The generated wg-quick text embeds the private key, so it is kept as a
        // CharArray that can be zeroed. String.fill() does not exist and strings
        // cannot be wiped at all.
        val wgQuick: CharArray = buildWgQuickString(config).toCharArray()
        val parsed = try {
            Config.parse(ByteArrayInputStream(wgQuick.concatToString().toByteArray(Charsets.UTF_8)))
        } catch (e: BadConfigException) {
            return Result.failure(ConnectionError.TunnelSetupFailed("invalid generated config"))
        } catch (e: IOException) {
            return Result.failure(ConnectionError.TunnelSetupFailed("config read failed"))
        } finally {
            // Do not keep the plaintext config around longer than needed.
            wgQuick.fill('\u0000')
        }

        return try {
            backend.setState(tunnel, Tunnel.State.UP, parsed)
            // The library returns after establishing the TUN device and starting
            // the userspace backend; a *handshake* still needs to happen, which
            // the caller verifies before claiming Connected.
            active = true
            upSince = clock()
            Result.success(Unit)
        } catch (e: BackendException) {
            active = false
            Result.failure(mapBackendException(e))
        } catch (e: UnknownHostException) {
            active = false
            Result.failure(ConnectionError.DnsResolutionFailed(config.endpointHost))
        } catch (e: Exception) {
            active = false
            Result.failure(ConnectionError.Transient(e.javaClass.simpleName))
        }
    }

    override suspend fun down(): Result<Unit> {
        if (!active) {
            return Result.success(Unit) // idempotent
        }
        return try {
            backend.setState(tunnel, Tunnel.State.DOWN, null)
            active = false
            upSince = null
            Result.success(Unit)
        } catch (e: Exception) {
            // Even on failure the interface may be gone; report it but do not
            // claim we are still protected.
            active = false
            upSince = null
            Result.failure(ConnectionError.Transient(e.javaClass.simpleName))
        }
    }

    override suspend fun status(): TunnelStatus =
        try {
            if (backend.getState(tunnel) == Tunnel.State.UP) TunnelStatus.Up else TunnelStatus.Down
        } catch (e: Exception) {
            TunnelStatus.Down
        }

    override suspend fun statistics(): TunnelStatistics {
        if (!active) return TunnelStatistics.EMPTY
        return try {
            // getStatistics(Tunnel) takes a parameter, so it is a method, not a
            // property (unlike getVersion()). Verified against Backend.java.
            val stats = backend.getStatistics(tunnel)
            // Statistics.PeerStats is a Java record: components are read as
            // properties from Kotlin, not as getter methods.
            val peer = stats.peers().firstOrNull()?.let { stats.peer(it) }
            val age = peer?.latestHandshakeEpochMillis?.takeIf { it > 0 }?.let {
                // latestHandshakeEpochMillis is wall-clock; convert to an age.
                (System.currentTimeMillis() - it).coerceAtLeast(0)
            }
            TunnelStatistics(
                rxBytes = stats.totalRx(),
                txBytes = stats.totalTx(),
                lastHandshakeAgeMillis = age,
            )
        } catch (e: Exception) {
            TunnelStatistics.EMPTY
        }
    }

    /**
     * Renders the subset of wg-quick syntax the library accepts, from our
     * validated model. This is deliberately hand-rolled instead of calling
     * `Config.toWgQuickString()` on a partially-built object, so that we can
     * guarantee the F1 invariant at the exact point where bytes become intent.
     *
     * The returned string contains the private key: callers must not log it.
     */
    private fun buildWgQuickString(config: TunnelConfig): String {
        val v6Routes = config.allowedIps.filter { it.contains(':') }
        // F1 guard: an EXCLUDED policy must never emit an IPv6 route.
        check(config.ipv6Policy != Ipv6Policy.EXCLUDED || v6Routes.isEmpty()) {
            "IPv6 route present while policy is EXCLUDED"
        }
        check(config.dnsServers.isNotEmpty()) { "DNS must be normalised before tunnel start" }

        return buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = ${config.privateKey.use()}")
            appendLine("Address = ${config.address}")
            appendLine("DNS = ${config.dnsServers.joinToString(", ")}")
            appendLine("MTU = ${config.mtu}")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = ${config.peerPublicKey}")
            config.presharedKey?.let { appendLine("PresharedKey = ${it.use()}") }
            appendLine("AllowedIPs = ${config.allowedIps.joinToString(", ")}")
            appendLine("Endpoint = ${config.endpointHost}:${config.endpointPort}")
            config.persistentKeepaliveSeconds?.let {
                appendLine("PersistentKeepalive = $it")
            }
        }
    }

    private fun mapBackendException(e: BackendException): ConnectionError =
        when (e.getReason()) {
            BackendException.Reason.DNS_RESOLUTION_FAILURE ->
                ConnectionError.DnsResolutionFailed(e.message?.substringAfterLast(' ') ?: "")
            BackendException.Reason.VPN_NOT_AUTHORIZED ->
                ConnectionError.PermissionRequired
            BackendException.Reason.TUN_CREATION_ERROR,
            BackendException.Reason.UNABLE_TO_START_VPN,
            BackendException.Reason.GO_ACTIVATION_ERROR_CODE,
            -> ConnectionError.TunnelSetupFailed(e.getReason().name)
            BackendException.Reason.TUNNEL_MISSING_CONFIG ->
                ConnectionError.TunnelSetupFailed("missing config")
            else -> ConnectionError.BackendFailure(e.getReason().name)
        }

    private companion object {
        const val TAG = "WgController"
        const val TUNNEL_NAME = "karentunnel"
    }
}
