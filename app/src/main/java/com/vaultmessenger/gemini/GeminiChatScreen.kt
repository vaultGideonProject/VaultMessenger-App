package com.vaultmessenger.gemini

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GeminiChatScreen(geminiViewModel: GeminiViewModel) {
    val messages by geminiViewModel.messages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize() // Ensure the column takes up the full screen
            .padding(16.dp) // Add padding around the content
    ) {
        // LazyColumn to display messages, using weight to fill available space
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Take up the remaining height
                .fillMaxWidth() // Make sure it stretches to the full width
        ) {
            items(messages) { message ->
                Text(
                    text = message,
                    modifier = Modifier
                        .padding(vertical = 4.dp) // Add spacing between messages
                )
            }
        }

        // Spacer for additional spacing between messages and input area
        Spacer(modifier = Modifier.height(8.dp))

        // UI for input and send button
        var messageText by remember { mutableStateOf("") }

        TextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Enter your message") },
            modifier = Modifier.fillMaxWidth() // Make the TextField take the full width
        )

        Spacer(modifier = Modifier.height(8.dp)) // Add space between TextField and Button

        Button(
            onClick = {
                if (messageText.isNotBlank()) {
                    geminiViewModel.sendMessage(messageText)
                    messageText = "" // Clear the input after sending
                }
            },
            modifier = Modifier.fillMaxWidth() // Make the button take the full width
        ) {
            Text("Send")
        }
    }
}

