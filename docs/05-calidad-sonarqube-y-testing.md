[← Volver al README](../README.md)

# 05 — Calidad, SonarQube y Testing

## Tabla de contenidos
1. [Reglas de calidad SonarQube](#1-reglas-de-calidad-sonarqube)
2. [JaCoCo — cobertura de código](#2-jacoco--cobertura-de-código)
3. [Catálogo completo de tests](#3-catálogo-completo-de-tests)
4. [Herramientas de testing](#4-herramientas-de-testing)
5. [MainDispatcherRule](#5-maindispatcherrule)
6. [Configuración JaCoCo en Gradle](#6-configuración-jacoco-en-gradle)
7. [Ejecutar SonarQube local con Docker](#7-ejecutar-sonarqube-local-con-docker)
8. [Trazabilidad](#8-trazabilidad)

---

## 1. Reglas de calidad SonarQube

El proyecto aplica las siguientes restricciones, verificadas en cada push mediante CI y localmente con SonarQube:

| Regla | Valor | Verificación |
|---|---|---|
| Líneas máximas por archivo `.kt` | **250 líneas** | SonarQube + revisión manual |
| Complejidad ciclomática | Alerta si > 10 | SonarQube |
| Duplicación de código | Alerta si > 3 % | SonarQube |
| Cobertura mínima de instrucciones | 30 % (objetivo 50 %) | JaCoCo |
| Secretos hardcodeados | 0 | SonarQube (regla RSPEC-6418) |

**Estado actual:** 0 archivos Kotlin > 250 líneas (el mayor es `GameRepository.kt` con 243 líneas).

---

## 2. JaCoCo — cobertura de código

Métricas tras la última ejecución de `./gradlew testDebugUnitTest jacocoTestReport`:

| Métrica | Cubiertas | Total | Porcentaje |
|---|---|---|---|
| **LINE** | 1 267 | 2 861 | **44.29 %** |
| **INSTRUCTION** | 8 998 | 23 844 | **37.74 %** |
| **BRANCH** | 560 | 1 389 | **40.32 %** |

Informe HTML: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

---

## 3. Catálogo completo de tests

### Tests unitarios — `app/src/test/`

| Clase | Módulo | Nº tests | Herramientas |
|---|---|---|---|
| `AuthViewModelTest` | ui/auth | 5 | MockK, runTest |
| `OnboardingViewModelTest` | ui/auth | 5 | MockK, runTest |
| `CommunityViewModelTest` | ui/community | 10 | MockK, runTest |
| `GameDetailViewModelTest` | ui/detail | ~20 | MockK, runTest |
| `DiscoverViewModelTest` | ui/discover | ~18 | MockK, runTest |
| `FavoritesViewModelTest` | ui/favorites | ~10 | MockK, runTest |
| `AppSettingsViewModelTest` | ui/settings | ~10 | MockK, runTest |
| `GameRepositoryTest` | data/repository | ~20 | MockK, runTest |
| `SettingsRepositoryTest` | data/repository | ~10 | MockK, runTest |

**Total: 98 tests unitarios · 0 fallos · 0 omitidos**

### Tests instrumentados — `app/src/androidTest/`

| Clase | Módulo | Nº tests | Herramientas |
|---|---|---|---|
| `GameDaoTest` | data/local | ~6 | Room in-memory, runTest |

Estos tests requieren un emulador o dispositivo conectado:
```bash
./gradlew connectedDebugAndroidTest
```

---

## 4. Herramientas de testing

### MockK

MockK es la librería de mocking nativa de Kotlin. Se usa en todos los tests unitarios para simular repositorios:

```kotlin
// Setup
private val mockGameRepository = mockk<GameRepository>(relaxed = true)

// Stubbing
coEvery { mockGameRepository.getFavorites() } returns flowOf(testGames)

// Verificación
coVerify(exactly = 1) { mockGameRepository.addFavorite(testGame) }
```

`relaxed = true` permite que los métodos no stubbeados devuelvan valores por defecto en lugar de lanzar excepciones.

### kotlinx-coroutines-test

```kotlin
@Test
fun `loadGames emits loading then success`() = runTest {
    val games = listOf(Game(id = "1", title = "Hades"))
    coEvery { mockGameRepository.getGames() } returns games

    viewModel.loadGames()
    advanceUntilIdle()   // ejecuta todas las corrutinas pendientes

    assertEquals(games, viewModel.uiState.value.games)
    assertFalse(viewModel.uiState.value.isLoading)
}
```

---

## 5. MainDispatcherRule

Regla JUnit4 que sustituye `Dispatchers.Main` por un `TestCoroutineDispatcher` durante los tests:

```kotlin
class MainDispatcherRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
```

Sin esta regla, `viewModelScope.launch` lanzaría `IllegalStateException` en tests JVM al no existir el looper de Main.

---

## 6. Configuración JaCoCo en Gradle

[`app/build.gradle.kts`](../app/build.gradle.kts) configura JaCoCo para excluir código generado:

```kotlin
// Fragmento simplificado de app/build.gradle.kts
tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)   // Para SonarQube
        html.required.set(true)  // Para revisión visual
    }
    classDirectories.setFrom(
        fileTree("$buildDir/tmp/kotlin-classes/debug") {
            exclude(
                "**/R.class", "**/R\$*.class",
                "**/BuildConfig.*", "**/Manifest*.*",
                "**/*_Factory*", "**/*_Impl*",   // Hilt/Room generados
                "**/*ComposableSingletons*"       // Compose generados
            )
        }
    )
}
```

---

## 7. Ejecutar SonarQube local con Docker

```bash
# 1. Levantar SonarQube
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community

# 2. Configurar sonar-project.properties o sonar{} en build.gradle.kts
#    - sonar.host.url=http://localhost:9000
#    - sonar.login=<token generado en la UI>

# 3. Ejecutar análisis completo
./gradlew testDebugUnitTest jacocoTestReport sonar

# 4. Ver resultados en http://localhost:9000
```

El token de SonarQube se pasa como variable de entorno o en `gradle.properties` **fuera del repositorio**.

---

## 8. Trazabilidad

| Elemento | Archivo |
|---|---|
| `MainDispatcherRule` | [`MainDispatcherRule.kt`](../app/src/test/java/com/pamn/gamematch/ui/MainDispatcherRule.kt) |
| Configuración JaCoCo | [`app/build.gradle.kts`](../app/build.gradle.kts) |
| Plugin SonarQube | [`app/build.gradle.kts`](../app/build.gradle.kts) línea 10 |
| `AuthViewModelTest` | [`app/src/test/.../AuthViewModelTest.kt`](../app/src/test/java/com/pamn/gamematch/ui/auth/AuthViewModelTest.kt) |
| `CommunityViewModelTest` | [`app/src/test/.../CommunityViewModelTest.kt`](../app/src/test/java/com/pamn/gamematch/ui/community/CommunityViewModelTest.kt) |
| `GameRepositoryTest` | [`app/src/test/.../GameRepositoryTest.kt`](../app/src/test/java/com/pamn/gamematch/data/repository/GameRepositoryTest.kt) |
| `GameDaoTest` | [`app/src/androidTest/.../GameDaoTest.kt`](../app/src/androidTest/java/com/pamn/gamematch/data/local/GameDaoTest.kt) |
