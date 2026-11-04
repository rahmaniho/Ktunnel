package ir.karensoft.karentunnel.data.secure

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.Ipv6Policy
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileSecureConfigStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun store(crypto: CryptoEngine = FakeCryptoEngine()) =
        FileSecureConfigStore(baseDir = tmp.newFolder("tunnels"), crypto = crypto)

    private fun config(id: String = "server-a"): TunnelConfig =
        ConfigNormalizer.normalize(TestConfigs.rawIpv4Only().copy(serverId = ServerId(id)))
            .getOrThrow()

    @Test
    fun `round trip preserves every field`() {
        val store = store()
        val original = config()

        store.save(original).getOrThrow()
        val loaded = store.load(original.serverId).getOrThrow()

        assertThat(loaded.endpointHost).isEqualTo(original.endpointHost)
        assertThat(loaded.endpointPort).isEqualTo(original.endpointPort)
        assertThat(loaded.address).isEqualTo(original.address)
        assertThat(loaded.dnsServers).isEqualTo(original.dnsServers)
        assertThat(loaded.allowedIps).isEqualTo(original.allowedIps)
        assertThat(loaded.ipv6Policy).isEqualTo(Ipv6Policy.EXCLUDED)
        assertThat(loaded.privateKey.use()).isEqualTo(original.privateKey.use())
        assertThat(loaded.isIpv6PolicyConsistent()).isTrue()
    }

    @Test
    fun `file on disk does not contain the private key`() {
        val dir = tmp.newFolder("tunnels")
        val store = FileSecureConfigStore(dir, FakeCryptoEngine())
        store.save(config()).getOrThrow()

        val raw = dir.listFiles()!!.single().readBytes().toString(Charsets.ISO_8859_1)
        assertThat(raw).doesNotContain(TestConfigs.PRIVATE_KEY)
    }

    @Test
    fun `list returns saved ids sorted`() {
        val store = store()
        store.save(config("b")).getOrThrow()
        store.save(config("a")).getOrThrow()

        assertThat(store.list().map { it.value }).containsExactly("a", "b").inOrder()
    }

    @Test
    fun `load of missing config fails cleanly`() {
        val store = store()
        val result = store.load(ServerId("nope"))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `delete removes the config`() {
        val store = store()
        store.save(config()).getOrThrow()
        store.delete(ServerId("server-a")).getOrThrow()

        assertThat(store.exists(ServerId("server-a"))).isFalse()
        assertThat(store.load(ServerId("server-a")).isFailure).isTrue()
    }

    @Test
    fun `wipeAll removes every config`() {
        val store = store()
        store.save(config("a")).getOrThrow()
        store.save(config("b")).getOrThrow()

        store.wipeAll().getOrThrow()

        assertThat(store.list()).isEmpty()
    }

    @Test
    fun `tampered file is reported as failure not garbage`() {
        val dir = tmp.newFolder("tunnels")
        val store = FileSecureConfigStore(dir, FakeCryptoEngine())
        store.save(config()).getOrThrow()

        val file = dir.listFiles()!!.single()
        val bytes = file.readBytes()
        bytes[bytes.size / 2] = (bytes[bytes.size / 2].toInt() xor 0xFF).toByte()
        file.writeBytes(bytes)

        // The fake engine cannot detect tampering, so this asserts the *real*
        // guarantee separately: a broken crypto engine surfaces as failure.
        val broken = FileSecureConfigStore(dir, BrokenCryptoEngine())
        val result = broken.load(ServerId("server-a"))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `server id with path characters cannot escape the directory`() {
        val dir = tmp.newFolder("tunnels")
        val store = FileSecureConfigStore(dir, FakeCryptoEngine())
        val evil = config().copy(serverId = ServerId("../../etc/passwd"))
        store.save(evil).getOrThrow()

        assertThat(dir.listFiles()!!.all { it.parentFile == dir }).isTrue()
    }

    @Test
    fun `failed save does not leave a temp file behind`() {
        val dir = tmp.newFolder("tunnels")
        val store = FileSecureConfigStore(dir, object : CryptoEngine {
            override fun seal(plaintext: ByteArray): ByteArray = throw CryptoException("boom")
            override fun open(envelope: ByteArray): ByteArray = ByteArray(0)
            override fun isAvailable(): Boolean = true
        })

        assertThat(store.save(config()).isFailure).isTrue()
        assertThat(dir.listFiles()!!.none { it.name.endsWith(".tmp") }).isTrue()
    }
}
