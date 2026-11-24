package com.pamn.gamematch.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pamn.gamematch.R
import com.pamn.gamematch.utils.PriceFormatter

@Composable
fun priceLabel(price: Double?): String {
    return when {
        price != null && price > 0.0 -> PriceFormatter.formatPrice(price)
        price == 0.0 -> stringResource(R.string.game_price_free)
        else -> stringResource(R.string.game_price_not_available)
    }
}
