package com.pamn.gamematch.ui.swipe

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.detail.GameDetailModal

@Composable
fun SwipeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFullDetails: (String) -> Unit = {},
    viewModel: SwipeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadCatalog()
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    SwipeScreenContent(
        uiState = uiState,
        onDragProgress = viewModel::setDragProgress,
        onSwipeLeft = viewModel::onSwipeLeft,
        onSwipeRight = viewModel::onSwipeRight,
        onUndo = viewModel::onUndo,
        onReloadCatalog = viewModel::loadCatalog,
        onSelectGameForModal = viewModel::setSelectedGameForModal,
        onNavigateToFullDetails = onNavigateToFullDetails
    )
}

@Composable
fun SwipeScreenContent(
    uiState: SwipeUiState,
    onDragProgress: (Float) -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onUndo: () -> Unit = {},
    onReloadCatalog: () -> Unit = {},
    onSelectGameForModal: (Game?) -> Unit = {},
    onNavigateToFullDetails: (String) -> Unit = {}
) {
    val currentGame = uiState.gameList.firstOrNull()
    val nextGame = uiState.gameList.getOrNull(1)
    val bgScale = 0.90f + (uiState.dragProgress * 0.10f)
    val overlayAlpha = (0.85f - (uiState.dragProgress * 0.85f)).coerceIn(0f, 0.85f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                uiState.isLoadingCatalog -> CircularProgressIndicator()
                currentGame != null -> {
                    nextGame?.let { next ->
                        GameCard(
                            game = next,
                            overlayAlpha = overlayAlpha,
                            modifier = Modifier.scale(bgScale)
                        )
                    }
                    key(currentGame.id) {
                        GameCard(
                            game = currentGame,
                            overlayAlpha = 0f,
                            onDragProgress = onDragProgress,
                            onSwipeLeft = onSwipeLeft,
                            onSwipeRight = onSwipeRight
                        )
                    }
                }
                else -> SwipeEmptyState(onReloadCatalog = onReloadCatalog)
            }
        }

        if (!uiState.isLoadingCatalog && currentGame != null) {
            SwipeActionButtons(
                onSwipeLeft = onSwipeLeft,
                onUndo = onUndo,
                onShowInfo = { onSelectGameForModal(currentGame) },
                onSwipeRight = onSwipeRight,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    uiState.selectedGameForModal?.let { game ->
        GameDetailModal(
            game = game,
            onDismiss = { onSelectGameForModal(null) },
            onNavigateToFullDetails = onNavigateToFullDetails
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun SwipeScreenPreview() {
    SwipeScreenContent(
        uiState = SwipeUiState(
            isLoadingCatalog = false,
            gameList = SwipePreviewData.samplePreviewGames
        )
    )
}
