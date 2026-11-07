package com.pamn.gamematch.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pamn.gamematch.ui.community.CommunityScreen
import com.pamn.gamematch.ui.discover.DiscoverScreen
import com.pamn.gamematch.ui.favorites.FavoritesScreen
import com.pamn.gamematch.ui.profile.ProfileScreen
import com.pamn.gamematch.ui.swipe.SwipeScreen

import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MainContainerScreen(
    onLogout: () -> Unit,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_swipe)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_swipe)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_discover)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_discover)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Groups, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_community)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_community)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_favorites)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_favorites)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_profile)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.nav_profile)) },
                    colors = navItemColors
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> SwipeScreen(
                    onNavigateToProfile = { selectedTab = 4 }
                )
                1 -> DiscoverScreen()
                2 -> CommunityScreen()
                3 -> FavoritesScreen()
                4 -> ProfileScreen(
                    onLogout = onLogout,
                    isDarkModeGlobal = isDarkMode,
                    onToggleDarkModeGlobal = onToggleDarkMode,
                    currentLanguage = currentLanguage,
                    onChangeLanguage = onChangeLanguage
                )
            }
        }
    }
}
