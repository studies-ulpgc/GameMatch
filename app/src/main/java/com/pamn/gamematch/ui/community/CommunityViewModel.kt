package com.pamn.gamematch.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.data.repository.GameRepository
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
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CommunityViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository(),
    private val gameRepository: GameRepository = GameRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
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
        applyRoomFilters()
    }

    fun onSearchGameQueryChanged(query: String) {
        _uiState.update { it.copy(searchGameQuery = query) }
        applyRoomFilters()
    }

    fun loadCatalogGames() {
        val games = gameRepository.getAllGames()
        _uiState.update { it.copy(catalogGames = games) }
    }

    fun loadLikedGames() {
        gameRepository.getLikedGames { result ->
            result.onSuccess { ids ->
                val catalog = gameRepository.getAllGames()
                val likedTitles = ids.mapNotNull { id ->
                    catalog.find { game -> game.id == id }?.title
                }.toSet()
                _uiState.update { it.copy(likedGameTitles = likedTitles) }
                applyRoomFilters()
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
                            errorMessage = error.message ?: "Error al cargar los usuarios"
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
                            errorMessage = error.message ?: "Error al cargar las salas"
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
            // Excluir salas propias de la pestaña principal "Salas de Juego" (se gestionan en la pestaña "Mis Salas")
            val isNotOwnRoom = room.hostId != currentUid

            // Regla de visibilidad: si es solo para seguidos, el usuario actual debe seguir al host
            val isFollowingHost = room.hostId in following
            val isVisible = !room.isFollowersOnly || isFollowingHost

            // Regla de filtro activo en la pestaña:
            val passesChipFilter = when (filterMode) {
                RoomFilter.ALL -> true
                RoomFilter.FOLLOWING_ONLY -> isFollowingHost
                RoomFilter.FAVORITES_ONLY -> likedTitles.any { title ->
                    title.equals(room.gameTitle, ignoreCase = true) || room.gameTitle.contains(title, ignoreCase = true)
                }
            }

            // Regla de búsqueda por texto:
            val matchesSearch = searchQuery.isEmpty() ||
                    room.gameTitle.contains(searchQuery, ignoreCase = true) ||
                    room.description.contains(searchQuery, ignoreCase = true)

            isNotOwnRoom && isVisible && passesChipFilter && matchesSearch
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
                    val msg = if (isFollowing) "Has dejado de seguir a este usuario" else "¡Ahora sigues a este usuario!"
                    _uiState.update { state ->
                        state.copy(
                            followingUserIds = updatedFollowing,
                            isActionInProgress = false,
                            successMessage = msg
                        )
                    }
                    applyRoomFilters()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isActionInProgress = false,
                            errorMessage = error.message ?: "Error al actualizar seguimiento"
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
                    _uiState.update { it.copy(isActionInProgress = false, successMessage = "¡Te has unido a la sala!") }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = error.message ?: "Error al unirte a la sala") }
                }
        }
    }

    fun leaveRoom(roomId: String) {
        val currentUid = communityRepository.getCurrentUserId() ?: return
        _uiState.update { it.copy(isActionInProgress = true) }

        viewModelScope.launch {
            communityRepository.leaveRoom(roomId, currentUid)
                .onSuccess {
                    _uiState.update { it.copy(isActionInProgress = false, successMessage = "Has salido de la sala") }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = error.message ?: "Error al salir de la sala") }
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
                            successMessage = "Miembro expulsado de la sala con éxito"
                        )
                    }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = error.message ?: "Error al expulsar al miembro"
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
        onComplete: (Boolean) -> Unit
    ) {
        if (gameTitle.isBlank() || description.isBlank() || contactInfo.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, completa todos los campos requeridos") }
            onComplete(false)
            return
        }

        val currentUser = _uiState.value.currentUserProfile
        val hostName = currentUser?.username?.ifBlank { "Anfitrión" } ?: "Anfitrión"
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
                    _uiState.update { state -> state.copy(successMessage = "¡Sala creada con éxito!") }
                    loadRooms()
                    onComplete(true)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.message ?: "Error al crear la sala")
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
            _uiState.update { it.copy(errorMessage = "Por favor, completa todos los campos requeridos") }
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
                    _uiState.update { state -> state.copy(successMessage = "¡Sala actualizada con éxito!") }
                    loadRooms()
                    onComplete(true)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.message ?: "Error al actualizar la sala")
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
                    _uiState.update { state -> state.copy(successMessage = "¡Sala eliminada con éxito!") }
                    loadRooms()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.message ?: "Error al eliminar la sala")
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}