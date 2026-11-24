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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.UnreleasedBadgeColor
import com.pamn.gamematch.utils.GenreUtils
import kotlin.math.roundToInt

@Composable
fun GameCard(
    game: Game,
    overlayAlpha: Float = 0f,
    compact: Boolean = false,
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

    val priceBadgeText = com.pamn.gamematch.ui.components.priceLabel(game.price)

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
                .fillMaxHeight(if (compact) 0.7f else 0.55f)
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
                .padding(
                    horizontal = if (compact) 12.dp else 20.dp,
                    vertical = if (compact) 12.dp else 85.dp
                )
        ) {
            Text(
                text = game.title,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = GenreUtils.getLocalizedGenre(game.genre),
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!game.isReleased) {
                    Surface(
                        color = UnreleasedBadgeColor,
                        shape = RoundedCornerShape(if (compact) 6.dp else 8.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.game_status_upcoming),
                            modifier = Modifier.padding(
                                horizontal = if (compact) 6.dp else 8.dp,
                                vertical = if (compact) 2.dp else 4.dp
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = SteamAccentBlue,
                    shape = RoundedCornerShape(if (compact) 6.dp else 8.dp)
                ) {
                    Text(
                        text = priceBadgeText,
                        modifier = Modifier.padding(
                            horizontal = if (compact) 6.dp else 8.dp,
                            vertical = if (compact) 2.dp else 4.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
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
