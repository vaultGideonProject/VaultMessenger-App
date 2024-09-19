package com.vaultmessenger.ui

import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vaultmessenger.R
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun SignInScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    val auth = FirebaseService.auth
    var userAuth by remember { mutableStateOf(auth.currentUser) }
    var isLoading by remember { mutableStateOf(userAuth == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val firebaseAuthLauncher = rememberFirebaseAuthLauncher(
        onAuthComplete = { authResult ->
            userAuth = authResult.user
            isLoading = false
            navController.navigate("main")
        },
        onAuthError = { error ->
            errorMessage = error.localizedMessage
            isLoading = false
        }
    )

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    SideEffect {
        if(!userAuth?.uid.isNullOrEmpty()){
            navController.navigate("main")
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
                    painter = painterResource(id = R.drawable.baseline_chat_24),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(24.dp))

                val loginText = if (!isLoading) {
                    "Loading your account..."
                } else {
                    "Login to your account"
                }

                Text(
                    text = loginText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Spacer(Modifier.height(24.dp))

                if (!isLoading) {
                    CircularProgressIndicator(color = Color(0xFF435E91))
                } else {
                    if (userAuth == null) {
                        Button(
                            onClick = {
                                isLoading = true
                                // Request credential using Credential Manager
                                val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()

                                val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                                val signInIntent = googleSignInClient.signInIntent

                                firebaseAuthLauncher.launch(signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF435E91)),
                            modifier = Modifier.padding(padding)
                        ) {
                            Text(text = "Continue with Google", color = Color.White)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (Exception) -> Unit
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
