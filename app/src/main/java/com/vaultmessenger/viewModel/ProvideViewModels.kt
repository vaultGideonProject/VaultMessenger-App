package com.vaultmessenger.viewModel

import NotificationsViewModel
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultmessenger.interfaces.LocalMessageStorage
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.interfaces.RemoteMessageStorage
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.modules.ReceiverUserRepository

data class ViewModels(
    val chatViewModel: ChatViewModel,
    val profileViewModel: ProfileViewModel,
    val notificationsViewModel: NotificationsViewModel,
    val conversationViewModel: ConversationViewModel,
    val contactsViewModel: ContactsViewModel,
    val receiverUserViewModel: ReceiverUserViewModel
)

@Composable
fun ProvideViewModels(
    context: Context,
    receiverUID:String? = null
): ViewModels {
    val localStorage: MessageStorage = LocalMessageStorage() // Use actual implementation
    val remoteStorage: MessageStorage = RemoteMessageStorage(ChatRepository()) // Use actual implementation
    val chatRepository: ChatRepository = ChatRepository()
   // val context:Context = LocalContext.current

    val chatViewModelFactory = ChatViewModelFactory(localStorage, remoteStorage, chatRepository, context)
    val chatViewModel: ChatViewModel = viewModel(factory = chatViewModelFactory)

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(NotificationRepository())
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(FirebaseUserRepository())
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(ConversationRepository())
    )
    val contactsViewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(ContactRepository())
    )
    val receiverUserRepository = ReceiverUserRepository(receiverUID)
    val receiverViewModelFactory = ReceiverUserViewModelFactory(receiverUserRepository)
    val receiverUserViewModel = viewModel<ReceiverUserViewModel>(factory = receiverViewModelFactory)

    return ViewModels(
        chatViewModel,
        profileViewModel,
        notificationsViewModel,
        conversationViewModel,
        contactsViewModel,
        receiverUserViewModel
        )
}

