package com.kisanseva.ai.ui.presentation.main.alerts

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AlertListScreen(onNavigateToAlert: (String) -> Unit) {
    Button(onClick = { onNavigateToAlert("alertId") }) {
        Text("Go to Alert")
    }
}