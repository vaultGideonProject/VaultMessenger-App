package com.vaultmessenger.modules

import android.util.Log
import androidx.compose.runtime.internal.illegalDecoyCallException
import com.google.firebase.firestore.DocumentChange
import com.google.gson.Gson
import com.vaultmessenger.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ChatRepository() {

    companion object {
        const val MESSAGES_COLLECTION = "messages"
    }

    private val functions = FirebaseService.functions
    private val firestore = FirebaseService.firestore
    val _messageChangedFlow = MutableStateFlow(false)

    // Expose StateFlow to observe the changes
    val messageChangedFlow: StateFlow<Boolean> = _messageChangedFlow


    // Function to send a message using Cloud Function

    suspend fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        // Create a data map without converting the message to JSON string
        val data = hashMapOf(
            "senderUid" to senderUid,
            "receiverUid" to receiverUid,
            "message" to Gson().toJson(message) // Pass the Message object directly
        )
        try {
            val endpointSendMessages = URL("https://services.vaultmessenger.com/send-message")

            val result = functions
                .getHttpsCallableFromUrl(endpointSendMessages)
                .call(data)
                .await()

            Log.d("sendMessage Success", "Message sent successfully from $senderUid to $receiverUid")
            Log.d("sendMessage service", "service response: ${result.data.toString()}")
        } catch (e: Exception) {
            Log.e("sendMessage Failed", "Error sending message: ${e.message}", e)
        }
    }
    // Function to retrieve messages using Cloud Function
    // fixed issue of making too many network calls, now caching to firebase
    suspend fun getMessagesFlow(senderUid: String, receiverUid: String): Flow<List<Message>> {
        return flow {

            try {
                val endpointGetMessages = URL(
                    "https://services.vaultmessenger.com/get-messages"
                )

                val result = functions
                    .getHttpsCallableFromUrl( endpointGetMessages) // Name of your cloud function
                    .call(mapOf("senderUid" to senderUid, "receiverUid" to receiverUid))
                    .await()

                // Process the response
                val data = result.data as? Map<*, *>
                if(data.isNullOrEmpty()){
                    return@flow
                }
                val success = data?.get("success") as? Boolean ?: false

               // Log.d("getMessagesFlow", "status ${success.equals(true)}")
                Log.d("getMessagesFlow", "${result.data}")

                if (success.toString().isNotEmpty()) {
                    val messagesList = data?.get("message") as? List<Map<String, Any>> ?: emptyList()
                    val messages = messagesList.map { map ->
                        Message(
                            id = (map["id"] as? Number)?.toInt() ?: 0,
                            conversationId = map["conversationId"] as? String,
                            imageUrl = map["imageUrl"] as? String,
                            voiceNoteURL = map["voiceNoteURL"] as? String,
                            voiceNoteDuration = map["voiceNoteDuration"] as? String,
                            messageText = map["messageText"] as? String ?: "",
                            name = map["name"] as? String ?: "",
                            photoUrl = map["photoUrl"] as? String ?: "",
                            timestamp = map["timestamp"] as? String ?: "",
                            userId1 = map["userId1"] as? String ?: "",
                            userId2 = map["userId2"] as? String ?: "",
                            loading = map["loading"] as? Boolean ?: true,
                            isTyping = map["isTyping"] as? Boolean ?: false
                        )
                    }
                    emit(messages)
                } else {

                    // Handle failure case
                    emit(emptyList())
                    throw illegalDecoyCallException("Failed to get Message List is Empty")

                }
            } catch (e: Exception) {
                // Handle error case
               // emit(emptyList())
            }
        }
    }

    // Function to delete a message using Cloud Function
    suspend fun deleteMessage(senderUid: String, receiverUid: String, messageId: String) {
        val data = hashMapOf(
            "senderUid" to senderUid,
            "receiverUid" to receiverUid,
            "messageId" to messageId
        )

        try {
            functions.getHttpsCallable("deleteMessage")
                .call(data)
                .await()
            Log.d("deleteMessage Success", "Message deleted successfully")
        } catch (e: Exception) {
            Log.e("deleteMessage Failed", "Error deleting message: ${e.message}", e)
        }
    }

    fun listenForMessageChanges(senderUid: String, receiverUid: String): StateFlow<Boolean> {
        val messagesRef = firestore
            .collection("messages")
            .document(senderUid)
            .collection(receiverUid)

        var isInitialLoad = true // Flag to track the initial load

        // Set up a real-time listener
        messagesRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Listen failed: $e")
                return@addSnapshotListener
            }

            if (snapshots == null || snapshots.isEmpty) {
                println("No message changes")
                return@addSnapshotListener
            }

            var hasRelevantChanges = false

            for (dc in snapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        println("New message: ${dc.document.data}")
                        hasRelevantChanges = true
                    }
                    DocumentChange.Type.MODIFIED -> {
                        println("Modified message: ${dc.document.data}")
                        hasRelevantChanges = true
                    }
                    DocumentChange.Type.REMOVED -> {
                        println("Removed message: ${dc.document.data}")
                        hasRelevantChanges = true
                    }
                }
            }

            // Only update the flow if there were relevant changes and this is not the initial load
            if (hasRelevantChanges && !isInitialLoad) {
                _messageChangedFlow.value = true
            }

            // Mark initial load as complete after the first snapshot is processed
            isInitialLoad = false
        }

        // Return the StateFlow to allow observing the flow of changes
        return messageChangedFlow
    }

}