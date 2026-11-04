package ir.karensoft.karentunnel.core.model

import kotlinx.serialization.Serializable

/**
 * Everything in this file is pure Kotlin: no Android imports, no I/O.
 * That keeps the safety-critical normalisation logic testable on the JVM.
 */

/** Stable identifier for a user-configured server. Never derived from a secret. */
@JvmInline
@Serializable
value class ServerId(val value: String) {
    init {
        require(value.isNotBlank()) { "ServerId must not be blank" }
        require(value.length <= 64) { "ServerId too long" }
    }
}

/** Transport family of the tunnel. Only WireGuard ships in 1.0.0 (ADR-001). */
enum class Protocol {
    WIREGUARD,
}

/**
 * What the app decided to do about IPv6 for a given configuration.
 *
 * There is deliberately no `AUTO` value: the decision must be explicit and
 * auditable because of the library behaviour documented as F1 in
 * docs/TECHNICAL-FEASIBILITY.md (GoBackend calls allowFamily(AF_INET6)
 * whenever no default route is seen, which can leak IPv6 outside the tunnel).
 */
enum class Ipv6Policy {
    /** IPv6 is routed through the tunnel (`::/0` present). */
    TUNNELED,

    /** IPv6 addresses/routes are stripped so the OS cannot use IPv6 at all. */
    EXCLUDED,
}

/**
 * A validated, normalised tunnel configuration.
 *
 * [privateKey] is the only sensitive field. It must never reach: UI state,
 * logs, crash reports, or persisted plaintext. See [SensitiveString] and
 * data/secure for the storage contract.
 */
data class TunnelConfig(
    val serverId: ServerId,
    val protocol: Protocol,
    /** e.g. "vpn.example.net:51820", already parsed into host + port. */
    val endpointHost: String,
    val endpointPort: Int,
    /** Interface address, e.g. "10.7.0.2/32". */
    val address: String,
    /** DNS servers that must all be reachable through the tunnel. Never empty. */
    val dnsServers: List<String>,
    val mtu: Int,
    /** `0.0.0.0/0` style allowed IPs after normalisation. */
    val allowedIps: List<String>,
    val ipv6Policy: Ipv6Policy,
    val peerPublicKey: String,
    val privateKey: SensitiveString,
    val presharedKey: SensitiveString? = null,
    val persistentKeepaliveSeconds: Int? = null,
) {
    /** True when this config will route all IPv4 traffic through the tunnel. */
    val routesAllIpv4: Boolean get() = allowedIps.any { it == "0.0.0.0/0" }

    /**
     * Defence in depth: a config that carries IPv6 routes must be marked
     * TUNNELED, and one without them must not claim TUNNELED.
     */
    fun isIpv6PolicyConsistent(): Boolean {
        val hasIpv6Route = allowedIps.any { it.contains(':') }
        return when (ipv6Policy) {
            Ipv6Policy.TUNNELED -> hasIpv6Route
            Ipv6Policy.EXCLUDED -> !hasIpv6Route
        }
    }
}

/**
 * Wrapper that makes accidental disclosure hard: toString() and the whole
 * CharSequence surface are redacted, and values are compared CONSTANT-TIME
 * to avoid turning a comparison into an oracle.
 */
class SensitiveString private constructor(private val value: CharArray) {

    fun use(): String = value.concatToString()

    /** Only call sites that must hand the secret to the tunnel library. */
    fun copyChars(): CharArray = value.copyOf()

    fun wipe() = value.fill('\u0000')

    override fun toString(): String = REDACTED

    override fun equals(other: Any?): Boolean =
        other is SensitiveString && constantTimeEquals(value, other.value)

    override fun hashCode(): Int = value.size

    companion object {
        const val REDACTED = "***redacted***"

        fun of(raw: String): SensitiveString = SensitiveString(raw.toCharArray())

        private fun constantTimeEquals(a: CharArray, b: CharArray): Boolean {
            if (a.size != b.size) return false
            var diff = 0
            for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
            return diff == 0
        }
    }
}
