package com.pamn.gamematch.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.ui.components.SearchBarField

@Composable
internal fun MyRoomsTabContent(
    uiState: CommunityUiState,
    currentUid: String,
    onViewMembers: (GameRoom) -> Unit,
    onEditRoom: (GameRoom) -> Unit,
    onDeleteRoom: (GameRoom) -> Unit,
    onCreateRoomClick: () -> Unit,
    onRetry: () -> Unit
) {
    val myHostedRooms = remember(uiState.rooms, currentUid) {
        uiState.rooms.filter { it.hostId == currentUid }
    }

    when {
        uiState.isLoadingRooms -> CenteredLoadingBox()
        myHostedRooms.isEmpty() -> CenteredEmptyBox(
            message = stringResource(R.string.community_my_rooms_empty),
            buttonLabel = stringResource(R.string.community_create_room_btn),
            onAction = onCreateRoomClick,
            secondaryLabel = stringResource(R.string.community_reload),
            onSecondaryAction = onRetry
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myHostedRooms, key = { it.id }) { room ->
                MyRoomHostCard(
                    room = room,
                    isActionInProgress = uiState.isActionInProgress,
                    onViewMembers = { onViewMembers(room) },
                    onEditRoom = { onEditRoom(room) },
                    onDeleteRoom = { onDeleteRoom(room) }
                )
            }
        }
    }
}

@Composable
internal fun UserMatchTabContent(
    uiState: CommunityUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (UserFilter) -> Unit,
    onUserClick: (UserProfile) -> Unit,
    onToggleFollow: (String) -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.userFilter, uiState.searchUserQuery) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBarField(
            query = uiState.searchUserQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = stringResource(R.string.community_search_users_placeholder),
            searchContentDescription = stringResource(R.string.community_search_users_cd),
            clearContentDescription = stringResource(R.string.community_clear_search_cd),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CommunityFilterChip(
                    selected = uiState.userFilter == UserFilter.ALL,
                    label = stringResource(R.string.community_user_filter_all),
                    onClick = { onFilterChange(UserFilter.ALL) }
                )
            }
            item {
                CommunityFilterChip(
                    selected = uiState.userFilter == UserFilter.FOLLOWING_ONLY,
                    label = stringResource(R.string.community_user_filter_following),
                    onClick = { onFilterChange(UserFilter.FOLLOWING_ONLY) }
                )
            }
        }

        when {
            uiState.isLoadingUsers -> CenteredLoadingBox()
            uiState.filteredUserMatches.isEmpty() -> CenteredEmptyBox(
                message = when {
                    uiState.userFilter == UserFilter.FOLLOWING_ONLY -> stringResource(R.string.community_empty_users_following)
                    uiState.searchUserQuery.isNotBlank() -> stringResource(R.string.community_empty_users_search, uiState.searchUserQuery)
                    else -> stringResource(R.string.community_empty_users)
                },
                buttonLabel = stringResource(R.string.community_retry),
                onAction = onRetry
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredUserMatches, key = { it.uid }) { user ->
                    UserMatchCard(
                        user = user,
                        isFollowing = uiState.followingUserIds.contains(user.uid),
                        isActionInProgress = uiState.isActionInProgress,
                        onClick = { onUserClick(user) },
                        onToggleFollow = { onToggleFollow(user.uid) }
                    )
                }
            }
        }
    }
}
