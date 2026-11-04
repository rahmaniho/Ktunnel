package ir.karensoft.karentunnel.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.karensoft.karentunnel.core.connection.ConnectTrigger
import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.model.Ipv6Policy
import ir.karensoft.karentunnel.core.model.Protocol
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.data.secure.SecureConfigStore
import ir.karensoft.karentunnel.vpn.connection.ConnectionManager
import ir.karensoft.karentunnel.vpn.connection.HealthProbe
import ir.karensoft.karentunnel.vpn.controller.TunnelController
import ir.karensoft.karentunnel.vpn.network.NetworkMonitor
import ir.karensoft.karentunnel.vpn.network.NetworkSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Turns the connection machinery into something a screen can render.
 *
 * This class is where "the UI must never lie" is enforced at the UI boundary:
 * [DashboardUiState.connection] is copied straight from the manager's
 * StateFlow — it is never computed locally — and traffic counters are zeroed the
 * moment the state stops being protected.
 */
class DashboardViewModel(
    private val store: SecureConfigStore,
    controller: TunnelController,
    healthProbe: HealthProbe,
    private val networkMonitor: NetworkMonitor,
    private val foreground: ForegroundController,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val manager = ConnectionManager(
        controller = controller,
        scope = viewModelScope,
        healthProbe = healthProbe,
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var selected: ServerId? = null
    private var selectedName: String? = null

    init {
        // The FGS must exist before the tunnel does (F2). Ordering is enforced
        // here rather than inside the manager so it stays testable.
        manager.onBeforeTunnelStart = { foreground.start() }
        manager.onTunnelStopped = { foreground.stop() }

        viewModelScope.launch {
            manager.state.collectLatest { state ->
                applyState(state)
                if (state !is ConnectionState.Connected && state !is ConnectionState.Degraded) {
                    _uiState.update { it.copy(rxBytes = 0, txBytes = 0, latencyMillis = null) }
                }
            }
        }

        viewModelScope.launch {
            controller.let { c ->
                while (true) {
                    kotlinx.coroutines.delay(STATS_INTERVAL_MILLIS)
                    val state = manager.state.value
                    if (state is ConnectionState.Connected || state is ConnectionState.Degraded) {
                        val stats = c.statistics()
                        val since = (state as? ConnectionState.Connected)?.sinceMillis
                        _uiState.update {
                            it.copy(
                                rxBytes = stats.rxBytes,
                                txBytes = stats.txBytes,
                                uptimeMillis = since?.let { s -> clock() - s } ?: it.uptimeMillis,
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            networkMonitor.snapshots().collectLatest { snap ->
                _uiState.update { it.copy(networkLabel = snap.networkLabel()) }
                if (manager.state.value is ConnectionState.Connected) {
                    manager.onNetworkChanged()
                }
            }
        }
    }

    /** Select a stored server by id. Does not connect. */
    fun selectServer(id: ServerId, displayName: String) {
        selected = id
        selectedName = displayName
        _uiState.update { it.copy(serverName = displayName, selectedServerId = id) }
    }

    /**
     * Re-reads the selection after the servers screen mutated the store.
     *
     * Deleting or importing on another screen can invalidate the cached
     * selection; calling this keeps the dashboard from offering "connect" for a
     * server that no longer exists.
     */
    fun refreshServers() {
        val id = selected ?: return
        if (!store.exists(id)) {
            selected = null
            selectedName = null
            _uiState.update { it.copy(serverName = null, selectedServerId = null) }
        }
    }

    /**
     * The single user action. Interpreted by the current state, never by the
     * button's own memory — that is how "tap to disconnect" bugs happen.
     */
    fun onPrimaryAction() {
        if (_uiState.value.shouldDisconnect) {
            manager.disconnect()
            return
        }
        val id = selected ?: return
        val config = store.load(id).getOrElse {
            _uiState.update {
                it.copy(
                    connection = ConnectionState.Error(
                        ir.karensoft.karentunnel.core.error.ConnectionError.TunnelSetupFailed(
                            "config unavailable",
                        ),
                        retryable = false,
                    ),
                )
            }
            return
        }
        connect(config)
    }

    fun connect(config: TunnelConfig) {
        selected = config.serverId
        _uiState.update {
            it.copy(
                serverName = selectedName ?: it.serverName,
                protocolLabel = config.protocol.displayName(),
                ipv6Label = when (config.ipv6Policy) {
                    Ipv6Policy.TUNNELED -> "داخل تونل"
                    Ipv6Policy.EXCLUDED -> "خارج از تونل (مسدود)"
                },
            )
        }
        manager.connect(config, ConnectTrigger.USER)
    }

    fun onVpnPermissionGranted() {
        // Re-issue the same intent; the manager restarts from Preparing.
        onPrimaryAction()
    }

    /**
     * Consent was refused. We must surface this as an Error — staying silently
     * "Disconnected" after a connect tap is the kind of passive lie this app
     * exists to avoid.
     */
    fun onVpnPermissionDenied() {
        val state = ConnectionState.Error(
            ir.karensoft.karentunnel.core.error.ConnectionError.PermissionRequired,
            retryable = false,
        )
        _uiState.update { it.copy(connection = state, lastError = state.error) }
        foreground.update(state)
    }

    /** Called from the Activity when the system revokes our VPN consent. */
    fun onSystemRevoked() {
        viewModelScope.launch { manager.onExternalTeardown() }
    }

    fun emergencyDisconnect() {
        manager.disconnect()
    }

    private fun applyState(state: ConnectionState) {
        val error = (state as? ConnectionState.Error)?.error
        _uiState.update {
            it.copy(
                connection = state,
                lastError = error ?: if (state is ConnectionState.Connected) null else it.lastError,
                uptimeMillis = if (state is ConnectionState.Connected) 0 else it.uptimeMillis,
            )
        }
        foreground.update(state)
    }

    private companion object {
        const val STATS_INTERVAL_MILLIS = 1_000L
    }
}

/** Abstracts the notification service so the ViewModel stays JVM-testable. */
interface ForegroundController {
    fun start()
    fun stop()
    fun update(state: ConnectionState)
}

internal fun NetworkSnapshot.networkLabel(): String = when {
    !hasInternet -> "بدون اینترنت"
    transports.contains(NetworkSnapshot.Transport.WIFI) -> "Wi-Fi"
    transports.contains(NetworkSnapshot.Transport.CELLULAR) -> "دادهٔ همراه"
    transports.contains(NetworkSnapshot.Transport.ETHERNET) -> "اترنت"
    else -> "نامشخص"
}

internal fun Protocol.displayName(): String = when (this) {
    Protocol.WIREGUARD -> "WireGuard"
}
