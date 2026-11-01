package com.pamn.gamematch.ui.auth

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
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.login(email, pass) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error ?: "Error al iniciar sesión"
            }
        }
    }

    fun signUp(email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        if (pass != confirmPass) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.signUp(email, pass) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error ?: "Error al registrar usuario"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}