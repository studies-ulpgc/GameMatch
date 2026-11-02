package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    fun signUp(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun updatePassword(newPass: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "Usuario no autenticado")
        user.updatePassword(newPass)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "Usuario no autenticado")
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