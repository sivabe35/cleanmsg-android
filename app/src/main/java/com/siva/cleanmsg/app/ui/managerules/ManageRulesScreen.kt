package com.siva.cleanmsg.app.ui.managerules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.siva.cleanmsg.app.CleanMsgApplication
import com.siva.cleanmsg.app.data.classification.CategoryInfo
import com.siva.cleanmsg.app.data.classification.MatchType
import com.siva.cleanmsg.app.viewmodel.ManageRulesViewModel
import com.siva.cleanmsg.app.viewmodel.RuleUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRulesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = (context.applicationContext as CleanMsgApplication).container.smsRepository
    val viewModel: ManageRulesViewModel = viewModel(
        factory = viewModelFactory { initializer { ManageRulesViewModel(repository) } }
    )
    val uiState by viewModel.uiState.collectAsState()

    var editingRule by remember { mutableStateOf<RuleUiModel?>(null) }
    var isAddingRule by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Rules") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = { TextButton(onClick = { isAddingRule = true }) { Text("+ Add") } }
            )
        }
    ) { innerPadding ->
        if (uiState.rules.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp)) {
                Text("No custom rules yet. Tap \"+ Add\" to create one.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(uiState.rules, key = { it.id }) { rule ->
                    ListItem(
                        modifier = Modifier.fillMaxWidth().clickable { editingRule = rule },
                        headlineContent = { Text("${rule.matchType.label()}: ${rule.matchValue}") },
                        supportingContent = { Text("→ ${rule.targetCategoryDisplayName}") },
                        trailingContent = {
                            TextButton(onClick = { viewModel.deleteRule(rule) }) { Text("Delete") }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (isAddingRule) {
        RuleEditorDialog(
            existingRule = null,
            availableCategories = uiState.availableCategories,
            onDismiss = { isAddingRule = false },
            onConfirm = { matchType, matchValue, categoryKey ->
                viewModel.addRule(matchType, matchValue, categoryKey)
                isAddingRule = false
            }
        )
    }

    editingRule?.let { rule ->
        RuleEditorDialog(
            existingRule = rule,
            availableCategories = uiState.availableCategories,
            onDismiss = { editingRule = null },
            onConfirm = { matchType, matchValue, categoryKey ->
                viewModel.updateRule(rule, matchType, matchValue, categoryKey)
                editingRule = null
            }
        )
    }
}

@Composable
private fun RuleEditorDialog(
    existingRule: RuleUiModel?,
    availableCategories: List<CategoryInfo>,
    onDismiss: () -> Unit,
    onConfirm: (MatchType, String, String) -> Unit
) {
    var matchType by remember { mutableStateOf(existingRule?.matchType ?: MatchType.KEYWORD) }
    var matchValue by remember { mutableStateOf(existingRule?.matchValue ?: "") }
    var targetCategoryKey by remember {
        mutableStateOf(existingRule?.targetCategoryKey ?: availableCategories.firstOrNull()?.key ?: "")
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var matchTypeMenuExpanded by remember { mutableStateOf(false) }

    val targetCategoryLabel = availableCategories.firstOrNull { it.key == targetCategoryKey }?.displayName ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingRule == null) "Add Rule" else "Edit Rule") },
        text = {
            Column {
                TextButton(onClick = { matchTypeMenuExpanded = true }) { Text("Match type: ${matchType.label()}") }
                DropdownMenu(expanded = matchTypeMenuExpanded, onDismissRequest = { matchTypeMenuExpanded = false }) {
                    // REGEX is a placeholder for a future version (PRD section 4) — not offered in v1.
                    listOf(MatchType.KEYWORD, MatchType.SENDER_ID).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label()) },
                            onClick = { matchType = option; matchTypeMenuExpanded = false }
                        )
                    }
                }

                OutlinedTextField(
                    value = matchValue,
                    onValueChange = { matchValue = it },
                    label = { Text(if (matchType == MatchType.KEYWORD) "Keyword" else "Sender ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = { categoryMenuExpanded = true }) { Text("Category: $targetCategoryLabel") }
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    availableCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = { targetCategoryKey = category.key; categoryMenuExpanded = false }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(matchType, matchValue, targetCategoryKey) },
                enabled = matchValue.isNotBlank() && targetCategoryKey.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun MatchType.label(): String = when (this) {
    MatchType.KEYWORD -> "Keyword"
    MatchType.SENDER_ID -> "Sender ID"
    MatchType.REGEX -> "Regex"
}
