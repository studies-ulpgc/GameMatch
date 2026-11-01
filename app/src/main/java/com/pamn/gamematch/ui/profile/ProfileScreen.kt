package com.pamn.gamematch.ui.profile

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.repository.GameRepository

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

    // Lista de géneros disponibles para elegir
    val availableGenres = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

    // Estados de las preferencias del usuario
    var username by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var maxPrice by remember { mutableFloatStateOf(70f) }
    var onlyReleased by remember { mutableStateOf(true) }
    var birthYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Cargar datos actuales desde Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("username")
                    if (name != null) username = name

                    val genresList = doc.get("preferredGenres") as? List<String>
                    if (genresList != null) selectedGenres = genresList.toSet()

                    val price = doc.getDouble("maxPrice")
                    if (price != null) maxPrice = price.toFloat()

                    val released = doc.getBoolean("onlyReleased")
                    if (released != null) onlyReleased = released

                    val year = doc.getString("birthYear")
                    if (year != null) birthYear = year

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
            Text(
                text = "Mi Perfil y Preferencias ⚙️",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Tarjeta de información de cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Correo:", style = MaterialTheme.typography.labelMedium)
                    Text(text = user?.email ?: "Sin correo registrado", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de Usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seccion 1: Año de Nacimiento (Validacion edad)
            OutlinedTextField(
                value = birthYear,
                onValueChange = { if (it.length <= 4) birthYear = it },
                label = { Text("Año de Nacimiento (Ej. 2002)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Seccion 2: Generos Favoritos
            Text(text = "Géneros Favoritos:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableGenres.forEach { genre ->
                    FilterChip(
                        selected = selectedGenres.contains(genre),
                        onClick = {
                            selectedGenres = if (selectedGenres.contains(genre)) {
                                selectedGenres - genre
                            } else {
                                selectedGenres + genre
                            }
                        },
                        label = { Text(genre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seccion 3: Precio Maximo
            Text(
                text = "Precio máximo por juego: ${maxPrice.toInt()} €",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                valueRange = 0f..100f,
                steps = 10
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Seccion 4: Filtro de Solo Estrenados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ocultar juegos aún no estrenados", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = onlyReleased,
                    onCheckedChange = { onlyReleased = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Boton para guardar preferencias
            Button(
                onClick = {
                    isSaving = true
                    gameRepository.saveUserPreferences(
                        username = username,
                        genres = selectedGenres.toList(),
                        maxPrice = maxPrice,
                        onlyReleased = onlyReleased,
                        birthYear = birthYear
                    ) { success ->
                        isSaving = false
                        if (success) {
                            Toast.makeText(context, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de Cerrar Sesión
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