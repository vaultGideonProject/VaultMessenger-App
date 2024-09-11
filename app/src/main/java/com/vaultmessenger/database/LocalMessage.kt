package com.vaultmessenger.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class LocalMessage(
    @PrimaryKey(autoGenerate = false)var conversationId: String,
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "imageUrl")  val imageUrl: String? = null,
    @ColumnInfo(name = "voiceNoteURL") val voiceNoteURL: String? = null,
    @ColumnInfo(name = "voiceNoteDuration") val voiceNoteDuration:String? = null,
    @ColumnInfo(name = "messageText") val messageText: String = "",
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "photoUrl") val photoUrl: String = "",
    @ColumnInfo(name = "timestamp") val timestamp: String = "",
    @ColumnInfo(name = "userId1") val userId1: String = "",
    @ColumnInfo(name = "userId2") val userId2: String = "",
    @ColumnInfo(name = "loading") var loading: Boolean? = true,
    @ColumnInfo(name = "isTyping") val isTyping: Boolean? = false
)
