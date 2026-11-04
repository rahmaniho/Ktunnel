package ir.karensoft.karentunnel.vpn.connection

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.connection.RetryPolicy
import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.vpn.controller.TunnelStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Test
    fun `becomes connected only when all evidence is present`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

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
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

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
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Degraded::class.java)
    }

    @Test
    fun `missing system vpn network prevents connected`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(false))

        manager.connect(config)
        advanceUntilIdle()

        assertThat(manager.state.value).isInstanceOf(ConnectionState.Degraded::class.java)
    }

    @Test
    fun `permission failure goes terminal without retry`() = runTest {
        val controller = FakeTunnelController(failWith = ConnectionError.PermissionRequired)
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

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
        )

        manager.connect(config)
        advanceUntilIdle()

        assertThat(controller.upCalls).isEqualTo(3)
        assertThat(manager.state.value).isInstanceOf(ConnectionState.Error::class.java)
    }

    @Test
    fun `disconnect tears the tunnel down and clears state`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()
        assertThat(manager.state.value).isInstanceOf(ConnectionState.Connected::class.java)

        manager.disconnect()
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(ConnectionState.Disconnected)
        assertThat(controller.isActive).isFalse()
        assertThat(controller.downCalls).isAtLeast(1)
    }

    @Test
    fun `external teardown disconnects safely`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

        manager.connect(config)
        advanceUntilIdle()

        manager.onExternalTeardown()
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(ConnectionState.Disconnected)
        assertThat(controller.isActive).isFalse()
    }

    @Test
    fun `double connect does not start two tunnels`() = runTest {
        val controller = FakeTunnelController()
        val manager = ConnectionManager(controller, this, healthProbe = FakeHealthProbe(true))

        manager.connect(config)
        manager.connect(config)
        advanceUntilIdle()

        assertThat(controller.upCalls).isEqualTo(1)
    }
}
