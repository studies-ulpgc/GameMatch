---
description: Inicia cualquier tarea usando Spec-Driven Development (spec.md, plan.md, tasks.md) con cambios pequeños y verificables.
---

Analiza la petición del usuario: "$ARGUMENTS"

1. Lee `AGENTS.md` y las guías especializadas de `.opencode/agents/` que correspondan a esta tarea.
2. Inspecciona los archivos reales afectados en `app/src/` antes de proponer cambios.
3. Crea la carpeta `.specs/<nombre-corto-tarea>/` y genera tres archivos:
    - `spec.md`: Objetivo técnico, alcance exacto, archivos a crear/modificar y criterios de aceptación.
    - `plan.md`: Arquitectura de la solución asegurando que ningún archivo supere las 250 líneas y que no se rompan los tests actuales.
    - `tasks.md`: Lista ordenada de micro-tareas (`- [ ] Tarea 1...`), donde cada tarea sea pequeña y tenga un comando `./gradlew` asociado para verificarla.
4. Presenta un resumen conciso de `tasks.md` y pide confirmación antes de modificar cualquier fichero de código fuente.