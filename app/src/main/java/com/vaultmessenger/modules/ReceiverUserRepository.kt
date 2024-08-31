package com.vaultmessenger.modules

import com.vaultmessenger.model.ReceiverUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReceiverUserRepository(private val uid: String? = null) {
    // Access FirebaseAuth
    private val auth = FirebaseService.auth

    // Access Firestore
    private val firestore = FirebaseService.firestore

    // Access FirebaseStorage
    private val storage = FirebaseService.storage

    private fun getCurrentUserId(): String {
        return uid ?: "guest"
    }

    suspend fun saveUser(receiverUser: ReceiverUser) {
        val userId = getCurrentUserId()
        if (userId == "guest") {
            // Handle guest user logic here if needed
            throw Exception("Guest user cannot perform this operation")
        }
        val userRef = firestore.collection("users").document(userId)

        try {
            val documentSnapshot = userRef.get().await()
            if (documentSnapshot.exists()) {
                userRef.update(receiverUser.toMap()).await()
            } else {
                userRef.set(receiverUser).await()
            }
        } catch (e: Exception) {
            // Handle the error here (e.g., log the exception, show an error message)
        }
    }

    fun getReceiverUserFlow(): Flow<ReceiverUser?> {
        return flow {
            val userId = getCurrentUserId()
            if (userId == "guest") {
                emit(null)
                return@flow
            }
            val userRef = firestore.collection("users").document(userId)
            val documentSnapshot = userRef.get().await()
            emit(documentSnapshot.toObject(ReceiverUser::class.java))
        }
    }

    suspend fun createNewUserAccount() {
        val userId = getCurrentUserId()
        if (userId == "guest") {
            return
        }
        val userRef = firestore.collection("users").document(userId)
        val documentSnapshot = userRef.get().await()
        val receiverUser = documentSnapshot.toObject(ReceiverUser::class.java)

        if (receiverUser == null) {
            val currentUser = auth.currentUser
            val newUser = ReceiverUser(
                userId = userId,
                userName = currentUser?.displayName ?: "Unknown",
                email = currentUser?.email ?: "Unknown",
                profilePictureUrl = currentUser?.photoUrl?.toString() ?: "",
                nickName = "Set Nick Name",
                bio = "Tell us about you",
                dob = "01/01/2000",
                status = "ONLINE"
            )
            userRef.set(newUser).await()
        } else {
            if (receiverUser.profilePictureUrl.isBlank()) {
                userRef.update(receiverUser.toMap()).await()
            }
        }
    }
}

fun ReceiverUser.toMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "userName" to userName,
        "email" to email,
        "profilePictureUrl" to profilePictureUrl,
        "nickName" to nickName,
        "bio" to bio,
        "dob" to dob,
        "status" to status
    )
}