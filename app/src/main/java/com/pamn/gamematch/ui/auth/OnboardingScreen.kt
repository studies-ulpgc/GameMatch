package com.pamn.gamematch.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.data.repository.GameRepository
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository() }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val minValidYear = currentYear - 120
    val maxValidYear = currentYear - 8

    var username by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }

    var priceRange by remember { mutableStateOf(0f..70f) }
    var isDarkMode by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("Español") }

    var usernameTouched by remember { mutableStateOf(false) }
    var usernameHasFocused by remember { mutableStateOf(false) }

    var yearTouched by remember { mutableStateOf(false) }
    var yearHasFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val yearNum = birthYear.toIntOrNull()
    val isYearValid = birthYear.isBlank() || (yearNum != null && yearNum in minValidYear..maxValidYear)
    val availableGenres = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "¡Bienvenido a GameMatch! 🎮", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Configura tu experiencia inicial",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Preferencia de Apariencia 🎨", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tema Oscuro")
                    Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Idioma")
                    FilterChip(
                        selected = true,
                        onClick = { selectedLanguage = if (selectedLanguage == "Español") "English" else "Español" },
                        label = { Text(selectedLanguage) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de Usuario con error al salir o dejarlo vacio
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario *") },
            singleLine = true,
            isError = usernameTouched && username.isBlank(),
            supportingText = {
                if (usernameTouched && username.isBlank()) {
                    Text("El nombre de usuario es obligatorio")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        usernameHasFocused = true
                    } else if (usernameHasFocused) {
                        usernameTouched = true
                    }
                }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Año de Nacimiento con error instantaneo al escribir mal o salir
        OutlinedTextField(
            value = birthYear,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 4) {
                    birthYear = input
                    yearTouched = true // Muestra error al escribir un valor fuera de rango
                }
            },
            label = { Text("Año de Nacimiento (Ej. 2002) *") },
            singleLine = true,
            isError = (yearTouched || birthYear.isNotEmpty()) && (birthYear.isBlank() || !isYearValid),
            supportingText = {
                if ((yearTouched || birthYear.isNotEmpty()) && !isYearValid) {
                    Text("Año fuera de rango ($minValidYear - $maxValidYear)")
                } else if (yearTouched && birthYear.isBlank()) {
                    Text("El año de nacimiento es obligatorio")
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

        Text(
            text = "Rango de precio: ${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €",
            style = MaterialTheme.typography.titleMedium
        )
        RangeSlider(
            value = priceRange,
            onValueChange = { priceRange = it },
            valueRange = 0f..100f,
            steps = 19
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Géneros Preferidos (Opcional):", style = MaterialTheme.typography.titleMedium)
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
                    label = { Text(genre) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                usernameTouched = true
                yearTouched = true

                val yNum = birthYear.toIntOrNull()
                val validYear = yNum != null && yNum in minValidYear..maxValidYear

                if (username.isBlank() || birthYear.isBlank()) {
                    Toast.makeText(context, "Por favor, completa tu nombre y año de nacimiento", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!validYear) {
                    Toast.makeText(context, "Año de nacimiento fuera de rango ($minValidYear - $maxValidYear)", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                gameRepository.saveUserPreferences(
                    username = username.trim(),
                    genres = selectedGenres.toList(),
                    minPrice = priceRange.start,
                    maxPrice = priceRange.endInclusive,
                    onlyReleased = true,
                    birthYear = birthYear,
                    isDarkMode = isDarkMode,
                    language = selectedLanguage
                ) { success ->
                    isLoading = false
                    if (success) {
                        onOnboardingComplete()
                    } else {
                        Toast.makeText(context, "Error al guardar el perfil", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Comenzar a Explorar 🚀")
        }
    }
}