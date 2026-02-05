package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kisanseva.ai.domain.model.InterCropRecommendation
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedInterCropScreen(
    viewModel: RecommendedInterCropViewModel = hiltViewModel(),
    onNavigateToInterCroppingDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is InterCropEvent.NavigateToInterCroppingDetails -> onNavigateToInterCroppingDetails(event.interCropId)
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
            } else {
                uiState.interCrop?.let { interCrop ->
                    InterCropDetails(interCrop = interCrop)
                }
            }
            if (uiState.isSelectingCrop) {
                SelectingCropLoadingIndicator()
            }
        }
    }
}

@Composable
fun InterCropDetails(interCrop: InterCropRecommendation) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Intercrop Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Type: ${interCrop.intercropType}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Arrangement: ${interCrop.arrangement}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = interCrop.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        items(interCrop.crops) { monoCrop ->
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "${monoCrop.cropName} Crop Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                MonoCropHeader(monoCrop = monoCrop)
                Spacer(modifier = Modifier.height(16.dp))
                SowingWindowCard(sowingWindow = monoCrop.sowingWindow)
                Spacer(modifier = Modifier.height(16.dp))
                FinancialForecastingCard(financialForecasting = monoCrop.financialForecasting)
                Spacer(modifier = Modifier.height(16.dp))
                ReasonsCard(reasons = monoCrop.reasons)
                Spacer(modifier = Modifier.height(16.dp))
                RiskFactorsCard(riskFactors = monoCrop.riskFactors)
            }
        }
    }
}