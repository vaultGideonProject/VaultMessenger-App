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
    private val repositoryUser = MyApp(context).repositoryUser
    private val repositoryMessages = MyApp(context).repositoryMessages
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
        try {
            val loadedUser = remoteRepository.getUser()
            if (loadedUser != null) {
                remoteUser = loadedUser.toLocalUser()
            }
            println("remoteUser: $remoteUser")
            repositoryUser.addUser(remoteUser)
        } catch (e: Exception) {
            // Handle any exceptions that might occur
            println("Error fetching remote user: ${e.message}")
            e.printStackTrace()
        }
    }


    suspend fun getUser(): LocalUser? {
        return try {
            val userId = getCurrentUserId()
            if (userId == "guest") {
                return null
            }
            fetchRemoteUser() // This is a suspend function, so it can throw exceptions.
            repositoryUser.getUser(userId) // Returns the user if found
        } catch (e: Exception) {
            // Handle any exceptions that might occur
            println("Error in getUser: ${e.message}")
            e.printStackTrace()
            null // Return null if an exception occurs
        }
    }


    suspend fun saveUser(user: LocalUser): LocalUser {
        return try {
            repositoryUser.addUser(user)
            remoteRepository.saveUser(user.toUser())  // Save to Firestore
            user
        } catch (e: Exception) {
            Log.e("SharedUserRepository", "Error saving user: ${e.message}", e)
            throw Exception("Failed to save user: ${e.message}")
        }
    }


    suspend fun updateOnlineStatus(userId: String, status: String) {
        try {
            remoteRepository.updateOnlineStatus(userId, status)
        }catch (e: Exception){
        }
    }

    suspend fun isUserOnlineStatus(userId: String): Boolean {
        return try {
            remoteRepository.isUserOnlineStatus(userId)
        }catch (e: Exception){
            return false
        }
    }

    suspend fun updateUserProfilePhoto(userId: String, newProfilePictureUrl: String, user: User) {
        try {
            // Assuming you have a local database with UserDao or Firestore/Realtime Database.
            repositoryUser.updateUserProfilePhoto(userId, newProfilePictureUrl)
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

    private fun LocalUser.toUser(): User {
        return User(
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
