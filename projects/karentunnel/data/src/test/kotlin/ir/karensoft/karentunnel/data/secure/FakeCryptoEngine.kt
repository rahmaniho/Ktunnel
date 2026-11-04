package ir.karensoft.karentunnel.data.secure

/**
 * Test double that is *honest about being weak*: it XORs with a fixed byte
 * string. It exists only to exercise storage logic on the JVM. It must never be
 * used outside tests — production wiring is asserted in StorageWiringTest.
 */
class FakeCryptoEngine(private val key: Byte = 0x5A) : CryptoEngine {

    var sealCalls = 0
        private set

    override fun seal(plaintext: ByteArray): ByteArray {
        sealCalls++
        return plaintext.map { (it.toInt() xor key.toInt()).toByte() }.toByteArray()
    }

    override fun open(envelope: ByteArray): ByteArray {
        if (envelope.isEmpty()) throw CryptoException("empty envelope")
        return envelope.map { (it.toInt() xor key.toInt()).toByte() }.toByteArray()
    }

    override fun isAvailable(): Boolean = true
}

/** Simulates a key that was invalidated (e.g. user wiped credentials). */
class BrokenCryptoEngine : CryptoEngine {
    override fun seal(plaintext: ByteArray): ByteArray = plaintext
    override fun open(envelope: ByteArray): ByteArray =
        throw CryptoException("key invalidated")
    override fun isAvailable(): Boolean = false
}
