[← Volver al README](../README.md)

# 01 — Arquitectura MVVM y Navegación

## Tabla de contenidos
1. [Visión general](#1-visión-general)
2. [Capas de la arquitectura](#2-capas-de-la-arquitectura)
3. [Contenedor de dependencias](#3-contenedor-de-dependencias)
4. [AppViewModelProvider](#4-appviewmodelprovider)
5. [Navegación con NavHost](#5-navegación-con-navhost)
6. [Flujo UDF y StateFlow](#6-flujo-udf-y-stateflow)
7. [Trazabilidad](#7-trazabilidad)

---

## 1. Visión general

GameMatch implementa el patrón **MVVM** (Model-View-ViewModel) recomendado por Google para aplicaciones Android modernas con Jetpack Compose. Las tres capas se comunican de forma unidireccional (UDF — Unidirectional Data Flow):

```
Eventos del usuario
        ↓
   ViewModel  (lógica de presentación · StateFlow)
        ↓
  Repository  (fuente única de verdad)
        ↓
 Data Sources (Room · Firestore · Retrofit)
```

La inyección de dependencias se realiza de forma **manual** mediante `AppContainer`, sin frameworks externos como Hilt o Dagger, siguiendo las directrices de los codelabs oficiales de Android.

---

## 2. Capas de la arquitectura

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **UI** | `ui/` | Composables stateless, reciben `UiState`, emiten eventos al ViewModel |
| **ViewModel** | `ui/*/ViewModel.kt` | Transforma datos del repositorio en `StateFlow<UiState>` |
| **Repository** | `data/repository/` | Fuente única de verdad; decide entre caché local y red |
| **Data Sources** | `data/local/`, `data/remote/` | Room (SQLite) y Retrofit (REST) |
| **Model** | `data/model/` | Entidades de dominio puras (sin dependencias Android) |

---

## 3. Contenedor de dependencias

[`AppContainer.kt`](../app/src/main/java/com/pamn/gamematch/data/container/AppContainer.kt) define la interfaz; [`AppContainerImpl`](../app/src/main/java/com/pamn/gamematch/data/container/AppContainer.kt) la implementa con singletons lazy:

```kotlin
class AppContainerImpl(private val database: AppDatabase) : AppContainer {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth: FirebaseAuth  by lazy { FirebaseAuth.getInstance() }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(auth = firebaseAuth, db = firestore)
    }
    override val gameRepository: GameRepository by lazy {
        GameRepository(db = firestore, auth = firebaseAuth, gameDao = database.gameDao())
    }
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(db = firestore, auth = firebaseAuth)
    }
    override val communityRepository: CommunityRepository by lazy {
        CommunityRepository(db = firestore, auth = firebaseAuth)
    }
}
```

El contenedor se instancia una sola vez en `GameMatchApp` (subclase de `Application`) y queda accesible a través de `GameMatchApp.container`.

---

## 4. AppViewModelProvider

[`AppViewModelProvider.kt`](../app/src/main/java/com/pamn/gamematch/ui/AppViewModelProvider.kt) expone un único `ViewModelProvider.Factory` que construye los **9 ViewModels** de la aplicación:

| ViewModel | Repositorios inyectados |
|---|---|
| `AuthViewModel` | `authRepository` |
| `OnboardingViewModel` | `gameRepository` |
| `DiscoverViewModel` | `gameRepository` |
| `FavoritesViewModel` | `gameRepository` |
| `GameDetailViewModel` | `gameRepository`, `authRepository` |
| `AppSettingsViewModel` | `settingsRepository` |
| `SwipeViewModel` | `gameRepository` |
| `CommunityViewModel` | `communityRepository`, `gameRepository` |
| `ProfileViewModel` | `authRepository`, `gameRepository` |

---

## 5. Navegación con NavHost

La navegación se gestiona en `MainContainerScreen.kt` con `NavHost` de Jetpack Navigation Compose. Las rutas son cadenas de texto constantes definidas junto a cada pantalla.

```
NavHost (startDestination = "login")
├── login          → LoginScreen
├── signup         → SignUpScreen
├── onboarding     → OnboardingScreen
└── main           → MainContainerScreen (BottomBar)
    ├── discover   → DiscoverScreen
    ├── swipe      → SwipeScreen
    ├── favorites  → FavoritesScreen
    ├── community  → CommunityScreen
    └── profile    → ProfileScreen
```

---

## 6. Flujo UDF y StateFlow

Cada ViewModel expone un único `StateFlow<UiState>` que el Composable observa con `collectAsStateWithLifecycle()`. Los eventos se envían como métodos del ViewModel (no como `sealed class Event`), manteniendo el código simple:

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(DiscoverUiState())
val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    applyFilters()
}

// Composable
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

---

## 7. Trazabilidad

| Concepto | Archivo | Líneas clave |
|---|---|---|
| Interfaz `AppContainer` | [`AppContainer.kt`](../app/src/main/java/com/pamn/gamematch/data/container/AppContainer.kt) | 1-37 |
| Implementación `AppContainerImpl` | [`AppContainer.kt`](../app/src/main/java/com/pamn/gamematch/data/container/AppContainer.kt) | 18-37 |
| `AppViewModelProvider` | [`AppViewModelProvider.kt`](../app/src/main/java/com/pamn/gamematch/ui/AppViewModelProvider.kt) | 1-72 |
| `AppDatabase` Room | [`AppDatabase.kt`](../app/src/main/java/com/pamn/gamematch/data/local/AppDatabase.kt) | 1-30 |
| Navegación principal | [`MainContainerScreen.kt`](../app/src/main/java/com/pamn/gamematch/ui/MainContainerScreen.kt) | — |

---

> **Nota:** La elección de DI manual sobre Hilt se justifica por la finalidad académica del proyecto: favorece la comprensión de los principios de inyección sin magia de código generado.
