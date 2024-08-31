package com.vaultmessenger.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.interfaces.MessageStorage

class ChatViewModelFactory(
    private val localStorage: MessageStorage,
    private val remoteStorage: MessageStorage
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(localStorage, remoteStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
