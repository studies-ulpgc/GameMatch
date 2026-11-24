package com.pamn.gamematch

import com.pamn.gamematch.utils.GenreMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreMatcherTest {

    @Test
    fun testCanonicalGenres() {
        assertEquals(setOf("RPG", "Aventura"), GenreMatcher.canonicalGenres("Role-playing (RPG), Adventure"))
        assertEquals(setOf("Acción"), GenreMatcher.canonicalGenres("Shooter"))
        assertEquals(setOf("Acción"), GenreMatcher.canonicalGenres("Hack and slash/Beat 'em up"))
        assertEquals(setOf("Deportes"), GenreMatcher.canonicalGenres("Racing"))
        assertEquals(setOf("Deportes"), GenreMatcher.canonicalGenres("Sports"))
        assertEquals(setOf("Acción", "Terror"), GenreMatcher.canonicalGenres("Acción, Terror"))
        assertTrue(GenreMatcher.canonicalGenres("Puzzle").isEmpty())
        assertTrue(GenreMatcher.canonicalGenres("").isEmpty())
    }
}
