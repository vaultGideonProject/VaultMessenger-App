
package com.vaultmessenger.viewModel

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.model.Contact
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.viewModel.ConversationViewModel.Companion.MAX_MESSAGE_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactRepository) : ViewModel() {
    private val _contactList = MutableStateFlow<List<Contact>>(emptyList())
    val contactList: StateFlow<List<Contact>> get() = _contactList
    private val _errorMessage = MutableStateFlow<String?>(null) // Nullable to handle no message state
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        // Initialize with a default empty list
        _contactList.value = emptyList()
    }

    // Function to load contacts and update _contactList
    fun loadContacts(userId: String) {
        viewModelScope.launch {
            repository.getContactsFlow(userId)
                .catch { e ->
                    _errorMessage.value = "Failed to load contacts: ${e.message}"
                    _contactList.value = emptyList()
                }
                .collect { contacts ->
                    _contactList.value = contacts
                }
        }
    }

    fun addUser(
        receiverId: String,
        viewModelStoreOwner: ViewModelStoreOwner?
    ) {
        viewModelScope.launch {
            try {
                val auth = FirebaseService.auth
                val currentUserId = auth.currentUser?.uid ?: run {
                    _errorMessage.value = "Please login"
                    return@launch
                }

                if (receiverId.isEmpty() || receiverId == currentUserId) {
                    _errorMessage.value = "Invalid receiver ID"
                    return@launch
                }

                // Launch the lookup and add contact operation
                launch {
                    val success = repository.lookUpAndAddContact(
                        viewModelStoreOwner = viewModelStoreOwner,
                        receiverId = receiverId,
                        userId = currentUserId
                    )

                    if (success) {
                        // Reload contacts if the contact was added successfully
                        loadContacts(currentUserId)
                    } else {
                        _errorMessage.value = "Failed to add contact"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding User: ${e.message}"
            }
        }

    }
    fun setError(errorMessage: String) {
        _errorMessage.value = errorMessage
    }
    fun clearError(){
        _errorMessage.value = null
    }
}

