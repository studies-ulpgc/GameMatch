package com.pamn.gamematch.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var isLoading by remember { mutableStateOf(false) }

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
            text = "Configura tu perfil para recomendarte los mejores juegos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campos Obligatorios
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = birthYear,
            onValueChange = { if (it.length <= 4) birthYear = it },
            label = { Text("Año de Nacimiento (Ej. 2002) *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campos Opcionales
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
                if (username.isBlank() || birthYear.isBlank()) {
                    Toast.makeText(context, "Por favor completa el nombre y año de nacimiento", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                gameRepository.saveUserPreferences(
                    username = username,
                    genres = selectedGenres.toList(),
                    maxPrice = maxPrice,
                    onlyReleased = true,
                    birthYear = birthYear
                ) { success ->
                    isLoading = false
                    if (success) {
                        onOnboardingComplete()
                    } else {
                        Toast.makeText(context, "Error al guardar perfil", Toast.LENGTH_SHORT).show()
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