package ir.karensoft.karentunnel.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.karensoft.karentunnel.R
import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.connection.DegradedReason
import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.ui.text.Fa
import ir.karensoft.karentunnel.ui.text.StatusText
import ir.karensoft.karentunnel.ui.theme.KarenTunnelTheme
import ir.karensoft.karentunnel.ui.theme.TunnelColors

/**
 * Dashboard: one dominant action, honest status, and the numbers a person
 * actually looks at while deciding whether their connection is healthy.
 *
 * Visual contract:
 *  * DISCONNECTED — neutral outline, no animation.
 *  * CONNECTING / RECONNECTING — amber + gentle pulse (motion is the only signal
 *    that something is *happening*, never that it succeeded).
 *  * CONNECTED — verified green, only ever driven by ConnectionState.Connected.
 *  * DEGRADED — amber + explicit warning copy; deliberately NOT green.
 *  * ERROR — red + the reason + a recovery action.
 */
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenServers: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DashboardTopBar(
            onOpenServers = onOpenServers,
            onOpenSettings = onOpenSettings,
        )

        Spacer(Modifier.height(8.dp))

        ServerHeader(
            serverName = state.serverName,
            protocolLabel = state.protocolLabel,
        )

        Spacer(Modifier.height(24.dp))

        ConnectButton(
            state = state.connection,
            onToggleConnection = onToggleConnection,
        )

        Spacer(Modifier.height(20.dp))

        StatusBlock(state.connection)

        Spacer(Modifier.height(20.dp))

        MetricsGrid(state)

        state.lastError?.let { error ->
            Spacer(Modifier.height(16.dp))
            ErrorCard(error = error)
        }

        if (state.connection is ConnectionState.Degraded) {
            Spacer(Modifier.height(12.dp))
            DegradedCard(state.connection as ConnectionState.Degraded, onDisconnect = onToggleConnection)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onOpenDiagnostics) {
            Text(stringResource(R.string.dashboard_open_diagnostics))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ServerHeader(serverName: String?, protocolLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = serverName ?: stringResource(R.string.dashboard_no_server),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = protocolLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectButton(
    state: ConnectionState,
    onToggleConnection: () -> Unit,
) {
    val visual = visualFor(state)
    val pulse = if (visual.animated) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val scale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseScale",
        )
        scale
    } else {
        1f
    }

    val description = stringResource(R.string.dashboard_connect_cd_format, StatusText.label(state))

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(196.dp)
            .scale(pulse)
            .semantics { contentDescription = description },
    ) {
        Surface(
            shape = CircleShape,
            color = visual.container,
            border = androidx.compose.foundation.BorderStroke(2.dp, visual.border),
            modifier = Modifier.fillMaxSize(),
            onClick = onToggleConnection,
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    visual.spinner -> CircularProgressIndicator(
                        color = visual.content,
                        modifier = Modifier.size(44.dp),
                    )
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = visual.actionLabel(state),
                            style = MaterialTheme.typography.titleLarge,
                            color = visual.content,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBlock(state: ConnectionState) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "status",
    ) { current ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = StatusText.label(current),
                style = MaterialTheme.typography.titleLarge,
                color = statusColor(current),
                textAlign = TextAlign.Center,
            )
            StatusText.detail(current)?.let { detail ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid(state: DashboardUiState) {
    val connected = state.connection is ConnectionState.Connected ||
        state.connection is ConnectionState.Degraded
    if (!connected) return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricRow(
                label = stringResource(R.string.metric_duration),
                value = Fa.duration(state.uptimeMillis),
            )
            MetricRow(
                label = stringResource(R.string.metric_download),
                value = Fa.bytes(state.rxBytes),
            )
            MetricRow(
                label = stringResource(R.string.metric_upload),
                value = Fa.bytes(state.txBytes),
            )
            MetricRow(
                label = stringResource(R.string.metric_latency),
                value = Fa.latency(state.latencyMillis),
            )
            MetricRow(
                label = stringResource(R.string.metric_network),
                value = state.networkLabel,
            )
            MetricRow(
                label = stringResource(R.string.metric_ipv6),
                value = state.ipv6Label,
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorCard(error: ConnectionError) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = StatusText.connectionError(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.action_tap_to_retry),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .semantics { contentDescription = "تلاش دوباره" }
                    .padding(vertical = 4.dp),
            )
            // The primary action is the large control above; no second button here.
        }
    }
}

@Composable
private fun DegradedCard(state: ConnectionState.Degraded, onDisconnect: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.degraded_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = StatusText.detail(state) ?: "",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onDisconnect) {
                Text(stringResource(R.string.action_disconnect_now))
            }
        }
    }
}

// --------------------------------------------------------------------- visuals

private data class ButtonVisual(
    val container: Color,
    val content: Color,
    val border: Color,
    val animated: Boolean,
    val spinner: Boolean,
    val actionLabel: (ConnectionState) -> String,
)

@Composable
private fun visualFor(state: ConnectionState): ButtonVisual {
    val scheme = MaterialTheme.colorScheme
    val connected = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        TunnelColors.connectedDark
    } else {
        TunnelColors.connectedLight
    }
    return when (state) {
        ConnectionState.Disconnected -> ButtonVisual(
            container = scheme.surfaceVariant,
            content = scheme.onSurface,
            border = scheme.outline,
            animated = false,
            spinner = false,
            actionLabel = { "اتصال" },
        )
        ConnectionState.Preparing, ConnectionState.Connecting -> ButtonVisual(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = scheme.secondary,
            animated = true,
            spinner = true,
            actionLabel = { "در حال اتصال" },
        )
        is ConnectionState.Connected -> ButtonVisual(
            container = connected.copy(alpha = 0.14f),
            content = connected,
            border = connected,
            animated = false,
            spinner = false,
            actionLabel = { "قطع اتصال" },
        )
        is ConnectionState.Degraded -> ButtonVisual(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = scheme.secondary,
            animated = false,
            spinner = false,
            actionLabel = { "قطع اتصال" },
        )
        is ConnectionState.Reconnecting -> ButtonVisual(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = scheme.secondary,
            animated = true,
            spinner = true,
            actionLabel = { "تلاش دوباره" },
        )
        ConnectionState.Disconnecting -> ButtonVisual(
            container = scheme.surfaceVariant,
            content = scheme.onSurface,
            border = scheme.outline,
            animated = true,
            spinner = true,
            actionLabel = { "در حال قطع" },
        )
        is ConnectionState.Error -> ButtonVisual(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            border = scheme.error,
            animated = false,
            spinner = false,
            actionLabel = { "تلاش دوباره" },
        )
    }
}

@Composable
private fun statusColor(state: ConnectionState): Color = when (state) {
    is ConnectionState.Connected -> if (androidx.compose.foundation.isSystemInDarkTheme()) {
        TunnelColors.connectedDark
    } else {
        TunnelColors.connectedLight
    }
    is ConnectionState.Degraded -> MaterialTheme.colorScheme.secondary
    is ConnectionState.Error -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

// ---------------------------------------------------------------------- previews

@Preview(name = "Disconnected", showBackground = true, locale = "fa")
@Composable
private fun PreviewDisconnected() {
    KarenTunnelTheme {
        DashboardScreen(
            state = DashboardUiState(connection = ConnectionState.Disconnected),
            onToggleConnection = {},
        )
    }
}

@Preview(name = "Connected", showBackground = true, locale = "fa")
@Composable
private fun PreviewConnected() {
    KarenTunnelTheme {
        DashboardScreen(
            state = DashboardUiState(
                connection = ConnectionState.Connected(sinceMillis = 0),
                serverName = "سرور شخصی",
                protocolLabel = "WireGuard",
                uptimeMillis = 754_000,
                rxBytes = 12_582_912,
                txBytes = 2_097_152,
                latencyMillis = 48,
                networkLabel = "Wi-Fi",
                ipv6Label = "خارج از تونل",
            ),
            onToggleConnection = {},
        )
    }
}

@Preview(name = "Degraded", showBackground = true, locale = "fa")
@Composable
private fun PreviewDegraded() {
    KarenTunnelTheme {
        DashboardScreen(
            state = DashboardUiState(
                connection = ConnectionState.Degraded(DegradedReason.StaleHandshake, 400_000),
                serverName = "سرور شخصی",
                protocolLabel = "WireGuard",
                uptimeMillis = 400_000,
                networkLabel = "دادهٔ همراه",
                ipv6Label = "خارج از تونل",
            ),
            onToggleConnection = {},
        )
    }
}

@Preview(name = "Error", showBackground = true, locale = "fa")
@Composable
private fun PreviewError() {
    KarenTunnelTheme {
        DashboardScreen(
            state = DashboardUiState(
                connection = ConnectionState.Error(ConnectionError.Timeout, retryable = true),
                serverName = "سرور شخصی",
                protocolLabel = "WireGuard",
                lastError = ConnectionError.Timeout,
            ),
            onToggleConnection = {},
        )
    }
}

/**
 * Two icon actions, no overflow menu: with four screens an icon pair is faster
 * than a drawer and keeps the dashboard's single-action promise intact.
 */
@Composable
private fun DashboardTopBar(
    onOpenServers: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenServers) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = stringResource(R.string.dashboard_open_servers),
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.dashboard_open_settings),
            )
        }
    }
}
