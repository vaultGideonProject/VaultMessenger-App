package com.vaultmessenger.viewModel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceNoteViewModel(
    application: Application,
    private val errorsViewModel: ErrorsViewModel
) : AndroidViewModel(application) {

    private val _voiceNoteStates = MutableLiveData<Map<String, VoiceNoteState>>(emptyMap())
    val voiceNoteStates: LiveData<Map<String, VoiceNoteState>> get() = _voiceNoteStates

    private val players = mutableMapOf<String, ExoPlayer>()

    private val _currentPlayer = MutableLiveData("")
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
                        try {
                            val durationMs = player.duration
                            updateVoiceNoteState(voiceNoteId) {
                                it.copy(
                                    duration = formatDuration(durationMs),
                                    isBuffering = false // Ensure buffering state is reset
                                )
                            }
                        } catch (e: Exception) {
                            errorsViewModel.setError("Error during Player.STATE_READY: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    Player.STATE_ENDED -> {
                        try {
                            updateVoiceNoteState(voiceNoteId) {
                                it.copy(isPlaying = false, progress = 0f, isBuffering = false)
                            }
                            progressUpdateJob?.cancel()
                        } catch (e: Exception) {
                            errorsViewModel.setError("Error during Player.STATE_ENDED: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        try {
                            updateVoiceNoteState(voiceNoteId) {
                                it.copy(isBuffering = true)
                            }
                        } catch (e: Exception) {
                            errorsViewModel.setError("Error during Player.STATE_BUFFERING: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    Player.STATE_IDLE -> {
                        try {
                            updateVoiceNoteState(voiceNoteId) {
                                it.copy(isPlaying = false, progress = 0f, isBuffering = false)
                            }
                            progressUpdateJob?.cancel()
                        } catch (e: Exception) {
                            errorsViewModel.setError("Error during Player.STATE_IDLE: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            }
        })

        players[voiceNoteId] = player
    }

    fun playPause(voiceNoteId: String) {
        try {
            val currentVoiceNoteId = _currentPlayer.value

            if (currentVoiceNoteId != null && currentVoiceNoteId != voiceNoteId) {
                val currentPlayer = players[currentVoiceNoteId]
                currentPlayer?.let {
                    try {
                        it.playWhenReady = false
                        updateVoiceNoteState(currentVoiceNoteId) { state ->
                            state.copy(isPlaying = false)
                        }
                        progressUpdateJob?.cancel()
                    } catch (e: Exception) {
                        errorsViewModel.setError("Error stopping current player: ${e.message}")
                    }
                }
            }

            val player = players[voiceNoteId] ?: return

            val isPlayingNow = try {
                player.isPlaying
            } catch (e: Exception) {
                errorsViewModel.setError("Error checking player state: ${e.message}")
                return
            }

            try {
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
            } catch (e: Exception) {
                errorsViewModel.setError("Error during play/pause operation: ${e.message}")
            }
        } catch (e: Exception) {
            errorsViewModel.setError("Error in playPause function: ${e.message}")
        }
    }

    private fun startUpdatingProgress(voiceNoteId: String, player: ExoPlayer) {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            try {
                while (true) {
                    try {
                        val progress = player.currentPosition.toFloat() / player.duration
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(progress = progress)
                        }
                        delay(500L) // Update progress every 500ms
                    } catch (e: Exception) {
                        errorsViewModel.setError("Error updating progress for voice note $voiceNoteId: ${e.message}")
                        break
                    }
                }
            } catch (e: Exception) {
                errorsViewModel.setError("Error in startUpdatingProgress function: ${e.message}")
            }
        }
    }

    fun releasePlayer(voiceNoteId: String) {
        try {
            players.remove(voiceNoteId)?.release()
        } catch (e: Exception) {
            errorsViewModel.setError("Error releasing player for voice note $voiceNoteId: ${e.message}")
        }
    }

    private fun updateVoiceNoteState(voiceNoteId: String, update: (VoiceNoteState) -> VoiceNoteState) {
        try {
            val currentState = _voiceNoteStates.value ?: emptyMap()
            val updatedState = update(currentState[voiceNoteId] ?: VoiceNoteState())
            _voiceNoteStates.value = currentState + (voiceNoteId to updatedState)
        } catch (e: Exception) {
            errorsViewModel.setError("Error updating voice note state for $voiceNoteId: ${e.message}")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMs: Long): String {
        return try {
            val minutes = (durationMs / 60000).toInt()
            val seconds = ((durationMs % 60000) / 1000).toInt()
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            errorsViewModel.setError("Error formatting duration: ${e.message}")
            "00:00" // Default fallback in case of error
        }
    }
}


data class VoiceNoteState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: String = "00:00",
    val isBuffering: Boolean = false
)
