package com.pamn.gamematch.ui.profile

import com.google.firebase.auth.FirebaseUser
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.repository.AuthRepository
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

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val gameRepository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        val firebaseUser = mockk<FirebaseUser>(relaxed = true)
        every { firebaseUser.email } returns "test@example.com"
        every { authRepository.currentUser } returns firebaseUser

        val prefsSlot = slot<(Result<User>) -> Unit>()
        every { gameRepository.getUserPreferences(capture(prefsSlot)) } answers {
            prefsSlot.captured.invoke(Result.success(User(username = "TestUser", birthYear = "1995")))
        }

        viewModel = ProfileViewModel(authRepository, gameRepository)
    }

    @Test
    fun loadProfile_populatesUiState() {
        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("TestUser", state.username)
        assertEquals("test@example.com", state.email)
        assertEquals("1995", state.birthYear)
    }

    @Test
    fun saveProfile_withInvalidYear_showsError() {
        viewModel.onUsernameChanged("TestUser")
        viewModel.onBirthYearChanged("1800")

        viewModel.saveProfile(1904, 2016, true, "es")

        val state = viewModel.uiState.value
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.profile_error_invalid_year, (state.userMessage as UiText.StringResource).resId)
    }

    @Test
    fun saveProfile_failure_showsSaveError() {
        val saveSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.saveUserPreferences(any(), capture(saveSlot)) } answers {
            saveSlot.captured.invoke(false)
        }

        viewModel.onUsernameChanged("TestUser")
        viewModel.onBirthYearChanged("1995")

        viewModel.saveProfile(1904, 2016, true, "es")

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.profile_save_error, (state.userMessage as UiText.StringResource).resId)
    }

    @Test
    fun updatePassword_success_updatesStateAndCallsSuccessCallback() {
        val updateSlot = slot<(Boolean, String?) -> Unit>()
        every { authRepository.updatePassword("newpass123", capture(updateSlot)) } answers {
            updateSlot.captured.invoke(true, null)
        }

        var callbackCalled = false
        viewModel.updatePassword("newpass123") { callbackCalled = true }

        val state = viewModel.uiState.value
        assertTrue(callbackCalled)
        assertFalse(state.showPasswordDialog)
        assertEquals(R.string.profile_password_updated_success, (state.userMessage as UiText.StringResource).resId)
    }
}
