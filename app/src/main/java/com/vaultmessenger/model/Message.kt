package com.vaultmessenger.model


data class Message(
    val id: Int = 0,
    var conversationId: String? = null,
    val imageUrl: String? = null,
    val voiceNoteURL: String? = null,
    val voiceNoteDuration:String? = null,
    val messageText: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val timestamp: String = "",
    val userId1: String = "",
    val userId2: String = "",
    var loading: Boolean? = true,
    val isTyping: Boolean? = false,
    val messageRead: Boolean? = false
)
