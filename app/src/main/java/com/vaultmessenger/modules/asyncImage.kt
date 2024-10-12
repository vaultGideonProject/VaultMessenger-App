package com.vaultmessenger.modules

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun asyncImage(imageUrl: String, modifier: Modifier = Modifier, context: Context) {
    var imageFile by remember { mutableStateOf<File?>(null) }

    // Download and save the image in the background
    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            imageFile = remoteImage(context, imageUrl)
        }
    }

    // If image is saved successfully, load it into Image composable
    imageFile?.let { file ->
        Image(
            painter = rememberAsyncImagePainter(file),
            contentDescription = null,
            modifier = modifier.size(150.dp)
        )
    }
}
