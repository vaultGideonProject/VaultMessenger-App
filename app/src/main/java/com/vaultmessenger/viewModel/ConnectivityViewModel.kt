package com.vaultmessenger.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.vaultmessenger.modules.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

class ConnectivityViewModel : ViewModel() {
    private val _isConnected = MutableLiveData(true)
    val isConnected: LiveData<Boolean> = _isConnected

    private val firestore = FirebaseService.firestore
    private val auth = FirebaseService.auth
   private val storage = FirebaseService.storage
    private val functions = FirebaseService.functions

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val online = isOnline()
                _isConnected.postValue(online)
                handleFirebaseConnectivity(online)
                delay(5000)
            }
        }
    }

    private fun handleFirebaseConnectivity(isConnected: Boolean) {
        if (isConnected) {

            firestore.enableNetwork().addOnCompleteListener {
                // Handle any actions after enabling network
            }
        } else {
            firestore.disableNetwork().addOnCompleteListener {
                // Handle any actions after disabling network
            }
        }
    }

    private fun isOnline(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 60500)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
