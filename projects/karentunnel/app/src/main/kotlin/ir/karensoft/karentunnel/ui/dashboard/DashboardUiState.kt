package ir.karensoft.karentunnel.ui.dashboard

import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.error.ConnectionError

/**
 * A single immutable snapshot for the dashboard. Derived from the connection
 * state plus live statistics; the UI holds no mutable state of its own, so a
 * stale "connected" badge cannot survive a disconnect.
 */
data class DashboardUiState(
    val connection: ConnectionState = ConnectionState.Disconnected,
    val serverName: String? = null,
    /** Id of the selected server, when one is selected. Null before selection. */
    val selectedServerId: ir.karensoft.karentunnel.core.model.ServerId? = null,
    val protocolLabel: String = "WireGuard",
    val uptimeMillis: Long = 0,
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val latencyMillis: Long? = null,
    val networkLabel: String = "—",
    val ipv6Label: String = "—",
    val lastError: ConnectionError? = null,
) {
    /**
     * Whether the primary control should be interpreted as "connect" or
     * "disconnect". While reconnecting, the control means "stop this", which is
     * the safe interpretation: a user tapping during a retry loop wants it to end.
     */
    val shouldDisconnect: Boolean
        get() = connection is ConnectionState.Connected ||
            connection is ConnectionState.Degraded ||
            connection is ConnectionState.Connecting ||
            connection is ConnectionState.Preparing ||
            connection is ConnectionState.Reconnecting
}
