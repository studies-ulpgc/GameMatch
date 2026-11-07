package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.utils.LocaleUtils

data class UserSettings(
    val isDarkMode: Boolean = true,
    val language: String = "es"
)

class SettingsRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getSettings(onResult: (UserSettings?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val isDarkMode = doc.getBoolean("isDarkMode")
                        ?: doc.getBoolean("darkMode")
                        ?: true
                    val language = LocaleUtils.normalizeLanguage(doc.getString("language"))
                    onResult(UserSettings(isDarkMode = isDarkMode, language = language))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun saveDarkMode(isDarkMode: Boolean, onResult: ((Boolean) -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: run {
            onResult?.invoke(false)
            return
        }
        val data = mapOf(
            "isDarkMode" to isDarkMode,
            "darkMode" to isDarkMode
        )
        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onResult?.invoke(true) }
            .addOnFailureListener { onResult?.invoke(false) }
    }

    fun saveLanguage(language: String, onResult: ((Boolean) -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: run {
            onResult?.invoke(false)
            return
        }
        val normalized = LocaleUtils.normalizeLanguage(language)
        val data = mapOf("language" to normalized)
        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onResult?.invoke(true) }
            .addOnFailureListener { onResult?.invoke(false) }
    }
}
