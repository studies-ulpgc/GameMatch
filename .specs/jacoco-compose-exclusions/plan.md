# Plan Técnico: Exclusiones Compose en JaCoCo y Auditoría de Cobertura

## 1. Estado Actual
- En [`app/build.gradle.kts`](../../app/build.gradle.kts), `jacocoTestReport` filtra actualmente `R`, `BuildConfig`, `Manifest`, `*Test*`, `*Binding*`, `*_Impl*`, `ComposableSingletons*` y `ui/theme/**`.
- La inspección de `app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes/com/pamn/gamematch/` muestra múltiples clases compiladas de vistas Compose (`*ScreenKt*.class`, `*ModalKt*.class`, `*ComponentsKt*.class`, `*CardKt*.class`, etc.) y `MainActivity*.class` que distorsionan el porcentaje de cobertura de lógica de negocio en `testDebugUnitTest`.
- Nota documental: el archivo de referencia de testing en el repositorio es [`.opencode/agents/testing-guide.md`](../../.opencode/agents/testing-guide.md) (no existe `.opencode/agents/testing.md`).

## 2. Diseño de la Modificación
1. **Ampliar `exclusions` en [`app/build.gradle.kts`](../../app/build.gradle.kts):**
   Añadir exactamente los siete patrones solicitados al listado `exclusions` utilizado por `kotlinClasses`, `legacyKotlinClasses` y `javaClasses`:
   - `**/*Screen*.class`
   - `**/*Modal*.class`
   - `**/*Components*.class`
   - `**/*Card*.class`
   - `**/*Item*.class`
   - `**/*Dialog*.class`
   - `**/MainActivity*.class`
2. **Regeneración y lectura del reporte XML:**
   Ejecutar `.\gradlew.bat testDebugUnitTest jacocoTestReport` para recalcular `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` y consultar sus nodos `<package>` / `<class>` para extraer todas las clases `*ViewModel` y `*Repository` (incluyendo sus corrutinas/clases internas o consolidado por clase principal) con cobertura `0 %`.

## 3. Control de Calidad
- No se crean ni modifican ficheros `.kt` (límite de 250 líneas intacto).
- [`app/build.gradle.kts`](../../app/build.gradle.kts) permanece en ~167 líneas.
- No se introducen secretos ni se alteran los tests existentes.
