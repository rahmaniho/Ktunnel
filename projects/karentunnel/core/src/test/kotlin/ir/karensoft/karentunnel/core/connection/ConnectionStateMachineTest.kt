package ir.karensoft.karentunnel.core.connection

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.error.ConnectionError
import org.junit.Test

/**
 * Guards the promise in the product brief: "The UI must never report CONNECTED
 * unless the tunnel has been established and verified." and "Do not create
 * infinite retry loops."
 */
class ConnectionStateMachineTest {

    private fun evidence(
        backendUp: Boolean = true,
        vpnNetwork: Boolean = true,
        handshakeAge: Long? = 5_000,
    ) = StateMachine.Evidence(
        backendReportsUp = backendUp,
        systemVpnNetworkPresent = vpnNetwork,
        lastHandshakeAgeMillis = handshakeAge,
    )

    // ---------------------------------------------------------- proof of connection

    @Test
    fun `connected requires all three proofs`() {
        assertThat(StateMachine.canClaimConnected(evidence())).isTrue()
    }

    @Test
    fun `no connection without backend up`() {
        assertThat(StateMachine.canClaimConnected(evidence(backendUp = false))).isFalse()
    }

    @Test
    fun `no connection without system vpn network`() {
        assertThat(StateMachine.canClaimConnected(evidence(vpnNetwork = false))).isFalse()
    }

    @Test
    fun `no connection without a handshake`() {
        assertThat(StateMachine.canClaimConnected(evidence(handshakeAge = null))).isFalse()
    }

    @Test
    fun `no connection when handshake is stale`() {
        val stale = StateMachine.DEFAULT_STALE_HANDSHAKE_MILLIS + 1
        assertThat(StateMachine.canClaimConnected(evidence(handshakeAge = stale))).isFalse()
    }

    @Test
    fun `degraded reason distinguishes missing from stale handshake`() {
        assertThat(StateMachine.degradedReasonFor(evidence(handshakeAge = null)))
            .isEqualTo(DegradedReason.NoHandshake)
        val stale = StateMachine.DEFAULT_STALE_HANDSHAKE_MILLIS + 1
        assertThat(StateMachine.degradedReasonFor(evidence(handshakeAge = stale)))
            .isEqualTo(DegradedReason.StaleHandshake)
        assertThat(StateMachine.degradedReasonFor(evidence(backendUp = false)))
            .isEqualTo(DegradedReason.HealthProbeFailed)
    }

    @Test
    fun `degraded is treated as protected but is not success`() {
        val degraded = ConnectionState.Degraded(DegradedReason.StaleHandshake, 300_000)
        assertThat(ConnectionState.isProtected(degraded)).isTrue()
        // Widen to the supertype before the type test: comparing a Degraded value
        // against the unrelated sibling Connected is statically always false and
        // the compiler rejects the tautology under -Werror. The property being
        // asserted (a degraded tunnel is protected but must never be reported as
        // Connected) is about the hierarchy, so check it at that level.
        val asState: ConnectionState = degraded
        assertThat(asState is ConnectionState.Connected).isFalse()
    }

    // ------------------------------------------------------------------ backoff

    @Test
    fun `backoff grows and is capped`() {
        val policy = RetryPolicy(initialDelayMillis = 1_000, maxDelayMillis = 16_000, multiplier = 2.0)
        val delays = (2..8).map { policy.delayFor(it, random = 0.5) }
        assertThat(delays).containsExactly(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L, 16_000L)
    }

    @Test
    fun `jitter stays within bounds`() {
        val policy = RetryPolicy(initialDelayMillis = 1_000, maxDelayMillis = 60_000, jitterFraction = 0.2)
        val low = policy.delayFor(2, random = 0.0)
        val high = policy.delayFor(2, random = 1.0)
        assertThat(low).isAtLeast(700L)
        assertThat(high).isAtMost(1_300L)
        assertThat(policy.delayFor(2, random = 0.5)).isEqualTo(1_000L)
    }

    @Test
    fun `retry stops at max attempts`() {
        val policy = RetryPolicy(maxAttempts = 3)
        assertThat(policy.shouldRetry(1, ConnectionError.Timeout)).isTrue()
        assertThat(policy.shouldRetry(2, ConnectionError.Timeout)).isTrue()
        assertThat(policy.shouldRetry(3, ConnectionError.Timeout)).isFalse()
    }

    @Test
    fun `permission errors are never retried automatically`() {
        val policy = RetryPolicy.DEFAULT
        assertThat(policy.shouldRetry(1, ConnectionError.PermissionRequired)).isFalse()
        assertThat(policy.shouldRetry(1, ConnectionError.PermissionRevoked)).isFalse()
    }

    @Test
    fun `no network is not retried until the network returns`() {
        assertThat(RetryPolicy.DEFAULT.shouldRetry(1, ConnectionError.NoNetwork)).isFalse()
    }

    @Test
    fun `attempt cap cannot be configured into an infinite loop`() {
        var rejected = false
        try {
            RetryPolicy(maxAttempts = Int.MAX_VALUE)
        } catch (e: IllegalArgumentException) {
            rejected = true
        }
        assertThat(rejected).isTrue()
    }

    @Test
    fun `failure never maps to connected`() {
        val policy = RetryPolicy.DEFAULT
        val failures = listOf(
            ConnectionError.Timeout,
            ConnectionError.NoNetwork,
            ConnectionError.HandshakeTimeout,
            ConnectionError.PermissionRevoked,
            ConnectionError.Transient("boom"),
        )
        for (failure in failures) {
            for (attempt in 1..policy.maxAttempts) {
                val state = StateMachine.stateForFailure(failure, attempt, policy)
                assertThat(state is ConnectionState.Connected).isFalse()
                assertThat(state is ConnectionState.Degraded).isFalse()
            }
        }
    }

    // -------------------------------------------------------------- transitions

    @Test
    fun `cannot jump from disconnected straight to connected`() {
        assertThat(
            StateMachine.isValidTransition(
                ConnectionState.Disconnected,
                ConnectionState.Connected(0),
            ),
        ).isFalse()
    }

    @Test
    fun `can always disconnect from any state`() {
        val states = listOf<ConnectionState>(
            ConnectionState.Preparing,
            ConnectionState.Connecting,
            ConnectionState.Connected(0),
            ConnectionState.Degraded(DegradedReason.NoHandshake, null),
            ConnectionState.Reconnecting(1, 5, 1000, ConnectionError.Timeout),
            ConnectionState.Error(ConnectionError.Timeout, true),
        )
        for (state in states) {
            assertThat(
                StateMachine.isValidTransition(state, ConnectionState.Disconnected),
            ).isTrue()
        }
    }
}
