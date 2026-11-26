# Tareas: Pipeline CI en GitHub Actions (`android-ci.yml`)

- [x] **Tarea 1: Validar localmente `lintDebug` y la cadena completa de tareas CI**
  - Ejecutar la secuencia exacta que correrá en CI para comprobar que `lintDebug` pasa sin errores bloqueantes y confirmar las rutas exactas de salida (`app/build/reports/tests/`, `app/build/reports/jacoco/jacocoTestReport/`, `app/build/outputs/apk/debug/app-debug.apk`).
  - Verificación: `.\gradlew.bat testDebugUnitTest jacocoTestReport lintDebug assembleDebug`

- [x] **Tarea 2: Asegurar permisos de ejecución de `gradlew` y crear `.github/workflows/android-ci.yml`**
  - Garantizar permiso de ejecución en Linux mediante `chmod +x gradlew` en el paso de ejecución del workflow (nota: en el índice Git local `gradlew` permanece en `100644` al no poder escribir `.git/index.lock` desde el entorno sandbox; opcionalmente ejecutable fuera de él con `git update-index --chmod=+x gradlew`).
  - Crear `.github/workflows/android-ci.yml` siguiendo [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md) y [`.specs/github-actions/spec.md`](./spec.md): triggers `push`/`pull_request` en `main` y `develop`, `ubuntu-latest`, Temurin JDK 17, `gradle/actions/setup-gradle@v4`, `Create dummy local.properties`, ejecución de `./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug` y subida de artefactos con `if: always()`.
  - Verificación: comprobar presencia y estructura del workflow en `.github/workflows/android-ci.yml`.

- [x] **Tarea 3: Verificación integral de rutas de artefactos y estado final del repositorio**
  - Verificar que el archivo `.github/workflows/android-ci.yml` no contiene secretos ni rutas inexistentes respecto a las generadas en `app/build/`.
  - Confirmar con `.\gradlew.bat testDebugUnitTest jacocoTestReport lintDebug assembleDebug` y revisión de `git status` que todo el flujo pasa en verde.
  - Verificación: `.\gradlew.bat testDebugUnitTest jacocoTestReport lintDebug assembleDebug`
