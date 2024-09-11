package com.vaultmessenger.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: LocalUser)

    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getUserById(id: Int): LocalUser?

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<LocalUser>
}
