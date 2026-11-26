# Tareas: Exclusiones Compose en JaCoCo y Auditoría de Cobertura

- [x] **Tarea 1: Añadir exclusiones de vistas Compose y `MainActivity` en `app/build.gradle.kts`**
  - Incorporar `**/*Screen*.class`, `**/*Modal*.class`, `**/*Components*.class`, `**/*Card*.class`, `**/*Item*.class`, `**/*Dialog*.class` y `**/MainActivity*.class` en `exclusions` dentro de `jacocoTestReport`.
  - Verificación: `.\gradlew.bat testDebugUnitTest jacocoTestReport`

- [x] **Tarea 2: Auditar el XML de JaCoCo e identificar `ViewModels` y `Repositories` con `0 %` de cobertura**
  - Analizar `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` tras la regeneración para verificar que las vistas excluidas no aparecen y listar todos los `*ViewModel` y `*Repository` con `0 %` de cobertura (junto con el resumen global de cobertura actualizado).
  - Verificación: lectura/parseo XML de `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` y contraste con [`.opencode/agents/testing-guide.md`](../../.opencode/agents/testing-guide.md).
