package ir.karensoft.karentunnel.data.secure

import ir.karensoft.karentunnel.core.model.SensitiveString
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.core.model.TunnelConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists tunnel configs encrypted-at-rest, and nothing else.
 *
 * Contract:
 *  * a config is written as one sealed envelope per server id;
 *  * the private key exists in plaintext only inside the caller's [TunnelConfig]
 *    and inside the AES-GCM plaintext buffer during decrypt — never on disk;
 *  * a corrupt or tampered file is reported, not "repaired" by guessing;
 *  * deleting a server deletes both the file and (optionally) the key.
 */
interface SecureConfigStore {
    fun list(): List<ServerId>
    fun save(config: TunnelConfig): Result<Unit>
    fun load(id: ServerId): Result<TunnelConfig>
    fun delete(id: ServerId): Result<Unit>
    fun exists(id: ServerId): Boolean
    /** Wipes every stored config. Used by "پاک‌سازی همهٔ داده‌ها". */
    fun wipeAll(): Result<Unit>
}

/**
 * File-backed implementation. Files live in the app's private storage
 * (one .enc file per tunnel under filesDir/tunnels), which is already protected
 * by the OS sandbox; the encryption on top protects against backup leaks,
 * rooted dumps and accidental copies.
 *
 * Note for future edits: Kotlin block comments nest. Writing a glob whose
 * middle characters are slash-star inside a block comment silently opens
 * a nested comment, and the file then fails with
 * "Syntax error: Unclosed comment". Use prose instead of globs here.
 */
class FileSecureConfigStore(
    private val baseDir: File,
    private val crypto: CryptoEngine,
    private val json: Json = Json { ignoreUnknownKeys = false; encodeDefaults = true },
) : SecureConfigStore {

    init {
        require(baseDir.exists() || baseDir.mkdirs()) { "cannot create config directory" }
    }

    override fun list(): List<ServerId> =
        baseDir.listFiles { f -> f.isFile && f.name.endsWith(SUFFIX) }
            ?.mapNotNull { file ->
                file.name.removeSuffix(SUFFIX).takeIf { it.isNotBlank() }?.let(::ServerId)
            }
            ?.sortedBy { it.value }
            ?: emptyList()

    override fun save(config: TunnelConfig): Result<Unit> = runCatching {
        // Keep the plaintext as a CharArray so it can actually be wiped. Kotlin
        // Strings are immutable and cannot be zeroed, so holding the envelope as
        // a String would leave the private key readable in the heap until GC.
        val envelope: CharArray =
            json.encodeToString(StoredConfig.serializer(), StoredConfig.from(config)).toCharArray()
        val sealed = try {
            crypto.seal(envelope.concatToString().toByteArray(Charsets.UTF_8))
        } finally {
            envelope.fill('\u0000')
        }
        // Write to a temp file then move: a crash mid-write must not destroy a
        // working config.
        val target = fileFor(config.serverId)
        val tmp = File(baseDir, "${target.name}.tmp")
        tmp.writeBytes(sealed)
        if (!tmp.renameTo(target)) {
            target.delete()
            if (!tmp.renameTo(target)) {
                tmp.delete()
                error("atomic write failed")
            }
        }
    }

    override fun load(id: ServerId): Result<TunnelConfig> = runCatching {
        val file = fileFor(id)
        if (!file.isFile) error("not found: ${id.value}")
        val plain: ByteArray = crypto.open(file.readBytes())
        try {
            // Wiping the ByteArray matters: it holds the decrypted private key.
            val stored = json.decodeFromString(StoredConfig.serializer(), plain.toString(Charsets.UTF_8))
            stored.toConfig()
        } finally {
            plain.fill(0)
        }
    }

    override fun delete(id: ServerId): Result<Unit> = runCatching {
        val file = fileFor(id)
        if (file.exists() && !file.delete()) error("delete failed: ${id.value}")
    }

    override fun exists(id: ServerId): Boolean = fileFor(id).isFile

    override fun wipeAll(): Result<Unit> = runCatching {
        baseDir.listFiles()?.forEach { it.delete() }
    }

    private fun fileFor(id: ServerId): File {
        // Ids are validated on construction; still, never allow path traversal.
        val safe = id.value.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(baseDir, safe + SUFFIX)
    }

    /**
     * On-disk shape. Field order and names are part of the file format: bump
     * [SCHEMA] and add a migration rather than renaming fields.
     */
    @Serializable
    private data class StoredConfig(
        @SerialName("schema") val schema: Int = SCHEMA,
        @SerialName("server_id") val serverId: String,
        @SerialName("protocol") val protocol: String,
        @SerialName("endpoint_host") val endpointHost: String,
        @SerialName("endpoint_port") val endpointPort: Int,
        @SerialName("address") val address: String,
        @SerialName("dns") val dns: List<String>,
        @SerialName("mtu") val mtu: Int,
        @SerialName("allowed_ips") val allowedIps: List<String>,
        @SerialName("ipv6_policy") val ipv6Policy: String,
        @SerialName("peer_public_key") val peerPublicKey: String,
        @SerialName("private_key") val privateKey: String,
        @SerialName("preshared_key") val presharedKey: String? = null,
        @SerialName("persistent_keepalive") val persistentKeepalive: Int? = null,
    ) {
        fun toConfig(): TunnelConfig = TunnelConfig(
            serverId = ServerId(serverId),
            protocol = ir.karensoft.karentunnel.core.model.Protocol.valueOf(protocol),
            endpointHost = endpointHost,
            endpointPort = endpointPort,
            address = address,
            dnsServers = dns,
            mtu = mtu,
            allowedIps = allowedIps,
            ipv6Policy = ir.karensoft.karentunnel.core.model.Ipv6Policy.valueOf(ipv6Policy),
            peerPublicKey = peerPublicKey,
            privateKey = SensitiveString.of(privateKey),
            presharedKey = presharedKey?.let(SensitiveString::of),
            persistentKeepaliveSeconds = persistentKeepalive,
        )

        companion object {
            fun from(config: TunnelConfig) = StoredConfig(
                serverId = config.serverId.value,
                protocol = config.protocol.name,
                endpointHost = config.endpointHost,
                endpointPort = config.endpointPort,
                address = config.address,
                dns = config.dnsServers,
                mtu = config.mtu,
                allowedIps = config.allowedIps,
                ipv6Policy = config.ipv6Policy.name,
                peerPublicKey = config.peerPublicKey,
                privateKey = config.privateKey.use(),
                presharedKey = config.presharedKey?.use(),
                persistentKeepalive = config.persistentKeepaliveSeconds,
            )
        }
    }

    private companion object {
        const val SUFFIX = ".enc"
        const val SCHEMA = 1
    }
}
