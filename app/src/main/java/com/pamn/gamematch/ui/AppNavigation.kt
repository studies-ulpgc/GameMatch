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
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Descubrir") },
                    label = { Text("Swipe") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> SwipeScreen(
                    onNavigateToProfile = { selectedTab = 2 },
                    onNavigateToFullDetails = { /* Conectar cuando tu compañera suba la pantalla */ }
                )
                1 -> FavoritesScreen(
                    onNavigateToFullDetails = { /* Conectar cuando tu compañera suba la pantalla */ }
                )
                2 -> ProfileScreen(
                    onNavigateBack = { selectedTab = 0 },
                    onLogout = onLogout
                )
            }
        }
    }
}