package com.pamn.gamematch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SteamAccentBlue,
    onPrimary = Color.Black,
    secondary = SteamDarkSurface,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2A2E38), // Elimina violeta en botones
    onSecondaryContainer = Color.White,
    background = SteamDarkBg,
    onBackground = Color.White,
    surface = SteamDarkCard,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF232730),     // Elimina violeta en Footer
    onSurfaceVariant = Color(0xFFADADAD)
)

private val LightColorScheme = lightColorScheme(
    primary = SteamAccentBlue,
    onPrimary = Color.White,
    secondary = SteamLightSurface,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFE0E0E0), // Elimina violeta en botones
    onSecondaryContainer = Color.Black,
    background = SteamLightBg,
    onBackground = Color(0xFF171A21),
    surface = SteamLightCard,
    onSurface = Color(0xFF171A21),
    surfaceVariant = Color(0xFFE5E7EB),     // Elimina violeta en Footer
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun GameMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}