package com.pamn.gamematch.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.pamn.gamematch.R
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.utils.UiText

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<UiText?>(null)
        private set

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank()) {
            errorMessage = UiText.StringResource(R.string.auth_error_fill_all_fields)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = UiText.StringResource(R.string.auth_error_invalid_email_format)
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.login(trimmedEmail, pass) { success, exception ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = mapAuthException(exception, trimmedEmail)
            }
        }
    }

    fun signUp(email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            errorMessage = UiText.StringResource(R.string.auth_error_fill_all_fields)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = UiText.StringResource(R.string.auth_error_invalid_email_format)
            return
        }

        if (pass.length < 6) {
            errorMessage = UiText.StringResource(R.string.auth_error_password_length)
            return
        }

        if (pass != confirmPass) {
            errorMessage = UiText.StringResource(R.string.auth_error_passwords_dont_match)
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.signUp(trimmedEmail, pass) { success, exception ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = mapAuthException(exception, trimmedEmail)
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

    // Calcula la fortaleza de la contraseña para la UI
    fun getPasswordStrength(pass: String): Float {
        if (pass.isEmpty()) return 0f
        var score = 0f
        if (pass.length >= 6) score += 0.35f
        if (pass.length >= 8) score += 0.25f
        if (pass.any { it.isDigit() }) score += 0.2f
        if (pass.any { !it.isLetterOrDigit() }) score += 0.2f
        return score.coerceAtMost(1f)
    }

    fun clearError() {
        errorMessage = null
    }
}
