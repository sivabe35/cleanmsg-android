package com.siva.cleanmsg.app.data.classification

import android.content.Context
import org.json.JSONObject

/** Parses the read-only, seeded default rule config (PRD section 4) from assets. */
class BuiltInRuleLoader(private val context: Context) {

    val rules: List<ClassificationRule> by lazy { loadRules() }

    private fun loadRules(): List<ClassificationRule> {
        val json = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        val rulesArray = JSONObject(json).getJSONArray("rules")
        return buildList {
            for (i in 0 until rulesArray.length()) {
                val ruleJson = rulesArray.getJSONObject(i)
                val valuesJson = ruleJson.getJSONArray("values")
                val values = buildList { for (j in 0 until valuesJson.length()) add(valuesJson.getString(j)) }
                add(
                    ClassificationRule(
                        categoryKey = ruleJson.getString("category"),
                        matchType = MatchType.valueOf(ruleJson.getString("matchType")),
                        values = values
                    )
                )
            }
        }
    }

    private companion object {
        const val ASSET_FILE_NAME = "classification_rules.json"
    }
}
