# Especificación: Tests Unitarios para ViewModels Pendientes

## 1. Objetivo Técnico
Implementar suites de tests unitarios con `JUnit 4`, `MockK` y `kotlinx-coroutines-test` (`MainDispatcherRule`) para los cuatro `ViewModel` que actualmente carecen de cobertura propia o tienen cobertura residual según [`.opencode/agents/testing-guide.md`](../../.opencode/agents/testing-guide.md): `AppSettingsViewModel`, `FavoritesViewModel`, `GameDetailViewModel` y `DiscoverViewModel`.

## 2. Alcance Exacto

### Incluido
- Creación de cuatro archivos de test bajo `app/src/test/java/com/pamn/gamematch/ui/`:
  1. `settings/AppSettingsViewModelTest.kt`
  2. `favorites/FavoritesViewModelTest.kt`
  3. `detail/GameDetailViewModelTest.kt`
  4. `discover/DiscoverViewModelTest.kt`
- Cobertura de estados iniciales, flujos exitosos, entradas inválidas/vacías, respuestas nulas y escenarios de fallo/error de red o repositorio (`Result.failure`, callbacks `false`).
- Verificación de compilación, ejecución verde de todos los tests y actualización del reporte JaCoCo mediante `./gradlew testDebugUnitTest jacocoTestReport`.

### Excluido
- Modificación de la lógica de producción en `app/src/main/java/`.
- Creación de tests para `AuthRepository`, `CommunityRepository`, `SettingsRepository` o tests instrumentados en `app/src/androidTest/`.

## 3. Archivos a Crear

| Archivo | ViewModel Cubierto | Dependencias Mockeadas |
| --- | --- | --- |
| `app/src/test/java/com/pamn/gamematch/ui/settings/AppSettingsViewModelTest.kt` | [`AppSettingsViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/settings/AppSettingsViewModel.kt) | `SettingsRepository`, `LocaleManager`, `AppCompatDelegate` (estático) |
| `app/src/test/java/com/pamn/gamematch/ui/favorites/FavoritesViewModelTest.kt` | [`FavoritesViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/favorites/FavoritesViewModel.kt) | `GameRepository` |
| `app/src/test/java/com/pamn/gamematch/ui/detail/GameDetailViewModelTest.kt` | [`GameDetailViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/detail/GameDetailViewModel.kt) | `GameRepository`, `AuthRepository` |
| `app/src/test/java/com/pamn/gamematch/ui/discover/DiscoverViewModelTest.kt` | [`DiscoverViewModel`](../../app/src/main/java/com/pamn/gamematch/ui/discover/DiscoverViewModel.kt) | `GameRepository` |

## 4. Criterios de Aceptación
1. Cada nuevo fichero `.kt` cumple el límite máximo de **250 líneas** establecido en [`AGENTS.md`](../../AGENTS.md).
2. Cada clase de test contiene al menos 5 casos de prueba, incluyendo escenarios de éxito, listas vacías/datos nulos y errores.
3. Todos los tests unitarios existentes y nuevos compilan y pasan con `.\gradlew.bat testDebugUnitTest jacocoTestReport`.
4. La cobertura de `AppSettingsViewModel`, `FavoritesViewModel`, `GameDetailViewModel` y `DiscoverViewModel` en `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` sube desde `0 %` / `2,48 %` a cobertura efectiva de sus métodos y ramas principales.
