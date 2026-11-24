package com.pamn.gamematch.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoomFilter {
    ALL,
    FOLLOWING_ONLY,
    FAVORITES_ONLY
}

data class CommunityUiState(
    val selectedTab: Int = 0, // 0: Salas de Juego, 1: Mis Salas, 2: Jugadores Afines
    val userMatches: List<UserProfile> = emptyList(),
    val currentUserProfile: UserProfile? = null,
    val rooms: List<GameRoom> = emptyList(),
    val filteredRooms: List<GameRoom> = emptyList(),
    val catalogGames: List<Game> = emptyList(),
    val followingUserIds: Set<String> = emptySet(),
    val likedGameTitles: Set<String> = emptySet(),
    val searchGameQuery: String = "",
    val roomFilter: RoomFilter = RoomFilter.ALL,
    val isLoadingUsers: Boolean = false,
    val isLoadingRooms: Boolean = false,
    val isCreatingRoom: Boolean = false,
    val isActionInProgress: Boolean = false,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    fun loadInitialData() {
        loadCatalogGames()
        loadLikedGames()
        loadFollowingUserIds()
        loadRooms()
        loadCommunityUsers()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setRoomFilter(filter: RoomFilter) {
        _uiState.update { it.copy(roomFilter = filter) }
        loadFollowingUserIds()
        loadRooms()
    }

    fun onSearchGameQueryChanged(query: String) {
        _uiState.update { it.copy(searchGameQuery = query) }
        applyRoomFilters()
    }

    fun loadCatalogGames() {
        viewModelScope.launch {
            val games = gameRepository.getGamesCatalog().getOrDefault(emptyList())
            _uiState.update { it.copy(catalogGames = games) }
        }
    }

    fun loadLikedGames() {
        viewModelScope.launch {
            val catalog = gameRepository.getGamesCatalog().getOrDefault(emptyList())
            gameRepository.getLikedGames { result ->
                result.onSuccess { ids ->
                    val likedTitles = ids.mapNotNull { id ->
                        catalog.find { game -> game.id == id }?.title
                    }.toSet()
                    _uiState.update { it.copy(likedGameTitles = likedTitles) }
                    applyRoomFilters()
                }
            }
        }
    }

    fun loadFollowingUserIds() {
        viewModelScope.launch {
            communityRepository.getFollowingUserIds().onSuccess { ids ->
                _uiState.update { it.copy(followingUserIds = ids.toSet()) }
                applyRoomFilters()
            }
        }
    }

    fun loadCommunityUsers() {
        _uiState.update { it.copy(isLoadingUsers = true, errorMessage = null) }
        viewModelScope.launch {
            val result = communityRepository.getCommunityUsers()
            result
                .onSuccess { allUsers ->
                    val currentUid = communityRepository.getCurrentUserId()
                    val currentProfile = allUsers.find { it.uid == currentUid }
                    val currentGenres = currentProfile?.preferredGenres?.toSet() ?: emptySet()

                    val matches = allUsers.filter { it.uid != currentUid }.map { other ->
                        val otherGenres = other.preferredGenres.toSet()
                        val common = currentGenres.intersect(otherGenres).toList()
                        val percentage = if (currentGenres.isNotEmpty()) {
                            (common.size.toFloat() / currentGenres.size.toFloat() * 100).toInt().coerceIn(0, 100)
                        } else if (otherGenres.isNotEmpty()) {
                            50
                        } else {
                            0
                        }

                        other.copy(
                            matchPercentage = percentage,
                            commonGenres = common
                        )
                    }.sortedByDescending { it.matchPercentage }

                    _uiState.update { state ->
                        state.copy(
                            currentUserProfile = currentProfile,
                            userMatches = matches,
                            isLoadingUsers = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingUsers = false,
                            errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.community_load_users_error)
                        )
                    }
                }
        }
    }

    fun loadRooms() {
        _uiState.update { it.copy(isLoadingRooms = true, errorMessage = null) }
        viewModelScope.launch {
            val result = communityRepository.getRooms()
            result
                .onSuccess { roomsList ->
                    _uiState.update { state ->
                        state.copy(
                            rooms = roomsList,
                            isLoadingRooms = false
                        )
                    }
                    applyRoomFilters()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingRooms = false,
                            errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.community_load_rooms_error)
                        )
                    }
                }
        }
    }

    private fun applyRoomFilters() {
        val state = _uiState.value
        val currentUid = communityRepository.getCurrentUserId() ?: ""
        val following = state.followingUserIds
        val filterMode = state.roomFilter
        val searchQuery = state.searchGameQuery.trim()
        val likedTitles = state.likedGameTitles

        val filtered = state.rooms.filter { room ->
            val isOwnRoom = room.hostId == currentUid
            val isFollowingHost = room.hostId in following
            val isMember = room.members.contains(currentUid)

            // Visibilidad: La sala es visible si NO es solo seguidos, O si sigues al host, O si es tu propia sala, O si eres miembro
            val isVisible = !room.isFollowersOnly || isFollowingHost || isOwnRoom || isMember

            // Regla de filtro activo en la pestaña:
            val passesChipFilter = when (filterMode) {
                RoomFilter.ALL -> !isOwnRoom
                RoomFilter.FOLLOWING_ONLY -> isFollowingHost || room.isFollowersOnly
                RoomFilter.FAVORITES_ONLY -> !isOwnRoom && likedTitles.any { title ->
                    title.equals(room.gameTitle, ignoreCase = true) || room.gameTitle.contains(title, ignoreCase = true)
                }
            }

            // Regla de búsqueda por texto:
            val matchesSearch = searchQuery.isEmpty() ||
                    room.gameTitle.contains(searchQuery, ignoreCase = true) ||
                    room.description.contains(searchQuery, ignoreCase = true)

            isVisible && passesChipFilter && matchesSearch
        }

        _uiState.update { it.copy(filteredRooms = filtered) }
    }

    fun toggleFollowUser(targetUserId: String) {
        val isFollowing = _uiState.value.followingUserIds.contains(targetUserId)
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            val result = if (isFollowing) {
                communityRepository.unfollowUser(targetUserId)
            } else {
                communityRepository.followUser(targetUserId)
            }

            result
                .onSuccess {
                    val updatedFollowing = if (isFollowing) {
                        _uiState.value.followingUserIds - targetUserId
                    } else {
                        _uiState.value.followingUserIds + targetUserId
                    }
                    val msg = if (isFollowing) UiText.StringResource(R.string.community_user_unfollow_success) else UiText.StringResource(R.string.community_user_follow_success)
                    _uiState.update { state ->
                        state.copy(
                            followingUserIds = updatedFollowing,
                            isActionInProgress = false,
                            successMessage = msg
                        )
                    }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isActionInProgress = false,
                            errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.community_follow_error)
                        )
                    }
                }
        }
    }

    fun joinRoom(roomId: String) {
        val currentUid = communityRepository.getCurrentUserId() ?: return
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            communityRepository.joinRoom(roomId, currentUid)
                .onSuccess {
                    _uiState.update { it.copy(isActionInProgress = false, successMessage = UiText.StringResource(R.string.community_joined_room_success)) }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.community_join_room_error)) }
                }
        }
    }

    fun leaveRoom(roomId: String) {
        val currentUid = communityRepository.getCurrentUserId() ?: return
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            communityRepository.leaveRoom(roomId, currentUid)
                .onSuccess {
                    _uiState.update { it.copy(isActionInProgress = false, successMessage = UiText.StringResource(R.string.community_left_room_success)) }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.community_leave_room_error)) }
                }
        }
    }

    fun removeMemberFromRoom(roomId: String, memberUid: String) {
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            communityRepository.removeMemberFromRoom(roomId, memberUid)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            successMessage = UiText.StringResource(R.string.community_member_removed_success)
                        )
                    }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.community_remove_member_error)
                        )
                    }
                }
        }
    }

    fun loadMembersProfiles(memberUids: List<String>, onResult: (List<UserProfile>) -> Unit) {
        viewModelScope.launch {
            communityRepository.getMembersProfiles(memberUids)
                .onSuccess { profiles -> onResult(profiles) }
                .onFailure { onResult(emptyList()) }
        }
    }

    fun createRoom(
        gameTitle: String,
        description: String,
        platform: String,
        contactInfo: String,
        maxPlayers: Int,
        isFollowersOnly: Boolean,
        defaultHostName: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (gameTitle.isBlank() || description.isBlank() || contactInfo.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.community_fields_required_error)) }
            onComplete(false)
            return
        }

        val currentUser = _uiState.value.currentUserProfile
        val hostName = currentUser?.username?.ifBlank { defaultHostName } ?: defaultHostName
        val hostId = currentUser?.uid ?: communityRepository.getCurrentUserId() ?: ""

        val newRoom = GameRoom(
            hostId = hostId,
            hostName = hostName,
            gameTitle = gameTitle.trim(),
            description = description.trim(),
            platform = platform,
            contactInfo = contactInfo.trim(),
            maxPlayers = maxPlayers.coerceAtLeast(2),
            currentPlayers = 1,
            timestamp = System.currentTimeMillis(),
            isFollowersOnly = isFollowersOnly,
            members = listOf(hostId)
        )

        _uiState.update { it.copy(isCreatingRoom = true, errorMessage = null) }

        viewModelScope.launch {
            val result = communityRepository.createRoom(newRoom)
            _uiState.update { it.copy(isCreatingRoom = false) }
            result
                .onSuccess {
                    _uiState.update { state -> state.copy(successMessage = UiText.StringResource(R.string.community_room_created_success)) }
                    loadRooms()
                    onComplete(true)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.community_create_room_error))
                    }
                    onComplete(false)
                }
        }
    }

    fun updateRoom(
        roomId: String,
        gameTitle: String,
        description: String,
        platform: String,
        contactInfo: String,
        maxPlayers: Int,
        isFollowersOnly: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        if (gameTitle.isBlank() || description.isBlank() || contactInfo.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.community_fields_required_error)) }
            onComplete(false)
            return
        }

        _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }

        viewModelScope.launch {
            val result = communityRepository.updateRoom(
                roomId = roomId,
                gameTitle = gameTitle.trim(),
                description = description.trim(),
                platform = platform,
                contactInfo = contactInfo.trim(),
                maxPlayers = maxPlayers.coerceAtLeast(2),
                isFollowersOnly = isFollowersOnly
            )

            _uiState.update { it.copy(isActionInProgress = false) }

            result
                .onSuccess {
                    _uiState.update { state -> state.copy(successMessage = UiText.StringResource(R.string.community_room_updated_success)) }
                    loadRooms()
                    onComplete(true)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.community_update_room_error))
                    }
                    onComplete(false)
                }
        }
    }

    fun deleteRoom(roomId: String) {
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            val result = communityRepository.deleteRoom(roomId)
            _uiState.update { it.copy(isActionInProgress = false) }

            result
                .onSuccess {
                    _uiState.update { state -> state.copy(successMessage = UiText.StringResource(R.string.community_room_deleted_success)) }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.community_delete_room_error))
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
