package com.vaultmessenger.sharedRepository

import android.content.Context
import android.util.Log
import com.vaultmessenger.MyApp
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.FirebaseUserRepository

class SharedUserRepository(
    private val context: Context,
    private val uid: String? = null,
) {
    private val myApp = MyApp(context).repositoryUser
    private val auth = FirebaseService.auth
    private val remoteRepository: FirebaseUserRepository = FirebaseUserRepository()

    private var remoteUser = LocalUser(
        userId = "guest",
        userName = "",
        hashUserId = "guest",
        profilePictureUrl = "",
        bio = "",
        email = "",
        nickName = "",
        dob = "",
        status = ""
    )

    private fun getCurrentUserId(): String {
        return uid ?: auth.currentUser?.uid ?: "guest"
    }

    // Updated function name to avoid conflict with 'remoteUser' variable
    private suspend fun fetchRemoteUser() {
        val loadedUser = remoteRepository.getUser()
        if (loadedUser != null) {
            remoteUser = loadedUser.toLocalUser()
        }
        println("remoteUser: $remoteUser")
        myApp.addUser(remoteUser)
    }

    suspend fun getUser(): LocalUser? {
        val userId = getCurrentUserId()
        if (userId == "guest") {
            return null
        }
        fetchRemoteUser()
        return myApp.getUser(userId)
    }

    suspend fun saveUser(user: LocalUser): LocalUser {
        myApp.addUser(user)
        return user
    }

    suspend fun updateOnlineStatus(userId: String, status: String) {
        remoteRepository.updateOnlineStatus(userId, status)
       // fetchRemoteUser()
    }

    suspend fun isUserOnlineStatus(userId: String): Boolean {
        return remoteRepository.isUserOnlineStatus(userId)
    }

    suspend fun updateUserProfilePhoto(userId: String, newProfilePictureUrl: String, user: User) {
        try {
            // Assuming you have a local database with UserDao or Firestore/Realtime Database.
            myApp.updateUserProfilePhoto(userId, newProfilePictureUrl)
            remoteRepository.saveUser(user)
        } catch (e: Exception) {
            // Handle potential errors, such as network issues or database write failures
            Log.e("SharedUserRepository", "Failed to update profile photo for userId: $userId", e)
            throw Exception("Failed to update profile photo: ${e.message}")
        }
    }


    // Convert the Firebase User model to LocalUser
    private fun User.toLocalUser(): LocalUser {
        return LocalUser(
            userId = this.userId,
            userName = this.userName,
            email = this.email,
            profilePictureUrl = this.profilePictureUrl,
            nickName = this.nickName,
            bio = this.bio,
            dob = this.dob,
            status = this.status,
            hashUserId = this.hashUserId
        )
    }
}
