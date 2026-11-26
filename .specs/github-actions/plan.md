# Plan Técnico: Pipeline CI en GitHub Actions (`android-ci.yml`)

## 1. Análisis del Estado Actual en el Repositorio
- **Build y Gradle:** Gradle Wrapper `9.6.0` ([`gradle-wrapper.properties`](../../gradle/wrapper/gradle-wrapper.properties)), AGP `9.4.1` y Kotlin `2.2.10` ([`libs.versions.toml`](../../gradle/libs.versions.toml)). Tanto Gradle `9.6.0` como AGP `9.4.1` soportan **JDK 17** como versión base de ejecución, coincidiendo con `java-version: '17'` especificado en [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md).
- **Credenciales y Google Services:** [`app/build.gradle.kts`](../../app/build.gradle.kts) lee `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET` desde `local.properties` en `defaultConfig` con respaldo vacío si no existen; creando un `local.properties` ficticio en CI se compila `BuildConfig` sin exponer credenciales reales. [`app/google-services.json`](../../app/google-services.json) ya está versionado en Git (`100644`), por lo que `processDebugGoogleServices` funciona en CI sin pasos extra.
- **Permisos de `gradlew` en Git:** se comprobó con `git ls-files --stage gradlew` que [`gradlew`](../../gradlew) tiene actualmente modo `100644` (no ejecutable). En `ubuntu-latest`, invocar `./gradlew` directamente fallaría con `Permission denied` si no se añade permiso de ejecución (`chmod +x gradlew` y/o `git update-index --chmod=+x gradlew`).
- **Rutas de salida actuales:**
  - Tests HTML/XML: `app/build/reports/tests/`
  - Cobertura JaCoCo (XML/HTML): `app/build/reports/jacoco/jacocoTestReport/` (configurado en `.specs/devops-sonarqube/`).
  - APK Debug: `app/build/outputs/apk/debug/app-debug.apk`.

## 2. Arquitectura del Workflow (`.github/workflows/android-ci.yml`)

### 2.1. Triggers y Permisos
- Nombre del workflow: `Android CI`.
- Triggers:
  - `push` en ramas `[main, develop]`.
  - `pull_request` hacia ramas `[main, develop]`.
- Permisos mínimos (`permissions: contents: read`) por principio de mínimo privilegio.

### 2.2. Job `build-and-test` (`ubuntu-latest`)
Secuencia ordenada de pasos:
1. **`Checkout repository`:** `actions/checkout@v4`.
2. **`Set up JDK 17`:** `actions/setup-java@v4` con `distribution: 'temurin'` y `java-version: '17'`.
3. **`Setup Gradle`:** `gradle/actions/setup-gradle@v4` para cachear dependencias y estado de Gradle entre ejecuciones.
4. **`Create dummy local.properties`:** escribir en `local.properties` las claves ficticias `IGDB_CLIENT_ID=dummy_id` e `IGDB_CLIENT_SECRET=dummy_secret` (en `ubuntu-latest`, el SDK Android se resuelve automáticamente mediante `ANDROID_HOME` preinstalado en el runner).
5. **`Run tests, coverage, lint and assemble debug APK`:** asegurar permiso de ejecución (`chmod +x gradlew`) y ejecutar:
   ```bash
   ./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug
   ```
6. **`Upload test and coverage reports` (`if: always()`):** `actions/upload-artifact@v4` subiendo `app/build/reports/tests/` y `app/build/reports/jacoco/jacocoTestReport/` (por ejemplo en artefactos separados `test-reports` y `jacoco-coverage-report` o agrupados bajo `reports`, con retención estándar) para poder inspeccionar fallos de tests o cobertura incluso si un paso posterior falla.
7. **`Upload debug APK` (`if: always()`):** `actions/upload-artifact@v4` subiendo `app/build/outputs/apk/debug/app-debug.apk` bajo `app-debug-apk` (`if-no-files-found: warn` o `ignore` si falló antes de empaquetar, de modo que un fallo previo en tests/lint no quede ocultado por un error de subida).

## 3. Restricciones y Riesgos Mitigados
- **Límite de 250 líneas en `.kt`:** No se modifica ni crea ningún fichero `.kt`.
- **Cero secretos:** El workflow utiliza exclusivamente valores `dummy_id` / `dummy_secret` en `local.properties` y no referencia secretos ni tokens de SonarQube.
- **Riesgo de `lintDebug` en el estado actual del repo:** Antes de dar por cerrada la configuración del CI, la Tarea 1 ejecutará `.\gradlew.bat testDebugUnitTest jacocoTestReport lintDebug assembleDebug` localmente para comprobar que `lintDebug` no falla por errores previos en el código existente y que todas las rutas de artefactos existen exactamente donde apunta el YAML.
- **Riesgo de `Permission denied` en `./gradlew` en Ubuntu:** Mitigado tanto fijando `+x` en el índice Git (`100755`) como incluyendo `chmod +x gradlew` en el paso de ejecución del workflow.
