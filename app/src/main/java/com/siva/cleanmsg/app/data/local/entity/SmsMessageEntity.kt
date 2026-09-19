package com.siva.cleanmsg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.siva.cleanmsg.app.data.classification.BuiltInCategory

/**
 * Cached mirror of a row from the platform SMS ContentProvider (content://sms).
 * [smsId] is the provider's own _ID, reused as the primary key so re-scanning the
 * inbox is a plain upsert (OnConflictStrategy.REPLACE) rather than a duplicate insert.
 */
@Entity(tableName = "messages")
data class SmsMessageEntity(
    @PrimaryKey val smsId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int,
    val category: String = BuiltInCategory.UNCATEGORIZED.key,
    // Set by the Message Detail "reclassify" action; classifyAllMessages() skips
    // these so a manual override survives the next rule run or inbox re-scan.
    val isManuallyClassified: Boolean = false
)
