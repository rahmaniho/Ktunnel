package ir.karensoft.karentunnel.vpn.connection

import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.vpn.controller.TunnelController
import ir.karensoft.karentunnel.vpn.controller.TunnelStatistics
import ir.karensoft.karentunnel.vpn.controller.TunnelStatus

/**
 * Scriptable fake used to prove the state machine's promises on the JVM.
 * It records calls so tests can assert on cleanup and idempotence.
 */
class FakeTunnelController(
    var upResult: Result<Unit> = Result.success(Unit),
    var statusAfterUp: TunnelStatus = TunnelStatus.Up,
    var statisticsAfterUp: TunnelStatistics = TunnelStatistics(
        rxBytes = 1024,
        txBytes = 512,
        lastHandshakeAgeMillis = 2_000,
    ),
    /** When true, every up() call fails with this error. */
    var failWith: ir.karensoft.karentunnel.core.error.ConnectionError? = null,
) : TunnelController {

    var upCalls = 0
        private set
    var downCalls = 0
        private set
    var active = false
        private set

    override suspend fun backendVersion(): String = "fake-backend-1.0"

    override suspend fun up(config: TunnelConfig): Result<Unit> {
        upCalls++
        failWith?.let { return Result.failure(it) }
        active = true
        return upResult
    }

    override suspend fun down(): Result<Unit> {
        downCalls++
        active = false
        return Result.success(Unit)
    }

    override suspend fun status(): TunnelStatus =
        if (active) statusAfterUp else TunnelStatus.Down

    override suspend fun statistics(): TunnelStatistics =
        if (active) statisticsAfterUp else TunnelStatistics.EMPTY

    override fun isActive(): Boolean = active
}

class FakeHealthProbe(var vpnPresent: Boolean = true) : HealthProbe {
    override suspend fun systemVpnNetworkPresent(): Boolean = vpnPresent
}
