package com.vaultmessenger.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.VoiceRecorder
import com.vaultmessenger.shared_repository.SharedMessageRepository
import com.vaultmessenger.viewModel.ConversationViewModel.Companion.MAX_MESSAGE_LENGTH
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.File

class ChatViewModel(
    private val errorsViewModel: ErrorsViewModel,
    context: Context,
    conversationViewModel: ConversationViewModel,
    senderUid: String,
    receiverUid: String,
) : ViewModel() {


    // StateFlow to expose messages to the UI
    private val _messagesFlow = MutableStateFlow<List<LocalMessage>>(emptyList())
    val messagesFlow: StateFlow<List<LocalMessage>> = _messagesFlow

    // State to track loading and errors
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val sharedMessageRepository: SharedMessageRepository =
        SharedMessageRepository(context = context, errorsViewModel,
        this, senderUid, receiverUid)

    private val _isMessageValid = MutableStateFlow(true)
    val isMessageValid: StateFlow<Boolean> = _isMessageValid.asStateFlow()

    private val _validationMessage = MutableStateFlow("")
    val validationMessage: StateFlow<String> = _validationMessage.asStateFlow()

    private val _getConversationId = MutableStateFlow<List<String?>>(emptyList())
    val getConversationId: StateFlow<List<String?>> = _getConversationId.asStateFlow()

    private var voiceRecorder: VoiceRecorder? = null
    private var audioFile: File? = null


    private var conversationId: String? = null

    init {
        voiceRecorder = VoiceRecorder(context)
    }

    fun updateMessage(text: String) {
      //  _messageText.value = text
        validateMessage(text)
    }

    private fun validateMessage(text: String) {
        when {
            text.isBlank() -> {
                _isMessageValid.value = false
                _validationMessage.value = "Message cannot be empty"
                errorsViewModel.setError("Message cannot be empty")
            }
            text.length > MAX_MESSAGE_LENGTH -> {
                _isMessageValid.value = false
                _validationMessage.value = "Message exceeds character limit"
                errorsViewModel.setError("Message exceeds character limit")
            }
            else -> {
                _isMessageValid.value = true
                _validationMessage.value = ""
            }
        }
    }

    private val _messagesReady = MutableStateFlow(false)
    val messagesReady: StateFlow<Boolean> = _messagesReady

    // Function to send a message
    fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            if (message.messageText.isBlank() || message.messageText.length > MAX_MESSAGE_LENGTH) {
                // Handle error for invalid message text
                return@launch
            }
            try {
                sharedMessageRepository.sendMessage(
                    senderUID = senderUid,
                    receiverUID = receiverUid,
                    message = message)

            } catch (e: Exception) {
                // Handle error
                errorsViewModel.setError(e.message ?: "An error occurred")
            } finally {
            }
        }
    }
    fun sendImageMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            if (message.imageUrl?.isBlank() == true || message.messageText.length > MAX_MESSAGE_LENGTH) {
                // Handle error for invalid message text
                return@launch
            }
            try {
                sharedMessageRepository.sendMessage(
                    senderUID = senderUid,
                    receiverUID = receiverUid,
                    message = message)

            } catch (e: Exception) {
                // Handle error
                errorsViewModel.setError(e.message ?: "An error occurred")
            } finally {
            }
        }
    }
    fun startVoiceRecording(context: Context) {
        viewModelScope.launch {
            // Define your file path for audio recording
            audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp4")
            audioFile?.let {
                try{
                    voiceRecorder?.start(it)
                }catch (e:Exception){
                    errorsViewModel.setError(e.message ?: "an error occurred")
                }
            }
        }
    }

    fun stopVoiceRecording(senderUid: String, receiverUid: String) {
        viewModelScope.launch {
            voiceRecorder?.stop(
                onSuccess = { voiceNoteUrl ->
                    Log.d("stopVoiceRecording", "VoiceNote URL: $voiceNoteUrl")
                    if (voiceNoteUrl.isNotEmpty()) {
                        // Create the voice message object
                        val voiceMessage = Message(
                            imageUrl = null,
                            voiceNoteURL = voiceNoteUrl,
                            voiceNoteDuration = null,  // Assuming no duration value here
                            messageText = "",
                            name = "",
                            photoUrl = "",
                            timestamp = System.currentTimeMillis().toString(),
                            userId1 = senderUid,
                            userId2 = receiverUid,
                            loading = false,
                            isTyping = false
                        )

                        Log.d("stopVoiceRecording", "VoiceMessage created: $voiceMessage")

                        // Send the voice message
                        sendVoiceMessage(senderUid, receiverUid, voiceMessage)

                    } else {
                        Log.e("stopVoiceRecording", "VoiceNote URL is empty")
                        errorsViewModel.setError( "An error occurred")
                    }
                },
                onFailure = { e ->
                    Log.e("stopVoiceRecording", "Failed to stop and upload voice recording: ${e.message}")
                    errorsViewModel.setError(e.message ?: "An error occurred")
                }
            )
        }
    }

    private fun sendVoiceMessage(senderUid: String, receiverUid: String, message: Message) {
        viewModelScope.launch {
            try {
                message.voiceNoteURL?.let { voiceNoteUrl ->
                    if (voiceNoteUrl.isNotEmpty()) {
                        Log.d("sendVoiceMessage", "Sending voice message with URL: $voiceNoteUrl")
                        sharedMessageRepository.sendMessage(
                            senderUID = senderUid,
                            receiverUID = receiverUid,
                            message = message
                        )

                        Log.d("sendVoiceMessage", "Voice Message sent to DB")
                    } else {
                        Log.e("sendVoiceMessage", "VoiceNote URL is empty")
                        errorsViewModel.setError("An error occurred")
                    }
                } ?: run {
                    Log.e("sendVoiceMessage", "Message's voiceNoteURL is null")
                    errorsViewModel.setError("An error occurred")
                }
            } catch (e: Exception) {
                Log.e("sendVoiceMessage", "Voice Message error: $e")
                errorsViewModel.setError(e.message ?: "An error occurred")
            } finally {
            }
        }
    }

    // Load messages from Room
  suspend fun loadMessages(senderUid: String, receiverUid: String){
        viewModelScope.launch {
            sharedMessageRepository.getLocalMessages(senderUid, receiverUid).collect{
                collectMessages ->
                _messagesFlow.value = collectMessages
            }
        }
    }

    suspend fun loadRemoteMessages(senderUid: String, receiverUid: String){
        sharedMessageRepository.loadMessages(senderUid, receiverUid)
    }
}
