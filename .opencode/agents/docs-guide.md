# Guía Maestra de Documentación — GameMatch (`README.md` y `docs/`)

Cuando el usuario pida generar o actualizar el `README.md` principal o cualquiera de los documentos técnicos especializados en `docs/`, **debes inspeccionar primero los ficheros reales de `app/src/main` y `app/src/test` + `app/src/androidTest`** para que todos los enlaces relativos y nombres de clases sean 100% reales.

---

## 1. Estructura Obligatoria del `README.md` Principal

El `README.md` de la raíz debe seguir **exactamente** esta estructura y estilo HTML/Markdown:

### A. Cabecera Centrada ULPGC + Badges
```html
<div align="center">

<img src="readme_media/assets/ulpgc.png" alt="Escudo ULPGC" width="160" />

### UNIVERSIDAD DE LAS PALMAS DE GRAN CANARIA
**Grado en Ingeniería Informática — Asignatura: Programación de Aplicaciones Móviles Nativas**  
*Curso Académico 2026/2027*

---

<h1 style="color: #00E5FF; font-size: 2.3rem; font-weight: bold; letter-spacing: 3px; text-shadow: 0 0 12px rgba(0, 229, 255, 0.5);">
  GAMEMATCH — DESCUBRIMIENTO Y GESTIÓN DE VIDEOJUEGOS
</h1>

<h3 style="color: #B388FF; font-size: 1.05rem; letter-spacing: 1px;">
  Desarrollado por: <a href="[https://github.com/studies-ulpgc](https://github.com/studies-ulpgc)" style="color: #B388FF; text-decoration: none; font-weight: bold;">Dácil Santana Ortega</a> &amp; <a href="[https://github.com/Naiara262005](https://github.com/Naiara262005)" style="color: #B388FF; text-decoration: none; font-weight: bold;">Naiara Díaz Hernández</a>
</h3>

![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Persistence-Room_SQLite-green?style=flat-square&logo=sqlite&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD_Passing-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-Quality_Gate_Passed-4E9BCD?style=flat-square&logo=sonarqube&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-Completado_100%25-brightgreen?style=flat-square)

---

</div>
```

### B. Secciones Obligatorias del README.md Principal (en este orden exacto)

1. **Índice de Contenidos** numerado con enlaces ancla exactos a cada apartado.
2. **1. Resumen y Objetivos del Proyecto**: Qué problema resuelve GameMatch (descubrimiento mediante swipe, motor de recomendación por géneros/plataformas, consulta de precios en Steam en tiempo real, gestión de favoritos offline con Room y comunidad).
3. **2. Demostración en Vídeo y Flujo Funcional**:
    - Subsecciones numeradas (`2.1. Autenticación y Onboarding`, `2.2. Swipe y Recomendación`, `2.3. Descubrimiento, Detalle y Precios Steam`, `2.4. Favoritos Offline y Comunidad`).
    - Espacio reservado para enlaces de vídeo de GitHub (`[https://github.com/user-attachments/assets/](https://github.com/user-attachments/assets/)...`) o GIFs en `readme_media/shorts/` y bloques `> [!NOTE]` indicando comportamiento offline o interacciones clave.
4. **3. Requisitos, Configuración y Compilación**:
    - Versiones exactas (Android Studio, JDK 17, compileSdk, minSdk, targetSdk) leídas directamente de `app/build.gradle.kts`.
    - Instrucciones paso a paso para configurar `local.properties` a partir de `local.properties.example` (sin credenciales reales).
    - Comandos de compilación (`./gradlew assembleDebug`), tests unitarios (`./gradlew testDebugUnitTest`) y tests instrumentados (`./gradlew connectedDebugAndroidTest`).
5. **4. Estructura y Arquitectura del Proyecto**:
    - Árbol completo en bloque HTML `<pre><b>GameMatch/</b> ... </pre>` donde **cada carpeta y fichero clave sea un enlace `<a href="./app/src/...">` real** con comentario alineado a la derecha explicando su responsabilidad (mismo formato visual que `eFactura-ws-mock` y `IG_Semana_6_7`).
6. **5. Batería Completa de Pruebas (`test` y `androidTest`)**:
    - Tabla Markdown cruzando **todas** las clases de test reales del proyecto con las columnas:
      `| Clase de Test | Tipo (Unit / AndroidTest) | Clase / Módulo Probado | Ámbito de Prueba | Nº Tests |`
7. **6. Documentación Técnica Especializada por Módulos de la Asignatura**:
    - Lista con enlaces relativos a los 6 documentos profundos de `docs/`.
8. **7. Servicios Externos, Recursos y Declaración de IA**:
    - Tabla de APIs consumidas (IGDB, Twitch OAuth2, Steam Store API), librerías y licencias.

---

## 2. Estructura Obligatoria de los Documentos Especializados (`docs/*.md`)

Cuando se solicite generar la documentación especializada, crea **6 documentos técnicos profundos** en `docs/`, cubriendo cada bloque temático de **PAMN**:
1. `docs/01-arquitectura-mvvm-y-navegacion.md` — MVVM, `AppContainer`, `AppViewModelProvider`, `MainContainerScreen` y flujo unidireccional de estado.
2. `docs/02-corrutinas-flujos-y-asincronia.md` — `viewModelScope`, `StateFlow`, `Dispatchers.IO`, peticiones concurrentes IGDB + Steam y `MainDispatcherRule`.
3. `docs/03-persistencia-room-y-apis-rest.md` — `AppDatabase`, `GameDao`, `GameEntity`, `PlatformsConverter`, `RetrofitClient`, `IgdbAuthInterceptor`, `IgdbTokenProvider` y Mappers.
4. `docs/04-patrones-de-diseno-y-algoritmos.md` — Patrones Repository, DI Container, Mapper/DTO, Observer/StateFlow y lógica de `GameRecommender`, `GameSorter`, `GenreMatcher`, `PlatformFamily`.
5. `docs/05-calidad-sonarqube-y-testing.md` — Reglas de complejidad y longitud de clase, métricas de SonarQube y cobertura cruzada entre `src/test` y `src/androidTest`.
6. `docs/06-devops-github-actions-y-despliegue.md` — Pipeline CI en `.github/workflows/android-ci.yml`, ejecución de SonarQube en Docker, gestión de secretos y generación de APK.

### Plantilla Interna Obligatoria para CADA `docs/*.md`
Cada archivo dentro de `docs/` **debe incluir**:
1. Enlace superior `[← Volver al README Principal](../README.md)`.
2. Índice de contenidos propio con enlaces ancla.
3. Explicación técnica profunda con fragmentos cortos de código real extraídos del proyecto (máx. 15-20 líneas por bloque) y alertas `> [!IMPORTANT]` / `> [!TIP]`.
4. **Tabla de Trazabilidad Código ↔ Tests** (obligatoria en todos los archivos de `docs/`):
   `| Concepto / Patrón PAMN | Clase(s) de Implementación | Ubicación | Clase(s) de Test | Métodos / Casos Verificados |`
5. Sección desplegable `<details><summary><b>Ver detalles técnicos y métricas de verificación</b></summary>...</details>` al final y enlace de retorno `[← Volver al README Principal](../README.md)`.
6. Usa notas al pie estilo GitHub ([^1]) para aclaraciones técnicas secundarias (versiones de herramientas, restricciones de API o compatibilidad de emuladores) definiéndolas al final del documento.