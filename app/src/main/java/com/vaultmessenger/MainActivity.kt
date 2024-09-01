package com.vaultmessenger
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.nav.Navigation
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import android.os.Build
import androidx.annotation.RequiresApi
import com.vaultmessenger.notifications.SetPermissions
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.ui.item.ConversationItem
import com.vaultmessenger.ui.item.UserProfile
import java.util.UUID


class MainActivity : ComponentActivity() {
    private val userRepository by lazy { FirebaseUserRepository() }
    private val userViewModel by lazy {
        ViewModelProvider(this, ProfileViewModelFactory(userRepository)).get(ProfileViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchConfigs =  LaunchConfigs()

        if(BuildConfig.DEBUG){
            try {
                Firebase.initialize(context = this)
                Firebase.appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance(),
                )
            }catch (e:Exception){
                Log.d("AppCheck", e.toString())
            }
        }else{
            try {
                Firebase.initialize(context = this)
                Firebase.appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance(),
                )
            }catch (e:Exception){
                Log.d("AppCheck", e.toString())
            }
        }

        FirebaseService.initialize(this, useEmulators = launchConfigs.getEnv())

        // Access Firestore
        val firestore = FirebaseService.firestore

        val setPermissions = SetPermissions(this)

        setPermissions.checkAndRequestNotificationPermission()
        setPermissions.createNotificationChannel()

        val db = firestore

        val settings = firestoreSettings {
            // Use memory cache
            setLocalCacheSettings(memoryCacheSettings {})
            // Use persistent disk cache (default)
            setLocalCacheSettings(persistentCacheSettings {})
        }
        db.firestoreSettings = settings

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result
                val userId = FirebaseService.auth.currentUser?.uid
                if (userId != null && newToken != null) {
                    userViewModel.updateUserToken(userId, newToken)
                }
            } else {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
            }
        }

        enableEdgeToEdge()
        setContent {

            VaultmessengerTheme {
                Navigation()
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            // Update the user's status to "offline" when the activity is stopped
            userViewModel.updateOnlineStatus(userId, "online")
        } else {
            // Handle the case where userId is null, maybe log an error or show a message
            Log.e("MainActivity", "User ID is null, cannot update online status.")
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            // Update the user's status to "offline" when the activity is stopped
            userViewModel.updateOnlineStatus(userId, "online")
        } else {
            // Handle the case where userId is null, maybe log an error or show a message
            Log.e("MainActivity", "onResume | User ID is null, cannot update online status.")
        }
    }

    override fun onStop() {
        super.onStop()
        val getFireStoreInstance = FirebaseService.firestore
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            // Update the user's status to "offline" when the activity is stopped
            userViewModel.updateOnlineStatus(userId, "offline")

        } else {
            // Handle the case where userId is null, maybe log an error or show a message
            Log.e("MainActivity", "onStop | User ID is null, cannot update online status.")
        }
       // getFireStoreInstance.terminate()
    }

    override fun onPause() {
        super.onPause()

        val getFireStoreInstance = FirebaseService.firestore
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            // Update the user's status to "offline" when the activity is stopped
            userViewModel.updateOnlineStatus(userId, "offline")

        } else {
            // Handle the case where userId is null, maybe log an error or show a message
            Log.e("MainActivity", "onStop | User ID is null, cannot update online status.")
        }
      //  getFireStoreInstance.terminate()
    }

    override fun onDestroy() {
        super.onDestroy()

        val getFireStoreInstance = FirebaseService.firestore
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            // Update the user's status to "offline" when the activity is stopped
            userViewModel.updateOnlineStatus(userId, "offline")

        } else {
            // Handle the case where userId is null, maybe log an error or show a message
            Log.e("MainActivity", "onStop | User ID is null, cannot update online status.")
        }
      //  getFireStoreInstance.terminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        Log.d("Low Memory", "Low Memory Warning on device")

        val getFireStoreInstance = FirebaseService.firestore

        getFireStoreInstance.terminate()

        // Schedule to reconnect after 5 minutes
        Handler(Looper.getMainLooper()).postDelayed({
            // Re-enable Firestore network access
            getFireStoreInstance.run {
            }

        }, 5 * 60 * 1000) // 5 minutes in milliseconds
    }

}

@Composable
fun DrawerContent(user: User?, navController: NavHostController) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(vertical = 30.dp, horizontal = 10.dp)
        ) {
            UserProfile(user)
            Spacer(modifier = Modifier.height(40.dp))
            SettingsRow()
            Spacer(modifier = Modifier.height(10.dp))
            ProfileRow()
            SignOutButton(navController)
        }
    }
}

@Composable
fun ProfileIcon(user: User?) {
    if (user?.profilePictureUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(user.profilePictureUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
    }
}

@Composable
fun SettingsRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings Icon",
            tint = Color(0xFF565E71),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SETTINGS",
            color = Color(0xFF565E71),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile Icon",
            tint = Color(0xFF565E71),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "PROFILE",
            color = Color(0xFF565E71),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileImage(userPhotoUrl: String?, modifier: Modifier) {
    if (userPhotoUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(userPhotoUrl),
            contentDescription = "Profile Picture",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = modifier,
            tint = Color.Gray
        )
    }
}

@Composable
fun SignOutButton(navController: NavHostController) {
    // State to trigger the sign-out effect
    var signOutRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // Access FirebaseAuth
    val auth = FirebaseService.auth

// Access Firestore
    val firestore = FirebaseService.firestore

// Access FirebaseStorage
    val storage = FirebaseService.storage


    // Trigger sign-out effect when signOutRequested is true
    LaunchedEffect(signOutRequested) {
        if (signOutRequested) {
            isLoading = true

            coroutineScope.launch {
                snackbarHostState.showSnackbar("Signing you out...")
            }
            // Delay to allow Snackbar to be visible
            delay(2500)

            // Sign out from Firebase
            auth.signOut()

            // Navigate to the sign-in screen
            navController.navigate("sign_in") {
                // Clear the back stack to prevent the user from navigating back to the previous screen
                popUpTo("main") { inclusive = true }
            }

            // Reset the state to prevent repeated triggers
            signOutRequested = false
            isLoading = false
        }
    }

    // Sign out button
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable {
                signOutRequested = true
            }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Sign Out",
            tint = Color(0xFFBA1A1A),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SIGN OUT",
            color = Color(0xFFBA1A1A),
            fontWeight = FontWeight.Bold
        )
    }

    // Show a dialog with a progress indicator when signing out
    if (isLoading) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SignOutAnimation()
                }
            },
            text = { },
            buttons = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            backgroundColor = Color.White,
            contentColor = Color.Black
        )
    }


}
@Composable
fun SignOutAnimation() {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .alpha(alpha) // Apply alpha animation here
            .scale(scale) // Apply scale animation here
    ) {
        Text(
            "Please wait",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "We are signing you out...",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
fun randomNumberGen(): UUID? {
    return UUID.randomUUID()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {


    // Providing default values for CompositionLocal providers
        VaultmessengerTheme {
          //  Navigation()
        }
}

