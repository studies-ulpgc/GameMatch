package com.pamn.gamematch.ui.swipe

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.detail.GameDetailModal
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.LikeColor

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
    val bgColor = MaterialTheme.colorScheme.background

    val semanticColors = GameMatchTheme.semanticColors
    val dislikeBgColor = semanticColors.dislikeBg
    val dislikeIconColor = semanticColors.dislikeIcon
    val secondaryBtnBg = semanticColors.secondaryBtnBg
    val secondaryIconColor = semanticColors.secondaryIcon
    val infoIconTint = semanticColors.infoIconTint

    val currentGame = uiState.gameList.firstOrNull()
    val nextGame = uiState.gameList.getOrNull(1)

    val bgScale = 0.90f + (uiState.dragProgress * 0.10f)
    val overlayAlpha = (0.85f - (uiState.dragProgress * 0.85f)).coerceIn(0f, 0.85f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoadingCatalog) {
                CircularProgressIndicator()
            } else if (currentGame != null) {
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
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onReloadCatalog
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_reload_button))
                    }
                }
            }
        }

        if (!uiState.isLoadingCatalog && currentGame != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSwipeLeft,
                    modifier = Modifier
                        .size(56.dp)
                        .background(dislikeBgColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_cd_dislike),
                        tint = dislikeIconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .size(44.dp)
                        .background(secondaryBtnBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_cd_undo),
                        tint = secondaryIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (currentGame != null) {
                            onSelectGameForModal(currentGame)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(secondaryBtnBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_cd_info),
                        tint = infoIconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onSwipeRight,
                    modifier = Modifier
                        .size(56.dp)
                        .background(LikeColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_cd_like),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    uiState.selectedGameForModal?.let { game ->
        GameDetailModal(
            game = game,
            onDismiss = { onSelectGameForModal(null) },
            onNavigateToFullDetails = { gameId ->
                onNavigateToFullDetails(gameId)
            }
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
