package com.vaultmessenger.viewModel

import NotificationsViewModel
import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.sharedRepository.SharedConversationRepository
import com.vaultmessenger.sharedRepository.SharedUserRepository

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
    val signInViewModel: SignInViewModel,
)

@Composable
fun provideViewModels(
    context: Context,
    receiverUID:String? = "guest",
    senderUID:String,
): ViewModels {
    val errorsViewModel: ErrorsViewModel = viewModel()

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(NotificationRepository(), errorsViewModel = errorsViewModel)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            SharedUserRepository(context = context),
            errorsViewModel=errorsViewModel)
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(SharedConversationRepository(context, errorsViewModel), errorsViewModel)
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
        senderUID = senderUID,
        receiverUID = receiverUID!!,
        conversationViewModel = conversationViewModel
    )
    val chatViewModel: ChatViewModel = viewModel(factory = chatViewModelFactory)

    val signInViewModel: SignInViewModel = viewModel(factory = SignInViewModelFactory())

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
        signInViewModel,
        )
}

