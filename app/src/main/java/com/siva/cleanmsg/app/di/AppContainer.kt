package com.siva.cleanmsg.app.di

import android.content.Context
import androidx.room.Room
import com.siva.cleanmsg.app.data.classification.BuiltInRuleLoader
import com.siva.cleanmsg.app.data.classification.ClassificationEngine
import com.siva.cleanmsg.app.data.local.AppDatabase
import com.siva.cleanmsg.app.repository.SmsRepository
import com.siva.cleanmsg.app.repository.SmsRepositoryImpl

interface AppContainer {
    val database: AppDatabase
    val smsRepository: SmsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    private val classificationEngine: ClassificationEngine by lazy {
        ClassificationEngine(BuiltInRuleLoader(context))
    }

    override val smsRepository: SmsRepository by lazy {
        SmsRepositoryImpl(
            context,
            database.smsMessageDao(),
            database.userRuleDao(),
            database.categoryDao(),
            classificationEngine
        )
    }
}
