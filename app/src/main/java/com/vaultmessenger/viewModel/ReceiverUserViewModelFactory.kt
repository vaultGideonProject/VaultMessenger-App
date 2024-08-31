package com.vaultmessenger.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.modules.ReceiverUserRepository

class ReceiverUserViewModelFactory(
    private val repository: ReceiverUserRepository = ReceiverUserRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceiverUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceiverUserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
