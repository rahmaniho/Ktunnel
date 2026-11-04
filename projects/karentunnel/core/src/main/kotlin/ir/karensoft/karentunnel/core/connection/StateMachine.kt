package ir.karensoft.karentunnel.core.connection

import ir.karensoft.karentunnel.core.error.ConnectionError

/**
 * Pure transition rules for [ConnectionState]. No coroutines, no Android:
 * every rule below is unit-tested in ConnectionStateMachineTest.
 *
 * The table exists to make one class of bug impossible: reaching
 * [ConnectionState.Connected] from anywhere other than an *evidence-bearing*
 * transition. Callers cannot construct "connected" by accident.
 */
object StateMachine {

    /** Evidence required to claim Connected. All fields must hold. */
    data class Evidence(
        val backendReportsUp: Boolean,
        val systemVpnNetworkPresent: Boolean,
        val lastHandshakeAgeMillis: Long?,
        val handshakeStaleAfterMillis: Long = DEFAULT_STALE_HANDSHAKE_MILLIS,
    ) {
        val hasFreshHandshake: Boolean
            get() = lastHandshakeAgeMillis != null &&
                lastHandshakeAgeMillis in 0..handshakeStaleAfterMillis
    }

    const val DEFAULT_STALE_HANDSHAKE_MILLIS = 180_000L // WireGuard rekeys ~120s

    /**
     * Should we present Connected given this evidence?
     * Deliberately conservative: missing evidence is "no".
     */
    fun canClaimConnected(evidence: Evidence): Boolean =
        evidence.backendReportsUp &&
            evidence.systemVpnNetworkPresent &&
            evidence.hasFreshHandshake

    /** What to show when the tunnel object is up but proof is missing. */
    fun degradedReasonFor(evidence: Evidence): DegradedReason = when {
        !evidence.backendReportsUp || !evidence.systemVpnNetworkPresent ->
            DegradedReason.HealthProbeFailed
        evidence.lastHandshakeAgeMillis == null -> DegradedReason.NoHandshake
        else -> DegradedReason.StaleHandshake
    }

    /**
     * Transition validity table. Used by tests and by [ConnectionManager] as an
     * assertion in debug builds; it is *not* a whitelist that blocks progress,
     * because a user can always force a disconnect from any state.
     */
    fun isValidTransition(from: ConnectionState, to: ConnectionState): Boolean {
        if (to is ConnectionState.Disconnected) return true // always allowed
        if (to is ConnectionState.Error) return true        // always allowed
        if (to is ConnectionState.Disconnecting) return true

        return when (from) {
            is ConnectionState.Disconnected -> to is ConnectionState.Preparing
            is ConnectionState.Preparing ->
                to is ConnectionState.Connecting || to is ConnectionState.Reconnecting
            is ConnectionState.Connecting ->
                to is ConnectionState.Connected ||
                    to is ConnectionState.Degraded ||
                    to is ConnectionState.Reconnecting
            is ConnectionState.Connected ->
                to is ConnectionState.Degraded || to is ConnectionState.Reconnecting
            is ConnectionState.Degraded ->
                to is ConnectionState.Connected ||
                    to is ConnectionState.Reconnecting
            is ConnectionState.Reconnecting ->
                to is ConnectionState.Connecting ||
                    to is ConnectionState.Connected ||
                    to is ConnectionState.Degraded ||
                    to is ConnectionState.Error
            is ConnectionState.Disconnecting -> false
            is ConnectionState.Error ->
                to is ConnectionState.Preparing || to is ConnectionState.Reconnecting
        }
    }

    /**
     * The single place that maps a failure into the next user-visible state.
     * Never returns Connected.
     */
    fun stateForFailure(
        failure: ConnectionError,
        attempt: Int,
        policy: RetryPolicy,
    ): ConnectionState =
        if (policy.shouldRetry(attempt, failure)) {
            ConnectionState.Reconnecting(
                attempt = attempt,
                maxAttempts = policy.maxAttempts,
                nextDelayMillis = policy.delayFor(attempt + 1),
                lastError = failure,
            )
        } else {
            ConnectionState.Error(
                error = failure,
                retryable = failure.isRetryableForUi,
            )
        }

    /**
     * "Retryable" as shown to the user: even non-retryable errors (like
     * PermissionRequired) can be surfaced with a *fix* action, but they are not
     * loop-retryable. The UI distinguishes "تلاش دوباره" from "رفع مشکل".
     */
    private val ConnectionError.isRetryableForUi: Boolean
        get() = this !is ConnectionError.PermissionRevoked &&
            this !is ConnectionError.PermissionRequired
}
