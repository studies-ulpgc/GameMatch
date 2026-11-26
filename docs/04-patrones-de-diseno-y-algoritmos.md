[← Volver al README](../README.md)

# 04 — Patrones de Diseño y Algoritmos

## Tabla de contenidos
1. [Patrón Repository](#1-patrón-repository)
2. [Patrón DI Container](#2-patrón-di-container)
3. [Patrón Mapper / DTO](#3-patrón-mapper--dto)
4. [Patrón Observer con StateFlow](#4-patrón-observer-con-stateflow)
5. [Singleton thread-safe](#5-singleton-thread-safe)
6. [Algoritmos de filtrado y ordenación](#6-algoritmos-de-filtrado-y-ordenación)
7. [GenreUtils — localización de géneros](#7-genreutils--localización-de-géneros)
8. [Trazabilidad](#8-trazabilidad)

---

## 1. Patrón Repository

Cada fuente de datos está encapsulada en un repositorio. Los ViewModels nunca acceden directamente a Room, Firestore ni Retrofit:

```
ViewModel
    └── GameRepository
            ├── GameDao (Room)
            ├── FirebaseFirestore
            └── IgdbApiService (Retrofit)
```

Ventajas: testabilidad (se mockea el repositorio completo), intercambiabilidad de fuentes y lógica de caché centralizada.

Los repositorios del proyecto:

| Repositorio | Fuentes de datos | Responsabilidad principal |
|---|---|---|
| `AuthRepository` | Firebase Auth, Firestore | Login, registro, perfil |
| `GameRepository` | Firestore, IGDB API, Room | Catálogo, favoritos, reseñas |
| `CommunityRepository` | Firestore | Salas de juego, matches |
| `SettingsRepository` | Firestore | Preferencias de usuario |

---

## 2. Patrón DI Container

`AppContainerImpl` actúa como **Service Locator / IoC Container** manual:

```kotlin
// Creación lazy: solo se instancia cuando se necesita por primera vez
override val gameRepository: GameRepository by lazy {
    GameRepository(
        db      = firestore,
        auth    = firebaseAuth,
        gameDao = database.gameDao()
    )
}
```

La responsabilidad de crear y conectar dependencias está centralizada, no dispersa en las pantallas. El patrón es equivalente a `@Singleton` en Hilt pero sin procesado de anotaciones.

---

## 3. Patrón Mapper / DTO

La aplicación tiene dos tipos de objetos de transferencia:
- **Remote DTO** → modelo de dominio: las respuestas JSON de IGDB y Steam se mapean en `data/remote/mapper/`.
- **Entidad Room** → modelo de dominio: `GameEntity.toDomain()` / `Game.toEntity()` en `data/local/mapper/`.

```kotlin
// Remote DTO → dominio
fun IgdbGameDto.toDomain(): Game = Game(
    id       = this.id.toString(),
    title    = this.name ?: "Sin título",
    coverUrl = this.cover?.url?.let { "https:$it" },
    genres   = this.genres?.map { it.name } ?: emptyList(),
    // ...
)
```

Este patrón garantiza que los modelos de dominio no tienen dependencias de Gson, Room ni ningún framework externo.

---

## 4. Patrón Observer con StateFlow

Kotlin `StateFlow` implementa el patrón **Observer** de forma reactiva: múltiples Composables pueden observar el mismo `StateFlow` sin acoplamiento directo.

```kotlin
// Publisher (ViewModel)
val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

// Subscriber (Composable)
val state by viewModel.uiState.collectAsStateWithLifecycle()
// La UI se recompone automáticamente cuando uiState cambia
```

A diferencia de `LiveData`, `StateFlow` es agnóstico del ciclo de vida (lo gestiona `collectAsStateWithLifecycle`) y es 100 % compatible con corrutinas.

---

## 5. Singleton thread-safe

`AppDatabase` y `RetrofitClient` usan el patrón **Singleton con double-checked locking**:

```kotlin
// AppDatabase
companion object {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(/*...*/).build().also { INSTANCE = it }
        }
}
```

`@Volatile` garantiza visibilidad entre hilos; `synchronized` garantiza atomicidad en la creación.

---

## 6. Algoritmos de filtrado y ordenación

### Filtrado de juegos (`DiscoverViewModel`)

```kotlin
private fun applyFilters() {
    val query    = _uiState.value.searchQuery.lowercase()
    val genre    = _uiState.value.selectedGenre
    val platform = _uiState.value.selectedPlatform
    val minScore = _uiState.value.minScore

    val filtered = _uiState.value.allGames.filter { game ->
        (query.isBlank() || game.title.lowercase().contains(query)) &&
        (genre.isNullOrBlank() || game.genres.contains(genre)) &&
        (platform.isNullOrBlank() || game.platforms.contains(platform)) &&
        (game.rating ?: 0.0) >= minScore
    }
    _uiState.update { it.copy(filteredGames = filtered) }
}
```

Complejidad: **O(n · m)** donde n = número de juegos y m = número de géneros/plataformas por juego (típicamente ≤ 5).

### Ordenación (`SortOption`)

`SortOption.kt` define las opciones de ordenación disponibles y el comparador asociado. Los juegos filtrados se ordenan con `sortedWith(compareBy { ... })` en O(n log n).

### Matches de usuarios (`CommunityViewModel`)

`buildUserMatches` calcula el solapamiento de géneros favoritos entre el usuario actual y otros usuarios registrados:

```kotlin
// Pseudocódigo simplificado
fun buildUserMatches(currentUser: User, allUsers: List<User>): List<UserMatch> =
    allUsers
        .filter { it.uid != currentUser.uid }
        .map { other ->
            val commonGenres = currentUser.favoriteGenres intersect other.favoriteGenres.toSet()
            UserMatch(user = other, matchScore = commonGenres.size, sharedGenres = commonGenres.toList())
        }
        .sortedByDescending { it.matchScore }
```

---

## 7. GenreUtils — localización de géneros

`GenreUtils.kt` mapea nombres de géneros en inglés (tal como llegan de la API) a sus traducciones en el idioma del dispositivo:

```kotlin
object GenreUtils {
    fun getLocalizedGenre(genre: String): String =
        when (genre.lowercase()) {
            "role-playing (rpg)" -> stringResource(R.string.genre_rpg)
            "shooter"            -> stringResource(R.string.genre_shooter)
            "strategy"           -> stringResource(R.string.genre_strategy)
            // ...
            else                 -> genre
        }
}
```

> **Nota de implementación:** `getLocalizedGenre` acepta `String` (no `@Composable`) para poder usarse en lógica de filtrado fuera del árbol de composición; los Composables que necesitan la etiqueta localizada la llaman directamente desde el contexto Composable.

---

## 8. Trazabilidad

| Patrón | Archivo principal |
|---|---|
| Repository | [`GameRepository.kt`](../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt) |
| DI Container | [`AppContainerImpl`](../app/src/main/java/com/pamn/gamematch/data/container/AppContainer.kt) |
| Mapper remoto | `data/remote/mapper/` |
| Mapper local | `data/local/mapper/` |
| Observer / StateFlow | Todos los `*ViewModel.kt` |
| Singleton Room | [`AppDatabase.kt`](../app/src/main/java/com/pamn/gamematch/data/local/AppDatabase.kt) |
| Filtrado | `DiscoverViewModel.kt`, `CommunityViewModel.kt` |
| Ordenación | [`SortOption.kt`](../app/src/main/java/com/pamn/gamematch/utils/SortOption.kt) |
| Localización de géneros | [`GenreUtils.kt`](../app/src/main/java/com/pamn/gamematch/utils/GenreUtils.kt) |
