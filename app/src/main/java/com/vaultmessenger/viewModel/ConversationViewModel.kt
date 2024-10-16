package com.vaultmessenger.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.database.LocalConversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.sharedRepository.SharedConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ConversationViewModel(
    context: Context,
    conversationRepository: ConversationRepository,
    private val errorsViewModel: ErrorsViewModel,
) : ViewModel() {
    private val _conversations = MutableStateFlow<List<LocalConversation>>(emptyList())
    val conversations: StateFlow<List<LocalConversation>> get() = _conversations
    private val conversationRepository: SharedConversationRepository = SharedConversationRepository(
        context,
        this,
        conversationRepository,
        errorsViewModel
    )

    init {
        subscribeToConversations()
        loadConversations()
    }

    // Function to subscribe to real-time conversations from local object
    private fun subscribeToConversations() {
        viewModelScope.launch {
            val userId = FirebaseService.auth.currentUser?.uid ?: "guest"
            conversationRepository.getConversationsFlow(userId)
                .catch { error ->
                    errorsViewModel.setError(error.message ?: "Error loading conversations")
                }
                .collect { conversations ->
                    _conversations.value = conversations
                }
        }
    }
    //now lets fill bucket of local to best handle flow
   private fun loadConversations(){
       viewModelScope.launch {
           val userId = FirebaseService.auth.currentUser?.uid ?: "guest"
           conversationRepository.loadConversations()
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
