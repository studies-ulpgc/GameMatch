package com.pamn.gamematch.ui.community

import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.utils.UiText

enum class RoomFilter {
    ALL,
    FOLLOWING_ONLY,
    FAVORITES_ONLY
}

enum class UserFilter {
    ALL,
    FOLLOWING_ONLY
}

data class CommunityUiState(
    val selectedTab: Int = 0,
    val userMatches: List<UserProfile> = emptyList(),
    val filteredUserMatches: List<UserProfile> = emptyList(),
    val currentUserProfile: UserProfile? = null,
    val rooms: List<GameRoom> = emptyList(),
    val filteredRooms: List<GameRoom> = emptyList(),
    val catalogGames: List<Game> = emptyList(),
    val followingUserIds: Set<String> = emptySet(),
    val likedGameTitles: Set<String> = emptySet(),
    val searchGameQuery: String = "",
    val searchUserQuery: String = "",
    val roomFilter: RoomFilter = RoomFilter.ALL,
    val userFilter: UserFilter = UserFilter.ALL,
    val isLoadingUsers: Boolean = false,
    val isLoadingRooms: Boolean = false,
    val isCreatingRoom: Boolean = false,
    val isActionInProgress: Boolean = false,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)
