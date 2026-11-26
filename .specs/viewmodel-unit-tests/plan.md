# Plan Técnico: Tests Unitarios de ViewModels

## 1. Análisis de los ViewModels y Dependencias
- **[`AppSettingsViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/settings/AppSettingsViewModel.kt):** recibe `SettingsRepository` y `LocaleManager`. Tanto `AppSettingsUiState()` inicial como `applyLocaleIfNeeded()` invocan `AppCompatDelegate.getApplicationLocales()`, por lo que en JVM local (`src/test`) se usará `mockkStatic(AppCompatDelegate::class)` devolviendo `LocaleListCompat.getEmptyLocaleList()` y `unmockkStatic(AppCompatDelegate::class)` en `@After` para mantener aislamiento entre tests.
- **[`FavoritesViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/favorites/FavoritesViewModel.kt):** recibe `GameRepository`; combina llamada `suspend` (`getGamesCatalog()`) dentro de `viewModelScope.launch` con callbacks (`getLikedGames`, `removeLikedGame`, `saveLikedGame`). Se usará `MainDispatcherRule` (`UnconfinedTestDispatcher`) y `coEvery` / `every` con `slot` de MockK.
- **[`GameDetailViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/detail/GameDetailViewModel.kt):** recibe `GameRepository` y `AuthRepository`; combina corrutina `suspend` (`getSteamPrice`) con callbacks CRUD de reseñas (`getReviewsForGame`, `addReviewForGame`, `updateReview`, `deleteReview`) y validaciones de comentario vacío.
- **[`DiscoverViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/discover/DiscoverViewModel.kt):** recibe `GameRepository`; carga catálogo (`getGamesCatalog(pageSize = 500)`) y favoritos (`getLikedGames`), calcula top 8 géneros excluyendo `"Varios"`, ordena `featured`/`trending` y filtra/ordena en memoria por búsqueda, multi-selección de géneros, precio máximo y `SortOption`.

## 2. Diseño de Suites (Respetando <= 250 líneas por archivo)
- **Convenciones:** replicar la estructura de [`ProfileViewModelTest.kt`](../../app/src/test/java/com/pamn/gamematch/ui/profile/ProfileViewModelTest.kt) y [`SwipeViewModelTest.kt`](../../app/src/test/java/com/pamn/gamematch/ui/swipe/SwipeViewModelTest.kt) usando `@get:Rule val mainDispatcherRule = MainDispatcherRule()`, `runTest` cuando haya corrutinas y aserciones claras sobre `uiState.value` y `verify`/`coVerify`.
- **Control de tamaño:**
  - `AppSettingsViewModelTest.kt`: ~115 líneas (5 tests).
  - `FavoritesViewModelTest.kt`: ~145 líneas (6 tests).
  - `GameDetailViewModelTest.kt`: ~215 líneas (9 tests cubriendo Steam price, carga/media/autoría de reseñas, submit/update/delete, validación y `clearMessages`).
  - `DiscoverViewModelTest.kt`: ~195 líneas (7 tests cubriendo carga, error en favoritos, búsqueda, multi-selección de géneros, precio/ordenación, `toggleFavorite` y `matchesPriceFilter`).
- Cada fichero usará sus propios datos de prueba locales sin modificar harnesses compartidos.

## 3. Verificación
- Conteo de líneas de los 4 ficheros para asegurar `<= 250` líneas en todos.
- Ejecución de `.\gradlew.bat testDebugUnitTest jacocoTestReport` y comprobación de cobertura resultante en `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`.
