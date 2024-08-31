package com.vaultmessenger.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.modules.Encoder
import com.vaultmessenger.nav.ProfileToolbar
import com.vaultmessenger.ui.item.ProfileTextField
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.LaunchConfigs
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
    ) {


    LaunchedEffect(Unit) {
        val launchConfigs = LaunchConfigs()
        launchConfigs.defaults(
            userViewModel = profileViewModel,
            navController = navController
        ).run {
            // Your logic here, executed only once when the composable enters the composition
        }
    }

    val viewModel: ProfileViewModel = profileViewModel
    val user by viewModel.user.collectAsState()
    var fullName by remember {
        mutableStateOf(user?.userName.orEmpty())
    }
    var nickName by remember {
        mutableStateOf(user?.nickName.orEmpty())
    }
    var dob by remember {
        mutableStateOf(user?.dob.orEmpty())
    }
    var email by remember {
        mutableStateOf(user?.email.orEmpty())
    }
    var about by remember {
        mutableStateOf(user?.bio.orEmpty())
    }



    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveProfileImage(it) { downloadUrl ->
                viewModel.updateProfilePictureUrl(downloadUrl)
                user?.let { user ->
                    viewModel.saveUser(user.copy(profilePictureUrl = downloadUrl))
                }
            }
        }
    }
    val encodedUserId = Encoder.encodeWithSHA256(user?.userId)

    Scaffold(
        containerColor = Color.White,
        topBar = { ProfileToolbar(navController = navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .background(Color(0xFF435E91), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.profilePictureUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(user!!.profilePictureUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                tint = Color.White
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Profile Photo",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black, shape = CircleShape)
                                .padding(4.dp)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            tint = Color(0xFFFFFFFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Account Handle",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    if (encodedUserId != null) {
                        CopyableText(text = encodedUserId)
                    }
                }

                item {
                    user?.let {
                        ProfileTextField(label = "Full Name", value = fullName, onValueChange = { newValue -> fullName = newValue })
                    }
                }
                item { ProfileTextField(label = "Nickname", value = nickName, onValueChange = { newValue -> nickName = newValue}) }
                item {
                    ProfileTextField(
                        label = "Date of Birth",
                        value = dob,
                        onValueChange = { newValue -> dob = newValue},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    ProfileTextField(
                        label = "Email",
                        value = email,
                        onValueChange = { newValue -> email = newValue},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                item { ProfileTextField(label = "About", value = about, onValueChange = { newValue -> about = newValue}, singleLine = false) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    VaultmessengerTheme {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF435E91),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.LightGray
                            ),
                            onClick = {
                                user?.let { currentUser ->
                                    viewModel.saveUser(
                                        currentUser.copy(
                                            userName = fullName,
                                            nickName = nickName,
                                            dob = dob,
                                            email = email,
                                            bio = about
                                        )
                                    )
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Saved")
                                    }
                                }
                            }
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    )
}
fun saveProfileImage(uri: Uri, onSuccess: (String) -> Unit) {
    // Access FirebaseAuth
    val auth = FirebaseService.auth

// Access Firestore
    val firestore = FirebaseService.firestore

// Access FirebaseStorage
    val storage = FirebaseService.storage
    val getUserId = auth.currentUser!!.uid
    val storageRef = storage.reference.child("profile_images/${getUserId}/${UUID.randomUUID()}.jpg")

    val uploadTask = storageRef.putFile(uri)
    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            onSuccess(downloadUrl.toString())

        }
    }.addOnFailureListener {
        // Handle any errors
    }
}
fun copyToClipboard(context: Context?, label: String, text: String) {
    context?.let {
        val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}

@Composable
fun CopyableText(text: String) {
    val context = LocalContext.current

    Text(
        text = text,
        modifier = Modifier
            .clickable {
                copyToClipboard(context, "Account Handle", text)
                Toast
                    .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
            .padding(16.dp), // Adjust padding as needed
        overflow = TextOverflow.Ellipsis // Handle overflow if needed
    )
}

