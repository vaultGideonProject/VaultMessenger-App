package com.vaultmessenger.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VoiceNoteViewModelFactory(
    private val application: Application,
    private val errorsViewModel: ErrorsViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceNoteViewModel::class.java)) {
            return VoiceNoteViewModel(application, errorsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}