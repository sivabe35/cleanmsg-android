package com.siva.cleanmsg.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siva.cleanmsg.app.data.classification.CategoryInfo
import com.siva.cleanmsg.app.data.classification.MatchType
import com.siva.cleanmsg.app.data.local.entity.UserRuleEntity
import com.siva.cleanmsg.app.repository.SmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RuleUiModel(
    val id: Long,
    val matchType: MatchType,
    val matchValue: String,
    val targetCategoryKey: String,
    val targetCategoryDisplayName: String
)

data class ManageRulesUiState(
    val rules: List<RuleUiModel> = emptyList(),
    val availableCategories: List<CategoryInfo> = emptyList()
)

class ManageRulesViewModel(private val repository: SmsRepository) : ViewModel() {

    val uiState: StateFlow<ManageRulesUiState> = combine(
        repository.observeRules(),
        repository.observeAllCategories()
    ) { rules, categories ->
        val nameByKey = categories.associate { it.key to it.displayName }
        ManageRulesUiState(
            rules = rules.mapNotNull { rule ->
                val matchType = runCatching { MatchType.valueOf(rule.matchType) }.getOrNull() ?: return@mapNotNull null
                RuleUiModel(
                    id = rule.id,
                    matchType = matchType,
                    matchValue = rule.matchValue,
                    targetCategoryKey = rule.targetCategoryKey,
                    targetCategoryDisplayName = nameByKey[rule.targetCategoryKey] ?: rule.targetCategoryKey
                )
            },
            availableCategories = categories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageRulesUiState())

    fun addRule(matchType: MatchType, matchValue: String, targetCategoryKey: String) {
        viewModelScope.launch { repository.addRule(matchType, matchValue, targetCategoryKey) }
    }

    fun updateRule(rule: RuleUiModel, matchType: MatchType, matchValue: String, targetCategoryKey: String) {
        viewModelScope.launch {
            repository.updateRule(
                UserRuleEntity(
                    id = rule.id,
                    matchType = matchType.name,
                    matchValue = matchValue,
                    targetCategoryKey = targetCategoryKey
                )
            )
        }
    }

    fun deleteRule(rule: RuleUiModel) {
        viewModelScope.launch {
            repository.deleteRule(
                UserRuleEntity(
                    id = rule.id,
                    matchType = rule.matchType.name,
                    matchValue = rule.matchValue,
                    targetCategoryKey = rule.targetCategoryKey
                )
            )
        }
    }
}
