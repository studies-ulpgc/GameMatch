package com.pamn.gamematch.ui.swipe

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.theme.LikeColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue

@Composable
fun SwipeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFullDetails: (String) -> Unit = {},
    gameRepository: GameRepository? = if (LocalInspectionMode.current) null else remember { GameRepository() }
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background

    // Colores dinámicos del botón Dislike (❌)
    val dislikeBgColor = if (isDark) Color.White else Color(0xFF1E1E24)
    val dislikeIconColor = if (isDark) Color.Black else Color.White

    // Colores dinámicos de los botones secundarios (↺ e ℹ️)
    val secondaryBtnBg = if (isDark) Color(0xFF2C2C35) else Color(0xFFE5E7EB)
    val secondaryIconColor = if (isDark) Color.White else Color(0xFF171A21)

    var gameList by remember {
        mutableStateOf(
            listOf(
                Game("1", "The Legend of Zelda", "Aventura épica de exploración e investigación en mundo abierto.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg", "Aventura", 59.99, "Disponible", true),
                Game("2", "GTA VI", "Siguiente entrega de la aclamada franquicia de acción en mundo abierto.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg", "Acción", 79.99, "Lanzamiento: Otoño 2026", false),
                Game("3", "Hollow Knight: Silksong", "Explora el reino de Pharloom guiando a Hornet en una aventura 2D.", "https://cdn.cloudflare.steamstatic.com/steam/apps/367520/header.jpg", "Indie", 19.99, "Por anunciar", false),
                Game("4", "Elden Ring", "Desafiante juego de rol en un vasto e imponente mundo de fantasía.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg", "RPG", 69.99, "Disponible", true)
            )
        )
    }

    var history by remember { mutableStateOf(listOf<Game>()) }
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }

    val currentGame = gameList.firstOrNull()
    val nextGame = gameList.getOrNull(1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Pila de Tarjetas a pantalla completa
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (currentGame != null) {
                // 1. Tarjeta siguiente
                nextGame?.let { next ->
                    GameCard(
                        game = next,
                        modifier = Modifier
                            .scale(0.95f)
                            .alpha(0.45f)
                    )
                }

                // 2. Tarjeta actual
                key(currentGame.id) {
                    GameCard(
                        game = currentGame,
                        onSwipeLeft = { // Izquierda = Dislike ❌
                            history = history + currentGame
                            gameList = gameList.drop(1)
                        },
                        onSwipeRight = { // Derecha = Like ❤️
                            gameRepository?.saveLikedGame(currentGame.id) { success ->
                                if (success) Toast.makeText(context, "¡Guardado en favoritos!", Toast.LENGTH_SHORT).show()
                            }
                            history = history + currentGame
                            gameList = gameList.drop(1)
                        }
                    )
                }
            } else {
                Text(
                    text = "¡No hay más juegos por descubrir!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Botones de interacción flotantes sobre la imagen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Dislike (❌) - Invertido según Modo Oscuro/Claro
            IconButton(
                onClick = {
                    if (currentGame != null) {
                        history = history + currentGame
                        gameList = gameList.drop(1)
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(dislikeBgColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dislike",
                    tint = dislikeIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 2. Rebobinar / Deshacer (↺)
            IconButton(
                onClick = {
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history = history.dropLast(1)
                        gameList = listOf(last) + gameList

                        gameRepository?.removeLikedGame(last.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Deshecho y eliminado de favoritos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "No hay historial para deshacer", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(secondaryBtnBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Deshacer",
                    tint = secondaryIconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3. Pop-Up Más Información (ℹ️)
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
                    contentDescription = "Información",
                    tint = SteamAccentBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 4. Like (❤️)
            IconButton(
                onClick = {
                    if (currentGame != null) {
                        gameRepository?.saveLikedGame(currentGame.id) { success ->
                            if (success) Toast.makeText(context, "¡Guardado en favoritos!", Toast.LENGTH_SHORT).show()
                        }
                        history = history + currentGame
                        gameList = gameList.drop(1)
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(LikeColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Modal de Detalles
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

@Preview(showBackground = true)
@Composable
fun SwipeScreenPreview() {
    SwipeScreen()
}