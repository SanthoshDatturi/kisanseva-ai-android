package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Enhanced scroll physics logic
    var isScrollingUp by remember { mutableStateOf(true) }
    var previousOffset by remember { mutableStateOf(0) }
    var previousIndex by remember { mutableStateOf(0) }

    val isFabVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf true

            val lastVisibleItem = visibleItemsInfo.last()
            val totalItemsCount = layoutInfo.totalItemsCount
            
            // Reappear logic:
            // 1. If we are near the top
            // 2. If we are scrolling up (immediate response)
            
            val isAtVeryBottom = lastVisibleItem.index == totalItemsCount - 1 && 
                                (lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 10)
            
            if (isAtVeryBottom) return@derivedStateOf false
            
            val isNearTop = listState.firstVisibleItemIndex < 1
            isNearTop || isScrollingUp
        }
    }

    // Scroll listener for "1cm" immediate feedback
    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset
        
        isScrollingUp = if (currentIndex < previousIndex) {
            true
        } else if (currentIndex > previousIndex) {
            false
        } else {
            // Detect even small upward movements (1cm feel)
            currentOffset < previousOffset - 3
        }
        
        previousIndex = currentIndex
        previousOffset = currentOffset
    }

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
                title = { Text("Intercropping Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible && uiState.interCrop != null,
                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f) + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Button(
                    onClick = { viewModel.selectCropForCultivation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(60.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(30.dp), // Pill shape for modern look
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(Icons.Filled.Agriculture, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Start Cultivating This Plan", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.sp
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
