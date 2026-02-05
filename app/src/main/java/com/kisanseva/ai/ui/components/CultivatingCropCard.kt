package com.kisanseva.ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.domain.model.CultivatingCrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivatingCropCard(
    crop: CultivatingCrop,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = crop.imageUrl,
                    contentDescription = crop.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Crop State Badge at Right Top
                val stateInfo = getCropStateInfo(crop.cropState)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = stateInfo.color.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = stateInfo.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stateInfo.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = crop.variety,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (crop.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = crop.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class StateInfo(val icon: ImageVector, val color: Color, val label: String)

private fun getCropStateInfo(state: CropState): StateInfo {
    return when (state) {
        CropState.SELECTED -> StateInfo(
            icon = Icons.Default.CheckCircleOutline,
            color = Color(0xFF607D8B),
            label = "Selected"
        )
        CropState.PLANTED -> StateInfo(
            icon = Icons.Default.Agriculture,
            color = Color(0xFF8D6E63),
            label = "Planted"
        )
        CropState.GROWING -> StateInfo(
            icon = Icons.Default.Spa,
            color = Color(0xFF4CAF50),
            label = "Growing"
        )
        CropState.HARVESTED -> StateInfo(
            icon = Icons.Default.Inventory,
            color = Color(0xFFFFA000),
            label = "Harvested"
        )
        CropState.COMPLETE -> StateInfo(
            icon = Icons.Default.TaskAlt,
            color = Color(0xFF009688),
            label = "Completed"
        )
    }
}
