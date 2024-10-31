package com.vaultmessenger.ui.item

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.VoiceNoteState
import com.vaultmessenger.viewModel.VoiceNoteViewModel

@Composable
fun ChatListItem(
    localMessage: LocalMessage,
    receiverUID: String,
    receiverUser: ReceiverUser,
    profileViewModel: ProfileViewModel,
    voiceNoteViewModel: VoiceNoteViewModel,
    chatViewModel:ChatViewModel,
    senderUid: String,
    MenuActionItem: MutableState<String>,
    replyMessage:MutableState<String?>,
) {

    //get Receiver User Profile:


    //get current User Profile:
    val userList by profileViewModel.user.collectAsState()

    var zoomed by remember { mutableStateOf(true) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    val voiceNoteId = localMessage.voiceNoteURL ?: ""

    val voiceNoteStates by voiceNoteViewModel.voiceNoteStates.observeAsState(emptyMap())

    if (localMessage.voiceNoteURL?.isNotBlank() == true) {
        LaunchedEffect(localMessage.voiceNoteURL) {
            voiceNoteViewModel.setupPlayer(localMessage.voiceNoteURL, localMessage.voiceNoteURL)
        }
        DisposableEffect(localMessage.voiceNoteURL) {
            onDispose {
                voiceNoteViewModel.releasePlayer(localMessage.voiceNoteURL)
                //   voiceNoteViewModel.releaseAllPlayers(localMessage.voiceNoteURL)
            }
        }
    }

    val voiceNoteState = voiceNoteStates[voiceNoteId] ?: VoiceNoteState()

    //check if message is valid
    if(localMessage.messageText.isNotBlank()||localMessage.imageUrl?.isNotBlank() == true|| localMessage.voiceNoteURL?.isNotBlank() == true){

    }else{
        return
    }

    // Track whether the message has already been marked as read
    var isMessageRead by remember { mutableStateOf(localMessage.messageRead) }

    val isDropDownExpanded = remember { mutableStateOf(false) }
    val menuActionItem = remember { mutableStateOf("") }

    // LaunchedEffect to mark the message as read when it is displayed
    LaunchedEffect(key1 = localMessage, key2 = isMessageRead) {
        if (!isMessageRead!! && localMessage.userId2 == receiverUID) {
            chatViewModel.markMessageAsRead(localMessage, senderUid, receiverUID)
            isMessageRead = true
        }
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
            horizontalArrangement = if (localMessage.userId1 == "userList?.userId") Arrangement.End else Arrangement.Start
        ) {
            if (localMessage.userId1 != localMessage.userId2) {

                // Display avatar on the left if the message is from the other user
                ChatProfileImage(localMessage = localMessage,
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
                            bottomStart = if (localMessage.userId1 == localMessage.userId2) 16.dp else 0.dp,
                            bottomEnd = if (localMessage.userId1 == "userList?.userId") 0.dp else 16.dp
                        )
                    )
                    .background(
                        if(receiverUID == localMessage.userId2){
                            Color(0xFF0D62CA)
                        }else{
                            Color(0xFF902698)
                        }
                    )
                    .padding(16.dp)
                    .clickable(
                        onClick = {
                            isDropDownExpanded.value = true
                        }
                    )
            ) {
                ChatMessagesItemMenu(isDropDownExpanded) { selectedAction ->
                    menuActionItem.value = selectedAction // Set the selected action
                    MenuActionItem.value = selectedAction
                    replyMessage.value = localMessage.messageText
                    Log.d("Menuclick", selectedAction) // Log the selected action
                }

                if(localMessage.messageText.isNotBlank()){
                    ChatTextMessage(localMessage = localMessage)
                }
                if(localMessage.imageUrl?.isNotBlank() == true){
                    ChatImageMessage(
                        localMessage = localMessage,
                    )
                }
                if (localMessage.voiceNoteURL?.isNotBlank() == true) {
                    if (localMessage.voiceNoteURL.isNotBlank()) {
                        VoiceNoteUI(
                            isPlaying = voiceNoteState.isPlaying,
                            progress = voiceNoteState.progress,
                            duration = voiceNoteState.duration,
                            onPlayPauseClick = { voiceNoteViewModel.playPause(localMessage.voiceNoteURL) }
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