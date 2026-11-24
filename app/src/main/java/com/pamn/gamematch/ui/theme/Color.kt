package com.pamn.gamematch.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- PALETA STEAM MODO OSCURO ---
val SteamDarkBg = Color(0xFF171A21)         // Fondo principal
val SteamDarkCard = Color(0xFF232730)       // Tarjetas
val SteamDarkSurface = Color(0xFF292E37)    // Superficies
val SteamAccentBlue = Color(0xFF66C0F4)     // Azul Steam Neón (Oscuro)

// --- PALETA STEAM MODO CLARO (Colores más oscuros e intensos) ---
val SteamLightBg = Color(0xFFF3F4F6)        // Fondo suave
val SteamLightCard = Color(0xFFFFFFFF)      // Tarjeta blanca limpia
val SteamLightSurface = Color(0xFFE5E7EB)   // Superficie gris claro
val SteamLightAccentBlue = Color(0xFF1072B8) // Azul más oscuro y elegante para modo claro

// --- BOTONES Y BADGES ---
val LikeColor = Color(0xFFFF2A5F)
val DislikeColor = Color(0xFF2C2C35)
val UnreleasedBadgeColor = Color(0xFFFF9800)
val SoftErrorColor = Color(0xFFFF6B6B)

@Immutable
data class SemanticColors(
    val dislikeBg: Color,
    val dislikeIcon: Color,
    val secondaryBtnBg: Color,
    val secondaryIcon: Color,
    val infoIconTint: Color,
    val starYellow: Color,
    val passwordStrengthWeak: Color,
    val passwordStrengthMedium: Color,
    val passwordStrengthStrong: Color,
    val authCardBg: Color,
    val authTextFieldBorder: Color,
    val authTextFieldContainer: Color,
    val authTextSecondary: Color,
    val authGradientMid: Color,
    val authGradientBottom: Color,
    val authTextPrimary: Color,
    val authButtonText: Color,
    val authTextFieldText: Color
)

val DarkSemanticColors = SemanticColors(
    dislikeBg = Color.White,
    dislikeIcon = Color.Black,
    secondaryBtnBg = Color(0xFF2C2C35),
    secondaryIcon = Color.White,
    infoIconTint = Color.White,
    starYellow = Color(0xFFFFC107),
    passwordStrengthWeak = SoftErrorColor,
    passwordStrengthMedium = Color(0xFFFFA000),
    passwordStrengthStrong = Color(0xFF4CAF50),
    authCardBg = Color(0xFF1E222B),
    authTextFieldBorder = Color(0xFF3A3F4D),
    authTextFieldContainer = Color(0xFF171A21),
    authTextSecondary = Color(0xFFD1D5DB),
    authGradientMid = Color(0xFF0E1015),
    authGradientBottom = Color(0xFF050608),
    authTextPrimary = Color.White,
    authButtonText = Color.Black,
    authTextFieldText = Color.White
)

val LightSemanticColors = SemanticColors(
    dislikeBg = Color(0xFF1E1E24),
    dislikeIcon = Color.White,
    secondaryBtnBg = Color(0xFFD1D5DB),
    secondaryIcon = Color(0xFF171A21),
    infoIconTint = SteamAccentBlue,
    starYellow = Color(0xFFFFC107),
    passwordStrengthWeak = SoftErrorColor,
    passwordStrengthMedium = Color(0xFFFFA000),
    passwordStrengthStrong = Color(0xFF4CAF50),
    authCardBg = Color(0xFF1E222B),
    authTextFieldBorder = Color(0xFF3A3F4D),
    authTextFieldContainer = Color(0xFF171A21),
    authTextSecondary = Color(0xFFD1D5DB),
    authGradientMid = Color(0xFF0E1015),
    authGradientBottom = Color(0xFF050608),
    authTextPrimary = Color.White,
    authButtonText = Color.Black,
    authTextFieldText = Color.White
)

val LocalSemanticColors = staticCompositionLocalOf { DarkSemanticColors }
