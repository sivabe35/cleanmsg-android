package com.siva.cleanmsg.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.siva.cleanmsg.app.data.local.dao.CategoryDao
import com.siva.cleanmsg.app.data.local.dao.SmsMessageDao
import com.siva.cleanmsg.app.data.local.dao.UserRuleDao
import com.siva.cleanmsg.app.data.local.entity.CategoryEntity
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity
import com.siva.cleanmsg.app.data.local.entity.UserRuleEntity

@Database(
    entities = [SmsMessageEntity::class, UserRuleEntity::class, CategoryEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun userRuleDao(): UserRuleDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "cleanmsg.db"
    }
}
