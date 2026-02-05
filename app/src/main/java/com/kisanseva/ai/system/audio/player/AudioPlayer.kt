package com.kisanseva.ai.system.audio.player

import kotlinx.coroutines.flow.StateFlow

enum class AudioState {
    Idle, Loading, Playing, Paused, Completed, Error
}

interface AudioPlayer {
    fun play(uri: String)
    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun seekTo(position: Long)

    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val audioState: StateFlow<AudioState>
    val playbackError: StateFlow<String?>
    val currentAudioUrl: StateFlow<String?>
}
