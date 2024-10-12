package com.vaultmessenger.modules

import android.content.Context
import android.util.Log
import androidx.compose.runtime.internal.illegalDecoyCallException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.sharedRepository.SharedUserRepository
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationRepository(
    private val errorsViewModel: ErrorsViewModel,
    private val context: Context,
) {
    private val firestore = FirebaseService.firestore
    private val functions = FirebaseService.functions

    companion object {
        private const val TAG = "ConversationRepository"
    }

    private fun getUserConversationsCollection(userId: String): CollectionReference {
        return firestore.collection("conversations")
            .document(userId)
            .collection("messages")
    }

    suspend fun getConversationsFlow(userId: String): Flow<List<Conversation>> {
        return flow {
            try {
                val endpointGetConversations = URL(
                    "https://services.vaultmessenger.com/get-conversations"
                )

                // Call the cloud function
                val result = functions
                    .getHttpsCallableFromUrl(endpointGetConversations)
                    .call(mapOf("userId" to userId))
                    .await()

                // Process the response
                val data = result.data as? Map<*, *>
                val success = data?.get("success") as? Boolean ?: false

                Log.d("getConversationsFlow", "status $success")

                if (success) {
                    // Extract the conversation list from the response
                    val conversationList = data?.get("message") as? List<Map<String, Any>> ?: emptyList()
                    val conversations = conversationList.map { map ->
                        Conversation(
                            conversationId = map["conversationId"] as? String ?: "",
                            lastMessage = map["lastMessage"] as? String ?: "",
                            timestamp = map["timestamp"] as? String ?: "",

                            // Casting userIds map and accessing values
                            userIds = (map["userIds"] as? Map<String, String>)?.let {
                                mapOf(
                                    "userId1" to (it["userId1"] as? String ?: ""),
                                    "userId2" to (it["userId2"] as? String ?: "")
                                )
                            } ?: emptyMap(),

                            // Casting userNames map and accessing values
                            userNames = (map["userNames"] as? Map<String, String>)?.let {
                                hashMapOf(
                                    "userName1" to (it["userName1"] as? String ?: ""),
                                    "userName2" to (it["userName2"] as? String ?: "")
                                )
                            } ?: hashMapOf(),

                            // Casting userPhotos map and accessing values
                            userPhotos = (map["userPhotos"] as? Map<String, String>)?.let {
                                mapOf(
                                    "profilePictureUrl_userId1" to (it["profilePictureUrl_userId1"] as? String ?: ""),
                                    "profilePictureUrl_userId2" to (it["profilePictureUrl_userId2"] as? String ?: "")
                                )
                            } ?: emptyMap(),

                            // Casting typing map and accessing values
                            isTyping = (map["typing"] as? Map<*, *>)?.let {
                                mapOf(
                                    "userId1" to (it["userId1"] as? Boolean ?: false),
                                    "userId2" to (it["userId2"] as? Boolean ?: false)
                                )
                            } ?: emptyMap()
                        )
                    }
                    conversations.forEach {
                        conversation ->
                        Log.d("getConversationsFlow", conversation.toString())
                    }
                    emit(conversations)

                } else {
                    // If success is false, return an empty list
                    Log.e("getConversationsFlow", "Failed to retrieve conversations")
                    emit(emptyList())
                }
            } catch (e: Exception) {
                // Handle exception and emit an empty list
                Log.e("getConversationsFlow", "Error retrieving conversations: ${e.message}")
                emit(emptyList())
            }
        }.retry(3) // Retry the flow 3 times if any error occurs
    }

    suspend fun setConversationBySenderId(senderId: String, receiverId: String, message: Message, viewModelStoreOwner: ViewModelStoreOwner?) {
        handleConversationPersistence(senderId, receiverId, message, viewModelStoreOwner)
    }

    suspend fun setConversationByReceiverId(senderId: String, receiverId: String, message: Message, viewModelStoreOwner: ViewModelStoreOwner?) {
        handleConversationPersistence(senderId, receiverId, message, viewModelStoreOwner)
    }

    private suspend fun handleConversationPersistence(
        senderId: String,
        receiverId: String,
        message: Message,
        viewModelStoreOwner: ViewModelStoreOwner?
    ) {
        val messageText = message.messageText.trim()

        val userId1Details = getProfileViewModel(viewModelStoreOwner, context)
        val userId2Details = getReceiverViewModel(viewModelStoreOwner, receiverId)
        val userId1PhotoURLs = userId1Details.user.value?.profilePictureUrl
        val userId2PhotoURLs = userId2Details.receiverUser.value?.profilePictureUrl

        val userIds = mapOf("userId1" to senderId, "userId2" to receiverId)
        val userIdsCheck1 = mapOf("userId1" to senderId, "userId2" to receiverId)
        val userIdsCheck2 = mapOf("userId1" to receiverId, "userId2" to senderId)
        val timestamp = generateTimestamp()
        val photoURLs = mapOf(
            "profilePictureUrl_userId1" to (userId1PhotoURLs ?: ""),
            "profilePictureUrl_userId2" to (userId2PhotoURLs ?: "")
        )
        val userName1 = userId1Details.user.value?.userName ?: ""
        val userName2 = userId2Details.receiverUser.value?.userName ?: ""
        val userNames = HashMap<String, String>().apply {
            put("userName1", userName1)
            put("userName2", userName2)
        }

        try {
            withContext(Dispatchers.IO) {
                val collectionRef1 = getUserConversationsCollection(senderId)
                val collectionRef2 = getUserConversationsCollection(receiverId)

                val querySnapshot1 = collectionRef1.whereEqualTo("userIds", userIdsCheck1).get(Source.CACHE).await()
                val querySnapshot2 = collectionRef2.whereEqualTo("userIds", userIdsCheck2).get(Source.CACHE).await()

                if (querySnapshot1.isEmpty && querySnapshot2.isEmpty) {
                    val serverSnapshot1 = collectionRef1.whereEqualTo("userIds", userIdsCheck1).get(Source.SERVER).await()
                    val serverSnapshot2 = collectionRef2.whereEqualTo("userIds", userIdsCheck2).get(Source.SERVER).await()

                    if (serverSnapshot1.isEmpty && serverSnapshot2.isEmpty) {
                        val setMessage = Conversation(
                            userIds = userIds,
                            userPhotos = photoURLs,
                            userNames = userNames,
                            conversationId = "",
                            lastMessage = messageText,
                            timestamp = timestamp
                        ).apply {
                            conversationId = firestore.collection("conversations")
                                .document(senderId)
                                .collection("messages")
                                .document().id
                        }

                        firestore.collection("conversations")
                            .document(senderId)
                            .collection("messages")
                            .document(setMessage.conversationId)
                            .set(setMessage)
                            .await()

                        firestore.collection("conversations")
                            .document(receiverId)
                            .collection("messages")
                            .document(setMessage.conversationId)
                            .set(setMessage)
                            .await()

                        Log.d(TAG, "Message sent successfully")
                    } else {
                        updateExistingConversation(
                            querySnapshot1,
                            querySnapshot2,
                            senderId,
                            receiverId,
                            messageText,
                            timestamp,
                            userIds,
                            photoURLs,
                            userNames
                        )
                    }
                } else {
                    updateExistingConversation(
                        querySnapshot1,
                        querySnapshot2,
                        senderId,
                        receiverId,
                        messageText,
                        timestamp,
                        userIds,
                        photoURLs,
                        userNames
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting conversation: ${e.message}", e)
        }
    }

    private suspend fun updateExistingConversation(
        querySnapshot1: QuerySnapshot,
        querySnapshot2: QuerySnapshot,
        senderId: String,
        receiverId: String,
        lastMessage: String,
        timestamp: String,
        userIds: Map<String, String>,
        photoURLs: Map<String, String>,
        userNames: HashMap<String, String>
    ) {
        val existingDocumentSnapshot = if (!querySnapshot1.isEmpty) querySnapshot1.documents[0] else querySnapshot2.documents[0]

        val conversationId = existingDocumentSnapshot.id
        val existingConversation = existingDocumentSnapshot.toObject(Conversation::class.java)
        existingConversation?.let {
            it.lastMessage = lastMessage
            it.timestamp = timestamp
            it.userIds = userIds
            it.userPhotos = photoURLs
            it.userNames = userNames

            firestore.collection("conversations")
                .document(senderId)
                .collection("messages")
                .document(conversationId)
                .set(it)
                .await()

            firestore.collection("conversations")
                .document(receiverId)
                .collection("messages")
                .document(conversationId)
                .set(it)
                .await()

            Log.d(TAG, "Last message updated successfully")
        }
    }

    suspend fun updateTypingStatus(existingConversation: Conversation, conversationId: String, senderId: String, receiverId: String, isTyping: Boolean) {
        try {
            val conversationRef1 = firestore.collection("conversations")
                .document(senderId)
                .collection("messages")
                .document(conversationId)
            val conversationRef2 = firestore.collection("conversations")
                .document(receiverId)
                .collection("messages")
                .document(conversationId)

            conversationRef1.update("isTyping.$senderId", isTyping).await()
            conversationRef2.update("isTyping", isTyping).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating typing status: ${e.message}", e)
        }
    }

    private fun getProfileViewModel(
        viewModelStoreOwner: ViewModelStoreOwner?,
        context: Context
    ): ProfileViewModel {
        val userRepository = SharedUserRepository(context)
        val factory = ProfileViewModelFactory(userRepository, errorsViewModel = errorsViewModel)
        return ViewModelProvider(viewModelStoreOwner!!, factory)[ProfileViewModel::class.java]
    }

    private fun getReceiverViewModel(
        viewModelStoreOwner: ViewModelStoreOwner?,
        receiverId: String
    ): ReceiverUserViewModel {
        val userRepository = ReceiverUserRepository(receiverId)
        val factory = ReceiverUserViewModelFactory(userRepository, errorsViewModel)
        return ViewModelProvider(viewModelStoreOwner!!, factory)[ReceiverUserViewModel::class.java]
    }

    private fun generateTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return sdf.format(Date())
    }
}
