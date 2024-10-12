package com.vaultmessenger.nav

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vaultmessenger.R
import com.vaultmessenger.gemini.GeminiChatScreen
import com.vaultmessenger.gemini.GeminiViewModel
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.ui.ChatScreen
import com.vaultmessenger.ui.ContactScreen
import com.vaultmessenger.ui.ConversationList
import com.vaultmessenger.ui.ProfileScreen
import com.vaultmessenger.ui.SignInScreen
import com.vaultmessenger.ui.SplashScreen
import com.vaultmessenger.viewModel.provideViewModels

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val navController = rememberNavController()

  val context: Context = LocalContext.current

    val (
      //  chatViewModel,
        chatViewModel,
        profileViewModel,
        notificationsViewModel,
        conversationViewModel,
        contactsViewModel,
        _,
        connectivityViewModel,
        errorsViewModel,
        voiceNoteViewModel,
        ) = provideViewModels(context, senderUID = "null")

    NavHost(navController = navController, startDestination = "splash") {
        val geminiViewModel = GeminiViewModel()

        composable("gemini") {
            GeminiChatScreen(geminiViewModel)
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
            SignInScreen(navController) { uid ->
                val user = FirebaseService.auth.currentUser
                val profileKey = context.getString(R.string.saved_profile_key)
                val defaultValue = 0

                val sharedPref = context.getSharedPreferences(profileKey, Context.MODE_PRIVATE)
                val isNewProfile = sharedPref?.getInt(profileKey, defaultValue)

                // Check if the current user is the same as the logged-in user
                if (user != null && uid == FirebaseService.auth.uid) {
                    // Ensure the email is verified
                    if (user.isEmailVerified) {
                        // Navigate to the main screen or profile setup based on the profile state
                        if (isNewProfile == 0) {
                            navController.navigate("profile/firstLogin") {
                                // Clear the back stack so that the user cannot navigate back to the sign-in screen
                                popUpTo("sign_in") { inclusive = true }
                            }
                        } else {
                            navController.navigate("main") {
                                // Clear the back stack so that the user cannot navigate back to the sign-in screen
                                popUpTo("sign_in") { inclusive = true }
                            }
                        }
                    } else {
                        // Show an error and offer to resend the verification email
                        errorsViewModel.setError("Please verify your email before logging in. Would you like to resend the verification email?")
                        user.sendEmailVerification().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Inform the user that the verification email has been resent
                                errorsViewModel.setError("Verification email has been sent. Please check your inbox.")
                            } else {
                                // Handle error if the email could not be sent
                                errorsViewModel.setError("Failed to resend verification email. Please try again later.")
                            }
                        }
                    }
                } else {
                    // Handle case where the user is not logged in or the user object is null
                    errorsViewModel.setError("Error: User is not authenticated. Please sign in again.")
                }
            }

        }

        composable(route = "main") {
            ConversationList(
                navController = navController,
                profileViewModel = profileViewModel,
                conversationViewModel = conversationViewModel,
                chatViewModel = chatViewModel,
                connectivityViewModel = connectivityViewModel,
                errorsViewModel = errorsViewModel,
            )
        }
        composable(
            route = "profile/{appContext}",
            arguments = listOf(
                navArgument("appContext") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Retrieve the appContext argument from the backStackEntry
            val appContext = backStackEntry.arguments?.getString("appContext") ?: "defaultContext"

            // Pass the arguments to your ProfileScreen Composable
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                errorsViewModel = errorsViewModel,
                appContext = appContext // Pass the retrieved appContext here
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
            val (_, _, _, _, _, receiverUserViewModel) = provideViewModels(
                receiverUID = receiverUID,
                context = context,
                senderUID =  "guest"
            )

            // Collect the receiver user state
            val receiverUser by receiverUserViewModel.receiverUser.collectAsState(initial = null)
            val profileUser by profileViewModel.user.collectAsState(initial = null)

            // Show a loading indicator while waiting for receiverUser to load
                if (receiverUser == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(), // Fills the available screen space
                        contentAlignment = Alignment.Center // Centers the content inside the Box
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp) // Size of the progress indicator
                        )
                    }
                }
             else if (senderUID != null && receiverUID != null) {


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
