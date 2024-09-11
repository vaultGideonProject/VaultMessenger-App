package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.model.Message
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.ui.item.getFormattedDate

@Composable
fun ChatImageMessage(
    localMessage: LocalMessage,
) {
    // Image zoom state
    var zoomed by remember { mutableStateOf(false) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    val messageTimeStamp = getFormattedDate(localMessage.timestamp)

    // Map to track loading states for each image
    val imageLoadingStates = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize the loading state for the current message
    LaunchedEffect(localMessage.conversationId) {
        if (!localMessage.imageUrl.isNullOrEmpty()) {
            imageLoadingStates[localMessage.conversationId!!] = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Adjust padding for overall layout
    ) {
        // Display loading indicator if the image is being retrieved
        if (imageLoadingStates[localMessage.conversationId] == true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }

        // Display image if URL is present and not "null"
        if (!localMessage.imageUrl.isNullOrEmpty() && localMessage.imageUrl != "null") {
            Image(
                painter = rememberAsyncImagePainter(
                    model = localMessage.imageUrl,
                    onLoading = {
                        imageLoadingStates[localMessage.conversationId!!] = true
                    },
                    onSuccess = {
                        imageLoadingStates[localMessage.conversationId!!] = false
                    },
                    onError = {
                        imageLoadingStates[localMessage.conversationId!!] = false
                    }
                ),
                contentDescription = "Attached Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .aspectRatio(1f), // Maintain aspect ratio for a square image
                contentScale = ContentScale.Crop
            )
        }

        // Display timestamp at the bottom of the image
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(Color(0x80000000)) // Semi-transparent background for readability
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = messageTimeStamp,
                color = Color.White,
                style = TextStyle(fontSize = 12.sp) // Adjust timestamp font size if needed
            )
        }
    }
}
