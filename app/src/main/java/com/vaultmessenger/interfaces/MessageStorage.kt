package com.vaultmessenger.interfaces

import com.vaultmessenger.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageStorage {

    suspend fun sendMessage(senderUid: String, receiverUid: String, message: Message)

    suspend fun getMessagesFlow(senderUid: String, receiverUid: String): Flow<List<Message>>

    suspend fun deleteMessage(senderUid: String, receiverUid: String, messageId: String)
}
