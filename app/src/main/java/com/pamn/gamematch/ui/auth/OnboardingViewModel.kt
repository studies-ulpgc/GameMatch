package com.pamn.gamematch.ui.auth

import androidx.lifecycle.ViewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import com.pamn.gamematch.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingUiState(
    val username: String = "",
    val birthYear: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..70f,
    val onlyReleased: Boolean = true,
    val profilePictureUrl: String = "https://api.dicebear.com/7.x/bottts/png?seed=default",
    val showAvatarSelector: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: UiText? = null
)

class OnboardingViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = OnboardingUiState()
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onBirthYearChanged(birthYear: String) {
        if (birthYear.all { it.isDigit() } && birthYear.length <= 4) {
            _uiState.update { it.copy(birthYear = birthYear) }
        }
    }

    fun toggleGenre(genre: String) {
        _uiState.update { state ->
            val updated = if (state.selectedGenres.contains(genre)) {
                state.selectedGenres - genre
            } else {
                state.selectedGenres + genre
            }
            state.copy(selectedGenres = updated)
        }
    }

    fun onPriceRangeChanged(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range) }
    }

    fun onOnlyReleasedChanged(onlyReleased: Boolean) {
        _uiState.update { it.copy(onlyReleased = onlyReleased) }
    }

    fun onAvatarSelected(url: String) {
        _uiState.update { it.copy(profilePictureUrl = url, showAvatarSelector = false) }
    }

    fun setShowAvatarSelector(show: Boolean) {
        _uiState.update { it.copy(showAvatarSelector = show) }
    }

    fun completeOnboarding(
        minValidYear: Int,
        maxValidYear: Int,
        isDarkMode: Boolean,
        currentLanguage: String,
        onComplete: () -> Unit
    ) {
        val state = _uiState.value

        if (state.username.isBlank() || state.birthYear.isBlank()) {
            _uiState.update { it.copy(userMessage = UiText.StringResource(R.string.onboarding_error_incomplete)) }
            return
        }

        if (!ValidationUtils.isBirthYearValid(state.birthYear, minValidYear, maxValidYear)) {
            _uiState.update {
                it.copy(userMessage = UiText.StringResourceWithSuffix(R.string.onboarding_birthyear_error_out_of_range, " ($minValidYear - $maxValidYear)"))
            }
            return
        }

        val updatedUser = User(
            username = state.username.trim(),
            preferredGenres = state.selectedGenres.toList(),
            minPrice = state.priceRange.start,
            maxPrice = state.priceRange.endInclusive,
            onlyReleased = state.onlyReleased,
            birthYear = state.birthYear,
            isDarkMode = isDarkMode,
            language = currentLanguage,
            profilePictureUrl = state.profilePictureUrl
        )

        _uiState.update { it.copy(isLoading = true) }

        gameRepository.saveUserPreferences(userModel = updatedUser) { success ->
            _uiState.update { it.copy(isLoading = false) }
            if (success) {
                resetState()
                onComplete()
            } else {
                _uiState.update { it.copy(userMessage = UiText.StringResource(R.string.onboarding_error_save)) }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
