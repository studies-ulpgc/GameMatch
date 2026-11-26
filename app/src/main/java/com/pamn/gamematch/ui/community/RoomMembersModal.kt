package com.pamn.gamematch.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile

@Composable
internal fun RoomMembersModal(
    room: GameRoom,
    viewModel: CommunityViewModel,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onRemoveMember: (String) -> Unit
) {
    var membersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoadingMembers by remember { mutableStateOf(true) }

    LaunchedEffect(room.members) {
        isLoadingMembers = true
        viewModel.loadMembersProfiles(room.members) { profiles ->
            membersList = profiles
            isLoadingMembers = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.community_room_members_title, room.gameTitle),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            when {
                isLoadingMembers -> Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                membersList.isEmpty() -> Text(stringResource(R.string.community_room_members_empty))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(membersList, key = { it.uid }) { member ->
                        MemberRow(
                            member = member,
                            isHostMember = member.uid == room.hostId,
                            isActionInProgress = isActionInProgress,
                            onRemove = { onRemoveMember(member.uid) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.community_close))
            }
        }
    )
}

@Composable
private fun MemberRow(
    member: UserProfile,
    isHostMember: Boolean,
    isActionInProgress: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = member.username.take(1).uppercase().ifBlank { "U" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isHostMember) "${member.username} (${stringResource(R.string.community_host_you)})" else member.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHostMember) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.language,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isHostMember) {
            IconButton(onClick = onRemove, enabled = !isActionInProgress) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = stringResource(R.string.community_kick_member),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
