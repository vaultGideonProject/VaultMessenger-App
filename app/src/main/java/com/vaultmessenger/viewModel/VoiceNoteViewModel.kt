package com.vaultmessenger.viewModel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceNoteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _voiceNoteStates = MutableLiveData<Map<String, VoiceNoteState>>(emptyMap())
    val voiceNoteStates: LiveData<Map<String, VoiceNoteState>> get() = _voiceNoteStates

    private val players = mutableMapOf<String, ExoPlayer>()

    private val _currentPlayer = MutableLiveData("")
   // val currentPlayer: LiveData<String> get() = _currentPlayer

    private var progressUpdateJob: Job? = null

    fun setupPlayer(voiceNoteId: String, url: String) {
        players[voiceNoteId]?.release()

        val context = getApplication<Application>().applicationContext
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = _voiceNoteStates.value?.get(voiceNoteId)?.isPlaying ?: false
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val durationMs = player.duration
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(
                                duration = formatDuration(durationMs),
                                isBuffering = false // Ensure buffering state is reset
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(isPlaying = false, progress = 0f, isBuffering = false)
                        }
                        progressUpdateJob?.cancel()
                    }
                    Player.STATE_BUFFERING -> {
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(isBuffering = true)
                        }
                    }
                    Player.STATE_IDLE -> {
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(isPlaying = false, progress = 0f, isBuffering = false)
                        }
                        progressUpdateJob?.cancel()
                    }
                }
            }
        })


        players[voiceNoteId] = player
    }

    fun playPause(voiceNoteId: String) {
        val currentVoiceNoteId = _currentPlayer.value

        if (currentVoiceNoteId != null && currentVoiceNoteId != voiceNoteId) {
            val currentPlayer = players[currentVoiceNoteId]
            currentPlayer?.let {
                it.playWhenReady = false
                updateVoiceNoteState(currentVoiceNoteId) { state ->
                    state.copy(isPlaying = false)
                }
                progressUpdateJob?.cancel()
            }
        }

        val player = players[voiceNoteId] ?: return
        val isPlayingNow = player.isPlaying

        updateVoiceNoteState(voiceNoteId) {
            it.copy(isPlaying = !isPlayingNow)
        }
        player.playWhenReady = !isPlayingNow

        if (!isPlayingNow) {
            startUpdatingProgress(voiceNoteId, player)
        } else {
            progressUpdateJob?.cancel()
        }

        if (!player.isPlaying && player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.playWhenReady = true
        }

        _currentPlayer.value = if (isPlayingNow) null else voiceNoteId
    }

    private fun startUpdatingProgress(voiceNoteId: String, player: ExoPlayer) {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                val progress = player.currentPosition.toFloat() / player.duration
                updateVoiceNoteState(voiceNoteId) {
                    it.copy(progress = progress)
                }
                delay(500L) // Update progress every 500ms
            }
        }
    }

    fun releasePlayer(voiceNoteId: String) {
        players.remove(voiceNoteId)?.release()
    }

    private fun updateVoiceNoteState(voiceNoteId: String, update: (VoiceNoteState) -> VoiceNoteState) {
        val currentState = _voiceNoteStates.value ?: emptyMap()
        val updatedState = update(currentState[voiceNoteId] ?: VoiceNoteState())
        _voiceNoteStates.value = currentState + (voiceNoteId to updatedState)
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 60000).toInt()
        val seconds = ((durationMs % 60000) / 1000).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}

data class VoiceNoteState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: String = "00:00",
    val isBuffering: Boolean = false
)
