package com.pamn.gamematch

import com.pamn.gamematch.utils.platformFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlatformFamilyTest {

    @Test
    fun testPlatformFamilyMapping() {
        assertEquals("PC", platformFamily("PC (Microsoft Windows)"))
        assertEquals("PlayStation", platformFamily("PlayStation 5"))
        assertEquals("Xbox", platformFamily("Xbox Series X|S"))
        assertEquals("Nintendo", platformFamily("Nintendo Switch"))
        assertEquals("Nintendo", platformFamily("Wii U"))
        assertEquals("Nintendo", platformFamily("Super Nintendo Entertainment System"))
        assertEquals("Nintendo", platformFamily("New Nintendo 3DS"))
        assertEquals("Nintendo", platformFamily("Game Boy Advance"))
        assertEquals("PC", platformFamily("Mac"))
        assertEquals("PC", platformFamily("DOS"))
        assertEquals("PlayStation", platformFamily("PlayStation VR2"))
        assertNull(platformFamily("Meta Quest 2"))
        assertNull(platformFamily("Android"))
        assertNull(platformFamily("Web browser"))
    }
}
