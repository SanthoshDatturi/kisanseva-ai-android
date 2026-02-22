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
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedMonoCropDetailsScreen(
    viewModel: RecommendedMonoCropDetailsViewModel = hiltViewModel(),
    onNavigateToCultivatingCrop: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Physics-based scroll logic
    var isScrollingUp by remember { mutableStateOf(false) }
    var previousOffset by remember { mutableStateOf(0) }
    var previousIndex by remember { mutableStateOf(0) }

    val isFabVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf true

            val lastVisibleItem = visibleItemsInfo.last()
            val totalItems = layoutInfo.totalItemsCount
            
            // Hide ONLY when at the absolute end
            val isAtVeryBottom = lastVisibleItem.index == totalItems - 1 && 
                                (lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset)
            
            if (isAtVeryBottom) return@derivedStateOf false
            
            // Show if near top OR user is scrolling up with sensitivity (physics)
            val isNearTop = listState.firstVisibleItemIndex < 1
            isNearTop || isScrollingUp
        }
    }

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset
        
        isScrollingUp = if (currentIndex < previousIndex) {
            true
        } else if (currentIndex > previousIndex) {
            false
        } else {
            // Sensitivity threshold for "1cm scroll" feel
            currentOffset < previousOffset - 8
        }
        
        previousIndex = currentIndex
        previousOffset = currentOffset
    }

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
                title = { Text("Crop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible && uiState.monoCrop != null,
                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f) + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Button(
                    onClick = { viewModel.selectCropForCultivation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp) // Taller, more prominent button
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(Icons.Filled.Agriculture, null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Start Cultivating This Crop", 
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
            } else if (uiState.monoCrop != null) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { MonoCropHeader(monoCrop = uiState.monoCrop!!) }
                    item { SowingWindowCard(sowingWindow = uiState.monoCrop!!.sowingWindow) }
                    item { FinancialForecastingCard(financialForecasting = uiState.monoCrop!!.financialForecasting) }
                    item { ReasonsCard(reasons = uiState.monoCrop!!.reasons) }
                    item { RiskFactorsCard(riskFactors = uiState.monoCrop!!.riskFactors) }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
            if (uiState.isSelectingCrop) {
                SelectingCropLoadingIndicator()
            }
        }
    }
}
