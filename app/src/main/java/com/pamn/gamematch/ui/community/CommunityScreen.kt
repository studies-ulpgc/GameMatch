package com.pamn.gamematch.ui.community

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.ui.discover.PLATFORM_OPTIONS
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import java.util.Locale

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel()
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
        viewModel.loadLikedGames()
        viewModel.loadFollowingUserIds()
        viewModel.loadRooms()
        viewModel.loadCommunityUsers()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { success ->
            Toast.makeText(context, success, Toast.LENGTH_SHORT).show()
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
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_fab_create)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_title),
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
                    text = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_rooms)) },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_rooms_icon)) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_my_rooms)) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_my_rooms)) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_players)) },
                    icon = { Icon(Icons.Default.People, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_tab_players_icon)) }
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
                    onEditRoom = { room -> selectedRoomForEdit = room },
                    onDeleteRoom = { room -> selectedRoomForDelete = room },
                    onContactCopy = { contactInfo ->
                        clipboardManager.setText(AnnotatedString(contactInfo))
                        val msg = context.resources.getString(com.pamn.gamematch.R.string.community_contact_copied, contactInfo)
                        Toast.makeText(
                            context,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onRetry = viewModel::loadRooms
                )

                1 -> MyRoomsTabContent(
                    uiState = uiState,
                    currentUid = currentUid,
                    onViewMembers = { room -> selectedRoomForMembers = room },
                    onEditRoom = { room -> selectedRoomForEdit = room },
                    onDeleteRoom = { room -> selectedRoomForDelete = room },
                    onCreateRoomClick = { showCreateRoomModal = true },
                    onRetry = viewModel::loadRooms
                )

                2 -> UserMatchTabContent(
                    uiState = uiState,
                    onUserClick = { user -> selectedUserForModal = user },
                    onToggleFollow = viewModel::toggleFollowUser,
                    onRetry = viewModel::loadCommunityUsers
                )
            }
        }
    }

    // Modal de Detalle de Usuario
    selectedUserForModal?.let { user ->
        val isFollowing = uiState.followingUserIds.contains(user.uid)
        UserDetailModal(
            user = user,
            isFollowing = isFollowing,
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { selectedUserForModal = null },
            onToggleFollow = {
                viewModel.toggleFollowUser(user.uid)
            }
        )
    }

    // Modal de Gestión de Miembros para el Anfitrión
    selectedRoomForMembers?.let { room ->
        RoomMembersModal(
            room = room,
            viewModel = viewModel,
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { selectedRoomForMembers = null },
            onRemoveMember = { memberUid ->
                viewModel.removeMemberFromRoom(room.id, memberUid)
            }
        )
    }

    // Modal de Editar Sala
    selectedRoomForEdit?.let { room ->
        EditRoomModal(
            room = room,
            catalogGames = uiState.catalogGames,
            isSaving = uiState.isActionInProgress,
            onDismiss = { selectedRoomForEdit = null },
            onUpdate = { gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly ->
                viewModel.updateRoom(
                    roomId = room.id,
                    gameTitle = gameTitle,
                    description = description,
                    platform = platform,
                    contactInfo = contactInfo,
                    maxPlayers = maxPlayers,
                    isFollowersOnly = isFollowersOnly
                ) { success ->
                    if (success) {
                        selectedRoomForEdit = null
                    }
                }
            }
        )
    }

    // Diálogo de Confirmación para Eliminar Sala
    selectedRoomForDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { selectedRoomForDelete = null },
            title = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_delete_room_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_delete_room_confirm, room.gameTitle)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(room.id)
                        selectedRoomForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRoomForDelete = null }) {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_cancel))
                }
            }
        )
    }

    // Modal de Crear Sala
    if (showCreateRoomModal) {
        CreateRoomModal(
            catalogGames = uiState.catalogGames,
            isPublishing = uiState.isCreatingRoom,
            onDismiss = { showCreateRoomModal = false },
            onPublish = { gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly ->
                viewModel.createRoom(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly) { success ->
                    if (success) {
                        showCreateRoomModal = false
                    }
                }
            }
        )
    }
}

@Composable
private fun MyRoomsTabContent(
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

    if (uiState.isLoadingRooms) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (myHostedRooms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_my_rooms_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onCreateRoomClick,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_create_room_btn))
                }
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_reload))
                }
            }
        }
    } else {
        LazyColumn(
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
private fun MyRoomHostCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.gameTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Counter de jugadores
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "👥 ${room.currentPlayers}/${room.maxPlayers}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    text = "Anfitrión (Tú)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (room.isFollowersOnly) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_followers_only),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Plataforma Badge reutilizada
                    Surface(
                        color = SteamAccentBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = room.platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = room.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewMembers,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Ver Miembros",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_view_members_count, room.members.size), style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onEditRoom,
                    enabled = !isActionInProgress
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteRoom,
                    enabled = !isActionInProgress
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomMembersModal(
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
                text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_members_title, room.gameTitle),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            if (isLoadingMembers) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (membersList.isEmpty()) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_members_empty))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(membersList, key = { it.uid }) { member ->
                        val isHostMember = member.uid == room.hostId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = member.username.take(1).uppercase(Locale.getDefault()).ifBlank { "U" },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isHostMember) "${member.username} (${androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_host_you)})" else member.username,
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
                                IconButton(
                                    onClick = { onRemoveMember(member.uid) },
                                    enabled = !isActionInProgress
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonRemove,
                                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_kick_member),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_close))
            }
        }
    )
}

@Composable
private fun RoomsTabContent(
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
        // Buscador superior por juego
        OutlinedTextField(
            value = uiState.searchGameQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_search_cd)
                )
            },
            trailingIcon = {
                if (uiState.searchGameQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_clear_search_cd)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Chips de Filtro
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.roomFilter == RoomFilter.ALL,
                    onClick = { onFilterChange(RoomFilter.ALL) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.roomFilter == RoomFilter.FOLLOWING_ONLY,
                    onClick = { onFilterChange(RoomFilter.FOLLOWING_ONLY) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_filter_following)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.roomFilter == RoomFilter.FAVORITES_ONLY,
                    onClick = { onFilterChange(RoomFilter.FAVORITES_ONLY) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_filter_favorites)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (uiState.isLoadingRooms) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (uiState.roomFilter) {
                            RoomFilter.FOLLOWING_ONLY -> androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_empty_following)
                            RoomFilter.FAVORITES_ONLY -> androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_empty_favorites)
                            RoomFilter.ALL -> if (uiState.searchGameQuery.isNotBlank()) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_empty_search, uiState.searchGameQuery) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_empty_all)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_retry))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredRooms, key = { it.id }) { room ->
                    val isHost = currentUid.isNotBlank() && room.hostId == currentUid
                    val isMember = currentUid.isNotBlank() && (room.members.contains(currentUid) || isHost)
                    val isFull = room.currentPlayers >= room.maxPlayers

                    RoomCard(
                        room = room,
                        isHost = isHost,
                        isMember = isMember,
                        isFull = isFull,
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
private fun RoomCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.gameTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Counter de jugadores
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "👥 ${room.currentPlayers}/${room.maxPlayers}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_host, room.hostName),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (room.isFollowersOnly) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_followers_only),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Plataforma Badge reutilizada
                    Surface(
                        color = SteamAccentBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = room.platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = room.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gestión Real de Plazas y Edición de Anfitrión
            if (isHost) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onContactCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_copy_cd),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_btn), style = MaterialTheme.typography.labelMedium)
                    }

                    IconButton(
                        onClick = onEditRoom,
                        enabled = !isActionInProgress
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDeleteRoom,
                        enabled = !isActionInProgress
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else if (isMember) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onContactCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_copy_cd),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_btn))
                    }

                    Button(
                        onClick = onLeaveRoom,
                        enabled = !isActionInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_leave))
                    }
                }
            } else {
                Button(
                    onClick = onJoinRoom,
                    enabled = !isFull && !isActionInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isFull) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_full) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_room_join))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserMatchTabContent(
    uiState: CommunityUiState,
    onUserClick: (UserProfile) -> Unit,
    onToggleFollow: (String) -> Unit,
    onRetry: () -> Unit
) {
    if (uiState.isLoadingUsers) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.userMatches.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_empty_users),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_retry))
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.userMatches, key = { it.uid }) { user ->
                val isFollowing = uiState.followingUserIds.contains(user.uid)
                UserMatchCard(
                    user = user,
                    isFollowing = isFollowing,
                    isActionInProgress = uiState.isActionInProgress,
                    onClick = { onUserClick(user) },
                    onToggleFollow = { onToggleFollow(user.uid) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserMatchCard(
    user: UserProfile,
    isFollowing: Boolean,
    isActionInProgress: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username.take(1).uppercase(Locale.getDefault()).ifBlank { "U" },
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
                        text = user.username.ifBlank { androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_default_name) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge de Match %
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${user.matchPercentage}% Match",
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
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_language, user.language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón de Seguimiento Persistente Real
                    if (isFollowing) {
                        OutlinedButton(
                            onClick = onToggleFollow,
                            enabled = !isActionInProgress,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_following), style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Button(
                            onClick = onToggleFollow,
                            enabled = !isActionInProgress,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_follow), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (user.commonGenres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        user.commonGenres.take(3).forEach { genre ->
                            AssistChip(
                                onClick = {},
                                label = { Text(genre, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserDetailModal(
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
                text = user.username.ifBlank { androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_default_name) },
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_affinity, user.matchPercentage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_language, user.language))
                if (user.birthYear.isNotBlank()) {
                    Text(text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_birthyear, user.birthYear))
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (user.preferredGenres.isNotEmpty()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_preferred_genres),
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = user.preferredGenres.joinToString(", "))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onToggleFollow,
                enabled = !isActionInProgress
            ) {
                Text(if (isFollowing) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_unfollowed) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_user_follow_user))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_close))
            }
        }
    )
}

@Composable
private fun CreateRoomModal(
    catalogGames: List<Game>,
    isPublishing: Boolean,
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, Int, Boolean) -> Unit
) {
    var gameTitle by remember { mutableStateOf(catalogGames.firstOrNull()?.title ?: "") }
    var expandedGameDropdown by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf("PC") }
    var expandedPlatformDropdown by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableIntStateOf(4) }
    var isFollowersOnly by remember { mutableStateOf(false) }

    val platformOptions = remember { PLATFORM_OPTIONS.filter { it != "Todas" && it != "All" } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_create_room_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Selector o Campo de Juego
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_game_title_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gameTitle,
                        onValueChange = { gameTitle = it },
                        trailingIcon = {
                            IconButton(onClick = { expandedGameDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_expand_games_cd))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_game_title_placeholder)) }
                    )

                    DropdownMenu(
                        expanded = expandedGameDropdown,
                        onDismissRequest = { expandedGameDropdown = false }
                    ) {
                        catalogGames.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game.title) },
                                onClick = {
                                    gameTitle = game.title
                                    expandedGameDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de Plataforma
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_platform_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedPlatformDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_expand_platforms_cd))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedPlatformDropdown = true }
                    )

                    DropdownMenu(
                        expanded = expandedPlatformDropdown,
                        onDismissRequest = { expandedPlatformDropdown = false }
                    ) {
                        platformOptions.forEach { plat ->
                            DropdownMenuItem(
                                text = { Text(plat) },
                                onClick = {
                                    platform = plat
                                    expandedPlatformDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_desc_label)) },
                    placeholder = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_desc_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Información de contacto
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_label)) },
                    placeholder = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Switch de Solo Seguidos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_followers_only_switch),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Switch(
                        checked = isFollowersOnly,
                        onCheckedChange = { isFollowersOnly = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Máximo de Jugadores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_max_players_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (maxPlayers > 2) maxPlayers-- },
                            enabled = maxPlayers > 2
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = maxPlayers.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { if (maxPlayers < 10) maxPlayers++ },
                            enabled = maxPlayers < 10
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPublish(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly)
                },
                enabled = !isPublishing && gameTitle.isNotBlank() && description.isNotBlank() && contactInfo.isNotBlank()
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_publish_room_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPublishing) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_cancel))
            }
        }
    )
}

@Composable
private fun EditRoomModal(
    room: GameRoom,
    catalogGames: List<Game>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, Int, Boolean) -> Unit
) {
    var gameTitle by remember { mutableStateOf(room.gameTitle) }
    var expandedGameDropdown by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf(room.platform) }
    var expandedPlatformDropdown by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(room.description) }
    var contactInfo by remember { mutableStateOf(room.contactInfo) }
    var maxPlayers by remember { mutableIntStateOf(room.maxPlayers) }
    var isFollowersOnly by remember { mutableStateOf(room.isFollowersOnly) }

    val platformOptions = remember { PLATFORM_OPTIONS.filter { it != "Todas" && it != "All" } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_edit_room_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Selector o Campo de Juego
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_game_title_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gameTitle,
                        onValueChange = { gameTitle = it },
                        trailingIcon = {
                            IconButton(onClick = { expandedGameDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_expand_games_cd))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expandedGameDropdown,
                        onDismissRequest = { expandedGameDropdown = false }
                    ) {
                        catalogGames.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game.title) },
                                onClick = {
                                    gameTitle = game.title
                                    expandedGameDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de Plataforma
                Text(
                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_platform_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedPlatformDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_expand_platforms_cd))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedPlatformDropdown = true }
                    )

                    DropdownMenu(
                        expanded = expandedPlatformDropdown,
                        onDismissRequest = { expandedPlatformDropdown = false }
                    ) {
                        platformOptions.forEach { plat ->
                            DropdownMenuItem(
                                text = { Text(plat) },
                                onClick = {
                                    platform = plat
                                    expandedPlatformDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_desc_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Información de contacto
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_contact_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Switch de Solo Seguidos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_followers_only_switch),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Switch(
                        checked = isFollowersOnly,
                        onCheckedChange = { isFollowersOnly = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Máximo de Jugadores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_max_players_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (maxPlayers > room.currentPlayers) maxPlayers-- },
                            enabled = maxPlayers > room.currentPlayers
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = maxPlayers.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { if (maxPlayers < 10) maxPlayers++ },
                            enabled = maxPlayers < 10
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly)
                },
                enabled = !isSaving && gameTitle.isNotBlank() && description.isNotBlank() && contactInfo.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_save_changes_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.community_cancel))
            }
        }
    )
}