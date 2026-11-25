# Plan Técnico: Configuración de `local.properties.example`, `sonar-project.properties` y JaCoCo

## 1. Análisis del Estado Actual
- **Herramientas de build verificadas:** Gradle Wrapper `9.6.0` ([`gradle-wrapper.properties`](../../gradle/wrapper/gradle-wrapper.properties)), Android Gradle Plugin `9.4.1` y Kotlin `2.2.10` ([`libs.versions.toml`](../../gradle/libs.versions.toml)), `compileSdk = 37`, `JavaVersion.VERSION_11` y `org.gradle.configuration-cache=true` en [`gradle.properties`](../../gradle.properties).
- **Gestión de propiedades actual:** [`app/build.gradle.kts`](../../app/build.gradle.kts) (105 líneas) carga `local.properties` en `defaultConfig` y lee `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET` (con respaldo a `project.findProperty(...)`) para generar `BuildConfig`. En [`local.properties`](../../local.properties) existen únicamente `sdk.dir`, `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET`. [`.gitignore`](../../.gitignore) ya excluye `local.properties`.
- **Plugins actuales:** [`build.gradle.kts`](../../build.gradle.kts) (7 líneas) declara `android.application`, `kotlin.compose`, `ksp` y `com.google.gms.google-services` con `apply false`.
- **Tests existentes:** 18 ficheros bajo `app/src/test/java/com/pamn/gamematch/` (mappers, utilidades y ViewModels).

## 2. Arquitectura de la Solución

### 2.1. Plantilla `local.properties.example`
- Crear `local.properties.example` en la raíz con comentarios breves de uso y las claves esperadas por [`app/build.gradle.kts`](../../app/build.gradle.kts):
  - `sdk.dir=/ruta/a/tu/Android/Sdk`
  - `IGDB_CLIENT_ID=tu_igdb_client_id`
  - `IGDB_CLIENT_SECRET=tu_igdb_client_secret`
- No se modificará la lógica de lectura en [`app/build.gradle.kts`](../../app/build.gradle.kts), manteniendo intacto el comportamiento actual de `BuildConfig`.

### 2.2. Configuración de JaCoCo en [`app/build.gradle.kts`](../../app/build.gradle.kts)
- Aplicar el plugin estándar de Gradle `jacoco` dentro de `plugins { ... }`.
- Registrar la tarea `jacocoTestReport` de tipo `JacocoReport` dependiendo de `testDebugUnitTest`:
  - `reports`: activar `xml.required.set(true)` (genera `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`) y `html.required.set(true)` para inspección local.
  - `sourceDirectories`: `files("$projectDir/src/main/java")`.
  - `classDirectories`: `fileTree` sobre `intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes` (ruta real de `built_in_kotlinc` en AGP `9.4.1`), `tmp/kotlin-classes/debug` (respaldo) e `intermediates/javac/debug/compileDebugJavaWithJavac/classes`, aplicando las exclusiones de [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md) y artefactos generados de Android:
    - `**/R.class`, `**/R$*.class`, `**/BuildConfig.*`, `**/Manifest*.*`, `**/*Test*.*`
    - `**/*Binding*.*`, `**/*_Impl*.*`, `**/ComposableSingletons*.*`, `**/ui/theme/**`
  - `executionData`: `fileTree(layout.buildDirectory)` apuntando a los archivos `.exec` generados por `testDebugUnitTest` (`jacoco/testDebugUnitTest.exec` y `outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`).
- Configurar en `tasks.withType<Test>()` las opciones necesarias de JaCoCo (`includeNoLocationClasses = true` y `excludes = listOf("jdk.internal.*")`) para evitar fallos de instrumentación en JDK recientes y con mocks de MockK/Corrutinas.
- Usar `layout.buildDirectory` en lugar de `buildDir` para mantener compatibilidad con Gradle `9.6.0` y `org.gradle.configuration-cache=true`.

### 2.3. Configuración de SonarQube (`build.gradle.kts` y `sonar-project.properties`)
- Crear `sonar-project.properties` en la raíz con las propiedades exactas indicadas en [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md):
  - `sonar.projectKey=gamematch-pamn`
  - `sonar.projectName=GameMatch`
  - `sonar.host.url=http://localhost:9000`
  - `sonar.sources=app/src/main/java`
  - `sonar.tests=app/src/test/java,app/src/androidTest/java`
  - `sonar.java.binaries=app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes,app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes`
  - `sonar.coverage.jacoco.xmlReportPaths=app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
- Añadir `id("org.sonarqube") version "7.6.0.18749"` en [`build.gradle.kts`](../../build.gradle.kts) (la versión `5.1.0.4882` falla en AGP `9.4.1` con `Extension of type 'AppExtension' does not exist`, eliminada en AGP 9).
- Se carga `sonar-project.properties` en el bloque `sonar { properties { ... } }` de [`build.gradle.kts`](../../build.gradle.kts) filtrando `sonar.token`, de modo que `./gradlew sonar` y `sonar-scanner` compartan una única fuente de verdad sin duplicar valores ni almacenar secretos.

## 3. Restricciones y Control de Calidad
- **Límite de 250 líneas:** No se modifica ni se crea ningún fichero `.kt`. [`build.gradle.kts`](../../build.gradle.kts) pasará de 7 a ~10–20 líneas y [`app/build.gradle.kts`](../../app/build.gradle.kts) de 105 a ~145 líneas.
- **Compatibilidad de tests:** No se modifican fuentes ni tests existentes; la suite `testDebugUnitTest` actual debe seguir pasando en su totalidad.
- **Seguridad:** Ningún secreto o token de SonarQube se escribirá en `sonar-project.properties`, `local.properties.example` ni en archivos Gradle.
- **Riesgos mitigados:**
  - *Cobertura 0% en SonarQube* ([`.opencode/agents/troubleshooting.md`](../../.opencode/agents/troubleshooting.md)): asegurado ejecutando `jacocoTestReport` antes que `sonar` y alineando `sonar.java.binaries` y `sonar.coverage.jacoco.xmlReportPaths` con `app/build/tmp/kotlin-classes/debug` y la ruta XML real.
  - *Incompatibilidad de versión de JaCoCo con Kotlin 2.2.10 / AGP 9.4.1*: si el JaCoCo embebido en Gradle `9.6.0` presenta errores de bytecode al ejecutar `jacocoTestReport`, se fijará explícitamente `jacoco { toolVersion = ... }` compatible durante la Tarea 2.
