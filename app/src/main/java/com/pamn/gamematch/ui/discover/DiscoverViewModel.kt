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

data class DiscoverUiState(
    val searchQuery: String = "",
    val selectedPlatforms: Set<String> = setOf("Todas"),
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
        get() = searchQuery.isNotBlank() || !selectedPlatforms.contains("Todas") || maxPrice < 100.0
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

            val featured = catalog.filter { it.isFeatured }.sortedByDescending { it.rating }.take(15)
            val trending = catalog.filter { it.isTrending }.sortedByDescending { it.ratingCount ?: 0 }.take(15)

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
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /** Permite selección múltiple de plataformas o seleccionar 'Todas' para limpiar seleccionadas. */
    fun onPlatformSelected(platform: String) {
        _uiState.update { state ->
            val current = state.selectedPlatforms
            val updated = if (platform == "Todas") {
                setOf("Todas")
            } else {
                val withoutAll = current - "Todas"
                val toggled = if (withoutAll.contains(platform)) {
                    withoutAll - platform
                } else {
                    withoutAll + platform
                }
                if (toggled.isEmpty()) {
                    setOf("Todas")
                } else {
                    toggled
                }
            }
            state.copy(selectedPlatforms = updated)
        }
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
        val platforms = state.selectedPlatforms
        val maxPrice = state.maxPrice

        val filtered = allGamesCatalog.filter { game ->
            val matchesQuery = query.isEmpty() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.genre.contains(query, ignoreCase = true)

            val matchesPlatform = platforms.contains("Todas") ||
                    game.platforms.any { p ->
                        val family = com.pamn.gamematch.utils.platformFamily(p)
                        family != null && platforms.contains(family)
                    }

            val matchesPrice = (game.price ?: 0.0) <= maxPrice

            matchesQuery && matchesPlatform && matchesPrice
        }

        _uiState.update { it.copy(filteredGames = filtered) }
    }
}
