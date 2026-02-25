package com.kisanseva.ai.ui.presentation.main.chat.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.domain.model.Role
import com.kisanseva.ai.domain.model.websocketModels.Command
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.ui.components.AudioPlayBar
import com.kisanseva.ai.ui.components.AudioRecordingBar
import com.kisanseva.ai.ui.components.rememberGalleryLauncher
import com.kisanseva.ai.util.UrlUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToFarmProfile: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current

    val galleryLauncher = rememberGalleryLauncher(
        currentImageCount = uiState.imageParts.size,
        onImageSelected = viewModel::addImage
    )

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            viewModel.bottomSheetState(true)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.chatEvent.collectLatest { event ->
            when (event) {
                is ChatEvent.HandleCommand<*> -> {
                    when (event.command) {
                        Command.OPEN_CAMERA -> {
                            Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
                            // TODO: Implement camera logic
                        }

                        Command.LOCATION -> {
                            Toast.makeText(context, "Getting location...", Toast.LENGTH_SHORT).show()
                            viewModel.setCommand(Command.LOCATION)
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                viewModel.bottomSheetState(true)
                            }
                        }

                        Command.EXIT -> {
                            val farmProfile = event.data as FarmProfile
                            Toast.makeText(context, "Farm profile received: $farmProfile", Toast.LENGTH_SHORT).show()
                            if (uiState.chatType == ChatType.FARM_SURVEY) {
                                onNavigateToFarmProfile(farmProfile.id)
                            }
                        }

                        Command.CONTINUE -> {
                            // Do nothing
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(context.getString(R.string.chats)) })
        },
        bottomBar = {
            MessageInput(
                message = viewModel.message,
                onMessageChange = viewModel::onMessageChange,
                onSendMessage = viewModel::sendMessage,
                isSendingMessage = uiState.isSendingMessage,
                imageParts = uiState.imageParts,
                onRemoveImage = viewModel::removeImage,
                onAttachmentClick = { galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                ) },
                isRecording = uiState.isRecording,
                isUploading = uiState.isUploading,
                audioFile = uiState.audioFile,
                onStartRecording = viewModel::onStartRecording,
                onIsRecordingChange = viewModel::onIsRecordingChange,
                onAudioFileChange = viewModel::onAudioFileChange,
                onRecordingComplete = viewModel::onRecordingComplete,
                onRecordingCancel = viewModel::onRecordingCancel
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.messages.reversed()) { message ->
                    MessageItem(message = message, audioPlayer = viewModel.audioPlayer)
                }
            }
        }
    }
    if (uiState.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.bottomSheetState(false)
                viewModel.setCommand(null)
            },
            sheetState = sheetState
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when (uiState.command) {
                    Command.LOCATION -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        OutlinedIconButton(
                            onClick = {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            viewModel.onMessageChange("Captured Location: (${it.latitude}, ${it.longitude})")
                                            viewModel.sendMessage()
                                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                                if (!sheetState.isVisible) {
                                                    viewModel.bottomSheetState(false)
                                                    viewModel.setCommand(null)
                                                }
                                            }
                                        } ?: run {
                                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(150.dp)
                                .scale(scale),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                modifier = Modifier.size(80.dp),
                                contentDescription = "Share Location"
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    message: Message,
    audioPlayer: AudioPlayer
) {
    val isUserMessage = message.content.role == Role.USER.name.lowercase()

    var text: String? = null
    val imageUris: MutableList<String?> = mutableListOf()
    var audioUri: String? = null

    for (part in message.content.parts ?: emptyList()) {
        if (!part.text.isNullOrBlank()) text = part.text
        val fileData = part.fileData
        if (fileData?.mimeType?.contains("image") == true) {
            val uri = fileData.localUri ?: fileData.fileUri?.let { UrlUtils.getFullUrlFromRef(it) }
            imageUris += uri
        }
        if (fileData?.mimeType?.contains("audio") == true) {
            audioUri = fileData.localUri ?: fileData.fileUri?.let { UrlUtils.getFullUrlFromRef(it) }
        }
    }

    val bubbleColor = if (isUserMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUserMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleShape = if (isUserMessage) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = alignment
    ) {
        if (imageUris.isNotEmpty()) {
            val carouselState = rememberCarouselState { imageUris.size }

            HorizontalMultiBrowseCarousel(
                state = carouselState,
                preferredItemWidth = 200.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(bottom = 4.dp)
            ) { i ->
                val uri = imageUris[i]
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .maskClip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        text?.let {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                modifier = Modifier.widthIn(max = 320.dp),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
            }
        }
        audioUri?.let {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                modifier = Modifier.widthIn(max = 320.dp),
                shadowElevation = 1.dp
            ) {
                AudioPlayBar(
                    audioSource = it,
                    audioPlayer = audioPlayer,
                    tint = contentColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentClick: () -> Unit,
    isSendingMessage: Boolean,
    imageParts: List<Part>,
    onRemoveImage: (Part) -> Unit,
    isRecording: Boolean,
    isUploading: Boolean,
    audioFile: File?,
    onStartRecording: () -> File,
    onIsRecordingChange: (Boolean) -> Unit,
    onAudioFileChange: (File?) -> Unit,
    onRecordingComplete: (File) -> Unit,
    onRecordingCancel: () -> Unit = {},
) {
    val alignment = if (imageParts.isNotEmpty()) Alignment.Bottom else Alignment.CenterVertically

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(8.dp),
        verticalAlignment = alignment,
    ) {
        OutlinedIconButton(
            onClick = onAttachmentClick,
            enabled = imageParts.size < 5,
            shape = CircleShape,
            modifier = Modifier.size(50.dp),
            border = BorderStroke(0.5.dp, Color(0xFFCCCCCC)),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.Default.Add, modifier = Modifier.size(30.dp), contentDescription = "Add attachment")
        }

        Spacer(Modifier.size(8.dp))

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (imageParts.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imageParts) { part ->
                            val uri = part.fileData?.localUri ?: part.fileData?.fileUri?.let { UrlUtils.getFullUrlFromRef(it) }
                            Box(modifier = Modifier.size(140.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (part.fileData?.fileUri == null) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = Color.White
                                    )
                                }
                                OutlinedIconButton(
                                    onClick = { onRemoveImage(part) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording && audioFile == null) {
                        TextField(
                            value = message,
                            onValueChange = onMessageChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                        )
                    }

                    AudioRecordingBar(
                        modifier = if (isRecording || audioFile != null) Modifier.fillMaxWidth() else Modifier,
                        isRecording = isRecording,
                        audioFile = audioFile,
                        onStartRecording = onStartRecording,
                        onIsRecordingChange = onIsRecordingChange,
                        onAudioFileChange = onAudioFileChange,
                        onRecordingComplete = onRecordingComplete,
                        onRecordingCancel = onRecordingCancel
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        OutlinedIconButton(
            onClick = onSendMessage,
            enabled = !(isSendingMessage || isRecording || isUploading),
            shape = CircleShape,
            modifier = Modifier.size(50.dp),
            border = BorderStroke(0.5.dp, Color(0xFFCCCCCC)),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, modifier = Modifier.size(30.dp), contentDescription = "Add attachment")
        }
    }
}
