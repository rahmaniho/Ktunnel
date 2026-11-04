package ir.karensoft.karentunnel.core.error

/**
 * Reasons a configuration was refused. Each maps to one Persian message in the
 * UI (see ui/text/ErrorText.kt) — never to a raw exception string, because raw
 * messages can echo secrets back to the screen.
 */
sealed class ConfigError(message: String? = null) : Exception(message) {
    data object MissingEndpoint : ConfigError("missing endpoint")
    data object MissingEndpointPort : ConfigError("missing endpoint port")
    data object MissingAddress : ConfigError("missing address")
    data object MissingDns : ConfigError("missing dns")
    data object MissingAllowedIps : ConfigError("missing allowed ips")
    data object InvalidEndpoints : ConfigError("invalid endpoint")
    data object InvalidPrivateKey : ConfigError("invalid private key")
    data object InvalidPeerPublicKey : ConfigError("invalid peer public key")
    data object InvalidPresharedKey : ConfigError("invalid preshared key")
    data object KeyReuseNotAllowed : ConfigError("key reuse")

    /** IPv6 is partly exposed: interface has v6 but allowed-ips lacks ::/0. */
    data object Ipv6NotFullyTunneled : ConfigError("ipv6 not fully tunneled")

    /** Every configured DNS server is IPv6 while IPv6 is not tunneled. */
    data object Ipv6OnlyDnsUnsupported : ConfigError("ipv6-only dns")

    data class InvalidEndpoint(val raw: String) : ConfigError("invalid endpoint: $raw")
    data class InvalidPort(val raw: String) : ConfigError("invalid port: $raw")
    data class InvalidAddress(val raw: String) : ConfigError("invalid address: $raw")
    data class InvalidDns(val raw: String) : ConfigError("invalid dns: $raw")
    data class InvalidMtu(val value: Int) : ConfigError("invalid mtu: $value")
    data class InvalidKeepalive(val value: Int) : ConfigError("invalid keepalive: $value")
    data class TooManyDnsServers(val count: Int) : ConfigError("too many dns: $count")

    /** The .conf file could not be parsed at all. */
    data class Malformed(val detail: String) : ConfigError("malformed: $detail")
}

/**
 * Connection failures, classified so that the retry policy can act on them.
 *
 * The classification is deliberately conservative: anything we cannot prove is
 * [Transient], which means "retry a bounded number of times", never "loop".
 */
sealed class ConnectionError(message: String? = null) : Exception(message) {
    /** The user (or the OS) has not granted our VPN consent. */
    data object PermissionRequired : ConnectionError("permission required")

    /** Consent was revoked while connected (VpnService.onRevoke). */
    data object PermissionRevoked : ConnectionError("permission revoked")

    /** No usable network at all. */
    data object NoNetwork : ConnectionError("no network")

    /** Endpoint name did not resolve. */
    data class DnsResolutionFailed(val host: String) : ConnectionError("dns resolution failed: $host")

    /** TCP/UDP connect or handshake timed out. */
    data object Timeout : ConnectionError("timeout")

    /** Tunnel interface could not be created or brought up. */
    data class TunnelSetupFailed(val reason: String) : ConnectionError("tunnel setup failed: $reason")

    /** Library reported a failure we cannot classify further. */
    data class BackendFailure(val reason: String) : ConnectionError("backend failure: $reason")

    /** No handshake observed in time: "connected" would be a lie. */
    data object HandshakeTimeout : ConnectionError("handshake timeout")

    /** Tunnel came up, then the handshake went stale. */
    data object TunnelDegraded : ConnectionError("tunnel degraded")

    /** Anything unclassified. Treated as retryable but logged precisely. */
    data class Transient(val reason: String) : ConnectionError("transient: $reason")
}

/**
 * Whether the connection manager may retry a given failure.
 *
 * Hard rule: no unbounded retries exist. Even [Retryable] is capped by
 * RetryPolicy.maxAttempts.
 */
val ConnectionError.isRetryable: Boolean
    get() = when (this) {
        ConnectionError.Timeout,
        ConnectionError.HandshakeTimeout,
        ConnectionError.TunnelDegraded,
        is ConnectionError.Transient,
        is ConnectionError.BackendFailure,
        is ConnectionError.DnsResolutionFailed,
        is ConnectionError.TunnelSetupFailed,
        -> true

        ConnectionError.PermissionRequired,
        ConnectionError.PermissionRevoked,
        ConnectionError.NoNetwork,
        -> false
    }

/** Failures that mean "the user must act before anything can work". */
val ConnectionError.requiresUserAction: Boolean
    get() = when (this) {
        ConnectionError.PermissionRequired,
        ConnectionError.PermissionRevoked,
        ConnectionError.NoNetwork,
        -> true
        else -> false
    }
