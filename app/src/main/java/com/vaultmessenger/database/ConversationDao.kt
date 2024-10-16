package com.vaultmessenger.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(localConversations: List<LocalConversation>): List<Long>

    @Query("""
        SELECT * FROM conversation 
        WHERE userIds LIKE '%' || :userId || '%'
        ORDER BY timestamp ASC
    """)
    fun getMessagesForConversation(userId: String): Flow<List<LocalConversation>>

    @Query("DELETE FROM conversation") // Correct table name
    suspend fun deleteAllConversations()

    @Query("UPDATE conversation SET lastMessage = :isRead WHERE conversationId = :conversationId")
    suspend fun updateMessageReadStatus(conversationId: String, isRead: Boolean)
}
