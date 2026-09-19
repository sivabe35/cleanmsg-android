package com.siva.cleanmsg.app.data.classification

/**
 * Fixed v1 category set (PRD section 4). Unlike future user-created categories,
 * these never live in Room — their [key] is the stable string every message's
 * `category` column and every rule's `targetCategoryKey` ultimately points at.
 */
enum class BuiltInCategory(val key: String, val displayName: String) {
    OTP("OTP", "OTP / Verification"),
    BANKING("BANKING", "Banking / Transactional"),
    PROMOTIONAL("PROMOTIONAL", "Promotional"),
    PERSONAL("PERSONAL", "Personal"),
    UNCATEGORIZED("UNCATEGORIZED", "Uncategorized")
}
