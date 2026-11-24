package com.pamn.gamematch

import com.google.gson.JsonParser
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchWithoutPrice
import com.pamn.gamematch.data.remote.mapper.parseSteamIsFree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun testParseSteamIsFree() {
        val freeJson = JsonParser.parseString("""{"success": true, "data": {"is_free": true}}""")
        val paidJson = JsonParser.parseString("""{"success": true, "data": {"is_free": false}}""")
        val emptyDataJson = JsonParser.parseString("""{"success": true, "data": []}""")
        val successFalseJson = JsonParser.parseString("""{"success": false}""")

        assertTrue(parseSteamIsFree(freeJson))
        assertFalse(parseSteamIsFree(paidJson))
        assertFalse(parseSteamIsFree(emptyDataJson))
        assertFalse(parseSteamIsFree(successFalseJson))
    }

    @Test
    fun testParseSteamBatchWithoutPrice() {
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
            "440": {
                "success": true,
                "data": []
            },
            "730": {
                "success": true,
                "data": {
                    "type": "game",
                    "is_free": true
                }
            },
            "999": {
                "success": false
            }
        }
        """.trimIndent()

        val parsedElement = JsonParser.parseString(jsonResponse)
        val responseMap = parsedElement.asJsonObject.entrySet().associate { it.key to it.value }

        val result = parseSteamBatchWithoutPrice(responseMap)

        assertEquals(2, result.size)
        assertTrue(result.contains(440))
        assertTrue(result.contains(730))
        assertFalse(result.contains(1091500))
        assertFalse(result.contains(999))
    }
}
