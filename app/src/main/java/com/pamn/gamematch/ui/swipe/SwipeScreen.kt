package com.pamn.gamematch.ui.swipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.repository.GameRepository

@Composable
fun SwipeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFullDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository() }

    var gameList by remember {
        mutableStateOf(
            listOf(
                Game("1", "The Legend of Zelda", "Aventura épica de exploración e investigación en mundo abierto.", "", "Aventura", 59.99),
                Game("2", "Elden Ring", "Desafiante juego de rol en un vasto e imponente mundo de fantasía.", "", "RPG", 69.99),
                Game("3", "Hollow Knight", "Explora cavernas sinuosas y combate criaturas corruptas en 2D.", "", "Indie", 14.99),
                Game("4", "Cyberpunk 2077", "RPG de acción y aventura ambientado en la megalópolis de Night City.", "", "Acción", 39.99)
            )
        )
    }

    var history by remember { mutableStateOf(listOf<Game>()) }
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }

    val currentGame = gameList.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera "Descubrir"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Descubrir", style = MaterialTheme.typography.headlineLarge)
            IconButton(onClick = onNavigateToProfile) {
                Text(text = "👤", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Area Central (Tarjeta)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentGame != null) {
                key(currentGame.id) {
                    GameCard(
                        game = currentGame,
                        onSwipeLeft = {
                            history = history + currentGame
                            gameList = gameList.drop(1)
                        },
                        onSwipeRight = {
                            gameRepository.saveLikedGame(currentGame.id) { success ->
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

        // Botones de interacción inferiores: Rebobinar | Dislike | Info | Like
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rebobinar / Deshacer (↺)
            SmallFloatingActionButton(
                onClick = {
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history = history.dropLast(1)
                        gameList = listOf(last) + gameList

                        // Borra la ID de Firebase por si se le dio Like previamente
                        gameRepository.removeLikedGame(last.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Deshecho y eliminado de favoritos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "No hay historial para deshacer", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Deshacer")
            }

            // Dislike (❌)
            FloatingActionButton(
                onClick = {
                    if (currentGame != null) {
                        history = history + currentGame
                        gameList = gameList.drop(1)
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dislike", tint = MaterialTheme.colorScheme.error)
            }

            // Pop-Up Más Información (i)
            SmallFloatingActionButton(
                onClick = {
                    if (currentGame != null) {
                        selectedGameForModal = currentGame
                    }
                }
            ) {
                Icon(Icons.Default.Info, contentDescription = "Información")
            }

            // Like (❤️)
            FloatingActionButton(
                onClick = {
                    if (currentGame != null) {
                        gameRepository.saveLikedGame(currentGame.id) { success ->
                            if (success) Toast.makeText(context, "¡Guardado en favoritos!", Toast.LENGTH_SHORT).show()
                        }
                        history = history + currentGame
                        gameList = gameList.drop(1)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Like", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Modal emergente de tu componente (si el usuario pulsa en 'i')
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