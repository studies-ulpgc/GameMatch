package com.pamn.gamematch.ui.profile

import com.pamn.gamematch.utils.UiText

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val birthYear: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..70f,
    val onlyReleased: Boolean = true,
    val profilePictureUrl: String = "https://api.dicebear.com/7.x/bottts/png?seed=default",
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSyncingIgdb: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showAvatarSelector: Boolean = false,
    val userMessage: UiText? = null
)
