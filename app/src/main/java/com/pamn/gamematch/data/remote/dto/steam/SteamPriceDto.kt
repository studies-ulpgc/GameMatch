package com.pamn.gamematch.data.remote.dto.steam

import com.google.gson.annotations.SerializedName

data class SteamAppContainerDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: SteamAppDataDto? = null
)

data class SteamAppDataDto(
    @SerializedName("is_free") val isFree: Boolean? = false,
    @SerializedName("price_overview") val priceOverview: SteamPriceOverviewDto? = null
)

data class SteamPriceOverviewDto(
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("initial") val initial: Int? = null,
    @SerializedName("final") val finalPrice: Int? = null,
    @SerializedName("discount_percent") val discountPercent: Int? = 0,
    @SerializedName("initial_formatted") val initialFormatted: String? = null,
    @SerializedName("final_formatted") val finalFormatted: String? = null
)