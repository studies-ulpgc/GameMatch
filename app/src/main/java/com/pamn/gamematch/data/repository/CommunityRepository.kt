package com.pamn.gamematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.model.GameRoom
import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.utils.LocaleUtils
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCommunityUsers(): Result<List<UserProfile>> {
        return try {
            val querySnapshot = db.collection("users").get().await()
            val profiles = querySnapshot.documents.map { it.toUserProfile() }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun deleteRoom(roomId: String): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinRoom(roomId: String, userId: String): Result<Unit> =
        updateRoomMembership(roomId, FieldValue.arrayUnion(userId), 1L)

    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> =
        updateRoomMembership(roomId, FieldValue.arrayRemove(userId), -1L)

    suspend fun removeMemberFromRoom(roomId: String, memberUid: String): Result<Unit> =
        updateRoomMembership(roomId, FieldValue.arrayRemove(memberUid), -1L)

    private suspend fun updateRoomMembership(
        roomId: String,
        memberOperation: FieldValue,
        playerDelta: Long
    ): Result<Unit> {
        return try {
            db.collection("rooms").document(roomId).update(
                "members", memberOperation,
                "currentPlayers", FieldValue.increment(playerDelta)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMembersProfiles(memberUids: List<String>): Result<List<UserProfile>> {
        if (memberUids.isEmpty()) return Result.success(emptyList())
        return try {
            val querySnapshot = db.collection("users").get().await()
            val profiles = querySnapshot.documents.mapNotNull { doc ->
                if (doc.id in memberUids) doc.toUserProfile(defaultUsername = "Jugador") else null
            }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getFollowingUserIds(): Result<List<String>> {
        val currentUid = getCurrentUserId() ?: return Result.success(emptyList())

        return try {
            val snapshot = db.collection("users").document(currentUid)
                .collection("following").get().await()
            Result.success(snapshot.documents.map { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toUserProfile(defaultUsername: String = ""): UserProfile {
        val preferredGenres = (get("preferredGenres") as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
        return UserProfile(
            uid = id,
            username = getString("username") ?: defaultUsername,
            email = getString("email") ?: "",
            birthYear = getString("birthYear") ?: "",
            language = LocaleUtils.normalizeLanguage(getString("language")),
            minPrice = getDouble("minPrice") ?: 0.0,
            maxPrice = getDouble("maxPrice") ?: 100.0,
            onlyReleased = getBoolean("onlyReleased") ?: true,
            isDarkMode = getBoolean("isDarkMode") ?: true,
            preferredGenres = preferredGenres,
            profilePictureUrl = getString("profilePictureUrl") ?: ""
        )
    }
}
