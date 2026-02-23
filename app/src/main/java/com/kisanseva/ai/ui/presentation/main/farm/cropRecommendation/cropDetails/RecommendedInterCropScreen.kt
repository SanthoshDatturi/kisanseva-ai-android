package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val listState = rememberLazyListState()

    var isBottomBarVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -2f) { // Scrolling down (content up) -> Hide
                    isBottomBarVisible = false
                } else if (delta > 2f) { // Scrolling up (content down) -> Show
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is InterCropEvent.NavigateToInterCroppingDetails -> onNavigateToInterCroppingDetails(event.interCropId)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Intercropping Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else {
                uiState.interCrop?.let { interCrop ->
                    InterCropContent(interCrop = interCrop, listState = listState)
                }
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 0.dp)
                        .navigationBarsPadding()
                ) {
                    CropSelectionButton {
                        viewModel.selectCropForCultivation()
                    }
                }
            }
            if (uiState.isSelectingCrop) {
                SelectingCropLoadingIndicator()
            }
        }
    }
}

@Composable
fun InterCropContent(interCrop: InterCropRecommendation, listState: androidx.compose.foundation.lazy.LazyListState) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { InterCropHeader(interCrop = interCrop) }
        item { InterCropArrangementCard(interCrop.arrangement, interCrop.specificArrangement) }
        if (interCrop.benefits.isNotEmpty()) item { BenefitsCard(benefits = interCrop.benefits) }
        
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        itemsIndexed(interCrop.crops) { index, monoCrop ->
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                MonoCropHeader(monoCrop = monoCrop)
                SowingWindowCard(sowingWindow = monoCrop.sowingWindow)
                FinancialForecastingCard(financialForecasting = monoCrop.financialForecasting)
                ReasonsCard(reasons = monoCrop.reasons)
                RiskFactorsCard(riskFactors = monoCrop.riskFactors)
                if (index < interCrop.crops.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }
}
