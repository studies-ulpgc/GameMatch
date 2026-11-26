package com.pamn.gamematch.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.ui.theme.GameMatchTheme

@Composable
fun StarRatingDisplay(
    rating: Int,
    starSize: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val isFilled = index < rating
            Icon(
                imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (isFilled) GameMatchTheme.semanticColors.starYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    starContentDescription: @Composable (Int) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            val starIndex = index + 1
            val isFilled = starIndex <= rating
            IconButton(
                onClick = { onRatingSelected(starIndex) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = starContentDescription(starIndex),
                    tint = if (isFilled) GameMatchTheme.semanticColors.starYellow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
