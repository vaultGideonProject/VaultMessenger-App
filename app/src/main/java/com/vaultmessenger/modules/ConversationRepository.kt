package com.vaultmessenger.modules

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.firestore.*
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationRepository(
    private val errorsViewModel: ErrorsViewModel
) {
    private val auth = FirebaseService.auth
    private val firestore = FirebaseService.firestore
    private val storage = FirebaseService.storage

    companion object {
        private const val TAG = "ConversationRepository"
    }

    private fun getUserConversationsCollection(userId: String): CollectionReference {
        return firestore.collection("conversations")
            .document(userId)
            .collection("messages")
    }

    fun getConversationsFlow(userId: String): Flow<List<Conversation>> = callbackFlow {
        val collectionRef = getUserConversationsCollection(userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val initialQuery = collectionRef.get(Source.SERVER)
        initialQuery.addOnSuccessListener { documents ->
            val messages = documents.toObjects(Conversation::class.java)
            trySend(messages).isSuccess
        }.addOnFailureListener { e ->
            close(e)
        }

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.let {
                val conversations = it.toObjects(Conversation::class.java)
                trySend(conversations).isSuccess
            }
        }

        awaitClose { listenerRegistration.remove() }
    }.retry(3) { e -> e is FirebaseFirestoreException }
        .catch { e -> Log.e(TAG, "Failed to retrieve conversations: $e") }

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
        val messageText = message.messageText?.trim() ?: return

        val userId1Details = getProfileViewModel(viewModelStoreOwner, senderId)
        val userId2Details = getReceiverViewModel(viewModelStoreOwner, receiverId)
        val userId1PhotoURLs = userId1Details.user.value?.profilePictureUrl
        val userId2PhotoURLs = userId2Details.receiverUser.value?.profilePictureUrl

        val userIds = mapOf("userId1" to senderId, "userId2" to receiverId)
        val userIdsCheck1 = mapOf("userId1" to senderId, "userId2" to receiverId)
        val userIdsCheck2 = mapOf("userId1" to receiverId, "userId2" to senderId)
        val lastMessage = messageText
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
                            lastMessage = lastMessage,
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
                            lastMessage,
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
                        lastMessage,
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

    private suspend fun getProfileViewModel(
        viewModelStoreOwner: ViewModelStoreOwner?,
        userId: String
    ): ProfileViewModel {
        val userRepository = FirebaseUserRepository(userId)
        val factory = ProfileViewModelFactory(userRepository, errorsViewModel = errorsViewModel)
        return ViewModelProvider(viewModelStoreOwner!!, factory)
            .get(ProfileViewModel::class.java)
    }

    private suspend fun getReceiverViewModel(
        viewModelStoreOwner: ViewModelStoreOwner?,
        receiverId: String
    ): ReceiverUserViewModel {
        val userRepository = ReceiverUserRepository(receiverId)
        val factory = ReceiverUserViewModelFactory(userRepository, errorsViewModel)
        return ViewModelProvider(viewModelStoreOwner!!, factory)
            .get(ReceiverUserViewModel::class.java)
    }

    private fun generateTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return sdf.format(Date())
    }
}
