package com.vaultmessenger.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(localMessages: List<LocalMessage>): List<Long>

    // Return Flow directly from Room
    @Query("""
    SELECT * FROM message 
    WHERE (userId1 = :senderUID AND userId2 = :receiverUID)
    OR (userId1 = :receiverUID AND userId2 = :senderUID)
    ORDER BY timestamp ASC
    """)
    fun getMessagesForConversation(senderUID: String, receiverUID: String): Flow<List<LocalMessage>>

    @Query("DELETE  FROM message")
    fun deleteAll()

}


