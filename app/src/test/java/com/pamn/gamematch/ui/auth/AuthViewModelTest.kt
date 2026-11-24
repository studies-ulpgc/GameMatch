package com.pamn.gamematch.ui.auth

import com.pamn.gamematch.R
import com.pamn.gamematch.data.repository.AuthRepository
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

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        viewModel = AuthViewModel(authRepository)
    }

    @Test
    fun login_emptyFields_setsFillAllFieldsError() {
        var success = false
        viewModel.login("", "", onSuccess = { success = true })

        val state = viewModel.uiState.value
        assertFalse(success)
        assertTrue(state.errorMessage is UiText.StringResource)
        assertEquals(R.string.auth_error_fill_all_fields, (state.errorMessage as UiText.StringResource).resId)
    }

    @Test
    fun login_invalidEmail_setsInvalidEmailFormatError() {
        var success = false
        viewModel.login("invalidemail", "password123", onSuccess = { success = true })

        val state = viewModel.uiState.value
        assertFalse(success)
        assertTrue(state.errorMessage is UiText.StringResource)
        assertEquals(R.string.auth_error_invalid_email_format, (state.errorMessage as UiText.StringResource).resId)
    }

    @Test
    fun login_validCredentials_callsSuccessOnAuthRepositorySuccess() {
        val loginSlot = slot<(Boolean, Exception?) -> Unit>()
        every { authRepository.login(eq("user@example.com"), eq("password123"), capture(loginSlot)) } answers {
            loginSlot.captured.invoke(true, null)
        }

        var success = false
        viewModel.login("user@example.com", "password123", onSuccess = { success = true })

        val state = viewModel.uiState.value
        assertTrue(success)
        assertFalse(state.isLoading)
        assertEquals(null, state.errorMessage)
        verify { authRepository.login("user@example.com", "password123", any()) }
    }

    @Test
    fun signUp_passwordsMismatch_setsPasswordsMismatchError() {
        var success = false
        viewModel.signUp("user@example.com", "pass123", "pass456", onSuccess = { success = true })

        val state = viewModel.uiState.value
        assertFalse(success)
        assertTrue(state.errorMessage is UiText.StringResource)
        assertEquals(R.string.auth_error_passwords_dont_match, (state.errorMessage as UiText.StringResource).resId)
    }

    @Test
    fun signUp_shortPassword_setsPasswordLengthError() {
        var success = false
        viewModel.signUp("user@example.com", "123", "123", onSuccess = { success = true })

        val state = viewModel.uiState.value
        assertFalse(success)
        assertTrue(state.errorMessage is UiText.StringResource)
        assertEquals(R.string.auth_error_password_length, (state.errorMessage as UiText.StringResource).resId)
    }
}
