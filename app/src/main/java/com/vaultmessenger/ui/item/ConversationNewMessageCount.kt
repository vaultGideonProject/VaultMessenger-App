package com.vaultmessenger.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ConversationNewMessageCount(messageCount: Int){
    if(messageCount == 0 ){
        return
    }
    Box(
        contentAlignment = Alignment.Center, // Centers the content inside the Box
        modifier = Modifier
            .background(Color(0xFF435E91), shape = CircleShape) // Background color and circle shape
            .size(30.dp) // Equal width and height for a perfect circle
            .clip(CircleShape) // Clip the Box to a circle
    ) {
        Text(
            text = messageCount.toString(),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall, // Ensure the text fits the circle
            modifier = Modifier.padding(4.dp) // Padding to center the text inside the circle
        )
    }
}