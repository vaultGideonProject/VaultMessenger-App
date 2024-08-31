package com.vaultmessenger.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

@Composable
fun RecordingScreen() {
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaRecorder = remember { MediaRecorder() }
    val output = remember { Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3" }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(context as ComponentActivity, permissions, 0)
                } else {
                    if (!isRecording) {
                        startRecording(context, mediaRecorder, output)
                        isRecording = true
                        isPaused = false
                        Toast.makeText(context, "Recording started!", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Start Recording")
            }

            Button(onClick = {
                if (isRecording) {
                    stopRecording(mediaRecorder)
                    isRecording = false
                    Toast.makeText(context, "Recording stopped!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "You are not recording right now!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Stop Recording")
            }

            Button(onClick = {
                if (isRecording) {
                    if (isPaused) {
                        resumeRecording(mediaRecorder)
                        isPaused = false
                        Toast.makeText(context, "Recording resumed!", Toast.LENGTH_SHORT).show()
                    } else {
                        pauseRecording(mediaRecorder)
                        isPaused = true
                        Toast.makeText(context, "Recording paused!", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(if (isPaused) "Resume Recording" else "Pause Recording")
            }
        }
    }
}

@SuppressLint("NewApi")
fun startRecording(context: Context, mediaRecorder: MediaRecorder, output: String) {
    mediaRecorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(output)

        try {
            prepare()
            start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

@SuppressLint("NewApi")
fun pauseRecording(mediaRecorder: MediaRecorder) {
    mediaRecorder.pause()
}

@SuppressLint("NewApi")
fun resumeRecording(mediaRecorder: MediaRecorder) {
    mediaRecorder.resume()
}

fun stopRecording(mediaRecorder: MediaRecorder) {
    mediaRecorder.apply {
        stop()
        release()
    }
}
