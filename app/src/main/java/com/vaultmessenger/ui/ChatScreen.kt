package com.vaultmessenger.ui

import NotificationsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.nav.ChatToolbar
import com.vaultmessenger.ui.item.ChatInputBox
import com.vaultmessenger.ui.item.ChatListItem
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.VoiceNoteViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.viewModel.provideViewModels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    senderUID: String,
    receiverUID: String,
    conversationViewModel: ConversationViewModel,
    notificationsViewModel: NotificationsViewModel,
    receiverUserViewModel: ReceiverUserViewModel,
    profileViewModel: ProfileViewModel,
    user: LocalUser?,
    receiverUser: ReceiverUser,
    errorsViewModel: ErrorsViewModel,
    voiceNoteViewModel: VoiceNoteViewModel,
) {
    val context = LocalContext.current
    val (chatViewModel, _, _, _, _, _) = provideViewModels(context = context, senderUID = senderUID, receiverUID = receiverUID)

    val viewModelStoreOwner = LocalViewModelStoreOwner.current

    // Collect state lazily
    val messagesList by chatViewModel.messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val messageReady by chatViewModel.messagesReady.collectAsStateWithLifecycle(false)

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by errorsViewModel.errorMessage.observeAsState(initial = "")
    val currentErrorMessage by rememberUpdatedState(errorMessage)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val showScrollDownButton by remember {derivedStateOf {
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 } }

    LaunchedEffect(senderUID, receiverUID) {
        chatViewModel.getMessagesFlow(senderUID, receiverUID)
    }

    // Scroll to bottom when messagesList changes
    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
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
                ),
                onSendMessage = { message, _ ->
                    chatViewModel.viewModelScope.launch {
                        chatViewModel.sendMessage(receiverUid = receiverUID, senderUid = senderUID, message = message)
                    }
                    conversationViewModel.viewModelScope.launch {
                        conversationViewModel.setConversationBySenderId(
                            senderUID,
                            receiverUID,
                            message,
                            viewModelStoreOwner)
                    }
                },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(bottom = 40.dp) // Moves the input box up by 40.dp
                    .fillMaxWidth(),
                notificationsViewModel = notificationsViewModel,
                profileViewModel = profileViewModel,
                chatViewModel = chatViewModel,
                conversationViewModel = conversationViewModel,
                navController = navController
            )
        },
        content = { padding ->
            Box(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {
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
                            voiceNoteViewModel = voiceNoteViewModel,
                            chatViewModel = chatViewModel,
                            senderUid = senderUID
                        )
                    }
                }

                // Scroll down button
                if (!showScrollDownButton) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(messagesList.size - 1) // Scroll to bottom

                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = Color(0xFF435E91) // Set the background color to blue
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Scroll Down",
                            tint = Color.White // Set the icon color to white for contrast
                        )
                    }
                }

            }
        }
    )
}