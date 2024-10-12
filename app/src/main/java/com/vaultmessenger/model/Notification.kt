package com.vaultmessenger.model

data class Notification(
    var notificationId: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var messageId: String = "",
    var messageText: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var status: String = "pending" // can be "delivered", "read"
)
