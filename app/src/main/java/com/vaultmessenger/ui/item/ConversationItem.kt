package com.vaultmessenger.ui.item

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.vaultmessenger.ProfileImage
import com.vaultmessenger.R
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.modules.asyncImage
import com.vaultmessenger.modules.countUnreadMessages
import com.vaultmessenger.modules.formatTimestamp
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProvideViewModels
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ConversationItem(
    conversation: Conversation,
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

    //Lets set conversation count of new messages!
    val (
      chatViewModel,
    ) = ProvideViewModels(context, senderUID = userId!!)

    // Get the messages flow for this specific conversation
    val messagesFlow = chatViewModel.getMessagesFlow(userId!!, receiverUID!!)

    // Collect the flow as state
    val messagesState by messagesFlow.collectAsStateWithLifecycle(emptyList())

    // Calculate unread messages
    var unreadConversationCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId, receiverUID, messagesState, conversation.lastMessage) {
        val filteredMessages = messagesState.filter {
            (it.userId1 == receiverUID && it.userId2 == userId) ||
                    (it.userId1 == userId && it.userId2 == receiverUID)
        }

        unreadConversationCount = countUnreadMessages(filteredMessages)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("Chat/${userId}/${receiverUID}")
            }
    ) {
        asyncImage(
            model = pictureURL, // The URL or model for the image
            contentDescription = "Profile Image", // Description for accessibility
            placeholder = painterResource(id = R.drawable.ic_account_circle_foreground), // Placeholder while loading
            error = painterResource(id = R.drawable.ic_stat_name), // Error image if loading fails
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop, // How the image should be scaled
        )

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
