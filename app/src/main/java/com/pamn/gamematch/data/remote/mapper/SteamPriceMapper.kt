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

fun parseSteamBatchWithoutPrice(response: Map<String, JsonElement>): Set<Int> {
    val result = mutableSetOf<Int>()
    for ((appIdStr, element) in response) {
        try {
            val appId = appIdStr.toIntOrNull() ?: continue
            if (!element.isJsonObject) continue
            val obj = element.asJsonObject

            if (obj.has("success") && obj.get("success").asBoolean) {
                val data = obj.get("data")
                if (data == null || !data.isJsonObject) {
                    result.add(appId)
                } else {
                    val dataObj = data.asJsonObject
                    if (!dataObj.has("price_overview") || !dataObj.get("price_overview").isJsonObject) {
                        result.add(appId)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorar excepciones individuales
        }
    }
    return result
}

fun parseSteamIsFree(element: JsonElement?): Boolean {
    return try {
        if (element == null || !element.isJsonObject) return false
        val obj = element.asJsonObject
        if (!obj.has("success") || !obj.get("success").asBoolean) return false
        if (!obj.has("data") || !obj.get("data").isJsonObject) return false
        val dataObj = obj.get("data").asJsonObject
        dataObj.has("is_free") && dataObj.get("is_free").asBoolean
    } catch (e: Exception) {
        false
    }
}
