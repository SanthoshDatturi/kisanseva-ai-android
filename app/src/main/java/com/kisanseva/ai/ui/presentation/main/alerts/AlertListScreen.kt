package com.kisanseva.ai.ui.presentation.main.alerts

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kisanseva.ai.R

@Composable
fun AlertListScreen(onNavigateToAlert: (String) -> Unit) {
    Button(onClick = { onNavigateToAlert("alertId") }) {
        Text(stringResource(R.string.go_to_alert))
    }
}