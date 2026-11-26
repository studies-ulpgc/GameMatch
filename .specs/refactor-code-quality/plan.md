# Plan Técnico: Refactorización de Calidad y Modularización

## 1. Principios de Diseño ([`.opencode/agents/constitution.md`](../../.opencode/agents/constitution.md))
- Mantener intactos todos los paquetes (`data/local`, `data/remote`, `data/repository`, `data/container`, `ui/<feature>`, `utils`) y las firmas públicas consumidas por `AppContainer`, `AppViewModelProvider` y los tests unitarios existentes.
- Cada archivo nuevo o modificado debe tener una responsabilidad única y mantenerse estrictamente en **<= 250 líneas** (objetivo práctico <= 220 líneas para margen de mantenimiento).
- Comentarios breves solo para el *porqué* no obvio ([`.opencode/skills/clean-human-code/SKILL.md`](../../.opencode/skills/clean-human-code/SKILL.md)).

## 2. Desglose Arquitectónico por Área

### 2.1. Componentes Compartidos (`ui/components/`) y Autenticación (`ui/auth/`)
- **`ui/components/SearchBarField.kt`:** `OutlinedTextField` parametrizado (placeholder, `leadingIcon` Search, `trailingIcon` Clear) compartido por `DiscoverScreen`, `RoomsTabContent` y `UserMatchTabContent`.
- **`ui/components/StarRatingRow.kt`:** componente de 5 estrellas parametrizado para lectura (`StarRatingDisplay`) y selección (`StarRatingSelector`) en `GameDetailModal`.
- **`ui/components/PreferenceCards.kt` y `ui/components/PreferenceFormFields.kt`:**
  - `AppearanceSettingsCard` (modo oscuro + idioma), `PriceRangeCard` (`RangeSlider`), `OnlyReleasedCard` (`Switch`) y `GenreSelectorSection` (`FlowRow` de géneros).
  - `UsernameField` y `BirthYearField` con gestión interna de `hasFocused`/`touched`.
- **`ui/auth/AuthComponents.kt`:** ampliar con `AuthScreenContainer` (gradiente + `Card` + cabecera), `AuthEmailField`, `AuthPasswordField` (con visibilidad y barra de fortaleza opcional `showStrengthBar`) y `AuthSubmitSection`, reduciendo [`LoginScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/LoginScreen.kt) y [`SignUpScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/SignUpScreen.kt).

### 2.2. Perfil y Onboarding (`ui/profile/`, `ui/auth/OnboardingScreen.kt`)
- **[`OnboardingScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/auth/OnboardingScreen.kt):** reutilizar `PreferenceCards` y `PreferenceFormFields`.
- **`ui/profile/ProfileUiState.kt` y [`ProfileViewModel.kt`](../../app/src/main/java/com/pamn/gamematch/ui/profile/ProfileViewModel.kt):** mover `ProfileUiState` a archivo propio y extraer helper privado `handleAuthResult(...)` para `updatePassword` / `deleteAccount`.
- **`ui/profile/ProfileReadOnlyCard.kt`, `ui/profile/ProfileDevToolsCard.kt`, `ui/profile/ProfileDialogs.kt` y [`ProfileScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/profile/ProfileScreen.kt):** separar tarjetas de solo lectura, herramientas de desarrollo `BuildConfig.DEBUG`, diálogos de contraseña (reutilizando `AuthPasswordField` o indicador de fortaleza compartido) y confirmación de borrado.

### 2.3. Comunidad (`ui/community/`)
- **`ui/community/CommunityUiState.kt` y `ui/community/CommunityFilters.kt`:** mover enums `RoomFilter`/`UserFilter` y `CommunityUiState`, más funciones puras `buildUserMatches`, `filterRooms` y `filterAndSortUsers`.
- **[`CommunityViewModel.kt`](../../app/src/main/java/com/pamn/gamematch/ui/community/CommunityViewModel.kt):** extraer `executeRoomAction(...)` y `areRoomFieldsValid(...)` para eliminar repetición entre `joinRoom`, `leaveRoom`, `removeMemberFromRoom`, `deleteRoom`, `createRoom` y `updateRoom`.
- **Subcomponentes de [`CommunityScreen.kt`](../../app/src/main/java/com/pamn/gamematch/ui/community/CommunityScreen.kt):**
  - `RoomFormModal.kt`: formulario único unificado para crear o editar sala (`initialRoom: GameRoom? = null`).
  - `RoomCardComponents.kt` + `RoomCards.kt` (o `RoomCard.kt` + `MyRoomHostCard.kt`): compartir cabecera, contador, badges de solo seguidos/plataforma y acciones de anfitrión.
  - `RoomsTabContent.kt`, `MyRoomsTabContent.kt`, `UserMatchTabContent.kt` (usando `SearchBarField` y un helper `CommunityFilterChip`).
  - `UserMatchCard.kt`, `UserDetailModal.kt`, `RoomMembersModal.kt`.
  - Eliminar el segundo `LaunchedEffect(Unit)` redundante en `CommunityScreen.kt`.

### 2.4. Descubrir, Detalle y Swipe (`ui/discover/`, `ui/detail/`, `ui/swipe/`)
- **`ui/discover/`:** extraer `DiscoverGameCard.kt` y `DiscoverFilters.kt` (con `sortOptionLabelRes(SortOption)` único y `DiscoverGameCarousel` compartido).
- **`ui/detail/`:** extraer `DetailHeaderSection.kt`, `UserReviewSection.kt` y `GameReviewItem.kt` usando `StarRatingRow`.
- **`ui/swipe/`:** extraer `SwipeActionButtons.kt` y `SwipeEmptyState.kt`, eliminando el `if (currentGame != null)` redundante dentro del bloque no nulo.

### 2.5. Repositorios (`data/repository/`)
- **[`CommunityRepository.kt`](../../app/src/main/java/com/pamn/gamematch/data/repository/CommunityRepository.kt):** extraer `DocumentSnapshot.toUserProfile(...)` y `updateRoomMembership(roomId, fieldValue, delta)` para unificar `leaveRoom` y `removeMemberFromRoom` (y `joinRoom`).
- **`data/repository/FirestoreGameParser.kt`, `data/repository/IgdbCatalogSyncService.kt` y [`GameRepository.kt`](../../app/src/main/java/com/pamn/gamematch/data/repository/GameRepository.kt):** delegar `parseGamesFromSnapshot` y `syncCatalogFromIgdb` manteniendo exactamente los métodos públicos de `GameRepository` (así `GameRepositoryTest` y `AppContainerImpl` siguen funcionando sin cambios).
