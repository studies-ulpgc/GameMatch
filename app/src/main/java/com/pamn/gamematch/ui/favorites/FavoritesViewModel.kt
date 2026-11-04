package com.pamn.gamematch.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estados posibles de la pantalla de Favoritos. */
sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val games: List<Game>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

class FavoritesViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadLikedGames()
    }

    /** Carga los juegos favoritos cruzando los IDs de Firestore con el catálogo general de Firestore. */
    fun loadLikedGames() {
        _uiState.value = FavoritesUiState.Loading
        viewModelScope.launch {
            val catalogResult = repository.getGamesCatalog()
            val catalog = catalogResult.getOrDefault(emptyList())

            repository.getLikedGames { result ->
                result
                    .onSuccess { ids ->
                        val likedGames = ids.mapNotNull { id ->
                            catalog.find { game -> game.id == id }
                        }
                        _uiState.value =
                            if (likedGames.isEmpty()) FavoritesUiState.Empty
                            else FavoritesUiState.Success(likedGames)
                    }
                    .onFailure {
                        _uiState.value =
                            FavoritesUiState.Error(it.message ?: "No se pudieron cargar tus matches")
                    }
            }
        }
    }

    /** Quita un juego de favoritos; actualiza la UI al instante si Firestore confirma. */
    fun removeFavorite(gameId: String) {
        repository.removeLikedGame(gameId) { success ->
            if (!success) return@removeLikedGame
            val current = _uiState.value as? FavoritesUiState.Success ?: return@removeLikedGame
            val updated = current.games.filterNot { it.id == gameId }
            _uiState.value =
                if (updated.isEmpty()) FavoritesUiState.Empty
                else FavoritesUiState.Success(updated)
        }
    }

    /** Vuelve a añadir un juego a favoritos (útil para la función deshacer). */
    fun addFavorite(game: Game) {
        repository.saveLikedGame(game.id) { success ->
            if (!success) return@saveLikedGame
            val current = _uiState.value
            if (current is FavoritesUiState.Success) {
                if (current.games.none { it.id == game.id }) {
                    _uiState.value = FavoritesUiState.Success(current.games + game)
                }
            } else if (current is FavoritesUiState.Empty) {
                _uiState.value = FavoritesUiState.Success(listOf(game))
            }
        }
    }
}