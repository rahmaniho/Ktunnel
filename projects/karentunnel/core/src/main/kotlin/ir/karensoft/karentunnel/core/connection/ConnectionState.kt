package ir.karensoft.karentunnel.core.connection

import ir.karensoft.karentunnel.core.error.ConnectionError

/**
 * The only connection states the UI is allowed to render.
 *
 * Note the absence of any "connected-ish" state: [Connected] means the tunnel
 * exists AND we have proof (fresh handshake). A tunnel that is up without proof
 * is [Degraded], which renders as a warning, never as success.
 * See docs/TECHNICAL-FEASIBILITY.md F3.
 */
sealed interface ConnectionState {

    data object Disconnected : ConnectionState

    /** Reading config, normalising, asking for consent. */
    data object Preparing : ConnectionState

    /** Tunnel is being created / brought up. No proof yet. */
    data object Connecting : ConnectionState

    /**
     * Proof obtained: backend UP + fresh handshake + system VPN network present.
     * Only this state may render the "connected" affordance.
     */
    data class Connected(val sinceMillis: Long) : ConnectionState

    /**
     * Tunnel object is up but the handshake is stale or absent. Traffic may be
     * blackholed by the server. Rendered as a warning with a disconnect action.
     */
    data class Degraded(
        val reason: DegradedReason,
        val lastHandshakeAgeMillis: Long?,
    ) : ConnectionState

    /** Automatic retry in progress, with an explicit attempt counter. */
    data class Reconnecting(
        val attempt: Int,
        val maxAttempts: Int,
        val nextDelayMillis: Long,
        val lastError: ConnectionError,
    ) : ConnectionState

    /** Waiting to be torn down. */
    data object Disconnecting : ConnectionState

    /** Terminal failure for this attempt; the user must act or retry manually. */
    data class Error(
        val error: ConnectionError,
        val retryable: Boolean,
    ) : ConnectionState

    companion object {
        /** States in which traffic is expected to be protected. */
        fun isProtected(state: ConnectionState): Boolean =
            state is Connected || state is Degraded
    }
}

enum class DegradedReason {
    /** No handshake at all since the tunnel came up. */
    NoHandshake,

    /** Handshake exists but is older than the staleness threshold. */
    StaleHandshake,

    /** Last health probe failed while the tunnel object stayed up. */
    HealthProbeFailed,
}

/** Why a connection attempt was requested; drives notification + restore logic. */
enum class ConnectTrigger {
    USER,
    WIDGET,
    ALWAYS_ON,
    BOOT_RESTORE,
    NETWORK_CHANGE,
}
