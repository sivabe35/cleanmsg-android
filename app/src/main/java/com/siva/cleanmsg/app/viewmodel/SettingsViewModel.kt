package com.siva.cleanmsg.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.siva.cleanmsg.app.BuildConfig
import com.siva.cleanmsg.app.CleanMsgApplication
import com.siva.cleanmsg.app.repository.SmsRepository
import com.siva.cleanmsg.app.util.SmsRoleUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val isDefaultSmsApp: Boolean = false,
    val isRescanning: Boolean = false,
    val appVersion: String = BuildConfig.VERSION_NAME
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmsRepository =
        (application as CleanMsgApplication).container.smsRepository

    private val _uiState = MutableStateFlow(SettingsUiState(isDefaultSmsApp = SmsRoleUtils.isDefaultSmsApp(application)))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun refreshDefaultSmsAppStatus() {
        _uiState.update { it.copy(isDefaultSmsApp = SmsRoleUtils.isDefaultSmsApp(getApplication())) }
    }

    /** A full, explicit re-scan — distinct from the routine scan-then-classify done on launch (PRD section 4). */
    suspend fun rescanInbox() {
        _uiState.update { it.copy(isRescanning = true) }
        repository.scanAndCacheInbox()
        repository.classifyAllMessages()
        _uiState.update { it.copy(isRescanning = false) }
    }
}
