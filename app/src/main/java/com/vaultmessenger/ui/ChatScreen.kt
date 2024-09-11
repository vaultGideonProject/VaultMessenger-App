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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.nav.ChatToolbar
import com.vaultmessenger.ui.item.ChatInputBox
import com.vaultmessenger.ui.item.ChatListItem
import com.vaultmessenger.ui.item.ChatScrollDownButton
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ChatViewModelFactory
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProvideViewModels
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.VoiceNoteViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.util.UUID

@Composable
fun ChatScreen(
    navController: NavHostController,
    senderUID: String,
    receiverUID: String,
    conversationViewModel: ConversationViewModel,
    notificationsViewModel: NotificationsViewModel,
    receiverUserViewModel: ReceiverUserViewModel,
    profileViewModel: ProfileViewModel,
    user: User?,
    receiverUser: ReceiverUser,
    listState: LazyListState,
    errorsViewModel: ErrorsViewModel,
    voiceNoteViewModel: VoiceNoteViewModel,
) {
    val context = LocalContext.current
    val (chatViewModel, _, _, _, _, _) = ProvideViewModels(context = context, senderUID = senderUID, receiverUID = receiverUID)

    // Collect state lazily
    val messagesList by chatViewModel.messagesFlow.collectAsState(initial = emptyList())
    val messageReady by chatViewModel.messagesReady.collectAsState(initial = false)

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by errorsViewModel.errorMessage.observeAsState(initial = "")
    val currentErrorMessage by rememberUpdatedState(errorMessage)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = senderUID, key2 = receiverUID) {
      chatViewModel.loadRemoteMessages(senderUID, receiverUID)
    }

    LaunchedEffect(messageReady) {
        chatViewModel.loadMessages(senderUID, receiverUID)
    }

    // Handling side effects for errors
    SideEffect {
        currentErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            errorsViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            receiverUser.let {
                ChatToolbar(
                    navController = navController,
                    receiverUID = it.userName,
                    profilePhoto = it.profilePictureUrl
                )
            }
        },
        bottomBar = {
            ChatInputBox(
                senderUID = user?.userId ?: "",
                receiverUID = receiverUser.userId,
                name = user?.userName ?: "",
                photoUrl = user?.profilePictureUrl ?: "",
                conversationUserIds = mapOf(
                    "userId1" to user?.userId!!,
                    "userId2" to receiverUser.userId
                ),
                conversationUserNames = hashMapOf(
                    "userId1" to user.userName,
                    "userId2" to receiverUser.userName
                ),
                conversationUserProfilePhotos = mapOf(
                    "userId1" to user.profilePictureUrl,
                    "userId2" to receiverUser.profilePictureUrl
                ),onSendMessage = { message, conversation ->
                    chatViewModel.viewModelScope.launch {
                        chatViewModel.sendMessage(receiverUid = receiverUID, senderUid = senderUID, message = message)
                    }
                },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(bottom = 40.dp) // Moves the input box up by 20.dp
                    .fillMaxWidth(),
                notificationsViewModel = notificationsViewModel,
                profileViewModel = profileViewModel,
                chatViewModel = chatViewModel,
                conversationViewModel = conversationViewModel,
                navController = navController
            )
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
                    reverseLayout = false // Display messages in order
                ) {
                    items(messagesList, key = { it.conversationId ?: UUID.randomUUID() }) { localMessage ->
                        ChatListItem(
                            localMessage = localMessage,
                            receiverUID = receiverUID,
                            receiverUser = receiverUser,
                            profileViewModel = profileViewModel,
                            voiceNoteViewModel = voiceNoteViewModel
                        )
                    }

                }
            }
        }
    )
}

