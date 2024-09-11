package com.vaultmessenger.viewModel

import NotificationsViewModel
import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.shared_repository.SharedMessageRepository

data class ViewModels(
    val chatViewModel: ChatViewModel,
    val profileViewModel: ProfileViewModel,
    val notificationsViewModel: NotificationsViewModel,
    val conversationViewModel: ConversationViewModel,
    val contactsViewModel: ContactsViewModel,
    val receiverUserViewModel: ReceiverUserViewModel,
    val connectivityViewModel: ConnectivityViewModel,
    val errorsViewModel: ErrorsViewModel,
    val voiceNoteViewModel: VoiceNoteViewModel,
)

@Composable
fun ProvideViewModels(
    context: Context,
    receiverUID:String? = "guest",
    senderUID:String,
): ViewModels {
    val errorsViewModel: ErrorsViewModel = viewModel()

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(NotificationRepository(), errorsViewModel = errorsViewModel)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(FirebaseUserRepository(), errorsViewModel)
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(ConversationRepository(errorsViewModel), errorsViewModel)
    )
    val contactsViewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(ContactRepository(errorsViewModel))
    )
    val connectivityViewModel: ConnectivityViewModel = viewModel()

    val receiverUserRepository = ReceiverUserRepository(receiverUID)
    val receiverViewModelFactory = ReceiverUserViewModelFactory(receiverUserRepository, errorsViewModel = errorsViewModel)
    val receiverUserViewModel = viewModel<ReceiverUserViewModel>(factory = receiverViewModelFactory)

    val voiceNoteViewModel: VoiceNoteViewModel = viewModel(
    factory = VoiceNoteViewModelFactory(LocalContext.current.applicationContext as Application, errorsViewModel)
    )

    val chatViewModelFactory = ChatViewModelFactory(
        context,
        errorsViewModel,
        conversationViewModel,
        senderUID = senderUID,
        receiverUID = receiverUID!!,
    )
    val chatViewModel: ChatViewModel = viewModel(factory = chatViewModelFactory)

    return ViewModels(
        chatViewModel,
        profileViewModel,
        notificationsViewModel,
        conversationViewModel,
        contactsViewModel,
        receiverUserViewModel,
        connectivityViewModel,
        errorsViewModel,
        voiceNoteViewModel,
        )
}

