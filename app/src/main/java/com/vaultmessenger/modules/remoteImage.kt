package com.vaultmessenger.modules

import android.content.Context
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

// Function to download image and save to local storage, or return existing file
fun remoteImage(context: Context, imageUrl: String): File? {
    if (imageUrl.isBlank()) {
        return null
    }

    val fileName = imageUrl.let {
        Uri.parse(it).lastPathSegment?.substringAfterLast("/") ?:"Unknown"
    }

    // Define the file path
    val file = File(context.filesDir, fileName)

    // Check if the file already exists
    if (file.exists()) {
        return file // Return the existing file if found
    }

    // Proceed to download the file if it doesn't exist
    val client = OkHttpClient()
    val request = Request.Builder().url(imageUrl).build()

    return try {
        val response = client.newCall(request).execute()
        val inputStream = response.body?.byteStream()

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file // Return the downloaded file
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null if there's an error
    }
}
