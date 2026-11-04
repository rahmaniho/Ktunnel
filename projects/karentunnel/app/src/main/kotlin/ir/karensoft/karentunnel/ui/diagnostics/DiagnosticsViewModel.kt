package ir.karensoft.karentunnel.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.karensoft.karentunnel.core.model.ServerId
import ir.karensoft.karentunnel.data.repo.ServerRepository
import ir.karensoft.karentunnel.diagnostics.DiagnosticReport
import ir.karensoft.karentunnel.diagnostics.ProbeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DiagnosticUi {
    data object Idle : DiagnosticUi
    data object Running : DiagnosticUi
    data class Done(val report: DiagnosticReport) : DiagnosticUi
    data class Failed(val message: String) : DiagnosticUi
}

data class DiagnosticsUiState(
    val serverId: ServerId? = null,
    val serverLabel: String? = null,
    val state: DiagnosticUi = DiagnosticUi.Idle,
)

/**
 * Runs the probe engine on demand.
 *
 * The screen is deliberately *manual*: probes open sockets, resolve DNS and
 * connect to the endpoint, which is observable by a network operator. Doing that
 * automatically in the background would leak timing metadata for no benefit, so
 * a run only happens when the user asks.
 */
class DiagnosticsViewModel(
    private val repository: ServerRepository,
    private val engine: ProbeEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null

    fun select(id: ServerId?) {
        _uiState.update { it.copy(serverId = id, state = DiagnosticUi.Idle) }
        if (id == null) return
        viewModelScope.launch {
            val summary = repository.summaryFor(id)
            _uiState.update { it.copy(serverLabel = summary?.endpointHost) }
        }
    }

    fun run() {
        val id = _uiState.value.serverId ?: return
        if (_uiState.value.state is DiagnosticUi.Running) return
        runJob?.cancel()

        _uiState.update { it.copy(state = DiagnosticUi.Running) }
        runJob = viewModelScope.launch {
            val config = repository.configFor(id).getOrElse { error ->
                _uiState.update {
                    it.copy(
                        state = DiagnosticUi.Failed(
                            "پیکربندی خوانده نشد: ${error.message ?: "خطای نامشخص"}",
                        ),
                    )
                }
                return@launch
            }
            // ProbeEngine is bounded by its own total budget, so this cannot hang
            // the screen forever.
            val report = runCatching { engine.run(config) }
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(state = DiagnosticUi.Failed(error.message ?: "اجرای آزمون ناموفق بود"))
                    }
                    return@launch
                }
            _uiState.update { it.copy(state = DiagnosticUi.Done(report)) }
        }
    }

    fun reset() {
        runJob?.cancel()
        _uiState.update { it.copy(state = DiagnosticUi.Idle) }
    }
}
