package com.pamn.gamematch.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.utils.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val roomActions by lazy {
        CommunityRoomActions(communityRepository, _uiState, viewModelScope, ::loadRooms)
    }

    fun loadInitialData() {
        loadCatalogGames()
        loadLikedGames()
        loadFollowingUserIds()
        loadRooms()
        loadCommunityUsers()
    }

    fun selectTab(tabIndex: Int) = _uiState.update { it.copy(selectedTab = tabIndex) }

    fun setRoomFilter(filter: RoomFilter) {
        _uiState.update { it.copy(roomFilter = filter) }
        loadFollowingUserIds()
        loadRooms()
    }

    fun setUserFilter(filter: UserFilter) {
        _uiState.update { it.copy(userFilter = filter) }
        applyUserFilters()
    }

    fun onSearchGameQueryChanged(query: String) {
        _uiState.update { it.copy(searchGameQuery = query) }
        applyRoomFilters()
    }

    fun onSearchUserQueryChanged(query: String) {
        _uiState.update { it.copy(searchUserQuery = query) }
        applyUserFilters()
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
                    val likedTitles = ids.mapNotNull { id -> catalog.find { it.id == id }?.title }.toSet()
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
                applyUserFilters()
            }
        }
    }

    fun loadCommunityUsers() {
        _uiState.update { it.copy(isLoadingUsers = true, errorMessage = null) }
        viewModelScope.launch {
            communityRepository.getCommunityUsers()
                .onSuccess { allUsers ->
                    val (currentProfile, matches) = buildUserMatches(allUsers, communityRepository.getCurrentUserId())
                    _uiState.update {
                        it.copy(currentUserProfile = currentProfile, userMatches = matches, isLoadingUsers = false)
                    }
                    applyUserFilters()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadingUsers = false, errorMessage = error.toCommunityUiText(R.string.community_load_users_error))
                    }
                }
        }
    }

    fun loadRooms() {
        _uiState.update { it.copy(isLoadingRooms = true, errorMessage = null) }
        viewModelScope.launch {
            communityRepository.getRooms()
                .onSuccess { roomsList ->
                    _uiState.update { it.copy(rooms = roomsList, isLoadingRooms = false) }
                    applyRoomFilters()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadingRooms = false, errorMessage = error.toCommunityUiText(R.string.community_load_rooms_error))
                    }
                }
        }
    }

    private fun applyRoomFilters() {
        val state = _uiState.value
        val filtered = filterRooms(
            rooms = state.rooms,
            currentUid = communityRepository.getCurrentUserId() ?: "",
            followingIds = state.followingUserIds,
            filterMode = state.roomFilter,
            searchQuery = state.searchGameQuery,
            likedTitles = state.likedGameTitles
        )
        _uiState.update { it.copy(filteredRooms = filtered) }
    }

    private fun applyUserFilters() {
        val state = _uiState.value
        val filtered = filterAndSortUsers(
            userMatches = state.userMatches,
            searchQuery = state.searchUserQuery,
            filterMode = state.userFilter,
            followingIds = state.followingUserIds
        )
        _uiState.update { it.copy(filteredUserMatches = filtered) }
    }

    fun toggleFollowUser(targetUserId: String) {
        val isFollowing = _uiState.value.followingUserIds.contains(targetUserId)
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            val result = if (isFollowing) communityRepository.unfollowUser(targetUserId) else communityRepository.followUser(targetUserId)
            result
                .onSuccess {
                    val updated = if (isFollowing) _uiState.value.followingUserIds - targetUserId else _uiState.value.followingUserIds + targetUserId
                    val msgRes = if (isFollowing) R.string.community_user_unfollow_success else R.string.community_user_follow_success
                    _uiState.update {
                        it.copy(followingUserIds = updated, isActionInProgress = false, successMessage = UiText.StringResource(msgRes))
                    }
                    applyUserFilters()
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isActionInProgress = false, errorMessage = error.toCommunityUiText(R.string.community_follow_error))
                    }
                }
        }
    }

    fun joinRoom(roomId: String) {
        val currentUid = communityRepository.getCurrentUserId() ?: return
        roomActions.executeAction(R.string.community_joined_room_success, R.string.community_join_room_error) {
            communityRepository.joinRoom(roomId, currentUid)
        }
    }

    fun leaveRoom(roomId: String) {
        val currentUid = communityRepository.getCurrentUserId() ?: return
        roomActions.executeAction(R.string.community_left_room_success, R.string.community_leave_room_error) {
            communityRepository.leaveRoom(roomId, currentUid)
        }
    }

    fun removeMemberFromRoom(roomId: String, memberUid: String) {
        roomActions.executeAction(R.string.community_member_removed_success, R.string.community_remove_member_error) {
            communityRepository.removeMemberFromRoom(roomId, memberUid)
        }
    }

    fun deleteRoom(roomId: String) {
        roomActions.executeAction(R.string.community_room_deleted_success, R.string.community_delete_room_error) {
            communityRepository.deleteRoom(roomId)
        }
    }

    fun loadMembersProfiles(memberUids: List<String>, onResult: (List<UserProfile>) -> Unit) =
        roomActions.loadMembersProfiles(memberUids, onResult)

    fun createRoom(
        gameTitle: String,
        description: String,
        platform: String,
        contactInfo: String,
        maxPlayers: Int,
        isFollowersOnly: Boolean,
        defaultHostName: String,
        onComplete: (Boolean) -> Unit
    ) = roomActions.createRoom(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly, defaultHostName, onComplete)

    fun updateRoom(
        roomId: String,
        gameTitle: String,
        description: String,
        platform: String,
        contactInfo: String,
        maxPlayers: Int,
        isFollowersOnly: Boolean,
        onComplete: (Boolean) -> Unit
    ) = roomActions.updateRoom(roomId, gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly, onComplete)

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
