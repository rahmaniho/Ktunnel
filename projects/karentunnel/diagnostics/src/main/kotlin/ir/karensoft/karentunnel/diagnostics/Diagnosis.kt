package ir.karensoft.karentunnel.diagnostics

import kotlinx.serialization.Serializable

/**
 * The epistemic contract of the diagnostic engine.
 *
 * The product brief forbids claiming that a filtering mechanism was
 * "identified" without evidence. This type makes that rule structural: there is
 * no `Confirmed(Filtering)` constructor, and [Probable] requires an explicit
 * confidence plus the evidence that produced it.
 */
@Serializable
sealed interface Diagnosis {

    /** A failure we can reproduce and name. */
    @Serializable
    data class Confirmed(
        val failure: FailureKind,
        val evidence: List<Evidence>,
    ) : Diagnosis

    /** A hypothesis consistent with the evidence. Never phrased as fact. */
    @Serializable
    data class Probable(
        val hypothesis: Hypothesis,
        val confidence: Confidence,
        val evidence: List<Evidence>,
    ) : Diagnosis

    /** We observed symptoms but cannot attribute them. */
    @Serializable
    data class Unknown(
        val symptoms: List<Evidence>,
    ) : Diagnosis
}

@Serializable
enum class Confidence { LOW, MEDIUM, HIGH }

/**
 * Named, reproducible failures only. If you cannot describe the failure as one
 * of these, it is not Confirmed.
 */
@Serializable
enum class FailureKind {
    DNS_RESOLUTION_FAILED,
    ENDPOINT_UNREACHABLE,
    CONNECT_TIMEOUT,
    HANDSHAKE_TIMEOUT,
    NO_NETWORK,
    VPN_PERMISSION_MISSING,
    CONFIG_INVALID,
    TUNNEL_DOWN_WHILE_EXPECTED,
    IPV6_PATH_EXPOSED,
    DNS_PATH_NOT_TUNNELED,
}

/**
 * Careful, non-accusatory hypotheses. These are worded for the UI to show
 * verbatim (Persian strings live in the app module and map 1:1).
 */
@Serializable
enum class Hypothesis {
    /** Endpoint resolved but no packet came back at all. */
    ENDPOINT_BLOCKED_OR_DOWN,

    /** Handshake started but no valid response arrived. */
    HANDSHAKE_INTERFERENCE,

    /** DNS queries fail while raw TCP to an IP works. */
    DNS_MEDIATION_OR_FAILURE,

    /** Mobile carrier NAT behaviour that breaks UDP mappings. */
    CARRIER_NAT_MAPPING_LOSS,

    /** Server-side misconfiguration (wrong keys, wrong allowed-ips). */
    SERVER_CONFIGURATION_MISMATCH,
}

@Serializable
data class Evidence(
    val kind: EvidenceKind,
    val detail: String,
    val observedAtMillis: Long,
)

@Serializable
enum class EvidenceKind {
    DNS_LOOKUP,
    TCP_CONNECT,
    HANDSHAKE_AGE,
    INTERFACE_STATE,
    ROUTE_STATE,
    CONFIG_CHECK,
    NETWORK_CAPABILITY,
}
