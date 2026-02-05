package com.kisanseva.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kisanseva.ai.ui.navigation.auth.AuthNavigation
import com.kisanseva.ai.ui.navigation.main.MainNavigation
import com.kisanseva.ai.ui.presentation.language.LanguageSelectionScreen
import com.kisanseva.ai.ui.presentation.main.alerts.AlertScreen
import com.kisanseva.ai.ui.presentation.main.chat.chat.ChatScreen
import com.kisanseva.ai.ui.presentation.main.cultivatingCrop.CultivatingCropScreen
import com.kisanseva.ai.ui.presentation.main.cultivatingCrop.interCroppingDetails.InterCroppingDetailsScreen
import com.kisanseva.ai.ui.presentation.main.cultivationCalendar.CultivationCalendarScreen
import com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails.RecommendedInterCropScreen
import com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails.RecommendedMonoCropDetailsScreen
import com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.recommedations.RecommendationsScreen
import com.kisanseva.ai.ui.presentation.main.farm.farmProfile.FarmProfileScreen
import com.kisanseva.ai.ui.presentation.main.investment.InvestmentBreakdownScreen
import com.kisanseva.ai.ui.presentation.main.pesticides.pesticideRecommendation.PesticideRecommendationScreen
import com.kisanseva.ai.ui.presentation.main.soilHealth.SoilHealthScreen

@Composable
fun AppNavigation(
    isLoggedIn: Boolean
) {
    val navController = rememberNavController()

    val startDestination = if (isLoggedIn) AppDest.Main else AppDest.LanguageSelection()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<AppDest.LanguageSelection> { backStackEntry ->
            val args = backStackEntry.toRoute<AppDest.LanguageSelection>()
            LanguageSelectionScreen {
                if (args.fromSettings) {
                    navController.popBackStack()
                } else {
                    navController.navigate(AppDest.Auth)
                }
            }
        }
        composable<AppDest.Auth> {
            AuthNavigation {
                navController.navigate(AppDest.Main) {
                    popUpTo<AppDest.LanguageSelection> { inclusive = true }
                }
            }
        }
        composable<AppDest.Main> { MainNavigation(appNavController = navController) }

        // Nested navigation

        composable<FarmDest.FarmProfile> {
            FarmProfileScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCropRecommendation = { farmId ->
                    navController.navigate(FarmDest.CropRecommendation(farmId))
                },
                onNavigateToCultivatingCrop = { cropId ->
                    navController.navigate(FarmDest.CultivatingCrop(cropId))
                }
            )
        }

        composable<FarmDest.CultivatingCrop> {
            CultivatingCropScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCalendar = { cropId -> 
                    navController.navigate(FarmDest.CultivationCalendar(cropId))
                },
                onNavigateToInvestment = { cropId -> 
                    navController.navigate(FarmDest.InvestmentBreakdown(cropId))
                },
                onNavigateToSoilHealth = { cropId -> 
                    navController.navigate(FarmDest.SoilHealthRecommendations(cropId))
                },
                onNavigateToIntercroppingDetails = { cropId ->
                    navController.navigate(FarmDest.InterCroppingDetails(cropId))
                }
            )
        }

        composable<FarmDest.CropRecommendation> {
            RecommendationsScreen(
                onBackClick = { navController.popBackStack() },
                onMonoCropClick = { cropId, farmId, cropRecommendationResponseId ->
                    navController.navigate(
                        FarmDest.RecommendedMonoCropDetails(
                        cropId,
                        farmId,
                        cropRecommendationResponseId
                    ))
                },
                onInterCropClick = { cropId, farmId, cropRecommendationResponseId ->
                    navController.navigate(
                        FarmDest.RecommendedInterCropDetails(
                        cropId,
                        farmId,
                        cropRecommendationResponseId
                    ))
                }
            )
        }

        composable<FarmDest.RecommendedMonoCropDetails> {
            RecommendedMonoCropDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCultivatingCrop = { cropId ->
                    navController.navigate(FarmDest.CultivatingCrop(cropId))
                }
            )
        }

        composable<FarmDest.RecommendedInterCropDetails> {
            RecommendedInterCropScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInterCroppingDetails = { cropId ->
                    navController.navigate(FarmDest.InterCroppingDetails(cropId))
                }
            )
        }

        composable<FarmDest.InterCroppingDetails> {
            InterCroppingDetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<FarmDest.CultivationCalendar> {
            CultivationCalendarScreen {
                navController.popBackStack()
            }
        }

        composable<FarmDest.InvestmentBreakdown> {
            InvestmentBreakdownScreen {
                navController.popBackStack()
            }
        }

        composable<FarmDest.SoilHealthRecommendations> {
            SoilHealthScreen{ navController.popBackStack() }
        }

        composable<PesticideDest.PesticideRecommendation> {
            PesticideRecommendationScreen {
                navController.popBackStack()
            }
        }

        composable<ChatDest.Chat> { ChatScreen(
            onNavigateToFarmProfile = { farmId ->
                navController.navigate(FarmDest.FarmProfile(farmId = farmId))
            }
        ) }

        composable<AlertDest.Alert> {
            AlertScreen(it.toRoute<AlertDest.Alert>().alertId)
        }
    }
}
