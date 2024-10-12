package com.vaultmessenger.sharedRepository

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import com.vaultmessenger.MyApp
import com.vaultmessenger.database.LocalConversation
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.viewModel.ErrorsViewModel
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class SharedConversationRepository(context: Context, errorsViewModel: ErrorsViewModel) {
    private val myApp = MyApp(context).repositoryConversation
    private val remoteRepository = ConversationRepository(errorsViewModel, context)

    fun getConversationsFlow(userId:String): Flow<List<LocalConversation>> {
        return myApp.getMessagesForConversation(userId)
    }

    suspend fun loadConversations(userId: String, delayMillis: Long = 15000L) {
        while (isActive){
            try {
                remoteRepository.getConversationsFlow(userId).collect { conversations ->
                    // Map each Conversation to LocalConversation
                    val localConversations = conversations.map { it.toLocalConversation() }

                    // Insert the converted LocalConversations into the local database
                    myApp.insertConversations(localConversations)
                }
            }catch (e:Exception){

            }
            delay(delayMillis)
        }
    }

    suspend fun setConversationBySenderId(
        senderId:String,
        receiverId:String,
        message: Message,
        viewModelStoreOwner:ViewModelStoreOwner?,
    ){
        remoteRepository.setConversationBySenderId(
            senderId,
            receiverId,
            message,
            viewModelStoreOwner
        )
    }

    suspend fun setConversationByReceiverId(
        senderId:String,
        receiverId:String,
        message: Message,
        viewModelStoreOwner:ViewModelStoreOwner?,
    ){
        remoteRepository.setConversationByReceiverId(
            senderId,
            receiverId,
            message,
            viewModelStoreOwner
        )
    }
    private fun Conversation.toLocalConversation(): LocalConversation {
        return LocalConversation(
            conversationId = this.conversationId ?: "", // Fallback to empty string if null
            lastMessage = this.lastMessage ?: "", // Use messageText as lastMessage
            timestamp = this.timestamp?.toString() ?: System.currentTimeMillis().toString(), // Convert timestamp to String, fallback to current time if null
            userIds = HashMap(this.userIds), // Convert Map to HashMap
            userNames = HashMap(this.userNames), // Convert Map to HashMap
            userPhotos = HashMap(this.userPhotos), // Convert Map to HashMap
            isTyping = HashMap(this.isTyping) // Convert Map to HashMap
        )
    }
}