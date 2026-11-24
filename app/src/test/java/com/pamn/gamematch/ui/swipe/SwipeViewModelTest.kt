package com.pamn.gamematch.ui.swipe

import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.UiText
import io.mockk.coEvery
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

class SwipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: SwipeViewModel

    private val testGame1 = Game(
        id = "1",
        title = "Zelda",
        description = "Aventura",
        imageUrl = "http://example.com/z.jpg",
        genre = "Aventura",
        price = 50.0,
        releaseDate = "Disponible",
        isReleased = true
    )

    private val testGame2 = Game(
        id = "2",
        title = "Mario",
        description = "Plataformas",
        imageUrl = "http://example.com/m.jpg",
        genre = "Plataformas",
        price = 60.0,
        releaseDate = "Disponible",
        isReleased = true
    )

    @Before
    fun setUp() {
        coEvery { gameRepository.getGamesCatalog(pageSize = 500) } returns Result.success(listOf(testGame1, testGame2))
        val likedSlot = slot<(Result<List<String>>) -> Unit>()
        every { gameRepository.getLikedGames(capture(likedSlot)) } answers {
            likedSlot.captured.invoke(Result.success(emptyList()))
        }
        val prefsSlot = slot<(Result<User>) -> Unit>()
        every { gameRepository.getUserPreferences(capture(prefsSlot)) } answers {
            prefsSlot.captured.invoke(Result.success(User()))
        }

        viewModel = SwipeViewModel(gameRepository)
    }

    @Test
    fun loadCatalog_populatesGameListAndSetsLoadingFalse() {
        viewModel.loadCatalog()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingCatalog)
        assertEquals(2, state.gameList.size)
        assertEquals("1", state.gameList.first().id)
    }

    @Test
    fun onSwipeLeft_addsToHistoryAndRemovesFromGameList() {
        viewModel.loadCatalog()
        viewModel.onSwipeLeft()

        val state = viewModel.uiState.value
        assertEquals(1, state.history.size)
        assertEquals("1", state.history.first().id)
        assertEquals(1, state.gameList.size)
        assertEquals("2", state.gameList.first().id)
    }

    @Test
    fun onSwipeRight_savesLikedGameAndAddsToHistory() {
        val saveSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.saveLikedGame(eq("1"), capture(saveSlot)) } answers {
            saveSlot.captured.invoke(true)
        }

        viewModel.loadCatalog()
        viewModel.onSwipeRight()

        val state = viewModel.uiState.value
        assertEquals(1, state.history.size)
        assertEquals(1, state.gameList.size)
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.swipe_saved_favorites, (state.userMessage as UiText.StringResource).resId)
        verify { gameRepository.saveLikedGame("1", any()) }
    }

    @Test
    fun onUndo_withEmptyHistory_showsEmptyHistoryMessage() {
        viewModel.onUndo()

        val state = viewModel.uiState.value
        assertTrue(state.userMessage is UiText.StringResource)
        assertEquals(R.string.swipe_undo_empty, (state.userMessage as UiText.StringResource).resId)
    }

    @Test
    fun onUndo_withHistory_restoresGameAndRemovesFromLiked() {
        val removeSlot = slot<(Boolean) -> Unit>()
        every { gameRepository.removeLikedGame(eq("1"), capture(removeSlot)) } answers {
            removeSlot.captured.invoke(true)
        }

        viewModel.loadCatalog()
        viewModel.onSwipeLeft() // move game1 to history

        viewModel.onUndo()

        val state = viewModel.uiState.value
        assertTrue(state.history.isEmpty())
        assertEquals("1", state.gameList.first().id)
        verify { gameRepository.removeLikedGame("1", any()) }
    }
}
