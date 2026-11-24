package com.pamn.gamematch.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import com.pamn.gamematch.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val birthYear: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..70f,
    val onlyReleased: Boolean = true,
    val profilePictureUrl: String = "https://api.dicebear.com/7.x/bottts/png?seed=default",
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSyncingIgdb: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showAvatarSelector: Boolean = false,
    val userMessage: UiText? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        val currentUser = authRepository.currentUser
        val email = currentUser?.email ?: ""
        _uiState.value = ProfileUiState(isLoading = true, email = email)

        gameRepository.getUserPreferences { result ->
            result
                .onSuccess { user ->
                    _uiState.update { state ->
                        state.copy(
                            username = user.username,
                            email = email,
                            birthYear = user.birthYear,
                            selectedGenres = user.preferredGenres.toSet(),
                            priceRange = user.minPrice..user.maxPrice,
                            onlyReleased = user.onlyReleased,
                            profilePictureUrl = user.profilePictureUrl.ifBlank { "https://api.dicebear.com/7.x/bottts/png?seed=default" },
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            email = email,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun toggleEditing() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
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

    fun setShowPasswordDialog(show: Boolean) {
        _uiState.update { it.copy(showPasswordDialog = show) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun saveProfile(
        minValidYear: Int,
        maxValidYear: Int,
        isDarkModeGlobal: Boolean,
        currentLanguage: String
    ) {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.username.isBlank() || state.birthYear.isBlank()) {
            _uiState.update { it.copy(userMessage = UiText.StringResource(R.string.profile_error_incomplete)) }
            return
        }

        if (!ValidationUtils.isBirthYearValid(state.birthYear, minValidYear, maxValidYear)) {
            _uiState.update {
                it.copy(userMessage = UiText.StringResource(R.string.profile_error_invalid_year, minValidYear, maxValidYear))
            }
            return
        }

        val currentUser = authRepository.currentUser
        val updatedUser = User(
            uid = currentUser?.uid ?: "",
            email = currentUser?.email ?: "",
            username = state.username.trim(),
            preferredGenres = state.selectedGenres.toList(),
            minPrice = state.priceRange.start,
            maxPrice = state.priceRange.endInclusive,
            onlyReleased = state.onlyReleased,
            birthYear = state.birthYear,
            isDarkMode = isDarkModeGlobal,
            language = currentLanguage,
            profilePictureUrl = state.profilePictureUrl
        )

        _uiState.update { it.copy(isSaving = true) }

        gameRepository.saveUserPreferences(userModel = updatedUser) { success ->
            _uiState.update {
                if (success) {
                    it.copy(
                        isEditing = false,
                        isSaving = false,
                        userMessage = UiText.StringResource(R.string.profile_updated_success)
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        userMessage = UiText.StringResource(R.string.profile_save_error)
                    )
                }
            }
        }
    }

    fun updatePassword(newPass: String, onSuccess: (() -> Unit)? = null) {
        authRepository.updatePassword(newPass) { success, err ->
            if (success) {
                _uiState.update {
                    it.copy(
                        showPasswordDialog = false,
                        userMessage = UiText.StringResource(R.string.profile_password_updated_success)
                    )
                }
                onSuccess?.invoke()
            } else {
                _uiState.update {
                    it.copy(
                        userMessage = err?.let { msg -> UiText.DynamicString(msg) }
                            ?: UiText.StringResource(R.string.profile_password_update_error)
                    )
                }
            }
        }
    }

    fun deleteAccount(onLogout: () -> Unit) {
        authRepository.deleteAccount { success, err ->
            if (success) {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        userMessage = UiText.StringResource(R.string.profile_account_deleted)
                    )
                }
                onLogout()
            } else {
                _uiState.update {
                    it.copy(
                        userMessage = err?.let { msg -> UiText.DynamicString(msg) }
                            ?: UiText.StringResource(R.string.profile_delete_error)
                    )
                }
            }
        }
    }

    fun syncCatalogFromIgdb() {
        _uiState.update { it.copy(isSyncingIgdb = true) }
        viewModelScope.launch {
            val result = gameRepository.syncCatalogFromIgdb()
            result
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(
                            isSyncingIgdb = false,
                            userMessage = UiText.StringResource(R.string.profile_sync_success, count)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSyncingIgdb = false,
                            userMessage = error.localizedMessage?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.profile_sync_error)
                        )
                    }
                }
        }
    }

    fun logout(onLogout: () -> Unit) {
        authRepository.logout()
        onLogout()
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
