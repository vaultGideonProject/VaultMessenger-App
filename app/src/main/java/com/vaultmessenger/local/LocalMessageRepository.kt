package com.vaultmessenger.local

import androidx.annotation.WorkerThread
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.database.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry

class LocalMessageRepository(private val messageDao: MessageDao) {
    // Fetch messages using Flow directly from Room
   fun getMessagesForConversation(senderUID: String, receiverUID: String): Flow<List<LocalMessage>> {
        return messageDao.getMessagesForConversation(senderUID, receiverUID)

    }
    suspend fun updateMessageReadStatus(conversationId: String, messageRead:Boolean){
        return messageDao.updateMessageReadStatus(conversationId = conversationId, messageRead = messageRead)
    }
    suspend fun deleteAllMessages(){
        return messageDao.deleteAllMessages()
    }
}