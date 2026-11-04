package ir.karensoft.karentunnel

import android.app.Application
import android.util.Log
import ir.karensoft.karentunnel.data.secure.KeystoreCryptoEngine
import ir.karensoft.karentunnel.di.AppContainer

/**
 * Process-wide wiring. There is no DI framework on purpose (ADR-007): five
 * modules and a handful of objects do not justify the build-time and runtime
 * cost, and an explicit container is easier to audit for security purposes.
 */
class KarenTunnelApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Fails loudly instead of silently falling back to plaintext storage.
        val crypto = KeystoreCryptoEngine()
        if (!crypto.isAvailable()) {
            Log.e(TAG, "Android Keystore unavailable: encrypted config storage disabled")
        }

        container = AppContainer(this, crypto)
    }

    private companion object {
        const val TAG = "KarenTunnelApp"
    }
}
