package com.siva.cleanmsg.app.sms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.siva.cleanmsg.app.R
import com.siva.cleanmsg.app.ui.theme.CleanMsgTheme

/**
 * Minimal stub for SENDTO/VIEW intents. CleanMsg is a cleanup tool, not a messaging
 * app — sending/composing is out of scope for v1 (see PRD section 5).
 */
class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanMsgTheme {
                ComposeSmsStubScreen(onClose = { finish() })
            }
        }
    }
}

@Composable
private fun ComposeSmsStubScreen(onClose: () -> Unit) {
    Scaffold { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.compose_sms_stub_message),
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onClose, modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        }
    }
}
