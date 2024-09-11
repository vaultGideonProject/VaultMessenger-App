package com.vaultmessenger.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class LocalUser(
    @PrimaryKey(autoGenerate = false)val userId: String = "",
    val userName: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val nickName: String = "",
    val bio: String = "",
    val dob: String = "",
    val status: String = "",
    val hashUserId: String = ""
)
