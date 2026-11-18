package com.pamn.gamematch

import android.app.Application
import com.pamn.gamematch.data.local.AppDatabase

class GameMatchApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
    }

    companion object {
        private var instance: GameMatchApp? = null
        private var database: AppDatabase? = null

        fun getDatabase(): AppDatabase? = database
    }
}
