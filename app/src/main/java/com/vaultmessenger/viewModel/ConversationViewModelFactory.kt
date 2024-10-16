package com.vaultmessenger.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.sharedRepository.SharedConversationRepository

class ConversationViewModelFactory(
   private val context: Context,
   private val conversationRepository: ConversationRepository,
    private val errorsViewModel: ErrorsViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConversationViewModel( context,
                conversationRepository,
                errorsViewModel = errorsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    }