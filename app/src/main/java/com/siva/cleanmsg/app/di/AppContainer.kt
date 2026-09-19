package com.siva.cleanmsg.app.di

import android.content.Context
import androidx.room.Room
import com.siva.cleanmsg.app.data.local.AppDatabase

interface AppContainer {
    val database: AppDatabase
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()
    }
}
