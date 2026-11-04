package ir.karensoft.karentunnel.data.repo

import ir.karensoft.karentunnel.core.model.ConfigNormalizer
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import ir.karensoft.karentunnel.data.secure.SecureConfigStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * A server as the UI is allowed to see it.
 *
 * Deliberately contains **no secret**: no private key, no preshared key. The
 * list screen, the settings screen and any future export feature work from this
 * type, so a private key cannot be rendered, logged or shared by accident.
 */
data class ServerSummary(
    val id: ServerId,
    /** Display label. Always derived from the config, never invented. */
    val endpointHost: String,
    val endpointPort: Int,
    val addressCidr: String,
    val dnsServers: List<String>,
    val mtu: Int,
    val ipv6PolicyLabel: String,
    val allowedIpCount: Int,
)

/**
 * The one place that reads and writes servers.
 *
 * Why a separate type rather than letting screens call [SecureConfigStore]:
 *  * the store speaks [TunnelConfig], which carries the private key. Handing that
 *    to a ViewModel invites it into UI state, saved-state bundles and logs.
 *  * listing has to tolerate one corrupt file: a single unreadable tunnel must not
 *    make the whole screen fail.
 */
class ServerRepository(
    private val store: SecureConfigStore,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {

    /** Bumped after every mutation so [servers] re-reads the store. */
    private val revisions = MutableStateFlow(0)

    /**
     * Every readable server, sorted by label.
     *
     * A config that fails to load is skipped and counted, never fabricated: the
     * UI can then say "یک پیکربندی خوانده نشد" instead of silently showing a
     * shorter list as if nothing were wrong.
     */
    val servers: Flow<List<ServerSummary>> = revisions.asStateFlow().map { loadAll().servers }

    /** Number of stored files that could not be decrypted or parsed. */
    val unreadableCount: Flow<Int> = revisions.asStateFlow().map { loadAll().unreadable }

    suspend fun loadAll(): ServerList = withContext(io) {
        val ids = runCatching { store.list() }.getOrDefault(emptyList())
        var unreadable = 0
        val summaries = ids.mapNotNull { id ->
            val config = store.load(id).getOrNull()
            if (config == null) {
                unreadable++
                null
            } else {
                config.toSummary()
            }
        }
        ServerList(
            servers = summaries.sortedBy { it.endpointHost },
            unreadable = unreadable,
        )
    }

    suspend fun snapshot(): ServerList = loadAll()

    /** The full config, including the key. Only for handing to the tunnel. */
    suspend fun configFor(id: ServerId): Result<TunnelConfig> = withContext(io) {
        store.load(id)
    }

    suspend fun summaryFor(id: ServerId): ServerSummary? = withContext(io) {
        store.load(id).getOrNull()?.toSummary()
    }

    /**
     * Imports `.conf` text under [id].
     *
     * Import is transactional with respect to the store: the text is parsed and
     * normalised *before* anything is written, so an invalid paste leaves no
     * half-created server behind.
     */
    suspend fun import(text: String, id: ServerId): Result<ServerSummary> = withContext(io) {
        ConfImporter.import(text, id)
            .mapCatching { raw -> ConfigNormalizer.normalize(raw).getOrThrow() }
            .mapCatching { config ->
                store.save(config).getOrThrow()
                bump()
                config.toSummary()
            }
    }

    suspend fun delete(id: ServerId): Result<Unit> = withContext(io) {
        store.delete(id).also { if (it.isSuccess) bump() }
    }

    suspend fun wipeAll(): Result<Unit> = withContext(io) {
        store.wipeAll().also { if (it.isSuccess) bump() }
    }

    /** True when an id is already taken; the import screen uses it to re-id. */
    suspend fun exists(id: ServerId): Boolean = withContext(io) { store.exists(id) }

    private fun bump() {
        revisions.value = revisions.value + 1
    }

    private fun TunnelConfig.toSummary() = ServerSummary(
        id = serverId,
        endpointHost = endpointHost,
        endpointPort = endpointPort,
        addressCidr = address,
        dnsServers = dnsServers,
        mtu = mtu,
        ipv6PolicyLabel = when (ipv6Policy) {
            ir.karensoft.karentunnel.core.model.Ipv6Policy.TUNNELED -> "IPv6 داخل تونل"
            ir.karensoft.karentunnel.core.model.Ipv6Policy.EXCLUDED -> "IPv6 حذف‌شده"
        },
        allowedIpCount = allowedIps.size,
    )
}

data class ServerList(
    val servers: List<ServerSummary>,
    val unreadable: Int,
)

/**
 * A tiny, deterministic id generator for imports.
 *
 * Contains no randomness: the same endpoint always maps to the same id, so
 * re-importing the same file updates that server instead of piling up
 * duplicates. Uniqueness is enforced by the caller when a collision on a
 * *different* config is detected.
 */
object ServerIdFactory {

    fun fromEndpoint(host: String, port: Int): ServerId {
        val safePort = port.coerceIn(1, 65535)
        val safeHost = host.lowercase()
            // Dots are legal in a hostname but ".." is a traversal sequence, and
            // the id becomes a filename. Collapse runs of dots first so that
            // "../../etc" cannot survive as "..-..-etc".
            .replace(Regex("\\.{2,}"), ".")
            .replace(Regex("[^a-z0-9.-]"), "-")
            .trim('-', '.')
            .ifBlank { "host" }
        return ServerId("wg-$safeHost-$safePort")
    }
}
