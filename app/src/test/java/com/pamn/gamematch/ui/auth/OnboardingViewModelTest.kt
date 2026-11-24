package com.pamn.gamematch.ui.auth

import com.pamn.gamematch.R
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.UiText
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        viewModel = OnboardingViewModel(gameRepository)
    }

    @Test
    fun toggleGenre_addsAndRemovesGenre() {
        viewModel.toggleGenre("Acción")
        assertTrue(viewModel.uiState.value.selectedGenres.contains("Acción"))

        viewModel.toggleGenre("Acción")
        assertFalse(viewModel.uiState.value.selectedGenres.contains("Acción"))
    }

    @Test
    fun completeOnboarding_withIncompleteFields_showsIncompleteError() {
        var completed = false
        viewModel.completeOnboarding(
            minValidYear = 1904,
            maxValidYear = 2016,
            isDarkMode = true,
            currentLanguage = "es",
            onComplete = { completed = true }
        )

        val state = viewModel.uiState.value
        assertFalse(completed)
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.onboarding_error_incomplete, (state.userMessage as UiText.StringResource).resId)
    }

    @Test
    fun completeOnboarding_withOutOfRangeBirthYear_showsYearError() {
        var completed = false
        viewModel.onUsernameChanged("UserTest")
        viewModel.onBirthYearChanged("1800") // Out of range

        viewModel.completeOnboarding(
            minValidYear = 1904,
            maxValidYear = 2016,
            isDarkMode = true,
            currentLanguage = "es",
            onComplete = { completed = true }
        )

        val state = viewModel.uiState.value
        assertFalse(completed)
        assertTrue(state.userMessage is UiText.StringResourceWithSuffix)
    }

    @Test
    fun completeOnboarding_success_resetsStateAndCallsOnComplete() {
        val saveSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.saveUserPreferences(any(), capture(saveSlot)) } answers {
            saveSlot.captured.invoke(true)
        }

        var completed = false
        viewModel.onUsernameChanged("UserTest")
        viewModel.onBirthYearChanged("2000")

        viewModel.completeOnboarding(
            minValidYear = 1904,
            maxValidYear = 2016,
            isDarkMode = true,
            currentLanguage = "es",
            onComplete = { completed = true }
        )

        assertTrue(completed)
        verify { gameRepository.saveUserPreferences(any(), any()) }
    }

    @Test
    fun completeOnboarding_failure_showsSaveError() {
        val saveSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.saveUserPreferences(any(), capture(saveSlot)) } answers {
            saveSlot.captured.invoke(false)
        }

        var completed = false
        viewModel.onUsernameChanged("UserTest")
        viewModel.onBirthYearChanged("2000")

        viewModel.completeOnboarding(
            minValidYear = 1904,
            maxValidYear = 2016,
            isDarkMode = true,
            currentLanguage = "es",
            onComplete = { completed = true }
        )

        val state = viewModel.uiState.value
        assertFalse(completed)
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.onboarding_error_save, (state.userMessage as UiText.StringResource).resId)
    }
}
