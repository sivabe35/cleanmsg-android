package com.siva.cleanmsg.app.ui.messagedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.siva.cleanmsg.app.viewmodel.MessageDetailViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    smsId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as CleanMsgApplication).container.smsRepository
    val viewModel: MessageDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { MessageDetailViewModel(repository, smsId) } }
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { innerPadding ->
        if (!uiState.isFound) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp)) {
                Text("Message not found.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = uiState.address, style = MaterialTheme.typography.titleLarge)
            Text(text = formatFullDateTime(uiState.date), style = MaterialTheme.typography.bodyMedium)

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = uiState.body, modifier = Modifier.padding(16.dp))
            }

            ReclassifySection(
                currentCategoryDisplayName = uiState.categoryDisplayName,
                availableCategories = uiState.availableCategories,
                onCategorySelected = viewModel::reclassify
            )
        }
    }
}

@Composable
private fun ReclassifySection(
    currentCategoryDisplayName: String,
    availableCategories: List<CategoryInfo>,
    onCategorySelected: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column {
        Text(text = "Category: $currentCategoryDisplayName", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { menuExpanded = true }) { Text("Reclassify") }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.displayName) },
                    onClick = {
                        onCategorySelected(category.key)
                        menuExpanded = false
                    }
                )
            }
        }
    }
}

private fun formatFullDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
