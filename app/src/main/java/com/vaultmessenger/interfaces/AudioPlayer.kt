package com.vaultmessenger.interfaces
import android.net.Uri

interface AudioPlayer {
    fun playFile(uri: Uri)
    fun stop()
}
