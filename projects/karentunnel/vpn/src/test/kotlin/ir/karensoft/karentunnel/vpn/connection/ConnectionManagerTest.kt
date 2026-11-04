package ir.karensoft.karentunnel.vpn.connection

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.connection.RetryPolicy
import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.vpn.controller.TunnelController
import ir.karensoft.karentunnel.vpn.controller.TunnelStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionManagerTest {

    private val config: TunnelConfig = ConfigNormalizer.normalize(
        ConfigNormalizer.Raw(
            serverId = ServerId("test"),
            endpoint = "vpn.example.com:51820",
            address = "10.66.0.2/32",
            dnsServers = listOf("10.66.0.1"),
            allowedIps = listOf("0.0.0.0/0"),
            peerPublicKey = "cGVlcnBlZXJwZWVycGVlcnBlZXJwZWVycGVlcnBlZXI=",
            privateKey = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q=",
        ),
    ).getOrThrow()

    /**
     * The background confirmation loop is intentionally endless in production,
     * so a test must park it: with the real 5 s period, runTest's virtual clock
     * would keep advancing forever and every test would time out after 60 s.
     * A one-hour period means the loop never completes during a test.
     */
    private fun TestScope.newManager(
        controller: TunnelController,
        healthProbe: HealthProbe? = null,
    ) = ConnectionManager(
        controller = controller,
        scope = this,
        healthProbe = healthProbe,
        healthIntervalMillis = 3_600_000L,
    )

    @Test
    fun `becomes connected only when all evidence is present`() = runTest {
        val controller = FakeTunnelController()
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Connected::class.java)
        assertThat(controller.upCalls).isEqualTo(1)
    }

    @Test
    fun `tunnel up without handshake is degraded not connected`() = runTest {
        val controller = FakeTunnelController(
            statisticsAfterUp = TunnelStatistics(lastHandshakeAgeMillis = null),
        )
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        val state = manager.state.value
        assertThat(state).isInstanceOf(ConnectionState.Degraded::class.java)
        assertThat((state as ConnectionState.Degraded).reason)
            .isEqualTo(ir.karensoft.karentunnel.core.connection.DegradedReason.NoHandshake)
    }

    @Test
    fun `stale handshake is degraded`() = runTest {
        val controller = FakeTunnelController(
            statisticsAfterUp = TunnelStatistics(lastHandshakeAgeMillis = 600_000),
        )
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Degraded::class.java)
    }

    @Test
    fun `missing system vpn network prevents connected`() = runTest {
        val controller = FakeTunnelController()
        val manager = newManager(controller, FakeHealthProbe(false))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Degraded::class.java)
    }

    @Test
    fun `permission failure goes terminal without retry`() = runTest {
        val controller = FakeTunnelController(failWith = ConnectionError.PermissionRequired)
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Error::class.java)
        assertThat(controller.upCalls).isEqualTo(1)
        assertThat(controller.downCalls).isAtLeast(1)
    }

    @Test
    fun `transient failure retries a bounded number of times`() = runTest {
        val controller = FakeTunnelController(failWith = ConnectionError.Timeout)
        val policy = RetryPolicy(initialDelayMillis = 10, maxAttempts = 3)
        val manager = ConnectionManager(
            controller,
            this,
            retryPolicy = policy,
            healthProbe = FakeHealthProbe(true),
            healthIntervalMillis = 3_600_000L,
        )

        manager.connect(config)
        advanceUntilIdle()

        assertThat(controller.upCalls).isEqualTo(3)
        assertThat(manager.state.value).isInstanceOf(ConnectionState.Error::class.java)
    }

    @Test
    fun `health loop demotes a connected tunnel when the handshake goes stale`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(
            controller = controller,
            scope = this,
            healthProbe = FakeHealthProbe(true),
            // Short enough to fire on the virtual clock, and one-shot: the loop
            // returns after the first demotion, so this cannot spin.
            healthIntervalMillis = 1_000L,
        )
        val degradedReasons = mutableListOf<ir.karensoft.karentunnel.core.connection.DegradedReason>()
        manager.onDegraded = { degradedReasons += it.reason }

        manager.connect(config)
        advanceUntilIdle()
        assertThat(manager.state.value).isInstanceOf(ConnectionState.Connected::class.java)

        // The peer stops answering: no handshake is reported from now on.
        controller.statisticsAfterUp = TunnelStatistics(lastHandshakeAgeMillis = null)
        advanceTimeBy(1_500L)
        advanceUntilIdle()

        val state = manager.state.value
        assertThat(state).isInstanceOf(ConnectionState.Degraded::class.java)
        assertThat((state as ConnectionState.Degraded).reason)
            .isEqualTo(ir.karensoft.karentunnel.core.connection.DegradedReason.NoHandshake)
        // The callback fires exactly once; recovery is the caller's decision.
        assertThat(degradedReasons).hasSize(1)
    }

    @Test
    fun `disconnect tears the tunnel down and clears state`() = runTest {
        val controller = FakeTunnelController()
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()
        assertThat(manager.state.value).isInstanceOf(ConnectionState.Connected::class.java)

        manager.disconnect()
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(ConnectionState.Disconnected)
        assertThat(controller.isActive()).isFalse()
        assertThat(controller.downCalls).isAtLeast(1)
    }

    @Test
    fun `external teardown disconnects safely`() = runTest {
        val controller = FakeTunnelController()
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        manager.onExternalTeardown()
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(ConnectionState.Disconnected)
        assertThat(controller.isActive()).isFalse()
    }

    @Test
    fun `double connect does not start two tunnels`() = runTest {
        val controller = FakeTunnelController()
        val manager = newManager(controller, FakeHealthProbe(true))

        manager.connect(config)
        manager.connect(config)
        advanceUntilIdle()

        assertThat(controller.upCalls).isEqualTo(1)
    }
}
