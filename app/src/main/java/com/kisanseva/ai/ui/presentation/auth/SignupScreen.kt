package com.kisanseva.ai.ui.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanseva.ai.R
import com.kisanseva.ai.ui.components.LoadingButton
import com.kisanseva.ai.ui.presentation.language.composable.languageOptions

@Composable
fun SignupScreen(onNavigateToLoginScreen: () -> Unit, onNavigateToOTPScreen: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUIState.OTPSent -> {
                onNavigateToOTPScreen(phone)
                viewModel.resetState()
            }
            is AuthUIState.Error -> {
                val message = context.getString(mapErrorCodeToStringResource(state.code))
                snackbarHostState.showSnackbar(message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text(stringResource(id = R.string.phone_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LoadingButton(
                text = stringResource(id = R.string.sign_up),
                isLoading = uiState is AuthUIState.Loading,
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.name_required),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@LoadingButton
                    }
                    if (phone.length != 10) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.invalid_phone_number),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@LoadingButton
                    }
                    viewModel.sendOtp(
                        phone = phone,
                        name = name,
                        language = languageOptions
                            .filter { it.code == language }[0]
                            .displayName
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onNavigateToLoginScreen() },
                enabled = uiState !is AuthUIState.Loading
            ) {
                Text(stringResource(id = R.string.have_account))
            }
        }
    }
}
