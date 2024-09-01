package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.viewModel.VoiceNoteState
import com.vaultmessenger.viewModel.VoiceNoteViewModel

@Composable
fun ChatListItem(
    message: Message,
    receiverUID: String,
    receiverUser: ReceiverUser
) {

    //get Receiver User Profile:


    //get current User Profile:
    val userRepository = FirebaseUserRepository() // Create an instance of the repository
    val userViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository)
    )
    val userList by userViewModel.user.collectAsState()

    var zoomed by remember { mutableStateOf(true) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    val voiceNoteId = message.voiceNoteURL ?: ""
    val voiceNoteViewModel: VoiceNoteViewModel = viewModel()
    val voiceNoteStates by voiceNoteViewModel.voiceNoteStates.observeAsState(emptyMap())

    if (message.voiceNoteURL?.isNotBlank() == true) {
        LaunchedEffect(message.voiceNoteURL) {
            voiceNoteViewModel.setupPlayer(message.voiceNoteURL, message.voiceNoteURL)
        }
        DisposableEffect(message.voiceNoteURL) {
            onDispose {
                voiceNoteViewModel.releasePlayer(message.voiceNoteURL)
            }
        }
    }

    val voiceNoteState = voiceNoteStates[voiceNoteId] ?: VoiceNoteState()


    //check if message is valid
    if(message.messageText.isNotBlank()||message.imageUrl?.isNotBlank() == true|| message.voiceNoteURL?.isNotBlank() == true){

    }else{
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        //Set Profile Image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom, // Align avatar with the bottom of the message box
            horizontalArrangement = if (message.userId1 == "userList?.userId") Arrangement.End else Arrangement.Start
        ) {
            if (message.userId1 != message.userId2) {

                // Display avatar on the left if the message is from the other user
                ChatProfileImage(message = message,
                    receiverUID = receiverUID,
                    receiverUser = receiverUser,
                    userList = userList)

                Spacer(modifier = Modifier.width(8.dp)) // Space between avatar and message
            }
            //Set Chat message
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
                if(message.messageText.isNotBlank()){
                    ChatTextMessage(message = message)
                }
                if(message.imageUrl?.isNotBlank() == true){
                    ChatImageMessage(
                        message = message,
                        )
                }
                if (message.voiceNoteURL?.isNotBlank() == true) {
                    if (message.voiceNoteURL.isNotBlank()) {
                        VoiceNoteUI(
                            isPlaying = voiceNoteState.isPlaying,
                            progress = voiceNoteState.progress,
                            duration = voiceNoteState.duration,
                            onPlayPauseClick = { voiceNoteViewModel.playPause(message.voiceNoteURL) }
                        )
                    }
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