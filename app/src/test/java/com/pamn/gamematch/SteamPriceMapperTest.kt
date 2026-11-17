package com.pamn.gamematch

import com.google.gson.JsonParser
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices
import org.junit.Assert.assertEquals
import org.junit.Test

class SteamPriceMapperTest {

    @Test
    fun testParseSteamBatchPrices_handlesEmptyDataArray() {
        val jsonResponse = """
        {
            "1091500": {
                "success": true,
                "data": {
                    "price_overview": {
                        "final": 5999
                    }
                }
            },
            "1245620": {
                "success": true,
                "data": {
                    "price_overview": {
                        "final": 6999
                    }
                }
            },
            "440": {
                "success": true,
                "data": []
            },
            "730": {
                "success": true,
                "data": []
            },
            "999": {
                "success": false
            }
        }
        """.trimIndent()

        val parsedElement = JsonParser.parseString(jsonResponse)
        val responseMap = parsedElement.asJsonObject.entrySet().associate { it.key to it.value }

        val result = parseSteamBatchPrices(responseMap)

        assertEquals(2, result.size)
        assertEquals(59.99, result[1091500]!!, 0.001)
        assertEquals(69.99, result[1245620]!!, 0.001)
    }
}
