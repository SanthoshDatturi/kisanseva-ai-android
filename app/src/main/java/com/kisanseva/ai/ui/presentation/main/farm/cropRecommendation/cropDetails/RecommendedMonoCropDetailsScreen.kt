package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedMonoCropDetailsScreen(
    viewModel: RecommendedMonoCropDetailsViewModel = hiltViewModel(),
    onNavigateToCultivatingCrop: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is Event.NavigateToCultivatingCrop -> onNavigateToCultivatingCrop(event.cropId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommended Crop Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.selectCropForCultivation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null
                    )
                },
                text = { Text(text = "Select for Cultivation") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.monoCrop != null) {
                val monoCrop = uiState.monoCrop!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MonoCropHeader(monoCrop = monoCrop)
                    }
                    item {
                        SowingWindowCard(sowingWindow = monoCrop.sowingWindow)
                    }
                    item {
                        FinancialForecastingCard(financialForecasting = monoCrop.financialForecasting)
                    }
                    item {
                        ReasonsCard(reasons = monoCrop.reasons)
                    }
                    item {
                        RiskFactorsCard(riskFactors = monoCrop.riskFactors)
                    }
                }
            }
            if (uiState.isSelectingCrop) {
                SelectingCropLoadingIndicator()
            }
        }
    }
}
