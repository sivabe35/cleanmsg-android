package com.siva.cleanmsg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
