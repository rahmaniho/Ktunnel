package ir.karensoft.karentunnel.diagnostics

import ir.karensoft.karentunnel.core.model.TunnelConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Runs a small, bounded set of probes against endpoints the user configured.
 *
 * Scope discipline (from the brief: "Only test user-configured endpoints …
 * Avoid aggressive scanning"):
 *  * the only destinations are the endpoint host/port of the selected config and
 *    its DNS servers;
 *  * every probe has a timeout and there is a global budget;
 *  * probes run sequentially, never in a fan-out;
 *  * results are evidence, never verdicts.
 */
class ProbeEngine(
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
    private val dnsLookup: suspend (String) -> List<String> = ::resolve,
    private val tcpProbe: suspend (String, Int, Int) -> Boolean = ::tcpConnect,
) {

    data class Options(
        val connectTimeoutMillis: Int = 3_000,
        val totalBudgetMillis: Long = 15_000,
    )

    suspend fun run(config: TunnelConfig, options: Options = Options()): DiagnosticReport =
        withTimeoutOrNull(options.totalBudgetMillis) {
            val evidence = mutableListOf<Evidence>()

            // 1. DNS
            val resolution = withContext(io) { runCatching { dnsLookup(config.endpointHost) } }
            when {
                resolution.isFailure -> evidence += Evidence(
                    EvidenceKind.DNS_LOOKUP,
                    "نام endpoint قابل تبدیل نشد: ${config.endpointHost}",
                    clock(),
                )
                resolution.getOrNull().isNullOrEmpty() -> evidence += Evidence(
                    EvidenceKind.DNS_LOOKUP,
                    "نتیجهٔ خالی برای ${config.endpointHost}",
                    clock(),
                )
                else -> evidence += Evidence(
                    EvidenceKind.DNS_LOOKUP,
                    "resolved ${config.endpointHost} -> ${resolution.getOrThrow().joinToString(",")}",
                    clock(),
                )
            }

            // 2. TCP reachability of the endpoint (WireGuard itself needs UDP;
            //    TCP success/failure is used only as a coarse reachability signal
            //    and is labelled as such).
            val tcpOk = withContext(io) {
                runCatching { tcpProbe(config.endpointHost, config.endpointPort, options.connectTimeoutMillis) }
                    .getOrDefault(false)
            }
            evidence += Evidence(
                EvidenceKind.TCP_CONNECT,
                if (tcpOk) "TCP connect OK" else "TCP connect failed/timed out",
                clock(),
            )

            DiagnosticReport(
                serverId = config.serverId,
                ipv6Policy = config.ipv6Policy,
                evidence = evidence,
                diagnosis = diagnose(evidence, config),
            )
        } ?: DiagnosticReport(
            serverId = config.serverId,
            ipv6Policy = config.ipv6Policy,
            evidence = emptyList(),
            diagnosis = Diagnosis.Unknown(listOf(Evidence(EvidenceKind.NETWORK_CAPABILITY, "probe budget exhausted", clock()))),
        )

    /**
     * Maps evidence to the strongest claim the evidence supports. This function
     * is the reason the type system distinguishes Probable from Confirmed:
     * every branch here was chosen to stay below the threshold of accusation.
     */
    fun diagnose(evidence: List<Evidence>, config: TunnelConfig): Diagnosis {
        val dnsFailed = evidence.any {
            it.kind == EvidenceKind.DNS_LOOKUP && it.detail.startsWith("نام endpoint")
        }
        val tcpFailed = evidence.any {
            it.kind == EvidenceKind.TCP_CONNECT && it.detail.contains("failed")
        }
        val tcpOk = evidence.any {
            it.kind == EvidenceKind.TCP_CONNECT && it.detail.contains("OK")
        }

        return when {
            dnsFailed && tcpFailed -> Diagnosis.Unknown(
                symptoms = evidence,
            )

            dnsFailed -> Diagnosis.Probable(
                hypothesis = Hypothesis.DNS_MEDIATION_OR_FAILURE,
                confidence = Confidence.MEDIUM,
                evidence = evidence,
            )

            tcpFailed -> Diagnosis.Probable(
                hypothesis = Hypothesis.ENDPOINT_BLOCKED_OR_DOWN,
                confidence = Confidence.LOW,
                evidence = evidence,
            )

            tcpOk -> Diagnosis.Unknown(symptoms = evidence)

            else -> Diagnosis.Unknown(symptoms = evidence)
        }
    }

    companion object {
        private suspend fun resolve(host: String): List<String> =
            withContext(Dispatchers.IO) {
                InetAddress.getAllByName(host).mapNotNull { it.hostAddress }
            }

        private suspend fun tcpConnect(host: String, port: Int, timeoutMillis: Int): Boolean =
            withContext(Dispatchers.IO) {
                runCatching {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), timeoutMillis)
                        true
                    }
                }.getOrDefault(false)
            }
    }
}
