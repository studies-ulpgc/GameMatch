package com.pamn.gamematch.ui.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.pamn.gamematch.ui.AppViewModelProvider

@Composable
fun GameDetailModal(
    game: Game,
    onDismiss: () -> Unit,
    onNavigateToFullDetails: (String) -> Unit = {},
    viewModel: GameDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isEditingOwnReview by remember { mutableStateOf(false) }
    var userRatingInput by remember { mutableIntStateOf(5) }
    var userCommentInput by remember { mutableStateOf("") }

    val steamOpenErrorMsg = stringResource(R.string.detail_steam_open_error)

    // Función auxiliar para abrir la tienda de Steam
    fun openSteamStore(steamAppId: Int) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://store.steampowered.com/app/$steamAppId/")
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, steamOpenErrorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // Al abrir el diálogo para un juego, reiniciar estado del formulario y cargar reseñas y precio en Steam
    LaunchedEffect(game.id) {
        isEditingOwnReview = false
        userRatingInput = 5
        userCommentInput = ""
        viewModel.loadReviewsForGame(game.id)
        viewModel.loadSteamPrice(game.steamAppId, game.price)
    }

    // Precargar datos del usuario solo si ya existe una reseña registrada para este juego
    LaunchedEffect(uiState.userReview) {
        val myRev = uiState.userReview
        if (myRev != null) {
            userRatingInput = myRev.rating
            userCommentInput = myRev.comment
        } else {
            userRatingInput = 5
            userCommentInput = ""
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Al publicarse o actualizarse con éxito, limpiar formulario y cerrar modo edición
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { success ->
            Toast.makeText(context, success.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            isEditingOwnReview = false
            if (uiState.userReview == null) {
                userRatingInput = 5
                userCommentInput = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Imagen de Portada y Datos Generales
                item {
                    if (game.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = game.imageUrl,
                            contentDescription = game.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.8f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_genre, com.pamn.gamematch.utils.GenreUtils.getLocalizedGenre(game.genre)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SteamAccentBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (uiState.isPriceLoading) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else {
                                    val priceText = com.pamn.gamematch.ui.components.priceLabel(uiState.price)

                                    Text(
                                        text = priceText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (game.steamAppId != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    onClick = { openSteamStore(game.steamAppId) }
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_steam_badge),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Promedio de Puntuación
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.detail_rating_cd),
                            tint = GameMatchTheme.semanticColors.starYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (uiState.reviews.isNotEmpty())
                                "%.1f / 5 (%d)".format(uiState.averageRating, uiState.reviews.size)
                            else stringResource(R.string.detail_no_reviews),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (game.steamAppId != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { openSteamStore(game.steamAppId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_steam_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                }

                // Sección de Mi Reseña (CRUD)
                item {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_your_review),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val currentUserReview = uiState.userReview
                    if (currentUserReview != null && !isEditingOwnReview) {
                        // Reseña Existente del Usuario
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        repeat(5) { index ->
                                            val isFilled = index < currentUserReview.rating
                                            Icon(
                                                imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = null,
                                                tint = if (isFilled) GameMatchTheme.semanticColors.starYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { isEditingOwnReview = true },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_edit_review),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteReview(currentUserReview.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_delete_review),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentUserReview.comment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        // Formulario de Crear / Editar Reseña
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(5) { index ->
                                    val starIndex = index + 1
                                    val isFilled = starIndex <= userRatingInput
                                    IconButton(
                                        onClick = { userRatingInput = starIndex },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                                            contentDescription = stringResource(R.string.detail_star_cd, starIndex),
                                            tint = if (isFilled) GameMatchTheme.semanticColors.starYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = userCommentInput,
                                onValueChange = { userCommentInput = it },
                                label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_review_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isEditingOwnReview) {
                                    TextButton(onClick = {
                                        isEditingOwnReview = false
                                        userRatingInput = uiState.userReview?.rating ?: 5
                                        userCommentInput = uiState.userReview?.comment ?: ""
                                    }) {
                                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_cancel))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                val defaultUserName = stringResource(R.string.detail_default_username)
                                Button(
                                    onClick = {
                                        val myReview = uiState.userReview
                                        if (myReview != null) {
                                            viewModel.updateReview(myReview.id, userRatingInput, userCommentInput)
                                        } else {
                                            viewModel.submitReview(game.id, game.title, userRatingInput, userCommentInput, defaultUserName)
                                        }
                                    },
                                    enabled = !uiState.isSaving && userCommentInput.isNotBlank()
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text(if (uiState.userReview != null) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_save_changes) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_publish_review))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                }

                // Lista de Reseñas de Otros Usuarios
                item {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_community_reviews),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.reviews.isEmpty()) {
                    item {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_no_other_reviews),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.reviews, key = { it.id }) { review ->
                        GameReviewItem(review = review)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_close))
            }
        }
    )
}

@Composable
private fun GameReviewItem(review: GameReview) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = remember(review.timestamp) {
        dateFormat.format(Date(review.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row {
                repeat(5) { index ->
                    val isFilled = index < review.rating
                    Icon(
                        imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (isFilled) GameMatchTheme.semanticColors.starYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}