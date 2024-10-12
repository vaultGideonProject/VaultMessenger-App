package com.vaultmessenger.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ErrorsViewModel : ViewModel() {
    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    fun setError(errorMessage: String) {
        _errorMessage.postValue(errorMessage)
    }
    fun clearError(){
        _errorMessage.postValue("")
    }
}