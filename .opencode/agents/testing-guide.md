# Guía de Testing — GameMatch (`src/test` y `src/androidTest`)

## 1. Inventario Actual del Proyecto
### Tests Unitarios existentes (`app/src/test/java/com/pamn/gamematch/`)
- **Mappers y Lógica Pura:** `ComputeFlagsTest.kt`, `GameEntityMapperTest.kt`, `GameMapperTest.kt`, `SteamPriceMapperTest.kt`, `GameRecommenderTest.kt`, `GameSorterTest.kt`, `GenreMatcherTest.kt`, `PlatformFamilyTest.kt`, `PriceFilterTest.kt`, `PriceFormatterTest.kt`, `ValidationUtilsTest.kt`.
- **ViewModels:** `AuthViewModelTest.kt`, `OnboardingViewModelTest.kt`, `CommunityViewModelTest.kt`, `ProfileViewModelTest.kt`, `SwipeViewModelTest.kt` (apoyados en `MainDispatcherRule.kt`).
- **Pendientes en `src/test`:** `GameDetailViewModelTest.kt`, `DiscoverViewModelTest.kt`, `FavoritesViewModelTest.kt`, `AppSettingsViewModelTest.kt`.

### Tests Instrumentados existentes (`app/src/androidTest/java/com/pamn/gamematch/`)
- Actualmente solo existe `ExampleInstrumentedTest.kt`. **Falta cobertura real de Room y Jetpack Compose.**

---

## 2. Protocolo de Auditoría y Creación de Tests

Cuando el usuario pida revisar o ampliar los tests, ejecuta este orden:

### Paso A: Auditoría de `src/test` (Sin romper nada existente)
1. Inspecciona los 16 ficheros actuales en `app/src/test/`.
2. Identifica cualquier clase de test que tenga **menos de 3 casos de prueba** o carezca de escenarios de error (excepciones de red, listas vacías, datos nulos).
3. Añade los casos faltantes manteniendo el estilo y estructura de los tests existentes.
4. Crea los tests unitarios para los ViewModels que aún no tienen cobertura (`GameDetailViewModelTest`, `DiscoverViewModelTest`, `FavoritesViewModelTest`, `AppSettingsViewModelTest`), mockeando los repositorios y usando `MainDispatcherRule`.

### Paso B: Creación de Tests Instrumentados en `src/androidTest`
1. **`GameDaoTest.kt` (Persistencia Local Room):**
    - Crea la base de datos en memoria en `@Before` con `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).allowMainThreadQueries().build()` y ciérrala en `@After` con `db.close()`.
    - Verifica: inserción de `GameEntity`, consulta reactiva mediante `Flow` (`first()`), actualización del estado de favorito, filtrado y borrado.
2. **`AuthAndNavigationUiTest.kt` y `SwipeScreenUiTest.kt` (Jetpack Compose UI):**
    - Usa `createComposeRule()`.
    - Verifica que los elementos principales se renderizan (`onNodeWithText`, `onNodeWithTag`), comprueba `contentDescription` para accesibilidad y valida interacciones básicas (clics en botones de login/onboarding y acciones de like/dislike en `GameCard`).

### Paso C: Verificación
- Ejecuta `./gradlew testDebugUnitTest` tras cada bloque de cambios.
- Asegúrate de que todos los tests pasan en verde antes de pasar a la siguiente tarea del `tasks.md`.