package com.vaultmessenger.viewModel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultmessenger.interfaces.LocalMessageStorage
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.interfaces.RemoteMessageStorage
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.FirebaseUserRepository

@Composable
fun ProvideViewModels(
    context: Context
): Pair<ChatViewModel, ProfileViewModel> {
 val localStorage: MessageStorage = LocalMessageStorage() // Use actual implementation
    val remoteStorage: MessageStorage = RemoteMessageStorage(ChatRepository()) // Use actual implementation

    val chatViewModelFactory = ChatViewModelFactory(localStorage, remoteStorage)
    val chatViewModel: ChatViewModel = viewModel(factory = chatViewModelFactory)

    val userRepository = FirebaseUserRepository()
    val userViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userRepository))

    return Pair(chatViewModel, userViewModel)
}
