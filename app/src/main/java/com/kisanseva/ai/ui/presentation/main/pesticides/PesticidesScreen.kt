package com.kisanseva.ai.ui.presentation.main.pesticides

import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import com.kisanseva.ai.ui.components.ActionButton
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // New Issue Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = stringResource(R.string.upload_crop_issues),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    PhotoSelectionSection(
                        imageParts = uiState.imageParts,
                        onUploadClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveImage = viewModel::removeImage
                    )

                    CombinedInputSection(
                        description = viewModel.description,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        uiState = uiState,
                        viewModel = viewModel
                    )

                    if (uiState.isRequesting) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            if (uiState.progressMessages.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        uiState.progressMessages.forEach { progress ->
                                            Text(
                                                text = "- $progress",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ActionButton(
                            text = stringResource(R.string.request_recommendation),
                            icon = Icons.AutoMirrored.Filled.Send,
                            onClick = { viewModel.requestRecommendation() },
                            showChevron = true,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (uiState.imageParts.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                            surfaceColor = if (uiState.imageParts.isNotEmpty()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
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
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                items(uiState.previousPesticides) { (recId, pesticide) ->
                    PesticideActionItem(
                        pesticide = pesticide,
                        onClick = { onNavigateToPesticideRecommendation(recId) }
                    )
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
fun CombinedInputSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    uiState: PesticideUiState,
    viewModel: PesticideViewModel
) {
    AnimatedContent(
        targetState = uiState.audioPart != null,
        transitionSpec = {
            fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 } using SizeTransform(clip = false)
        },
        label = "input_transition"
    ) { hasAudio ->
        if (hasAudio) {
            // Audio Playback Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioPlayBar(
                        audioSource = uiState.audioPart?.fileData?.localUri ?: "",
                        audioPlayer = viewModel.audioPlayer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.onRecordingCancel() }) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = stringResource(R.string.clear_recording), 
                            tint = MaterialTheme.colorScheme.error, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            // Unified Text + Audio Input Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.isRecording) {
                        TextField(
                            value = description,
                            onValueChange = onDescriptionChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { 
                                Text(
                                    text = stringResource(R.string.add_description_placeholder),
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    }

                    AudioRecordingBar(
                        modifier = if (uiState.isRecording) Modifier.fillMaxWidth() else Modifier,
                        isRecording = uiState.isRecording,
                        // Always pass null for audioFile when not recording to keep the bar in its starting (mic) state.
                        // This hides the waveform/delete UI after recording completes and is moved to the pill above.
                        audioFile = if (uiState.isRecording) uiState.audioFile else null,
                        onStartRecording = viewModel::onStartRecording,
                        onIsRecordingChange = viewModel::onIsRecordingChange,
                        onAudioFileChange = viewModel::onAudioFileChange,
                        onRecordingComplete = viewModel::onRecordingComplete,
                        onRecordingCancel = viewModel::onRecordingCancel
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoSelectionSection(
    imageParts: List<Part>,
    onUploadClick: () -> Unit,
    onRemoveImage: (Part) -> Unit
) {
    if (imageParts.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(onClick = onUploadClick),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.upload_photos_step),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(imageParts) { part ->
                MediaPreviewItem(
                    part = part,
                    onRemove = { onRemoveImage(part) }
                )
            }

            if (imageParts.size < 5) {
                item {
                    AddMediaButton(onClick = onUploadClick)
                }
            }
        }
    }
}

@Composable
fun MediaPreviewItem(part: Part, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(120.dp)) {
        AsyncImage(
            model = part.fileData?.localUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .background(MaterialTheme.colorScheme.surface.copy(0.9f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AddMediaButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
    }
}


