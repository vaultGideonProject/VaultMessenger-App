package com.vaultmessenger.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileViewModel(private val repository: FirebaseUserRepository) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _onlineStatus = MutableStateFlow("offline")
    val onlineStatus: StateFlow<String> get() = _onlineStatus
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage:StateFlow<String?> = _errorMessage

    private var lastFetchTime: Long = 0
    private val cacheExpirationTime = 5 * 60 * 1000 // 5 minutes

    init {
        viewModelScope.launch {
            loadUser().collect { user ->
                refreshUser()
            }
        }
    }

    private fun loadUser(): Flow<User?> = flow {
        val currentTime = System.currentTimeMillis()
        val cachedUser = _user.value

        if (cachedUser != null && (currentTime - lastFetchTime) < cacheExpirationTime) {
            emit(cachedUser)
        } else {
            try {
                val loadedUser = repository.getUser()
                _user.value = loadedUser
                lastFetchTime = currentTime
                emit(loadedUser)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading user: ${e.message}", e)
               _errorMessage.value = e.message

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

    fun updateProfilePictureUrl(newValue: String) {
        _user.value = _user.value?.copy(profilePictureUrl = newValue)
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            try {
                repository.saveUser(user)
                _user.value = user
                lastFetchTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving user: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            try {
                val loadedUser = repository.getUser()
                if (loadedUser != null) {
                    _user.value = loadedUser
                    lastFetchTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing user: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    fun updateOnlineStatus(userId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateOnlineStatus(userId, status)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating online status: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    fun isUserOnlineStatus(userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isOnline = repository.isUserOnlineStatus(userId)
                callback(isOnline)
            } catch (e: Exception) {
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
                Log.e("ProfileViewModel", "Error checking account lock status: ${e.message}", e)
                _errorMessage.value = e.message
                navController.navigate("sign_in")
            }
        }
    }
    fun clearError(){
        _errorMessage.value = null
    }

    fun setError(errorMessage: String) {
        _errorMessage.value = errorMessage
    }
}