package com.vaultmessenger.modules

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.vaultmessenger.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    companion object {
        const val MESSAGES_COLLECTION = "messages"
    }

    // Access FirebaseAuth
    private val auth = FirebaseService.auth

    // Access Firestore
    private val firestore = FirebaseService.firestore

    // Access FirebaseStorage
    private val storage = FirebaseService.storage


   // private private val firestore = firestore

    // Function to send a message
    suspend fun sendMessage(senderUid: String, receiverUid: String, message: Message) {
        try {
            val messagesCollectionSender = firestore.collection(MESSAGES_COLLECTION)
                .document(senderUid)
                .collection(receiverUid)

            val messagesCollectionReceiver = firestore.collection(MESSAGES_COLLECTION)
                .document(receiverUid)
                .collection(senderUid)

            // Add the message to both sender's and receiver's collections
            messagesCollectionSender.add(message).await()
            messagesCollectionReceiver.add(message).await()

            Log.d("sendMessage Success", "Message sent successfully from $senderUid to $receiverUid")
        } catch (e: Exception) {
            Log.e("sendMessage Failed", "Error sending message: ${e.message}", e)
        }
    }

    // Function to retrieve messages between two users in real-time
    // fixed issue of making too many network calls, now caching to firebase
    fun getMessagesFlow(senderUid: String, receiverUid: String): Flow<List<Message>> = callbackFlow {
        // Register real-time updates listener
        val collectionRef = firestore.collection(MESSAGES_COLLECTION)
            .document(senderUid)
            .collection(receiverUid)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        // Attempt to retrieve cached data first
        collectionRef.get(Source.CACHE)
            .addOnSuccessListener { documents ->
                val cachedMessages = documents.toObjects(Message::class.java)
                trySend(cachedMessages).isSuccess
            }
            .addOnFailureListener { e ->
                // Handle the error if there's an issue fetching from the cache
                Log.e("getMessagesFlow", "Error getting cached messages: ${e.message}", e)
                // Optionally: Close the flow or handle it in a specific way
            }

        // Register real-time updates listener
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow if there's an error
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val messages = snapshot.toObjects(Message::class.java)
                trySend(messages).isSuccess // Emit the updated list of messages
            }
        }

        // Ensure the listener is removed when the flow is closed
        awaitClose {
            listenerRegistration.remove()
        }
    }

    // Function to delete a specific message
    suspend fun deleteMessage(senderUid: String, receiverUid: String, messageId: String) {
        firestore.collection(MESSAGES_COLLECTION)
            .document(senderUid)
            .collection(receiverUid)
            .document(messageId)
            .delete()
            .await()

        firestore.collection(MESSAGES_COLLECTION)
            .document(receiverUid)
            .collection(senderUid)
            .document(messageId)
            .delete()
            .await()
    }
}
