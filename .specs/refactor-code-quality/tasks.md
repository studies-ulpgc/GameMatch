# Tareas: Refactorización de Calidad, Modularización y Límite de 250 Líneas

- [x] **Tarea 1: Extraer componentes compartidos (`ui/components/`) y refactorizar `ui/auth/` (`LoginScreen`, `SignUpScreen`)**
  - Crear `SearchBarField.kt`, `StarRatingRow.kt`, `PreferenceCards.kt` y `PreferenceFormFields.kt` en `ui/components/`.
  - Ampliar `ui/auth/AuthComponents.kt` (`AuthScreenContainer`, `AuthEmailField`, `AuthPasswordField`) y simplificar `LoginScreen.kt` y `SignUpScreen.kt` (`<= 250` líneas).
  - Verificación: `.\gradlew.bat assembleDebug testDebugUnitTest`

- [x] **Tarea 2: Refactorizar `OnboardingScreen.kt`, `ProfileViewModel.kt` y `ProfileScreen.kt`**
  - Actualizar `OnboardingScreen.kt` reutilizando `PreferenceCards` y `PreferenceFormFields`.
  - Extraer `ProfileUiState.kt` y helper de callbacks en `ProfileViewModel.kt`.
  - Extraer `ProfileReadOnlyCard.kt`, `ProfileDevToolsCard.kt`, `ProfileDialogs.kt` y simplificar `ProfileScreen.kt` (`<= 250` líneas cada uno).
  - Verificación: `.\gradlew.bat assembleDebug testDebugUnitTest`

- [x] **Tarea 3: Refactorizar `CommunityViewModel.kt` y modularizar `CommunityScreen.kt`**
  - Extraer `CommunityUiState.kt` y `CommunityFilters.kt` desde `CommunityViewModel.kt`, unificando acciones y validaciones de sala.
  - Descomponer `CommunityScreen.kt` eliminando el `LaunchedEffect(Unit)` duplicado, unificando `CreateRoomModal` y `EditRoomModal` en `RoomFormModal.kt` y extrayendo `RoomCardComponents.kt`, `RoomCards.kt`, `RoomsTabContent.kt`, `MyRoomsTabContent.kt`, `UserMatchTabContent.kt`, `UserMatchCard.kt`, `UserDetailModal.kt` y `RoomMembersModal.kt` (`<= 250` líneas cada uno).
  - Verificación: `.\gradlew.bat assembleDebug testDebugUnitTest`

- [x] **Tarea 4: Refactorizar `DiscoverScreen.kt`, `GameDetailModal.kt` y `SwipeScreen.kt`**
  - Extraer `DiscoverGameCard.kt` y `DiscoverFilters.kt` (unificando `SortOption` y carruseles) en `ui/discover/`.
  - Extraer `DetailHeaderSection.kt`, `UserReviewSection.kt` y `GameReviewItem.kt` en `ui/detail/`.
  - Extraer `SwipeActionButtons.kt` y `SwipeEmptyState.kt` en `ui/swipe/`, eliminando la comprobación redundante en `SwipeScreen.kt`.
  - Verificación: `.\gradlew.bat assembleDebug testDebugUnitTest`

- [x] **Tarea 5: Refactorizar `CommunityRepository.kt` y `GameRepository.kt`**
  - Simplificar `CommunityRepository.kt` unificando el mapeo `UserProfile` y la actualización de miembros en salas.
  - Extraer `FirestoreGameParser.kt` e `IgdbCatalogSyncService.kt` desde `GameRepository.kt` manteniendo intacta su API pública y sus tests.
  - Verificación: `.\gradlew.bat assembleDebug testDebugUnitTest`

- [x] **Tarea 6: Verificación integral de límite de líneas (`<= 250`), compilación y cobertura**
  - Confirmar que ningún fichero `.kt` de `app/src/main/java` supera las 250 líneas.
  - Ejecutar la suite completa con reporte JaCoCo y compilación de `androidTest`.
  - Verificación: `.\gradlew.bat assembleDebug compileDebugAndroidTestKotlin testDebugUnitTest jacocoTestReport`
