package com.vaultmessenger.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.nav.ProfileToolbar
import com.vaultmessenger.ui.item.ProfileTextField
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.LaunchConfigs
import com.vaultmessenger.viewModel.ErrorsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import com.vaultmessenger.R
import com.vaultmessenger.modules.remoteImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    errorsViewModel: ErrorsViewModel,
    appContext: String,
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

    val userReady by viewModel.userReady.collectAsState()

    if (!userReady) {
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
                viewModel.viewModelScope.launch {
                    viewModel.updateProfilePictureUrl(downloadUrl)
                }
            }
        }
    }
    val encodedUserId = user?.userId

    val safeEncodedUserId = encodedUserId ?: "Unknown User"

    val errorMessage by errorsViewModel.errorMessage.observeAsState(initial = "")
    val currentErrorMessage by rememberUpdatedState(errorMessage)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(user?.profilePictureUrl) {
        withContext(Dispatchers.IO) {
            val profileUrl = user?.profilePictureUrl
            // Log the URL for debugging

            if (!profileUrl.isNullOrBlank() &&
                (profileUrl.startsWith("http://") || profileUrl.startsWith("https://"))) {
                // If the URL is valid, load the image
                imageFile = remoteImage(context, profileUrl)
            } else {
                // Handle empty or invalid URL (e.g., use a placeholder image)
                println("URL is either empty or invalid: $profileUrl")
                // Set a placeholder or default image here
            }
        }
    }

    SideEffect {
        currentErrorMessage.takeIf { it.isNotBlank() }?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            errorsViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = { ProfileToolbar(
            navController = navController,
            appContext = appContext
            ) },
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

                        if (imageFile != null) {
                            // Use the locally downloaded image
                            Image(
                                painter = rememberAsyncImagePainter(imageFile),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(420.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show placeholder while loading
                            Image(
                                painter = painterResource(R.drawable.ic_stat_name),  // Placeholder image
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(420.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
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
                    CopyableText(
                        text = safeEncodedUserId,
                        scope = scope,
                        snackbarHostState = snackbarHostState
                    )
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
                                    viewModel.viewModelScope.launch {
                                        viewModel.saveUser(
                                            currentUser.copy(
                                                userName = fullName,
                                                nickName = nickName,
                                                dob = dob,
                                                email = email,
                                                bio = about
                                            )
                                        )
                                    }
                                   val isInputValid = validateInput(
                                        fullName = fullName,
                                        nickName = nickName,
                                        dob = dob,
                                        email = email,
                                        about = about,
                                        errorsViewModel = errorsViewModel
                                    )
                                    if (!isInputValid){
                                        return@Button
                                    }
                                    if(appContext == "firstLogin"){
                                        navController.navigate("main")
                                    }else{
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Saved")
                                        }
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
fun CopyableText(text: String, scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current

    Text(
        text = text,
        modifier = Modifier
            .clickable {
                copyToClipboard(context, "Account Handle", text)
                scope.launch {
                    snackbarHostState.showSnackbar("Hash Copied")
                }
            }
            .padding(16.dp), // Adjust padding as needed
        overflow = TextOverflow.Ellipsis // Handle overflow if needed
    )
}
@RequiresApi(Build.VERSION_CODES.O)
fun validateInput(
    fullName: String?,
    nickName: String?,
    dob: String?,
    email: String?,
    about: String?,
    errorsViewModel: ErrorsViewModel
): Boolean {

    // Full name validation (must be two words: name and surname)
    if (!isValidFullName(fullName)) {
        errorsViewModel.setError("Full name must include both first name and surname.")
        return false
    }

    // Nickname validation (must be one word and max 10 characters)
    if (!isValidNickName(nickName)) {
        errorsViewModel.setError("Nickname must be one word and no more than 10 characters.")
        return false
    }

    // Date of birth validation (must be 16 years or older)
    if (!isValidDob(dob)) {
        errorsViewModel.setError("You must be at least 16 years old. Pattern yyyy-MM-dd")
        return false
    }

    // Email validation (must be non-null and valid format)
    if (email.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        errorsViewModel.setError("Please provide a valid email address.")
        return false
    }

    // About validation (must be at least 15 characters)
    if (about.isNullOrBlank() || about.length < 15) {
        errorsViewModel.setError("About section must be at least 15 characters long.")
        return false
    }

    // Validation successful
    return true
}
// Full name validation (name and surname only)
fun isValidFullName(fullName: String?): Boolean {
    if (fullName.isNullOrBlank()) return false
    val nameParts = fullName.trim().split("\\s+".toRegex())
    return nameParts.size == 2
}

// Nickname validation (one word, max 10 characters)
fun isValidNickName(nickName: String?): Boolean {
    if (nickName.isNullOrBlank()) return false
    return nickName.length <= 10 && !nickName.contains("\\s".toRegex())
}

// Date of birth validation (must be at least 16 years old)
@RequiresApi(Build.VERSION_CODES.O)
fun isValidDob(dob: String?): Boolean {
    if (dob.isNullOrBlank()) return false
    try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthDate = java.time.LocalDate.parse(dob, formatter)
        val today = java.time.LocalDate.now()
        val age = java.time.Period.between(birthDate, today).years
        return age >= 16
    } catch (e: Exception) {
        return false
    }
}


