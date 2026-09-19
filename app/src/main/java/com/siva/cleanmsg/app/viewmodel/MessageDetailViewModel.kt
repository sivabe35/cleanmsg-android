package com.siva.cleanmsg.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siva.cleanmsg.app.data.classification.CategoryInfo
import com.siva.cleanmsg.app.repository.SmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MessageDetailUiState(
    val isFound: Boolean = false,
    val address: String = "",
    val body: String = "",
    val date: Long = 0L,
    val categoryKey: String = "",
    val categoryDisplayName: String = "",
    val availableCategories: List<CategoryInfo> = emptyList()
)

class MessageDetailViewModel(
    private val repository: SmsRepository,
    private val smsId: Long
) : ViewModel() {

    val uiState: StateFlow<MessageDetailUiState> = combine(
        repository.observeMessage(smsId),
        repository.observeAllCategories()
    ) { message, categories ->
        if (message == null) {
            MessageDetailUiState(isFound = false, availableCategories = categories)
        } else {
            MessageDetailUiState(
                isFound = true,
                address = message.address,
                body = message.body,
                date = message.date,
                categoryKey = message.category,
                categoryDisplayName = categories.firstOrNull { it.key == message.category }?.displayName
                    ?: message.category,
                availableCategories = categories
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MessageDetailUiState())

    fun reclassify(newCategoryKey: String) {
        viewModelScope.launch { repository.reclassifyMessage(smsId, newCategoryKey) }
    }
}
