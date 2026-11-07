package com.pamn.gamematch.utils

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LocaleUtils {
    
    fun getCurrentAppLanguage(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            return normalizeLanguage(appLocales.get(0)?.language)
        }
        return normalizeLanguage(Locale.getDefault().language)
    }

    /**
     * Normaliza un idioma a un código ISO válido de 2 letras ("es" o "en").
     */
    fun normalizeLanguage(language: String?): String {
        val lower = language?.trim()?.lowercase() ?: ""
        if (lower.startsWith("en") || lower == "english") return "en"
        if (lower.startsWith("es") || lower == "español") return "es"
        return "es" // Default a español si no es inglés
    }
}