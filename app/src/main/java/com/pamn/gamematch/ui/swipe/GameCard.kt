package com.pamn.gamematch.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.UnreleasedBadgeColor
import kotlin.math.roundToInt

@Composable
fun GameCard(
    game: Game,
    overlayAlpha: Float = 0f,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDragProgress: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    var offsetX by remember { mutableFloatStateOf(0f) }

    // Ángulo de inclinación suave según el arrastre
    val rotation = (offsetX / 600f) * 15f

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = rotation
            }
            .pointerInput(game.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 120f) {
                            onSwipeRight()
                        } else if (offsetX < -120f) {
                            onSwipeLeft()
                        }
                        offsetX = 0f
                        onDragProgress(0f)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                        val progress = (kotlin.math.abs(offsetX) / 300f).coerceIn(0f, 1f)
                        onDragProgress(progress)
                    }
                )
            }
    ) {
        AsyncImage(
            model = if (game.imageUrl.isBlank()) "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg" else game.imageUrl,
            contentDescription = game.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Capa de velo negro más intensa que se desvanece al acercarse al centro
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
            )
        }

        // Degradado inferior para legibilidad del texto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            bgColor.copy(alpha = 0.65f),
                            bgColor.copy(alpha = 0.95f),
                            bgColor
                        )
                    )
                )
        )

        // Título y detalles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 85.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.genre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!game.isReleased) {
                        Surface(
                            color = UnreleasedBadgeColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Próximamente",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = SteamAccentBlue,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${game.price} €",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun GameCardPreview() {
    GameCard(
        game = Game(
            id = "1",
            title = "GTA VI",
            description = "Acción en mundo abierto",
            imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
            genre = "Acción • Mundo Abierto",
            price = 79.99,
            releaseDate = "2026",
            isReleased = false
        )
    )
}