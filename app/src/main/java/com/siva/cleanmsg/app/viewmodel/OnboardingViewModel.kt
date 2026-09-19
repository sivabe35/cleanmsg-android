package com.siva.cleanmsg.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.siva.cleanmsg.app.util.SmsRoleUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnboardingUiState(
    val hasReadSmsPermission: Boolean = false,
    val isDefaultSmsApp: Boolean = false,
    val isSmsRoleAvailable: Boolean = true
) {
    val isOnboardingComplete: Boolean get() = hasReadSmsPermission && isDefaultSmsApp
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        val context = getApplication<Application>()
        _uiState.value = OnboardingUiState(
            hasReadSmsPermission = SmsRoleUtils.hasReadSmsPermission(context),
            isDefaultSmsApp = SmsRoleUtils.isDefaultSmsApp(context),
            isSmsRoleAvailable = SmsRoleUtils.isSmsRoleAvailable(context)
        )
    }
}
