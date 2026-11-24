package com.pamn.gamematch

import com.pamn.gamematch.data.remote.mapper.computeFlags
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComputeFlagsTest {

    @Test
    fun testComputeFlags() {
        val nowMs = 1700000000000L
        val sixMonthsAgoSec = (nowMs - (180L * 24 * 3600 * 1000)) / 1000L
        val threeYearsAgoSec = (nowMs - (3L * 365 * 24 * 3600 * 1000)) / 1000L
        val futureSec = (nowMs + (30L * 24 * 3600 * 1000)) / 1000L

        // 1. Nota 85 con 20 votos -> destacado
        val (featured1, _) = computeFlags(
            rating = 85.0,
            ratingCount = 20,
            firstReleaseDateSeconds = threeYearsAgoSec,
            nowMs = nowMs
        )
        assertTrue(featured1)

        // 2. Nota 100 con 5 votos -> no destacado
        val (featured2, _) = computeFlags(
            rating = 100.0,
            ratingCount = 5,
            firstReleaseDateSeconds = threeYearsAgoSec,
            nowMs = nowMs
        )
        assertFalse(featured2)

        // 3. Lanzado hace 6 meses con 10 votos -> trending
        val (_, trending3) = computeFlags(
            rating = 70.0,
            ratingCount = 10,
            firstReleaseDateSeconds = sixMonthsAgoSec,
            nowMs = nowMs
        )
        assertTrue(trending3)

        // 4. Lanzado hace 3 años -> no trending
        val (_, trending4) = computeFlags(
            rating = 70.0,
            ratingCount = 10,
            firstReleaseDateSeconds = threeYearsAgoSec,
            nowMs = nowMs
        )
        assertFalse(trending4)

        // 5. Fecha futura -> no trending
        val (_, trending5) = computeFlags(
            rating = 70.0,
            ratingCount = 10,
            firstReleaseDateSeconds = futureSec,
            nowMs = nowMs
        )
        assertFalse(trending5)

        // 6. Null en todo -> ambos false
        val (featured6, trending6) = computeFlags(
            rating = null,
            ratingCount = null,
            firstReleaseDateSeconds = null,
            nowMs = nowMs
        )
        assertFalse(featured6)
        assertFalse(trending6)
    }
}
