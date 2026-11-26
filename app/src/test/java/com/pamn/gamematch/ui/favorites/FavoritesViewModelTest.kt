package com.pamn.gamematch.ui.favorites

import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.UiText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: FavoritesViewModel

    private val game1 = Game(id = "1", title = "Hades", genre = "Action")
    private val game2 = Game(id = "2", title = "Celeste", genre = "Indie")

    @Before
    fun setUp() {
        coEvery { repository.getGamesCatalog() } returns Result.success(listOf(game1, game2))
        viewModel = FavoritesViewModel(repository)
    }

    private fun stubLikedGames(result: Result<List<String>>) {
        val callbackSlot = slot<(Result<List<String>>) -> Unit>()
        every { repository.getLikedGames(capture(callbackSlot)) } answers {
            callbackSlot.captured.invoke(result)
        }
    }

    @Test
    fun loadLikedGames_withMatchingIds_emitsSuccess() = runTest {
        stubLikedGames(Result.success(listOf("2", "999")))

        viewModel.loadLikedGames()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(listOf(game2), (state as FavoritesUiState.Success).games)
    }

    @Test
    fun loadLikedGames_whenEmptyOrNoCatalogMatches_emitsEmpty() = runTest {
        coEvery { repository.getGamesCatalog() } returns Result.failure(RuntimeException("offline"))
        stubLikedGames(Result.success(listOf("1")))

        viewModel.loadLikedGames()

        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun loadLikedGames_whenFailureWithMessage_emitsDynamicError() = runTest {
        stubLikedGames(Result.failure(RuntimeException("Firestore timeout")))

        viewModel.loadLikedGames()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Error)
        val message = (state as FavoritesUiState.Error).message
        assertTrue(message is UiText.DynamicString)
        assertEquals("Firestore timeout", (message as UiText.DynamicString).value)
    }

    @Test
    fun loadLikedGames_whenFailureWithoutMessage_emitsDefaultResourceError() = runTest {
        stubLikedGames(Result.failure(RuntimeException()))

        viewModel.loadLikedGames()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Error)
        val message = (state as FavoritesUiState.Error).message
        assertTrue(message is UiText.StringResource)
        assertEquals(R.string.favorites_error_load, (message as UiText.StringResource).resId)
    }

    @Test
    fun removeFavorite_whenConfirmed_updatesListAndTransitionsToEmpty() = runTest {
        stubLikedGames(Result.success(listOf("1", "2")))
        viewModel.loadLikedGames()

        val removeSlot = slot<(Boolean) -> Unit>()
        every { repository.removeLikedGame(any(), capture(removeSlot)) } answers {
            removeSlot.captured.invoke(true)
        }

        viewModel.removeFavorite("1")
        val afterFirst = viewModel.uiState.value
        assertTrue(afterFirst is FavoritesUiState.Success)
        assertEquals(listOf(game2), (afterFirst as FavoritesUiState.Success).games)

        viewModel.removeFavorite("2")
        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun removeFavorite_whenRejected_keepsExistingFavorites() = runTest {
        stubLikedGames(Result.success(listOf("1")))
        viewModel.loadLikedGames()

        val removeSlot = slot<(Boolean) -> Unit>()
        every { repository.removeLikedGame("1", capture(removeSlot)) } answers {
            removeSlot.captured.invoke(false)
        }

        viewModel.removeFavorite("1")

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(listOf(game1), (state as FavoritesUiState.Success).games)
    }

    @Test
    fun addFavorite_restoresOnEmptyAndAvoidsDuplicatesOnSuccess() = runTest {
        stubLikedGames(Result.success(emptyList()))
        viewModel.loadLikedGames()
        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)

        val saveSlot = slot<(Boolean) -> Unit>()
        every { repository.saveLikedGame(any(), capture(saveSlot)) } answers {
            saveSlot.captured.invoke(true)
        }

        viewModel.addFavorite(game1)
        viewModel.addFavorite(game1)
        viewModel.addFavorite(game2)

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(listOf(game1, game2), (state as FavoritesUiState.Success).games)
    }
}
