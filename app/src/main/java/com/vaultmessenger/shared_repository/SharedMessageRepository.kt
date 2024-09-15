package com.vaultmessenger.shared_repository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.MyApp
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.local.LocalMessageRepository
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SharedMessageRepository(
    context: Context,
    private val errorsViewModel: ErrorsViewModel,
    private val chatViewModel: ChatViewModel,
    private val senderUID: String,
    private val receiverUID: String
) {

    private val chat = chatViewModel
    private val conversationRepository: ConversationRepository = ConversationRepository(errorsViewModel)
    private val chatRepository: ChatRepository = ChatRepository()

   private val MyApp = MyApp(context).repositoryMessages

    init {

    }

    // Function to send a message
    suspend fun sendMessage(
        senderUID: String,
        receiverUID: String,
        message: Message
    ) {

        val localMessage = message.toLocalMessage()
        // Insert into local database
       MyApp.insertMessages(listOf(localMessage))
       try{
           // Send message to remote repository
           chatRepository.sendMessage(
               senderUid = senderUID,
               receiverUid = receiverUID,
               message = message
           )
       }catch (e:Exception){
           errorsViewModel.setError("message not sent:${e.message}")
       }
        Log.d("sharedMessageSend", "Message Sent")
    }

    // Load messages from Room
    fun getLocalMessages(senderUID: String, receiverUID: String): Flow<List<LocalMessage>> {
        return MyApp.getMessagesForConversation(senderUID, receiverUID)
    }

    // load messages into ROOm
    suspend fun loadMessages(senderUID: String, receiverUID: String, delayMillis: Long = 60000L) {
        while (isActive) {  // Ensure the coroutine continues running only if it's active
            try {
                // Collect messages from chatRepository
                chatRepository.getMessagesFlow(senderUID, receiverUID).collect { messages ->
                    // Convert List<Message> to List<LocalMessage>
                    val localMessages = messages.map { it.toLocalMessage() }

                    // Insert List<LocalMessage> into the database
                    MyApp.insertMessages(localMessages)
                }
            } catch (e: Exception) {
                // Handle any exceptions during the message loading or insertion
                 errorsViewModel.setError("Error loading messages for $senderUID and $receiverUID, $e")
            }

            // Wait before fetching messages again
            delay(delayMillis)
        }
    }




    // Extension function to convert remote Message to LocalMessage
    private fun Message.toLocalMessage(): LocalMessage {
        return LocalMessage(
            id = this.id,
            conversationId = this.conversationId ?: "",
            imageUrl = this.imageUrl,
            voiceNoteURL = this.voiceNoteURL,
            voiceNoteDuration = this.voiceNoteDuration,
            messageText = this.messageText,
            name = this.name,
            photoUrl = this.photoUrl,
            timestamp = this.timestamp,
            userId1 = this.userId1,
            userId2 = this.userId2,
            loading = this.loading ?: true,
            isTyping = this.isTyping ?: false
        )
    }
}
