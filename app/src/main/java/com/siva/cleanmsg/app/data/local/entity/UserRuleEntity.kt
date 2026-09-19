package com.siva.cleanmsg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User-defined classification rule (PRD section 4). [matchType] is stored as plain text
 * ("KEYWORD" | "SENDER_ID" today) rather than a Room enum converter so that "REGEX" can
 * be added as a future legal value without a schema migration.
 */
@Entity(tableName = "user_rules")
data class UserRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchType: String,
    val matchValue: String,
    val targetCategoryKey: String
)
