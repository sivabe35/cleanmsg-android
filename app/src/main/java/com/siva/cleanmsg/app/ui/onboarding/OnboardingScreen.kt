package com.siva.cleanmsg.app.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siva.cleanmsg.app.R
import com.siva.cleanmsg.app.util.SmsRoleUtils
import com.siva.cleanmsg.app.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) onOnboardingComplete()
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshStatus() }

    val requestDefaultSmsAppLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshStatus() }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)
            Text(text = stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyMedium)

            OnboardingStepCard(
                title = stringResource(R.string.onboarding_step1_title),
                description = stringResource(R.string.onboarding_step1_description),
                isDone = uiState.hasReadSmsPermission,
                doneLabel = stringResource(R.string.onboarding_permission_granted),
                actionLabel = stringResource(R.string.onboarding_grant_permission),
                actionEnabled = true,
                onAction = { requestPermissionLauncher.launch(android.Manifest.permission.READ_SMS) }
            )

            if (!uiState.isSmsRoleAvailable) {
                Text(text = stringResource(R.string.onboarding_role_unavailable), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { context.startActivity(appSettingsIntent(context.packageName)) }) {
                    Text(text = stringResource(R.string.onboarding_open_settings))
                }
            } else {
                OnboardingStepCard(
                    title = stringResource(R.string.onboarding_step2_title),
                    description = stringResource(R.string.onboarding_step2_description),
                    isDone = uiState.isDefaultSmsApp,
                    doneLabel = stringResource(R.string.onboarding_default_set),
                    actionLabel = stringResource(R.string.onboarding_set_default),
                    actionEnabled = uiState.hasReadSmsPermission,
                    onAction = {
                        requestDefaultSmsAppLauncher.launch(SmsRoleUtils.createRequestDefaultSmsAppIntent(context))
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepCard(
    title: String,
    description: String,
    isDone: Boolean,
    doneLabel: String,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            if (isDone) {
                Text(text = doneLabel, style = MaterialTheme.typography.bodyMedium)
            } else {
                Button(onClick = onAction, enabled = actionEnabled) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

private fun appSettingsIntent(packageName: String): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
