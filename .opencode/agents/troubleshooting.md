# Troubleshooting — Guía Rápida de Resolución de Problemas

## 1. Fallo de compilación por `BuildConfig` o claves API faltantes
- **Síntoma:** `Unresolved reference: IGDB_CLIENT_ID` o fallo en `RetrofitClient` / `IgdbTokenProvider`.
- **Solución:** Comprueba que `local.properties` existe en la raíz del proyecto con las claves definidas y que `buildFeatures { buildConfig = true }` está activo en `app/build.gradle.kts`.

## 2. Corrutinas que fallan o se quedan colgadas en `src/test`
- **Síntoma:** `IllegalStateException: Module with the Main dispatcher had failed to initialize` o tests intermitentes.
- **Solución:** Añade `@get:Rule val mainDispatcherRule = MainDispatcherRule()` en la clase de test y asegúrate de avanzar el reloj virtual con `advanceUntilIdle()` tras llamar a funciones del `ViewModel`.

## 3. SonarQube marca 0% de cobertura en Kotlin
- **Síntoma:** El panel en `localhost:9000` muestra los code smells pero `Coverage: 0.0%`.
- **Solución:** Verifica que `sonar.java.binaries` apunta a `app/build/tmp/kotlin-classes/debug` y que `jacocoTestReport` tiene `xml.required.set(true)` ejecutándose **antes** de la tarea `sonar`.

## 4. Error en `connectedDebugAndroidTest` con Room
- **Síntoma:** `Cannot access database on the main thread` o datos residuales entre tests.
- **Solución:** Usa `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries().build()` en `@Before` y llama obligatoriamente a `database.close()` en `@After`.

## 5. Clase Compose rechazada por exceso de líneas (>250)
- **Síntoma:** Pantalla con demasiados componentes anidados y alta complejidad.
- **Solución:** Extrae cada tarjeta, diálogo o barra superior a su propio archivo `@Composable` stateless en `ui/components/` y pasa únicamente los datos primitivos o el `UiState` necesario.