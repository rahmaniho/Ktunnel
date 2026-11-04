package ir.karensoft.karentunnel.data.repo

import com.google.common.truth.Truth.assertThat
import ir.karensoft.karentunnel.core.error.ConfigError
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.data.secure.SecureConfigStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServerRepositoryTest {

    /** In-memory store: the repository's job is policy, not file I/O. */
    private class FakeStore : SecureConfigStore {
        val saved = linkedMapOf<String, TunnelConfig>()
        val unreadable: MutableSet<String> = mutableSetOf()

        override fun list(): List<ServerId> = saved.keys.map(::ServerId)

        override fun save(config: TunnelConfig): Result<Unit> {
            saved[config.serverId.value] = config
            return Result.success(Unit)
        }

        override fun load(id: ServerId): Result<TunnelConfig> =
            if (id.value in unreadable) {
                Result.failure(ConfigError.Malformed("cannot decrypt"))
            } else {
                saved[id.value]?.let { Result.success(it) }
                    ?: Result.failure(ConfigError.Malformed("missing"))
            }

        override fun delete(id: ServerId): Result<Unit> {
            saved.remove(id.value)
            return Result.success(Unit)
        }

        override fun exists(id: ServerId): Boolean = id.value in saved

        override fun wipeAll(): Result<Unit> {
            saved.clear()
            return Result.success(Unit)
        }
    }

    private val conf = """
        [Interface]
        PrivateKey = dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q=
        Address = 10.66.0.2/32
        DNS = 10.66.0.1

        [Peer]
        PublicKey = cGVlcnBlZXJwZWVycGVlcnBlZXJwZWVycGVlcnBlZXI=
        AllowedIPs = 0.0.0.0/0
        Endpoint = vpn.example.com:51820
    """.trimIndent()

    @Test
    fun `import stores a readable server`() = runTest {
        val store = FakeStore()
        val repo = ServerRepository(store)

        val summary = repo.import(conf, ServerId("s1")).getOrThrow()

        assertThat(summary.endpointHost).isEqualTo("vpn.example.com")
        assertThat(summary.endpointPort).isEqualTo(51820)
        assertThat(summary.dnsServers).containsExactly("10.66.0.1")
        assertThat(repo.snapshot().servers).hasSize(1)
    }

    @Test
    fun `invalid config is refused and nothing is written`() = runTest {
        val store = FakeStore()
        val repo = ServerRepository(store)

        val result = repo.import("this is not a wireguard config", ServerId("bad"))

        assertThat(result.isFailure).isTrue()
        assertThat(store.saved).isEmpty()
    }

    @Test
    fun `one unreadable file does not hide the readable ones`() = runTest {
        val store = FakeStore()
        val repo = ServerRepository(store)
        repo.import(conf, ServerId("good"))

        // A config that exists on disk but cannot be decrypted.
        store.saved["broken"] = store.saved.getValue("good").copy(serverId = ServerId("broken"))
        store.unreadable += "broken"

        val all = repo.snapshot()

        assertThat(all.servers).hasSize(1)
        assertThat(all.servers.single().id).isEqualTo(ServerId("good"))
        assertThat(all.unreadable).isEqualTo(1)
    }

    @Test
    fun `deleting removes it from the snapshot`() = runTest {
        val store = FakeStore()
        val repo = ServerRepository(store)
        repo.import(conf, ServerId("s1"))

        repo.delete(ServerId("s1"))

        assertThat(repo.snapshot().servers).isEmpty()
    }

    @Test
    fun `derived id is stable for the same endpoint`() {
        val a = ServerIdFactory.fromEndpoint("vpn.example.com", 51820)
        val b = ServerIdFactory.fromEndpoint("vpn.example.com", 51820)

        assertThat(a).isEqualTo(b)
        assertThat(a.value).isEqualTo("wg-vpn.example.com-51820")
    }

    @Test
    fun `derived id cannot contain path characters`() {
        val id = ServerIdFactory.fromEndpoint("../../etc/passwd", 1)

        assertThat(id.value).doesNotContain("/")
        assertThat(id.value).doesNotContain("..")
    }
}
