package com.vaultmessenger.local

import androidx.annotation.WorkerThread
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.database.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalMessageRepository(private val messageDao: MessageDao) {

    // Insert a local message into the database
    @WorkerThread
    suspend fun insertMessages(localMessage: List<LocalMessage>) {
        messageDao.insertMessages(localMessage)
    }

    // Fetch messages using Flow directly from Room
    fun getMessagesForConversation(senderUID: String, receiverUID: String): Flow<List<LocalMessage>> {
        return messageDao.getMessagesForConversation(senderUID, receiverUID)
    }
}

