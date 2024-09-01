package com.vaultmessenger.ui.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultmessenger.model.Message

@Composable
fun ChatTextMessage(
    message: Message
){
    val messageTimeStamp = getFormattedDate(message.timestamp)

    Column(
        modifier = Modifier.padding(2.dp) // Padding around the entire message block
    ) {
        // Display message text if it's not blank
        if (message.messageText.isNotBlank() || message.messageText != "") {
            Text(text = message.messageText, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp)) // Space between text and loading indicator or image
        }
        // Display timestamp at the bottom
        Text(
            text = messageTimeStamp,
            color = Color.White,
            style = TextStyle(fontSize = 12.sp) // Adjust timestamp font size if needed
        )
    }
}