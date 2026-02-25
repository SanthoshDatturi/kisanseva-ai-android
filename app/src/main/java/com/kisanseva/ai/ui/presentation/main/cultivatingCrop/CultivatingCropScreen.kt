package com.kisanseva.ai.ui.presentation.main.cultivatingCrop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.ui.components.ActionItem
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
                            contentDescription = "Back"
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
        
        // Dynamic State Badge at Right Top
        val stateInfo = getCropStateInfo(crop.cropState)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            color = stateInfo.color,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = stateInfo.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stateInfo.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
        
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
                            text = "Intercropped",
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
            text = "Description",
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
            text = "Management Tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (intercroppingId != null) {
            ActionItem(
                title = "Intercropping Guide",
                subtitle = "View mixed crop arrangement",
                icon = Icons.Rounded.Grid4x4,
                color = Color(0xFF673AB7), // Deep Purple for unique tool
                onClick = { onNavigateToIntercroppingDetails(intercroppingId) }
            )
        }

        ActionItem(
            title = "Cultivation Calendar",
            subtitle = "Track growth and tasks",
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFF2196F3),
            onClick = { onNavigateToCalendar(cropId) }
        )

        ActionItem(
            title = "Investment Analysis",
            subtitle = "Costs and profit estimates",
            icon = Icons.Default.Payments,
            color = Color(0xFF4CAF50),
            onClick = { onNavigateToInvestment(cropId) }
        )

        ActionItem(
            title = "Soil Health Advice",
            subtitle = "Maintain soil fertility",
            icon = Icons.Default.Science,
            color = Color(0xFFFF9800),
            onClick = { onNavigateToSoilHealth(cropId) }
        )
    }
}

data class CropStateInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

fun getCropStateInfo(state: CropState): CropStateInfo {
    return when (state) {
        CropState.SELECTED -> CropStateInfo("Selected", Icons.Default.CheckCircle, Color(0xFF9C27B0))
        CropState.PLANTED -> CropStateInfo("Planted", Icons.Default.Agriculture, Color(0xFF4CAF50))
        CropState.GROWING -> CropStateInfo("Growing", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF2196F3))
        CropState.HARVESTED -> CropStateInfo("Harvested", Icons.Default.Inventory, Color(0xFFFF9800))
        CropState.COMPLETE -> CropStateInfo("Complete", Icons.Default.Verified, Color(0xFF795548))
    }
}
