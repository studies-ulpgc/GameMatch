package com.pamn.gamematch.data.local

import androidx.room.TypeConverter

class PlatformsConverter {
    @TypeConverter
    fun fromListToString(platforms: List<String>?): String {
        return platforms?.joinToString(";") ?: ""
    }

    @TypeConverter
    fun fromStringToList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
