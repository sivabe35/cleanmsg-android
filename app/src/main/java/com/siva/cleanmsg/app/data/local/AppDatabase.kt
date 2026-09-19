package com.siva.cleanmsg.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity

@Database(
    entities = [SmsMessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "cleanmsg.db"
    }
}
