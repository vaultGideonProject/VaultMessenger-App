package com.vaultmessenger.local

import com.vaultmessenger.database.ConversationDao
import com.vaultmessenger.database.LocalConversation as RoomLocalConversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalConversationRepository(private val conversationDao: ConversationDao) {

    // Function to insert a list of conversations
    suspend fun insertConversations(conversations: List<RoomLocalConversation>): List<Long> {
        try {
            return conversationDao.insertConversations(conversations)
        }catch (e:Exception){
            return listOf()
        }
    }

    // Function to get messages for a specific conversation
    fun getMessagesForConversation(userId:String): Flow<List<RoomLocalConversation>> {
        try {
            return conversationDao.getMessagesForConversation(userId)
        }catch (e:Exception){
            return flow {

            }
        }
    }

    // Function to delete all conversations
    suspend fun deleteAllConversations() {
        conversationDao.deleteAll()
    }

    // Function to update the read status of a message
    suspend fun updateMessageReadStatus(conversationId: String, isRead: Boolean) {
        conversationDao.updateMessageReadStatus(conversationId, isRead)
    }
}
