package ir.karensoft.karentunnel.data.secure

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM with a key that is generated inside the Android Keystore and is
 * not exportable — on devices with a TEE/StrongBox it never exists in app memory
 * as key material at all.
 *
 * Envelope layout (single byte-array, no external metadata file):
 *
 * ```
 * [ 1 byte version ][ 1 byte iv length ][ iv ][ ciphertext + 16-byte GCM tag ]
 * ```
 *
 * Design choices worth stating:
 *
 * * GCM gives us integrity: tampering is detected at [open] time and surfaces as
 *   [CryptoException], which the repository turns into "config unreadable"
 *   instead of silently starting a tunnel with corrupted settings.
 * * A fresh random IV per seal, as GCM requires. The IV is not secret.
 * * No `setUserAuthenticationRequired(true)`: requiring biometrics to reconnect
 *   a VPN at boot would make always-on mode unusable. This is a deliberate,
 *   documented trade-off (see docs/SECURITY-REPORT.md §key-storage).
 * * `setRandomizedEncryptionRequired` stays at its secure default (true).
 */
class KeystoreCryptoEngine(
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
) : CryptoEngine {

    override fun isAvailable(): Boolean =
        runCatching {
            keyStore()
            loadOrCreateKey()
            true
        }.getOrDefault(false)

    override fun seal(plaintext: ByteArray): ByteArray {
        val key = loadOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        require(iv.size <= MAX_IV_LENGTH) { "unexpected IV length" }
        val ciphertext = cipher.doFinal(plaintext)
        return ByteArray(2 + iv.size + ciphertext.size).also { out ->
            out[0] = VERSION
            out[1] = iv.size.toByte()
            iv.copyInto(out, destinationOffset = 2)
            ciphertext.copyInto(out, destinationOffset = 2 + iv.size)
        }
    }

    override fun open(envelope: ByteArray): ByteArray {
        if (envelope.size < 2 + MIN_TAG_BITS / 8) {
            throw CryptoException("envelope too short")
        }
        if (envelope[0] != VERSION) {
            throw CryptoException("unsupported envelope version ${envelope[0]}")
        }
        val ivLength = envelope[1].toInt()
        if (ivLength <= 0 || ivLength > MAX_IV_LENGTH || envelope.size < 2 + ivLength + 16) {
            throw CryptoException("invalid IV length")
        }
        return try {
            val key = loadOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(MIN_TAG_BITS, envelope, 2, ivLength),
            )
            cipher.doFinal(envelope, 2 + ivLength, envelope.size - 2 - ivLength)
        } catch (e: CryptoException) {
            throw e
        } catch (e: Exception) {
            // Includes AEADBadTagException: wrong key or tampered data.
            throw CryptoException("decryption failed", e)
        }
    }

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun loadOrCreateKey(): SecretKey {
        val ks = keyStore()
        (ks.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        return try {
            generator.generateKey()
        } catch (e: Exception) {
            // Some devices report StrongBox but refuse to use it; retry without.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                e is StrongBoxUnavailableException
            ) {
                generator.init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
                generator.generateKey()
            } else {
                throw e
            }
        }
    }

    /** Deletes the key. Every stored config becomes permanently unreadable. */
    fun destroyKey() {
        runCatching { keyStore().deleteEntry(keyAlias) }
    }

    companion object {
        const val DEFAULT_KEY_ALIAS = "karentunnel.config.v1"
        private const val PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val VERSION: Byte = 1
        private const val MAX_IV_LENGTH = 16
        private const val MIN_TAG_BITS = 128
    }
}
