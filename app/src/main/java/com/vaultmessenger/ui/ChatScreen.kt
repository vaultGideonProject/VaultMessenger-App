package com.vaultmessenger.ui

import NotificationsViewModel
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vaultmessenger.interfaces.LocalMessageStorage
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.interfaces.RemoteMessageStorage
import com.vaultmessenger.model.Message
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.nav.ChatToolbar
import com.vaultmessenger.ui.item.ChatInputBox
import com.vaultmessenger.ui.item.ChatListItem
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ChatViewModelFactory
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ConversationViewModelFactory
import com.vaultmessenger.viewModel.NotificationsViewModelFactory
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import com.vaultmessenger.viewModel.ProvideViewModels
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.internal.wait

@Composable
fun ChatScreen(
    navController: NavHostController,
    senderUID: String?,
    receiverUID: String,
    conversationViewModel: ConversationViewModel,
    notificationsViewModel: NotificationsViewModel,
    profileViewModel: ProfileViewModel,
    receiverUser: ReceiverUser?,
    chatViewModel: ChatViewModel,
    listState: LazyListState,
    context: Context
) {

    // Use remember to avoid recomputing these variables unnecessarily
    val viewModelStoreOwner = LocalViewModelStoreOwner.current

    // Collecting state lazily in a LaunchedEffect
    val messagesReady by chatViewModel.messagesReady.collectAsState(initial = false)
    val isMessageValid by chatViewModel.isMessageValid.collectAsState(initial = true)
    val validationMessage by chatViewModel.validationMessage.collectAsState(initial = "")
    val chatMessagesList by chatViewModel.messages.collectAsState(initial = emptyList())

    // Observing user profile state
    val user by profileViewModel.user.collectAsState(initial = null)
    val userList = remember(user) { user }

    // Only run launchConfigs if necessary, and lazy run them in a LaunchedEffect
    LaunchedEffect(Unit) {
        val launchConfigs = LaunchConfigs()
        launchConfigs.defaults(
            userViewModel = profileViewModel,
            navController = navController
        ).run {
            // Your logic here, executed only once when the composable enters the composition
        }
    }

    // Handling side effects for messagesReady and validationMessage changes
    LaunchedEffect(messagesReady) {
        if (messagesReady) {
            fun getChatCount(): Int{
                return if(chatMessagesList.lastIndex != -1){
                    chatMessagesList.lastIndex
                }else{
                    0
                }
            }
            listState.scrollToItem(getChatCount())
        }
    }

    LaunchedEffect(validationMessage) {
        if (!isMessageValid && validationMessage.isNotEmpty()) {
            Toast.makeText(context, validationMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            receiverUser?.let {
                ChatToolbar(
                    navController = navController,
                    receiverUID = it.userName,
                    profilePhoto = it.profilePictureUrl
                )
            }
        },
        bottomBar = {
            userList?.let { senderUser ->
                receiverUser?.let { receiverUser ->
                    ChatInputBox(
                        senderUID = senderUser.userId,
                        receiverUID = receiverUser.userId,
                        name = senderUser.userName,
                        photoUrl = senderUser.profilePictureUrl,
                        conversationUserIds = mapOf(
                            "userId1" to senderUser.userId,
                            "userId2" to receiverUser.userId
                        ),
                        conversationUserNames = hashMapOf(
                            "userId1" to senderUser.userName,
                            "userId2" to receiverUser.userName
                        ),
                                conversationUserProfilePhotos = mapOf(
                            "userId1" to senderUser.profilePictureUrl,
                            "userId2" to receiverUser.profilePictureUrl
                        ),
                        onSendMessage = { message, conversation ->

                            chatViewModel.updateMessage(message.messageText)

                            chatViewModel.viewModelScope.launch {
                                chatViewModel.sendMessage(
                                    receiverUid = receiverUID!!,
                                    senderUid = senderUID!!, // Assumes senderUID is non-null here
                                    message = message
                                )
                            }
                            conversationViewModel.viewModelScope.launch {
                                conversationViewModel.setConversationBySenderId(
                                    senderId = senderUID!!,
                                    receiverId = receiverUID!!,
                                    viewModelStoreOwner = viewModelStoreOwner,
                                    message = message)
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(), // Ensure the input box fills the entire width
                        notificationsViewModel = notificationsViewModel,
                        profileViewModel = profileViewModel,
                        chatViewModel = chatViewModel,
                        conversationViewModel = conversationViewModel
                    )
                }
            }
            Spacer(
                modifier = Modifier.padding(vertical = 65.dp))
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    reverseLayout = false // Display messages in reverse chronological order
                ) {
                    items(chatMessagesList, key = { it.timestamp + 1}) { message ->
                        ChatListItem(message = message, receiverUID = receiverUID)
                    }
                }
            }
        }
    )
}
