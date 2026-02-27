package com.kisanseva.ai.ui.presentation.main.soilHealth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.ImmediateAction
import com.kisanseva.ai.domain.model.SoilHealthRecommendations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilHealthScreen(
    viewModel: SoilHealthViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.soil_health_advice), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                uiState.isRefreshing && uiState.recommendations.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.recommendations.isEmpty() -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.recommendations.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_soil_recommendations),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(uiState.recommendations) { recommendation ->
                            SoilRecommendationItem(recommendation)
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SoilRecommendationItem(recommendation: SoilHealthRecommendations) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Description Card - Immersive with Icon
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Immediate Actions Section
        Column {
            SectionHeader(icon = Icons.Default.FlashOn, title = stringResource(R.string.immediate_actions), color = Color(0xFFE91E63))
            Spacer(modifier = Modifier.height(12.dp))
            recommendation.immediateActions.forEach { action ->
                ImmediateActionCard(action)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Long Term Improvements Section
        Column {
            SectionHeader(icon = Icons.Default.Autorenew, title = stringResource(R.string.long_term_improvements), color = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    recommendation.longTermImprovements.forEach { improvement ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = improvement,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ImmediateActionCard(action: ImmediateAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = action.parameter,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = Color(0xFFE91E63).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = action.cost,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE91E63)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = action.recommendation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = action.product,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun Surface(
    color: Color,
    shape: Shape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color)
    ) {
        content()
    }
}
