package com.vaultmessenger.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.vaultmessenger.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiApiService {

    private val generativeModel = GenerativeModel(
        // Specify the Gemini model appropriate for your use case
        modelName = "gemini-1.5-flash",
        // Use BuildConfig to securely access the API key
        apiKey = ""
    )

    /**
     * Generate content based on the provided prompt
     * This function runs asynchronously and returns the response
     */
    suspend fun generateContent(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text ?: "No response from Gemini"
            } catch (e: Exception) {
                // Handle any potential exceptions (network, model issues, etc.)
                "Error: ${e.localizedMessage}"
            }
        }
    }
}
