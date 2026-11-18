package com.pamn.gamematch

import com.pamn.gamematch.data.remote.dto.igdb.IgdbExternalGameDto
import com.pamn.gamematch.data.remote.dto.igdb.IgdbGameDto
import com.pamn.gamematch.data.remote.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameMapperTest {

    @Test
    fun testToDomain_steamAppIdParsing() {
        // 1. Multiple comma-separated IDs in uid -> returns first valid integer (292030)
        val gameWithCommaUid = IgdbGameDto(
            id = 100L,
            name = "Game 1",
            externalGames = listOf(
                IgdbExternalGameDto(externalGameSource = 1, uid = "292030,378649,378648")
            )
        )
        assertEquals(292030, gameWithCommaUid.toDomain().steamAppId)

        // 2. Single valid UID -> returns 12345
        val gameWithSingleUid = IgdbGameDto(
            id = 101L,
            name = "Game 2",
            externalGames = listOf(
                IgdbExternalGameDto(externalGameSource = 1, uid = "12345")
            )
        )
        assertEquals(12345, gameWithSingleUid.toDomain().steamAppId)

        // 3. No Steam entry -> returns null
        val gameWithoutSteam = IgdbGameDto(
            id = 102L,
            name = "Game 3",
            externalGames = listOf(
                IgdbExternalGameDto(externalGameSource = 14, uid = "99999")
            )
        )
        assertNull(gameWithoutSteam.toDomain().steamAppId)
    }
}
