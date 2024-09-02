package com.vaultmessenger.viewModel

import NotificationsViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.modules.NotificationRepository

class NotificationsViewModelFactory(
    private val repository: NotificationRepository,
    private val errorsViewModel: ErrorsViewModel
    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(repository, errorsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}