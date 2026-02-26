package com.kisanseva.ai.ui.presentation.main.farm.farmProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kisanseva.ai.domain.model.*
import com.kisanseva.ai.ui.components.ActionItem
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

    LaunchedEffect(uiState.selectedTabIndex) {
        if (pagerState.currentPage != uiState.selectedTabIndex) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

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
                        Text(text = farm.name, fontWeight = FontWeight.ExtraBold)
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
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Farm Details", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("My Crops", fontWeight = FontWeight.Bold) }
                    )
                }

                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    when (page) {
                        0 -> FarmTabContent(uiState, onNavigateToCropRecommendation)
                        1 -> CropsTabContent(uiState, onNavigateToCultivatingCrop)
                    }
                }
            }
        }
    }
}

@Composable
fun FarmTabContent(uiState: FarmProfileUiState, onNavigateToCropRecommendation: (String) -> Unit) {
    uiState.farm?.let { farm ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { WeatherWidget(weather = uiState.weather, isLoading = uiState.isWeatherLoading) }
            
            item {
                ActionItem(
                    title = "Crop Recommendation",
                    subtitle = "Discover the best crops for your farm",
                    icon = Icons.Outlined.Psychology,
                    color = MaterialTheme.colorScheme.primary
                ) { onNavigateToCropRecommendation(farm.id) }
            }

            item {
                SectionHeader("Land Information", Icons.Outlined.Terrain)
                InfoGroupCard {
                    InfoRowItem(Icons.Outlined.SquareFoot, "Total Area", "${farm.totalAreaAcres} acres")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRowItem(Icons.Outlined.Grass, "Cultivated Area", "${farm.cultivatedAreaAcres} acres")
                }
            }

            item {
                SectionHeader("Location", Icons.Default.PinDrop)
                InfoGroupCard {
                    InfoRowItem(Icons.Outlined.Home, "Village", farm.location.village)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRowItem(Icons.Outlined.Map, "Mandal", farm.location.mandal)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRowItem(Icons.Outlined.Public, "District", farm.location.district)
                }
            }

            item {
                SectionHeader("Resources", Icons.Outlined.WaterDrop)
                InfoGroupCard {
                    InfoRowItem(Icons.Outlined.Opacity, "Water Source", farm.waterSource.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRowItem(Icons.Outlined.Science, "Soil Type", farm.soilType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
                }
            }

            if (!farm.crops.isNullOrEmpty()) {
                item { SectionHeader("Crop History", Icons.Outlined.History) }
                items(farm.crops) { CropHistoryCard(it) }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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
                        Box(modifier = Modifier.height(32.dp).width(1.dp).background(Color.White.copy(alpha = 0.2f)))
                        WeatherDetailItem(
                            icon = Icons.Outlined.Air,
                            label = "Wind",
                            value = "${weather.wind.speed} m/s"
                        )
                        Box(modifier = Modifier.height(32.dp).width(1.dp).background(Color.White.copy(alpha = 0.2f)))
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
fun WeatherDetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun CropsTabContent(uiState: FarmProfileUiState, onNavigateToCultivatingCrop: (String) -> Unit) {
    if (uiState.isCropsLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (uiState.cultivatingCrops.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No cultivating crops found.") }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.cultivatingCrops) { crop ->
                CultivatingCropCard(crop = crop, onClick = { onNavigateToCultivatingCrop(crop.id) })
            }
        }
    }
}

@Composable
fun CropHistoryCard(crop: PreviousCrops) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Grass, null, tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = crop.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${crop.season} • ${crop.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (crop.yieldPerAcre != null) {
                Text(text = crop.yieldPerAcre!!, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
