package com.vaultmessenger.viewModel

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

class VoiceNoteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _voiceNoteStates = MutableLiveData<Map<String, VoiceNoteState>>(emptyMap())
    val voiceNoteStates: LiveData<Map<String, VoiceNoteState>> get() = _voiceNoteStates

    private val players = mutableMapOf<String, ExoPlayer>()

    @OptIn(UnstableApi::class)
    fun setupPlayer(voiceNoteId: String, url: String) {
        // Release the existing player for the voiceNoteId
        players[voiceNoteId]?.let { player ->
            player.release()
        }

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
                            it.copy(duration = formatDuration(durationMs))
                        }
                    }
                    Player.STATE_ENDED -> {
                        updateVoiceNoteState(voiceNoteId) {
                            it.copy(isPlaying = false, progress = 0f) // Update state on EOF
                        }
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onPositionDiscontinuity(reason: Int) {
                val currentPosition = player.currentPosition
                updateVoiceNoteState(voiceNoteId) {
                    it.copy(progress = currentPosition.toFloat() / player.duration)
                }
            }
        })

        // Update the players map with the new player instance
        players[voiceNoteId] = player
    }

    fun playPause(voiceNoteId: String) {
        val player = players[voiceNoteId] ?: return
        val isPlayingNow = player.isPlaying
        updateVoiceNoteState(voiceNoteId) {
            it.copy(isPlaying = !isPlayingNow)
        }
        player.playWhenReady = !isPlayingNow
        if (!player.isPlaying && player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0) // Reset position if playback has ended
            player.playWhenReady = true // Automatically start playing from the beginning
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
    val duration: String = "00:00"
)
