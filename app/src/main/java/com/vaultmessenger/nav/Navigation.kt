package com.vaultmessenger.nav

import NotificationsViewModel
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vaultmessenger.interfaces.MessageStorage
import com.vaultmessenger.interfaces.RemoteMessageStorage
import com.vaultmessenger.modules.ChatRepository
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.ConversationRepository
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.ui.ChatScreen
import com.vaultmessenger.ui.ContactScreen
import com.vaultmessenger.ui.ConversationList
import com.vaultmessenger.ui.ProfileScreen
import com.vaultmessenger.ui.RecordingScreen
import com.vaultmessenger.ui.SignInScreen
import com.vaultmessenger.ui.SplashScreen
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ChatViewModelFactory
import com.vaultmessenger.viewModel.ContactsViewModel
import com.vaultmessenger.viewModel.ContactsViewModelFactory
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ConversationViewModelFactory
import com.vaultmessenger.viewModel.NotificationsViewModelFactory
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import com.vaultmessenger.viewModel.ProvideViewModels
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun Navigation() {
    val navController = rememberNavController()

    val context: Context = LocalContext.current

    // Use viewModel() lazily so that it only creates instances when needed
    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(NotificationRepository())
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(FirebaseUserRepository())
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(ConversationRepository())
    )
    val (chatViewModel, userViewModel) = ProvideViewModels(context)

    val repository = ContactRepository() // Create an instance of the repository
    val contactsViewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(repository)
    )


    NavHost(navController = navController, startDestination = "splash") {
        composable("recording"){
            RecordingScreen()
        }
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("sign_in") {
                    // Clear the back stack to prevent navigating back to splash screen
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable(route = "sign_in") {
            SignInScreen(navController)
        }
        composable(route = "main") {
            ConversationList(
                navController = navController,
                profileViewModel = profileViewModel,
                conversationViewModel = conversationViewModel
            )
        }
        composable(route = "Profile") {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable(route = "contacts") {
            ContactScreen(
                navController = navController,
                contactsViewModel = contactsViewModel,
                profileViewModel = profileViewModel
            )
        }
        composable(
            route = "Chat/{senderUID}/{receiverUID}",
            arguments = listOf(
                navArgument("senderUID") { type = NavType.StringType },
                navArgument("receiverUID") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val senderUID = backStackEntry.arguments?.getString("senderUID")
            val receiverUID = backStackEntry.arguments?.getString("receiverUID")

            val receiverUserRepository = ReceiverUserRepository(receiverUID)
            val receiverViewModelFactory = ReceiverUserViewModelFactory(receiverUserRepository)
            val receiverUserViewModel = viewModel<ReceiverUserViewModel>(factory = receiverViewModelFactory)
            val receiverUser by receiverUserViewModel.receiverUser.collectAsState()

            val listState = rememberLazyListState()

            LaunchedEffect(key1 = senderUID, key2 = receiverUID) {
                if (senderUID != null && receiverUID != null) {
                    chatViewModel.viewModelScope.launch {
                        chatViewModel.getMessages(senderUid = senderUID, receiverUID)
                    }
                }
            }

            if (senderUID != null && receiverUID != null) {
                ChatScreen(
                    senderUID = senderUID,
                    receiverUID = receiverUID,
                    navController = navController,
                    notificationsViewModel = notificationsViewModel,
                    profileViewModel = profileViewModel,
                    conversationViewModel = conversationViewModel,
                    receiverUser = receiverUser,
                    chatViewModel = chatViewModel,
                    listState = listState,
                    context = context
                )
            } else {
                // Handle the error case if senderUID or receiverUID is null
            }
        }
    }
}
