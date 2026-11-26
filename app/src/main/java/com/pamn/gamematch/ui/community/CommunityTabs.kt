package com.pamn.gamematch.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.ui.components.SearchBarField

@Composable
internal fun RoomsTabContent(
    uiState: CommunityUiState,
    currentUid: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (RoomFilter) -> Unit,
    onJoinRoom: (String) -> Unit,
    onLeaveRoom: (String) -> Unit,
    onEditRoom: (GameRoom) -> Unit,
    onDeleteRoom: (GameRoom) -> Unit,
    onContactCopy: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBarField(
            query = uiState.searchGameQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = stringResource(R.string.community_search_placeholder),
            searchContentDescription = stringResource(R.string.community_search_cd),
            clearContentDescription = stringResource(R.string.community_clear_search_cd),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CommunityFilterChip(
                    selected = uiState.roomFilter == RoomFilter.ALL,
                    label = stringResource(R.string.community_filter_all),
                    onClick = { onFilterChange(RoomFilter.ALL) }
                )
            }
            item {
                CommunityFilterChip(
                    selected = uiState.roomFilter == RoomFilter.FOLLOWING_ONLY,
                    label = stringResource(R.string.community_filter_following),
                    onClick = { onFilterChange(RoomFilter.FOLLOWING_ONLY) }
                )
            }
            item {
                CommunityFilterChip(
                    selected = uiState.roomFilter == RoomFilter.FAVORITES_ONLY,
                    label = stringResource(R.string.community_filter_favorites),
                    onClick = { onFilterChange(RoomFilter.FAVORITES_ONLY) }
                )
            }
        }

        when {
            uiState.isLoadingRooms -> CenteredLoadingBox()
            uiState.filteredRooms.isEmpty() -> CenteredEmptyBox(
                message = when (uiState.roomFilter) {
                    RoomFilter.FOLLOWING_ONLY -> stringResource(R.string.community_empty_following)
                    RoomFilter.FAVORITES_ONLY -> stringResource(R.string.community_empty_favorites)
                    RoomFilter.ALL -> if (uiState.searchGameQuery.isNotBlank()) {
                        stringResource(R.string.community_empty_search, uiState.searchGameQuery)
                    } else {
                        stringResource(R.string.community_empty_all)
                    }
                },
                buttonLabel = stringResource(R.string.community_retry),
                onAction = onRetry
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredRooms, key = { it.id }) { room ->
                    val isHost = currentUid.isNotBlank() && room.hostId == currentUid
                    val isMember = currentUid.isNotBlank() && (room.members.contains(currentUid) || isHost)
                    RoomCard(
                        room = room,
                        isHost = isHost,
                        isMember = isMember,
                        isFull = room.currentPlayers >= room.maxPlayers,
                        isActionInProgress = uiState.isActionInProgress,
                        onJoinRoom = { onJoinRoom(room.id) },
                        onLeaveRoom = { onLeaveRoom(room.id) },
                        onEditRoom = { onEditRoom(room) },
                        onDeleteRoom = { onDeleteRoom(room) },
                        onContactCopy = { onContactCopy(room.contactInfo) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun CommunityFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
internal fun CenteredLoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun CenteredEmptyBox(
    message: String,
    buttonLabel: String,
    onAction: () -> Unit,
    secondaryLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Button(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) {
                Text(buttonLabel)
            }
            if (secondaryLabel != null && onSecondaryAction != null) {
                TextButton(onClick = onSecondaryAction, modifier = Modifier.padding(top = 4.dp)) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}
