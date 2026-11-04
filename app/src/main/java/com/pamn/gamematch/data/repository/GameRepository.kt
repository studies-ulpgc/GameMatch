package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.User

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Catálogo general de juegos de la aplicación.
     */
    fun getAllGames(): List<Game> {
        return listOf(
            Game(
                id = "1",
                title = "The Legend of Zelda: Tears of the Kingdom",
                description = "Aventura épica de exploración e investigación en un vasto mundo abierto sobre las tierras y los cielos de Hyrule.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
                genre = "Aventura",
                price = 59.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("Nintendo"),
                isFeatured = true,
                isTrending = false
            ),
            Game(
                id = "2",
                title = "GTA VI",
                description = "Siguiente entrega de la aclamada franquicia de acción y crimen en un impresionante mundo abierto hiperrealista.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
                genre = "Acción",
                price = 79.99,
                releaseDate = "Lanzamiento: Otoño 2026",
                isReleased = false,
                platforms = listOf("PlayStation", "Xbox"),
                isFeatured = true,
                isTrending = true
            ),
            Game(
                id = "3",
                title = "Hollow Knight: Silksong",
                description = "Explora el reino de Pharloom guiando a Hornet en una desafiante aventura de plataformas y combate 2D.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/367520/header.jpg",
                genre = "Indie",
                price = 19.99,
                releaseDate = "Por anunciar",
                isReleased = false,
                platforms = listOf("PC", "Nintendo", "PlayStation", "Xbox"),
                isFeatured = false,
                isTrending = true
            ),
            Game(
                id = "4",
                title = "Elden Ring",
                description = "Desafiante juego de rol y acción de ambiente fantástico creado por Hidetaka Miyazaki y George R. R. Martin.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
                genre = "RPG",
                price = 69.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("PC", "PlayStation", "Xbox"),
                isFeatured = true,
                isTrending = true
            ),
            Game(
                id = "5",
                title = "Cyberpunk 2077: Phantom Liberty",
                description = "Una aventura de intriga y espionaje ambientada en el distrito sin ley de Dogtown en Night City.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
                genre = "RPG",
                price = 29.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("PC", "PlayStation", "Xbox"),
                isFeatured = false,
                isTrending = true
            ),
            Game(
                id = "6",
                title = "Super Mario Bros. Wonder",
                description = "La evolución clásica del scroll lateral en 2D de Mario con sorprendentes Flores Maravilla.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/367520/header.jpg",
                genre = "Plataformas",
                price = 59.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("Nintendo"),
                isFeatured = true,
                isTrending = false
            ),
            Game(
                id = "7",
                title = "God of War Ragnarök",
                description = "Kratos y Atreus se embarcan en un viaje místico en busca de respuestas antes de la batalla del fin del mundo.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
                genre = "Acción",
                price = 69.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("PlayStation", "PC"),
                isFeatured = true,
                isTrending = false
            ),
            Game(
                id = "8",
                title = "Halo Infinite",
                description = "La legendaria saga regresa con la campaña de Master Chief más ambiciosa hasta la fecha.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
                genre = "Shooter",
                price = 39.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("Xbox", "PC"),
                isFeatured = false,
                isTrending = true
            )
        )
    }

    /**
     * Devuelve únicamente los juegos marcados como destacados (+ Destacado).
     */
    fun getFeaturedGames(): List<Game> {
        return getAllGames().filter { it.isFeatured }
    }

    /**
     * Devuelve únicamente los juegos marcados como en tendencia (+ Trending).
     */
    fun getTrendingGames(): List<Game> {
        return getAllGames().filter { it.isTrending }
    }

    fun saveLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)

        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayUnion(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                val userData = hashMapOf("likedGames" to listOf(gameId))
                db.collection("users").document(userId)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
    }

    fun removeLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)

        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayRemove(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Lee el array "likedGames" del documento del usuario autenticado.
     * Devuelve lista vacía si el documento o el campo aún no existen.
     */
    fun getLikedGames(onResult: (Result<List<String>>) -> Unit) {
        val userId = auth.currentUser?.uid
            ?: return onResult(Result.failure(IllegalStateException("Usuario no autenticado")))

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val ids = (doc.get("likedGames") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()
                onResult(Result.success(ids))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveUserPreferences(
        userModel: User,
        onResult: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onResult(false)

        db.collection("users").document(currentUser.uid)
            .set(userModel, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}