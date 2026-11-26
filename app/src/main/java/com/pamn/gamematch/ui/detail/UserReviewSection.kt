package com.pamn.gamematch.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.ui.components.StarRatingDisplay
import com.pamn.gamematch.ui.components.StarRatingSelector

@Composable
internal fun UserReviewSection(
    userReview: GameReview?,
    isEditingOwnReview: Boolean,
    isSaving: Boolean,
    userRatingInput: Int,
    userCommentInput: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteReview: (String) -> Unit,
    onSubmitReview: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.detail_your_review),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))

    if (userReview != null && !isEditingOwnReview) {
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
                    StarRatingDisplay(rating = userReview.rating, starSize = 16.dp)
                    Row {
                        IconButton(onClick = onStartEdit, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.detail_edit_review),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDeleteReview(userReview.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.detail_delete_review),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = userReview.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            StarRatingSelector(
                rating = userRatingInput,
                onRatingSelected = onRatingChange,
                starContentDescription = { starIndex -> stringResource(R.string.detail_star_cd, starIndex) }
            )
            OutlinedTextField(
                value = userCommentInput,
                onValueChange = onCommentChange,
                label = { Text(stringResource(R.string.detail_review_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isEditingOwnReview) {
                    TextButton(onClick = onCancelEdit) {
                        Text(stringResource(R.string.detail_cancel))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                val defaultUserName = stringResource(R.string.detail_default_username)
                Button(
                    onClick = { onSubmitReview(defaultUserName) },
                    enabled = !isSaving && userCommentInput.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(if (userReview != null) R.string.detail_save_changes else R.string.detail_publish_review))
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider()
}
