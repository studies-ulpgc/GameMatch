package com.pamn.gamematch.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

interface LocaleManager {
    fun setLocale(languageCode: String)
}

class LocaleManagerImpl : LocaleManager {
    override fun setLocale(languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}