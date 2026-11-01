package com.pamn.gamematch.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pamn.gamematch.data.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "El formato del correo electrónico no es válido"
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.login(trimmedEmail, pass) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error ?: "Credenciales incorrectas"
            }
        }
    }

    fun signUp(email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Introduce un correo electrónico válido (ej. usuario@gmail.com)"
            return
        }

        if (pass.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (pass != confirmPass) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.signUp(trimmedEmail, pass) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error ?: "Error al registrar el usuario en Firebase"
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