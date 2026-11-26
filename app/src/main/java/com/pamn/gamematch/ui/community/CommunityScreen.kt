package com.pamn.gamematch.ui.community

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.ui.AppViewModelProvider

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCreateRoomModal by remember { mutableStateOf(false) }
    var selectedUserForModal by remember { mutableStateOf<UserProfile?>(null) }
    var selectedRoomForEdit by remember { mutableStateOf<GameRoom?>(null) }
    var selectedRoomForDelete by remember { mutableStateOf<GameRoom?>(null) }
    var selectedRoomForMembers by remember { mutableStateOf<GameRoom?>(null) }

    val currentUid = remember(uiState.currentUserProfile) {
        uiState.currentUserProfile?.uid ?: ""
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
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
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (uiState.selectedTab == 0 || uiState.selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showCreateRoomModal = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.community_fab_create)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = stringResource(R.string.community_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text(stringResource(R.string.community_tab_rooms)) },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = stringResource(R.string.community_tab_rooms_icon)) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text(stringResource(R.string.community_tab_my_rooms)) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = stringResource(R.string.community_tab_my_rooms)) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text(stringResource(R.string.community_tab_players)) },
                    icon = { Icon(Icons.Default.People, contentDescription = stringResource(R.string.community_tab_players_icon)) }
                )
            }

            when (uiState.selectedTab) {
                0 -> RoomsTabContent(
                    uiState = uiState,
                    currentUid = currentUid,
                    onSearchQueryChange = viewModel::onSearchGameQueryChanged,
                    onFilterChange = viewModel::setRoomFilter,
                    onJoinRoom = viewModel::joinRoom,
                    onLeaveRoom = viewModel::leaveRoom,
                    onEditRoom = { selectedRoomForEdit = it },
                    onDeleteRoom = { selectedRoomForDelete = it },
                    onContactCopy = { contactInfo ->
                        clipboardManager.setText(AnnotatedString(contactInfo))
                        val msg = context.resources.getString(R.string.community_contact_copied, contactInfo)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onRetry = viewModel::loadRooms
                )
                1 -> MyRoomsTabContent(
                    uiState = uiState,
                    currentUid = currentUid,
                    onViewMembers = { selectedRoomForMembers = it },
                    onEditRoom = { selectedRoomForEdit = it },
                    onDeleteRoom = { selectedRoomForDelete = it },
                    onCreateRoomClick = { showCreateRoomModal = true },
                    onRetry = viewModel::loadRooms
                )
                2 -> UserMatchTabContent(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchUserQueryChanged,
                    onFilterChange = viewModel::setUserFilter,
                    onUserClick = { selectedUserForModal = it },
                    onToggleFollow = viewModel::toggleFollowUser,
                    onRetry = viewModel::loadCommunityUsers
                )
            }
        }
    }

    selectedUserForModal?.let { user ->
        UserDetailModal(
            user = user,
            isFollowing = uiState.followingUserIds.contains(user.uid),
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { selectedUserForModal = null },
            onToggleFollow = { viewModel.toggleFollowUser(user.uid) }
        )
    }

    selectedRoomForMembers?.let { room ->
        RoomMembersModal(
            room = room,
            viewModel = viewModel,
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { selectedRoomForMembers = null },
            onRemoveMember = { memberUid -> viewModel.removeMemberFromRoom(room.id, memberUid) }
        )
    }

    selectedRoomForEdit?.let { room ->
        RoomFormModal(
            catalogGames = uiState.catalogGames,
            isBusy = uiState.isActionInProgress,
            initialRoom = room,
            onDismiss = { selectedRoomForEdit = null },
            onSubmit = { gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly ->
                viewModel.updateRoom(room.id, gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly) { ok ->
                    if (ok) selectedRoomForEdit = null
                }
            }
        )
    }

    selectedRoomForDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { selectedRoomForDelete = null },
            title = { Text(stringResource(R.string.community_delete_room_title)) },
            text = { Text(stringResource(R.string.community_delete_room_confirm, room.gameTitle)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(room.id)
                        selectedRoomForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.community_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRoomForDelete = null }) {
                    Text(stringResource(R.string.community_cancel))
                }
            }
        )
    }

    if (showCreateRoomModal) {
        val defaultHostName = stringResource(R.string.community_default_host_name)
        RoomFormModal(
            catalogGames = uiState.catalogGames,
            isBusy = uiState.isCreatingRoom,
            onDismiss = { showCreateRoomModal = false },
            onSubmit = { gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly ->
                viewModel.createRoom(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly, defaultHostName) { ok ->
                    if (ok) showCreateRoomModal = false
                }
            }
        )
    }
}
