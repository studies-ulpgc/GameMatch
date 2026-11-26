# Especificación: Refactorización de Calidad, Modularización y Límite de 250 Líneas

## 1. Objetivo Técnico
Eliminar la deuda estructural detectada en `app/src/main/java` reduciendo todos los ficheros `.kt` por debajo del límite estricto de **250 líneas** ([`AGENTS.md`](../../AGENTS.md)), extrayendo subcomponentes Compose y helpers reutilizables ([`.opencode/agents/constitution.md`](../../.opencode/agents/constitution.md)), y eliminando duplicación y code smells en `ui/community`, `ui/auth`, `ui/profile`, `ui/discover`, `ui/detail`, `ui/swipe` y `data/repository`, sin alterar el comportamiento funcional ni romper los tests existentes.

## 2. Inventario Inicial Detectado (> 250 líneas y Duplicaciones)

| Archivo | Líneas actuales | Problemas principales |
| --- | --- | --- |
| [`CommunityScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/community/CommunityScreen.kt) | 1745 | Monolito UI; duplica `CreateRoomModal`/`EditRoomModal`, cabeceras/badges de sala, buscadores y doble `LaunchedEffect(Unit)`. |
| [`ProfileScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/profile/ProfileScreen.kt) | 699 | Monolito UI; duplica formulario de preferencias y apariencia con `OnboardingScreen`, y campo contraseña con `SignUpScreen`. |
| [`DiscoverScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/discover/DiscoverScreen.kt) | 537 | Mapeo duplicado de `SortOption`, carruseles `featured`/`trending` duplicados, buscador repetido y tarjeta embebida. |
| [`GameDetailModal.kt`](../../app/src/main/java/com/pamn/gamematch/ui/detail/GameDetailModal.kt) | 521 | Secciones de cabecera, reseña propia y lista mezcladas; fila de estrellas duplicada. |
| [`CommunityViewModel.kt`](../../app/src/main/java/com/pamn/gamematch/ui/community/CommunityViewModel.kt) | 486 | Validación y manejo repetitivo de acciones sobre salas (`join`/`leave`/`removeMember`/`delete`) y filtros/afinidad acoplados. |
| [`GameRepository.kt`](../../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt) | 459 | Mezcla consulta de catálogo/caché/reseñas con sincronización masiva IGDB + lotes Steam y parseo de snapshots. |
| [`OnboardingScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/OnboardingScreen.kt) | 376 | Duplica tarjetas de apariencia, precio, lanzados, géneros y campos de perfil con `ProfileScreen`. |
| [`SignUpScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/SignUpScreen.kt) | 318 | Duplica fondo/tarjeta, campo email, campo contraseña y botones con [`LoginScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/LoginScreen.kt) (250 líneas). |
| [`CommunityRepository.kt`](../../app/src/main/java/com/pamn/gamematch/data/repository/CommunityRepository.kt) | 274 | Duplica mapeo `DocumentSnapshot -> UserProfile` y actualización `leaveRoom`/`removeMemberFromRoom`. |
| [`SwipeScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/swipe/SwipeScreen.kt) | 255 | Excede 250 líneas; contiene condición redundante `if (currentGame != null)` señalada por el compilador. |
| [`ProfileViewModel.kt`](../../app/src/main/java/com/pamn/gamematch/ui/profile/ProfileViewModel.kt) | 255 | Excede 250 líneas por `ProfileUiState` embebido y manejo repetitivo de callbacks `AuthRepository`. |

## 3. Alcance Exacto

### Incluido
- Extracción de subcomponentes reutilizables en `ui/components/`, `ui/auth/`, `ui/profile/`, `ui/community/`, `ui/discover/`, `ui/detail/` y `ui/swipe/`.
- Extracción de estados/filtros puros y colaboradores internos en `ui/community/`, `ui/profile/` y `data/repository/` preservando las firmas públicas usadas por `AppContainer`, `AppViewModelProvider`, las pantallas y los 98 tests unitarios existentes.
- Eliminación de lógica duplicada, anidamiento innecesario y el warning de condición siempre verdadera en `SwipeScreen.kt`.

### Excluido
- Cambios de comportamiento visual/negocio no solicitados o eliminación de recursos/strings existentes.
- Modificación de firmas públicas de ViewModels o Repositories que rompan los tests actuales.

## 4. Criterios de Aceptación
1. **Ningún archivo `.kt`** bajo `app/src/main/java` supera las **250 líneas**.
2. La lógica duplicada identificada (`CreateRoomModal`/`EditRoomModal`, `ProfileScreen`/`OnboardingScreen`, `LoginScreen`/`SignUpScreen`, buscadores, estrellas, `SortOption`, acciones de sala, mappers repetidos en `CommunityRepository`) queda unificada en componentes/helpers compartidos.
3. Desaparece el doble `LaunchedEffect(Unit)` en `CommunityScreen.kt` y el warning `Condition is always 'true'` en `SwipeScreen.kt`.
4. `.\gradlew.bat assembleDebug compileDebugAndroidTestKotlin testDebugUnitTest jacocoTestReport` pasa completamente en verde sin romper ninguno de los tests existentes.
