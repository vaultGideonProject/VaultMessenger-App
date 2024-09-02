package com.vaultmessenger.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.modules.ChatRepository

class ChatViewModelFactory(
    private val localStorage: MessageStorage,
    private val remoteStorage: MessageStorage,
    private val chatRepository: ChatRepository,
    private val context: Context, // Change this to the correct type if needed
    private val errorsViewModel: ErrorsViewModel,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                localStorage,
                remoteStorage,
                chatRepository,
                errorsViewModel,
                context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
