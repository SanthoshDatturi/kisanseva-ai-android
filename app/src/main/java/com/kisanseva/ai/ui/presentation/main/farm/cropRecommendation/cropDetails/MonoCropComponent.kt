package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kisanseva.ai.domain.model.FinancialForecasting
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.model.RiskFactor
import com.kisanseva.ai.domain.model.SowingWindow

@Composable
fun MonoCropHeader(monoCrop: MonoCrop) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(monoCrop.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = monoCrop.cropName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = monoCrop.cropName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = monoCrop.variety, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        Text(text = monoCrop.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SowingWindowCard(sowingWindow: SowingWindow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Sowing Window", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Start Date: ${sowingWindow.startDate}")
            Text(text = "End Date: ${sowingWindow.endDate}")
            Text(text = "Optimal Date: ${sowingWindow.optimalDate}")
        }
    }
}

@Composable
fun FinancialForecastingCard(financialForecasting: FinancialForecasting) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Financial Forecasting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total Estimated Investment: ${financialForecasting.totalEstimatedInvestment}")
            Text(text = "Current Market Price: ${financialForecasting.marketPriceCurrent}")
            Text(text = "Price Trend: ${financialForecasting.priceTrend}")
            Text(text = "Total Revenue Estimate: ${financialForecasting.totalRevenueEstimate}")
        }
    }
}

@Composable
fun ReasonsCard(reasons: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Reasons for Recommendation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            reasons.forEach { reason ->
                Text(text = "- $reason")
            }
        }
    }
}

@Composable
fun RiskFactorsCard(riskFactors: List<RiskFactor>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Risk Factors", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            riskFactors.forEach { riskFactor ->
                Text(text = "Risk: ${riskFactor.risk}")
                Text(text = "Probability: ${riskFactor.probability}")
                Text(text = "Impact: ${riskFactor.impact}")
                Text(text = "Mitigation: ${riskFactor.mitigation}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
