package ir.karensoft.karentunnel.vpn.controller

import ir.karensoft.karentunnel.core.model.TunnelConfig

/**
 * The seam between our connection logic and the actual tunnel implementation.
 *
 * Having this as an interface is not ceremony: it lets the ConnectionManager's
 * state machine be tested against every failure mode (timeouts, revocations,
 * degraded handshakes) without a device or a real tunnel.
 *
 * Implementations MUST NOT:
 *  - report [TunnelStatus.Up] before the interface exists,
 *  - swallow exceptions,
 *  - keep sockets or file descriptors alive after [down].
 */
interface TunnelController {

    /** Human-readable backend name for the About screen, e.g. "wireguard-go 0.0.2025…". */
    suspend fun backendVersion(): String

    /**
     * Bring the tunnel up with [config].
     *
     * Throws nothing: failures come back as [Result.failure] carrying a
     * [ir.karensoft.karentunnel.core.error.ConnectionError].
     */
    suspend fun up(config: TunnelConfig): Result<Unit>

    /** Tear down. Safe to call when already down; must be idempotent. */
    suspend fun down(): Result<Unit>

    /**
     * Observe the backend's own view of the tunnel.
     * DOWN / UP are the only values we consume (see F3: UP is necessary but
     * not sufficient evidence for a user-visible "connected").
     */
    suspend fun status(): TunnelStatus

    /** Bytes and handshake age for the dashboard. */
    suspend fun statistics(): TunnelStatistics

    /** True when an interface currently exists. */
    fun isActive(): Boolean
}

enum class TunnelStatus { Down, Up }

data class TunnelStatistics(
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    /** Age of the most recent successful handshake, or null if never. */
    val lastHandshakeAgeMillis: Long? = null,
) {
    companion object {
        val EMPTY = TunnelStatistics()
    }
}
