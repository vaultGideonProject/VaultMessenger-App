package com.vaultmessenger.modules

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.vaultmessenger.interfaces.AudioPlayer

class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {

    private var player: MediaPlayer? = null

    override fun playFile(uri: Uri) {
        MediaPlayer.create(context, uri).apply {
            player = this
            start()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}
