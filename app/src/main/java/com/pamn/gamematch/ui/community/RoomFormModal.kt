package com.pamn.gamematch.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.ui.discover.PLATFORM_OPTIONS

@Composable
fun RoomFormModal(
    catalogGames: List<Game>,
    isBusy: Boolean,
    initialRoom: GameRoom? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, Int, Boolean) -> Unit
) {
    val isEdit = initialRoom != null
    var gameTitle by remember { mutableStateOf(initialRoom?.gameTitle ?: catalogGames.firstOrNull()?.title ?: "") }
    var expandedGameMenu by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf(initialRoom?.platform ?: "PC") }
    var expandedPlatformMenu by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(initialRoom?.description ?: "") }
    var contactInfo by remember { mutableStateOf(initialRoom?.contactInfo ?: "") }
    var maxPlayers by remember { mutableIntStateOf(initialRoom?.maxPlayers ?: 4) }
    var isFollowersOnly by remember { mutableStateOf(initialRoom?.isFollowersOnly ?: false) }

    val minAllowedPlayers = initialRoom?.currentPlayers ?: 2
    val platformOptions = remember { PLATFORM_OPTIONS.filter { it != "Todas" && it != "All" } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (isEdit) R.string.community_edit_room_title else R.string.community_create_room_title))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DropdownSelectorField(
                    label = stringResource(R.string.community_game_title_label),
                    value = gameTitle,
                    onValueChange = { gameTitle = it },
                    readOnly = false,
                    placeholder = if (isEdit) null else stringResource(R.string.community_game_title_placeholder),
                    expanded = expandedGameMenu,
                    onExpandedChange = { expandedGameMenu = it },
                    contentDescription = stringResource(R.string.community_expand_games_cd),
                    options = catalogGames.map { it.title }
                )

                Spacer(modifier = Modifier.height(10.dp))

                DropdownSelectorField(
                    label = stringResource(R.string.community_platform_label),
                    value = platform,
                    onValueChange = { platform = it },
                    readOnly = true,
                    placeholder = null,
                    expanded = expandedPlatformMenu,
                    onExpandedChange = { expandedPlatformMenu = it },
                    contentDescription = stringResource(R.string.community_expand_platforms_cd),
                    options = platformOptions
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.community_desc_label)) },
                    placeholder = if (isEdit) null else ({ Text(stringResource(R.string.community_desc_placeholder)) }),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text(stringResource(R.string.community_contact_label)) },
                    placeholder = if (isEdit) null else ({ Text(stringResource(R.string.community_contact_placeholder)) }),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.community_followers_only_switch), style = MaterialTheme.typography.labelLarge)
                    Switch(checked = isFollowersOnly, onCheckedChange = { isFollowersOnly = it })
                }

                Spacer(modifier = Modifier.height(10.dp))

                PlayerCapacityStepper(
                    maxPlayers = maxPlayers,
                    minAllowedPlayers = minAllowedPlayers,
                    onDecrease = { if (maxPlayers > minAllowedPlayers) maxPlayers-- },
                    onIncrease = { if (maxPlayers < 10) maxPlayers++ }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(gameTitle, description, platform, contactInfo, maxPlayers, isFollowersOnly) },
                enabled = !isBusy && gameTitle.isNotBlank() && description.isNotBlank() && contactInfo.isNotBlank()
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(if (isEdit) R.string.community_save_changes_btn else R.string.community_publish_room_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(stringResource(R.string.community_cancel))
            }
        }
    )
}

@Composable
private fun DropdownSelectorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean,
    placeholder: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    contentDescription: String,
    options: List<String>
) {
    Text(text = label, style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(4.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            readOnly = readOnly,
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(true) }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = contentDescription)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .let { if (readOnly) it.clickable { onExpandedChange(true) } else it }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayerCapacityStepper(
    maxPlayers: Int,
    minAllowedPlayers: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.community_max_players_label), style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = maxPlayers > minAllowedPlayers) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = maxPlayers.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onIncrease, enabled = maxPlayers < 10) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
