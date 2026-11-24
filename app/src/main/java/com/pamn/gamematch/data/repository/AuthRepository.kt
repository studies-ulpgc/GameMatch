package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun getCurrentUserName(defaultName: String): String {
        val user = auth.currentUser ?: return defaultName
        val displayName = user.displayName
        if (!displayName.isNullOrBlank()) return displayName
        val email = user.email
        if (!email.isNullOrBlank()) return email.substringBefore("@")
        return defaultName
    }

    fun login(email: String, pass: String, onResult: (Boolean, Exception?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception)
                }
            }
    }

    fun signUp(email: String, pass: String, onResult: (Boolean, Exception?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun updatePassword(newPass: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, null)
        user.updatePassword(newPass)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, null)
        val uid = user.uid

        // 1. Borrar datos de Firestore
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // 2. Borrar credencial de Auth
                user.delete()
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.localizedMessage) }
            }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }
}
