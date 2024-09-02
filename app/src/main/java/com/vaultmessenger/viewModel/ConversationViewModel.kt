package com.vaultmessenger.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationRepository: ConversationRepository,
    private val errorsViewModel: ErrorsViewModel,
) : ViewModel() {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> get() = _conversations

    init {
        subscribeToConversations()
    }

    // Function to subscribe to real-time conversations
    private fun subscribeToConversations() {
        // Access FirebaseAuth
        val auth = FirebaseService.auth

        // Access Firestore
        val firestore = FirebaseService.firestore

        // Access FirebaseStorage
        val storage = FirebaseService.storage

        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            // Subscribe to real-time updates
            try {
                conversationRepository.getConversationsFlow(userId).collect { conversations ->
                    _conversations.value = conversations
                }
            }catch (e:Exception){
                errorsViewModel.setError(e.message ?: "An error occurred")
                return@launch
            }
        }
    }

    fun setConversationBySenderId(
        senderId: String,
        receiverId: String,
        message: Message,
        viewModelStoreOwner: ViewModelStoreOwner?
    ) {
        viewModelScope.launch {
            val isValid = message.messageText.isNotBlank() || message.imageUrl?.isNotBlank() == true
                    && message.messageText.length > MAX_MESSAGE_LENGTH
            if (!isValid) {
                // Handle error for invalid message text
                return@launch
            }
            try {

                conversationRepository.setConversationBySenderId(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = message,
                    viewModelStoreOwner = viewModelStoreOwner
                )
                conversationRepository.setConversationByReceiverId(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = message,
                    viewModelStoreOwner = viewModelStoreOwner
                )
            } catch (e: Exception) {
                // Handle error
                errorsViewModel.setError(e.message ?: "An error occurred")
                return@launch
            }
        }
    }

    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }
}
