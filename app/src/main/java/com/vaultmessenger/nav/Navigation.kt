package com.vaultmessenger.nav

import android.content.Context
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vaultmessenger.ui.ChatScreen
import com.vaultmessenger.ui.ContactScreen
import com.vaultmessenger.ui.ConversationList
import com.vaultmessenger.ui.ProfileScreen
import com.vaultmessenger.ui.SignInScreen
import com.vaultmessenger.ui.SplashScreen
import com.vaultmessenger.viewModel.ProvideViewModels

@Composable
fun Navigation() {
    val navController = rememberNavController()

  val context: Context = LocalContext.current

    val (
      //  chatViewModel,
        _,
        profileViewModel,
        notificationsViewModel,
        conversationViewModel,
        contactsViewModel,
        _,
        connectivityViewModel,
        errorsViewModel,
        voiceNoteViewModel,
        ) = ProvideViewModels(context, senderUID = "null")

    NavHost(navController = navController, startDestination = "sign_in") {


        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("sign_in") {
                    // Clear the back stack to prevent navigating back to splash screen
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable(route = "sign_in") {
            SignInScreen(navController, profileViewModel)
        }
        composable(route = "main") {
            ConversationList(
                navController = navController,
                profileViewModel = profileViewModel,
                conversationViewModel = conversationViewModel,
                connectivityViewModel = connectivityViewModel,
                errorsViewModel = errorsViewModel,
            )
        }
        composable(route = "Profile") {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                errorsViewModel = errorsViewModel
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

            // Destructure the view models, ensuring all needed ones are captured
            val (_, _, _, _, _, receiverUserViewModel) = ProvideViewModels(
                receiverUID = receiverUID,
                context = context,
                senderUID =  "guest"
            )

            // Collect the receiver user state
            val receiverUser by receiverUserViewModel.receiverUser.collectAsState(initial = null)
            val profileUser by profileViewModel.user.collectAsState(initial = null)

            // Show a loading indicator while waiting for receiverUser to load
            if (receiverUser == null) {
                // You can show a loading UI here
                // e.g., a CircularProgressIndicator
                CircularProgressIndicator()
            } else if (senderUID != null && receiverUID != null) {


                // Proceed to display the ChatScreen once the receiverUser is loaded
                receiverUser?.let { user ->
                    ChatScreen(
                        senderUID = senderUID,
                        receiverUID = receiverUID,
                        navController = navController,
                        notificationsViewModel = notificationsViewModel,
                        user = profileUser,
                        conversationViewModel = conversationViewModel,
                        receiverUser = user,
                        receiverUserViewModel = receiverUserViewModel,
                        profileViewModel = profileViewModel,
                        errorsViewModel = errorsViewModel,
                        voiceNoteViewModel = voiceNoteViewModel
                    )
                }
            } else {
                errorsViewModel.setError("Failed to Open Chats")
            }
        }

    }
}
