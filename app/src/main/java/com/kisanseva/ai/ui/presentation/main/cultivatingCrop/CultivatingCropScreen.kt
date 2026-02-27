package com.kisanseva.ai.ui.presentation.main.cultivatingCrop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.ui.components.ActionItem
import com.kisanseva.ai.ui.components.CropStateBadge
import com.kisanseva.ai.util.UrlUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivatingCropScreen(
    viewModel: CultivatingCropViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToCalendar: (String) -> Unit,
    onNavigateToInvestment: (String) -> Unit,
    onNavigateToSoilHealth: (String) -> Unit,
    onNavigateToIntercroppingDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.crop?.let {
                        Text(text = it.name, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            uiState.crop?.let { crop ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        CropImageHeader(crop)
                    }

                    item {
                        CropMainInfo(crop = crop)
                    }

                    item {
                        ManagementActions(
                            cropId = crop.id,
                            intercroppingId = crop.intercroppingId,
                            onNavigateToCalendar = onNavigateToCalendar,
                            onNavigateToInvestment = onNavigateToInvestment,
                            onNavigateToSoilHealth = onNavigateToSoilHealth,
                            onNavigateToIntercroppingDetails = onNavigateToIntercroppingDetails
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CropImageHeader(crop: CultivatingCrop) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = crop.imageUrl.let { UrlUtils.getFullUrlFromRef(it) },
            contentDescription = crop.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        
        // Unified Crop State Badge
        CropStateBadge(
            state = crop.cropState,
            modifier = Modifier.align(Alignment.TopEnd),
            padding = 16.dp
        )
        
        // Bottom Rounded Corner Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        )
    }
}

@Composable
fun CropMainInfo(crop: CultivatingCrop) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = crop.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (crop.intercroppingId != null) {
                Surface(
                    color = Color(0xFF673AB7).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF673AB7).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Grid4x4,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF673AB7)
                        )
                        Text(
                            text = stringResource(R.string.intercropped),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Grass,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = crop.variety,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.description),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = crop.description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ManagementActions(
    cropId: String,
    intercroppingId: String?,
    onNavigateToCalendar: (String) -> Unit,
    onNavigateToInvestment: (String) -> Unit,
    onNavigateToSoilHealth: (String) -> Unit,
    onNavigateToIntercroppingDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.management_tools),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (intercroppingId != null) {
            ActionItem(
                title = stringResource(R.string.intercropping_guide),
                subtitle = stringResource(R.string.intercropping_guide_desc),
                icon = Icons.Rounded.Grid4x4,
                color = Color(0xFF673AB7), // Deep Purple for unique tool
                onClick = { onNavigateToIntercroppingDetails(intercroppingId) }
            )
        }

        ActionItem(
            title = stringResource(R.string.cultivation_calendar),
            subtitle = stringResource(R.string.cultivation_calendar_desc),
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFF2196F3),
            onClick = { onNavigateToCalendar(cropId) }
        )

        ActionItem(
            title = stringResource(R.string.investment_analysis),
            subtitle = stringResource(R.string.investment_analysis_desc),
            icon = Icons.Default.Payments,
            color = Color(0xFF4CAF50),
            onClick = { onNavigateToInvestment(cropId) }
        )

        ActionItem(
            title = stringResource(R.string.soil_health_advice),
            subtitle = stringResource(R.string.soil_health_advice_desc),
            icon = Icons.Default.Science,
            color = Color(0xFFFF9800),
            onClick = { onNavigateToSoilHealth(cropId) }
        )
    }
}
