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

@Composable
fun ChatScreen(
    navController: NavHostController,
    senderUID: String?,
    receiverUID: String,
    conversationViewModel: ConversationViewModel,
    notificationsViewModel: NotificationsViewModel,
    receiverUserViewModel: ReceiverUserViewModel,
    profileViewModel: ProfileViewModel,
    user: User?,
    receiverUser: ReceiverUser,
    chatViewModel: ChatViewModel,
    listState: LazyListState,
    context: Context,
    errorsViewModel: ErrorsViewModel,
    voiceNoteViewModel: VoiceNoteViewModel,
) {

    // Use remember to avoid recomputing these variables unnecessarily
    val viewModelStoreOwner = LocalViewModelStoreOwner.current

    // Collecting state lazily in a LaunchedEffect
    val messagesReady by chatViewModel.messagesReady.collectAsState(initial = false)
    val receiverReady by receiverUserViewModel.receiverReady.collectAsState(initial = false)
    val userReady by profileViewModel.userReady.collectAsState(initial = false)
    val isMessageValid by chatViewModel.isMessageValid.collectAsState(initial = true)
    val validationMessage by chatViewModel.validationMessage.collectAsState(initial = "")
    val chatMessagesList by chatViewModel.messages.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by errorsViewModel.errorMessage.observeAsState(initial = "")
    val currentErrorMessage by rememberUpdatedState(errorMessage)
    val scope = rememberCoroutineScope()
    // Observing user profile state
    val userList = remember(user) { user }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Logic to check if the user is not at the bottom of the list
    val isScrolledToEnd by remember {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == scrollState.layoutInfo.totalItemsCount - 1
        }
    }

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
    LaunchedEffect(key1 = messagesReady, key2 = receiverReady, key3 = userReady) {
        if (messagesReady && receiverReady && userReady) {
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

    // Use SideEffect to respond to state changes
    SideEffect {
        currentErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            errorsViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
            ChatScrollDownButton({ null })
            Spacer(modifier = Modifier.height(20.dp))
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
                                    receiverUid = receiverUID,
                                    senderUid = senderUID!!, // Assumes senderUID is non-null here
                                    message = message
                                )
                            }
                            conversationViewModel.viewModelScope.launch {
                                conversationViewModel.setConversationBySenderId(
                                    senderId = senderUID!!,
                                    receiverId = receiverUID,
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
                        conversationViewModel = conversationViewModel,
                        navController = navController
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
                        ChatListItem(message = message, receiverUID = receiverUID, receiverUser = receiverUser, profileViewModel, voiceNoteViewModel)
                    }
                }
            }
        }
    )
}
