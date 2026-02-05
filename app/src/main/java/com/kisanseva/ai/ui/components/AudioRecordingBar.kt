package com.kisanseva.ai.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kisanseva.ai.system.audio.recorder.AndroidAudioRecorder
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioRecordingBar(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    audioFile: File?,
    onStartRecording: () -> File,
    onIsRecordingChange: (Boolean) -> Unit,
    onAudioFileChange: (File?) -> Unit,
    onRecordingComplete: (File) -> Unit,
    onRecordingCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioRecorder = remember { AndroidAudioRecorder(context) }

    val amplitudes = remember { mutableStateListOf<Float>() }
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)


    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                amplitudes.add(audioRecorder.getMaxAmplitude() / 32767f)
                delay(100)
            }
        } else {
            amplitudes.clear()
            repeat(20) {
                amplitudes.add(Random.nextFloat())
            }
        }
    }



    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                audioRecorder.cancel()
            }
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isRecording -> {
                // While recording view
                IconButton(onClick = {
                    audioRecorder.cancel()
                    onIsRecordingChange(false)
                    onAudioFileChange(null)
                    onRecordingCancel()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Recording")
                }
                AudioWaveform(modifier = Modifier.weight(1f), amplitudes = amplitudes.toList())
                IconButton(onClick = {
                    audioRecorder.stop()
                    onIsRecordingChange(false)
                    if (audioFile != null) {
                        onRecordingComplete(audioFile)
                    }
                }) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop Recording")
                }
            }
            audioFile != null && !isRecording -> {
                // After recording view
                IconButton(onClick = {
                    onAudioFileChange(null) // Just reset UI, don't delete file.
                    onRecordingCancel()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Recording")
                }
                AudioWaveform(modifier = Modifier.weight(1f), amplitudes = amplitudes.toList())
            }
            else -> {
                // Starting state
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if(recordAudioPermissionState.status.isGranted) {
                            val file = onStartRecording()
                            audioRecorder.start(file)
                            onAudioFileChange(file)
                            onIsRecordingChange(true)
                        } else {
                            recordAudioPermissionState.launchPermissionRequest()
                        }
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Start Recording")
                    }
                }
            }
        }
    }
}