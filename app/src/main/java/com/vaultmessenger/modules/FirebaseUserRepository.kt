package com.vaultmessenger.modules

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.vaultmessenger.model.User
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(private val uid: String? = null) {
    private val auth = FirebaseService.auth
    private val firestore = FirebaseService.firestore

    private fun getCurrentUserId(): String {
        return uid ?: auth.currentUser?.uid ?: "guest"
    }

    suspend fun saveUser(user: User) {
        val userId = getCurrentUserId()
        if (userId == "guest") {
            throw IllegalStateException("Guest user cannot perform this operation")
        }

        try {
            val userRef = firestore.collection("users").document(userId)
            val documentSnapshot = userRef.get().await()

            if (documentSnapshot.exists()) {
                userRef.update(user.toMap()).await()
            } else {
                userRef.set(user).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepository", "Error saving user: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUser(): User? {
        val userId = getCurrentUserId()
        if (userId == "guest") {
            return null
        }

        return try {
            val userRef = firestore.collection("users").document(userId)

            // Attempt to get the document from the server first
            val documentSnapshot = userRef.get(Source.SERVER).await()

            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    // Check if hashUserId is missing or empty
                    if (it.hashUserId.isEmpty()) {
                        val hashedUserId = Encoder.encodeWithSHA256(it.userId)
                        userRef.update("hashUserId", hashedUserId).await()
                    }
                }
                // Send data to cache
                userRef.get(Source.CACHE).await() // Fetch from cache to ensure it's stored
                return user
            } else {
                createNewUserAccount()
                val newUserSnapshot = userRef.get(Source.CACHE).await()
                return newUserSnapshot.toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepository", "Error getting user: ${e.message}", e)
            return null
        }
    }


    suspend fun createNewUserAccount() {
        val userId = getCurrentUserId()
        if (userId == "guest") return

        try {
            val userRef = firestore.collection("users").document(userId)
            val documentSnapshot = userRef.get().await()
            val user = documentSnapshot.toObject(User::class.java)

            if (user == null) {
                val currentUser = auth.currentUser
                val newUser = User(
                    userId = userId,
                    userName = currentUser?.displayName ?: "Unknown",
                    email = currentUser?.email ?: "Unknown",
                    profilePictureUrl = currentUser?.photoUrl?.toString() ?: "",
                    nickName = "Set Nick Name",
                    bio = "Tell us about you",
                    dob = "01/01/2000",
                    status = "ONLINE",
                    hashUserId = Encoder.encodeWithSHA256(userId)!!
                )
                userRef.set(newUser).await()
            } else {
                if (user.profilePictureUrl.isBlank()) {
                    userRef.update(user.toMap()).await()
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepository", "Error creating new user account: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateOnlineStatus(userId: String, status: String) {
        if (userId.isEmpty()) return

        try {
            val userRef = firestore.collection("users").document(userId)
            val snapshot = userRef.get().await()

            if (snapshot.exists()) {
                userRef.update("status", status).await()
            } else {
                Log.w("FirebaseUserRepository", "User document does not exist for userId: $userId")
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.e("FirebaseUserRepository", "Permission denied: ${e.message}")
                // Handle permission denial, e.g., notify the user or redirect
            } else {
                Log.e("FirebaseUserRepository", "Error updating online status: ${e.message}", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepository", "Error updating online status: ${e.message}", e)
            throw e
        }
    }


    suspend fun isUserOnlineStatus(userId: String): Boolean {
        if (userId.isEmpty()) return false

        return try {
            val userRef = firestore.collection("users").document(userId)
            val snapshot = userRef.get().await()

            if (snapshot.exists()) {
                snapshot.getString("status") == "ONLINE"
            } else {
                Log.w("FirebaseUserRepository", "User document does not exist for userId: $userId")
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepository", "Error checking online status: ${e.message}", e)
            false
        }
    }
}

fun User.toMap(): Map<String, Any?> {
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
