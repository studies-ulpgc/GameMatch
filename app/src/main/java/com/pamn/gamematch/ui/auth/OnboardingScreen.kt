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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository() }

    var username by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var maxPrice by remember { mutableFloatStateOf(70f) }
    var isDarkMode by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("Español") }

    var yearTouched by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val yearNum = birthYear.toIntOrNull()
    val isYearValid = yearNum != null && yearNum in 1905..2026
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

        // Ajustes Rápidos de Tema e Idioma
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

        // Nombre de Usuario
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Año con validacion al perder foco
        OutlinedTextField(
            value = birthYear,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 4) {
                    birthYear = input
                }
            },
            label = { Text("Año de Nacimiento (Ej. 2002) *") },
            singleLine = true,
            isError = yearTouched && !isYearValid && birthYear.isNotEmpty(),
            supportingText = {
                if (yearTouched && !isYearValid && birthYear.isNotEmpty()) {
                    Text("Año inválido (debe ser entre 1905 y 2026)")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && birthYear.isNotEmpty()) yearTouched = true
                }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Filtro de Precio Máximo
        Text(text = "Precio máximo por juego: ${maxPrice.toInt()} €", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = maxPrice,
            onValueChange = { maxPrice = it },
            valueRange = 0f..100f,
            steps = 10
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Géneros
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
                yearTouched = true

                if (username.isBlank() || birthYear.isBlank()) {
                    Toast.makeText(context, "Por favor, completa tu nombre y año de nacimiento", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!isYearValid) {
                    Toast.makeText(context, "Año de nacimiento fuera de rango (1905 - 2026)", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                gameRepository.saveUserPreferences(
                    username = username.trim(),
                    genres = selectedGenres.toList(),
                    maxPrice = maxPrice,
                    onlyReleased = true,
                    birthYear = birthYear
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