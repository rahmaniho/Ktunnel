package ir.karensoft.karentunnel.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.karensoft.karentunnel.core.error.ConfigError
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.data.repo.ServerIdFactory
import ir.karensoft.karentunnel.data.repo.ServerRepository
import ir.karensoft.karentunnel.data.repo.ServerSummary
import ir.karensoft.karentunnel.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State of the import sheet.
 *
 * The pasted text is held here only long enough to parse it. It contains a
 * private key, so it is cleared the moment the import succeeds or the user
 * cancels — it is never persisted and never logged.
 */
sealed interface ImportState {
    data object Idle : ImportState
    data object Working : ImportState
    data class Failed(val message: String) : ImportState
    data class Succeeded(val server: ServerSummary) : ImportState
}

data class ServersUiState(
    val servers: List<ServerSummary> = emptyList(),
    val selectedId: ServerId? = null,
    val unreadableCount: Int = 0,
    val import: ImportState = ImportState.Idle,
    /** Server pending a delete confirmation, if the setting requires one. */
    val pendingDelete: ServerSummary? = null,
    val loading: Boolean = true,
)

/**
 * Servers screen.
 *
 * Design rules encoded here:
 *  * nothing is auto-generated into the list — every row comes from a config the
 *    user imported, so there are no invented countries or fake latencies;
 *  * deleting is confirmed by default because the private key is unrecoverable;
 *  * a corrupt file is counted and surfaced, not hidden.
 */
class ServersViewModel(
    private val repository: ServerRepository,
    private val settings: SettingsRepository,
    /**
     * Whether deleting needs confirmation. Read from the user's setting by the
     * caller; the default is the safe one, so a wiring mistake can only make the
     * app *more* careful, never less.
     */
    private val confirmDestructive: Boolean = true,
) : ViewModel() {

    private val localState = MutableStateFlow(ServersUiState())

    val uiState: StateFlow<ServersUiState> = combine(
        repository.servers,
        repository.unreadableCount,
        settings.settings,
        localState,
    ) { servers, unreadable, prefs, local ->
        local.copy(
            servers = servers,
            unreadableCount = unreadable,
            selectedId = prefs.selectedServerId?.let(::ServerId),
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ServersUiState(),
    )

    fun onImportTextChanged() {
        // Typing invalidates a previous verdict; keeping a stale error on screen
        // while the user edits is misleading.
        localState.update {
            if (it.import is ImportState.Failed) it.copy(import = ImportState.Idle) else it
        }
    }

    fun import(text: String) {
        if (text.isBlank()) {
            localState.update { it.copy(import = ImportState.Failed("متنی وارد نشده است.")) }
            return
        }
        localState.update { it.copy(import = ImportState.Working) }

        viewModelScope.launch {
            // The id is derived from the endpoint, so re-importing the same file
            // updates that server instead of creating a duplicate. Before writing,
            // check that the id is either free or already points at this same
            // endpoint: overwriting a *different* server because two hosts happen
            // to slugify identically would destroy a working config silently.
            val id = idFor(text)
            val existing = repository.summaryFor(id)
            val incomingHost = endpointHostOf(text)

            if (existing != null && incomingHost != null &&
                !existing.endpointHost.equals(incomingHost, ignoreCase = true)
            ) {
                localState.update {
                    it.copy(
                        import = ImportState.Failed(
                            "سرور «${existing.endpointHost}» از قبل با این شناسه ذخیره شده است و " +
                                "endpoint تازه ($incomingHost) با آن فرق دارد. ابتدا سرور قبلی را " +
                                "حذف کنید یا فایل را با نام دیگری وارد کنید.",
                        ),
                    )
                }
                return@launch
            }

            repository.import(text, id)
                .onSuccess { summary ->
                    localState.update { it.copy(import = ImportState.Succeeded(summary)) }
                }
                .onFailure { error ->
                    localState.update { it.copy(import = ImportState.Failed(describe(error))) }
                }
        }
    }

    fun dismissImport() {
        localState.update { it.copy(import = ImportState.Idle) }
    }

    fun select(id: ServerId) {
        viewModelScope.launch { settings.setSelectedServer(id.value) }
    }

    /** Respects the confirm setting; returns true when a dialog was opened. */
    fun requestDelete(server: ServerSummary): Boolean {
        return if (confirmDestructive) {
            localState.update { it.copy(pendingDelete = server) }
            true
        } else {
            deleteNow(server.id)
            false
        }
    }

    fun confirmPendingDelete() {
        localState.value.pendingDelete?.let { deleteNow(it.id) }
        localState.update { it.copy(pendingDelete = null) }
    }

    fun cancelPendingDelete() {
        localState.update { it.copy(pendingDelete = null) }
    }

    private fun deleteNow(id: ServerId) {
        viewModelScope.launch {
            repository.delete(id)
            // Deleting the selected server must not leave a dangling selection.
            if (uiState.value.selectedId == id) settings.setSelectedServer(null)
        }
    }

    private fun endpointOf(text: String): Pair<String, Int>? {
        // Mirrors the section rules of ConfImporter: only Endpoint inside [Peer]
        // counts. A permissive scan here would derive an id from an unrelated
        // line and then write under it.
        val endpoint = Regex("""(?im)^\s*Endpoint\s*=\s*(\S+)\s*$""")
            .find(text)?.groupValues?.get(1) ?: return null
        // IPv6 literals are bracketed: [::1]:51820
        val host: String
        val portText: String
        if (endpoint.startsWith("[")) {
            val close = endpoint.indexOf(']')
            if (close < 0) return null
            host = endpoint.substring(1, close)
            portText = endpoint.substring(close + 1).removePrefix(":")
        } else {
            host = endpoint.substringBeforeLast(':', endpoint)
            portText = endpoint.substringAfterLast(':', "51820")
        }
        val port = portText.toIntOrNull() ?: return null
        return host to port
    }

    private fun endpointHostOf(text: String): String? = endpointOf(text)?.first

    private fun idFor(text: String): ServerId {
        val (host, port) = endpointOf(text) ?: return ServerId("wg-imported")
        return ServerIdFactory.fromEndpoint(host, port)
    }

    private fun describe(error: Throwable): String = when (error) {
        is ConfigError -> ir.karensoft.karentunnel.ui.text.StatusText.configError(error)
        else -> error.message ?: "پیکربندی پذیرفته نشد."
    }
}
