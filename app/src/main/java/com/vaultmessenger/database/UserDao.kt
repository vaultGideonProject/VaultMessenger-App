package com.vaultmessenger.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.StateFlow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: LocalUser)

    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getUserById(id: String): LocalUser

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<LocalUser>

    @Query("UPDATE user SET profilePictureUrl = :newProfilePictureUrl WHERE userId = :userId")
    suspend fun updateProfilePhoto(userId: String, newProfilePictureUrl: String)
}
