package com.pamn.gamematch.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
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
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.auth.AuthViewModel
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.theme.GameMatchTheme
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    isDarkModeGlobal: Boolean = true,
    onToggleDarkModeGlobal: (Boolean) -> Unit = {}
) {
    GameMatchTheme(darkTheme = isDarkModeGlobal) {
        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val gameRepository = remember { GameRepository() }
        val authRepository = remember { AuthRepository() }
        val user = auth.currentUser
        val coroutineScope = rememberCoroutineScope()
        var isSyncingRawg by remember { mutableStateOf(false) }

        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val minValidYear = currentYear - 120
        val maxValidYear = currentYear - 8

        val availableGenres = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

        var isEditing by remember { mutableStateOf(false) }
        var selectedLanguage by remember { mutableStateOf("Español") }

        var username by remember { mutableStateOf("") }
        var selectedGenres by remember { mutableStateOf(setOf<String>()) }
        var priceRange by remember { mutableStateOf(0f..70f) }
        var onlyReleased by remember { mutableStateOf(true) }
        var birthYear by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        // Control de foco para validaciones
        var usernameTouched by remember { mutableStateOf(false) }
        var usernameHasFocused by remember { mutableStateOf(false) }
        var yearTouched by remember { mutableStateOf(false) }
        var yearHasFocused by remember { mutableStateOf(false) }
        var isUsernameFocused by remember { mutableStateOf(false) }

        // Diálogos
        var showPasswordDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var newPasswordInput by remember { mutableStateOf("") }
        var confirmPasswordInput by remember { mutableStateOf("") }

        var newPasswordTouched by remember { mutableStateOf(false) }
        var newPasswordHasFocused by remember { mutableStateOf(false) }
        var confirmPasswordTouched by remember { mutableStateOf(false) }
        var confirmPasswordHasFocused by remember { mutableStateOf(false) }

        var isPasswordVisible by remember { mutableStateOf(false) }
        var isConfirmPasswordVisible by remember { mutableStateOf(false) }

        var profilePictureUrl by remember { mutableStateOf("https://api.dicebear.com/7.x/bottts/png?seed=default") }
        var showAvatarSelector by remember { mutableStateOf(false) }

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
            val userModel = User(
                uid = user?.uid ?: "",
                email = user?.email ?: "",
                username = username.trim(),
                preferredGenres = selectedGenres.toList(),
                minPrice = priceRange.start,
                maxPrice = priceRange.endInclusive,
                onlyReleased = onlyReleased,
                birthYear = birthYear,
                isDarkMode = darkMode,
                language = language,
                profilePictureUrl = profilePictureUrl
            )

            gameRepository.saveUserPreferences(userModel = userModel) { /* Guardado silencioso */ }
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
                        val fetchedDarkMode = doc.getBoolean("isDarkMode") ?: true
                        if (fetchedDarkMode != isDarkModeGlobal) {
                            onToggleDarkModeGlobal(fetchedDarkMode)
                        }
                        selectedLanguage = doc.getString("language") ?: "Español"
                        profilePictureUrl = doc.getString("profilePictureUrl") ?: "https://api.dicebear.com/7.x/bottts/png?seed=default"
                        isLoading = false
                    }
                    .addOnFailureListener { isLoading = false }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Título y botón editar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mi Perfil",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Perfil",
                                tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatar(
                            imageUrl = profilePictureUrl,
                            isEditing = isEditing,
                            onClick = { showAvatarSelector = true }
                        )
                    }

                    // Tarjeta de Ajustes Generales
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ajustes de la Aplicación",
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
                                    Text("Modo Oscuro")
                                }
                                Switch(
                                    checked = isDarkModeGlobal,
                                    onCheckedChange = { newMode ->
                                        onToggleDarkModeGlobal(newMode)
                                        updateQuickSettings(newMode, selectedLanguage)
                                    }
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
                                    Text("Idioma")
                                }
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        selectedLanguage = if (selectedLanguage == "Español") "English" else "Español"
                                        updateQuickSettings(isDarkModeGlobal, selectedLanguage)
                                    },
                                    label = { Text(selectedLanguage) },
                                    border = null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isEditing) {
                        // --- MODO LECTURA ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Información Personal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                ProfileInfoRow(label = "Usuario", value = if (username.isBlank()) "Sin especificar" else username)
                                ProfileInfoRow(label = "Correo", value = user?.email ?: "No disponible")
                                ProfileInfoRow(label = "Contraseña", value = "••••••••••••")
                                ProfileInfoRow(label = "Año de Nacimiento", value = if (birthYear.isBlank()) "Sin especificar" else birthYear)

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = "Preferencias de Juego",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                ProfileInfoRow(label = "Rango de Precio", value = "${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €")
                                ProfileInfoRow(label = "Solo Estrenados", value = if (onlyReleased) "Sí" else "No (Incluye próximos)")
                                ProfileInfoRow(label = "Géneros Favoritos", value = if (selectedGenres.isEmpty()) "Todos" else selectedGenres.joinToString(", "))
                            }
                        }
                    } else {
                        // --- MODO EDICIÓN ---
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nombre de Usuario *") },
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
                                    Text("El nombre de usuario es obligatorio")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    isUsernameFocused = focusState.isFocused
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rango de precio: ${priceRange.start.toInt()} € - ${priceRange.endInclusive.toInt()} €",
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

                        // Switch para incluir o excluir juegos sin estrenar
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
                                        text = "Solo juegos ya estrenados",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (onlyReleased) "Ocultar juegos en desarrollo/próximos" else "Mostrar también juegos no lanzados",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = onlyReleased,
                                    onCheckedChange = { onlyReleased = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Géneros Favoritos",
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
                                    label = { Text(genre) },
                                    border = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

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

                                val updatedUser = User(
                                    uid = user?.uid ?: "",
                                    email = user?.email ?: "",
                                    username = username.trim(),
                                    preferredGenres = selectedGenres.toList(),
                                    minPrice = priceRange.start,
                                    maxPrice = priceRange.endInclusive,
                                    onlyReleased = onlyReleased,
                                    birthYear = birthYear,
                                    isDarkMode = isDarkModeGlobal,
                                    language = selectedLanguage,
                                    profilePictureUrl = profilePictureUrl
                                )

                                gameRepository.saveUserPreferences(userModel = updatedUser) { success ->
                                    if (success) {
                                        isEditing = false
                                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Guardar Cambios", style = MaterialTheme.typography.titleMedium)
                        }

                        // --- ZONA DE ACCIONES DE CUENTA ---
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Opciones de Cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showPasswordDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cambiar Contraseña")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar Cuenta")
                            }
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Herramientas de Desarrollador",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sincroniza el catálogo de RAWG API hacia Cloud Firestore.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        isSyncingRawg = true
                                        coroutineScope.launch {
                                            val result = gameRepository.syncCatalogFromRawg()
                                            isSyncingRawg = false
                                            result
                                                .onSuccess { count ->
                                                    Toast.makeText(context, "¡Éxito! Sincronizados $count juegos con RAWG.", Toast.LENGTH_LONG).show()
                                                }
                                                .onFailure { error ->
                                                    Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    },
                                    enabled = !isSyncingRawg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSyncingRawg) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sincronizando...")
                                    } else {
                                        Text("Sincronizar catálogo con RAWG (Admin)")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            auth.signOut()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Cerrar Sesión", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // --- DIÁLOGOS DE CONTRASEÑA Y ELIMINACIÓN ---
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = strengthColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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

        if (showAvatarSelector) {
            AvatarSelectionDialog(
                onDismiss = { showAvatarSelector = false },
                onAvatarSelected = { url -> profilePictureUrl = url }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}