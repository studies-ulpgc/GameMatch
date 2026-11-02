package com.pamn.gamematch.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pamn.gamematch.ui.favorites.FavoritesScreen
import com.pamn.gamematch.ui.profile.ProfileScreen
import com.pamn.gamematch.ui.swipe.SwipeScreen

@Composable
fun MainContainerScreen(
    onLogout: () -> Unit,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Swipe") },
                    label = { Text("Swipe") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
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
                    onNavigateToProfile = { selectedTab = 2 }
                )
                1 -> FavoritesScreen()
                2 -> ProfileScreen(
                    onLogout = onLogout,
                    isDarkModeGlobal = isDarkMode,
                    onToggleDarkModeGlobal = onToggleDarkMode
                )
            }
        }
    }
}