[← Volver al README](../README.md)

# 03 — Persistencia Room y APIs REST

## Tabla de contenidos
1. [Base de datos Room](#1-base-de-datos-room)
2. [GameDao — operaciones disponibles](#2-gamedao--operaciones-disponibles)
3. [GameEntity y TypeConverter](#3-gameentity-y-typeconverter)
4. [Mappers dominio ↔ entidad](#4-mappers-dominio--entidad)
5. [RetrofitClient](#5-retrofitclient)
6. [Autenticación IGDB — IgdbAuthInterceptor](#6-autenticación-igdb--igdbauthinterceptor)
7. [IgdbTokenProvider](#7-igdbtokenprovider)
8. [Estrategia de caché offline](#8-estrategia-de-caché-offline)
9. [Trazabilidad](#9-trazabilidad)

---

## 1. Base de datos Room

[`AppDatabase.kt`](../app/src/main/java/com/pamn/gamematch/data/local/AppDatabase.kt) define la base de datos Room con la entidad `GameEntity` y el converter `PlatformsConverter`:

```kotlin
@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
@TypeConverters(PlatformsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gamematch_database"
                ).build().also { INSTANCE = it }
            }
    }
}
```

La instancia es un **singleton thread-safe** con doble verificación de lock.

---

## 2. GameDao — operaciones disponibles

[`GameDao.kt`](../app/src/main/java/com/pamn/gamematch/data/local/GameDao.kt) expone 4 operaciones `suspend`:

```kotlin
@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    suspend fun getAll(): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<GameEntity>)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun count(): Int

    @Query("DELETE FROM games")
    suspend fun clear()
}
```

`upsertAll` con `OnConflictStrategy.REPLACE` garantiza que re-sincronizar el catálogo nunca genera duplicados.

---

## 3. GameEntity y TypeConverter

`GameEntity` representa una fila en la tabla `games`. La lista de plataformas (`List<String>`) no es un tipo nativo SQLite, por lo que `PlatformsConverter` la serializa como cadena JSON:

```kotlin
class PlatformsConverter {
    @TypeConverter
    fun fromString(value: String): List<String> =
        Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromList(list: List<String>): String =
        Gson().toJson(list)
}
```

---

## 4. Mappers dominio ↔ entidad

El paquete `data/local/mapper/` separa el modelo de dominio (`Game`) de la entidad Room (`GameEntity`):

```kotlin
// GameEntity → Game (dominio)
fun GameEntity.toDomain(): Game = Game(
    id = this.id,
    title = this.title,
    platforms = this.platforms,
    coverUrl = this.coverUrl,
    // ...
)

// Game → GameEntity (persistencia)
fun Game.toEntity(): GameEntity = GameEntity(
    id = this.id,
    title = this.title,
    platforms = this.platforms,
    coverUrl = this.coverUrl,
    // ...
)
```

---

## 5. RetrofitClient

[`RetrofitClient.kt`](../app/src/main/java/com/pamn/gamematch/data/remote/RetrofitClient.kt) configura tres clientes Retrofit como `object` lazy:

| Servicio | Base URL | Auth |
|---|---|---|
| `twitchAuthApiService` | `https://id.twitch.tv/` | Ninguna (obtiene token) |
| `igdbApiService` | `https://api.igdb.com/` | `IgdbAuthInterceptor` (Bearer) |
| `steamApiService` | `https://store.steampowered.com/` | Ninguna (API pública) |

El cliente IGDB usa `ScalarsConverterFactory` (primero) + `GsonConverterFactory` para manejar tanto respuestas de texto plano como JSON.

---

## 6. Autenticación IGDB — IgdbAuthInterceptor

[`IgdbAuthInterceptor.kt`](../app/src/main/java/com/pamn/gamematch/data/remote/auth/IgdbAuthInterceptor.kt) implementa `okhttp3.Interceptor` y añade cabeceras a cada petición:

```kotlin
class IgdbAuthInterceptor(private val tokenProvider: IgdbTokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.getAccessToken(forceRefresh = false) }
        val request = chain.request().newBuilder()
            .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .header("Authorization", "Bearer $token")
            .build()

        var response = chain.proceed(request)
        if (response.code == 401) {           // token expirado → refresco automático
            response.close()
            val newToken = runBlocking { tokenProvider.getAccessToken(forceRefresh = true) }
            response = chain.proceed(
                chain.request().newBuilder()
                    .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
                    .header("Authorization", "Bearer $newToken")
                    .build()
            )
        }
        return response
    }
}
```

El patrón **retry-on-401** garantiza recuperación transparente ante tokens expirados.

---

## 7. IgdbTokenProvider

`IgdbTokenProvider` obtiene el token OAuth2 de Twitch con `client_credentials` y lo cachea en memoria. Si `forceRefresh = true` o el token ha expirado, realiza una nueva llamada a `TwitchAuthApiService`.

```kotlin
class IgdbTokenProvider(private val twitchAuth: TwitchAuthApiService) {
    private var cachedToken: String? = null
    private var expiresAt: Long = 0L

    suspend fun getAccessToken(forceRefresh: Boolean): String {
        if (!forceRefresh && cachedToken != null && System.currentTimeMillis() < expiresAt) {
            return cachedToken!!
        }
        val response = twitchAuth.getToken(
            clientId     = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET,
            grantType    = "client_credentials"
        )
        cachedToken = response.accessToken
        expiresAt   = System.currentTimeMillis() + response.expiresIn * 1000L
        return cachedToken!!
    }
}
```

---

## 8. Estrategia de caché offline

`GameRepository` implementa una estrategia **Network-first with local fallback**:

1. Si Room tiene datos (`count() > 0`) y no se ha solicitado refresco forzado → devuelve caché local.
2. Si la red está disponible → sincroniza desde Firestore/IGDB y actualiza Room (`upsertAll`).
3. Si la red falla → devuelve la caché local sin lanzar excepción al usuario.

---

## 9. Trazabilidad

| Componente | Archivo |
|---|---|
| `AppDatabase` | [`AppDatabase.kt`](../app/src/main/java/com/pamn/gamematch/data/local/AppDatabase.kt) |
| `GameDao` | [`GameDao.kt`](../app/src/main/java/com/pamn/gamematch/data/local/GameDao.kt) |
| `GameEntity` | [`GameEntity.kt`](../app/src/main/java/com/pamn/gamematch/data/local/GameEntity.kt) |
| `PlatformsConverter` | [`PlatformsConverter.kt`](../app/src/main/java/com/pamn/gamematch/data/local/PlatformsConverter.kt) |
| `RetrofitClient` | [`RetrofitClient.kt`](../app/src/main/java/com/pamn/gamematch/data/remote/RetrofitClient.kt) |
| `IgdbAuthInterceptor` | [`IgdbAuthInterceptor.kt`](../app/src/main/java/com/pamn/gamematch/data/remote/auth/IgdbAuthInterceptor.kt) |
| `IgdbTokenProvider` | [`IgdbTokenProvider.kt`](../app/src/main/java/com/pamn/gamematch/data/remote/auth/IgdbTokenProvider.kt) |
| Mappers local | `data/local/mapper/` |
| Mappers remoto | `data/remote/mapper/` |

---

> **Nota:** Las claves `IGDB_CLIENT_ID` e `IGDB_CLIENT_SECRET` nunca se hardcodean en el código fuente. Se leen de `local.properties` y se inyectan como `BuildConfig` en tiempo de compilación (ver [`app/build.gradle.kts`](../app/build.gradle.kts) líneas 33-41).
