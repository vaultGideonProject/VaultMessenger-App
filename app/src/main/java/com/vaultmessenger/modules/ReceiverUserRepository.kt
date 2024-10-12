package com.vaultmessenger.modules

import com.vaultmessenger.model.ReceiverUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReceiverUserRepository(private val uid: String? = null) {

    // Access Firestore
    private val firestore = FirebaseService.firestore

    private fun getCurrentUserId(): String {
        return uid ?: "guest"
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