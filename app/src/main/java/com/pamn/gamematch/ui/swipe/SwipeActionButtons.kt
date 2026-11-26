package com.pamn.gamematch.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.LikeColor

@Composable
internal fun SwipeEmptyState(onReloadCatalog: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.swipe_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.swipe_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReloadCatalog) {
            Text(stringResource(R.string.swipe_reload_button))
        }
    }
}

@Composable
internal fun SwipeActionButtons(
    onSwipeLeft: () -> Unit,
    onUndo: () -> Unit,
    onShowInfo: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = GameMatchTheme.semanticColors

    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSwipeLeft,
            modifier = Modifier.size(56.dp).background(semanticColors.dislikeBg, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.swipe_cd_dislike),
                tint = semanticColors.dislikeIcon,
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(
            onClick = onUndo,
            modifier = Modifier.size(44.dp).background(semanticColors.secondaryBtnBg, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.swipe_cd_undo),
                tint = semanticColors.secondaryIcon,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(
            onClick = onShowInfo,
            modifier = Modifier.size(44.dp).background(semanticColors.secondaryBtnBg, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.swipe_cd_info),
                tint = semanticColors.infoIconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(
            onClick = onSwipeRight,
            modifier = Modifier.size(56.dp).background(LikeColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = stringResource(R.string.swipe_cd_like),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
