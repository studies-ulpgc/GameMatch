package com.pamn.gamematch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SteamAccentBlue,
    onPrimary = Color.Black,
    secondary = SteamDarkSurface,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2A2E38),
    onSecondaryContainer = Color.White,
    background = SteamDarkBg,
    onBackground = Color.White,
    surface = SteamDarkCard,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF232730),
    onSurfaceVariant = Color(0xFFADADAD)
)

private val LightColorScheme = lightColorScheme(
    primary = SteamLightAccentBlue,
    onPrimary = Color.White,
    secondary = SteamLightSurface,
    onSecondary = Color(0xFF171A21),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF171A21),
    background = SteamLightBg,
    onBackground = Color(0xFF171A21),
    surface = SteamLightCard,
    onSurface = Color(0xFF171A21),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563)
)

object GameMatchTheme {
    val semanticColors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current
}

@Composable
fun GameMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
