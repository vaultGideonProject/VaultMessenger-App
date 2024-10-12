package com.vaultmessenger.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GeminiViewModel : ViewModel() {

    private val geminiService = GeminiApiService()

    // Example state holding the list of messages
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    // Function to send a message and get the AI response
    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                // Send the message and get AI response from GeminiService
                val aiResponse = geminiService.generateContent(message)
                val updatedMessages = _messages.value + "You: $message" + "Gemini: $aiResponse"
                _messages.value = updatedMessages

            } catch (e: Exception) {
                // Handle any errors and update UI accordingly
                _messages.value = _messages.value + "Error: ${e.localizedMessage}"
            }
        }
    }
}

