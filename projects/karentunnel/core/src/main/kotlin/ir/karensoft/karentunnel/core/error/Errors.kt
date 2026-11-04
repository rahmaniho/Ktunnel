package ir.karensoft.karentunnel.core.error

/**
 * Reasons a configuration was refused. Each maps to one Persian message in the
 * UI (see ui/text/ErrorText.kt) — never to a raw exception string, because raw
 * messages can echo secrets back to the screen.
 */
sealed interface ConfigError {
    data object MissingEndpoint : ConfigError
    data object MissingEndpointPort : ConfigError
    data object MissingAddress : ConfigError
    data object MissingDns : ConfigError
    data object MissingAllowedIps : ConfigError
    data object InvalidEndpoints : ConfigError
    data object InvalidPrivateKey : ConfigError
    data object InvalidPeerPublicKey : ConfigError
    data object InvalidPresharedKey : ConfigError
    data object KeyReuseNotAllowed : ConfigError

    /** IPv6 is partly exposed: interface has v6 but allowed-ips lacks ::/0. */
    data object Ipv6NotFullyTunneled : ConfigError

    /** Every configured DNS server is IPv6 while IPv6 is not tunneled. */
    data object Ipv6OnlyDnsUnsupported : ConfigError

    data class InvalidEndpoint(val raw: String) : ConfigError
    data class InvalidPort(val raw: String) : ConfigError
    data class InvalidAddress(val raw: String) : ConfigError
    data class InvalidDns(val raw: String) : ConfigError
    data class InvalidMtu(val value: Int) : ConfigError
    data class InvalidKeepalive(val value: Int) : ConfigError
    data class TooManyDnsServers(val count: Int) : ConfigError

    /** The .conf file could not be parsed at all. */
    data class Malformed(val detail: String) : ConfigError
}

/**
 * Connection failures, classified so that the retry policy can act on them.
 *
 * The classification is deliberately conservative: anything we cannot prove is
 * [Transient], which means "retry a bounded number of times", never "loop".
 */
sealed interface ConnectionError {
    /** The user (or the OS) has not granted our VPN consent. */
    data object PermissionRequired : ConnectionError

    /** Consent was revoked while connected (VpnService.onRevoke). */
    data object PermissionRevoked : ConnectionError

    /** No usable network at all. */
    data object NoNetwork : ConnectionError

    /** Endpoint name did not resolve. */
    data class DnsResolutionFailed(val host: String) : ConnectionError

    /** TCP/UDP connect or handshake timed out. */
    data object Timeout : ConnectionError

    /** Tunnel interface could not be created or brought up. */
    data class TunnelSetupFailed(val reason: String) : ConnectionError

    /** Library reported a failure we cannot classify further. */
    data class BackendFailure(val reason: String) : ConnectionError

    /** No handshake observed in time: "connected" would be a lie. */
    data object HandshakeTimeout : ConnectionError

    /** Tunnel came up, then the handshake went stale. */
    data object TunnelDegraded : ConnectionError

    /** Anything unclassified. Treated as retryable but logged precisely. */
    data class Transient(val reason: String) : ConnectionError
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
