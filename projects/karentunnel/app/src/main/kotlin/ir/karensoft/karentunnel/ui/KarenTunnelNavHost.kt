package ir.karensoft.karentunnel.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.karensoft.karentunnel.ui.dashboard.DashboardScreen
import ir.karensoft.karentunnel.ui.dashboard.DashboardUiState
import ir.karensoft.karentunnel.ui.diagnostics.DiagnosticsScreen
import ir.karensoft.karentunnel.ui.diagnostics.DiagnosticsViewModel
import ir.karensoft.karentunnel.ui.servers.ServersScreen
import ir.karensoft.karentunnel.ui.servers.ServersViewModel
import ir.karensoft.karentunnel.ui.settings.SettingsScreen
import ir.karensoft.karentunnel.ui.settings.SettingsViewModel

/**
 * Destinations.
 *
 * A hand-written sealed type instead of Navigation-Compose: the app has four
 * screens and no deep links, and adding a navigation library would mean another
 * dependency to keep pinned for no behavioural gain. [Route.Dashboard] is not
 * saved, so process death always returns to the dashboard rather than to a
 * half-finished import sheet.
 */
enum class Route { Dashboard, Servers, Settings, Diagnostics }

@Composable
fun KarenTunnelNavHost(
    dashboardState: DashboardUiState,
    dashboardViewModel: ir.karensoft.karentunnel.ui.dashboard.DashboardViewModel,
    serversViewModel: ServersViewModel,
    settingsViewModel: SettingsViewModel,
    diagnosticsViewModel: DiagnosticsViewModel,
    onConnectRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var route by rememberSaveable { mutableStateOf(Route.Dashboard.name) }
    val current = remember(route) { Route.valueOf(route) }

    Surface(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = current,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "route",
        ) { target ->
            when (target) {
                Route.Dashboard -> DashboardScreen(
                    state = dashboardState,
                    onToggleConnection = onConnectRequested,
                    onOpenServers = { route = Route.Servers.name },
                    onOpenSettings = { route = Route.Settings.name },
                    onOpenDiagnostics = {
                        diagnosticsViewModel.select(
                            dashboardState.selectedServerId,
                        )
                        route = Route.Diagnostics.name
                    },
                    modifier = Modifier.safeDrawingPadding(),
                )

                Route.Servers -> {
                    val state by serversViewModel.uiState.collectAsStateWithLifecycle()
                    ServersScreen(
                        state = state,
                        onBack = { route = Route.Dashboard.name },
                        onSelect = { id ->
                            serversViewModel.select(id)
                            dashboardViewModel.selectServer(id, id.value)
                        },
                        onRequestDelete = { serversViewModel.requestDelete(it) },
                        onConfirmDelete = {
                            serversViewModel.confirmPendingDelete()
                            dashboardViewModel.refreshServers()
                        },
                        onCancelDelete = { serversViewModel.cancelPendingDelete() },
                        onImport = { serversViewModel.import(it) },
                        onImportTextChanged = { serversViewModel.onImportTextChanged() },
                        onDismissImport = { serversViewModel.dismissImport() },
                    )
                }

                Route.Settings -> {
                    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        state = state,
                        onBack = { route = Route.Dashboard.name },
                        onReconnectOnNetworkChange = settingsViewModel::setReconnectOnNetworkChange,
                        onRestoreOnBoot = settingsViewModel::setRestoreOnBoot,
                        onPreferAlwaysOn = settingsViewModel::setPreferAlwaysOn,
                        onConfirmDestructive = settingsViewModel::setConfirmDestructive,
                        onRequestWipe = settingsViewModel::requestWipe,
                        onConfirmWipe = settingsViewModel::confirmWipe,
                        onCancelWipe = settingsViewModel::cancelWipe,
                        onDismissWipeResult = settingsViewModel::clearWipeResult,
                    )
                }

                Route.Diagnostics -> {
                    val state by diagnosticsViewModel.uiState.collectAsStateWithLifecycle()
                    DiagnosticsScreen(
                        state = state,
                        onBack = { route = Route.Dashboard.name },
                        onRun = diagnosticsViewModel::run,
                        onReset = diagnosticsViewModel::reset,
                    )
                }
            }
        }
    }
}
