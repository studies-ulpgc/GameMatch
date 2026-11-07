package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Obtiene los perfiles de usuario de la colección 'users'.
     */
    suspend fun getCommunityUsers(): Result<List<UserProfile>> {
        return try {
            val querySnapshot = db.collection("users").get().await()
            val profiles = querySnapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val username = doc.getString("username") ?: ""
                val email = doc.getString("email") ?: ""
                val birthYear = doc.getString("birthYear") ?: ""
                val language = com.pamn.gamematch.utils.LocaleUtils.normalizeLanguage(doc.getString("language"))
                val minPrice = (doc.getDouble("minPrice") ?: 0.0)
                val maxPrice = (doc.getDouble("maxPrice") ?: 100.0)
                val onlyReleased = doc.getBoolean("onlyReleased") ?: true
                val isDarkMode = doc.getBoolean("isDarkMode") ?: true
                val preferredGenres = (doc.get("preferredGenres") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val profilePictureUrl = doc.getString("profilePictureUrl") ?: ""

                UserProfile(
                    uid = uid,
                    username = username,
                    email = email,
                    birthYear = birthYear,
                    language = language,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    onlyReleased = onlyReleased,
                    isDarkMode = isDarkMode,
                    preferredGenres = preferredGenres,
                    profilePictureUrl = profilePictureUrl
                )
            }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las salas de juego desde la colección 'rooms' ordenadas por timestamp descendente.
     */
    suspend fun getRooms(): Result<List<GameRoom>> {
        return try {
            val querySnapshot = db.collection("rooms").get().await()
            val rooms = querySnapshot.documents.map { doc ->
                val membersList = (doc.get("members") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()

                GameRoom(
                    id = doc.id,
                    hostId = doc.getString("hostId") ?: "",
                    hostName = doc.getString("hostName") ?: "Anfitrión",
                    gameTitle = doc.getString("gameTitle") ?: "",
                    description = doc.getString("description") ?: "",
                    platform = doc.getString("platform") ?: "PC",
                    contactInfo = doc.getString("contactInfo") ?: "",
                    maxPlayers = (doc.getLong("maxPlayers") ?: 4L).toInt(),
                    currentPlayers = (doc.getLong("currentPlayers") ?: 1L).toInt(),
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    isFollowersOnly = doc.getBoolean("isFollowersOnly") ?: false,
                    members = membersList
                )
            }.sortedByDescending { it.timestamp }
            Result.success(rooms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una nueva sala en la colección 'rooms'.
     */
    suspend fun createRoom(room: GameRoom): Result<Unit> {
        return try {
            val data = hashMapOf(
                "hostId" to room.hostId,
                "hostName" to room.hostName,
                "gameTitle" to room.gameTitle,
                "description" to room.description,
                "platform" to room.platform,
                "contactInfo" to room.contactInfo,
                "maxPlayers" to room.maxPlayers,
                "currentPlayers" to room.currentPlayers,
                "timestamp" to room.timestamp,
                "isFollowersOnly" to room.isFollowersOnly,
                "members" to room.members
            )
            db.collection("rooms").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza los datos de una sala existente en Cloud Firestore.
     */
    suspend fun updateRoom(
        roomId: String,
        gameTitle: String,
        description: String,
        platform: String,
        contactInfo: String,
        maxPlayers: Int,
        isFollowersOnly: Boolean
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "gameTitle" to gameTitle,
                "description" to description,
                "platform" to platform,
                "contactInfo" to contactInfo,
                "maxPlayers" to maxPlayers,
                "isFollowersOnly" to isFollowersOnly
            )
            db.collection("rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una sala existente de la colección 'rooms' en Cloud Firestore.
     */
    suspend fun deleteRoom(roomId: String): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Añade un usuario a los miembros de la sala e incrementa atómicamente el contador de jugadores.
     */
    suspend fun joinRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).update(
                "members", FieldValue.arrayUnion(userId),
                "currentPlayers", FieldValue.increment(1)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remueve un usuario de los miembros de la sala y decrementa atómicamente el contador de jugadores.
     */
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).update(
                "members", FieldValue.arrayRemove(userId),
                "currentPlayers", FieldValue.increment(-1)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Expulsa a un miembro específico de una sala por parte del anfitrión en Cloud Firestore.
     */
    suspend fun removeMemberFromRoom(roomId: String, memberUid: String): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).update(
                "members", FieldValue.arrayRemove(memberUid),
                "currentPlayers", FieldValue.increment(-1)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Consulta la información de perfil de una lista de UIDs de miembros.
     */
    suspend fun getMembersProfiles(memberUids: List<String>): Result<List<UserProfile>> {
        if (memberUids.isEmpty()) return Result.success(emptyList())
        return try {
            val querySnapshot = db.collection("users").get().await()
            val profiles = querySnapshot.documents.mapNotNull { doc ->
                if (doc.id in memberUids) {
                    UserProfile(
                        uid = doc.id,
                        username = doc.getString("username") ?: "Jugador",
                        email = doc.getString("email") ?: "",
                        birthYear = doc.getString("birthYear") ?: "",
                        language = com.pamn.gamematch.utils.LocaleUtils.normalizeLanguage(doc.getString("language")),
                        preferredGenres = (doc.get("preferredGenres") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList()
                    )
                } else null
            }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda la relación de seguimiento en la subcolección 'users/{currentUid}/following/{targetUserId}'.
     */
    suspend fun followUser(targetUserId: String): Result<Unit> {
        val currentUid = getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Usuario no autenticado"))

        return try {
            val data = hashMapOf("timestamp" to System.currentTimeMillis())
            db.collection("users").document(currentUid)
                .collection("following").document(targetUserId)
                .set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina el documento de la subcolección 'users/{currentUid}/following/{targetUserId}'.
     */
    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val currentUid = getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Usuario no autenticado"))

        return try {
            db.collection("users").document(currentUid)
                .collection("following").document(targetUserId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Consulta la subcolección 'following' del usuario autenticado y devuelve los UIDs seguidos.
     */
    suspend fun getFollowingUserIds(): Result<List<String>> {
        val currentUid = getCurrentUserId()
            ?: return Result.success(emptyList())

        return try {
            val snapshot = db.collection("users").document(currentUid)
                .collection("following").get().await()
            val followingIds = snapshot.documents.map { it.id }
            Result.success(followingIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}