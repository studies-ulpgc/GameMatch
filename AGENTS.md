# AGENTS.md — GameMatch (Android / Kotlin / Jetpack Compose)

Aplicación móvil nativa Android (**PAMN - ULPGC**) desarrollada en **Kotlin** y **Jetpack Compose** bajo arquitectura **MVVM** con inyección manual (`AppContainer`), base de datos local **Room**, APIs REST (**IGDB**, **Steam**, **Twitch Auth**) con **Retrofit** y asincronía con **Corrutinas + StateFlow**.

## Comandos de Verificación Obligatorios
- Compilar APK debug: `./gradlew assembleDebug`
- Tests unitarios: `./gradlew testDebugUnitTest`
- Tests instrumentados (emulador): `./gradlew connectedDebugAndroidTest`
- Cobertura + SonarQube local (Docker `:9000`): `./gradlew testDebugUnitTest jacocoTestReport sonar`

## Reglas Críticas (Non-Negotiable)
1. **SDD antes de picar código:** Para cualquier tarea de refactor, testing o nueva feature, genera primero `.specs/<tarea>/spec.md`, `plan.md` y `tasks.md` con cambios pequeños y comprobables. No edites código fuente hasta tener el plan aprobado.
2. **Límite de líneas:** Ningún fichero `.kt` puede superar las **250 líneas** (regla de calidad SonarQube). Divide clases o pantallas Compose largas en subcomponentes cohesivos.
3. **Cero secretos:** Prohibido hardcodear API keys, tokens o contraseñas en código o docs.
4. **Estilo humano:** Sigue siempre `.opencode/skills/clean-human-code/SKILL.md`.

## Mapa de Guías Especializadas (Lee antes de actuar)
- **Documentación y READMEs estilo universitario:** `.opencode/agents/docs-guide.md`
- **Tests (`src/test` y `src/androidTest`):** `.opencode/agents/testing-guide.md`
- **Arquitectura MVVM, Refactorización y SonarQube:** `.opencode/agents/constitution.md`
- **GitHub Actions, JaCoCo y Docker SonarQube:** `.opencode/agents/deployment.md`
- **Resolución de fallos Gradle/Room/Compose:** `.opencode/agents/troubleshooting.md`