package com.vaultmessenger.modules

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.vaultmessenger.interfaces.AudioRecorder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class VoiceRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val userId:String = FirebaseService.auth.currentUser?.uid.toString()

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder(context)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun start(outputFile: File) {
        Log.d("VoiceRecorder", "Starting recording to file: ${outputFile.absolutePath}")

        this.outputFile = outputFile
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
            Log.d("VoiceRecorder", "Recording started successfully")
        }
    }

    override fun stop(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            recorder?.stop()
            recorder?.reset()
            recorder = null

            outputFile?.let {
                if (it.exists()) {
                    Log.d("VoiceRecorder", "File exists, proceeding with upload")
                    uploadVoiceNoteToFirebaseStorage(it, { url ->
                        Log.d("VoiceRecorder", "Upload success, URL: $url")
                        onSuccess(url)
                    }, onFailure)
                } else {
                    Log.e("VoiceRecorder", "File does not exist at: ${it.absolutePath}")
                    onFailure(Exception("Output file does not exist"))
                }
            } ?: run {
                onFailure(Exception("Output file is null"))
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error stopping recording: ${e.message}")
            onFailure(e)
        }
    }

    private fun uploadVoiceNoteToFirebaseStorage(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageReference = FirebaseService.storage.reference
        val fileReference = storageReference.child("/voice_notes/${userId}/${UUID.randomUUID()}.mp4")

        fileReference.putFile(file.toUri(context))
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()
                    Log.d("VoiceRecorder", "Download URL retrieved: $url")
                    onSuccess(url)
                }.addOnFailureListener { e ->
                    Log.e("VoiceRecorder", "Failed to retrieve download URL: ${e.message}")
                    onFailure(e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("VoiceRecorder", "Upload failed: ${e.message}")
                onFailure(e)
            }
    }


    private fun File.toUri(context: Context): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            this
        )
    }

    private fun saveFileToMediaStore(file: File, context: Context): Uri? {
        Log.d("VoiceRecorder", "Saving file to MediaStore: ${file.absolutePath}")
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("VoiceRecorder", "File saved to MediaStore successfully: $uri")
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Error saving file to MediaStore: ${e.message}")
                return null
            }
        }
        return uri
    }
}
