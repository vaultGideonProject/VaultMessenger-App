package com.vaultmessenger.ui.item

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.R
import com.vaultmessenger.database.LocalConversation
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.modules.remoteImage
import com.vaultmessenger.modules.countUnreadMessages
import com.vaultmessenger.modules.formatTimestamp
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.provideViewModels
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ConversationItem(
    conversation: LocalConversation,
    chatViewModel: ChatViewModel,
    navController: NavHostController,
    userId: String?,
    errorsViewModel: ErrorsViewModel
) {
    val otherUserIdKey = if (conversation.userIds["userId1"] == userId) "userId2" else "userId1"
    val otherUserNameKey = if (conversation.userIds["userId1"] == userId) "userName2" else "userName1"
    val otherUserPhotoKey = if (conversation.userIds["userId1"] == userId) "profilePictureUrl_userId2" else "profilePictureUrl_userId1"

    val receiverUID = conversation.userIds[otherUserIdKey]
    val pictureURL = conversation.userPhotos[otherUserPhotoKey]

    // Access the receiver user's ViewModel directly
    val receiverUserRepository = ReceiverUserRepository(receiverUID)
    val receiverViewModelFactory = ReceiverUserViewModelFactory(receiverUserRepository, errorsViewModel)
    val receiverUserViewModel: ReceiverUserViewModel = viewModel(factory = receiverViewModelFactory)
    val receiverUser by receiverUserViewModel.receiverUser.collectAsState()
    val conversationTimestamp: String = conversation.timestamp
    val formattedTime = formatTimestamp(conversationTimestamp)
    val context: Context = LocalContext.current
    val messageReady by chatViewModel.messagesReady.collectAsStateWithLifecycle()

    //Lets set conversation count of new messages!

    // Get the messages flow for this specific conversation
    val messagesFlow by chatViewModel.messagesFlow.collectAsState()

// Calculate unread messages and handle image file
    var unreadConversationCount by remember { mutableIntStateOf(0) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(userId, receiverUID) {
        chatViewModel.getMessagesFlow(userId!!, receiverUID!!)
    }

    LaunchedEffect(userId, receiverUID, messagesFlow, conversation.lastMessage) {
        val filteredMessages = messagesFlow.filter {
            (it.userId1 == receiverUID && it.userId2 == userId) ||
                    (it.userId1 == userId && it.userId2 == receiverUID)
        }

        unreadConversationCount = countUnreadMessages(filteredMessages)
    }
    LaunchedEffect(pictureURL) {
        withContext(Dispatchers.IO) {
            // Log the URL for debugging
            println("Profile URL: $pictureURL")

            if (!pictureURL.isNullOrBlank() &&
                (pictureURL.startsWith("http://") || pictureURL.startsWith("https://"))) {
                // If the URL is valid, load the image
                imageFile = remoteImage(context, pictureURL)
            } else {
                // Handle empty or invalid URL (e.g., use a placeholder image)
                println("URL is either empty or invalid: $pictureURL")
                // Set a placeholder or default image here
            }
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("Chat/${userId}/${receiverUID}")
            }
    ) {
      //  asyncImage(
      //      model = pictureURL, // The URL or model for the image
       //     contentDescription = "Profile Image", // Description for accessibility
       //     placeholder = painterResource(id = R.drawable.ic_account_circle_foreground), // Placeholder while loading
      //      error = painterResource(id = R.drawable.ic_stat_name), // Error image if loading fails
      //      modifier = Modifier
      //          .size(55.dp)
      //          .clip(CircleShape),
       //     contentScale = ContentScale.Crop, // How the image should be scaled
      //  )
        if (imageFile != null) {
            // Use the locally downloaded image
            Image(
                painter = rememberAsyncImagePainter(imageFile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape),
                //  .clickable { },
                contentScale = ContentScale.Crop
            )
        } else {
            // Show placeholder while loading
            Image(
                painter = painterResource(R.drawable.ic_account_circle_foreground),  // Placeholder image
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape),
                // .clickable { },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.userNames[otherUserNameKey] ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            horizontalAlignment = Alignment.End // Align the text to the right
        ) {
            // Time text on top
            Text(
                text = formattedTime, // Ensure this is formatted to your needs (e.g., "HH:mm a")
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF001A41)
            )

            Spacer(modifier = Modifier.height(2.dp))

            ConversationNewMessageCount(messageCount = unreadConversationCount)

        }

        Spacer(modifier = Modifier.width(10.dp))
    }
}
