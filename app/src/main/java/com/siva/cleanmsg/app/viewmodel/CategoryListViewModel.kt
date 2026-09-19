package com.siva.cleanmsg.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity
import com.siva.cleanmsg.app.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Sort directions the PRD names explicitly; Sender and Length each have a single fixed direction. */
enum class SortOption(val label: String) {
    DATE_NEWEST_FIRST("Date (newest first)"),
    DATE_OLDEST_FIRST("Date (oldest first)"),
    SENDER_A_TO_Z("Sender (A–Z)"),
    LENGTH("Length")
}

private const val BODY_PREVIEW_MAX_CHARS = 200

data class MessageListItemUiModel(
    val smsId: Long,
    val address: String,
    val bodyPreview: String,
    val date: Long
)

data class CategoryListUiState(
    val categoryDisplayName: String = "",
    val sortOption: SortOption = SortOption.DATE_NEWEST_FIRST,
    val messages: List<MessageListItemUiModel> = emptyList(),
    val selectedSmsIds: Set<Long> = emptySet()
)

class CategoryListViewModel(
    private val repository: SmsRepository,
    private val categoryKey: String
) : ViewModel() {

    private val sortOption = MutableStateFlow(SortOption.DATE_NEWEST_FIRST)
    private val selectedSmsIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<CategoryListUiState> = combine(
        repository.observeMessagesByCategory(categoryKey),
        repository.observeAllCategories(),
        sortOption,
        selectedSmsIds
    ) { messages, categories, sort, selected ->
        CategoryListUiState(
            categoryDisplayName = categories.firstOrNull { it.key == categoryKey }?.displayName ?: categoryKey,
            sortOption = sort,
            messages = messages.sortedWith(sort.comparator()).map { it.toUiModel() },
            selectedSmsIds = selected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryListUiState())

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    fun toggleSelection(smsId: Long) {
        selectedSmsIds.update { if (smsId in it) it - smsId else it + smsId }
    }

    fun clearSelection() {
        selectedSmsIds.value = emptySet()
    }

    fun selectAll() {
        selectedSmsIds.value = uiState.value.messages.map { it.smsId }.toSet()
    }

    /** Deletes the currently selected messages, then clears the selection. Caller drives its own UI feedback. */
    suspend fun deleteSelected() {
        repository.deleteMessages(selectedSmsIds.value)
        clearSelection()
    }

    private fun SmsMessageEntity.toUiModel() = MessageListItemUiModel(
        smsId = smsId,
        address = address,
        bodyPreview = if (body.length > BODY_PREVIEW_MAX_CHARS) body.take(BODY_PREVIEW_MAX_CHARS) + "…" else body,
        date = date
    )
}

private fun SortOption.comparator(): Comparator<SmsMessageEntity> = when (this) {
    SortOption.DATE_NEWEST_FIRST -> compareByDescending { it.date }
    SortOption.DATE_OLDEST_FIRST -> compareBy { it.date }
    SortOption.SENDER_A_TO_Z -> compareBy { it.address.lowercase() }
    SortOption.LENGTH -> compareByDescending { it.body.length }
}
