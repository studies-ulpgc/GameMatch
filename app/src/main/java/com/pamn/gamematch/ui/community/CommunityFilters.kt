package com.pamn.gamematch.ui.community

import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile

internal fun buildUserMatches(
    allUsers: List<UserProfile>,
    currentUid: String?
): Pair<UserProfile?, List<UserProfile>> {
    val currentProfile = allUsers.find { it.uid == currentUid }
    val currentGenres = currentProfile?.preferredGenres?.toSet() ?: emptySet()

    val matches = allUsers
        .filter { it.uid != currentUid }
        .map { other ->
            val otherGenres = other.preferredGenres.toSet()
            val common = currentGenres.intersect(otherGenres).toList()
            val percentage = when {
                currentGenres.isNotEmpty() -> (common.size.toFloat() / currentGenres.size.toFloat() * 100).toInt().coerceIn(0, 100)
                otherGenres.isNotEmpty() -> 50
                else -> 0
            }
            other.copy(matchPercentage = percentage, commonGenres = common)
        }
        .sortedByDescending { it.matchPercentage }

    return currentProfile to matches
}

internal fun filterRooms(
    rooms: List<GameRoom>,
    currentUid: String,
    followingIds: Set<String>,
    filterMode: RoomFilter,
    searchQuery: String,
    likedTitles: Set<String>
): List<GameRoom> {
    val query = searchQuery.trim()
    return rooms.filter { room ->
        val isOwnRoom = room.hostId == currentUid
        val isFollowingHost = room.hostId in followingIds
        val isMember = room.members.contains(currentUid)

        val isVisible = !room.isFollowersOnly || isFollowingHost || isOwnRoom || isMember
        val passesChip = when (filterMode) {
            RoomFilter.ALL -> !isOwnRoom
            RoomFilter.FOLLOWING_ONLY -> isFollowingHost || room.isFollowersOnly
            RoomFilter.FAVORITES_ONLY -> !isOwnRoom && likedTitles.any { title ->
                title.equals(room.gameTitle, ignoreCase = true) || room.gameTitle.contains(title, ignoreCase = true)
            }
        }
        val matchesSearch = query.isEmpty() ||
            room.gameTitle.contains(query, ignoreCase = true) ||
            room.description.contains(query, ignoreCase = true)

        isVisible && passesChip && matchesSearch
    }
}

internal fun filterAndSortUsers(
    userMatches: List<UserProfile>,
    searchQuery: String,
    filterMode: UserFilter,
    followingIds: Set<String>
): List<UserProfile> {
    val query = searchQuery.trim()
    return userMatches
        .filter { user ->
            val matchesQuery = query.isEmpty() || user.username.contains(query, ignoreCase = true)
            val passesFilter = filterMode == UserFilter.ALL || user.uid in followingIds
            matchesQuery && passesFilter
        }
        .sortedWith(
            compareByDescending<UserProfile> { it.matchPercentage }
                .thenByDescending { it.commonGenres.size }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.username }
        )
}
