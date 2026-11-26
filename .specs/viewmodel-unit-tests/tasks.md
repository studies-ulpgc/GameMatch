# Tareas: Tests Unitarios de ViewModels

- [x] **Tarea 1: Crear `AppSettingsViewModelTest.kt` y `FavoritesViewModelTest.kt`**
  - Implementar `app/src/test/java/com/pamn/gamematch/ui/settings/AppSettingsViewModelTest.kt` (carga con datos/nulos, toggle modo oscuro con y sin guardado en Firestore, cambio y normalización de idioma + `LocaleManager`).
  - Implementar `app/src/test/java/com/pamn/gamematch/ui/favorites/FavoritesViewModelTest.kt` (éxito, vacío, fallo con mensaje dinámico y recurso por defecto, `removeFavorite` y `addFavorite` en éxito/fallo).
  - Verificación: `.\gradlew.bat testDebugUnitTest --tests "com.pamn.gamematch.ui.settings.AppSettingsViewModelTest" --tests "com.pamn.gamematch.ui.favorites.FavoritesViewModelTest"`

- [x] **Tarea 2: Crear `GameDetailViewModelTest.kt`**
  - Implementar `app/src/test/java/com/pamn/gamematch/ui/detail/GameDetailViewModelTest.kt` cubriendo `loadSteamPrice` (nulo, éxito y fallo), `loadReviewsForGame` (éxito con media y `userReview`, fallo), `submitReview`, `updateReview`, `deleteReview` (validación en blanco, éxito con recarga y fallo) y `clearMessages`, manteniendo el fichero `<= 250` líneas.
  - Verificación: `.\gradlew.bat testDebugUnitTest --tests "com.pamn.gamematch.ui.detail.GameDetailViewModelTest"`

- [x] **Tarea 3: Crear `DiscoverViewModelTest.kt`**
  - Implementar `app/src/test/java/com/pamn/gamematch/ui/discover/DiscoverViewModelTest.kt` cubriendo `loadData` (éxito y error de favoritos), `onSearchQueryChanged`, `onGenreSelected` (multi-selección y vuelta a `"Todas"`), `onMaxPriceChanged`, `onSortSelected`, `toggleFavorite` (añadir/quitar) y `matchesPriceFilter`, manteniendo el fichero `<= 250` líneas.
  - Verificación: `.\gradlew.bat testDebugUnitTest --tests "com.pamn.gamematch.ui.discover.DiscoverViewModelTest"`

- [x] **Tarea 4: Verificación completa, conteo de líneas y reporte JaCoCo**
  - Comprobar que ninguno de los nuevos `.kt` supera las 250 líneas y ejecutar la suite completa con JaCoCo para verificar que todos los tests compilan, pasan y actualizan la cobertura.
  - Verificación: `.\gradlew.bat testDebugUnitTest jacocoTestReport`
