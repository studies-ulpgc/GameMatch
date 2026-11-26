[← Volver al README](../README.md)

# 02 — Corrutinas, Flujos y Asincronía

## Tabla de contenidos
1. [Modelo de concurrencia](#1-modelo-de-concurrencia)
2. [viewModelScope y Dispatchers](#2-viewmodelscope-y-dispatchers)
3. [StateFlow como fuente de verdad reactiva](#3-stateflow-como-fuente-de-verdad-reactiva)
4. [Fetch paralelo en IgdbCatalogSyncService](#4-fetch-paralelo-en-igdbcatalogsyncservice)
5. [MainDispatcherRule en tests](#5-maindispatcherrule-en-tests)
6. [Trazabilidad](#6-trazabilidad)

---

## 1. Modelo de concurrencia

GameMatch usa **Kotlin Coroutines** para toda operación de larga duración: llamadas a APIs REST (IGDB, Steam, Twitch), acceso a Firestore y consultas a Room. Las corrutinas se cancelan automáticamente cuando el ViewModel se destruye (`viewModelScope`), evitando memory leaks.

```
Composable
    │  collectAsStateWithLifecycle()   ← Main thread
    ▼
ViewModel (viewModelScope)
    │  launch { Dispatchers.IO }       ← IO pool
    ▼
Repository
    ├── Room suspend funs              ← Dispatcher gestionado por Room
    ├── Firestore Tasks → await()      ← IO
    └── Retrofit suspend funs          ← IO
```

---

## 2. viewModelScope y Dispatchers

Todos los ViewModels lanzan operaciones costosas en `Dispatchers.IO`:

```kotlin
// Ejemplo: DiscoverViewModel
fun loadGames() {
    viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val games = gameRepository.getGames()
            _uiState.update { it.copy(games = games, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
```

El resultado se propaga al hilo principal de forma segura porque `StateFlow` es thread-safe y `collectAsStateWithLifecycle()` observa en `Main`.

---

## 3. StateFlow como fuente de verdad reactiva

Cada ViewModel expone exactamente un `StateFlow<UiState>`:

```kotlin
private val _uiState = MutableStateFlow(DiscoverUiState())
val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()
```

`MutableStateFlow` permite actualizaciones atómicas con `.update { }`, evitando condiciones de carrera:

```kotlin
_uiState.update { current ->
    current.copy(
        filteredGames = applyFilters(current.allGames, current.activeFilters)
    )
}
```

---

## 4. Fetch paralelo en IgdbCatalogSyncService

[`IgdbCatalogSyncService.kt`](../app/src/main/java/com/pamn/gamematch/data/repository/IgdbCatalogSyncService.kt) sincroniza el catálogo de IGDB a Firestore. Las llamadas a la API se pueden lanzar en paralelo con `async`:

```kotlin
// Patrón de fetch paralelo
val igdbDeferred  = async { igdbApiService.queryGames(apicalypseQuery) }
val steamDeferred = async { steamApiService.getAppDetails(steamAppId) }

val igdbResult  = igdbDeferred.await()
val steamResult = steamDeferred.await()
```

Esto reduce el tiempo total de sincronización al solapar la latencia de red de ambas APIs.

---

## 5. MainDispatcherRule en tests

Los tests unitarios de ViewModels necesitan controlar el dispatcher de Main para que `viewModelScope.launch` no falle fuera de un entorno Android real. Se usa `MainDispatcherRule`:

```kotlin
// app/src/test/java/com/pamn/gamematch/ui/MainDispatcherRule.kt
class MainDispatcherRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
```

Uso en tests:

```kotlin
class DiscoverViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadGames updates uiState with games list`() = runTest {
        // given
        every { mockRepository.getGames() } returns testGames
        // when
        viewModel.loadGames()
        // then
        assertEquals(testGames, viewModel.uiState.value.games)
    }
}
```

---

## 6. Trazabilidad

| Concepto | Archivo | Notas |
|---|---|---|
| `viewModelScope.launch` | Todos los `*ViewModel.kt` | Dispatcher IO para I/O |
| `StateFlow` / `MutableStateFlow` | Todos los `*ViewModel.kt` | Patrón `_uiState` / `uiState` |
| `IgdbCatalogSyncService` | [`IgdbCatalogSyncService.kt`](../app/src/main/java/com/pamn/gamematch/data/repository/IgdbCatalogSyncService.kt) | Sincronización IGDB→Firestore |
| `MainDispatcherRule` | [`MainDispatcherRule.kt`](../app/src/test/java/com/pamn/gamematch/ui/MainDispatcherRule.kt) | Rule para tests de ViewModels |
| `runTest` | Todos los `*ViewModelTest.kt` | Bloque de test con corrutinas |

---

> **Nota:** Se evita el uso de `GlobalScope` en toda la base de código. Las corrutinas siempre se lanzan desde un scope con ciclo de vida definido (`viewModelScope`) o en tests con `runTest`.
