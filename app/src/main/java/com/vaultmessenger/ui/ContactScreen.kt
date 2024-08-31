package com.vaultmessenger.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.vaultmessenger.R
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.nav.ContactsToolbar
import com.vaultmessenger.nav.addNewContact
import com.vaultmessenger.ui.item.ContactsListItem
import com.vaultmessenger.viewModel.ContactsViewModel
import com.vaultmessenger.viewModel.ContactsViewModelFactory
import com.vaultmessenger.viewModel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ContactScreen(
    navController: NavHostController,
    contactsViewModel: ContactsViewModel,
    profileViewModel: ProfileViewModel
) {
    val contactList by contactsViewModel.contactList.collectAsState()
    val userId = FirebaseService.auth.currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val errorMessage by contactsViewModel.errorMessage.collectAsState()
    val currentErrorMessage by rememberUpdatedState(errorMessage)
    val scope = rememberCoroutineScope()


    // Use SideEffect to respond to state changes
    SideEffect {
        currentErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            contactsViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        val launchConfigs = LaunchConfigs()
        launchConfigs.defaults(
            userViewModel = profileViewModel,
            navController = navController
        ).run {
            // Your logic here, executed only once when the composable enters the composition
        }
    }

    // Replace "userId" with the actual userId you are working with
    LaunchedEffect(Unit) {
        contactsViewModel.loadContacts(userId!!)
    }

    Scaffold(
        topBar = { ContactsToolbar(navController = navController) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { padding ->

            Surface(
                modifier = Modifier.padding(padding),
            ) {
                addNewContact(
                    contactsViewModel = contactsViewModel,
                    viewModelStoreOwner = viewModelStoreOwner!!

                )
                ContactsListItem(
                    navController = navController,
                    contactProfilePhoto = contactList.map { it.uid.toIntOrNull() ?: R.drawable.ic_account_circle_foreground }, // Replace with your logic
                    contactName = contactList.map { it.name },
                    contactNumber = contactList.map { it.number },
                    contactUID = contactList.map { it.uid },
                    modifier = Modifier.padding(vertical = 1.dp)
                )

            }
        }
    )
}
