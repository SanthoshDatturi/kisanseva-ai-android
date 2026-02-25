package com.kisanseva.ai.ui.presentation.main.pesticides.pesticideRecommendation

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kisanseva.ai.domain.model.PesticideInfo
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.util.UrlUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideRecommendationScreen(
    viewModel: PesticideRecommendationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommendations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::loadRecommendation) { Text("Retry") }
                }
            } else {
                uiState.recommendation?.let { recommendation ->
                    val allPesticides = recommendation.recommendations.sortedBy { it.rank }
                    
                    val recommendationsToShow = remember(allPesticides) {
                        val applied = allPesticides.filter { it.stage == PesticideStage.APPLIED }
                        if (applied.isNotEmpty()) {
                            applied
                        } else {
                            val selected = allPesticides.filter { it.stage == PesticideStage.SELECTED }
                            if (selected.isNotEmpty()) {
                                selected
                            } else {
                                allPesticides
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Disease Summary
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Detected Problem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(recommendation.diseaseDetails, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Text("Recommended Pesticides", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        recommendationsToShow.forEach { pesticide ->
                            PesticideCard(
                                pesticide = pesticide,
                                onUpdateStage = { stage, date ->
                                    viewModel.updateStage(pesticide.id, stage, date)
                                },
                                getTodayDate = viewModel::getTodayDate
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("General Advice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(recommendation.generalAdvice, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            if (uiState.isUpdating) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun PesticideCard(
    pesticide: PesticideInfo,
    onUpdateStage: (PesticideStage, String?) -> Unit,
    getTodayDate: () -> String
) {
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pesticide.pesticideName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        pesticide.pesticideType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (pesticide.stage) {
                        PesticideStage.RECOMMENDED -> MaterialTheme.colorScheme.surfaceVariant
                        PesticideStage.SELECTED -> MaterialTheme.colorScheme.primaryContainer
                        PesticideStage.APPLIED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        pesticide.stage.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (pesticide.stage) {
                            PesticideStage.APPLIED -> Color(0xFF2E7D32)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(Icons.Default.Scale, "Dosage", pesticide.dosage, Modifier.weight(1f))
                InfoItem(Icons.Default.Hardware, "Method", pesticide.applicationMethod, Modifier.weight(1f))
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Explanation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(pesticide.explanation, style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Precautions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    pesticide.precautions.forEach { 
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold)
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (pesticide.appliedDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val formattedDate = remember(pesticide.appliedDate) {
                            try {
                                val dateStr = pesticide.appliedDate
                                if (dateStr.contains("T")) {
                                    val dt = LocalDateTime.parse(dateStr)
                                    dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                                } else {
                                    dateStr
                                }
                            } catch (e: Exception) {
                                pesticide.appliedDate
                            }
                        }
                        Text("Applied on: $formattedDate", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Show Less" else "Show More")
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (pesticide.stage) {
                        PesticideStage.RECOMMENDED -> {
                            Button(onClick = { onUpdateStage(PesticideStage.SELECTED, null) }) {
                                Text("Select This")
                            }
                        }
                        PesticideStage.SELECTED -> {
                            TextButton(
                                onClick = { onUpdateStage(PesticideStage.RECOMMENDED, null) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Unselect")
                            }
                            Button(
                                onClick = { showDatePicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Applied")
                            }
                        }
                        PesticideStage.APPLIED -> {
                            // No further actions usually, as per requirement: "just show applied Info"
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("When did you apply this?") },
            text = { Text("Select the date of application for accurate tracking.") },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateStage(PesticideStage.APPLIED, getTodayDate())
                    showDatePicker = false
                }) {
                    Text("Today")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            onUpdateStage(PesticideStage.APPLIED, selectedDate)
                            showDatePicker = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text("Select Date")
                }
            }
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
        }
    }
}
