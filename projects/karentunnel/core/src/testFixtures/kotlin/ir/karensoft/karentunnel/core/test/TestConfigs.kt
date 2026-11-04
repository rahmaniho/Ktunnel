package ir.karensoft.karentunnel.core.test

import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.ServerId

/**
 * Deterministic, clearly-fake material for tests only.
 *
 * PRIVATE_KEY is a *valid-format* base64 key ("test" repeated then padded).
 * It is not, and must never be, a working credential for any server. It exists
 * so format validation and redaction can be exercised.
 */
object TestConfigs {

    const val PRIVATE_KEY = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q="
    const val PEER_KEY = "cGVlcnBlZXJwZWVycGVlcnBlZXJwZWVycGVlcnBlZXI="
    const val PSK_KEY = "cHNrY3BrY3BrY3BrY3BrY3BrY3BrY3BrY3BrY3BrY3A="

    fun rawIpv4Only(): ConfigNormalizer.Raw = ConfigNormalizer.Raw(
        serverId = ServerId("local-test-1"),
        endpoint = "vpn.example.com:51820",
        address = "10.66.0.2/32",
        dnsServers = listOf("10.66.0.1"),
        allowedIps = listOf("0.0.0.0/0"),
        peerPublicKey = PEER_KEY,
        privateKey = PRIVATE_KEY,
    )

    fun rawDualStack(): ConfigNormalizer.Raw = rawIpv4Only().copy(
        address = "10.66.0.2/32",
        dnsServers = listOf("10.66.0.1"),
        allowedIps = listOf("0.0.0.0/0", "::/0"),
    )
}
