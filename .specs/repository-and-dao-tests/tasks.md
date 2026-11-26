# Tareas: Tests de Repositorios y `GameDaoTest`

- [x] **Tarea 1: Crear `SettingsRepositoryTest.kt`**
  - Implementar `app/src/test/java/com/pamn/gamematch/data/repository/SettingsRepositoryTest.kt` cubriendo `getSettings` (sin usuario, doc existente con `darkMode` fallback, doc inexistente, error), `saveDarkMode` y `saveLanguage` (sin usuario, éxito y fallo).
  - Verificación: `.\gradlew.bat testDebugUnitTest --tests "com.pamn.gamematch.data.repository.SettingsRepositoryTest"`

- [x] **Tarea 2: Crear `GameRepositoryTest.kt`**
  - Implementar `app/src/test/java/com/pamn/gamematch/data/repository/GameRepositoryTest.kt` (`<= 250` líneas) cubriendo `getSteamPrice` (lote, F2P y fallo), `getGamesCatalog` (caché Room válida, refresco Firestore + `upsertAll`, fallback Room, filtros), favoritos (`saveLikedGame`, `removeLikedGame`, `getLikedGames`), preferencias (`getUserPreferences`, `saveUserPreferences`) y reseñas (`getReviewsForGame`, `addReviewForGame`, `updateReview`, `deleteReview`).
  - Verificación: `.\gradlew.bat testDebugUnitTest --tests "com.pamn.gamematch.data.repository.GameRepositoryTest"`

- [x] **Tarea 3: Crear `GameDaoTest.kt` en `androidTest`**
  - Implementar `app/src/androidTest/java/com/pamn/gamematch/data/local/GameDaoTest.kt` con `Room.inMemoryDatabaseBuilder`, `@Before`/`@After` (`db.close()`), pruebas de `upsertAll`, `getAll`, conflicto `REPLACE`, `PlatformsConverter`, `count` y `clear`.
  - Verificación: `.\gradlew.bat compileDebugAndroidTestKotlin`

- [x] **Tarea 4: Verificar límite de líneas, ejecutar suite completa y obtener nuevo porcentaje global JaCoCo**
  - Comprobar que los tres archivos tienen `<= 250` líneas, ejecutar `.\gradlew.bat testDebugUnitTest jacocoTestReport` y leer los contadores globales actualizados en `jacocoTestReport.xml`.
  - Verificación: `.\gradlew.bat testDebugUnitTest jacocoTestReport`
