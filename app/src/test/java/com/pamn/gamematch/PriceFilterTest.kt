package com.pamn.gamematch

import com.pamn.gamematch.ui.discover.matchesPriceFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceFilterTest {

    @Test
    fun testMatchesPriceFilter() {
        // 1. Precio null con barra en 100 -> entra (true)
        assertTrue(matchesPriceFilter(price = null, maxPrice = 100.0))

        // 2. Precio null con barra en 50 -> no entra (false)
        assertFalse(matchesPriceFilter(price = null, maxPrice = 50.0))

        // 3. Precio 0.0 con barra en 0 -> entra (true)
        assertTrue(matchesPriceFilter(price = 0.0, maxPrice = 0.0))

        // 4. Precio 30 con barra en 20 -> no entra (false)
        assertFalse(matchesPriceFilter(price = 30.0, maxPrice = 20.0))

        // 5. Precio 30 con barra en 50 -> entra (true)
        assertTrue(matchesPriceFilter(price = 30.0, maxPrice = 50.0))
    }
}
