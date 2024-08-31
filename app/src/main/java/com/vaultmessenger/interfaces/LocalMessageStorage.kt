package com.vaultmessenger.interfaces

import com.vaultmessenger.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LocalMessageStorage : MessageStorage {

    override suspend fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        // Save the message to local storage
    }

    override suspend fun getMessagesFlow(senderUid: String, receiverUid: String): Flow<List<Message>> {
        // Retrieve messages from local storage
        return flowOf(listOf()) // Replace with actual retrieval logic
    }

    override suspend fun deleteMessage(senderUid: String, receiverUid: String, messageId: String) {
        // Delete the message from local storage
    }
}
