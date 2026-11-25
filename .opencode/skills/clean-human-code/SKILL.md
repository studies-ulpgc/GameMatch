# Guía de Código Limpio y Natural

## Reglas de Código (Kotlin & Compose)
1. **Comentarios útiles, nunca obvios:** Prohibido comentar qué hace una línea evidente (ej. `// Inicializa el ViewModel` o `// Devuelve la lista`). Comenta únicamente decisiones de negocio, workarounds técnicos o restricciones de API.
2. **Nombres expresivos:** Usa nombres claros en inglés técnico coherentes con el proyecto (`GameRepository`, `ComputeFlags`, `GameUiState`).
3. **Funciones cortas y responsabilidad única:** Si un Composable supera las 60-80 líneas, extrae subcomponentes privados o en archivos separados dentro de su paquete `ui/<feature>/`.
4. **Inmutabilidad:** Prioriza `val`, `data class`, `StateFlow` expuesto como `asStateFlow()` y listas inmutables.
5. **Manejo explícito de errores:** Nunca dejes bloques `catch (e: Exception) {}` vacíos. Registra o mapea el error a un estado de UI controlado.

## Reglas de Redacción (Markdown)
1. Evita muletillas genéricas de IA ("En conclusión", "Cabe destacar que", "En el mundo actual").
2. Usa tono técnico, directo y preciso acorde a una memoria de Ingeniería Informática.
3. Cita siempre rutas reales del proyecto con enlaces relativos válidos.