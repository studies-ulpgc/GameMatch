package com.pamn.gamematch.data.remote.dto.igdb

import com.google.gson.annotations.SerializedName

data class TwitchTokenResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("token_type") val tokenType: String? = null
)

data class IgdbGameDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = "",
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("first_release_date") val firstReleaseDate: Long? = null,
    @SerializedName("cover") val cover: IgdbImageDto? = null,
    @SerializedName("screenshots") val screenshots: List<IgdbImageDto>? = null,
    @SerializedName("genres") val genres: List<IgdbNamedItemDto>? = null,
    @SerializedName("platforms") val platforms: List<IgdbNamedItemDto>? = null,
    @SerializedName("external_games") val externalGames: List<IgdbExternalGameDto>? = null
)

data class IgdbImageDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("image_id") val imageId: String? = null
)

data class IgdbNamedItemDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = ""
)

data class IgdbExternalGameDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("uid") val uid: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("category") val category: Int? = null,
    @SerializedName("external_game_source") val externalGameSource: Int? = null
)
