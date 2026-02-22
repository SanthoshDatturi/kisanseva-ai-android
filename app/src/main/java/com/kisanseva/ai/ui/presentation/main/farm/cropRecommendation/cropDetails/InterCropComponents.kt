package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.SpecificArrangement

@Composable
fun InterCropHeader(interCrop: InterCropRecommendation) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            interCrop.crops.forEach { crop ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(crop.imageUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
                shape = RoundedCornerShape(16.dp), 
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    Icon(Icons.Rounded.Hub, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) 
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(interCrop.intercropType, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("${interCrop.noOfCrops} Crops Mixed Cultivation", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(interCrop.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 28.sp)
    }
}

@Composable
fun InterCropArrangementCard(arrangement: String, specificArrangements: List<SpecificArrangement>) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.Schema, "Farm Layout Plan")
            Spacer(Modifier.height(8.dp))
            Text(arrangement, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            
            Spacer(Modifier.height(24.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                specificArrangements.forEachIndexed { index, item ->
                    ArrangementItem(index, item)
                }
            }
        }
    }
}

@Composable
fun ArrangementItem(index: Int, item: SpecificArrangement) {
    val color = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = item.cropName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Text(
                text = item.variety,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 22.dp)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Surface(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Straighten,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.arrangement,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitsCard(benefits: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(32.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.AutoAwesome, "Why this combination works", MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(20.dp))
            benefits.forEach { benefit ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.CheckCircle, 
                        null, 
                        tint = MaterialTheme.colorScheme.tertiary, 
                        modifier = Modifier.size(22.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(benefit, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
