# Guía de DevOps, CI/CD, JaCoCo y SonarQube

## 1. Configuración de SonarQube Local (Docker en puerto 9000)
El contenedor de SonarQube corre localmente en `http://localhost:9000`. Cuando el usuario pida configurar SonarQube:
1. Añade el plugin `id("org.sonarqube") version "5.1.0.4882"` en `build.gradle.kts` (raíz) y `jacoco` en `app/build.gradle.kts`.
2. Configura la tarea `jacocoTestReport` en `app/build.gradle.kts` para generar el reporte XML de los tests unitarios (`testDebugUnitTest`), excluyendo clases generadas por Android/Compose (`*Binding*`, `*_Impl*`, `ComposableSingletons*`, `ui/theme/*`).
3. Crea `sonar-project.properties` en la raíz con:
    - `sonar.projectKey=gamematch-pamn`
    - `sonar.projectName=GameMatch`
    - `sonar.host.url=http://localhost:9000`
    - `sonar.sources=app/src/main/java`
    - `sonar.tests=app/src/test/java,app/src/androidTest/java`
    - `sonar.java.binaries=app/build/tmp/kotlin-classes/debug`
    - `sonar.coverage.jacoco.xmlReportPaths=app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
4. **Seguridad:** Nunca escribas el token de SonarQube en `sonar-project.properties` ni en `build.gradle.kts`. Ejecútalo siempre pasando el parámetro por CLI:
   `./gradlew testDebugUnitTest jacocoTestReport sonar -Dsonar.token=TU_TOKEN`

## 2. Pipeline CI en GitHub Actions (`.github/workflows/android-ci.yml`)
Cuando el usuario pida crear o actualizar el workflow de GitHub Actions, genera `.github/workflows/android-ci.yml` con:
- **Triggers:** `push` y `pull_request` sobre `main` y `develop`.
- **Job `build-and-test` en `ubuntu-latest`:**
    1. `actions/checkout@v4`
    2. `actions/setup-java@v4` con `distribution: 'temurin'` y `java-version: '17'`.
    3. `gradle/actions/setup-gradle@v4` para cachear dependencias.
    4. Paso `Create dummy local.properties` que escriba claves de prueba (`IGDB_CLIENT_ID=dummy_id`, `IGDB_CLIENT_SECRET=dummy_secret`, etc.) para que Gradle compile `BuildConfig` en CI sin exponer credenciales reales.
    5. Ejecución de `./gradlew testDebugUnitTest lintDebug assembleDebug`.
    6. `actions/upload-artifact@v4` (con `if: always()`) subiendo los reportes HTML de tests (`app/build/reports/tests/`) y el APK de demostración (`app/build/outputs/apk/debug/app-debug.apk`).

## 3. Plantilla de Credenciales (`local.properties.example`)
Crea siempre en la raíz `local.properties.example` con las claves vacías o de ejemplo para que cualquier miembro del equipo o profesorado pueda compilar el proyecto.