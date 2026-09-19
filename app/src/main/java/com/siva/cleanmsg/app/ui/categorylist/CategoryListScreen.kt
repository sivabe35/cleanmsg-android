package com.siva.cleanmsg.app.ui.categorylist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.siva.cleanmsg.app.CleanMsgApplication
import com.siva.cleanmsg.app.viewmodel.CategoryListUiState
import com.siva.cleanmsg.app.viewmodel.CategoryListViewModel
import com.siva.cleanmsg.app.viewmodel.MessageListItemUiModel
import com.siva.cleanmsg.app.viewmodel.SortOption
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    categoryKey: String,
    onBack: () -> Unit,
    onMessageClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as CleanMsgApplication).container.smsRepository
    val viewModel: CategoryListViewModel = viewModel(
        factory = viewModelFactory { initializer { CategoryListViewModel(repository, categoryKey) } }
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CategoryListTopBar(
                uiState = uiState,
                onBack = onBack,
                onClearSelection = viewModel::clearSelection,
                onSelectAll = viewModel::selectAll,
                onSortOptionSelected = viewModel::setSortOption,
                onDeleteClick = { showDeleteConfirmation = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            items(uiState.messages, key = { it.smsId }) { message ->
                MessageRow(
                    message = message,
                    isSelected = message.smsId in uiState.selectedSmsIds,
                    isSelectionMode = uiState.selectedSmsIds.isNotEmpty(),
                    onClick = {
                        if (uiState.selectedSmsIds.isNotEmpty()) {
                            viewModel.toggleSelection(message.smsId)
                        } else {
                            onMessageClick(message.smsId)
                        }
                    },
                    onLongClick = { viewModel.toggleSelection(message.smsId) }
                )
                HorizontalDivider()
            }
        }
    }

    if (showDeleteConfirmation) {
        val selectedCount = uiState.selectedSmsIds.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete $selectedCount message${if (selectedCount == 1) "" else "s"}?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    scope.launch {
                        viewModel.deleteSelected()
                        snackbarHostState.showSnackbar("Deleted $selectedCount message${if (selectedCount == 1) "" else "s"}")
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryListTopBar(
    uiState: CategoryListUiState,
    onBack: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onDeleteClick: () -> Unit
) {
    val selectionCount = uiState.selectedSmsIds.size
    if (selectionCount > 0) {
        TopAppBar(
            title = { Text(text = "$selectionCount selected") },
            navigationIcon = { TextButton(onClick = onClearSelection) { Text("✕") } },
            actions = {
                TextButton(onClick = onSelectAll) { Text("Select All") }
                TextButton(onClick = onDeleteClick) { Text("Delete") }
            }
        )
    } else {
        var sortMenuExpanded by remember { mutableStateOf(false) }
        TopAppBar(
            title = { Text(text = uiState.categoryDisplayName) },
            navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
            actions = {
                if (uiState.messages.isNotEmpty()) {
                    TextButton(onClick = onSelectAll) { Text("Select All") }
                }
                TextButton(onClick = { sortMenuExpanded = true }) { Text("Sort") }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.label) },
                            onClick = {
                                onSortOptionSelected(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageRow(
    message: MessageListItemUiModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        headlineContent = { Text(text = message.address) },
        supportingContent = {
            Column {
                Text(text = message.bodyPreview, maxLines = 3)
                Text(text = formatDate(message.date), style = MaterialTheme.typography.labelSmall)
            }
        },
        trailingContent = if (isSelectionMode) {
            { Text(text = if (isSelected) "✓" else "") }
        } else null
    )
}

private fun formatDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
