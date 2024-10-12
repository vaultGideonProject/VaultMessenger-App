package com.vaultmessenger.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.sharedRepository.SharedUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class ProfileViewModel(
    private val repository: SharedUserRepository,
    private val errorsViewModel: ErrorsViewModel,
    ) : ViewModel() {
    private val _user = MutableStateFlow<LocalUser?>(LocalUser(
        userId = "guest",
        userName ="",
        hashUserId = "guest",
        profilePictureUrl = "",
        bio = "",
        email ="",
        nickName = "",
        dob = "",
        status = ""))
    val user: StateFlow<LocalUser?> get() = _user

    private val _onlineStatus = MutableStateFlow("offline")
    val onlineStatus: StateFlow<String> get() = _onlineStatus
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage:StateFlow<String?> = _errorMessage

    private val _userReady = MutableStateFlow(false)
    val userReady: StateFlow<Boolean> = _userReady

    private var lastFetchTime: Long = 0
    private val cacheExpirationTime = 5 * 60 * 1000 // 5 minutes

    init {
        viewModelScope.launch {
            loadUser().collect { user ->
                refreshUser()
            }
        }
    }

    private suspend fun loadUser(): Flow<LocalUser?> = flow {
        val currentTime = System.currentTimeMillis()

        _userReady.value = false // Reset userReady at the start of loading

        val cachedUser = _user.value
        if (cachedUser != null && (currentTime - lastFetchTime) < cacheExpirationTime) {
            emit(cachedUser)
            _userReady.value = true // Cached user is ready
        } else {
            try {
                val loadedUser = repository.getUser()
                if (loadedUser != null) {
                    _user.value = loadedUser
                    lastFetchTime = currentTime
                    emit(loadedUser)
                    _userReady.value = true // User successfully loaded and ready
                } else {
                    _userReady.value = false
                    emit(null)
                }
            } catch (e: Exception) {
                _userReady.value = false
                errorsViewModel.setError(e.message ?: "Error loading user")
                Log.e("ProfileViewModel", "Error loading user: ${e.message}", e)
                emit(null)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun updateUserName(newValue: String) {
        _user.value = _user.value?.copy(userName = newValue)
    }

    fun updateNickname(newValue: String) {
        _user.value = _user.value?.copy(nickName = newValue)
    }

    fun updateDob(newValue: String) {
        _user.value = _user.value?.copy(dob = newValue)
    }

    fun updateEmail(newValue: String) {
        _user.value = _user.value?.copy(email = newValue)
    }

    fun updateAbout(newValue: String) {
        _user.value = _user.value?.copy(bio = newValue)
    }

    suspend fun updateProfilePictureUrl(newValue: String) {
        // Ensure the user exists before trying to update
        val currentUser = _user.value
        println("currentUser: $currentUser")
        if (currentUser != null) {
            // Update the local user state with the new profile picture URL
            _user.value = currentUser.copy(profilePictureUrl = newValue)

            // Ensure you pass the current user details to the repository
            try {
                repository.updateUserProfilePhoto(
                    currentUser.userId,
                    newValue,
                    _user.value!!.toUser()
                )
            } catch (e: Exception) {
                // Handle any errors that might occur during the update process
                errorsViewModel.setError(e.message ?: "Failed to update profile picture")
                Log.e("ProfileViewModel", "Error updating profile picture: ${e.message}", e)
            }
        } else {
            Log.e("ProfileViewModel", "User is null, cannot update profile picture")
        }
    }

    // Extension function to convert LocalUser to User
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

    suspend fun saveUser(user: LocalUser) {
        viewModelScope.launch {
            try {
              val localUser:LocalUser =  repository.saveUser(user)

                _user.value = localUser

                lastFetchTime = System.currentTimeMillis()
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error saving user: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

  suspend  fun refreshUser() {
        viewModelScope.launch {
            try {
                val loadedUser = repository.getUser()
                if (loadedUser != null) {
                    _user.value = loadedUser
                    lastFetchTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error refreshing user: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

  suspend  fun updateOnlineStatus(userId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateOnlineStatus(userId, status)
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error updating online status: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

   suspend fun isUserOnlineStatus(userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isOnline = repository.isUserOnlineStatus(userId)
                callback(isOnline)
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error checking online status: ${e.message}", e)
                _errorMessage.value = e.message
                callback(false)
            }
        }
    }

    fun updateUserToken(userId: String, newToken: String?) {
        val notificationRepository = NotificationRepository()
        viewModelScope.launch {
            try {
                notificationRepository.getToken(userId) { currentToken ->
                    if (currentToken != newToken) {
                        notificationRepository.saveToken(userId, newToken!!)
                        Log.d("ProfileViewModel", "Token updated successfully.")
                    } else {
                        Log.d("ProfileViewModel", "Token has not changed.")
                    }
                }
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error updating token: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    suspend fun getUserToken(userId: String): String? = suspendCancellableCoroutine { continuation ->
        val notificationRepository = NotificationRepository()
        viewModelScope.launch {
            try {
                notificationRepository.getToken(userId) { currentToken ->
                    continuation.resume(currentToken)
                }
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error fetching token: ${e.message}", e)
                _errorMessage.value = e.message
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    }

    suspend fun isUserAccLocked(navController: NavController) {
        viewModelScope.launch {
            try {
                val auth = FirebaseService.auth
                auth.currentUser?.reload()?.await()
                delay(200)
                if (auth.currentUser?.uid?.isEmpty() == true) {
                    navController.navigate("sign_in")
                }
            } catch (e: Exception) {
                errorsViewModel.setError(e.message ?: "An error occurred")
                Log.e("ProfileViewModel", "Error checking account lock status: ${e.message}", e)
                _errorMessage.value = e.message
                navController.navigate("sign_in")
            }
        }
    }
}