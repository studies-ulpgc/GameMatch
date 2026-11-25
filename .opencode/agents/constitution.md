# Constitution — Arquitectura, Refactorización y Calidad SonarQube

## 1. Arquitectura MVVM y Fronteras de Capa
Respeta estrictamente la separación de paquetes existente en `com.pamn.gamematch`:
- **`data/local/` (`AppDatabase`, `GameDao`, `GameEntity`, `PlatformsConverter`):** Solo persistencia Room. Nunca pases `GameEntity` a los ViewModels o pantallas Compose; transforma siempre usando `GameEntityMapper`.
- **`data/remote/` (`RetrofitClient`, `IgdbApiService`, `SteamApiService`, `TwitchAuthApiService`, `IgdbAuthInterceptor`, `IgdbTokenProvider`):** Solo red, serialización DTO (`IgdbDtos`) y mapeo (`GameMapper`, `SteamPriceMapper`, `ComputeFlags`).
- **`data/repository/` (`AuthRepository`, `CommunityRepository`, `GameRepository`, `SettingsRepository`):** Única fuente de verdad que coordina caché local (Room) y llamadas remotas (Retrofit).
- **`data/container/` (`AppContainer`):** Inyección de dependencias manual. Todo nuevo servicio, sensor o repositorio debe registrarse aquí.
- **`ui/<feature>/` (`*Screen.kt`, `*ViewModel.kt`):**
    - Los `*Screen.kt` son funciones `@Composable` sin estado de negocio propio; reciben `uiState` inmutable y emiten lambdas de eventos (`onLike`, `onRefresh`, `onSearch`).
    - Los `*ViewModel.kt` exponen un único `StateFlow<UiState>` inmutable mediante `.asStateFlow()`.

## 2. Reglas de Refactorización y SonarQube (Límite de Líneas)
1. **Regla Crítica de Tamaño:** Ningún fichero `.kt` puede superar las **250 líneas de código**. Si SonarQube o una auditoría detecta una clase larga (habitual en `SwipeScreen.kt`, `GameDetailModal.kt`, `DiscoverScreen.kt` o `ProfileScreen.kt`):
    - Extrae subcomponentes visuales reutilizables a `ui/components/` o a archivos hermanos dentro de su carpeta (`ui/detail/DetailHeader.kt`, `ui/detail/SteamPriceSection.kt`).
    - Extrae funciones de filtrado, formateo o cálculo a `utils/`.
2. **Complejidad Ciclomática:** Máximo 3 niveles de anidamiento (`if`/`when`/`for`). Usa retornos tempranos y expresiones `when` limpias.
3. **Corrutinas Testables:** Inyecta `CoroutineDispatcher = Dispatchers.IO` en los constructores de los repositorios en lugar de escribir `Dispatchers.IO` fijo dentro de los métodos, permitiendo usar `MainDispatcherRule` en los tests.
4. **Sensores e Integración Hardware (Temario Bloque 3):** Si se solicita integrar el acelerómetro (ej. gesto de agitar *shake-to-shuffle* en `SwipeScreen`), encapsúlalo en una clase dedicada en `utils/ShakeDetector.kt` gestionando `SensorManager` con `DisposableEffect` para liberar el listener en `onDispose` y evitar fugas de memoria.