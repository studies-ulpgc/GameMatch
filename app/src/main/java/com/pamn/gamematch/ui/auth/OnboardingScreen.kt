package com.pamn.gamematch.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.theme.GameMatchTheme
import java.util.Calendar
import com.pamn.gamematch.data.model.User

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {}
) {
    GameMatchTheme(darkTheme = isDarkMode) {
        val context = LocalContext.current
        val gameRepository = remember { GameRepository() }

        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val minValidYear = currentYear - 120
        val maxValidYear = currentYear - 8

        var username by remember { mutableStateOf("") }
        var birthYear by remember { mutableStateOf("") }
        var selectedGenres by remember { mutableStateOf(setOf<String>()) }
        var isUsernameFocused by remember { mutableStateOf(false) }

        var priceRange by remember { mutableStateOf(0f..70f) }

        var usernameTouched by remember { mutableStateOf(false) }
        var usernameHasFocused by remember { mutableStateOf(false) }

        var yearTouched by remember { mutableStateOf(false) }
        var yearHasFocused by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        var profilePictureUrl by remember { mutableStateOf("https://api.dicebear.com/7.x/bottts/png?seed=default") }
        var showAvatarSelector by remember { mutableStateOf(false) }
        var onlyReleased by remember { mutableStateOf(true) }

        val isYearValid = remember(birthYear) {
            if (birthYear.isEmpty()) true
            else {
                val yearNum = birthYear.toIntOrNull()
                if (yearNum == null) false
                else if (birthYear.length < 4) {
                    val minPrefix = minValidYear.toString().take(birthYear.length).toInt()
                    val maxPrefix = maxValidYear.toString().take(birthYear.length).toInt()
                    yearNum in minPrefix..maxPrefix
                } else {
                    yearNum in minValidYear..maxValidYear
                }
            }
        }
        val availableGenres = com.pamn.gamematch.utils.GenreUtils.ALL_GENRES

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Encabezado
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileAvatar(
                    imageUrl = profilePictureUrl,
                    isEditing = true,
                    onClick = { showAvatarSelector = true }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Configuración de Apariencia (Tarjeta limpia sin contorno)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_appearance),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_dark_mode))
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode(it) }
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_language))
                            }
                            FilterChip(
                                selected = true,
                                onClick = { 
                                    val newLangCode = if (currentLanguage == "es") "en" else "es"
                                    onChangeLanguage(newLangCode)
                                },
                                label = { Text(if (currentLanguage == "es") "Español" else "English") },
                                border = null // Eliminamos borde
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Datos de Usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_username_label)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isUsernameFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    isError = usernameTouched && username.isBlank(),
                    supportingText = {
                        if (usernameTouched && username.isBlank()) {
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_username_error))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isUsernameFocused = focusState.isFocused // Captura el foco
                            if (focusState.isFocused) {
                                usernameHasFocused = true
                            } else if (usernameHasFocused) {
                                usernameTouched = true
                            }
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            birthYear = input
                            yearTouched = true
                        }
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_label)) },
                    singleLine = true,
                    isError = !isYearValid || (yearTouched && birthYear.isBlank()),
                    supportingText = {
                        if ((yearTouched || birthYear.isNotEmpty()) && !isYearValid) {
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_error_out_of_range) + " ($minValidYear - $maxValidYear)")
                        } else if (yearTouched && birthYear.isBlank()) {
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_error_required))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                yearHasFocused = true
                            } else if (yearHasFocused) {
                                yearTouched = true
                            }
                        }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Rango de Precio (Tarjeta limpia sin contorno)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_price_range) + " ${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RangeSlider(
                            value = priceRange,
                            onValueChange = { priceRange = it },
                            valueRange = 0f..100f,
                            steps = 19
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Ajuste de Juegos No Estrenados
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (onlyReleased) androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_desc_on) else androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_desc_off),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = onlyReleased,
                            onCheckedChange = { onToggleDarkMode(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Géneros Preferidos
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_genres_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableGenres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenres.contains(genre),
                                onClick = {
                                    selectedGenres = if (selectedGenres.contains(genre)) selectedGenres - genre else selectedGenres + genre
                                },
                                label = { Text(com.pamn.gamematch.utils.GenreUtils.getLocalizedGenre(genre)) },
                                border = null // Eliminamos borde alrededor de los chips de selección
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón Final
                Button(
                    onClick = {
                        usernameTouched = true
                        yearTouched = true

                        val yNum = birthYear.toIntOrNull()
                        val validYear = yNum != null && yNum in minValidYear..maxValidYear

                        if (username.isBlank() || birthYear.isBlank()) {
                            Toast.makeText(context, context.getString(com.pamn.gamematch.R.string.onboarding_error_incomplete), Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!validYear) {
                            Toast.makeText(context, context.getString(com.pamn.gamematch.R.string.onboarding_birthyear_error_out_of_range) + " ($minValidYear - $maxValidYear)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // LLAMADA NUEVA REFACTORIZADA
                        val updatedUser = User(
                            username = username.trim(),
                            preferredGenres = selectedGenres.toList(),
                            minPrice = priceRange.start,
                            maxPrice = priceRange.endInclusive,
                            onlyReleased = onlyReleased,
                            birthYear = birthYear,
                            isDarkMode = isDarkMode,
                            language = currentLanguage,
                            profilePictureUrl = profilePictureUrl
                        )

                        gameRepository.saveUserPreferences(userModel = updatedUser) { success ->
                            isLoading = false
                            if (success) {
                                onOnboardingComplete()
                            } else {
                                Toast.makeText(context, context.getString(com.pamn.gamematch.R.string.onboarding_error_save), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_continue), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (showAvatarSelector) {
            AvatarSelectionDialog(
                onDismiss = { showAvatarSelector = false },
                onAvatarSelected = { url -> profilePictureUrl = url }
            )
        }
    }
}
