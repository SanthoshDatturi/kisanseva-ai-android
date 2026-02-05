package com.kisanseva.ai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kisanseva.ai.ui.navigation.AppNavigation
import com.kisanseva.ai.ui.navigation.AppViewModel
import com.kisanseva.ai.ui.theme.KisanMithraTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: AppUiState by mutableStateOf(AppUiState.Loading)

        // Keep the splash screen on-screen until the UI state is loaded.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedIn.collect { isLoggedIn ->
                    if (isLoggedIn != null) {
                        uiState = AppUiState.Success(isLoggedIn)
                    }
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            uiState is AppUiState.Loading
        }

        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(
            window, false
        )

        setContent {
            KisanMithraTheme {
                if (uiState is AppUiState.Success) {
                    AppNavigation(isLoggedIn = (uiState as AppUiState.Success).isLoggedIn)
                }
            }
        }
    }
}

sealed interface AppUiState {
    data object Loading : AppUiState
    data class Success(val isLoggedIn: Boolean) : AppUiState
}
