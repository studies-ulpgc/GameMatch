[← Volver al README](../README.md)

# 06 — DevOps, GitHub Actions y Despliegue

## Tabla de contenidos
1. [Pipeline de CI/CD](#1-pipeline-de-cicd)
2. [Fases del workflow](#2-fases-del-workflow)
3. [Gestión de secretos en CI](#3-gestión-de-secretos-en-ci)
4. [Artefactos generados](#4-artefactos-generados)
5. [SonarQube con Docker](#5-sonarqube-con-docker)
6. [Despliegue local del APK](#6-despliegue-local-del-apk)
7. [Trazabilidad](#7-trazabilidad)

---

## 1. Pipeline de CI/CD

El fichero [`.github/workflows/android-ci.yml`](../.github/workflows/android-ci.yml) define el pipeline de integración continua. Se activa en cada `push` o `pull_request` a las ramas `main` y `develop`.

```yaml
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
```

El job `build-and-test` se ejecuta en `ubuntu-latest` con permisos de solo lectura sobre el repositorio.

---

## 2. Fases del workflow

```
Checkout  →  JDK 17  →  Gradle Setup  →  local.properties  →  Tests + JaCoCo + Lint + APK  →  Upload artefactos
```

| Paso | Acción | Descripción |
|---|---|---|
| Checkout | `actions/checkout@v4` | Clona el repositorio |
| JDK 17 | `actions/setup-java@v4` | Instala Temurin JDK 17 |
| Gradle | `gradle/actions/setup-gradle@v4` | Cachea dependencias Gradle |
| `local.properties` | Inline `cat` | Crea credenciales dummy para compilar |
| Build | `./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug` | Tests + cobertura + lint + APK |
| Upload reports | `actions/upload-artifact@v4` | Publica informes de tests en la UI de GitHub |
| Upload JaCoCo | `actions/upload-artifact@v4` | Publica informe de cobertura HTML |
| Upload APK | `actions/upload-artifact@v4` | Publica el APK debug para descarga |

---

## 3. Gestión de secretos en CI

En el pipeline de CI se inyectan credenciales **dummy** para que la compilación no falle al leer `local.properties`:

```yaml
- name: Create dummy local.properties
  run: |
    cat <<'EOF' > local.properties
    IGDB_CLIENT_ID=dummy_id
    IGDB_CLIENT_SECRET=dummy_secret
    EOF
```

Los tests unitarios no realizan llamadas reales a APIs (todo está mockeado con MockK), por lo que las credenciales dummy son suficientes.

Para un pipeline de **staging/producción** real, los secretos se gestionan con **GitHub Secrets** (`Settings → Secrets and variables → Actions`):

```yaml
- name: Create real local.properties
  run: |
    echo "IGDB_CLIENT_ID=${{ secrets.IGDB_CLIENT_ID }}" >> local.properties
    echo "IGDB_CLIENT_SECRET=${{ secrets.IGDB_CLIENT_SECRET }}" >> local.properties
```

> **⚠️ Nunca pongas valores reales en el código fuente ni en archivos versionados.**

---

## 4. Artefactos generados

Tras cada ejecución exitosa del workflow, GitHub Actions publica tres artefactos descargables:

| Artefacto | Ruta en el build | Descripción |
|---|---|---|
| `test-reports` | `app/build/reports/tests/` | Resultados HTML de JUnit |
| `jacoco-coverage-reports` | `app/build/reports/jacoco/jacocoTestReport/` | Cobertura HTML + XML |
| `app-debug-apk` | `app/build/outputs/apk/debug/app-debug.apk` | APK firmado con debug keystore |

---

## 5. SonarQube con Docker

Para análisis de calidad local completo:

```bash
# 1. Levantar contenedor SonarQube (primera vez: descarga ~500 MB)
docker run -d --name sonarqube \
  -p 9000:9000 \
  -v sonarqube_data:/opt/sonarqube/data \
  sonarqube:lts-community

# 2. Abrir http://localhost:9000 y crear un proyecto + token

# 3. Añadir en gradle.properties (NO en el repo):
#    systemProp.sonar.host.url=http://localhost:9000
#    systemProp.sonar.token=<tu_token>

# 4. Ejecutar análisis completo
./gradlew testDebugUnitTest jacocoTestReport sonar
```

La configuración `sonar{}` se encuentra en [`app/build.gradle.kts`](../app/build.gradle.kts).

---

## 6. Despliegue local del APK

```bash
# Compilar APK debug
./gradlew assembleDebug

# Instalar en emulador/dispositivo conectado
adb install app/build/outputs/apk/debug/app-debug.apk

# O usar Android Studio: Run ▶ → seleccionar dispositivo
```

Para **release** (requiere keystore propio):

```bash
./gradlew assembleRelease
# El APK sin firmar queda en app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 7. Trazabilidad

| Elemento | Archivo |
|---|---|
| Workflow CI | [`.github/workflows/android-ci.yml`](../.github/workflows/android-ci.yml) |
| Plugin SonarQube Gradle | [`app/build.gradle.kts`](../app/build.gradle.kts) línea 10 |
| Configuración JaCoCo | [`app/build.gradle.kts`](../app/build.gradle.kts) líneas 2-3, 67+ |
| `local.properties.example` | [`local.properties.example`](../local.properties.example) |
| Reglas Proguard | [`app/proguard-rules.pro`](../app/proguard-rules.pro) |

---

> **Nota:** El workflow actual no incluye un paso de SonarQube en CI porque requiere un servidor accesible públicamente o autohosteado. En un entorno de producción real se añadiría `sonarcloud` (SaaS gratuito para repos públicos) o se configuraría un runner self-hosted con acceso al Docker local.
