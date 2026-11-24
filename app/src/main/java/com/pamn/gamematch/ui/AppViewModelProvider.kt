package com.pamn.gamematch.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pamn.gamematch.GameMatchApp
import com.pamn.gamematch.ui.auth.AuthViewModel
import com.pamn.gamematch.ui.auth.OnboardingViewModel
import com.pamn.gamematch.ui.community.CommunityViewModel
import com.pamn.gamematch.ui.detail.GameDetailViewModel
import com.pamn.gamematch.ui.discover.DiscoverViewModel
import com.pamn.gamematch.ui.favorites.FavoritesViewModel
import com.pamn.gamematch.ui.profile.ProfileViewModel
import com.pamn.gamematch.ui.settings.AppSettingsViewModel
import com.pamn.gamematch.ui.swipe.SwipeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                authRepository = gameMatchApplication().container.authRepository
            )
        }
        initializer {
            CommunityViewModel(
                communityRepository = gameMatchApplication().container.communityRepository,
                gameRepository = gameMatchApplication().container.gameRepository
            )
        }
        initializer {
            DiscoverViewModel(
                repository = gameMatchApplication().container.gameRepository
            )
        }
        initializer {
            FavoritesViewModel(
                repository = gameMatchApplication().container.gameRepository
            )
        }
        initializer {
            GameDetailViewModel(
                gameRepository = gameMatchApplication().container.gameRepository,
                authRepository = gameMatchApplication().container.authRepository
            )
        }
        initializer {
            AppSettingsViewModel(
                settingsRepository = gameMatchApplication().container.settingsRepository
            )
        }
        initializer {
            SwipeViewModel(
                gameRepository = gameMatchApplication().container.gameRepository
            )
        }
        initializer {
            ProfileViewModel(
                authRepository = gameMatchApplication().container.authRepository,
                gameRepository = gameMatchApplication().container.gameRepository
            )
        }
        initializer {
            OnboardingViewModel(
                gameRepository = gameMatchApplication().container.gameRepository
            )
        }
    }
}

fun CreationExtras.gameMatchApplication(): GameMatchApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GameMatchApp)
