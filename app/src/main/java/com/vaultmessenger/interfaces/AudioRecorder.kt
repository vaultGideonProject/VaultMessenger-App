package com.vaultmessenger.interfaces

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}