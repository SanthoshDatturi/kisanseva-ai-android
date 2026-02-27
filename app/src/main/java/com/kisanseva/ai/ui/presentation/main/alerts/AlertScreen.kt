package com.kisanseva.ai.ui.presentation.main.alerts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kisanseva.ai.R

@Composable
fun AlertScreen(alertId: String) {
    Text(stringResource(R.string.alert_id_label, alertId))
}