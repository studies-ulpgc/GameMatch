package com.pamn.gamematch.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.auth.AuthViewModel
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
    val authRepository = remember { AuthRepository() }
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

    // Control de foco para validaciones del perfil
    var usernameTouched by remember { mutableStateOf(false) }
    var usernameHasFocused by remember { mutableStateOf(false) }
    var yearTouched by remember { mutableStateOf(false) }
    var yearHasFocused by remember { mutableStateOf(false) }

    // Estados para los diálogos de cuenta
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    // Flags de interacción y visibilidad para el diálogo de contraseña
    var newPasswordTouched by remember { mutableStateOf(false) }
    var newPasswordHasFocused by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }
    var confirmPasswordHasFocused by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

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

    fun updateQuickSettings(darkMode: Boolean, language: String) {
        gameRepository.saveUserPreferences(
            username = username.trim(),
            genres = selectedGenres.toList(),
            minPrice = priceRange.start,
            maxPrice = priceRange.endInclusive,
            onlyReleased = onlyReleased,
            birthYear = birthYear,
            isDarkMode = darkMode,
            language = language
        ) { /* Actualización silenciosa en Firestore */ }
    }

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
                    isDarkMode = doc.getBoolean("isDarkMode") ?: true
                    selectedLanguage = doc.getString("language") ?: "Español"
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
                            onCheckedChange = {
                                isDarkMode = it
                                updateQuickSettings(isDarkMode, selectedLanguage)
                            }
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
                                updateQuickSettings(isDarkMode, selectedLanguage)
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
                        Text(text = "Contraseña: ••••••••••••", style = MaterialTheme.typography.bodyMedium)
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
                    label = { Text("Nombre de Usuario *") },
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

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            birthYear = input
                            yearTouched = true
                        }
                    },
                    label = { Text("Año de Nacimiento ($minValidYear - $maxValidYear) *") },
                    singleLine = true,
                    isError = !isYearValid || (yearTouched && birthYear.isBlank()),
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
                        usernameTouched = true
                        yearTouched = true

                        val yNum = birthYear.toIntOrNull()
                        val validYear = yNum != null && yNum in minValidYear..maxValidYear

                        if (username.isBlank() || birthYear.isBlank()) {
                            Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!validYear) {
                            Toast.makeText(context, "Año inválido ($minValidYear - $maxValidYear)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        gameRepository.saveUserPreferences(
                            username = username.trim(),
                            genres = selectedGenres.toList(),
                            minPrice = priceRange.start,
                            maxPrice = priceRange.endInclusive,
                            onlyReleased = onlyReleased,
                            birthYear = birthYear,
                            isDarkMode = isDarkMode,
                            language = selectedLanguage
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

                // --- ZONA DE ACCIONES DE CUENTA ---
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Opciones de Cuenta ⚠️", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { showPasswordDialog = true }) {
                        Text("Cambiar Contraseña")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar Cuenta")
                    }
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

    // --- DIÁLOGO DE CAMBIO DE CONTRASEÑA IGUAL A SIGN UP ---
    if (showPasswordDialog) {
        val authViewModel = remember { AuthViewModel() }
        val isNewPasswordValid = newPasswordInput.length >= 6
        val isConfirmPasswordValid = confirmPasswordInput.isNotEmpty() && confirmPasswordInput == newPasswordInput

        val newPasswordStrength = authViewModel.getPasswordStrength(newPasswordInput)
        val strengthColor = when {
            newPasswordStrength < 0.35f -> MaterialTheme.colorScheme.error
            newPasswordStrength < 0.7f -> Color(0xFFFFA000)
            else -> Color(0xFF4CAF50)
        }

        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                newPasswordTouched = false
                confirmPasswordTouched = false
            },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    // Campo Nueva Contraseña
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Nueva contraseña (min. 6 caracteres)") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar/ocultar contraseña"
                                )
                            }
                        },
                        isError = newPasswordTouched && (newPasswordInput.isBlank() || !isNewPasswordValid),
                        supportingText = {
                            if (newPasswordTouched) {
                                when {
                                    newPasswordInput.isBlank() -> Text("La contraseña es obligatoria")
                                    !isNewPasswordValid -> Text("Debe tener al menos 6 caracteres")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    newPasswordHasFocused = true
                                } else if (newPasswordHasFocused) {
                                    newPasswordTouched = true
                                }
                            }
                    )

                    if (newPasswordInput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { newPasswordStrength },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = strengthColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Confirmar Nueva Contraseña
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar/ocultar contraseña"
                                )
                            }
                        },
                        isError = confirmPasswordTouched && (confirmPasswordInput.isBlank() || !isConfirmPasswordValid),
                        supportingText = {
                            if (confirmPasswordTouched) {
                                when {
                                    confirmPasswordInput.isBlank() -> Text("Confirma tu contraseña")
                                    !isConfirmPasswordValid -> Text("Las contraseñas no coinciden")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    confirmPasswordHasFocused = true
                                } else if (confirmPasswordHasFocused) {
                                    confirmPasswordTouched = true
                                }
                            }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    newPasswordTouched = true
                    confirmPasswordTouched = true

                    if (isNewPasswordValid && isConfirmPasswordValid) {
                        authRepository.updatePassword(newPasswordInput) { success, err ->
                            if (success) {
                                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                                showPasswordDialog = false
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                newPasswordTouched = false
                                confirmPasswordTouched = false
                            } else {
                                Toast.makeText(context, err ?: "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    newPasswordTouched = false
                    confirmPasswordTouched = false
                }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGO DE ELIMINAR CUENTA ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar Cuenta?") },
            text = { Text("Esta acción es permanente y eliminará todos tus favoritos y preferencias de Firebase.") },
            confirmButton = {
                Button(
                    onClick = {
                        authRepository.deleteAccount { success, err ->
                            if (success) {
                                Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                                onLogout()
                            } else {
                                Toast.makeText(context, err ?: "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirmar Borrado") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}