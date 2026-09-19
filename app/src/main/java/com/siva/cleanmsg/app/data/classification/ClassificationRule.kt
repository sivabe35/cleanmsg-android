package com.siva.cleanmsg.app.data.classification

/** REGEX is a placeholder for a future version (PRD section 12) — not implemented in v1. */
enum class MatchType {
    KEYWORD,
    SENDER_ID,
    REGEX
}

data class ClassificationRule(
    val categoryKey: String,
    val matchType: MatchType,
    val values: List<String>
) {
    fun matches(address: String, body: String): Boolean = when (matchType) {
        MatchType.SENDER_ID -> values.any { address.contains(it, ignoreCase = true) }
        MatchType.KEYWORD -> values.any { body.contains(it, ignoreCase = true) }
        MatchType.REGEX -> false
    }
}
