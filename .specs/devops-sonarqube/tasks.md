# Tareas: Configuración de `local.properties.example`, `sonar-project.properties` y JaCoCo

- [x] **Tarea 1: Crear plantilla de credenciales `local.properties.example`**
  - Crear `local.properties.example` en la raíz con `sdk.dir`, `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET` con valores de ejemplo, sin secretos reales.
  - Comprobar que `local.properties` sigue ignorado por Git y el proyecto compila.
  - Verificación: `./gradlew assembleDebug`

- [x] **Tarea 2: Configurar JaCoCo y la tarea `jacocoTestReport` en `app/build.gradle.kts`**
  - Aplicar el plugin `jacoco` en [`app/build.gradle.kts`](../../app/build.gradle.kts).
  - Configurar `tasks.withType<Test>()` (`includeNoLocationClasses = true`, `excludes = listOf("jdk.internal.*")`) y registrar `jacocoTestReport` dependiente de `testDebugUnitTest` con reporte XML activado y las exclusiones definidas (`*Binding*`, `*_Impl*`, `ComposableSingletons*`, `ui/theme/*` y clases Android autogeneradas), apuntando a las rutas reales de AGP `9.4.1` (`intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes` e `intermediates/javac/debug/compileDebugJavaWithJavac/classes`).
  - Verificar que se genera `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` y que todos los tests unitarios siguen pasando.
  - Verificación: `./gradlew testDebugUnitTest jacocoTestReport`

- [x] **Tarea 3: Configurar `org.sonarqube` en `build.gradle.kts` y crear `sonar-project.properties`**
  - Crear `sonar-project.properties` en la raíz con las propiedades indicadas en [`.opencode/agents/deployment.md`](../../.opencode/agents/deployment.md) adaptando `sonar.java.binaries` a AGP `9.4.1`, sin incluir `sonar.token`.
  - Añadir `id("org.sonarqube") version "7.6.0.18749"` (compatible con AGP `9.4.1` / `ApplicationExtension`) en [`build.gradle.kts`](../../build.gradle.kts) y enlazar la carga de `sonar-project.properties`.
  - Comprobar que la tarea `sonar` queda registrada y es compatible con `org.gradle.configuration-cache=true`.
  - Verificación: `./gradlew sonar --dry-run`

- [x] **Tarea 4: Verificación integral de compilación, tests, cobertura y SonarQube local**
  - Ejecutar la compilación debug y la generación completa del reporte JaCoCo tras los cambios finales.
  - Si el contenedor Docker de SonarQube está activo en `http://localhost:9000` y se dispone de token por parámetro CLI, validar el envío completo del análisis.
  - Verificación: `./gradlew assembleDebug && ./gradlew testDebugUnitTest jacocoTestReport` (y opcionalmente con servidor levantado: `./gradlew testDebugUnitTest jacocoTestReport sonar -Dsonar.token=TU_TOKEN`)
