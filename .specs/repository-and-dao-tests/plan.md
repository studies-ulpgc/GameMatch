# Plan Técnico: Tests de Repositorios y `GameDaoTest`

## 1. Análisis Técnico de Dependencias y Entorno
- **[`SettingsRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/SettingsRepository.kt):** depende de `FirebaseFirestore` y `FirebaseAuth` sobre `Task<DocumentSnapshot>` y `Task<Void>`. Se crearán helpers locales privados para simular `Task` exitosa/fallida con `addOnSuccessListener` y `addOnFailureListener`.
- **[`GameRepository`](../../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt):**
  - Invoca `android.util.Log` (`Log.i`, `Log.w`, `Log.e`), por lo que en JVM local se aplicará `mockkStatic(Log::class)` en `@Before` y `unmockkStatic(Log::class)` en `@After`.
  - Para `getSteamPrice`, usa `RetrofitClient.steamApiService`: se aplicará `mockkObject(RetrofitClient)` en `@Before` y `unmockkObject(RetrofitClient)` en `@After` para simular `SteamApiService` sin llamadas reales.
  - Para `getGamesCatalog`, combina `GameDao` (`suspend`) con `db.collection(...).limit(...).get().await()` (`kotlinx-coroutines-play-services`): la `Task<QuerySnapshot>` mockeada devolverá `isComplete = true`, `isSuccessful = true`, `isCanceled = false`, `exception = null` y `result = querySnapshot` (ruta rápida de `Task.await()`).
  - Para métodos callback de Firestore (`saveLikedGame`, `removeLikedGame`, `getLikedGames`, `getUserPreferences`, `saveUserPreferences`, `getReviewsForGame`, `addReviewForGame`, `updateReview`, `deleteReview`), se reutilizarán helpers locales que capturen `OnSuccessListener` / `OnFailureListener`.
- **[`GameDao`](../../app/src/main/java/com/pamn/gamematch/data/local/GameDao.kt):** define exclusivamente funciones `suspend` (`getAll`, `upsertAll`, `count`, `clear`) sobre `GameEntity` (con `PlatformsConverter`). En [`app/build.gradle.kts`](../../app/build.gradle.kts) `kotlinx-coroutines-test` solo está declarado en `testImplementation`, no en `androidTestImplementation`, por lo que `GameDaoTest` usará `kotlinx.coroutines.runBlocking` junto con `ApplicationProvider.getApplicationContext()` y `AndroidJUnit4`.

## 2. Control de Tamaño (<= 250 líneas por fichero)
- `SettingsRepositoryTest.kt`: ~145 líneas.
- `GameRepositoryTest.kt`: ~235 líneas (usando helpers compactos para `Task` y `DocumentSnapshot` / `QuerySnapshot` para mantenerse por debajo de 250 líneas sin duplicar mocks).
- `GameDaoTest.kt`: ~115 líneas.

## 3. Verificación
- Comprobación de conteo de líneas (`<= 250`) en los tres ficheros.
- Compilación de `GameDaoTest`: `.\gradlew.bat compileDebugAndroidTestKotlin`.
- Ejecución completa de unit tests + JaCoCo y lectura del nuevo porcentaje global en `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`: `.\gradlew.bat testDebugUnitTest jacocoTestReport`.
