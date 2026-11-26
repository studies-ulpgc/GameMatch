package com.pamn.gamematch.ui.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
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
    fun openSteamStore(steamAppId: Int) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://store.steampowered.com/app/$steamAppId/"))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, steamOpenErrorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(game.id) {
        isEditingOwnReview = false
        userRatingInput = 5
        userCommentInput = ""
        viewModel.loadReviewsForGame(game.id)
        viewModel.loadSteamPrice(game.steamAppId, game.price)
    }

    LaunchedEffect(uiState.userReview) {
        userRatingInput = uiState.userReview?.rating ?: 5
        userCommentInput = uiState.userReview?.comment ?: ""
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

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
                modifier = Modifier.fillMaxWidth().height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    DetailHeaderSection(game = game, uiState = uiState, onOpenSteamStore = ::openSteamStore)
                }
                item {
                    UserReviewSection(
                        userReview = uiState.userReview,
                        isEditingOwnReview = isEditingOwnReview,
                        isSaving = uiState.isSaving,
                        userRatingInput = userRatingInput,
                        userCommentInput = userCommentInput,
                        onRatingChange = { userRatingInput = it },
                        onCommentChange = { userCommentInput = it },
                        onStartEdit = { isEditingOwnReview = true },
                        onCancelEdit = {
                            isEditingOwnReview = false
                            userRatingInput = uiState.userReview?.rating ?: 5
                            userCommentInput = uiState.userReview?.comment ?: ""
                        },
                        onDeleteReview = viewModel::deleteReview,
                        onSubmitReview = { defaultUserName ->
                            val myReview = uiState.userReview
                            if (myReview != null) {
                                viewModel.updateReview(myReview.id, userRatingInput, userCommentInput)
                            } else {
                                viewModel.submitReview(game.id, game.title, userRatingInput, userCommentInput, defaultUserName)
                            }
                        }
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.detail_community_reviews),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                when {
                    uiState.isLoading -> item {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.reviews.isEmpty() -> item {
                        Text(
                            text = stringResource(R.string.detail_no_other_reviews),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> items(uiState.reviews, key = { it.id }) { review ->
                        GameReviewItem(review = review)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.detail_close))
            }
        }
    )
}
