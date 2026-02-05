package com.kisanseva.ai.ui.presentation.main.farm.farmProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.model.IrrigationSystem
import com.kisanseva.ai.domain.model.Location
import com.kisanseva.ai.domain.model.PreviousCrops
import com.kisanseva.ai.domain.model.SoilTestProperties
import com.kisanseva.ai.domain.model.SoilType
import com.kisanseva.ai.domain.model.WaterSource
import com.kisanseva.ai.ui.components.CultivatingCropCard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmProfileScreen(
    viewModel: FarmProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToCropRecommendation: (String) -> Unit,
    onNavigateToCultivatingCrop: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // Sync ViewModel tab selection to Pager
    LaunchedEffect(uiState.selectedTabIndex) {
        if (pagerState.currentPage != uiState.selectedTabIndex) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    // Sync Pager swipes to ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onTabSelected(page)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.farm?.let { farm ->
                        Text(text = farm.name, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (uiState.farm == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(paddingValues))
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = { Text("Farm") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = { Text("Crops") }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> FarmTabContent(
                            uiState = uiState,
                            onNavigateToCropRecommendation = onNavigateToCropRecommendation
                        )
                        1 -> CropsTabContent(
                            uiState = uiState,
                            onNavigateToCultivatingCrop = onNavigateToCultivatingCrop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmTabContent(
    uiState: FarmProfileUiState,
    onNavigateToCropRecommendation: (String) -> Unit
) {
    uiState.farm?.let { farm ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeatherWidget(
                    weather = uiState.weather,
                    isLoading = uiState.isWeatherLoading
                )
            }

            item {
                RecommendationBanner(
                    onClick = {
                        onNavigateToCropRecommendation(farm.id)
                    }
                )
            }

            item { BasicInfoCard(farm) }
            item { LocationCard(farm.location) }
            item { WaterCard(farm.waterSource, farm.irrigationSystem) }
            item { SoilCard(farm.soilType, farm.soilTestProperties) }

            if (!farm.crops.isNullOrEmpty()) {
                item {
                    Text(
                        text = "Crop History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(farm.crops) { crop ->
                    CropHistoryCard(crop = crop)
                }
            }
        }
    }
}

@Composable
fun CropsTabContent(
    uiState: FarmProfileUiState,
    onNavigateToCultivatingCrop: (String) -> Unit
) {
    if (uiState.isCropsLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.cultivatingCrops.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No cultivating crops found.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.cultivatingCrops) { crop ->
                CultivatingCropCard(
                    crop = crop,
                    onClick = { onNavigateToCultivatingCrop(crop.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendationBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Crop Recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Discover the best crops for your farm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WeatherWidget(
    weather: CurrentWeatherResponse?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                }
            } else if (weather != null) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Weather",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = weather.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        AsyncImage(
                            model = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@2x.png",
                            contentDescription = weather.weather.firstOrNull()?.description,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${weather.main.temp.roundToInt()}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = weather.weather.firstOrNull()?.main ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Feels like ${weather.main.feelsLike.roundToInt()}°",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailItem(
                            icon = Icons.Outlined.WaterDrop,
                            label = "Humidity",
                            value = "${weather.main.humidity}%"
                        )
                        VerticalDivider()
                        WeatherDetailItem(
                            icon = Icons.Outlined.Air,
                            label = "Wind",
                            value = "${weather.wind.speed} m/s"
                        )
                        VerticalDivider()
                        WeatherDetailItem(
                            icon = Icons.Outlined.DeviceThermostat,
                            label = "Pressure",
                            value = "${weather.main.pressure} hPa"
                        )
                    }
                }
            } else {
                Text(
                    text = "Weather data unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.2f))
    )
}

@Composable
fun BasicInfoCard(farm: FarmProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Total Area", value = "${farm.totalAreaAcres} acres")
            InfoRow(label = "Cultivated Area", value = "${farm.cultivatedAreaAcres} acres")
        }
    }
}

@Composable
fun LocationCard(location: Location) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = "Location",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Village", value = location.village)
            InfoRow(label = "Mandal", value = location.mandal)
            InfoRow(label = "District", value = location.district)
            InfoRow(label = "State", value = location.state)
            InfoRow(label = "ZIP Code", value = location.zipCode)
        }
    }
}

@Composable
fun WaterCard(waterSource: WaterSource, irrigationSystem: IrrigationSystem?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = "Water",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Water & Irrigation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Water Source", value = waterSource.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
            irrigationSystem?.let { irrigationSystem ->
                InfoRow(label = "Irrigation System", value = irrigationSystem.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun SoilCard(soilType: SoilType, soilTestProperties: SoilTestProperties?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.SquareFoot,
                    contentDescription = "Soil",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Soil Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Soil Type", value = soilType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
            soilTestProperties?.let {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Soil Test Properties",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "pH Level", value = it.phLevel.toString())
                InfoRow(label = "Organic Carbon", value = "${it.organicCarbonPercent}%")
                InfoRow(label = "Nitrogen", value = "${it.nitrogenKgPerAcre} kg/acre")
                InfoRow(label = "Phosphorus", value = "${it.phosphorusKgPerAcre} kg/acre")
                InfoRow(label = "Potassium", value = "${it.potassiumKgPerAcre} kg/acre")
            }
        }
    }
}

@Composable
fun CropHistoryCard(crop: PreviousCrops) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Grass,
                        contentDescription = "Crop",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = crop.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = crop.year.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Season", value = crop.season)
            crop.yieldPerAcre?.let { InfoRow(label = "Yield per Acre", value = it) }
            crop.fertilizersUsed?.let {
                if(it.isNotEmpty()) InfoRow(label = "Fertilizers Used", value = it.joinToString())
            }
            crop.pesticidesUsed?.let {
                if(it.isNotEmpty()) InfoRow(label = "Pesticides Used", value = it.joinToString())
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
