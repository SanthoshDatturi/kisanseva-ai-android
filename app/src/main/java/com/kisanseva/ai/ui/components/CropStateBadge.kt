package com.kisanseva.ai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails.fadingShadow

data class CropStateInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

fun CropState.toInfo(): CropStateInfo {
    return when (this) {
        CropState.SELECTED -> CropStateInfo("Selected", Icons.Default.CheckCircle, Color(0xFF5A7E7E))
        CropState.PLANTED -> CropStateInfo("Planted", Icons.Default.Agriculture, Color(0xFF795548))
        CropState.GROWING -> CropStateInfo("Growing", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF4CAF50))
        CropState.HARVESTED -> CropStateInfo("Harvested", Icons.Default.Inventory, Color(0xFFFF9800))
        CropState.COMPLETE -> CropStateInfo("Complete", Icons.Default.Verified, Color(0xFF009688))
    }
}

@Composable
fun CropStateBadge(
    state: CropState,
    modifier: Modifier = Modifier,
    padding: Dp = 12.dp
) {
    val stateInfo = state.toInfo()
    
    Surface(
        modifier = modifier
            .padding(padding)
            .fadingShadow(),
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
