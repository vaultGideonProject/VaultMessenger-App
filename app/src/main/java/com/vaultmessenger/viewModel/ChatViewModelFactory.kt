package com.vaultmessenger.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.modules.ChatRepository

class ChatViewModelFactory(
    private val context: Context, // Change this to the correct type if needed
    private val errorsViewModel: ErrorsViewModel,
    private val senderUID:String,
    private val receiverUID:String,
    private val  conversationViewModel: ConversationViewModel,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                errorsViewModel,
                context,
                conversationViewModel,
                chatRepository,
                senderUID,
                receiverUID
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
