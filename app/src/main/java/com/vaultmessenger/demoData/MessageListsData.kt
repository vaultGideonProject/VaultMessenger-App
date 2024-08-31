package com.vaultmessenger.demoData

import com.vaultmessenger.R
import com.vaultmessenger.model.Conversation

class MessageListsData {
    val imageIds = listOf(
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground
    )

    val names = listOf(
        "John",
        "Emily",
        "Michael",
        "Sarah",
        "David",
        "Jessica"
    )

    val messageContent = listOf(
        "Hey, have you tried the new restaurant downtown? They have amazing vegan options!",
        "I can't believe it's already July! Time flies so fast.",
        "Did you catch the game last night? It was intense!",
        "I'm planning a road trip next weekend. Any recommendations for must-see places?",
        "Just finished reading a great book. I'll lend it to you next time we meet.",
        "What do you think about the latest movie release? I've heard mixed reviews."
    )
   // fun getMessageList(): Conversation{
      //  return Conversation(imageIds, names, messageContent)
   //     return null
   // }
}