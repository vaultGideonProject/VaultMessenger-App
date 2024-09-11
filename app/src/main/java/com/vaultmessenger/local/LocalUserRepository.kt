package com.vaultmessenger.local

import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.database.UserDao

class LocalUserRepository(private val userDao: UserDao) {

    suspend fun addUser(user: LocalUser) {
        userDao.insert(user)
    }

    suspend fun getUser(id: Int): LocalUser? {
        return userDao.getUserById(id)
    }

    suspend fun getAllUsers(): List<LocalUser> {
        return userDao.getAllUsers()
    }
}
