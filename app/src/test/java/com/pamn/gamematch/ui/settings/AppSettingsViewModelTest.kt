package com.pamn.gamematch.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.pamn.gamematch.data.repository.SettingsRepository
import com.pamn.gamematch.data.repository.UserSettings
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.LocaleManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val localeManager: LocaleManager = mockk(relaxed = true)
    private lateinit var viewModel: AppSettingsViewModel

    @Before
    fun setUp() {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.getApplicationLocales() } returns LocaleListCompat.getEmptyLocaleList()
        viewModel = AppSettingsViewModel(settingsRepository, localeManager)
    }

    @After
    fun tearDown() {
        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun loadSettings_withExistingSettings_updatesStateAndAppliesLocale() {
        val callbackSlot = slot<(UserSettings?) -> Unit>()
        every { settingsRepository.getSettings(capture(callbackSlot)) } answers {
            callbackSlot.captured.invoke(UserSettings(isDarkMode = false, language = "en"))
        }

        viewModel.loadSettings()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isDarkMode)
        assertEquals("en", state.language)
        verify { localeManager.setLocale("en") }
    }

    @Test
    fun loadSettings_whenNull_stopsLoadingWithoutOverwritingDefaults() {
        val callbackSlot = slot<(UserSettings?) -> Unit>()
        every { settingsRepository.getSettings(capture(callbackSlot)) } answers {
            callbackSlot.captured.invoke(null)
        }

        viewModel.loadSettings()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isDarkMode)
        verify(exactly = 0) { localeManager.setLocale(any()) }
    }

    @Test
    fun onToggleDarkMode_updatesStateAndSavesWhenRequested() {
        viewModel.onToggleDarkMode(enabled = false, saveToFirestore = true)

        assertFalse(viewModel.uiState.value.isDarkMode)
        verify { settingsRepository.saveDarkMode(false) }
    }

    @Test
    fun onToggleDarkMode_withSaveFalse_skipsRepositoryPersistence() {
        viewModel.onToggleDarkMode(enabled = false, saveToFirestore = false)

        assertFalse(viewModel.uiState.value.isDarkMode)
        verify(exactly = 0) { settingsRepository.saveDarkMode(any()) }
    }

    @Test
    fun onChangeLanguage_normalizesUpdatesStateAppliesLocaleAndSaves() {
        viewModel.onChangeLanguage("English", saveToFirestore = true)

        assertEquals("en", viewModel.uiState.value.language)
        verify { localeManager.setLocale("en") }
        verify { settingsRepository.saveLanguage("en") }
    }

    @Test
    fun applyLocaleIfNeeded_whenCurrentLocaleMatches_doesNotCallLocaleManager() {
        every { AppCompatDelegate.getApplicationLocales() } returns LocaleListCompat.forLanguageTags("es")

        viewModel.applyLocaleIfNeeded("es")

        verify(exactly = 0) { localeManager.setLocale(any()) }
    }
}
