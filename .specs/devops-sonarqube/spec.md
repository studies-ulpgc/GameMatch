# Especificación: Configuración de `local.properties.example`, `sonar-project.properties` y JaCoCo

## 1. Objetivo Técnico
Estandarizar la configuración de credenciales locales mediante una plantilla versionada (`local.properties.example`), habilitar la generación de reportes de cobertura XML con JaCoCo sobre los tests unitarios (`testDebugUnitTest`) e integrar el análisis estático y de cobertura con SonarQube local en `http://localhost:9000`.

## 2. Alcance Exacto

### Incluido
- Creación de [`local.properties.example`](../../local.properties.example) en la raíz del repositorio con las propiedades que consume [`app/build.gradle.kts`](../../app/build.gradle.kts) (`sdk.dir`, `IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET`), sin valores reales ni secretos.
- Registro del plugin `org.sonarqube` (`version "7.6.0.18749"`, compatible con AGP 9.4.1) en [`build.gradle.kts`](../../build.gradle.kts).
- Activación del plugin `jacoco` y definición de la tarea `jacocoTestReport` en [`app/build.gradle.kts`](../../app/build.gradle.kts) para generar el reporte XML a partir de `testDebugUnitTest`, excluyendo clases generadas por Android/Room/Compose y el paquete de tema (`ui/theme/*`).
- Creación de [`sonar-project.properties`](../../sonar-project.properties) en la raíz del proyecto siguiendo las propiedades definidas en [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md), sin almacenar ningún token.

### Excluido
- Creación o modificación del pipeline de GitHub Actions (`.github/workflows/android-ci.yml`).
- Creación o ampliación de tests unitarios o instrumentados en `app/src/test` o `app/src/androidTest`.
- Refactorización de ficheros Kotlin que superen las 250 líneas.
- Inclusión de credenciales reales o tokens de SonarQube en archivos versionados.

## 3. Archivos a Crear o Modificar

| Acción | Archivo | Propósito |
| --- | --- | --- |
| Crear | `local.properties.example` | Plantilla de claves requeridas (`sdk.dir`, `IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET`). |
| Crear | `sonar-project.properties` | Propiedades del proyecto para SonarQube local (`http://localhost:9000`). |
| Modificar | [`build.gradle.kts`](../../build.gradle.kts) | Declarar el plugin `id("org.sonarqube") version "7.6.0.18749"` y cargar `sonar-project.properties`. |
| Modificar | [`app/build.gradle.kts`](../../app/build.gradle.kts) | Aplicar `jacoco` y configurar la tarea `jacocoTestReport` con salida XML y exclusiones. |

## 4. Criterios de Aceptación
1. `local.properties.example` contiene `sdk.dir`, `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET` con valores vacíos o de ejemplo, sin secretos reales, y [`local.properties`](../../local.properties) permanece ignorado en [`.gitignore`](../../.gitignore).
2. La ejecución de `./gradlew testDebugUnitTest jacocoTestReport` finaliza con éxito y genera el archivo `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`.
3. La tarea `jacocoTestReport` excluye `*Binding*`, `*_Impl*`, `ComposableSingletons*`, `ui/theme/*` y las clases autogeneradas estándar de Android (`R`, `BuildConfig`, `Manifest`).
4. `sonar-project.properties` define `sonar.projectKey=gamematch-pamn`, `sonar.projectName=GameMatch`, `sonar.host.url=http://localhost:9000`, `sonar.sources=app/src/main/java`, `sonar.tests=app/src/test/java,app/src/androidTest/java`, `sonar.java.binaries` apuntando a las rutas reales de compilación de AGP 9.4.1 (`app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes,app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes`) y `sonar.coverage.jacoco.xmlReportPaths=app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`, sin incluir `sonar.token`.
5. `./gradlew assembleDebug` y `./gradlew testDebugUnitTest` pasan sin regresiones y la configuración es compatible con `org.gradle.configuration-cache=true` en [`gradle.properties`](../../gradle.properties).
6. Con el contenedor de SonarQube activo en `http://localhost:9000`, `./gradlew testDebugUnitTest jacocoTestReport sonar -Dsonar.token=<TOKEN>` publica métricas y cobertura de código Kotlin sin error de cobertura al `0.0%`.
