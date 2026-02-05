package com.kisanseva.ai.system.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AndroidAudioPlayer (
    private val context: Context
) : AudioPlayer, DefaultLifecycleObserver {

    private var player: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _audioState = MutableStateFlow(AudioState.Idle)
    override val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    override val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _currentAudioUrl = MutableStateFlow<String?>(null)
    override val currentAudioUrl: StateFlow<String?> = _currentAudioUrl.asStateFlow()

    init {
        scope.launch {
            withContext(Dispatchers.Main) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(this@AndroidAudioPlayer)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stop()
    }

    override fun play(uri: String) {
        // If playing the same file and paused, resume it
        if (_currentAudioUrl.value == uri && (_audioState.value == AudioState.Paused || _audioState.value == AudioState.Completed)) {
            resume()
            return
        }

        // If already playing the same file, do nothing
        if (_currentAudioUrl.value == uri && _audioState.value == AudioState.Playing) {
            return
        }

        // Stop previous playback
        stop()

        _currentAudioUrl.value = uri
        _audioState.value = AudioState.Loading
        _playbackError.value = null

        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                val parsedUri = Uri.parse(uri)
                if (parsedUri.scheme == null) {
                    // Assume it's a local file path
                    setDataSource(uri)
                } else {
                    setDataSource(context, parsedUri)
                }

                setOnPreparedListener { mp ->
                    _duration.value = mp.duration.toLong()
                    start()
                    _audioState.value = AudioState.Playing
                    _isPlaying.value = true
                    startProgressTracking()
                }
                setOnCompletionListener {
                    _audioState.value = AudioState.Completed
                    _isPlaying.value = false
                    _currentPosition.value = 0L
                    stopProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    _playbackError.value = "MediaPlayer Error: $what, $extra"
                    _audioState.value = AudioState.Error
                    _isPlaying.value = false
                    stopProgressTracking()
                    resetPlayer()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playbackError.value = "Error initializing player: ${e.message}"
            _audioState.value = AudioState.Error
            _isPlaying.value = false
            resetPlayer()
        }
    }

    override fun pause() {
        if (_audioState.value == AudioState.Playing) {
            try {
                player?.pause()
                _audioState.value = AudioState.Paused
                _isPlaying.value = false
                stopProgressTracking()
            } catch (e: Exception) {
                _playbackError.value = "Error pausing: ${e.message}"
            }
        }
    }

    override fun resume() {
        if (_audioState.value == AudioState.Paused || _audioState.value == AudioState.Completed) {
            try {
                player?.start()
                _audioState.value = AudioState.Playing
                _isPlaying.value = true
                startProgressTracking()
            } catch (e: Exception) {
                 _playbackError.value = "Error resuming: ${e.message}"
            }
        }
    }

    override fun stop() {
        stopProgressTracking()
        resetPlayer()
        _audioState.value = AudioState.Idle
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    private fun resetPlayer() {
        try {
            if (player?.isPlaying == true) {
                player?.stop()
            }
            player?.reset()
            player?.release()
        } catch (e: Exception) {
            // Ignore errors during reset
        }
        player = null
    }

    override fun release() {
        // Since this is a Singleton, we do not remove the LifecycleObserver.
        // We simply stop playback to free resources.
        stop()
    }

    override fun seekTo(position: Long) {
        player?.let {
            if (_audioState.value != AudioState.Idle && _audioState.value != AudioState.Error && _audioState.value != AudioState.Loading) {
                try {
                    it.seekTo(position.toInt())
                    _currentPosition.value = position
                } catch (e: Exception) {
                     _playbackError.value = "Error seeking: ${e.message}"
                }
            }
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (true) {
                if (_isPlaying.value) {
                    player?.let {
                        try {
                            _currentPosition.value = it.currentPosition.toLong()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                delay(50) // Update every 50ms for smooth slider
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
}
