package com.vaultmessenger.sharedRepository

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.MyApp
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SharedMessageRepository(
    context: Context,
    private val errorsViewModel: ErrorsViewModel,
    private val chatViewModel: ChatViewModel,
    private val chatRepository: ChatRepository,
    private val senderUID: String,
    private val receiverUID: String,
) {

    private val repositoryMessages = MyApp(context).repositoryMessages
    private val repositoryInsertMessages = MyApp(context).repositoryInsertMessages
    private val listenForMessageChanges = chatRepository.listenForMessageChanges(senderUID, receiverUID)

    // Function to send a message
    suspend fun sendMessage(
        senderUID: String,
        receiverUID: String,
        message: Message
    ) {

        val localMessage = message.toLocalMessage()
        // Insert into local database
        repositoryInsertMessages.insertMessages(listOf(localMessage))
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
   private suspend fun getLocalMessages(senderUID: String, receiverUID: String): Flow< List<LocalMessage>> {
        return repositoryMessages.getMessagesForConversation(senderUID, receiverUID)
    }

    // Assuming MyAppInsert is your DAO
    private suspend fun insertMessagesInDatabase(localMessages: List<LocalMessage>) {
        withContext(Dispatchers.IO) {
            repositoryInsertMessages.insertMessages(localMessages)
        }
    }


    // load messages into ROOm
    private suspend fun remoteMessages(senderUID: String, receiverUID: String, delayMillis: Long = 2000L) {
             // Ensure the coroutine continues running only if it's active
            try {
                // Collect messages from chatRepository
                chatRepository.getMessagesFlow(senderUID, receiverUID).collect { messages ->
                    // Convert List<Message> to List<LocalMessage>
                    val localMessages = messages.map { it.toLocalMessage() }

                    // Check if the list is not empty
                    if (localMessages.isNotEmpty()) {
                        // Insert List<LocalMessage> into the database
                        insertMessagesInDatabase(localMessages)
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions during the message loading or insertion
                errorsViewModel.setError("Error loading messages for $senderUID and $receiverUID: ${e.message}")
            }
    }

    suspend fun getMessages(senderUID: String, receiverUID: String): Flow<List<LocalMessage>> {
        // Get local messages from Room
        val localMessages: Flow<List<LocalMessage>> = getLocalMessages(senderUID, receiverUID)
        // Launch remote message fetching in ViewModel scope
        chatViewModel.viewModelScope.launch {
            remoteMessages(senderUID, receiverUID)

            withContext(Dispatchers.IO){
                listenForMessageChanges.collectLatest {
                        messageChangedFlow ->
                    if(messageChangedFlow){
                        remoteMessages(senderUID, receiverUID)
                        chatRepository._messageChangedFlow.value = false
                    }
                }
            }
        }
        return localMessages
    }

    suspend fun updateMessageReadStatus(senderUid: String, receiverUid: String, messageId: String, messageRead: Boolean) {
        try {
            //Lets update or insert remote if its not there
            // Reference to the Firestore collection for the messages
            val messagesCollection = FirebaseService.firestore.collection("messages")
                .document(senderUid)
                .collection(receiverUid)

            // Query the collection for the document where the "conversationId" matches the provided messageId
            val querySnapshot = messagesCollection
                .whereEqualTo("conversationId", messageId)
                .get()
                .await()  // await to suspend the function until the result is available

            if (!querySnapshot.isEmpty) {
                // If a document is found, update the "isRead" field
                val document = querySnapshot.documents[0] // Assuming only one document matches
                document.reference.update("messageRead", messageRead)
                    .await()  // await to suspend the function until the update is complete
                Log.d("MessageRead", "Message marked as read: $messageId")
            } else {
                // No document found with the matching "conversationId"
                Log.d("MessageRead", "Message not found with conversationId: $messageId")
            }

            withContext(Dispatchers.IO) {
                //then close by updating local
                repositoryMessages.updateMessageReadStatus(conversationId = messageId, messageRead = messageRead)
            }

        } catch (e: Exception) {
            // Handle exceptions
            Log.e("MessageRead", "Error updating message status", e)
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