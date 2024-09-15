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
import kotlinx.coroutines.isActive
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

    // Reusable function to handle errors
    private fun handlePlayerError(message: String, exception: Exception) {
        errorsViewModel.setError("$message: ${exception.message}")
        exception.printStackTrace()
    }

    fun setupPlayer(voiceNoteId: String, url: String) {
        // Release the current player if it exists
        players[voiceNoteId]?.release()

        val context = getApplication<Application>().applicationContext
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = _voiceNoteStates.value?.get(voiceNoteId)?.isPlaying ?: false
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                try {
                    when (playbackState) {
                        Player.STATE_READY -> updateStateReady(voiceNoteId, player)
                        Player.STATE_ENDED -> updateStateEnded(voiceNoteId)
                        Player.STATE_BUFFERING -> updateVoiceNoteState(voiceNoteId) { it.copy(isBuffering = true) }
                        Player.STATE_IDLE -> resetState(voiceNoteId)
                    }
                } catch (e: Exception) {
                    handlePlayerError("Error during playback state change", e)
                }
            }
        })

        players[voiceNoteId] = player
    }

    private fun updateStateReady(voiceNoteId: String, player: ExoPlayer) {
        val durationMs = player.duration
        updateVoiceNoteState(voiceNoteId) {
            it.copy(
                duration = formatDuration(durationMs),
                isBuffering = false
            )
        }
    }

    private fun updateStateEnded(voiceNoteId: String) {
        updateVoiceNoteState(voiceNoteId) {
            it.copy(isPlaying = false, progress = 0f, isBuffering = false)
        }
        progressUpdateJob?.cancel()
    }

    private fun resetState(voiceNoteId: String) {
        updateVoiceNoteState(voiceNoteId) {
            it.copy(isPlaying = false, progress = 0f, isBuffering = false)
        }
        progressUpdateJob?.cancel()
    }

    fun playPause(voiceNoteId: String) {
        val currentVoiceNoteId = _currentPlayer.value

        if (currentVoiceNoteId != null && currentVoiceNoteId != voiceNoteId) {
            stopCurrentPlayer(currentVoiceNoteId)
        }

        val player = players[voiceNoteId] ?: return

        val isPlayingNow = try {
            player.isPlaying
        } catch (e: Exception) {
            handlePlayerError("Error checking player state", e)
            return
        }

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

    private fun stopCurrentPlayer(currentVoiceNoteId: String) {
        players[currentVoiceNoteId]?.let { player ->
            player.playWhenReady = false
            updateVoiceNoteState(currentVoiceNoteId) { it.copy(isPlaying = false) }
            progressUpdateJob?.cancel()
        }
    }

    private fun startUpdatingProgress(voiceNoteId: String, player: ExoPlayer) {
        progressUpdateJob?.cancel()  // Cancel any existing progress update job

        progressUpdateJob = viewModelScope.launch {
            while (isActive) {  // Ensure the coroutine stops if it's cancelled
                try {
                    // Check if playback has ended
                    if (player.playbackState == Player.STATE_ENDED) {
                        // Ensure progress is set to 100% when the voice note ends
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(progress = 1f)
                        }
                        break
                    }

                    // Ensure the duration is valid before calculating progress
                    val duration = player.duration
                    if (duration > 0) {
                        // Calculate the progress as a fraction of the total duration
                        val progress = player.currentPosition.toFloat() / duration
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(progress = progress)
                        }
                    } else {
                        // Handle the case when duration is invalid (e.g., not yet loaded)
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(progress = 0f)
                        }
                    }

                    delay(500L)  // Adjust update frequency

                } catch (e: Exception) {
                    handlePlayerError("Error updating progress for voice note $voiceNoteId", e)
                    break
                }
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
        return try {
            val minutes = (durationMs / 60000).toInt()
            val seconds = ((durationMs % 60000) / 1000).toInt()
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            handlePlayerError("Error formatting duration", e)
            "00:00" // Default fallback
        }
    }
}

data class VoiceNoteState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: String = "00:00",
    val isBuffering: Boolean = false
)
