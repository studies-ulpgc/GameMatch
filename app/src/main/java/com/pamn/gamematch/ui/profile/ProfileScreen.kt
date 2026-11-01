package com.pamn.gamematch.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.repository.GameRepository
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val gameRepository = remember { GameRepository() }
    val user = auth.currentUser

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val minValidYear = currentYear - 120
    val maxValidYear = currentYear - 8

    val availableGenres = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

    var isEditing by remember { mutableStateOf(false) }

    var isDarkMode by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("Español") }

    var username by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var priceRange by remember { mutableStateOf(0f..70f) }
    var onlyReleased by remember { mutableStateOf(true) }
    var birthYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username") ?: ""
                    val genresList = doc.get("preferredGenres") as? List<String>
                    if (genresList != null) selectedGenres = genresList.toSet()

                    val minP = doc.getDouble("minPrice")?.toFloat() ?: 0f
                    val maxP = doc.getDouble("maxPrice")?.toFloat() ?: 70f
                    priceRange = minP..maxP

                    onlyReleased = doc.getBoolean("onlyReleased") ?: true
                    birthYear = doc.getString("birthYear") ?: ""
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Mi Perfil 👤", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Perfil",
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ajustes Rápido de Interfaz
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Ajustes de la App 🎨", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tema Oscuro")
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Idioma")
                        FilterChip(
                            selected = true,
                            onClick = {
                                selectedLanguage = if (selectedLanguage == "Español") "English" else "Español"
                            },
                            label = { Text(selectedLanguage) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditing) {
                // --- MODO LECTURA ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Usuario: ${if (username.isBlank()) "Sin especificar" else username}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Correo: ${user?.email}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Año de Nacimiento: ${if (birthYear.isBlank()) "Sin especificar" else birthYear}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Rango de Precio: ${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Géneros: ${if (selectedGenres.isEmpty()) "Todos" else selectedGenres.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // --- MODO EDICIÓN ---
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) birthYear = input
                    },
                    label = { Text("Año de Nacimiento ($minValidYear - $maxValidYear)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Géneros Favoritos:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Rango de precio: ${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €", style = MaterialTheme.typography.titleMedium)
                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 0f..100f,
                    steps = 19
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val yearNum = birthYear.toIntOrNull()
                        if (yearNum != null && (yearNum < minValidYear || yearNum > maxValidYear)) {
                            Toast.makeText(context, "Año inválido ($minValidYear - $maxValidYear)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        gameRepository.saveUserPreferences(
                            username = username.trim(),
                            genres = selectedGenres.toList(),
                            minPrice = priceRange.start,
                            maxPrice = priceRange.endInclusive,
                            onlyReleased = onlyReleased,
                            birthYear = birthYear
                        ) { success ->
                            if (success) {
                                isEditing = false
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}