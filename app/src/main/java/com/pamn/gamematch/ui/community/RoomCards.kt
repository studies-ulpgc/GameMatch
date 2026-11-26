package com.pamn.gamematch.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom

@Composable
internal fun MyRoomHostCard(
    room: GameRoom,
    isActionInProgress: Boolean,
    onViewMembers: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            RoomCardHeader(room)
            Spacer(modifier = Modifier.height(4.dp))
            RoomSubtitleAndBadges(
                subtitle = stringResource(R.string.community_host_you),
                subtitleColor = MaterialTheme.colorScheme.secondary,
                isFollowersOnly = room.isFollowersOnly,
                platform = room.platform
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = room.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            RoomHostActionsRow(
                isActionInProgress = isActionInProgress,
                onEditRoom = onEditRoom,
                onDeleteRoom = onDeleteRoom
            ) {
                OutlinedButton(onClick = onViewMembers, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = stringResource(R.string.community_room_view_members),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.community_room_view_members_count, room.members.size),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
internal fun RoomCard(
    room: GameRoom,
    isHost: Boolean,
    isMember: Boolean,
    isFull: Boolean,
    isActionInProgress: Boolean,
    onJoinRoom: () -> Unit,
    onLeaveRoom: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onContactCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            RoomCardHeader(room)
            Spacer(modifier = Modifier.height(4.dp))
            RoomSubtitleAndBadges(
                subtitle = stringResource(R.string.community_room_host, room.hostName),
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                isFollowersOnly = room.isFollowersOnly,
                platform = room.platform
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = room.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isHost -> RoomHostActionsRow(isActionInProgress, onEditRoom, onDeleteRoom) {
                    OutlinedButton(onClick = onContactCopy, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.community_contact_copy_cd),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.community_contact_btn), style = MaterialTheme.typography.labelMedium)
                    }
                }
                isMember -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onContactCopy, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.community_contact_copy_cd),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.community_contact_btn))
                    }
                    Button(
                        onClick = onLeaveRoom,
                        enabled = !isActionInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.community_room_leave))
                    }
                }
                else -> Button(
                    onClick = onJoinRoom,
                    enabled = !isFull && !isActionInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isFull) stringResource(R.string.community_room_full) else stringResource(R.string.community_room_join))
                }
            }
        }
    }
}
