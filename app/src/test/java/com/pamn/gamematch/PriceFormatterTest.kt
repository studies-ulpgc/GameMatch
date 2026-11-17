package com.pamn.gamematch

import com.pamn.gamematch.utils.PriceFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun testFormatPrice() {
        assertEquals("0,00 €", PriceFormatter.formatPrice(0.0))
        assertEquals("0,84 €", PriceFormatter.formatPrice(0.84))
        assertEquals("19,99 €", PriceFormatter.formatPrice(19.99))
        assertEquals("58,99 €", PriceFormatter.formatPrice(58.99))
    }
}
