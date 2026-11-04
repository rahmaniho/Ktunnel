package ir.karensoft.karentunnel.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.karensoft.karentunnel.data.repo.ServerRepository
import ir.karensoft.karentunnel.data.settings.Settings
import ir.karensoft.karentunnel.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: Settings = Settings(),
    /** Wiping all data is destructive and irreversible; it needs a dialog. */
    val confirmingWipe: Boolean = false,
    val wipeResult: String? = null,
)

class SettingsViewModel(
    private val repository: ServerRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val local = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        local,
    ) { prefs, state -> state.copy(settings = prefs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setReconnectOnNetworkChange(value: Boolean) =
        viewModelScope.launch { settingsRepository.setReconnectOnNetworkChange(value) }

    fun setRestoreOnBoot(value: Boolean) =
        viewModelScope.launch { settingsRepository.setRestoreOnBoot(value) }

    fun setPreferAlwaysOn(value: Boolean) =
        viewModelScope.launch { settingsRepository.setPreferAlwaysOn(value) }

    fun setConfirmDestructive(value: Boolean) =
        viewModelScope.launch { settingsRepository.setConfirmDestructiveActions(value) }

    fun requestWipe() = local.update { it.copy(confirmingWipe = true) }
    fun cancelWipe() = local.update { it.copy(confirmingWipe = false) }

    fun confirmWipe() {
        viewModelScope.launch {
            val result = repository.wipeAll()
            settingsRepository.setSelectedServer(null)
            local.update {
                it.copy(
                    confirmingWipe = false,
                    wipeResult = if (result.isSuccess) {
                        "همهٔ سرورها و کلیدهای ذخیره‌شده پاک شدند."
                    } else {
                        "پاک‌سازی کامل نشد: ${result.exceptionOrNull()?.message ?: "خطای نامشخص"}"
                    },
                )
            }
        }
    }

    fun clearWipeResult() = local.update { it.copy(wipeResult = null) }
}
