package com.pamn.gamematch.ui.community

import androidx.annotation.StringRes
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.utils.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CommunityRoomActions(
    private val communityRepository: CommunityRepository,
    private val uiState: MutableStateFlow<CommunityUiState>,
    private val scope: CoroutineScope,
    private val onRoomsChanged: () -> Unit
) {

    fun executeAction(
        @StringRes successRes: Int,
        @StringRes errorRes: Int,
        action: suspend () -> Result<Unit>
    ) {
        uiState.update { it.copy(isActionInProgress = true) }
        scope.launch {
            action()
                .onSuccess {
                    uiState.update { it.copy(isActionInProgress = false, successMessage = UiText.StringResource(successRes)) }
                    onRoomsChanged()
                }
                .onFailure { error ->
                    uiState.update { it.copy(isActionInProgress = false, errorMessage = error.toCommunityUiText(errorRes)) }
                }
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
        if (!validateFields(gameTitle, description, contactInfo, onComplete)) return

        val currentUser = uiState.value.currentUserProfile
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

        uiState.update { it.copy(isCreatingRoom = true, errorMessage = null) }
        scope.launch {
            val result = communityRepository.createRoom(newRoom)
            uiState.update { it.copy(isCreatingRoom = false) }
            handleMutationResult(result, R.string.community_room_created_success, R.string.community_create_room_error, onComplete)
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
        if (!validateFields(gameTitle, description, contactInfo, onComplete)) return

        uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }
        scope.launch {
            val result = communityRepository.updateRoom(
                roomId = roomId,
                gameTitle = gameTitle.trim(),
                description = description.trim(),
                platform = platform,
                contactInfo = contactInfo.trim(),
                maxPlayers = maxPlayers.coerceAtLeast(2),
                isFollowersOnly = isFollowersOnly
            )
            uiState.update { it.copy(isActionInProgress = false) }
            handleMutationResult(result, R.string.community_room_updated_success, R.string.community_update_room_error, onComplete)
        }
    }

    fun loadMembersProfiles(memberUids: List<String>, onResult: (List<UserProfile>) -> Unit) {
        scope.launch {
            communityRepository.getMembersProfiles(memberUids)
                .onSuccess { onResult(it) }
                .onFailure { onResult(emptyList()) }
        }
    }

    private fun validateFields(
        gameTitle: String,
        description: String,
        contactInfo: String,
        onComplete: (Boolean) -> Unit
    ): Boolean {
        if (gameTitle.isNotBlank() && description.isNotBlank() && contactInfo.isNotBlank()) return true
        uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.community_fields_required_error)) }
        onComplete(false)
        return false
    }

    private fun handleMutationResult(
        result: Result<Unit>,
        @StringRes successRes: Int,
        @StringRes errorRes: Int,
        onComplete: (Boolean) -> Unit
    ) {
        result
            .onSuccess {
                uiState.update { it.copy(successMessage = UiText.StringResource(successRes)) }
                onRoomsChanged()
                onComplete(true)
            }
            .onFailure { error ->
                uiState.update { it.copy(errorMessage = error.toCommunityUiText(errorRes)) }
                onComplete(false)
            }
    }
}

internal fun Throwable.toCommunityUiText(@StringRes fallbackRes: Int): UiText {
    return localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(fallbackRes)
}
