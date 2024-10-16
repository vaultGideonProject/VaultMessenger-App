package com.vaultmessenger.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface MessageDaoInsert {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(localMessages: List<LocalMessage>): List<Long>
}
