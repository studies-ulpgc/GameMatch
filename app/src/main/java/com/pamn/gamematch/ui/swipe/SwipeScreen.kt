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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.detail.GameDetailModal
import com.pamn.gamematch.ui.theme.LikeColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import kotlinx.coroutines.launch

@Composable
fun SwipeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFullDetails: (String) -> Unit = {},
    gameRepository: GameRepository? = if (LocalInspectionMode.current) null else remember { GameRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bgColor = MaterialTheme.colorScheme.background

    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.5f

    val dislikeBgColor = if (isDarkTheme) Color.White else Color(0xFF1E1E24)
    val dislikeIconColor = if (isDarkTheme) Color.Black else Color.White

    val secondaryBtnBg = if (isDarkTheme) Color(0xFF2C2C35) else Color(0xFFD1D5DB)
    val secondaryIconColor = if (isDarkTheme) Color.White else Color(0xFF171A21)

    var isLoadingCatalog by remember { mutableStateOf(true) }
    var gameList by remember { mutableStateOf<List<Game>>(emptyList()) }

    fun loadCatalog() {
        isLoadingCatalog = true
        if (gameRepository != null) {
            coroutineScope.launch {
                val catalogResult = gameRepository.getGamesCatalog(pageSize = 500)
                val catalog = catalogResult.getOrDefault(emptyList())

                gameRepository.getLikedGames { likedResult ->
                    val likedIds = likedResult.getOrDefault(emptyList()).toSet()

                    gameRepository.getUserPreferences { prefsResult ->
                        prefsResult
                            .onSuccess { userPrefs ->
                                val recommended = com.pamn.gamematch.utils.recommendGames(
                                    catalog = catalog,
                                    preferredGenres = userPrefs.preferredGenres,
                                    minPrice = userPrefs.minPrice.toDouble(),
                                    maxPrice = userPrefs.maxPrice.toDouble(),
                                    onlyReleased = userPrefs.onlyReleased,
                                    likedIds = likedIds
                                )
                                gameList = recommended.ifEmpty { catalog }
                                isLoadingCatalog = false
                            }
                            .onFailure { error ->
                                android.util.Log.w("SwipeScreen", "Error al cargar preferencias de usuario, manteniendo comportamiento por defecto: ${error.localizedMessage}")
                                val unlikedCatalog = catalog.filterNot { game -> likedIds.contains(game.id) }
                                gameList = unlikedCatalog.ifEmpty { catalog }
                                isLoadingCatalog = false
                            }
                    }
                }
            }
        } else {
            gameList = gameRepository?.getPreviewGames() ?: emptyList()
            isLoadingCatalog = false
        }
    }

    LaunchedEffect(Unit) {
        loadCatalog()
    }

    var history by remember { mutableStateOf(listOf<Game>()) }
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentGame = gameList.firstOrNull()
    val nextGame = gameList.getOrNull(1)

    // Escalado progresivo de la tarjeta trasera
    val bgScale = 0.90f + (dragProgress * 0.10f)

    // Oscurecimiento más fuerte (empieza en 0.85f) y dura más tiempo hasta que está totalmente en el centro
    val overlayAlpha = (0.85f - (dragProgress * 0.85f)).coerceIn(0f, 0.85f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingCatalog) {
                CircularProgressIndicator()
            } else if (currentGame != null) {
                // Tarjeta de fondo con sombra negra intensa y progresiva
                nextGame?.let { next ->
                    GameCard(
                        game = next,
                        overlayAlpha = overlayAlpha,
                        modifier = Modifier.scale(bgScale)
                    )
                }

                // Tarjeta interactiva principal
                key(currentGame.id) {
                    GameCard(
                        game = currentGame,
                        overlayAlpha = 0f,
                        onDragProgress = { progress ->
                            dragProgress = progress
                        },
                        onSwipeLeft = {
                            history = history + currentGame
                            gameList = gameList.drop(1)
                            dragProgress = 0f
                        },
                        onSwipeRight = {
                            gameRepository?.saveLikedGame(currentGame.id) { success ->
                                if (success) Toast.makeText(context, context.resources.getString(com.pamn.gamematch.R.string.swipe_saved_favorites), Toast.LENGTH_SHORT).show()
                            }
                            history = history + currentGame
                            gameList = gameList.drop(1)
                            dragProgress = 0f
                        }
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
                        onClick = { loadCatalog() }
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_reload_button))
                    }
                }
            }
        }

        if (!isLoadingCatalog && currentGame != null) {
            // Botones inferiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dislike (❌)
                IconButton(
                    onClick = {
                        if (currentGame != null) {
                            history = history + currentGame
                            gameList = gameList.drop(1)
                            dragProgress = 0f
                        }
                    },
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

                // 2. Rebobinar (↺)
                IconButton(
                    onClick = {
                        if (history.isNotEmpty()) {
                            val last = history.last()
                            history = history.dropLast(1)
                            gameList = listOf(last) + gameList
                            dragProgress = 0f

                            gameRepository?.removeLikedGame(last.id) { success ->
                                if (success) {
                                    Toast.makeText(context, context.resources.getString(com.pamn.gamematch.R.string.swipe_undo_success), Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, context.resources.getString(com.pamn.gamematch.R.string.swipe_undo_empty), Toast.LENGTH_SHORT).show()
                        }
                    },
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

                // 3. Info (ℹ️)
                IconButton(
                    onClick = {
                        if (currentGame != null) {
                            selectedGameForModal = currentGame
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(secondaryBtnBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.swipe_cd_info),
                        tint = if (isDarkTheme) secondaryIconColor else SteamAccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 4. Like (❤️)
                IconButton(
                    onClick = {
                        if (currentGame != null) {
                            gameRepository?.saveLikedGame(currentGame.id) { success ->
                                if (success) Toast.makeText(context, context.resources.getString(com.pamn.gamematch.R.string.swipe_saved_favorites), Toast.LENGTH_SHORT).show()
                            }
                            history = history + currentGame
                            gameList = gameList.drop(1)
                            dragProgress = 0f
                        }
                    },
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

    selectedGameForModal?.let { game ->
        GameDetailModal(
            game = game,
            onDismiss = { selectedGameForModal = null },
            onNavigateToFullDetails = { gameId ->
                onNavigateToFullDetails(gameId)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun SwipeScreenPreview() {
    SwipeScreen()
}
