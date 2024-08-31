package com.vaultmessenger.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.modules.ReceiverUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReceiverUserViewModel(private val repository: ReceiverUserRepository) : ViewModel() {

    private val _receiverUser = MutableStateFlow<ReceiverUser?>(null)
    val receiverUser: StateFlow<ReceiverUser?> = _receiverUser

    init {
        viewModelScope.launch {
            repository.getReceiverUserFlow().collect {
                _receiverUser.value = it
            }
        }
    }

    fun saveUser(receiverUser: ReceiverUser) {
        viewModelScope.launch {
            repository.saveUser(receiverUser)
        }
    }

    fun createNewUserAccount() {
        viewModelScope.launch {
            repository.createNewUserAccount()
        }
    }
}