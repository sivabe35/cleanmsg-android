package com.siva.cleanmsg.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siva.cleanmsg.app.CleanMsgApplication
import com.siva.cleanmsg.app.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryCardUiModel(
    val key: String,
    val displayName: String,
    val count: Int,
    val isBuiltIn: Boolean
)

data class DashboardUiState(
    val isScanning: Boolean = false,
    val categoryCards: List<CategoryCardUiModel> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmsRepository =
        (application as CleanMsgApplication).container.smsRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            repository.scanAndCacheInbox()
            repository.classifyAllMessages()
            _uiState.update { it.copy(isScanning = false) }
        }
        viewModelScope.launch {
            combine(
                repository.observeAllCategories(),
                repository.observeCategoryCounts()
            ) { categories, counts ->
                val countByKey = counts.associate { it.category to it.count }
                // Every known category always shows, even at zero, so the dashboard
                // reads as a complete set of buckets rather than "whatever has mail".
                categories.map { category ->
                    CategoryCardUiModel(
                        key = category.key,
                        displayName = category.displayName,
                        count = countByKey[category.key] ?: 0,
                        isBuiltIn = category.isBuiltIn
                    )
                }
            }.collect { cards ->
                _uiState.update { it.copy(categoryCards = cards) }
            }
        }
    }
}
