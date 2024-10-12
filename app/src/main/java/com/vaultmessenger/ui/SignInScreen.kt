package com.vaultmessenger.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.vaultmessenger.R
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.provideViewModels
import com.vaultmessenger.viewModel.SignInViewModel

@Composable
fun SignInScreen(
    navController: NavHostController,
    onLoginSuccess: (String) -> Unit
) {
    val auth = FirebaseService.auth
    var userAuth by remember { mutableStateOf(auth.currentUser) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoginMode by remember { mutableStateOf(true) } // Toggle between login and sign-up modes
    val context: Context = LocalContext.current
    val userId = if(userAuth?.uid != null){
        userAuth?.uid
    }else{
        "guest"
    }

    // ViewModels
    val (_, _, _, _, _, _, _, _, _, signInViewModel: SignInViewModel) = provideViewModels(
        context = context,
        senderUID = "guest"
    )

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    SideEffect {
        if (userId != "guest") {
            val sharedPref = context.getSharedPreferences("Profile", MODE_PRIVATE)
            val isNewProfile = sharedPref.getInt(context.getString(R.string.saved_profile_key), 0)  // Retrieve profile key, default to 0 if not set

            // Ensure the email is verified
            if (userAuth?.isEmailVerified == true) {
                if (isNewProfile != 0) {
                    // Navigate to the main screen if the email is verified and profile is not new
                    navController.navigate("main") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            } else {
                errorMessage = "Please verify your email before logging in."
            }
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

                Text(
                    text = if (isLoginMode) "Login to your account" else "Create a new account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Spacer(Modifier.height(24.dp))

                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF5449),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(16.dp))
                }

                if(isLoading){
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }

                Button(
                    onClick = {
                        isLoading = true
                        if (isLoginMode) {
                            handleFirebaseSignIn(
                                firebaseAuth = auth,
                                email = email,
                                password = password,
                                onLoginSuccess = onLoginSuccess,
                                onError = {
                                    error -> errorMessage = error
                                    isLoading = false
                                }
                            )

                        } else {
                            handleFirebaseSignUp(
                                firebaseAuth = auth,
                                email = email,
                                password = password,
                                onSignUpSuccess = {
                                    isLoginMode = true
                                    isLoading = false
                                },
                                onError = {
                                    error -> errorMessage = error
                                    isLoading = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF435E91)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isLoginMode) "Login" else "Sign Up", color = Color.White)
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                    }
                ) {
                    Text(
                        text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login",
                        color = Color(0xFF435E91)
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (isLoginMode) {
                    TextButton(
                        onClick = {
                            // Navigate to password reset or other actions if needed
                        }
                    ) {
                        Text(text = "Forgot Password?", color = Color(0xFF435E91))
                    }
                }
            }
        }
    }
}

private fun handleFirebaseSignIn(
    firebaseAuth: FirebaseAuth,
    email: String,
    password: String,
    onLoginSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    when {
                        user == null -> onError("User not found. Please register.")
                        !user.isEmailVerified -> onError("Please verify your email before logging in.")
                        user.isAnonymous -> onError("Anonymous accounts cannot log in. Please register.")
                        else -> onLoginSuccess(user.uid) // User is authenticated and email is verified
                    }
                } else {
                    // Check for specific error codes and provide user-friendly messages
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email. Please register."
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                        is FirebaseAuthUserCollisionException -> "An account already exists with this email. Please log in."
                        is FirebaseAuthActionCodeException -> "Please check your verification email."
                        is FirebaseAuthException -> {
                            // Handle generic FirebaseAuthException (including too many requests)
                            "Too many login attempts. Please try again later."
                        }
                        else -> "Login failed. Try again."
                    }
                    onError(errorMessage)
                }
            }
    } else {
        onError("Please enter both email and password.")
    }
}


private fun handleFirebaseSignUp(
    firebaseAuth: FirebaseAuth,
    email: String,
    password: String,
    onSignUpSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            // Email sent successfully
                            onSignUpSuccess(user.uid)  // Sign-up successful, return the UID

                        } else {
                            // Failed to send verification email
                            onError("Failed to send verification email.")
                        }
                    }
                } else {
                    onError(task.exception?.message ?: "Sign-up failed. Try again.")
                }
            }
    } else {
        onError("Please enter both email and password.")
    }
}



