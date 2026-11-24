package com.pamn.gamematch.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val gameId: String = "",
    val reviews: List<GameReview> = emptyList(),
    val userReview: GameReview? = null,
    val averageRating: Double = 0.0,
    val price: Double? = null,
    val isPriceLoading: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)

class GameDetailViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    /** Consulta asíncrona bajo demanda del precio en Steam. */
    fun loadSteamPrice(steamAppId: Int?, defaultPrice: Double?) {
        if (steamAppId != null) {
            _uiState.update { it.copy(isPriceLoading = true, price = null) }
            viewModelScope.launch {
                val result = gameRepository.getSteamPrice(steamAppId)
                result
                    .onSuccess { price ->
                        _uiState.update { it.copy(price = price ?: defaultPrice, isPriceLoading = false) }
                    }
                    .onFailure {
                        _uiState.update { it.copy(price = defaultPrice, isPriceLoading = false) }
                    }
            }
        } else {
            _uiState.update { it.copy(price = defaultPrice, isPriceLoading = false) }
        }
    }

    /** Carga las reseñas asociadas al juego garantizando el aislamiento por usuario autenticado. */
    fun loadReviewsForGame(gameId: String) {
        _uiState.update {
            it.copy(
                gameId = gameId,
                userReview = null,
                reviews = emptyList(),
                averageRating = 0.0,
                isLoading = true,
                errorMessage = null
            )
        }

        gameRepository.getReviewsForGame(gameId) { result ->
            result
                .onSuccess { reviewsList ->
                    val currentUid = authRepository.currentUserId ?: ""
                    val myReview = reviewsList.find { it.userId == currentUid }
                    val avg = if (reviewsList.isNotEmpty()) {
                        reviewsList.map { it.rating }.average()
                    } else 0.0

                    _uiState.update { state ->
                        state.copy(
                            reviews = reviewsList,
                            userReview = myReview,
                            averageRating = avg,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.detail_error_load_reviews)
                        )
                    }
                }
        }
    }

    fun submitReview(gameId: String, gameTitle: String, rating: Int, comment: String, defaultUserName: String) {
        if (comment.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.detail_error_comment_required)) }
            return
        }

        val currentUid = authRepository.currentUserId ?: ""
        val username = authRepository.getCurrentUserName(defaultUserName)

        val review = GameReview(
            gameId = gameId,
            gameTitle = gameTitle,
            userId = currentUid,
            userName = username,
            rating = rating,
            comment = comment.trim(),
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        gameRepository.addReviewForGame(review) { success ->
            _uiState.update { it.copy(isSaving = false) }
            if (success) {
                _uiState.update { it.copy(successMessage = UiText.StringResource(R.string.detail_review_published_success)) }
                loadReviewsForGame(gameId)
            } else {
                _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.detail_review_publish_error)) }
            }
        }
    }

    fun updateReview(reviewId: String, rating: Int, comment: String) {
        if (comment.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.detail_error_comment_required)) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        gameRepository.updateReview(reviewId, rating, comment.trim()) { success ->
            _uiState.update { it.copy(isSaving = false) }
            if (success) {
                _uiState.update { it.copy(successMessage = UiText.StringResource(R.string.detail_review_updated_success)) }
                loadReviewsForGame(_uiState.value.gameId)
            } else {
                _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.detail_review_update_error)) }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        _uiState.update { it.copy(isSaving = true) }

        gameRepository.deleteReview(reviewId) { success ->
            _uiState.update { it.copy(isSaving = false) }
            if (success) {
                _uiState.update { it.copy(successMessage = UiText.StringResource(R.string.detail_review_deleted_success)) }
                loadReviewsForGame(_uiState.value.gameId)
            } else {
                _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.detail_review_delete_error)) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
