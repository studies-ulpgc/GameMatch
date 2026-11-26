# Especificación: Pipeline CI en GitHub Actions (`android-ci.yml`)

## 1. Objetivo Técnico
Automatizar la verificación continua del proyecto Android en GitHub Actions mediante el workflow [`.github/workflows/android-ci.yml`](../../.github/workflows/android-ci.yml), ejecutando tests unitarios, reporte de cobertura JaCoCo, análisis Lint y compilación del APK debug ante cada `push` o `pull_request` hacia `main` y `develop`, publicando los reportes y el APK resultante como artefactos.

## 2. Alcance Exacto

### Incluido
- Creación de [`.github/workflows/android-ci.yml`](../../.github/workflows/android-ci.yml) con:
  - Disparadores `push` y `pull_request` sobre las ramas `main` y `develop`.
  - Job `build-and-test` sobre `ubuntu-latest` con `actions/checkout@v4`, `actions/setup-java@v4` (`temurin`, Java `17`) y `gradle/actions/setup-gradle@v4`.
  - Creación de `local.properties` ficticio (`IGDB_CLIENT_ID=dummy_id`, `IGDB_CLIENT_SECRET=dummy_secret`) para habilitar la generación de `BuildConfig` sin credenciales reales.
  - Garantía de ejecución de `./gradlew` en runners Linux (permiso `+x` en `gradlew`).
  - Ejecución de `./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug`.
  - Subida de artefactos con `actions/upload-artifact@v4` y condición `if: always()` para:
    - Reportes de tests (`app/build/reports/tests/`).
    - Reportes de cobertura JaCoCo (`app/build/reports/jacoco/jacocoTestReport/`).
    - APK debug (`app/build/outputs/apk/debug/app-debug.apk`).

### Excluido
- Ejecución de tests instrumentados en emulador (`connectedDebugAndroidTest`).
- Envío de métricas a SonarQube desde GitHub Actions (SonarQube está configurado para instancia local Docker en `http://localhost:9000`).
- Firma o publicación de APK `release` en Play Store / GitHub Releases.
- Modificación de reglas de código fuente `.kt` o secretos en el repositorio.

## 3. Archivos a Crear o Modificar

| Acción | Archivo | Propósito |
| --- | --- | --- |
| Crear | `.github/workflows/android-ci.yml` | Definición del pipeline CI de build, tests, cobertura, lint y artefactos. |
| Opcional (índice Git) | [`gradlew`](../../gradlew) | Asegurar permiso `100755` (`+x`) para ejecución directa en `ubuntu-latest`, además de `chmod +x gradlew` en el workflow. |

## 4. Criterios de Aceptación
1. El archivo `.github/workflows/android-ci.yml` es YAML válido y se dispara únicamente en `push` y `pull_request` sobre `main` y `develop`.
2. El job `build-and-test` usa `ubuntu-latest`, `actions/checkout@v4`, `actions/setup-java@v4` (`distribution: 'temurin'`, `java-version: '17'`) y `gradle/actions/setup-gradle@v4`.
3. El paso `Create dummy local.properties` crea `local.properties` en el runner con valores ficticios (`dummy_id`, `dummy_secret`), sin secretos reales ni variables hardcodeadas de producción.
4. El pipeline ejecuta `./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug` sin fallo de permisos sobre `./gradlew`.
5. Existen pasos `actions/upload-artifact@v4` con `if: always()` que apuntan a las rutas reales verificadas localmente: `app/build/reports/tests/`, `app/build/reports/jacoco/jacocoTestReport/` y `app/build/outputs/apk/debug/app-debug.apk`.
6. La ejecución local de `.\gradlew.bat testDebugUnitTest jacocoTestReport lintDebug assembleDebug` finaliza con éxito y genera todas las rutas de salida referenciadas por el workflow.
