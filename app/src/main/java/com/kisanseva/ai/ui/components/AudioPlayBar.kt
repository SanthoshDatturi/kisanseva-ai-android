package com.kisanseva.ai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanseva.ai.R
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.audio.player.AudioState
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AudioPlayBar(
    audioSource: String,
    audioPlayer: AudioPlayer,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPosition by audioPlayer.currentPosition.collectAsState()
    val duration by audioPlayer.duration.collectAsState()
    val currentAudioUrl by audioPlayer.currentAudioUrl.collectAsState()
    val audioState by audioPlayer.audioState.collectAsState()

    val isCurrentTrack = currentAudioUrl == audioSource

    val showPlaying = isCurrentTrack && isPlaying
    val isLoading = isCurrentTrack && audioState == AudioState.Loading
    val sliderPosition = if (isCurrentTrack) currentPosition.toFloat() else 0f
    val sliderDuration = if (isCurrentTrack && duration > 0) duration.toFloat() else 1f

    Row(
        modifier = modifier
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp),
                strokeWidth = 2.dp,
                color = tint
            )
        } else {
            IconButton(
                onClick = {
                    if (isCurrentTrack) {
                        if (showPlaying) audioPlayer.pause() else audioPlayer.resume()
                    } else {
                        audioPlayer.play(audioSource)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (showPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = {
                    if (isCurrentTrack) {
                        audioPlayer.seekTo(it.toLong())
                    }
                },
                valueRange = 0f..sliderDuration,
                colors = SliderDefaults.colors(
                    thumbColor = tint,
                    activeTrackColor = tint,
                    inactiveTrackColor = tint.copy(alpha = 0.3f)
                ),
                modifier = Modifier.height(18.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCurrentTrack) formatDuration(currentPosition) else "00:00",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = tint.copy(alpha = 0.8f)
                )
                Text(
                    text = if (isCurrentTrack && duration > 0) formatDuration(duration) else "00:00",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = tint.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
