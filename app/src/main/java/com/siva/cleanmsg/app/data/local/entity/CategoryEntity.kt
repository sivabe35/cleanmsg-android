package com.siva.cleanmsg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-added category (PRD section 5, item 7). Built-in categories are never stored here. */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val key: String,
    val name: String,
    val colorHex: String? = null
)
