package com.vaultmessenger.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "conversation")
@TypeConverters(Converters::class)
data class LocalConversation(
    @PrimaryKey(autoGenerate = false)
    var conversationId: String = "",
    var lastMessage: String = "",
    var timestamp: String = "",
    var userIds: HashMap<String, String> = HashMap(),
    var userNames: HashMap<String, String> = HashMap(),
    var userPhotos: HashMap<String, String> = HashMap(),
    var isTyping: HashMap<String, Boolean> = HashMap()
)

