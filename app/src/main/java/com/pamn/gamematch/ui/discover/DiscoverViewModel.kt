package com.pamn.gamematch.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun matchesPriceFilter(price: Double?, maxPrice: Double): Boolean {
    return if (maxPrice >= 100.0) {
        true
    } else {
        price != null && price <= maxPrice
    }
}

data class DiscoverUiState(
    val searchQuery: String = "",
    val selectedGenres: Set<String> = setOf("Todas"),
    val availableGenres: List<String> = listOf("Todas"),
    val maxPrice: Double = 100.0,
    val featuredGames: List<Game> = emptyList(),
    val trendingGames: List<Game> = emptyList(),
    val filteredGames: List<Game> = emptyList(),
    val likedGameIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** Deprecated property maintained for backwards compatibility. */
    val selectedPlatforms: Set<String> get() = selectedGenres

    /** Indica si hay algún filtro o búsqueda activo para cambiar de carruseles a resultados filtrados. */
    val isFilteringActive: Boolean
        get() = searchQuery.isNotBlank() || !selectedGenres.contains("Todas") || maxPrice < 100.0
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

    /** Carga inicial del catálogo desde Firestore y los favoritos desde el repositorio. */
    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val catalogResult = repository.getGamesCatalog(pageSize = 500)
            val catalog = catalogResult.getOrDefault(emptyList())
            allGamesCatalog = catalog

            val topGenres = catalog
                .flatMap { game -> game.genre.split(",").map { it.trim() } }
                .filter { it.isNotBlank() && !it.equals("Varios", ignoreCase = true) }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
                .take(8)

            val availableGenresList = listOf("Todas") + topGenres

            val featured = catalog.filter { it.isFeatured }.sortedByDescending { it.rating }.take(15)
            val trending = catalog.filter { it.isTrending }.sortedByDescending { it.ratingCount ?: 0 }.take(15)

            repository.getLikedGames { result ->
                result
                    .onSuccess { ids ->
                        _uiState.update { state ->
                            state.copy(
                                availableGenres = availableGenresList,
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
                                availableGenres = availableGenresList,
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
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /** Permite selección múltiple de géneros o seleccionar 'Todas' para limpiar seleccionadas. */
    fun onGenreSelected(genre: String) {
        _uiState.update { state ->
            val current = state.selectedGenres
            val updated = if (genre == "Todas") {
                setOf("Todas")
            } else {
                val withoutAll = current - "Todas"
                val toggled = if (withoutAll.contains(genre)) {
                    withoutAll - genre
                } else {
                    withoutAll + genre
                }
                if (toggled.isEmpty()) {
                    setOf("Todas")
                } else {
                    toggled
                }
            }
            state.copy(selectedGenres = updated)
        }
        applyFilters()
    }

    @Deprecated("Usar onGenreSelected", ReplaceWith("onGenreSelected(platform)"))
    fun onPlatformSelected(platform: String) = onGenreSelected(platform)

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
        val genres = state.selectedGenres
        val maxPrice = state.maxPrice

        val filtered = allGamesCatalog.filter { game ->
            val matchesQuery = query.isEmpty() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.genre.contains(query, ignoreCase = true)

            val gameGenres = game.genre.split(",").map { it.trim() }
            val matchesGenre = genres.contains("Todas") ||
                    gameGenres.any { g ->
                        genres.any { sel -> sel.equals(g, ignoreCase = true) }
                    }

            val matchesPrice = matchesPriceFilter(game.price, maxPrice)

            matchesQuery && matchesGenre && matchesPrice
        }

        _uiState.update { it.copy(filteredGames = filtered) }
    }
}
