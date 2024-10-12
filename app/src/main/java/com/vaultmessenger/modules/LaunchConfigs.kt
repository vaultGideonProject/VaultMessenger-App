package com.vaultmessenger.modules

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vaultmessenger.BuildConfig
import com.vaultmessenger.viewModel.ProfileViewModel
import kotlinx.coroutines.launch

class LaunchConfigs {
    // set pointing of app to online or local
    private val isLocal = false

   private val setLocalEnv = if(isLocal){
       setEnv(isLocal)
   }else{
       false
   }

    fun getEnv():Boolean{
        return setLocalEnv
    }

    fun defaults(
        userViewModel: ProfileViewModel,
        navController: NavController,
        userId: String? = FirebaseService.auth.currentUser?.uid
    ) {
        // Ensure the userId is not null before proceeding
        userId?.let {
            userViewModel.viewModelScope.launch {
                try {
                    userViewModel.isUserAccLocked(navController)
                    userViewModel.refreshUser()

                } catch (e: Exception) {
                    // Handle exceptions during monitoring and account locking
                    e.printStackTrace()
                    // You might want to navigate to an error screen or show a message
                }
            }
        } ?: run {
            // Handle the case where userId is null
            return@run // Navigate to login or any other appropriate screen
        }
    }
    private fun setEnv(setLocal:Boolean): Boolean{
        return try {
            if(setLocal){
                BuildConfig.DEBUG
            }else{
                false
            }
        }catch (e:Exception){
            false
        }
    }
}
