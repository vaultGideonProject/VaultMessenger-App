package com.vaultmessenger.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.modules.ReceiverUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReceiverUserViewModel(
    private val repository: ReceiverUserRepository,
    private val errorsViewModel: ErrorsViewModel,
) : ViewModel() {

    private val _receiverUser = MutableStateFlow<ReceiverUser?>(null)
    val receiverUser: StateFlow<ReceiverUser?> = _receiverUser

    private val _receiverReady = MutableStateFlow(false)
    val receiverReady: StateFlow<Boolean> = _receiverReady

    init {
        viewModelScope.launch {
            try {
                repository.getReceiverUserFlow().collect {
                    _receiverUser.value = it
                    _receiverReady.value = true
                }
            } catch (e: Exception) {
                errorsViewModel.setError("Error collecting receiver user flow: ${e.message}")
            }
        }
    }

}