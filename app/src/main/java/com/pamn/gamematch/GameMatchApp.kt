package com.pamn.gamematch

import android.app.Application
import com.pamn.gamematch.data.container.AppContainer
import com.pamn.gamematch.data.container.AppContainerImpl
import com.pamn.gamematch.data.local.AppDatabase

class GameMatchApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        container = AppContainerImpl(database)
    }
}
