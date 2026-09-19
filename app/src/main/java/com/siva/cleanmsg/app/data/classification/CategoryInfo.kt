package com.siva.cleanmsg.app.data.classification

/** A category as shown in the UI — either one of the fixed built-ins or a user-added one. */
data class CategoryInfo(
    val key: String,
    val displayName: String,
    val colorHex: String?,
    val isBuiltIn: Boolean
)
