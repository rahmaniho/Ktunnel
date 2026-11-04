package ir.karensoft.karentunnel.vpn.connection

import ir.karensoft.karentunnel.core.connection.ConnectTrigger
import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.connection.RetryPolicy
import ir.karensoft.karentunnel.core.connection.StateMachine
import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.vpn.controller.TunnelController
import ir.karensoft.karentunnel.vpn.controller.TunnelStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the connection lifecycle. This is the class that must never lie.
 *
 * Responsibilities (from the product brief, Phase 4):
 *  - initiation / termination / state tracking
 *  - bounded reconnect with exponential backoff
 *  - health monitoring that can demote Connected -> Degraded
 *
 * Non-responsibilities, on purpose:
 *  - it does not build tunnels (TunnelController does)
 *  - it does not persist configs (SecureConfigStore does)
 *  - it does not decide policy on 4G vs Wi-Fi (callers pass a trigger)
 *
 * The class is Android-free so it can be tested on the JVM against a fake
 * controller; only [HealthProbe] implementations bring in platform details.
 */
class ConnectionManager(
    private val controller: TunnelController,
    private val scope: CoroutineScope,
    private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT,
    private val healthProbe: HealthProbe? = null,
    private val now: () -> Long = { System.currentTimeMillis() },
) {

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val lock = Mutex()
    private var connectJob: Job? = null
    private var healthJob: Job? = null
    private var currentConfig: TunnelConfig? = null

    /** Delivered so the caller can start the FGS before the tunnel exists (F2). */
    var onBeforeTunnelStart: (suspend () -> Unit)? = null

    /** Delivered after the tunnel is down for any reason, so the FGS can stop. */
    var onTunnelStopped: (suspend () -> Unit)? = null

    fun currentConfig(): TunnelConfig? = currentConfig

    /**
     * Connect. Cancels any in-flight attempt first, so a double tap on the
     * dashboard cannot produce two overlapping tunnels.
     */
    fun connect(config: TunnelConfig, trigger: ConnectTrigger = ConnectTrigger.USER) {
        scope.launch {
            lock.withLock {
                connectJob?.cancel()
                currentConfig = config
                connectJob = scope.launch { runConnectLoop(config, trigger) }
            }
        }
    }

    /** Manual retry after a terminal error: starts a fresh attempt budget. */
    fun retry() {
        val config = currentConfig ?: return
        connect(config, ConnectTrigger.USER)
    }

    /**
     * Disconnect. Cancels retries and health monitoring, then tears down.
     * Idempotent and safe to call from any state, including Error.
     */
    fun disconnect() {
        scope.launch {
            lock.withLock {
                connectJob?.cancel()
                connectJob = null
                healthJob?.cancel()
                healthJob = null
                setState(ConnectionState.Disconnecting)
                controller.down()
                currentConfig = null
                onTunnelStopped?.invoke()
                setState(ConnectionState.Disconnected)
            }
        }
    }

    /** Called by the service when the OS revokes our VPN consent or tears us down. */
    suspend fun onExternalTeardown() {
        lock.withLock {
            connectJob?.cancel()
            connectJob = null
            healthJob?.cancel()
            healthJob = null
            controller.down()
            onTunnelStopped?.invoke()
            setState(ConnectionState.Disconnected)
        }
    }

    /** Called when the physical network changed while connected. */
    fun onNetworkChanged() {
        val state = _state.value
        if (state !is ConnectionState.Connected && state !is ConnectionState.Degraded) return
        val config = currentConfig ?: return
        connect(config, ConnectTrigger.NETWORK_CHANGE)
    }

    private suspend fun runConnectLoop(config: TunnelConfig, trigger: ConnectTrigger) {
        setState(ConnectionState.Preparing)

        var attempt = 1
        while (true) {
            setState(if (attempt == 1) ConnectionState.Connecting else _state.value)

            val failure = attemptOnce(config)

            if (failure == null) {
                // ---- Success path: we still need proof before claiming it ----
                startHealthMonitoring(config)
                val evidence = collectEvidence()
                val state = if (StateMachine.canClaimConnected(evidence)) {
                    ConnectionState.Connected(sinceMillis = now())
                } else {
                    ConnectionState.Degraded(
                        reason = StateMachine.degradedReasonFor(evidence),
                        lastHandshakeAgeMillis = evidence.lastHandshakeAgeMillis,
                    )
                }
                setState(state)
                return
            }

            val next = StateMachine.stateForFailure(failure, attempt, retryPolicy)
            setState(next)

            if (next !is ConnectionState.Reconnecting) {
                // Terminal for this run: tear the interface down so we are not
                // silently "up" while the user sees an error.
                controller.down()
                currentConfig = null
                onTunnelStopped?.invoke()
                return
            }

            delay(next.nextDelayMillis)
            attempt = next.attempt + 1
        }
    }

    /** One attempt. Returns null on success, or the classified failure. */
    private suspend fun attemptOnce(config: TunnelConfig): ConnectionError? {
        // 1. Our own foreground service must be alive first (F2).
        runCatching { onBeforeTunnelStart?.invoke() }
            .onFailure { return ConnectionError.Transient("foreground service failed") }

        // 2. Bring the tunnel up.
        val up = controller.up(config)
        up.exceptionOrNull()?.let { throwable ->
            return (throwable as? ConnectionError) ?: ConnectionError.Transient(
                throwable.javaClass.simpleName,
            )
        }

        // 3. Confirm the backend actually reports UP.
        return if (controller.status() == TunnelStatus.Up) {
            null
        } else {
            ConnectionError.TunnelSetupFailed("backend reported down after up()")
        }
    }

    private suspend fun collectEvidence(): StateMachine.Evidence {
        val stats = controller.statistics()
        val backendUp = controller.status() == TunnelStatus.Up
        val vpnNetwork = healthProbe?.systemVpnNetworkPresent() ?: true
        return StateMachine.Evidence(
            backendReportsUp = backendUp,
            systemVpnNetworkPresent = vpnNetwork,
            lastHandshakeAgeMillis = stats.lastHandshakeAgeMillis,
        )
    }

    private fun startHealthMonitoring(config: TunnelConfig) {
        healthJob?.cancel()
        healthJob = scope.launch {
            while (true) {
                delay(HEALTH_INTERVAL_MILLIS)
                val stats = controller.statistics()
                val evidence = StateMachine.Evidence(
                    backendReportsUp = controller.status() == TunnelStatus.Up,
                    systemVpnNetworkPresent = healthProbe?.systemVpnNetworkPresent() ?: true,
                    lastHandshakeAgeMillis = stats.lastHandshakeAgeMillis,
                )
                val current = _state.value
                val next = when {
                    StateMachine.canClaimConnected(evidence) ->
                        if (current is ConnectionState.Connected) current
                        else ConnectionState.Connected(now())
                    // Do not demote while we are still within the grace window of
                    // a fresh connection: the first handshake takes time.
                    current is ConnectionState.Connected && evidence.lastHandshakeAgeMillis == null ->
                        current
                    else -> ConnectionState.Degraded(
                        reason = StateMachine.degradedReasonFor(evidence),
                        lastHandshakeAgeMillis = evidence.lastHandshakeAgeMillis,
                    )
                }
                if (next != current) {
                    setState(next)
                    if (next is ConnectionState.Degraded && current is ConnectionState.Connected) {
                        // A degradation is a connection event worth an immediate
                        // network-change style recovery attempt.
                        onNetworkChanged()
                        return@launch
                    }
                }
                // Keep the config reference meaningful for diagnostics.
                if (currentConfig != config) return@launch
            }
        }
    }

    private fun setState(next: ConnectionState) {
        val previous = _state.value
        check(StateMachine.isValidTransition(previous, next)) {
            "illegal transition $previous -> $next"
        }
        _state.value = next
    }

    private companion object {
        const val HEALTH_INTERVAL_MILLIS = 5_000L
    }
}

/**
 * Platform facts the manager needs but must not implement itself.
 * Kept as an interface so tests can fake a "VPN network present" answer.
 */
interface HealthProbe {
    suspend fun systemVpnNetworkPresent(): Boolean
}
