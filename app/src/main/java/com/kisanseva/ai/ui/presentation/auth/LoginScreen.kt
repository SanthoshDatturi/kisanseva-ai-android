package com.kisanseva.ai.ui.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanseva.ai.R
import com.kisanseva.ai.ui.components.LoadingButton

@Composable
fun LoginScreen(onNavigateToSignUpScreen: () -> Unit, onNavigateToOTPScreen: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.login),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Welcome back! Enter your phone number to login",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text(stringResource(id = R.string.phone_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                prefix = { Text("+91 ", fontWeight = FontWeight.Bold) }
            )
            Spacer(modifier = Modifier.height(32.dp))
            LoadingButton(
                text = stringResource(id = R.string.login),
                isLoading = uiState is AuthUIState.Loading,
                onClick = {
                    if (phone.length == 10) {
                        viewModel.sendOtp(phone = phone)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.invalid_phone_number),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onNavigateToSignUpScreen,
                enabled = uiState !is AuthUIState.Loading
            ) {
                Text(
                    text = stringResource(id = R.string.no_account),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
