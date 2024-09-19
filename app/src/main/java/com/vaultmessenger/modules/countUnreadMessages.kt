package com.vaultmessenger.modules

import android.util.Log
import com.vaultmessenger.database.LocalMessage

fun countUnreadMessages(messages: List<LocalMessage>): Int {
    // Group messages by conversationId
    val groupedMessages = messages.groupBy { it.conversationId }

    Log.d("countUnreadMessages", groupedMessages.toString())

    // Count conversations with at least one unread message
    return groupedMessages.values.count { conversationMessages ->
        conversationMessages.any { it.messageRead == false }
    }
}

