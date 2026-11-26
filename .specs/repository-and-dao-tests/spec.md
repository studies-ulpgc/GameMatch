# Especificación: Tests Unitarios de Repositorios y Tests Instrumentados de `GameDao`

## 1. Objetivo Técnico
Añadir cobertura de tests unitarios con `MockK` y `kotlinx-coroutines-test` para [`GameRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt) y [`SettingsRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/SettingsRepository.kt), e implementar el test instrumentado de persistencia Room [`GameDaoTest`](../../app/src/androidTest/java/com/pamn/gamematch/data/local/GameDaoTest.kt) usando `Room.inMemoryDatabaseBuilder`.

## 2. Alcance Exacto

### Incluido
- Creación de `app/src/test/java/com/pamn/gamematch/data/repository/SettingsRepositoryTest.kt` cubriendo `getSettings`, `saveDarkMode` y `saveLanguage` (usuario nulo, documento existente/inexistente, fallback `darkMode`, normalización de idioma y fallos de Firestore).
- Creación de `app/src/test/java/com/pamn/gamematch/data/repository/GameRepositoryTest.kt` cubriendo `getSteamPrice`, `getGamesCatalog` (caché Room válida, caché caducada con refresco desde Firestore + `upsertAll`, fallback a Room ante fallo de Firestore, filtros por `searchQuery` y `genres`), `saveLikedGame` (éxito y fallback `set`), `removeLikedGame`, `getLikedGames`, `getUserPreferences`, `saveUserPreferences` y CRUD de reseñas (`getReviewsForGame`, `addReviewForGame`, `updateReview`, `deleteReview`).
- Creación de `app/src/androidTest/java/com/pamn/gamematch/data/local/GameDaoTest.kt` usando `Room.inMemoryDatabaseBuilder` con `@Before`/`@After` (`db.close()`), verificando `upsertAll`, `getAll`, reemplazo `OnConflictStrategy.REPLACE`, conversión de `platforms: List<String>`, `count` y `clear`.
- Verificación de compilación de `compileDebugAndroidTestKotlin` y ejecución de `.\gradlew.bat testDebugUnitTest jacocoTestReport`, reportando el nuevo porcentaje global de cobertura.

### Excluido
- Modificación de ficheros de producción en `app/src/main/java/` o dependencias en `app/build.gradle.kts`.
- Tests de `AuthRepository`, `CommunityRepository` o tests Compose UI en `androidTest`.

## 3. Archivos a Crear

| Archivo | Componente Cubierto | Tipo de Test |
| --- | --- | --- |
| `app/src/test/java/com/pamn/gamematch/data/repository/SettingsRepositoryTest.kt` | [`SettingsRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/SettingsRepository.kt) | Unitario (`JUnit 4`, `MockK`) |
| `app/src/test/java/com/pamn/gamematch/data/repository/GameRepositoryTest.kt` | [`GameRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt) | Unitario (`JUnit 4`, `MockK`, `kotlinx-coroutines-test`) |
| `app/src/androidTest/java/com/pamn/gamematch/data/local/GameDaoTest.kt` | [`GameDao`](../../app/src/main/java/com/pamn/gamematch/data/local/GameDao.kt) y [`AppDatabase`](../../app/src/main/java/com/pamn/gamematch/data/local/AppDatabase.kt) | Instrumentado (`AndroidJUnit4`, `Room` en memoria) |

## 4. Criterios de Aceptación
1. Ningún fichero `.kt` creado supera las **250 líneas** (`AGENTS.md`).
2. `SettingsRepositoryTest` y `GameRepositoryTest` incluyen escenarios de éxito, datos nulos/vacíos y errores de red/Firestore.
3. `GameDaoTest` compila con `.\gradlew.bat compileDebugAndroidTestKotlin`, usa `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries().build()` y cierra la base de datos en `@After`.
4. `.\gradlew.bat testDebugUnitTest jacocoTestReport` finaliza con éxito sin fallos y eleva el porcentaje global de cobertura en `jacocoTestReport.xml`.
