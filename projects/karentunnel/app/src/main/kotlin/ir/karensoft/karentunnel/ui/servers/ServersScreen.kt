package ir.karensoft.karentunnel.ui.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.karensoft.karentunnel.data.repo.ServerSummary
import ir.karensoft.karentunnel.ui.common.KeyValueRow
import ir.karensoft.karentunnel.ui.common.NoticeCard
import ir.karensoft.karentunnel.ui.common.NoticeTone
import ir.karensoft.karentunnel.ui.common.ScreenScaffold
import ir.karensoft.karentunnel.ui.common.SectionCard
import ir.karensoft.karentunnel.ui.text.Fa

/**
 * Servers: the list of tunnels the user has actually imported.
 *
 * There is no "add recommended server" affordance and no country picker, because
 * inventing a server would be inventing the very thing the app is supposed to
 * make trustworthy. The only way in is pasting a real `.conf`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    state: ServersUiState,
    onBack: () -> Unit,
    onSelect: (ir.karensoft.karentunnel.core.model.ServerId) -> Unit,
    onRequestDelete: (ServerSummary) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onImport: (String) -> Unit,
    onImportTextChanged: () -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showImport by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    ScreenScaffold(title = "سرورها", onBack = onBack) { padding ->
        Scaffold(
            modifier = modifier.padding(padding),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showImport = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("افزودن سرور") },
                )
            },
        ) { inner ->
            if (state.servers.isEmpty() && !state.loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "هنوز سروری اضافه نشده است.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "این برنامه هیچ سرور پیش‌فرضی ندارد و سرور نمی‌سازد. " +
                            "فایل پیکربندی WireGuard خود را که از ارائه‌دهندهٔ خودتان گرفته‌اید " +
                            "با دکمهٔ «افزودن سرور» وارد کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.loading) {
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(24.dp),
                                horizontalArrangement = Arrangement.Center,
                            ) { CircularProgressIndicator() }
                        }
                    }

                    if (state.unreadableCount > 0) {
                        item {
                            NoticeCard(
                                title = "پیکربندی خوانده‌نشده",
                                body = "${Fa.number(state.unreadableCount)} فایل ذخیره‌شده قابل " +
                                    "رمزگشایی یا خواندن نبود و در این فهرست نیست. این معمولاً یعنی " +
                                    "کلید سیستم تغییر کرده یا فایل آسیب دیده است؛ داده‌ای پاک نشده.",
                                tone = NoticeTone.Warning,
                            )
                        }
                    }

                    items(state.servers, key = { it.id.value }) { server ->
                        ServerRow(
                            server = server,
                            selected = server.id == state.selectedId,
                            onSelect = { onSelect(server.id) },
                            onDelete = { onRequestDelete(server) },
                        )
                    }
                }
            }
        }
    }

    if (showImport) {
        ModalBottomSheet(
            onDismissRequest = {
                showImport = false
                onDismissImport()
            },
            sheetState = sheetState,
        ) {
            ImportSheet(
                state = state.import,
                onImport = onImport,
                onTextChanged = onImportTextChanged,
            )
        }
    }

    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("حذف سرور؟") },
            text = {
                Text(
                    "سرور «${pending.endpointHost}» و کلید خصوصی آن برای همیشه پاک می‌شود. " +
                        "کلید را از جای دیگری نگه داشته‌اید؟ این کار برگشت‌پذیر نیست.",
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCancelDelete) { Text("انصراف") } },
        )
    }
}

@Composable
private fun ServerRow(
    server: ServerSummary,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    SectionCard {
        Column(Modifier.padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected, onClick = onSelect)
                Column(Modifier.weight(1f)) {
                    Text(server.endpointHost, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${Fa.number(server.endpointPort)} · ${server.addressCidr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف ${server.endpointHost}",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (selected) {
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = onSelect,
                    label = { Text("سرور فعال") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))
            KeyValueRow("DNS", server.dnsServers.joinToString("، "), showDivider = false)
            KeyValueRow("MTU", Fa.number(server.mtu), showDivider = false)
            KeyValueRow("سیاست IPv6", server.ipv6PolicyLabel, showDivider = false)
        }
    }
}

@Composable
private fun ImportSheet(
    state: ImportState,
    onImport: (String) -> Unit,
    onTextChanged: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("افزودن سرور", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "محتوای فایل .conf خود را اینجا بچسبانید. متن شامل کلید خصوصی است؛ فقط روی همین " +
                "دستگاه رمزنگاری و ذخیره می‌شود و جایی ارسال نمی‌گردد.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onTextChanged()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            label = { Text("[Interface] ... [Peer] ...") },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Default,
            ),
            enabled = state !is ImportState.Working,
        )

        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is ImportState.Failed -> {
                NoticeCard("پیکربندی پذیرفته نشد", s.message, NoticeTone.Danger)
                Spacer(Modifier.height(12.dp))
            }
            is ImportState.Succeeded -> {
                NoticeCard(
                    title = "افزوده شد",
                    body = "سرور ${s.server.endpointHost} ذخیره و رمزنگاری شد. " +
                        "می‌توانید آن را انتخاب کنید.",
                    tone = NoticeTone.Neutral,
                )
                Spacer(Modifier.height(12.dp))
            }
            else -> Unit
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = { onImport(text) },
                enabled = text.isNotBlank() && state !is ImportState.Working,
            ) {
                if (state is ImportState.Working) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text("افزودن")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
