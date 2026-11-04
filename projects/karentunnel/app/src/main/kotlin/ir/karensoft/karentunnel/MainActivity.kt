package ir.karensoft.karentunnel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.service.TunnelForegroundService
import ir.karensoft.karentunnel.ui.dashboard.DashboardScreen
import ir.karensoft.karentunnel.ui.dashboard.DashboardViewModel
import ir.karensoft.karentunnel.ui.dashboard.ForegroundController
import ir.karensoft.karentunnel.ui.theme.KarenTunnelTheme
import ir.karensoft.karentunnel.vpn.network.NetworkMonitor

/**
 * Single-activity host.
 *
 * Two platform flows are owned here because they need an Activity result:
 *  * `VpnService.prepare()` consent (must be re-checked every time: another app
 *    may have taken VPN ownership since we last asked).
 *  * `POST_NOTIFICATIONS` on Android 13+, required for the persistent FGS
 *    notification to be visible.
 */
class MainActivity : ComponentActivity() {

    private val appContainer get() = (application as KarenTunnelApp).container

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* The FGS still works without it; the notification is just less visible. */ }

    private val viewModel: DashboardViewModel by viewModels {
        viewModelFactory {
            initializer {
                DashboardViewModel(
                    store = appContainer.configStore,
                    controller = appContainer.tunnelController,
                    healthProbe = appContainer.healthProbe,
                    networkMonitor = NetworkMonitor(applicationContext),
                    foreground = object : ForegroundController {
                        override fun start() = TunnelForegroundService.start(applicationContext)
                        override fun stop() = TunnelForegroundService.stop(applicationContext)
                        override fun update(state: ir.karensoft.karentunnel.core.connection.ConnectionState) {
                            TunnelForegroundService.notificationStateFor(state)?.let {
                                TunnelForegroundService.update(applicationContext, it)
                            }
                        }
                    },
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        // Any consent we had may have been taken by another app; prepare() is cheap.
        ensureVpnPrepared(launch = false)

        setContent {
            KarenTunnelTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                Surface(Modifier.fillMaxSize()) {
                    DashboardScreen(
                        state = state,
                        onToggleConnection = {
                            if (state.shouldDisconnect) {
                                viewModel.onPrimaryAction()
                            } else {
                                ensureVpnPrepared(launch = true)
                            }
                        },
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
            }
        }

        selectDefaultServerIfAvailable()
    }

    override fun onResume() {
        super.onResume()
        // If the user revoked our VPN from system settings while we were away,
        // prepare() returns non-null and we must not keep claiming a connection.
        if (VpnService.prepare(this) != null) {
            viewModel.onSystemRevoked()
        }
    }

    /**
     * Returns true when we are already prepared. When [launch] is true and we are
     * not prepared, the system consent dialog is shown and the result is handled
     * by [vpnPermissionLauncher].
     */
    private fun ensureVpnPrepared(launch: Boolean): Boolean {
        val intent = VpnService.prepare(this)
        if (intent == null) return true
        if (launch) vpnPermissionLauncher.launch(intent)
        return false
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * Personal-use app: exactly one server is expected. Selecting it does not
     * connect; it only makes the dashboard meaningful. If several exist, the
     * servers screen (milestone 5) owns selection.
     */
    private fun selectDefaultServerIfAvailable() {
        val ids = appContainer.configStore.list()
        if (ids.size == 1) {
            val id = ids.single()
            viewModel.selectServer(id, displayNameFor(id))
        }
    }

    private fun displayNameFor(id: ServerId): String {
        // The store keeps configs, not labels; use the endpoint's host as a
        // truthful fallback rather than inventing a country flag or city name.
        return appContainer.configStore.load(id).map { it.endpointHost }.getOrDefault(id.value)
    }

    private fun connectIntentHandled(intent: Intent?) {
        if (intent?.action == ACTION_DISCONNECT) viewModel.emergencyDisconnect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        connectIntentHandled(intent)
    }

    companion object {
        const val ACTION_DISCONNECT = "ir.karensoft.karentunnel.action.DISCONNECT"
    }
}
