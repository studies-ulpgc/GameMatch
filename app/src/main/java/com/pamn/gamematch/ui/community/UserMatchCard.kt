package com.pamn.gamematch.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.utils.GenreUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserMatchCard(
    user: UserProfile,
    isFollowing: Boolean,
    isActionInProgress: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username.take(1).uppercase().ifBlank { "U" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.username.ifBlank { stringResource(R.string.community_user_default_name) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = stringResource(R.string.community_user_match_percentage, user.matchPercentage),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.community_user_language, user.language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FollowActionButton(
                        isFollowing = isFollowing,
                        isActionInProgress = isActionInProgress,
                        onToggleFollow = onToggleFollow
                    )
                }

                if (user.commonGenres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        user.commonGenres.take(3).forEach { genre ->
                            AssistChip(
                                onClick = {},
                                label = { Text(GenreUtils.getLocalizedGenre(genre), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowActionButton(
    isFollowing: Boolean,
    isActionInProgress: Boolean,
    onToggleFollow: () -> Unit
) {
    val padding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
    if (isFollowing) {
        OutlinedButton(
            onClick = onToggleFollow,
            enabled = !isActionInProgress,
            contentPadding = padding,
            modifier = Modifier.height(32.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.community_user_following), style = MaterialTheme.typography.labelSmall)
        }
    } else {
        Button(
            onClick = onToggleFollow,
            enabled = !isActionInProgress,
            contentPadding = padding,
            modifier = Modifier.height(32.dp)
        ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.community_user_follow), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
internal fun UserDetailModal(
    user: UserProfile,
    isFollowing: Boolean,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onToggleFollow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = user.username.ifBlank { stringResource(R.string.community_user_default_name) },
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.community_user_affinity, user.matchPercentage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.community_user_language, user.language))
                if (user.birthYear.isNotBlank()) {
                    Text(text = stringResource(R.string.community_user_birthyear, user.birthYear))
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (user.preferredGenres.isNotEmpty()) {
                    Text(text = stringResource(R.string.community_user_preferred_genres), fontWeight = FontWeight.Bold)
                    Text(text = user.preferredGenres.map { GenreUtils.getLocalizedGenre(it) }.joinToString(", "))
                }
            }
        },
        confirmButton = {
            Button(onClick = onToggleFollow, enabled = !isActionInProgress) {
                Text(stringResource(if (isFollowing) R.string.community_user_unfollowed else R.string.community_user_follow_user))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.community_close))
            }
        }
    )
}
