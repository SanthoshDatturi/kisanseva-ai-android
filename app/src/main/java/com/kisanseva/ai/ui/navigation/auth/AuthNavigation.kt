package com.kisanseva.ai.ui.navigation.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kisanseva.ai.ui.presentation.auth.LoginScreen
import com.kisanseva.ai.ui.presentation.auth.OTPScreen
import com.kisanseva.ai.ui.presentation.auth.SignupScreen
import kotlinx.serialization.Serializable


@Serializable
sealed interface AuthDest {
    @Serializable object Login : AuthDest
    @Serializable object Signup : AuthDest
    @Serializable data class OTP(val phone: String) : AuthDest
}

@Composable
fun AuthNavigation(onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AuthDest.Login) {
        composable<AuthDest.Login> {
            LoginScreen(
                onNavigateToSignUpScreen = {
                    navController.navigate(AuthDest.Signup) {
                        popUpTo(AuthDest.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToOTPScreen = { phone ->
                    navController.navigate(AuthDest.OTP(phone)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthDest.Signup> {
            SignupScreen(
                onNavigateToLoginScreen = {
                    navController.navigate(AuthDest.Login) {
                        popUpTo(AuthDest.Signup) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToOTPScreen = { phone ->
                    navController.navigate(AuthDest.OTP(phone)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthDest.OTP> {
            val args = it.toRoute<AuthDest.OTP>()
            OTPScreen(phone = args.phone, onSuccessfulVerification = onFinish)
        }
    }
}
