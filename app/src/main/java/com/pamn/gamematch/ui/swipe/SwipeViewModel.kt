package com.pamn.gamematch.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import com.pamn.gamematch.utils.recommendGames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwipeUiState(
    val isLoadingCatalog: Boolean = true,
    val gameList: List<Game> = emptyList(),
    val history: List<Game> = emptyList(),
    val selectedGameForModal: Game? = null,
    val dragProgress: Float = 0f,
    val userMessage: UiText? = null
)

class SwipeViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipeUiState())
    val uiState: StateFlow<SwipeUiState> = _uiState.asStateFlow()

    fun loadCatalog() {
        _uiState.update {
            it.copy(
                isLoadingCatalog = true,
                history = emptyList(),
                selectedGameForModal = null,
                dragProgress = 0f
            )
        }
        viewModelScope.launch {
            val catalogResult = gameRepository.getGamesCatalog(pageSize = 500)
            val catalog = catalogResult.getOrDefault(emptyList())

            gameRepository.getLikedGames { likedResult ->
                val likedIds = likedResult.getOrDefault(emptyList()).toSet()

                gameRepository.getUserPreferences { prefsResult ->
                    prefsResult
                        .onSuccess { userPrefs ->
                            val recommended = recommendGames(
                                catalog = catalog,
                                preferredGenres = userPrefs.preferredGenres,
                                minPrice = userPrefs.minPrice.toDouble(),
                                maxPrice = userPrefs.maxPrice.toDouble(),
                                onlyReleased = userPrefs.onlyReleased,
                                likedIds = likedIds
                            )
                            _uiState.update {
                                it.copy(
                                    gameList = recommended.ifEmpty { catalog },
                                    isLoadingCatalog = false
                                )
                            }
                        }
                        .onFailure { error ->
                            android.util.Log.w("SwipeViewModel", "Error al cargar preferencias de usuario, manteniendo comportamiento por defecto: ${error.localizedMessage}")
                            val unlikedCatalog = catalog.filterNot { game -> likedIds.contains(game.id) }
                            _uiState.update {
                                it.copy(
                                    gameList = unlikedCatalog.ifEmpty { catalog },
                                    isLoadingCatalog = false
                                )
                            }
                        }
                }
            }
        }
    }

    fun setDragProgress(progress: Float) {
        _uiState.update { it.copy(dragProgress = progress) }
    }

    fun onSwipeLeft() {
        val current = _uiState.value.gameList.firstOrNull() ?: return
        _uiState.update { state ->
            state.copy(
                history = state.history + current,
                gameList = state.gameList.drop(1),
                dragProgress = 0f
            )
        }
    }

    fun onSwipeRight() {
        val current = _uiState.value.gameList.firstOrNull() ?: return
        gameRepository.saveLikedGame(current.id) { success ->
            if (success) {
                _uiState.update { state ->
                    state.copy(userMessage = UiText.StringResource(R.string.swipe_saved_favorites))
                }
            }
        }
        _uiState.update { state ->
            state.copy(
                history = state.history + current,
                gameList = state.gameList.drop(1),
                dragProgress = 0f
            )
        }
    }

    fun onUndo() {
        val historyList = _uiState.value.history
        if (historyList.isNotEmpty()) {
            val last = historyList.last()
            _uiState.update { state ->
                state.copy(
                    history = state.history.dropLast(1),
                    gameList = listOf(last) + state.gameList,
                    dragProgress = 0f
                )
            }
            gameRepository.removeLikedGame(last.id) { success ->
                if (success) {
                    _uiState.update { state ->
                        state.copy(userMessage = UiText.StringResource(R.string.swipe_undo_success))
                    }
                }
            }
        } else {
            _uiState.update { state ->
                state.copy(userMessage = UiText.StringResource(R.string.swipe_undo_empty))
            }
        }
    }

    fun setSelectedGameForModal(game: Game?) {
        _uiState.update { it.copy(selectedGameForModal = game) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
