package com.vaultmessenger.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Conversation(
    var conversationId: String = "",
    var lastMessage: String = "",
    var timestamp: String = "",
    var userIds: Map<String, String> = emptyMap(),
    var userNames: HashMap<String, String> = HashMap(),
    var userPhotos: Map<String, String> = emptyMap(),
    var isTyping: Map<String, Boolean> = emptyMap()
)