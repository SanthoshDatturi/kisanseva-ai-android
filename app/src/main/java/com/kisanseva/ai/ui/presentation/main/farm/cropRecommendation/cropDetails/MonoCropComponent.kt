package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kisanseva.ai.domain.model.FinancialForecasting
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.model.RiskFactor
import com.kisanseva.ai.domain.model.RiskImpact
import com.kisanseva.ai.domain.model.SowingWindow

@Composable
fun MonoCropHeader(monoCrop: MonoCrop) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(monoCrop.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${(monoCrop.suitabilityScore * 100).toInt()}% Match", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(monoCrop.cropName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(monoCrop.variety, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(24.dp))
        // Stats Row - Using IntrinsicSize.Min to ensure equal height and handle wrapping
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), 
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoBlock(Icons.Rounded.Timer, "${monoCrop.growingPeriodDays}", "Days", Modifier.weight(1f).fillMaxHeight())
            InfoBlock(Icons.Rounded.Agriculture, monoCrop.expectedYieldPerAcre, "Yield/Acre", Modifier.weight(1.2f).fillMaxHeight())
            InfoBlock(Icons.Rounded.Verified, "${(monoCrop.confidence * 100).toInt()}%", "Confidence", Modifier.weight(1f).fillMaxHeight())
        }

        Spacer(Modifier.height(24.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(monoCrop.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 26.sp)
    }
}

@Composable
fun InfoBlock(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier, 
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp), 
        shape = RoundedCornerShape(20.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                value, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold, 
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.outline, 
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SowingWindowCard(sowingWindow: SowingWindow) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.CalendarMonth, "Sowing Schedule")
            Spacer(Modifier.height(24.dp))
            
            TimelineItem("Safe Start", sowingWindow.startDate, MaterialTheme.colorScheme.secondary, false)
            TimelineConnector(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
            TimelineItem("Best Date (Optimal)", sowingWindow.optimalDate, MaterialTheme.colorScheme.tertiary, true)
            TimelineConnector(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            TimelineItem("Last Date", sowingWindow.endDate, MaterialTheme.colorScheme.error.copy(alpha = 0.7f), false)
        }
    }
}

@Composable
fun TimelineConnector(startColor: Color, endColor: Color) {
    Box(modifier = Modifier.padding(start = 11.dp).width(2.dp).height(24.dp).background(Brush.verticalGradient(listOf(startColor, endColor))))
}

@Composable
fun TimelineItem(label: String, date: String, color: Color, isOptimal: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).padding(4.dp)) {
            if (isOptimal) Icon(Icons.Rounded.Star, null, tint = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(date, style = if (isOptimal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, fontWeight = if (isOptimal) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun FinancialForecastingCard(financialForecasting: FinancialForecasting) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.Payments, "Money Matters")
            Spacer(Modifier.height(24.dp))
            
            FinancialRow(Icons.Rounded.AccountBalanceWallet, "Your Investment", financialForecasting.totalEstimatedInvestment, MaterialTheme.colorScheme.primary)
            FinancialRow(Icons.Rounded.Storefront, "Market Price Now", financialForecasting.marketPriceCurrent, MaterialTheme.colorScheme.secondary)
            
            val isUp = financialForecasting.priceTrend.contains("up", ignoreCase = true)
            FinancialRow(if (isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown, "Price Trend", financialForecasting.priceTrend, if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828))
            
            Spacer(Modifier.height(24.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), 
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Potential Revenue", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(financialForecasting.totalRevenueEstimate, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun FinancialRow(icon: ImageVector, label: String, value: String, tint: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Surface(color = tint.copy(alpha = 0.12f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReasonsCard(reasons: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.AutoAwesome, "Why this is recommended")
            Spacer(Modifier.height(24.dp))
            
            reasons.forEach { reason ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), 
                    verticalAlignment = Alignment.CenterVertically // Centered icon and text
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF43A047).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check, 
                            null, 
                            tint = Color(0xFF43A047), 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        reason, 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RiskFactorsCard(riskFactors: List<RiskFactor>) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeader(Icons.Rounded.Warning, "Risks to Watch For", MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(24.dp))
            riskFactors.forEachIndexed { index, risk ->
                RiskItem(risk)
                if (index < riskFactors.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
fun RiskItem(risk: RiskFactor) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(risk.risk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            val (color, label) = when (risk.impact) {
                RiskImpact.LOW -> Color(0xFF43A047) to "Low Risk"
                RiskImpact.MEDIUM -> Color(0xFFFB8C00) to "Medium"
                RiskImpact.HIGH -> Color(0xFFE53935) to "High Danger"
            }
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
                Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface, 
            shape = RoundedCornerShape(16.dp), 
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How to manage:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(risk.mitigation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, iconColor: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(color = iconColor.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
    }
}
