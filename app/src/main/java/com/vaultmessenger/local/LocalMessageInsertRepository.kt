package com.vaultmessenger.local

import com.vaultmessenger.database.MessageDaoInsert
import com.vaultmessenger.database.LocalMessage

class LocalMessageInsertRepository(private val messageDaoInsert: MessageDaoInsert) {

    // This function inserts a list of messages into the database
    suspend fun insertMessages(localMessages: List<LocalMessage>): List<Long> {
        return messageDaoInsert.insertMessages(localMessages)  // Pass the list to DAO
    }

    // If you want to insert a single message, wrap it in a list
    suspend fun insertSingleMessage(localMessage: LocalMessage): List<Long> {
        return messageDaoInsert.insertMessages(listOf(localMessage))
    }
}
