package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.data.model.User

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Catálogo general de juegos de la aplicación.
     */
    fun getAllGames(): List<com.pamn.gamematch.data.model.Game> {
        return listOf(
            com.pamn.gamematch.data.model.Game("1", "The Legend of Zelda", "Aventura épica de exploración e investigación en mundo abierto.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg", "Aventura", 59.99, "Disponible", true),
            com.pamn.gamematch.data.model.Game("2", "GTA VI", "Siguiente entrega de la aclamada franquicia de acción en mundo abierto.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg", "Acción", 79.99, "Lanzamiento: Otoño 2026", false),
            com.pamn.gamematch.data.model.Game("3", "Hollow Knight: Silksong", "Explora el reino de Pharloom guiando a Hornet en una aventura 2D.", "https://cdn.cloudflare.steamstatic.com/steam/apps/367520/header.jpg", "Indie", 19.99, "Por anunciar", false),
            com.pamn.gamematch.data.model.Game("4", "Elden Ring", "Desafiante juego de rol en un vasto e imponente mundo de fantasía.", "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg", "RPG", 69.99, "Disponible", true)
        )
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