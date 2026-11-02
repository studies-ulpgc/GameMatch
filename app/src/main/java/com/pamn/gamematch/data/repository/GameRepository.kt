package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    fun saveUserPreferences(
        username: String,
        genres: List<String>,
        minPrice: Float,
        maxPrice: Float,
        onlyReleased: Boolean,
        birthYear: String,
        isDarkMode: Boolean,
        language: String,
        profilePictureUrl: String = "https://api.dicebear.com/7.x/bottts/png?seed=default",
        onResult: (Boolean) -> Unit
    ) {
        val user = auth.currentUser ?: return onResult(false)

        val preferences = hashMapOf(
            "email" to (user.email ?: ""),
            "username" to username,
            "preferredGenres" to genres,
            "minPrice" to minPrice,
            "maxPrice" to maxPrice,
            "onlyReleased" to onlyReleased,
            "birthYear" to birthYear,
            "isDarkMode" to isDarkMode,
            "language" to language,
            "profilePictureUrl" to profilePictureUrl
        )

        db.collection("users").document(user.uid)
            .set(preferences, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}