package com.pamn.gamematch.ui.swipe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.model.Game

@Composable
fun GameDetailModal(
    game: Game,
    onDismiss: () -> Unit,
    onNavigateToFullDetails: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = game.title, style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column {
                Text(
                    text = "Género: ${game.genre}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Precio: ${if (game.price == 0.0) "Gratis" else "${game.price} €"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onNavigateToFullDetails(game.id)
                }
            ) {
                Text("Más Información ➔")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}