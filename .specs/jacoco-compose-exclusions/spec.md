# Especificación: Exclusiones de Vistas Compose en `jacocoTestReport` y Auditoría de Cobertura

## 1. Objetivo Técnico
Filtrar las vistas declarativas de Jetpack Compose y `MainActivity` del reporte `jacocoTestReport` en [`app/build.gradle.kts`](../../app/build.gradle.kts) para medir la cobertura real sobre la lógica de negocio (ViewModels, Repositories, Mappers, Room y Utils), e identificar los `ViewModel` y `Repository` con `0 %` de cobertura.

## 2. Alcance Exacto

### Incluido
- Añadir en la lista `exclusions` de `jacocoTestReport` en [`app/build.gradle.kts`](../../app/build.gradle.kts) los patrones:
  - `**/*Screen*.class`
  - `**/*Modal*.class`
  - `**/*Components*.class`
  - `**/*Card*.class`
  - `**/*Item*.class`
  - `**/*Dialog*.class`
  - `**/MainActivity*.class`
- Regenerar el reporte ejecutando `.\gradlew.bat testDebugUnitTest jacocoTestReport`.
- Analizar `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` tras aplicar los filtros y listar exactamente qué clases `*ViewModel` y `*Repository` tienen `0 %` de cobertura.

### Excluido
- Creación o modificación de tests unitarios o instrumentados (a realizar en una tarea posterior guiada por [`.opencode/agents/testing-guide.md`](../../.opencode/agents/testing-guide.md)).
- Modificación de ficheros `.kt` de aplicación o de `sonar-project.properties`.

## 3. Archivos a Crear o Modificar

| Acción | Archivo | Propósito |
| --- | --- | --- |
| Modificar | [`app/build.gradle.kts`](../../app/build.gradle.kts) | Incorporar los 7 patrones de exclusión Compose/`MainActivity` en `jacocoTestReport`. |

## 4. Criterios de Aceptación
1. [`app/build.gradle.kts`](../../app/build.gradle.kts) incluye los 7 patrones indicados dentro de `exclusions` en `jacocoTestReport`, manteniendo las exclusiones previas.
2. `.\gradlew.bat testDebugUnitTest jacocoTestReport` finaliza con éxito y regenera `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`.
3. Las clases `*Screen*`, `*Modal*`, `*Components*`, `*Card*`, `*Item*`, `*Dialog*` y `MainActivity*` dejan de contabilizarse en el XML de JaCoCo.
4. El análisis del XML generado enumera de forma verificada qué `ViewModel` y `Repository` presentan `0 %` de cobertura (comparado con el inventario de [`.opencode/agents/testing-guide.md`](../../.opencode/agents/testing-guide.md)).
