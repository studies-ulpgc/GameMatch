package com.pamn.gamematch.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.detail.GameDetailModal
import com.pamn.gamematch.ui.swipe.GameCard
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    onNavigateToFullDetails: (String) -> Unit = {},
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadLikedGames()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.favorites_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            when (val state = uiState) {
                FavoritesUiState.Loading -> CenteredBox {
                    CircularProgressIndicator()
                }

                FavoritesUiState.Empty -> CenteredBox {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.favorites_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                is FavoritesUiState.Error -> CenteredBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = viewModel::loadLikedGames,
                            modifier = Modifier.padding(top = 12.dp)
                        ) { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.favorites_retry)) }
                    }
                }

                is FavoritesUiState.Success -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.games, key = { it.id }) { game ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        FavoriteGameGridCard(
                            game = game,
                            onShowInfo = { selectedGameForModal = game },
                            onRemove = {
                                viewModel.removeFavorite(game.id)
                                coroutineScope.launch {
                                    val msg = context.getString(com.pamn.gamematch.R.string.favorites_removed_msg, game.title)
                                    val undoMsg = context.getString(com.pamn.gamematch.R.string.favorites_undo)
                                    val result = snackbarHostState.showSnackbar(
                                        message = msg,
                                        actionLabel = undoMsg,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addFavorite(game)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    selectedGameForModal?.let { game ->
        GameDetailModal(
            game = game,
            onDismiss = { selectedGameForModal = null },
            onNavigateToFullDetails = { gameId ->
                selectedGameForModal = null
                onNavigateToFullDetails(gameId)
            }
        )
    }
}

/** Tarjeta que reutiliza GameCard dentro de la cuadrícula con botones de Info y Quitar Favorito. */
@Composable
private fun FavoriteGameGridCard(
    game: Game,
    onShowInfo: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GameCard(
            game = game,
            compact = true,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botón Info (ℹ️) - literal el mismo componente de Swipe
            IconButton(
                onClick = onShowInfo,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.favorites_info),
                    tint = SteamAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Favorito (❤️) - quitar de favoritos
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.favorites_remove),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { content() }
}