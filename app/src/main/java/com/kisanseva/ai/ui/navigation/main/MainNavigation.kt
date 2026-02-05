package com.kisanseva.ai.ui.navigation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.ui.navigation.AlertDest
import com.kisanseva.ai.ui.navigation.AppDest
import com.kisanseva.ai.ui.navigation.ChatDest
import com.kisanseva.ai.ui.navigation.FarmDest
import com.kisanseva.ai.ui.navigation.MainDest
import com.kisanseva.ai.ui.navigation.PesticideDest
import com.kisanseva.ai.ui.navigation.main.components.BottomNavBar
import com.kisanseva.ai.ui.presentation.main.alerts.AlertListScreen
import com.kisanseva.ai.ui.presentation.main.chat.chatList.ChatListScreen
import com.kisanseva.ai.ui.presentation.main.farm.farmList.FarmListScreen
import com.kisanseva.ai.ui.presentation.main.home.HomeScreen
import com.kisanseva.ai.ui.presentation.main.pesticides.PesticidesScreen
import com.kisanseva.ai.ui.presentation.main.user.settings.SettingsScreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    appNavController: NavController
) {
    val context = LocalContext.current

    val (topBarTitle, setTopBarTitle) = remember { mutableStateOf(context.getString(R.string.home)) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, fontWeight = FontWeight.Bold) },
                actions = {
                    val isAlertList = currentDestination?.hasRoute<MainDest.AlertList>() == true
                    IconButton(onClick = {
                        navController.navigate(MainDest.AlertList) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isAlertList) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }

                    val isSettings = currentDestination?.hasRoute<MainDest.Settings>() == true
                    IconButton(onClick = {
                        navController.navigate(MainDest.Settings) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isSettings) Icons.Filled.ManageAccounts else Icons.Outlined.ManageAccounts,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController, context) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDest.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<MainDest.Home> {
                setTopBarTitle(context.getString(R.string.home))
                HomeScreen(
                    onNavigateToCultivatingCrop = { cropId ->
                        appNavController.navigate(FarmDest.CultivatingCrop(cropId = cropId))
                    }
                )
            }
            composable<MainDest.FarmList> {
                setTopBarTitle(context.getString(R.string.farms))
                FarmListScreen(
                    onNavigateToFarmProfile = { farmId -> appNavController.navigate(
                        FarmDest.FarmProfile(farmId)
                    ) },
                    onAddFarmProfile = { appNavController.navigate(ChatDest.Chat(
                            chatId = "temp-${UUID.randomUUID()}", chatType = ChatType.FARM_SURVEY
                    )) }
                )
            }
            composable<MainDest.Pesticides> {
                setTopBarTitle(context.getString(R.string.pesticides))
                PesticidesScreen { recommendationId ->
                    appNavController.navigate(PesticideDest.PesticideRecommendation(recommendationId))
                }
            }
            composable<MainDest.ChatList> {
                setTopBarTitle(context.getString(R.string.chats))
                ChatListScreen { chatId, chatType, dataId ->
                    appNavController.navigate(ChatDest.Chat(chatId, chatType, dataId))
                }
            }
            composable<MainDest.AlertList> {
                setTopBarTitle(context.getString(R.string.alerts))
                AlertListScreen { alertId ->
                    appNavController.navigate(AlertDest.Alert(alertId))
                }
            }
            composable<MainDest.Settings> {
                setTopBarTitle(context.getString(R.string.settings))
                SettingsScreen(
                    onNavigateToLanguage = {
                        appNavController.navigate(AppDest.LanguageSelection(fromSettings = true))
                    },
                    onLogout = {
                        try {
                            // Navigate to the root data directory
                            val root = context.filesDir.parentFile
                            val files = root?.listFiles()

                            files?.forEach { file ->
                                // Delete everything except the 'lib' folder (native libraries)
                                if (file.name != "lib") {
                                    file.deleteRecursively()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        appNavController.navigate(AppDest.Auth) {
                            popUpTo(AppDest.Main) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
