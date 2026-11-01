package com.pamn.gamematch.ui.swipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.model.Game

@Composable
fun SwipeScreen(
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current

    // Juegos de prueba simulados
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

    val currentGame = gameList.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Título superior de la vista "Descubrir"
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

        // Área central de la tarjeta
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentGame != null) {
                // Usamos key(currentGame.id) para forzar a resetear el estado y la animación en cada nuevo juego
                key(currentGame.id) {
                    GameCard(
                        game = currentGame,
                        onSwipeLeft = {
                            Toast.makeText(context, "Pasaste de: ${currentGame.title}", Toast.LENGTH_SHORT).show()
                            gameList = gameList.drop(1)
                        },
                        onSwipeRight = {
                            Toast.makeText(context, "¡Like a: ${currentGame.title}!", Toast.LENGTH_SHORT).show()
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

        // Botones inferiores de interaccion (Dislike / Like)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    if (currentGame != null) {
                        Toast.makeText(context, "Pasaste de: ${currentGame.title}", Toast.LENGTH_SHORT).show()
                        gameList = gameList.drop(1)
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dislike", tint = MaterialTheme.colorScheme.error)
            }

            FloatingActionButton(
                onClick = {
                    if (currentGame != null) {
                        Toast.makeText(context, "¡Like a: ${currentGame.title}!", Toast.LENGTH_SHORT).show()
                        gameList = gameList.drop(1)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Like", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeScreenPreview() {
    SwipeScreen()
}