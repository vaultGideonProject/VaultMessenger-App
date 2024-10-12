package com.vaultmessenger.local

import androidx.annotation.WorkerThread
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.database.UserDao
import kotlinx.coroutines.flow.StateFlow

class LocalUserRepository(private val userDao: UserDao) {

    // Insert a local message into the database
    @WorkerThread
    suspend fun addUser(user: LocalUser) {
        userDao.insert(user)
    }

    suspend fun getUser(id: String): LocalUser {
        return userDao.getUserById(id)
    }

    suspend fun getAllUsers(): List<LocalUser> {
        return userDao.getAllUsers()
    }
    suspend fun updateUserProfilePhoto(userId: String, newUrl: String) {
        userDao.updateProfilePhoto(userId, newUrl)
    }
}
