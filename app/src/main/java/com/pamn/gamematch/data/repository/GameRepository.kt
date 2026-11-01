package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(false)
            return
        }

        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayUnion(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                // Si el documento de usuario no existe aun, lo crea
                val userData = hashMapOf("likedGames" to listOf(gameId))
                db.collection("users").document(userId)
                    .set(userData)
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

    fun saveUserPreferences(
        username: String,
        genres: List<String>,
        maxPrice: Float,
        onlyReleased: Boolean,
        birthYear: String,
        onResult: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onResult(false)

        val preferences = hashMapOf(
            "username" to username,
            "preferredGenres" to genres,
            "maxPrice" to maxPrice,
            "onlyReleased" to onlyReleased,
            "birthYear" to birthYear
        )

        db.collection("users").document(userId)
            .set(preferences, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}