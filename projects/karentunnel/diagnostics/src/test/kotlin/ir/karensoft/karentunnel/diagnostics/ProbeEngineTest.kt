package ir.karensoft.karentunnel.diagnostics

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.Ipv6Policy
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The central promise under test: the engine may say "probable" but must never
 * claim to have "confirmed" a filtering mechanism.
 */
class ProbeEngineTest {

    private val config: TunnelConfig = ConfigNormalizer.normalize(
        ConfigNormalizer.Raw(
            serverId = ServerId("probe-test"),
            endpoint = "vpn.example.com:51820",
            address = "10.66.0.2/32",
            dnsServers = listOf("10.66.0.1"),
            allowedIps = listOf("0.0.0.0/0"),
            peerPublicKey = "cGVlcnBlZXJwZWVycGVlcnBlZXJwZWVycGVlcnBlZXI=",
            privateKey = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q=",
        ),
    ).getOrThrow()

    private fun evidence(kind: EvidenceKind, detail: String) = Evidence(kind, detail, 0)

    @Test
    fun `dns failure with dead tcp stays unknown`() {
        val engine = ProbeEngine()
        val diagnosis = engine.diagnose(
            listOf(
                evidence(EvidenceKind.DNS_LOOKUP, "نام endpoint قابل تبدیل نشد: vpn.example.com"),
                evidence(EvidenceKind.TCP_CONNECT, "TCP connect failed/timed out"),
            ),
            config,
        )
        assertThat(diagnosis).isInstanceOf(Diagnosis.Unknown::class.java)
    }

    @Test
    fun `dns failure alone is only probable`() {
        val diagnosis = ProbeEngine().diagnose(
            listOf(
                evidence(EvidenceKind.DNS_LOOKUP, "نام endpoint قابل تبدیل نشد: vpn.example.com"),
                evidence(EvidenceKind.TCP_CONNECT, "TCP connect OK"),
            ),
            config,
        )
        assertThat(diagnosis).isInstanceOf(Diagnosis.Probable::class.java)
        val probable = diagnosis as Diagnosis.Probable
        assertThat(probable.hypothesis).isEqualTo(Hypothesis.DNS_MEDIATION_OR_FAILURE)
        assertThat(probable.confidence).isNotEqualTo(Confidence.HIGH)
    }

    @Test
    fun `unreachable endpoint is probable with low confidence`() {
        val diagnosis = ProbeEngine().diagnose(
            listOf(
                evidence(EvidenceKind.DNS_LOOKUP, "resolved vpn.example.com -> 203.0.113.9"),
                evidence(EvidenceKind.TCP_CONNECT, "TCP connect failed/timed out"),
            ),
            config,
        )
        val probable = diagnosis as Diagnosis.Probable
        assertThat(probable.hypothesis).isEqualTo(Hypothesis.ENDPOINT_BLOCKED_OR_DOWN)
        assertThat(probable.confidence).isEqualTo(Confidence.LOW)
    }

    @Test
    fun `healthy probes never produce a filtering claim`() {
        val diagnosis = ProbeEngine().diagnose(
            listOf(
                evidence(EvidenceKind.DNS_LOOKUP, "resolved vpn.example.com -> 203.0.113.9"),
                evidence(EvidenceKind.TCP_CONNECT, "TCP connect OK"),
            ),
            config,
        )
        assertThat(diagnosis).isInstanceOf(Diagnosis.Unknown::class.java)
    }

    @Test
    fun `run uses injected probes and records evidence`() = runTest {
        val engine = ProbeEngine(
            io = this.coroutineContext[kotlinx.coroutines.CoroutineDispatcher]
                ?: kotlinx.coroutines.Dispatchers.Unconfined,
            clock = { 123L },
            dnsLookup = { listOf("203.0.113.9") },
            tcpProbe = { _, _, _ -> true },
        )
        val report = engine.run(config)

        assertThat(report.serverId).isEqualTo(ServerId("probe-test"))
        assertThat(report.ipv6Policy).isEqualTo(Ipv6Policy.EXCLUDED)
        assertThat(report.evidence).hasSize(2)
        assertThat(report.evidence.map { it.observedAtMillis }).containsExactly(123L, 123L)
    }

    @Test
    fun `report headline never says filtering confirmed`() {
        val report = DiagnosticReport(
            serverId = ServerId("x"),
            ipv6Policy = Ipv6Policy.EXCLUDED,
            evidence = emptyList(),
            diagnosis = Diagnosis.Probable(
                Hypothesis.ENDPOINT_BLOCKED_OR_DOWN,
                Confidence.LOW,
                emptyList(),
            ),
        )
        assertThat(report.headline()).doesNotContain("قطعی")
    }
}
