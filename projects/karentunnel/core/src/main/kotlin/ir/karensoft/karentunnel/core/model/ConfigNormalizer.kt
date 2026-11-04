package ir.karensoft.karentunnel.core.model

import ir.karensoft.karentunnel.core.error.ConfigError

/**
 * Turns raw, untrusted `.conf` fields into a [TunnelConfig], or refuses.
 *
 * This is the *only* place allowed to decide what a valid configuration is.
 * It enforces the two safety invariants that the underlying library does not:
 *
 * 1. DNS is mandatory (no DNS => the OS falls back to the carrier resolver).
 * 2. IPv6 is either fully tunneled or fully excluded — never "left to chance".
 *    See docs/TECHNICAL-FEASIBILITY.md F1 for why this cannot be delegated.
 *
 * Every rejection returns a specific [ConfigError]; nothing throws for
 * user-supplied input, so the UI can always explain what is wrong in Persian.
 */
object ConfigNormalizer {

    const val MIN_MTU = 576
    const val MAX_MTU = 1500
    const val DEFAULT_MTU = 1280
    const val MAX_DNS_SERVERS = 4

    private val hostRe = Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")
    private val ipv4Re = Regex("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")

    data class Raw(
        val serverId: ServerId,
        val endpoint: String,
        val address: String,
        val dnsServers: List<String>,
        val allowedIps: List<String>,
        val peerPublicKey: String,
        val privateKey: String,
        val presharedKey: String? = null,
        val mtu: Int? = null,
        val persistentKeepalive: Int? = null,
    )

    fun normalize(raw: Raw): Result<TunnelConfig> {
        val endpoint = parseEndpoint(raw.endpoint).getOrElse { return Result.failure(it) }

        val address = raw.address.trim()
        if (address.isEmpty()) return Result.failure(ConfigError.MissingAddress)
        val addrIp = address.substringBefore('/')
        if (!isIpv4(addrIp) && !isIpv6(addrIp)) {
            return Result.failure(ConfigError.InvalidAddress(address))
        }

        val privateKey = raw.privateKey.trim()
        if (!isValidWireGuardKey(privateKey)) {
            return Result.failure(ConfigError.InvalidPrivateKey)
        }
        if (privateKey == raw.peerPublicKey.trim()) {
            // A config where the peer key equals our key cannot handshake and
            // usually means the user pasted the wrong block.
            return Result.failure(ConfigError.KeyReuseNotAllowed)
        }
        if (!isValidWireGuardKey(raw.peerPublicKey.trim())) {
            return Result.failure(ConfigError.InvalidPeerPublicKey)
        }
        val psk = raw.presharedKey?.trim()?.takeIf { it.isNotEmpty() }
        if (psk != null && !isValidWireGuardKey(psk)) {
            return Result.failure(ConfigError.InvalidPresharedKey)
        }

        // --- DNS: mandatory, bounded, validated -----------------------------
        val dns = raw.dnsServers.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (dns.isEmpty()) return Result.failure(ConfigError.MissingDns)
        if (dns.size > MAX_DNS_SERVERS) return Result.failure(ConfigError.TooManyDnsServers(dns.size))
        dns.firstOrNull { !isIpLiteral(it) }?.let { return Result.failure(ConfigError.InvalidDns(it)) }

        // --- IPv6 decision --------------------------------------------------
        val allowedRaw = raw.allowedIps.map { it.trim() }.filter { it.isNotEmpty() }
        if (allowedRaw.isEmpty()) return Result.failure(ConfigError.MissingAllowedIps)

        // The IPv6 decision is taken from the *default route*, not from the
        // presence of any v6 value. A stray "fd00::/8" in allowed-ips must not be
        // read as "IPv6 is tunneled"; only "::/0" makes that true (see F1).
        val hasIpv6DefaultRoute = allowedRaw.any { isIpv6DefaultRoute(it) }
        val hasIpv6InterfaceAddress = address.contains(':')
        val v6Dns = dns.filter { isIpv6(it) }

        val allowedIps: List<String>
        val ipv6Policy: Ipv6Policy
        val finalDns: List<String>

        if (hasIpv6DefaultRoute) {
            // Everything v6 is inside the tunnel, so v6 resolvers are reachable.
            allowedIps = allowedRaw.distinct()
            ipv6Policy = Ipv6Policy.TUNNELED
            finalDns = dns
        } else {
            if (hasIpv6InterfaceAddress) {
                // v6 address on the interface without a v6 default route: the OS
                // could emit IPv6 outside the tunnel. Refuse rather than pretend.
                return Result.failure(ConfigError.Ipv6NotFullyTunneled)
            }
            if (v6Dns.size == dns.size) {
                // Every resolver was v6 and v6 is excluded => no working DNS.
                return Result.failure(ConfigError.Ipv6OnlyDnsUnsupported)
            }
            // Stray v6 routes are dropped so the OS never gets a v6 path.
            allowedIps = allowedRaw.filterNot { it.contains(':') }.distinct()
            ipv6Policy = Ipv6Policy.EXCLUDED
            finalDns = dns.filterNot { isIpv6(it) }
        }

        if (allowedIps.isEmpty()) return Result.failure(ConfigError.MissingAllowedIps)

        val mtu = raw.mtu ?: DEFAULT_MTU
        if (mtu !in MIN_MTU..MAX_MTU) return Result.failure(ConfigError.InvalidMtu(mtu))

        val keepalive = raw.persistentKeepalive
        if (keepalive != null && keepalive !in 0..65535) {
            return Result.failure(ConfigError.InvalidKeepalive(keepalive))
        }

        val config = TunnelConfig(
            serverId = raw.serverId,
            protocol = Protocol.WIREGUARD,
            endpointHost = endpoint.host,
            endpointPort = endpoint.port,
            address = address,
            dnsServers = finalDns,
            mtu = mtu,
            allowedIps = allowedIps,
            ipv6Policy = ipv6Policy,
            peerPublicKey = raw.peerPublicKey.trim(),
            privateKey = SensitiveString.of(privateKey),
            presharedKey = psk?.let(SensitiveString::of),
            persistentKeepaliveSeconds = keepalive?.takeIf { it > 0 },
        )

        // Last line of defence: the config must satisfy its own invariants.
        check(config.isIpv6PolicyConsistent()) {
            "normaliser produced an inconsistent IPv6 policy"
        }
        return Result.success(config)
    }

    data class Endpoint(val host: String, val port: Int)

    fun parseEndpoint(raw: String): Result<Endpoint> {
        val value = raw.trim()
        if (value.isEmpty()) return Result.failure(ConfigError.MissingEndpoint)

        // [2001:db8::1]:51820 form
        if (value.startsWith("[")) {
            val close = value.indexOf(']')
            if (close <= 1) return Result.failure(ConfigError.InvalidEndpoint(raw))
            val host = value.substring(1, close)
            val portPart = value.substring(close + 1)
            if (!portPart.startsWith(":")) return Result.failure(ConfigError.MissingEndpointPort)
            val port = portPart.removePrefix(":").toIntOrNull()
                ?: return Result.failure(ConfigError.InvalidPort(portPart.removePrefix(":")))
            if (!isIpv6(host)) return Result.failure(ConfigError.InvalidEndpoint(raw))
            if (port !in 1..65535) return Result.failure(ConfigError.InvalidPort(port.toString()))
            return Result.success(Endpoint(host, port))
        }

        val colon = value.lastIndexOf(':')
        if (colon <= 0) return Result.failure(ConfigError.MissingEndpointPort)
        val host = value.substring(0, colon)
        val port = value.substring(colon + 1).toIntOrNull()
            ?: return Result.failure(ConfigError.InvalidPort(value.substring(colon + 1)))
        if (port !in 1..65535) return Result.failure(ConfigError.InvalidPort(port.toString()))
        if (!isIpv4(host) && !hostRe.matches(host)) {
            return Result.failure(ConfigError.InvalidEndpoint(raw))
        }
        return Result.success(Endpoint(host, port))
    }

    /** WireGuard keys are canonical base64 of 32 bytes. */
    fun isValidWireGuardKey(key: String): Boolean {
        if (key.length != 44 || !key.endsWith("=")) return false
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        if (key.dropLast(1).any { it !in alphabet }) return false
        return true
    }

    fun isIpv4(value: String): Boolean {
        val m = ipv4Re.matchEntire(value) ?: return false
        return (1..4).all { m.groupValues[it].toInt() in 0..255 }
    }

    fun isIpv6(value: String): Boolean {
        if (!value.contains(':')) return false
        // Reject a bare "::" style value with more than one "::".
        if (value.count { it == ':' } > 8 && "::" !in value) return false
        if (Regex("::.*::").containsMatchIn(value)) return false
        return value.all { it.isDigit() || it in "abcdefABCDEF:" || it == '.' }
    }

    fun isIpLiteral(value: String): Boolean = isIpv4(value) || isIpv6(value)

    private fun isIpv6DefaultRoute(cidr: String): Boolean {
        val (ip, prefix) = splitCidr(cidr) ?: return false
        return isIpv6(ip) && prefix == 0
    }

    fun splitCidr(cidr: String): Pair<String, Int>? {
        val slash = cidr.indexOf('/')
        if (slash <= 0 || slash == cidr.length - 1) return null
        val ip = cidr.substring(0, slash)
        val prefix = cidr.substring(slash + 1).toIntOrNull() ?: return null
        val max = if (isIpv6(ip)) 128 else 32
        if (prefix !in 0..max) return null
        return ip to prefix
    }
}
