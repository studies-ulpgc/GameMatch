package com.pamn.gamematch.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object PriceFormatter {
    fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("ES").build())
        format.currency = Currency.getInstance("EUR")
        // En Java el espacio entre el número y el símbolo de moneda puede ser un non-breaking space
        // Lo reemplazamos a espacio normal para uniformidad, y garantizamos que haya un espacio antes del €.
        var formatted = format.format(price).replace("\u00A0", " ")
        if (formatted.endsWith("€") && !formatted.endsWith(" €")) {
            formatted = formatted.replace("€", " €")
        }
        return formatted.trim()
    }
}
