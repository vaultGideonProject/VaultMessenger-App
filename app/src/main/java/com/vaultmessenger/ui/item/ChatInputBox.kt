package com.vaultmessenger.ui.item

import NotificationsViewModel
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import com.vaultmessenger.R
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatInputBox(
    senderUID: String,
    receiverUID: String,
    name: String,
    photoUrl: String,
    conversationUserIds: Map<String, String>,
    conversationUserNames:  HashMap<String, String>,
    conversationUserProfilePhotos: Map<String, String>,
    onSendMessage: (Message, Conversation) -> Unit, // Pass the onSendMessage lambda
    modifier: Modifier,
    notificationsViewModel: NotificationsViewModel,
    profileViewModel: ProfileViewModel,
    chatViewModel: ChatViewModel,
    conversationViewModel: ConversationViewModel,
    navController: NavController,
) {
    var chatInputBox by remember { mutableStateOf(TextFieldValue("")) }

    val scope = rememberCoroutineScope()

    val viewModelStoreOwner = LocalViewModelStoreOwner.current


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            sendImageMessage(it) { downloadUrl ->
                var imageToSend = Message(
                    messageText = "",
                    name = name,
                    photoUrl = photoUrl,
                    timestamp = System.currentTimeMillis().toString(),
                    userId1 = senderUID,
                    userId2 = receiverUID,
                    conversationId = generateConversationId(),
                    imageUrl = downloadUrl,
                )
                if(downloadUrl.isNotEmpty()){
                    try{
                        chatViewModel.viewModelScope.launch {
                            chatViewModel.sendImageMessage(
                                senderUid = senderUID,
                                receiverUid = receiverUID,
                                message = imageToSend
                            )
                        }
                        conversationViewModel.viewModelScope.launch {
                            val updatedImageMessage = imageToSend.copy(messageText = "[sent you a photo]")
                            conversationViewModel.setConversationBySenderId(
                                senderId = senderUID,
                                receiverId = receiverUID,
                                message = updatedImageMessage,
                                viewModelStoreOwner = viewModelStoreOwner,
                            )
                        }
                        scope.launch {
                            val getToken = profileViewModel.getUserToken(userId = receiverUID)
                            profileViewModel.isUserOnlineStatus(receiverUID) { isOnline ->
                                if (isOnline == false) {
                                    getToken?.let { token ->
                                        notificationsViewModel.sendNotification(
                                            token = token,
                                            body = "[sent you a Photo]",
                                            title = name,
                                            imageURL = photoUrl
                                        )
                                    }
                                }
                            }
                        }

                    }catch(e:Exception){
                        throw e
                    }
                }
            }
        }
    }

    Row(
        modifier = modifier
            .padding(1.dp)
            .background(Color(0xFFF6F7FA))
            .padding(horizontal = 1.dp, vertical = 0.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            value = chatInputBox,
            onValueChange = { newText ->
                chatInputBox = newText
            },

            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = Color.Blue,
                unfocusedLeadingIconColor = Color.Gray, // Use your desired color
                disabledLeadingIconColor = Color.LightGray, // Use your desired color
                focusedTrailingIconColor = Color.Blue,
                unfocusedTrailingIconColor = Color.Gray, // Use your desired color
                disabledTrailingIconColor = Color.LightGray // Use your desired color
            ),
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_attach_file_24),
                            contentDescription = "Add attachment"
                        )
                    }
                   // IconButton(
                   //     onClick = {

                  //      },
                  //      modifier = Modifier.padding(start = 8.dp) // Adjust spacing between icons
                 //   ) {
                 //       Icon(
                  //          imageVector = Icons.Filled.Camera, // Using the Camera icon from Icons.Filled
                 //           contentDescription = "Camera Icon", // Provide a meaningful description for accessibility
                  //          modifier = modifier// Apply any modifiers such as size or padding
                 //       )
                 //   }
                }
            },

            placeholder = {
                Text(text = "Type something")
            }
        )

        IconButton(
            onClick = {
                val msgText = chatInputBox.text
                if (msgText.isBlank()) return@IconButton

                val messageToSend = Message(
                    messageText = msgText,
                    name = name,
                    photoUrl = photoUrl,
                    timestamp = System.currentTimeMillis().toString(),
                    userId1 = senderUID,
                    userId2 = receiverUID,
                    conversationId = generateConversationId(),
                    imageUrl = null
                )

                val conversationToSend = Conversation(
                    conversationId = messageToSend.conversationId!!,
                    lastMessage = msgText,
                    timestamp = System.currentTimeMillis().toString(),
                    userIds = conversationUserIds,
                    userNames = conversationUserNames,
                    userPhotos = conversationUserProfilePhotos
                )

                // Call the onSendMessage lambda
                onSendMessage(messageToSend, conversationToSend)

                scope.launch {
                    val getToken = profileViewModel.getUserToken(userId = receiverUID)
                    profileViewModel.isUserOnlineStatus(receiverUID) { isOnline ->
                        if (!isOnline) {
                            getToken?.let { token ->
                                notificationsViewModel.sendNotification(
                                    token = token,
                                    body = messageToSend.messageText,
                                    title = name,
                                    imageURL = photoUrl
                                )
                            }
                        }
                    }
                }

                // Clear the chat input box
                chatInputBox = TextFieldValue("")
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF0D62CA))
                .align(Alignment.CenterVertically)
        )
        {
            if (chatInputBox.text.isEmpty()) {
                //lets set mic icon and function
                PulsingMicIcon(
                    chatViewModel = chatViewModel,
                    receiverUID = receiverUID,
                    senderUID = senderUID,
                    conversationViewModel = conversationViewModel,
                    viewModelStoreOwner = viewModelStoreOwner,
                    userName = name,
                    profilePhoto = photoUrl,
                    onSuccess = {sendNotification->
                        if(sendNotification){
                            scope.launch {
                                val getToken = profileViewModel.getUserToken(userId = receiverUID)
                                profileViewModel.isUserOnlineStatus(receiverUID) { isOnline ->
                                    if (!isOnline) {
                                        getToken?.let { token ->
                                            notificationsViewModel.sendNotification(
                                                token = token,
                                                body = "[sent you a recording]",
                                                title = name,
                                                imageURL = photoUrl
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_send_24),
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

fun sendImageMessage(uri: Uri, onSuccess: (String) -> Unit) {
    // Access FirebaseAuth
    val auth = FirebaseService.auth

// Access Firestore
    val firestore = FirebaseService.firestore

// Access FirebaseStorage
    val storage = FirebaseService.storage
    val getUserId = auth.currentUser!!.uid
    val storageRef = storage.reference.child("image_attachment/${getUserId}/${UUID.randomUUID()}.jpg")

    val uploadTask = storageRef.putFile(uri)
    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            onSuccess(downloadUrl.toString())
            Log.d("ImageUpload", "Success")

        }
    }.addOnFailureListener {
        // Handle any errors
        Log.d("ImageUpload", "Fail")
    }
}

fun generateConversationId(): String {
    return UUID.randomUUID().toString()
}