package com.vaultmessenger.interfaces

import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ChatRepository

import kotlinx.coroutines.flow.Flow

class RemoteMessageStorage(private val chatRepository: ChatRepository) : MessageStorage {
    override suspend fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        chatRepository.sendMessage(senderUid, receiverUid, message)
    }

    override suspend fun getMessagesFlow(senderUid: String, receiverUid: String): Flow<List<Message>> {
       return chatRepository.getMessagesFlow(senderUid, receiverUid)
    }

    override suspend fun deleteMessage(senderUid: String, receiverUid: String, messageId: String) {
        chatRepository.deleteMessage(senderUid, receiverUid, messageId)
    }
}

