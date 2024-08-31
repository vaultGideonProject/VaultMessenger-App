package com.vaultmessenger.model

sealed class MessageGroup {
    data class DateHeader(val date: String) : MessageGroup()
    data class MessageItem(val message: Message) : MessageGroup()
}
