package com.pamn.gamematch.ui.discover

import androidx.lifecycle.ViewModel
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiscoverUiState(
    val searchQuery: String = "",
    val selectedPlatform: String = "Todas",
    val maxPrice: Double = 100.0,
    val featuredGames: List<Game> = emptyList(),
    val trendingGames: List<Game> = emptyList(),
    val filteredGames: List<Game> = emptyList(),
    val likedGameIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** Indica si hay algún filtro o búsqueda activo para cambiar de carruseles a resultados filtrados. */
    val isFilteringActive: Boolean
        get() = searchQuery.isNotBlank() || selectedPlatform != "Todas" || maxPrice < 100.0
}

class DiscoverViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var allGamesCatalog: List<Game> = emptyList()

    init {
        loadData()
    }

    /** Carga inicial del catálogo y los favoritos desde el repositorio. */
    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        allGamesCatalog = repository.getAllGames()

        val featured = repository.getFeaturedGames()
        val trending = repository.getTrendingGames()

        repository.getLikedGames { result ->
            result
                .onSuccess { ids ->
                    _uiState.update { state ->
                        state.copy(
                            featuredGames = featured,
                            trendingGames = trending,
                            likedGameIds = ids.toSet(),
                            isLoading = false
                        )
                    }
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            featuredGames = featured,
                            trendingGames = trending,
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cargar favoritos"
                        )
                    }
                    applyFilters()
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onPlatformSelected(platform: String) {
        _uiState.update { it.copy(selectedPlatform = platform) }
        applyFilters()
    }

    fun onMaxPriceChanged(price: Double) {
        _uiState.update { it.copy(maxPrice = price) }
        applyFilters()
    }

    /** Añade o quita un juego de favoritos en Firestore y actualiza el estado. */
    fun toggleFavorite(gameId: String) {
        val isCurrentlyLiked = _uiState.value.likedGameIds.contains(gameId)
        if (isCurrentlyLiked) {
            repository.removeLikedGame(gameId) { success ->
                if (success) {
                    _uiState.update { state ->
                        state.copy(likedGameIds = state.likedGameIds - gameId)
                    }
                }
            }
        } else {
            repository.saveLikedGame(gameId) { success ->
                if (success) {
                    _uiState.update { state ->
                        state.copy(likedGameIds = state.likedGameIds + gameId)
                    }
                }
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val platform = state.selectedPlatform
        val maxPrice = state.maxPrice

        val filtered = allGamesCatalog.filter { game ->
            val matchesQuery = query.isEmpty() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.genre.contains(query, ignoreCase = true)

            val matchesPlatform = platform == "Todas" ||
                    game.platforms.any { it.equals(platform, ignoreCase = true) }

            val matchesPrice = game.price <= maxPrice

            matchesQuery && matchesPlatform && matchesPrice
        }

        _uiState.update { it.copy(filteredGames = filtered) }
    }
}