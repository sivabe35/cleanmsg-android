package com.siva.cleanmsg.app.ui.addeditcategory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.siva.cleanmsg.app.CleanMsgApplication
import com.siva.cleanmsg.app.viewmodel.AddEditCategoryViewModel
import kotlinx.coroutines.launch

private val PRESET_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
    "#2196F3", "#009688", "#4CAF50", "#FF9800"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    categoryKey: String?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as CleanMsgApplication).container.smsRepository
    val viewModel: AddEditCategoryViewModel = viewModel(
        factory = viewModelFactory { initializer { AddEditCategoryViewModel(repository, categoryKey) } }
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Category" else "Add Category") },
                navigationIcon = { TextButton(onClick = onDone) { Text("←") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::setName,
                label = { Text("Category name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color (optional)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PRESET_COLORS.forEach { hex ->
                    val isSelected = uiState.colorHex == hex
                    ColorSwatch(hex = hex, isSelected = isSelected, onClick = { viewModel.setColor(hex) })
                }
            }

            TextButton(
                onClick = {
                    scope.launch {
                        viewModel.save()
                        onDone()
                    }
                },
                enabled = uiState.name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun ColorSwatch(hex: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = Color(android.graphics.Color.parseColor(hex)), shape = CircleShape)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}
