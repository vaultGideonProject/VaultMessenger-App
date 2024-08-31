package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vaultmessenger.model.ReceiverUser
import kotlin.math.min

@Composable
fun ChatListItem(
    message: Message,
    receiverUID: String
) {
    //get Receiver User Profile:


    //get current User Profile:
    val userRepository = FirebaseUserRepository() // Create an instance of the repository
    val userViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository)
    )
    val userList by userViewModel.user.collectAsState()

    val messageTimeStamp = getFormattedDate(message.timestamp)

    var zoomed by remember { mutableStateOf(true) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    var isPlaying by remember {
        mutableStateOf(false)
    }
    var Progress by remember {
        mutableStateOf(0.0f)
    }
    var duration by remember {
        mutableStateOf("500")
    }


    // Map to track loading states for each image
    val imageLoadingStates = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize the loading state for the current message
    LaunchedEffect(message.conversationId) {
        if(message.imageUrl != null){
            if (!imageLoadingStates.contains(message.conversationId)) {
                imageLoadingStates[message.conversationId!!] = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom, // Align avatar with the bottom of the message box
            horizontalArrangement = if (message.userId1 == "userList?.userId") Arrangement.End else Arrangement.Start
        ) {
            if (message.userId1 != message.userId2) {
                // Display avatar on the left if the message is from the other user
                val profilePictureUrl = if (message.userId1 == receiverUID) {
                    message.photoUrl
                } else {
                    userList?.profilePictureUrl
                }

                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, color = Color.Blue, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp)) // Space between avatar and message
            }

            Row(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.userId1 == message.userId2) 16.dp else 0.dp,
                            bottomEnd = if (message.userId1 == "userList?.userId") 0.dp else 16.dp
                        )
                    )
                    .background(Color(0xFF0D62CA))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(2.dp) // Padding around the entire message block
                ) {
                    // Display message text if it's not blank
                    if (message.messageText.isNotBlank()) {
                        Text(text = message.messageText, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp)) // Space between text and loading indicator or image
                    }

                    // Display loading indicator if the image is being retrieved
                    if (imageLoadingStates[message.conversationId] == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp) // Adjust padding for the indicator
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color.White, // Use white color for the loading indicator
                                strokeWidth = 4.dp // Adjust the thickness of the indicator
                            )
                        }
                    }

                    // Display image if URL is present and not "null"
                    if (message.imageUrl != null && message.imageUrl != "null") {
                        Spacer(modifier = Modifier.height(4.dp)) // Space between text and image

                        Image(
                            painter = rememberAsyncImagePainter(
                                model = message.imageUrl,
                                onLoading = {
                                    // Image is still loading
                                    imageLoadingStates[message.conversationId!!] = true
                                },
                                onSuccess = {
                                    // Image loaded successfully
                                    imageLoadingStates[message.conversationId!!] = false
                                },
                                onError = {
                                    // Handle error if needed
                                    imageLoadingStates[message.conversationId!!] = false
                                }
                            ),
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { tapOffset ->
                                            zoomOffset =
                                                if (zoomed) Offset.Zero else calculateOffset(
                                                    tapOffset,
                                                    imageSize
                                                )
                                            zoomed = !zoomed
                                        }
                                    )
                                }
                                .graphicsLayer {
                                    scaleX = if (zoomed) 2f else 1f
                                    scaleY = if (zoomed) 2f else 1f
                                    translationX = zoomOffset.x
                                    translationY = zoomOffset.y
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .aspectRatio(1f), // Maintain aspect ratio for a square image
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Space between image and timestamp
                    }

                    if (message.voiceNoteURL != null && message.voiceNoteURL != "null") {
                        VoiceNoteUI(
                            isPlaying = isPlaying,
                            progress = Progress,
                            duration = duration,
                            onPlayPauseClick = { isPlaying = !isPlaying }
                        )
                    }

                    // Display timestamp at the bottom
                    Text(
                        text = messageTimeStamp,
                        color = Color.White,
                        style = TextStyle(fontSize = 12.sp) // Adjust timestamp font size if needed
                    )
                }
            }
            }
        }

}
// Function to convert Unix timestamp to Date
fun unixTimestampToDate(timestamp: Long): Date {
    return Date(timestamp)
}

// Function to format a Date object into a readable string
fun formatDate(date: Date?, pattern: String = "hh:mm a"): String {
    return if (date != null) {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        dateFormat.format(date)
    } else {
        ""
    }
}

fun calculateOffset(tapOffset: Offset, size: Size): Offset {
    // Center the zoom around the tap point
    val centerX = (size.width / 2) - tapOffset.x
    val centerY = (size.height / 2) - tapOffset.y

    // Ensure the image doesn't move out of bounds
    val constrainedX = centerX.coerceAtLeast(-size.width / 2).coerceAtMost(size.width / 2)
    val constrainedY = centerY.coerceAtLeast(-size.height / 2).coerceAtMost(size.height / 2)

    return Offset(constrainedX, constrainedY)
}

// Function to convert a Unix timestamp string to a formatted date string
fun getFormattedDate(timestamp: String, displayPattern: String = "hh:mm a"): String {
    return try {
        val date = unixTimestampToDate(timestamp.toLong())
        formatDate(date, displayPattern)
    } catch (e: NumberFormatException) {
        // Handle the case where the timestamp is not a valid number
        ""
    }
}