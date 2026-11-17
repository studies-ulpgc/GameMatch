package com.pamn.gamematch.data.remote.mapper

import com.google.gson.JsonElement

fun parseSteamBatchPrices(response: Map<String, JsonElement>): Map<Int, Double> {
    val result = mutableMapOf<Int, Double>()
    for ((appIdStr, element) in response) {
        try {
            val appId = appIdStr.toIntOrNull() ?: continue
            if (!element.isJsonObject) continue
            val obj = element.asJsonObject

            if (obj.has("success") && obj.get("success").asBoolean) {
                if (obj.has("data") && obj.get("data").isJsonObject) {
                    val data = obj.get("data").asJsonObject
                    if (data.has("price_overview") && data.get("price_overview").isJsonObject) {
                        val priceOverview = data.get("price_overview").asJsonObject
                        if (priceOverview.has("final")) {
                            val finalPrice = priceOverview.get("final").asInt
                            result[appId] = finalPrice / 100.0
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorar fallos individuales para no corromper el procesamiento del lote entero
        }
    }
    return result
}
