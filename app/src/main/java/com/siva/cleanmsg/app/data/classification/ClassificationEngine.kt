package com.siva.cleanmsg.app.data.classification

/**
 * Evaluation order (PRD section 4): user-defined rules first (so a user can override
 * a built-in match), then built-in rules, then structural fallbacks, then Uncategorized.
 */
class ClassificationEngine(private val builtInRuleLoader: BuiltInRuleLoader) {

    fun classify(address: String, body: String, userRules: List<ClassificationRule>): String {
        userRules.firstOrNull { it.matches(address, body) }?.let { return it.categoryKey }
        builtInRuleLoader.rules.firstOrNull { it.matches(address, body) }?.let { return it.categoryKey }

        if (isOtpShortCode(address)) return BuiltInCategory.OTP.key
        if (isPersonalNumber(address)) return BuiltInCategory.PERSONAL.key
        return BuiltInCategory.UNCATEGORIZED.key
    }

    /** Pure 4-6 digit numeric senders are typically OTP short codes (PRD section 4). */
    private fun isOtpShortCode(address: String): Boolean =
        address.length in 4..6 && address.all { it.isDigit() }

    /** Normal 10-digit numbers (optionally with a country code) are personal senders. */
    private fun isPersonalNumber(address: String): Boolean {
        val hasLetters = address.any { it.isLetter() }
        val digitCount = address.count { it.isDigit() }
        return !hasLetters && digitCount >= 9
    }
}
