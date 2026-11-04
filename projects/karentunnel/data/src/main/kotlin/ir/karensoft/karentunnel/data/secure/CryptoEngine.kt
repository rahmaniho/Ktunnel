package ir.karensoft.karentunnel.data.secure

/**
 * The minimum cryptographic surface this app needs: AES-GCM over a byte array.
 *
 * Note what is absent, deliberately: no hashing of passwords (we do not store
 * passwords), no key agreement, no "encrypt with password" API. All key
 * material lives in the Android Keystore and never leaves it — see
 * [KeystoreCryptoEngine]. Providing this as an interface lets the storage
 * layer be tested on the JVM with a fake, while production has exactly one
 * implementation that touches hardware-backed keys.
 */
interface CryptoEngine {
    /** Seal [plaintext] and return a self-describing envelope (iv + ciphertext + tag). */
    fun seal(plaintext: ByteArray): ByteArray

    /**
     * Open an envelope produced by [seal].
     * Throws [CryptoException] on tampering, wrong key, or malformed input —
     * never returns garbage as if it were plaintext.
     */
    fun open(envelope: ByteArray): ByteArray

    /** True when the underlying key exists or can be created. */
    fun isAvailable(): Boolean
}

class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)
