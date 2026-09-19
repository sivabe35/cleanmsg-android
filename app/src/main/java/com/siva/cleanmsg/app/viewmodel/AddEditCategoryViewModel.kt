package com.siva.cleanmsg.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siva.cleanmsg.app.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditCategoryUiState(
    val isEditMode: Boolean = false,
    val name: String = "",
    val colorHex: String? = null
)

class AddEditCategoryViewModel(
    private val repository: SmsRepository,
    private val categoryKey: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCategoryUiState(isEditMode = categoryKey != null))
    val uiState: StateFlow<AddEditCategoryUiState> = _uiState.asStateFlow()

    init {
        if (categoryKey != null) {
            viewModelScope.launch {
                repository.getCategory(categoryKey)?.let { category ->
                    _uiState.update { it.copy(name = category.name, colorHex = category.colorHex) }
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun setColor(colorHex: String?) {
        _uiState.update { it.copy(colorHex = colorHex) }
    }

    suspend fun save() {
        val state = _uiState.value
        if (categoryKey == null) {
            repository.addCategory(state.name.trim(), state.colorHex)
        } else {
            repository.updateCategory(categoryKey, state.name.trim(), state.colorHex)
        }
    }
}
