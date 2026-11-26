package com.pamn.gamematch.data.repository

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private val db: FirebaseFirestore = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val user: FirebaseUser = mockk(relaxed = true)
    private val usersCollection: CollectionReference = mockk(relaxed = true)
    private val userDocRef: DocumentReference = mockk(relaxed = true)
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        every { auth.currentUser } returns user
        every { user.uid } returns "uid-1"
        every { db.collection("users") } returns usersCollection
        every { usersCollection.document("uid-1") } returns userDocRef
        repository = SettingsRepository(db, auth)
    }

    private fun <T> successTask(result: T): Task<T> {
        val task = mockk<Task<T>>(relaxed = true)
        val slot = slot<OnSuccessListener<in T>>()
        every { task.addOnSuccessListener(capture(slot)) } answers {
            slot.captured.onSuccess(result)
            task
        }
        return task
    }

    private fun <T> failureTask(exception: Exception): Task<T> {
        val task = mockk<Task<T>>(relaxed = true)
        every { task.addOnSuccessListener(any()) } returns task
        val slot = slot<OnFailureListener>()
        every { task.addOnFailureListener(capture(slot)) } answers {
            slot.captured.onFailure(exception)
            task
        }
        return task
    }

    @Test
    fun getSettings_whenUnauthenticated_returnsNull() {
        every { auth.currentUser } returns null
        var received: UserSettings? = UserSettings()

        repository.getSettings { received = it }

        assertNull(received)
        verify(exactly = 0) { db.collection(any()) }
    }

    @Test
    fun getSettings_whenDocumentExists_parsesDarkModeFallbackAndLanguage() {
        val doc = mockk<DocumentSnapshot>(relaxed = true)
        every { doc.exists() } returns true
        every { doc.getBoolean("isDarkMode") } returns null
        every { doc.getBoolean("darkMode") } returns false
        every { doc.getString("language") } returns "English"
        every { userDocRef.get() } returns successTask(doc)

        var received: UserSettings? = null
        repository.getSettings { received = it }

        assertEquals(UserSettings(isDarkMode = false, language = "en"), received)
    }

    @Test
    fun getSettings_whenDocumentMissingOrFailure_returnsNull() {
        val missingDoc = mockk<DocumentSnapshot>(relaxed = true)
        every { missingDoc.exists() } returns false
        every { userDocRef.get() } returns successTask(missingDoc)

        var received: UserSettings? = UserSettings()
        repository.getSettings { received = it }
        assertNull(received)

        every { userDocRef.get() } returns failureTask(RuntimeException("network"))
        received = UserSettings()
        repository.getSettings { received = it }
        assertNull(received)
    }

    @Test
    fun saveDarkMode_handlesUnauthenticatedSuccessAndFailure() {
        every { auth.currentUser } returns null
        var result = true
        repository.saveDarkMode(false) { result = it }
        assertFalse(result)

        every { auth.currentUser } returns user
        every { userDocRef.set(any(), any<SetOptions>()) } returns successTask(mockk<Void>(relaxed = true))
        repository.saveDarkMode(false) { result = it }
        assertTrue(result)
        verify { userDocRef.set(mapOf("isDarkMode" to false, "darkMode" to false), SetOptions.merge()) }

        every { userDocRef.set(any(), any<SetOptions>()) } returns failureTask(RuntimeException("error"))
        repository.saveDarkMode(true) { result = it }
        assertFalse(result)
    }

    @Test
    fun saveLanguage_normalizesAndHandlesUnauthenticatedSuccessAndFailure() {
        every { auth.currentUser } returns null
        var result = true
        repository.saveLanguage("en") { result = it }
        assertFalse(result)

        every { auth.currentUser } returns user
        every { userDocRef.set(any(), any<SetOptions>()) } returns successTask(mockk<Void>(relaxed = true))
        repository.saveLanguage("English") { result = it }
        assertTrue(result)
        verify { userDocRef.set(mapOf("language" to "en"), SetOptions.merge()) }

        every { userDocRef.set(any(), any<SetOptions>()) } returns failureTask(RuntimeException("error"))
        repository.saveLanguage("es") { result = it }
        assertFalse(result)
    }
}
