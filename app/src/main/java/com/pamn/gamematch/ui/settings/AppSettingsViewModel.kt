package com.pamn.gamematch.ui.settings

import androidx.lifecycle.ViewModel
import com.pamn.gamematch.data.repository.SettingsRepository
import com.pamn.gamematch.utils.LocaleManager
import com.pamn.gamematch.utils.LocaleManagerImpl
import com.pamn.gamematch.utils.LocaleUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppSettingsUiState(
    val isDarkMode: Boolean = true,
    val language: String = LocaleUtils.getCurrentAppLanguage(),
    val isLoading: Boolean = false
)

class AppSettingsViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val localeManager: LocaleManager = LocaleManagerImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        _uiState.update { it.copy(isLoading = true) }
        settingsRepository.getSettings { settings ->
            if (settings != null) {
                _uiState.update {
                    it.copy(
                        isDarkMode = settings.isDarkMode,
                        language = settings.language,
                        isLoading = false
                    )
                }
                applyLocaleIfNeeded(settings.language)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleDarkMode(enabled: Boolean, saveToFirestore: Boolean = true) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        if (saveToFirestore) {
            settingsRepository.saveDarkMode(enabled)
        }
    }

    fun onChangeLanguage(newLanguage: String, saveToFirestore: Boolean = true) {
        val normalized = LocaleUtils.normalizeLanguage(newLanguage)
        _uiState.update { it.copy(language = normalized) }
        applyLocaleIfNeeded(normalized)
        if (saveToFirestore) {
            settingsRepository.saveLanguage(normalized)
        }
    }

    fun applyLocaleIfNeeded(language: String) {
        val currentLocale = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val normalized = LocaleUtils.normalizeLanguage(language)
        if (currentLocale != normalized) {
            localeManager.setLocale(normalized)
        }
    }
}
