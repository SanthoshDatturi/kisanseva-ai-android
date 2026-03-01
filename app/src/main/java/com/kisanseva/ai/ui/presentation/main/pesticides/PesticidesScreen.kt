package com.kisanseva.ai.ui.presentation.main.pesticides

import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.ui.components.AudioPlayBar
import com.kisanseva.ai.ui.components.AudioRecordingBar
import com.kisanseva.ai.ui.components.PesticideActionItem
import com.kisanseva.ai.ui.components.rememberGalleryLauncher
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticidesScreen(
    viewModel: PesticideViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToPesticideRecommendation: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberGalleryLauncher(
        currentImageCount = uiState.imageParts.size,
        onImageSelected = viewModel::addImage
    )

    LaunchedEffect(true) {
        viewModel.errorChannel.collectLatest { error ->
            Toast.makeText(context, error.asString(context), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PesticideEvent.RecommendationReceived -> {
                    onNavigateToPesticideRecommendation(event.id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pesticides), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Upload and Action Area
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    ActionArea(
                        uiState = uiState,
                        viewModel = viewModel,
                        onUploadClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }

            // Previous Pesticides
            if (uiState.previousPesticides.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.previous_pesticides),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                items(uiState.previousPesticides) { (recId, pesticide) ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PesticideActionItem(
                            pesticide = pesticide,
                            onClick = { onNavigateToPesticideRecommendation(recId) }
                        )
                    }
                }
            } else if (uiState.isRefreshing) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ActionArea(
    uiState: PesticideUiState,
    viewModel: PesticideViewModel,
    onUploadClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        val hasMedia = uiState.imageParts.isNotEmpty() || uiState.audioPart != null

        // Big Upload Card - Only show when no media is present
        AnimatedVisibility(
            visible = !hasMedia,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(onClick = onUploadClick),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(2.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(0.5f), MaterialTheme.colorScheme.secondary.copy(0.5f))))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.padding(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.upload_crop_issues),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.upload_crop_issues_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Media Previews & Input Area
        AnimatedVisibility(
            visible = hasMedia,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Media Previews Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(uiState.imageParts) { part ->
                        MediaPreviewItem(
                            part = part,
                            onRemove = { viewModel.removeImage(part) }
                        )
                    }

                    if (uiState.imageParts.size < 5) {
                        item {
                            AddMediaButton(onClick = onUploadClick)
                        }
                    }
                }

                // Audio Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.audioPart != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AudioPlayBar(
                                audioSource = uiState.audioPart.fileData?.localUri ?: "",
                                audioPlayer = viewModel.audioPlayer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.onRecordingCancel() }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_recording), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        AudioRecordingBar(
                            isRecording = uiState.isRecording,
                            audioFile = uiState.audioFile,
                            onStartRecording = viewModel::onStartRecording,
                            onRecordingComplete = viewModel::onRecordingComplete,
                            onRecordingCancel = viewModel::onRecordingCancel,
                            onIsRecordingChange = viewModel::onIsRecordingChange,
                            onAudioFileChange = viewModel::onAudioFileChange
                        )
                    }
                }

                // Description Input
                AnimatedVisibility(
                    visible = uiState.showDescriptionInput,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        TextField(
                            value = viewModel.description,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text(stringResource(R.string.add_description_placeholder)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = { viewModel.requestRecommendation() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isRequesting && !uiState.isUploading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isRequesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    stringResource(R.string.request_recommendation),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPreviewItem(part: Part, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(100.dp)) {
        AsyncImage(
            model = part.fileData?.localUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(MaterialTheme.colorScheme.surface.copy(0.7f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun AddMediaButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
