package com.pamn.gamematch.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.pamn.gamematch.R
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.utils.UiText
import com.pamn.gamematch.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_fill_all_fields)) }
            return
        }

        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_invalid_email_format)) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        authRepository.login(trimmedEmail, pass) { success, exception ->
            if (success) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                val error = mapAuthException(exception, trimmedEmail)
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    fun signUp(email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_fill_all_fields)) }
            return
        }

        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_invalid_email_format)) }
            return
        }

        if (pass.length < 6) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_password_length)) }
            return
        }

        if (pass != confirmPass) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_passwords_dont_match)) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        authRepository.signUp(trimmedEmail, pass) { success, exception ->
            if (success) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                val error = mapAuthException(exception, trimmedEmail)
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    private fun mapAuthException(exception: Exception?, email: String = ""): UiText {
        if (exception == null) return UiText.StringResource(R.string.auth_error_generic)
        return when (exception) {
            is FirebaseAuthInvalidUserException -> UiText.StringResource(R.string.auth_error_user_not_found, email)
            is FirebaseAuthInvalidCredentialsException -> UiText.StringResource(R.string.auth_error_invalid_credentials)
            is FirebaseAuthUserCollisionException -> UiText.StringResource(R.string.auth_error_email_already_in_use)
            is FirebaseNetworkException -> UiText.StringResource(R.string.auth_error_network)
            else -> {
                val msg = exception.localizedMessage ?: ""
                when {
                    msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
                    msg.contains("no user record", ignoreCase = true) ||
                    msg.contains("user may have been deleted", ignoreCase = true) ->
                        UiText.StringResource(R.string.auth_error_user_not_found, email)

                    msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    msg.contains("INVALID_CREDENTIALS", ignoreCase = true) ||
                    msg.contains("invalid", ignoreCase = true) ||
                    msg.contains("wrong", ignoreCase = true) ->
                        UiText.StringResource(R.string.auth_error_invalid_credentials)

                    msg.contains("EMAIL_EXISTS", ignoreCase = true) ||
                    msg.contains("already in use", ignoreCase = true) ->
                        UiText.StringResource(R.string.auth_error_email_already_in_use)

                    msg.contains("network", ignoreCase = true) ->
                        UiText.StringResource(R.string.auth_error_network)

                    msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
                    msg.contains("too many", ignoreCase = true) ->
                        UiText.StringResource(R.string.auth_error_too_many_requests)

                    else -> UiText.StringResource(R.string.auth_error_generic)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
