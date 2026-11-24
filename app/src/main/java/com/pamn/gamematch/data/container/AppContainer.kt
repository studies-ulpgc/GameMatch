package com.pamn.gamematch.data.container

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.data.local.AppDatabase
import com.pamn.gamematch.data.repository.AuthRepository
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.data.repository.SettingsRepository

interface AppContainer {
    val authRepository: AuthRepository
    val communityRepository: CommunityRepository
    val gameRepository: GameRepository
    val settingsRepository: SettingsRepository
}

class AppContainerImpl(
    private val database: AppDatabase
) : AppContainer {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(auth = firebaseAuth, db = firestore)
    }
    override val communityRepository: CommunityRepository by lazy {
        CommunityRepository(db = firestore, auth = firebaseAuth)
    }
    override val gameRepository: GameRepository by lazy {
        GameRepository(db = firestore, auth = firebaseAuth, gameDao = database.gameDao())
    }
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(db = firestore, auth = firebaseAuth)
    }
}
