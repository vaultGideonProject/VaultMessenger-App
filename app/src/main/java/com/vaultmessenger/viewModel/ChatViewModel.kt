package com.vaultmessenger.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.interfaces.LocalMessageStorage
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.model.Message
import com.vaultmessenger.viewModel.ConversationViewModel.Companion.MAX_MESSAGE_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val localStorage: MessageStorage,
    private val remoteStorage: MessageStorage
) : ViewModel() {

    private val _isMessageValid = MutableStateFlow(true)
    val isMessageValid: StateFlow<Boolean> = _isMessageValid.asStateFlow()

    private val _validationMessage = MutableStateFlow("")
    val validationMessage: StateFlow<String> = _validationMessage.asStateFlow()

    fun updateMessage(text: String) {
      //  _messageText.value = text
        validateMessage(text)
    }

    private fun validateMessage(text: String) {
        when {
            text.isBlank() -> {
                _isMessageValid.value = false
                _validationMessage.value = "Message cannot be empty"
            }
            text.length > MAX_MESSAGE_LENGTH -> {
                _isMessageValid.value = false
                _validationMessage.value = "Message exceeds character limit"
            }
            else -> {
                _isMessageValid.value = true
                _validationMessage.value = ""
            }
        }
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messagesReady = MutableStateFlow(false)
    val messagesReady: StateFlow<Boolean> = _messagesReady

    // Function to send a message
    fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            if (message.messageText.isBlank() || message.messageText.length > MAX_MESSAGE_LENGTH) {
                // Handle error for invalid message text
                return@launch
            }
            _isLoading.value = true
            try {
                localStorage.sendMessage(senderUid, receiverUid, message)
                remoteStorage.sendMessage(senderUid, receiverUid, message)
               // getMessages(senderUid, receiverUid)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun sendImageMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            if (message.imageUrl?.isBlank() == true || message.messageText.length > MAX_MESSAGE_LENGTH) {
                // Handle error for invalid message text
                return@launch
            }
            _isLoading.value = true
            try {
                localStorage.sendMessage(senderUid, receiverUid, message)
                remoteStorage.sendMessage(senderUid, receiverUid, message)
                // getMessages(senderUid, receiverUid)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun sendVoiceMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            if (message.voiceNoteURL?.isBlank() == true || message.messageText.length > MAX_MESSAGE_LENGTH) {
                // Handle error for invalid message text
                return@launch
            }
            _isLoading.value = true
            try {
                localStorage.sendMessage(senderUid, receiverUid, message)
                remoteStorage.sendMessage(senderUid, receiverUid, message)
                // getMessages(senderUid, receiverUid)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to retrieve messages
    fun getMessages(senderUid: String, receiverUid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _messagesReady.value = false

            try {
                localStorage.getMessagesFlow(senderUid, receiverUid)
                    .collectLatest { localMessages ->
                        if (localMessages.isNotEmpty()) {
                            _messages.value = localMessages
                            _messagesReady.value = true
                        } else {
                            // Fallback to remote storage if local storage is empty
                            remoteStorage.getMessagesFlow(senderUid, receiverUid)
                                .collectLatest { remoteMessages ->
                                    _messages.value = remoteMessages
                                    _messagesReady.value = true
                                    // Cache the remote messages locally
                                    remoteMessages.forEach { message ->
                                        localStorage.sendMessage(senderUid, receiverUid, message)
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to delete a specific message
    fun deleteMessage(senderUid: String, receiverUid: String, messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                localStorage.deleteMessage(senderUid, receiverUid, messageId)
                remoteStorage.deleteMessage(senderUid, receiverUid, messageId)
                getMessages(senderUid, receiverUid)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
