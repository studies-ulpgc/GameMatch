package com.pamn.gamematch.data.remote.dto.rawg

import com.google.gson.annotations.SerializedName

data class RawgGameResponseDto(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("next") val next: String? = null,
    @SerializedName("results") val results: List<RawgGameDto> = emptyList()
)

data class RawgGameDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = "",
    @SerializedName("background_image") val backgroundImage: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("released") val released: String? = null,
    @SerializedName("genres") val genres: List<RawgGenreDto>? = null,
    @SerializedName("platforms") val platforms: List<RawgPlatformWrapperDto>? = null
)

data class RawgGenreDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = ""
)

data class RawgPlatformWrapperDto(
    @SerializedName("platform") val platform: RawgPlatformDto? = null
)

data class RawgPlatformDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = ""
)