package com.pamn.gamematch.ui.detail

import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository: GameRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: GameDetailViewModel

    @Before
    fun setUp() {
        every { authRepository.currentUserId } returns "uid-1"
        every { authRepository.getCurrentUserName(any()) } returns "PlayerOne"
        viewModel = GameDetailViewModel(gameRepository, authRepository)
    }

    private fun stubReviews(result: Result<List<GameReview>>) {
        val callbackSlot = slot<(Result<List<GameReview>>) -> Unit>()
        every { gameRepository.getReviewsForGame(any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.invoke(result)
        }
    }

    @Test
    fun loadSteamPrice_whenAppIdIsNull_setsDefaultWithoutCallingRepository() = runTest {
        viewModel.loadSteamPrice(steamAppId = null, defaultPrice = 19.99)

        val state = viewModel.uiState.value
        assertEquals(19.99, state.price!!, 0.001)
        assertFalse(state.isPriceLoading)
        coVerify(exactly = 0) { gameRepository.getSteamPrice(any()) }
    }

    @Test
    fun loadSteamPrice_success_usesSteamPriceOrFallsBackToDefault() = runTest {
        coEvery { gameRepository.getSteamPrice(100) } returns Result.success(14.50)
        viewModel.loadSteamPrice(steamAppId = 100, defaultPrice = 29.99)
        assertEquals(14.50, viewModel.uiState.value.price!!, 0.001)

        coEvery { gameRepository.getSteamPrice(101) } returns Result.success(null)
        viewModel.loadSteamPrice(steamAppId = 101, defaultPrice = 29.99)
        assertEquals(29.99, viewModel.uiState.value.price!!, 0.001)
        assertFalse(viewModel.uiState.value.isPriceLoading)
    }

    @Test
    fun loadSteamPrice_failure_fallsBackToDefaultPrice() = runTest {
        coEvery { gameRepository.getSteamPrice(200) } returns Result.failure(RuntimeException("Network"))

        viewModel.loadSteamPrice(steamAppId = 200, defaultPrice = 39.99)

        assertEquals(39.99, viewModel.uiState.value.price!!, 0.001)
        assertFalse(viewModel.uiState.value.isPriceLoading)
    }

    @Test
    fun loadReviewsForGame_success_computesAverageAndIdentifiesUserReview() {
        val ownReview = GameReview(id = "r1", gameId = "g1", userId = "uid-1", rating = 5, comment = "Great")
        val otherReview = GameReview(id = "r2", gameId = "g1", userId = "uid-2", rating = 3, comment = "Ok")
        stubReviews(Result.success(listOf(ownReview, otherReview)))

        viewModel.loadReviewsForGame("g1")

        val state = viewModel.uiState.value
        assertEquals("g1", state.gameId)
        assertEquals(2, state.reviews.size)
        assertEquals(ownReview, state.userReview)
        assertEquals(4.0, state.averageRating, 0.001)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadReviewsForGame_failure_setsDynamicOrFallbackError() {
        stubReviews(Result.failure(RuntimeException("Load error")))
        viewModel.loadReviewsForGame("g1")
        assertEquals("Load error", (viewModel.uiState.value.errorMessage as UiText.DynamicString).value)

        stubReviews(Result.failure(RuntimeException()))
        viewModel.loadReviewsForGame("g1")
        assertEquals(
            R.string.detail_error_load_reviews,
            (viewModel.uiState.value.errorMessage as UiText.StringResource).resId
        )
    }

    @Test
    fun submitReview_withBlankComment_setsValidationErrorWithoutSaving() {
        viewModel.submitReview("g1", "Hades", 5, "   ", "Guest")

        val message = viewModel.uiState.value.errorMessage
        assertTrue(message is UiText.StringResource)
        assertEquals(R.string.detail_error_comment_required, (message as UiText.StringResource).resId)
        verify(exactly = 0) { gameRepository.addReviewForGame(any(), any()) }
    }

    @Test
    fun submitReview_successAndFailure_updateMessagesAndReloadOnSuccess() {
        stubReviews(Result.success(emptyList()))
        val addSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.addReviewForGame(any(), capture(addSlot)) } answers {
            addSlot.captured.invoke(true)
        }

        viewModel.submitReview("g1", "Hades", 5, "  Awesome  ", "Guest")
        assertEquals(
            R.string.detail_review_published_success,
            (viewModel.uiState.value.successMessage as UiText.StringResource).resId
        )
        verify(exactly = 1) { gameRepository.getReviewsForGame("g1", any()) }

        every { gameRepository.addReviewForGame(any(), capture(addSlot)) } answers {
            addSlot.captured.invoke(false)
        }
        viewModel.submitReview("g1", "Hades", 4, "Good", "Guest")
        assertEquals(
            R.string.detail_review_publish_error,
            (viewModel.uiState.value.errorMessage as UiText.StringResource).resId
        )
    }

    @Test
    fun updateReview_validatesBlankAndHandlesSuccessAndFailure() {
        viewModel.updateReview("r1", 4, "")
        assertEquals(
            R.string.detail_error_comment_required,
            (viewModel.uiState.value.errorMessage as UiText.StringResource).resId
        )

        stubReviews(Result.success(emptyList()))
        val updateSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.updateReview("r1", 4, "Edited", capture(updateSlot)) } answers {
            updateSlot.captured.invoke(true)
        }
        viewModel.updateReview("r1", 4, " Edited ")
        assertEquals(
            R.string.detail_review_updated_success,
            (viewModel.uiState.value.successMessage as UiText.StringResource).resId
        )

        every { gameRepository.updateReview("r1", 4, "Edited", capture(updateSlot)) } answers {
            updateSlot.captured.invoke(false)
        }
        viewModel.updateReview("r1", 4, "Edited")
        assertEquals(
            R.string.detail_review_update_error,
            (viewModel.uiState.value.errorMessage as UiText.StringResource).resId
        )
    }

    @Test
    fun deleteReview_and_clearMessages_updateStateCorrectly() {
        stubReviews(Result.success(emptyList()))
        val deleteSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.deleteReview("r1", capture(deleteSlot)) } answers {
            deleteSlot.captured.invoke(true)
        }

        viewModel.deleteReview("r1")
        assertEquals(
            R.string.detail_review_deleted_success,
            (viewModel.uiState.value.successMessage as UiText.StringResource).resId
        )

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.errorMessage)

        every { gameRepository.deleteReview("r1", capture(deleteSlot)) } answers {
            deleteSlot.captured.invoke(false)
        }
        viewModel.deleteReview("r1")
        assertEquals(
            R.string.detail_review_delete_error,
            (viewModel.uiState.value.errorMessage as UiText.StringResource).resId
        )
    }
}
