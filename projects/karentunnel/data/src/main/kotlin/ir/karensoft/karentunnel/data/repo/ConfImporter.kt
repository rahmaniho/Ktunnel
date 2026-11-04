package ir.karensoft.karentunnel.data.repo

import ir.karensoft.karentunnel.core.error.ConfigError
import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.ServerId

/**
 * Parses a standard WireGuard `.conf` into normalised fields, or refuses.
 *
 * Why not just call `com.wireguard.config.Config.parse`? Because that parser is
 * permissive about things we refuse to be permissive about (missing DNS, partial
 * IPv6) and because we want a Persian, field-level error story. We do use the
 * library's format rules as the reference for what is legal syntax.
 *
 * Safety properties:
 *  * size is capped before parsing (no zip-bomb style inputs);
 *  * unknown keys are rejected, not ignored — a typo in `AllowedIPs` must not
 *    silently produce a narrower tunnel than the user expects;
 *  * nothing here logs its input, which contains a private key.
 */
object ConfImporter {

    const val MAX_BYTES = 256 * 1024
    const val MAX_PEERS = 1

    private val knownInterfaceKeys = setOf(
        "privatekey", "address", "dns", "mtu", "listenport", "fwmark",
    )
    private val knownPeerKeys = setOf(
        "publickey", "presharedkey", "allowedips", "endpoint", "persistentkeepalive",
    )

    fun import(text: String, serverId: ServerId): Result<ConfigNormalizer.Raw> {
        if (text.toByteArray(Charsets.UTF_8).size > MAX_BYTES) {
            return Result.failure(ConfigError.Malformed("file too large"))
        }

        var section: String? = null
        val iface = mutableMapOf<String, String>()
        val peers = mutableListOf<MutableMap<String, String>>()

        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue

            if (line.startsWith("[")) {
                val name = line.removePrefix("[").removeSuffix("]").trim().lowercase()
                when (name) {
                    "interface" -> section = "interface"
                    "peer" -> {
                        section = "peer"
                        peers.add(mutableMapOf())
                    }
                    else -> return Result.failure(ConfigError.Malformed("unknown section [$name]"))
                }
                continue
            }

            val eq = line.indexOf('=')
            if (eq <= 0) return Result.failure(ConfigError.Malformed("expected key = value"))
            val key = line.substring(0, eq).trim().lowercase()
            val value = line.substring(eq + 1).trim()
            if (value.isEmpty()) return Result.failure(ConfigError.Malformed("empty value for $key"))

            when (section) {
                "interface" -> {
                    if (key !in knownInterfaceKeys) {
                        return Result.failure(ConfigError.Malformed("unknown Interface key '$key'"))
                    }
                    iface[key] = value
                }
                "peer" -> {
                    if (key !in knownPeerKeys) {
                        return Result.failure(ConfigError.Malformed("unknown Peer key '$key'"))
                    }
                    peers.last()[key] = value
                }
                else -> return Result.failure(ConfigError.Malformed("key outside of a section"))
            }
        }

        if (peers.isEmpty()) return Result.failure(ConfigError.Malformed("no [Peer] section"))
        if (peers.size > MAX_PEERS) {
            return Result.failure(ConfigError.Malformed("multiple peers are not supported in 1.0"))
        }

        val peer = peers.single()
        // Note: DNS is deliberately NOT required here. ConfigNormalizer owns that
        // rule and returns ConfigError.MissingDns, which maps to a specific
        // Persian message ("برای جلوگیری از نشتی DNS، پیکربندی رد شد"). Failing
        // earlier would produce a vaguer error for the same problem.
        val address = iface["address"] ?: return Result.failure(ConfigError.MissingAddress)

        return Result.success(
            ConfigNormalizer.Raw(
                serverId = serverId,
                endpoint = peer["endpoint"] ?: return Result.failure(ConfigError.MissingEndpoint),
                address = address,
                dnsServers = splitList(iface["dns"]),
                allowedIps = splitList(peer["allowedips"]),
                peerPublicKey = peer["publickey"]
                    ?: return Result.failure(ConfigError.InvalidPeerPublicKey),
                privateKey = iface["privatekey"]
                    ?: return Result.failure(ConfigError.InvalidPrivateKey),
                presharedKey = peer["presharedkey"],
                mtu = iface["mtu"]?.toIntOrNull(),
                persistentKeepalive = peer["persistentkeepalive"]?.toIntOrNull(),
            ),
        )
    }

    private fun splitList(value: String?): List<String> =
        value?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
}
