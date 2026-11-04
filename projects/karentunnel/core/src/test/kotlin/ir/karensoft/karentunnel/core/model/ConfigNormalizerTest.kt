package ir.karensoft.karentunnel.core.model

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.error.ConfigError
import ir.karensoft.karentunnel.core.test.TestConfigs
import org.junit.Test

/**
 * These tests encode the two safety invariants from the feasibility report:
 * DNS is mandatory, and IPv6 is either fully tunneled or fully excluded.
 */
class ConfigNormalizerTest {

    // ---------------------------------------------------------------- happy path

    @Test
    fun `ipv4 only config excludes IPv6`() {
        val result = ConfigNormalizer.normalize(TestConfigs.rawIpv4Only())
        val config = result.getOrThrow()

        assertThat(config.ipv6Policy).isEqualTo(Ipv6Policy.EXCLUDED)
        assertThat(config.allowedIps).containsExactly("0.0.0.0/0")
        assertThat(config.dnsServers).containsExactly("10.66.0.1")
        assertThat(config.isIpv6PolicyConsistent()).isTrue()
    }

    @Test
    fun `dual stack config tunnels IPv6`() {
        val result = ConfigNormalizer.normalize(TestConfigs.rawDualStack())
        val config = result.getOrThrow()

        assertThat(config.ipv6Policy).isEqualTo(Ipv6Policy.TUNNELED)
        assertThat(config.allowedIps).containsExactly("0.0.0.0/0", "::/0")
        assertThat(config.isIpv6PolicyConsistent()).isTrue()
    }

    @Test
    fun `ipv6 entries are stripped when policy is excluded`() {
        // allowed-ips has stray v6 but no ::/0 and no v6 interface address.
        val raw = TestConfigs.rawIpv4Only().copy(
            allowedIps = listOf("0.0.0.0/0", "fd00::/8"),
            dnsServers = listOf("10.66.0.1", "fd00::1"),
        )
        val config = ConfigNormalizer.normalize(raw).getOrThrow()

        assertThat(config.allowedIps).containsExactly("0.0.0.0/0")
        assertThat(config.dnsServers).containsExactly("10.66.0.1")
        assertThat(config.ipv6Policy).isEqualTo(Ipv6Policy.EXCLUDED)
    }

    // ------------------------------------------------------- F1 regression guard

    @Test
    fun `ipv6 interface address without default route is refused`() {
        // This is the leak scenario: v6 address present, no ::/0 route.
        val raw = TestConfigs.rawIpv4Only().copy(
            address = "fd00::2/128",
            allowedIps = listOf("0.0.0.0/0"),
        )
        val error = ConfigNormalizer.normalize(raw).exceptionOrNull()

        assertThat(error).isEqualTo(ConfigError.Ipv6NotFullyTunneled)
    }

    @Test
    fun `ipv6 only dns is refused when ipv6 cannot be tunneled`() {
        val raw = TestConfigs.rawIpv4Only().copy(dnsServers = listOf("fd00::1"))
        val error = ConfigNormalizer.normalize(raw).exceptionOrNull()

        assertThat(error).isEqualTo(ConfigError.Ipv6OnlyDnsUnsupported)
    }

    @Test
    fun `never produces TUNNELED without a v6 default route`() {
        val raw = TestConfigs.rawDualStack().copy(allowedIps = listOf("0.0.0.0/0", "fd00::/8"))
        val config = ConfigNormalizer.normalize(raw).getOrNull()

        // Either refused, or downgraded to EXCLUDED — never a false TUNNELED.
        if (config != null) {
            assertThat(config.ipv6Policy).isEqualTo(Ipv6Policy.EXCLUDED)
            assertThat(config.allowedIps.none { it.contains(':') }).isTrue()
        }
    }

    // ------------------------------------------------------------ DNS enforcement

    @Test
    fun `missing dns is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(dnsServers = emptyList())
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.MissingDns)
    }

    @Test
    fun `blank dns entries do not count as dns`() {
        val raw = TestConfigs.rawIpv4Only().copy(dnsServers = listOf("  ", ""))
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.MissingDns)
    }

    @Test
    fun `non-literal dns entry is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(dnsServers = listOf("dns.example.com"))
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.InvalidDns("dns.example.com"))
    }

    @Test
    fun `too many dns servers is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(
            dnsServers = listOf("10.0.0.1", "10.0.0.2", "10.0.0.3", "10.0.0.4", "10.0.0.5"),
        )
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.TooManyDnsServers(5))
    }

    // ------------------------------------------------------------------- keys

    @Test
    fun `key equal to peer key is refused`() {
        val raw = TestConfigs.rawIpv4Only().let { it.copy(peerPublicKey = it.privateKey) }
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.KeyReuseNotAllowed)
    }

    @Test
    fun `malformed private key is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(privateKey = "not-a-key")
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.InvalidPrivateKey)
    }

    @Test
    fun `secret is redacted in toString`() {
        val config = ConfigNormalizer.normalize(TestConfigs.rawIpv4Only()).getOrThrow()
        assertThat(config.toString()).doesNotContain(TestConfigs.PRIVATE_KEY)
        assertThat(config.privateKey.toString()).contains("redacted")
        assertThat(config.privateKey.use()).isEqualTo(TestConfigs.PRIVATE_KEY)
    }

    // -------------------------------------------------------------- endpoint

    @Test
    fun `endpoint without port is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(endpoint = "vpn.example.com")
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.MissingEndpointPort)
    }

    @Test
    fun `port out of range is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(endpoint = "vpn.example.com:70000")
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.InvalidPort("70000"))
    }

    @Test
    fun `ipv6 literal endpoint is parsed`() {
        val parsed = ConfigNormalizer.parseEndpoint("[2001:db8::1]:51820").getOrThrow()
        assertThat(parsed.host).isEqualTo("2001:db8::1")
        assertThat(parsed.port).isEqualTo(51820)
    }

    @Test
    fun `mtu outside range is refused`() {
        val raw = TestConfigs.rawIpv4Only().copy(mtu = 9000)
        assertThat(ConfigNormalizer.normalize(raw).exceptionOrNull())
            .isEqualTo(ConfigError.InvalidMtu(9000))
    }

    @Test
    fun `default mtu is applied`() {
        val config = ConfigNormalizer.normalize(TestConfigs.rawIpv4Only()).getOrThrow()
        assertThat(config.mtu).isEqualTo(ConfigNormalizer.DEFAULT_MTU)
    }

    // ------------------------------------------------------------- key format

    @Test
    fun `canonical base64 key of 32 bytes is accepted`() {
        assertThat(ConfigNormalizer.isValidWireGuardKey(TestConfigs.PRIVATE_KEY)).isTrue()
    }

    @Test
    fun `keys with wrong length are rejected`() {
        assertThat(ConfigNormalizer.isValidWireGuardKey("abc=")).isFalse()
        assertThat(ConfigNormalizer.isValidWireGuardKey(TestConfigs.PRIVATE_KEY + "x")).isFalse()
    }

    @Test
    fun `keys with invalid characters are rejected`() {
        assertThat(ConfigNormalizer.isValidWireGuardKey(TestConfigs.PRIVATE_KEY.dropLast(1) + "!"))
            .isFalse()
    }
}
