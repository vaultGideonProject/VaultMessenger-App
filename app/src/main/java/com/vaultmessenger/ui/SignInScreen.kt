package com.vaultmessenger.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vaultmessenger.R
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navController: NavHostController) {
    val auth = FirebaseService.auth
    var userAuth by remember { mutableStateOf(auth.currentUser) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = FirebaseUserRepository()
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))
    val user by viewModel.user.collectAsState()
    var retryAttempts by remember { mutableIntStateOf(0) }


    // Launch effect to fetch user data and retry if needed
    LaunchedEffect(Unit) {
        retryAttempts = 0
        while (retryAttempts < 5) {
            try {
                viewModel.user
                break // Exit the loop if successful
            } catch (e: Exception) {
                retryAttempts++
                if (retryAttempts >= 5) {
                    errorMessage = "Failed to fetch user data after multiple attempts."
                }
                // Optional: Delay before retrying
                delay(20)
            }
        }
        isLoading = false
    }

    // Launch effect to navigate based on user state
    LaunchedEffect(user) {
        if (user != null && userAuth != null) {
            viewModel.refreshUser()
            navController.navigate("main")
        }
    }

    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = { result ->
            userAuth = result.user
            isLoading = false
            // Fetch user data and update state
            viewModel.refreshUser()
            navController.navigate("main")
        },
        onAuthError = { exception ->
            userAuth = null
            isLoading = false
            errorMessage = exception.message
        }
    )
    val token = stringResource(R.string.default_web_client_id)
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_chat_24), // Replace with your app logo resource
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = if(user == null && userAuth == null){
                        "Login to your account"
                    }else{"Loading your account.."},
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Spacer(Modifier.height(24.dp))

                //Load to next screen effects
                if (isLoading) {
                    CircularProgressIndicator( color = Color(0xFF435E91))
                } else {
                    if(user == null && userAuth == null){
                        Button(
                            onClick = {
                                isLoading = true
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(token)
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                launcher.launch(googleSignInClient.signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF435E91) // Background color
                            ),
                            modifier = Modifier.padding(padding)
                        ) {
                            Text(
                                text = "Continue with Google",
                                color = Color.White,
                            )
                        }
                    }else{
                        viewModel.refreshUser()
                        navController.navigate("main")
                    }
                }
            }
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (Exception) -> Unit,
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                try {
                    val authResult = Firebase.auth.signInWithCredential(credential).await()
                    onAuthComplete(authResult)
                } catch (e: FirebaseAuthException) {
                    onAuthError(e)
                } catch (e: Exception) {
                    onAuthError(e)
                }
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }
    }
}
