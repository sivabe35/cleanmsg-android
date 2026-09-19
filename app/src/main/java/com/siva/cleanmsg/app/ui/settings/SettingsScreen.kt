package com.siva.cleanmsg.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siva.cleanmsg.app.R
import com.siva.cleanmsg.app.util.SmsRoleUtils
import com.siva.cleanmsg.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val changeDefaultSmsAppLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshDefaultSmsAppStatus() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.settings_default_sms_heading), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    if (uiState.isDefaultSmsApp) R.string.settings_default_sms_is else R.string.settings_default_sms_is_not
                )
            )
            Button(onClick = {
                changeDefaultSmsAppLauncher.launch(SmsRoleUtils.createChangeDefaultSmsAppIntent(context))
            }) {
                Text(stringResource(R.string.settings_change_default))
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_inbox_heading), style = MaterialTheme.typography.titleMedium)
            if (uiState.isRescanning) {
                CircularProgressIndicator()
                Text(stringResource(R.string.settings_rescanning))
            } else {
                Button(onClick = {
                    scope.launch {
                        viewModel.rescanInbox()
                        snackbarHostState.showSnackbar(context.getString(R.string.settings_rescanned_snackbar))
                    }
                }) {
                    Text(stringResource(R.string.settings_rescan))
                }
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_about_heading), style = MaterialTheme.typography.titleMedium)
            Text(text = "${stringResource(R.string.app_name)} v${uiState.appVersion}")
        }
    }
}
