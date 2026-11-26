package com.pamn.gamematch.ui.discover

import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import com.pamn.gamematch.utils.SortOption
import com.pamn.gamematch.utils.UiText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DiscoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: DiscoverViewModel

    private val rpgGame = Game(
        id = "1",
        title = "Elden Ring",
        genre = "RPG, Action",
        price = 49.99,
        rating = 9.5,
        ratingCount = 1200,
        isFeatured = true,
        isTrending = false
    )
    private val indieGame = Game(
        id = "2",
        title = "Hollow Knight",
        genre = "Indie, Metroidvania, Varios",
        price = 14.99,
        rating = 9.0,
        ratingCount = 2500,
        isFeatured = false,
        isTrending = true
    )
    private val freeGame = Game(
        id = "3",
        title = "Warframe",
        genre = "Action",
        price = 0.0,
        rating = 8.0,
        ratingCount = 800,
        isFeatured = true,
        isTrending = true
    )

    @Before
    fun setUp() {
        coEvery { repository.getGamesCatalog(pageSize = 500) } returns Result.success(
            listOf(rpgGame, indieGame, freeGame)
        )
        viewModel = DiscoverViewModel(repository)
    }

    private fun stubLikedGames(result: Result<List<String>>) {
        val callbackSlot = slot<(Result<List<String>>) -> Unit>()
        every { repository.getLikedGames(capture(callbackSlot)) } answers {
            callbackSlot.captured.invoke(result)
        }
    }

    @Test
    fun loadData_success_populatesGenresFeaturedTrendingAndLikedIds() = runTest {
        stubLikedGames(Result.success(listOf("2")))

        viewModel.loadData()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.availableGenres.first() == "Todas")
        assertFalse(state.availableGenres.contains("Varios"))
        assertEquals(listOf(rpgGame, freeGame), state.featuredGames)
        assertEquals(listOf(indieGame, freeGame), state.trendingGames)
        assertEquals(setOf("2"), state.likedGameIds)
        assertEquals(3, state.filteredGames.size)
        assertFalse(state.isFilteringActive)
    }

    @Test
    fun loadData_whenLikedGamesFails_setsErrorWhileKeepingCatalog() = runTest {
        stubLikedGames(Result.failure(RuntimeException()))

        viewModel.loadData()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.filteredGames.size)
        assertTrue(state.errorMessage is UiText.StringResource)
        assertEquals(R.string.discover_error_load_favorites, (state.errorMessage as UiText.StringResource).resId)
    }

    @Test
    fun onSearchQueryChanged_filtersByTitleOrGenreAndActivatesFiltering() = runTest {
        stubLikedGames(Result.success(emptyList()))
        viewModel.loadData()

        viewModel.onSearchQueryChanged("elden")

        val state = viewModel.uiState.value
        assertTrue(state.isFilteringActive)
        assertEquals(listOf(rpgGame), state.filteredGames)
    }

    @Test
    fun onGenreSelected_handlesMultiSelectionAndResetsToAll() = runTest {
        stubLikedGames(Result.success(emptyList()))
        viewModel.loadData()

        viewModel.onGenreSelected("RPG")
        assertEquals(setOf("RPG"), viewModel.uiState.value.selectedGenres)
        assertEquals(listOf(rpgGame), viewModel.uiState.value.filteredGames)

        viewModel.onGenreSelected("Indie")
        assertEquals(setOf("RPG", "Indie"), viewModel.uiState.value.selectedGenres)
        assertEquals(2, viewModel.uiState.value.filteredGames.size)

        viewModel.onGenreSelected("RPG")
        viewModel.onGenreSelected("Indie")
        assertEquals(setOf("Todas"), viewModel.uiState.value.selectedGenres)
        assertEquals(3, viewModel.uiState.value.filteredGames.size)
    }

    @Test
    fun onMaxPriceChanged_and_onSortSelected_filterAndSortResults() = runTest {
        stubLikedGames(Result.success(emptyList()))
        viewModel.loadData()

        viewModel.onMaxPriceChanged(20.0)
        viewModel.onSortSelected(SortOption.PRICE_ASC)

        val state = viewModel.uiState.value
        assertTrue(state.isFilteringActive)
        assertEquals(listOf(freeGame, indieGame), state.filteredGames)
    }

    @Test
    fun toggleFavorite_addsAndRemovesLikedGameIdWhenConfirmed() = runTest {
        stubLikedGames(Result.success(listOf("1")))
        viewModel.loadData()

        val saveSlot = slot<(Boolean) -> Unit>()
        every { repository.saveLikedGame("2", capture(saveSlot)) } answers {
            saveSlot.captured.invoke(true)
        }
        viewModel.toggleFavorite("2")
        assertEquals(setOf("1", "2"), viewModel.uiState.value.likedGameIds)

        val removeSlot = slot<(Boolean) -> Unit>()
        every { repository.removeLikedGame("1", capture(removeSlot)) } answers {
            removeSlot.captured.invoke(true)
        }
        viewModel.toggleFavorite("1")
        assertEquals(setOf("2"), viewModel.uiState.value.likedGameIds)
    }

    @Test
    fun matchesPriceFilter_coversUnlimitedNullAndThresholdCases() {
        assertTrue(matchesPriceFilter(price = null, maxPrice = 100.0))
        assertFalse(matchesPriceFilter(price = null, maxPrice = 50.0))
        assertTrue(matchesPriceFilter(price = 25.0, maxPrice = 25.0))
        assertFalse(matchesPriceFilter(price = 30.0, maxPrice = 25.0))
    }
}
