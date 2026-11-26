<div align="center">
  <img src="https://www.ulpgc.es/sites/default/files/ArchivosULPGC/identidad-corporativa/logo-ulpgc-horizontal.png" alt="ULPGC Logo" width="260"/>
  <h1>🎮 GameMatch</h1>
  <p><em>Aplicación Android nativa — PAMN · Universidad de Las Palmas de Gran Canaria</em></p>

  ![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)
  ![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
  ![Room](https://img.shields.io/badge/Room-local_DB-brightgreen?logo=android)
  ![GitHub Actions](https://img.shields.io/badge/CI-GitHub_Actions-2088FF?logo=githubactions&logoColor=white)
  ![SonarQube](https://img.shields.io/badge/SonarQube-local_Docker-CB3032?logo=sonarqube&logoColor=white)
  ![Estado](https://img.shields.io/badge/Estado-En_desarrollo-orange)
</div>

---

## Tabla de contenidos

1. [Resumen y Objetivos](#1-resumen-y-objetivos)
2. [Demo](#2-demo)
3. [Requisitos y Configuración](#3-requisitos-y-configuración)
4. [Arquitectura](#4-arquitectura)
5. [Tests y Cobertura](#5-tests-y-cobertura)
6. [Documentación técnica](#6-documentación-técnica)
7. [APIs, Librerías e IA](#7-apis-librerías-e-ia)

---

## 1. Resumen y Objetivos

**GameMatch** es una aplicación móvil nativa Android que permite a jugadores descubrir videojuegos, guardar favoritos, unirse a salas de juego comunitarias y conectar con otros usuarios con intereses similares.

| Objetivo | Descripción |
|---|---|
| Descubrimiento | Explorar catálogo sincronizado desde IGDB con filtros por género, plataforma y nota |
| Favoritos | Lista personal de juegos guardados con persistencia local (Room) |
| Comunidad | Crear y unirse a salas de juego, ver matches con otros jugadores |
| Perfil | Gestión de preferencias, géneros favoritos y avatar |
| Calidad | Cobertura de tests ≥ 40 %, 0 archivos > 250 líneas, CI/CD automatizado |

---

## 2. Demo

> *Los vídeos de demostración se añadirán próximamente en `readme_media/shorts/`.*

| Pantalla | Descripción |
|---|---|
| Discover | Catálogo de juegos con filtros y búsqueda |
| Swipe | Desliza juegos al estilo Tinder |
| Favoritos | Lista de juegos guardados |
| Comunidad | Salas de juego y matches de usuarios |
| Perfil | Edición de perfil y preferencias |

---

## 3. Requisitos y Configuración

### Requisitos del entorno

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Ladybug 2024.2+ |
| JDK | 17 (Temurin recomendado) |
| Android SDK compileSdk | 37 |
| Android SDK minSdk | 24 (Android 7.0) |
| Gradle | 8.x (wrapper incluido) |

### Configuración de credenciales

1. **Copia el archivo de ejemplo:**
   ```bash
   cp local.properties.example local.properties
   ```
2. **Rellena tus credenciales** en `local.properties` (ver [`local.properties.example`](local.properties.example)):
   ```properties
   IGDB_CLIENT_ID=tu_client_id_de_twitch
   IGDB_CLIENT_SECRET=tu_client_secret_de_twitch
   ```
3. **Firebase:** descarga `google-services.json` desde la [consola de Firebase](https://console.firebase.google.com/) y colócalo en `app/google-services.json`.

> **⚠️ Nunca subas `local.properties` ni `google-services.json` al repositorio.**

### Comandos principales

```bash
# Compilar APK debug
./gradlew assembleDebug

# Tests unitarios
./gradlew testDebugUnitTest

# Tests instrumentados (emulador conectado)
./gradlew connectedDebugAndroidTest

# Tests + JaCoCo (informe HTML en app/build/reports/jacoco/)
./gradlew testDebugUnitTest jacocoTestReport

# Tests + JaCoCo + análisis SonarQube (Docker en localhost:9000)
./gradlew testDebugUnitTest jacocoTestReport sonar
```

---

## 4. Arquitectura

GameMatch sigue la arquitectura **MVVM (Model-View-ViewModel)** con inyección de dependencias manual mediante `AppContainer`.

```
app/src/main/java/com/pamn/gamematch/
├── data/
│   ├── container/
│   │   ├── AppContainer.kt            ← Interfaz del contenedor DI
│   │   └── AppContainerImpl.kt        ← Implementación con lazy singletons
│   ├── local/
│   │   ├── AppDatabase.kt             ← Base de datos Room (v1)
│   │   ├── GameDao.kt                 ← DAO: getAll, upsertAll, count, clear
│   │   ├── GameEntity.kt              ← Entidad Room tabla "games"
│   │   ├── PlatformsConverter.kt      ← TypeConverter List<String>↔JSON
│   │   └── mapper/                    ← toDomain() / toEntity()
│   ├── model/
│   │   ├── Game.kt                    ← Modelo de dominio principal
│   │   ├── GameReview.kt
│   │   ├── GameRoom.kt
│   │   ├── User.kt
│   │   └── UserProfile.kt
│   ├── remote/
│   │   ├── RetrofitClient.kt          ← Clientes Retrofit (IGDB, Steam, Twitch)
│   │   ├── auth/
│   │   │   ├── IgdbAuthInterceptor.kt ← OkHttp interceptor + refresco 401
│   │   │   └── IgdbTokenProvider.kt   ← Token OAuth2 con caché en memoria
│   │   ├── api/
│   │   │   ├── IgdbApiService.kt
│   │   │   ├── SteamApiService.kt
│   │   │   └── TwitchAuthApiService.kt
│   │   └── mapper/                    ← DTOs → modelos de dominio
│   └── repository/
│       ├── AuthRepository.kt          ← Firebase Auth + Firestore usuarios
│       ├── CommunityRepository.kt     ← Salas y matches Firestore
│       ├── GameRepository.kt          ← IGDB + Steam + Room (caché offline)
│       ├── FirestoreGameParser.kt     ← Parser genérico documentos Firestore
│       ├── IgdbCatalogSyncService.kt  ← Sincronización catálogo IGDB→Firestore
│       └── SettingsRepository.kt     ← Preferencias de usuario en Firestore
├── ui/
│   ├── AppViewModelProvider.kt        ← Factory para los 9 ViewModels
│   ├── MainContainerScreen.kt         ← NavHost principal + BottomBar
│   ├── auth/        (AuthViewModel, OnboardingViewModel, pantallas)
│   ├── community/   (CommunityViewModel, pantallas, tabs, modales)
│   ├── detail/      (GameDetailViewModel, modal, header, reviews)
│   ├── discover/    (DiscoverViewModel, filtros, tarjetas)
│   ├── favorites/   (FavoritesViewModel)
│   ├── profile/     (ProfileViewModel, secciones, diálogos)
│   ├── settings/    (AppSettingsViewModel)
│   ├── swipe/       (SwipeViewModel, pantalla, botones)
│   └── components/  (SearchBarField, StarRatingRow, PreferenceCards…)
└── utils/
    ├── GenreUtils.kt
    ├── ValidationUtils.kt
    ├── LocaleUtils.kt
    ├── SortOption.kt
    └── UiText.kt
```

### Flujo de datos MVVM

```
UI (Composable)
   │  collectAsStateWithLifecycle()
   ▼
ViewModel (StateFlow<UiState>)
   │  viewModelScope.launch { Dispatchers.IO }
   ▼
Repository
   ├── Room (caché local)       ← GameRepository
   ├── Firestore (nube)         ← AuthRepository, CommunityRepository, SettingsRepository
   └── Retrofit (REST)          ← IGDB API, Steam API
```

---

## 5. Tests y Cobertura

**98 tests unitarios · 0 fallos · 0 omitidos**

| Clase de test | Tipo | Módulo | Nº tests |
|---|---|---|---|
| `AuthViewModelTest` | Unitario | ui/auth | 5 |
| `OnboardingViewModelTest` | Unitario | ui/auth | 5 |
| `CommunityViewModelTest` | Unitario | ui/community | 10 |
| `GameDetailViewModelTest` | Unitario | ui/detail | ~20 |
| `DiscoverViewModelTest` | Unitario | ui/discover | ~18 |
| `FavoritesViewModelTest` | Unitario | ui/favorites | ~10 |
| `AppSettingsViewModelTest` | Unitario | ui/settings | ~10 |
| `GameRepositoryTest` | Unitario | data/repository | ~20 |
| `SettingsRepositoryTest` | Unitario | data/repository | ~10 |
| `GameDaoTest` | Instrumentado | data/local | ~6 |

**Cobertura JaCoCo (última ejecución):**

| Métrica | Cubiertas | Total | % |
|---|---|---|---|
| LINE | 1 267 | 2 861 | 44.29% |
| INSTRUCTION | 8 998 | 23 844 | 37.74% |
| BRANCH | 560 | 1 389 | 40.32% |

Informe HTML disponible en: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

---

## 6. Documentación técnica

| # | Documento | Descripción |
|---|---|---|
| 1 | [Arquitectura MVVM y Navegación](docs/01-arquitectura-mvvm-y-navegacion.md) | Capas, `AppContainer`, `NavHost`, flujo UDF |
| 2 | [Corrutinas, Flujos y Asincronía](docs/02-corrutinas-flujos-y-asincronia.md) | `viewModelScope`, `StateFlow`, `Dispatchers`, `MainDispatcherRule` |
| 3 | [Persistencia Room y APIs REST](docs/03-persistencia-room-y-apis-rest.md) | `AppDatabase`, `GameDao`, `RetrofitClient`, interceptores, mappers |
| 4 | [Patrones de Diseño y Algoritmos](docs/04-patrones-de-diseno-y-algoritmos.md) | Repository, DI Container, Mapper/DTO, Observer, algoritmos de filtrado |
| 5 | [Calidad, SonarQube y Testing](docs/05-calidad-sonarqube-y-testing.md) | Reglas SonarQube, JaCoCo, MockK, `MainDispatcherRule`, todos los tests |
| 6 | [DevOps, GitHub Actions y Despliegue](docs/06-devops-github-actions-y-despliegue.md) | Pipeline CI, Docker SonarQube, secrets, generación APK |

---

## 7. APIs, Librerías e IA

### APIs externas

| API | Uso | Autenticación |
|---|---|---|
| [IGDB API v4](https://api-docs.igdb.com/) | Catálogo de videojuegos (búsqueda, metadatos, portadas) | OAuth2 Twitch (`Client Credentials`) |
| [Twitch Auth](https://dev.twitch.tv/docs/authentication/) | Token de acceso para IGDB | `client_id` + `client_secret` → Bearer token |
| [Steam Store API](https://store.steampowered.com/api/appdetails) | Detalles adicionales de juegos Steam (precio, descripción) | Sin autenticación (pública) |
| [Firebase Auth](https://firebase.google.com/docs/auth) | Registro e inicio de sesión con email/contraseña | `google-services.json` |
| [Cloud Firestore](https://firebase.google.com/docs/firestore) | Almacenamiento de usuarios, salas, favoritos y reseñas | Firebase SDK |

### Librerías principales

| Librería | Versión | Propósito |
|---|---|---|
| Jetpack Compose + Material3 | BOM 2024.x | UI declarativa |
| Room | 2.6+ | Base de datos local SQLite |
| Retrofit 2 + OkHttp | 2.11+ / 4.x | Clientes REST |
| Kotlin Coroutines | 1.8+ | Asincronía |
| Firebase BOM | 33.x | Auth + Firestore |
| Coil Compose | 2.x | Carga de imágenes asíncrona |
| MockK | 1.13+ | Mocking en tests unitarios |
| JaCoCo | 0.8.x | Cobertura de código |
| SonarQube Gradle Plugin | 4.x | Análisis estático |

### Declaración de uso de IA

Este proyecto utiliza herramientas de inteligencia artificial generativa (Antigravity / Google Gemini) como asistente de desarrollo para: generación de código boilerplate, refactorización guiada por reglas, generación de tests unitarios y redacción de documentación técnica. Todo el código generado ha sido revisado, validado y adaptado por los autores.

---

<div align="center">
  <sub>Desarrollado por <a href="https://github.com/studies-ulpgc">Dácil Santana Ortega</a> y <a href="https://github.com/Naiara262005">Naiara Díaz Hernández</a> · PAMN · ULPGC · 2024-2025</sub>
</div>
