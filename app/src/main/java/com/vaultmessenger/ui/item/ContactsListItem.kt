package com.vaultmessenger.ui.item

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.R
import com.vaultmessenger.nav.rememberVideocam
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.modules.asyncImage
import com.vaultmessenger.modules.remoteImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun ContactsItem(
    navController: NavController,
    itemIndex: Int,
    contactProfilePhoto: List<String>,
    contactName: List<String>,
    contactEmail: List<String>,
    contactNumber: List<String>,
    contactUID: List<String>,
    modifier: Modifier = Modifier,
    context:Context
) {
    // Access FirebaseAuth
    val auth = FirebaseService.auth

// Access Firestore
    val firestore = FirebaseService.firestore

// Access FirebaseStorage
    val storage = FirebaseService.storage

    val senderUID = auth.currentUser?.uid
    val receiverUID = contactUID[itemIndex]

    val itemCount = contactProfilePhoto.size
    var imageFiles by remember { mutableStateOf(List<File?>(itemCount) { null }) }

    LaunchedEffect(contactProfilePhoto[itemIndex]) {
        if (itemIndex < imageFiles.size) {
            withContext(Dispatchers.IO) {
                val profileUrl = contactProfilePhoto[itemIndex]
                // Log the URL for debugging
                println("Profile URL: $profileUrl")

                if (!profileUrl.isNullOrBlank() &&
                    (profileUrl.startsWith("http://") || profileUrl.startsWith("https://"))) {
                    // If the URL is valid, load the image
                    val loadedImage = remoteImage(context, profileUrl)

                    // Update the state safely
                    imageFiles = imageFiles.toMutableList().apply {
                        this[itemIndex] = loadedImage
                    }
                } else {
                    // Handle empty or invalid URL (e.g., use a placeholder image)
                    println("URL is either empty or invalid: $profileUrl")
                    // Set a placeholder image if necessary
                }
            }
        }
    }



    Card(
        modifier = modifier
            .clickable(onClick = {
            //    Log.d("Contact Item", contactName[itemIndex] + " selected!")
                navController.navigate("Chat/${senderUID}/${receiverUID}")
            })
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .wrapContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE6E6E7),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (imageFiles[itemIndex] != null) {
                // Use the locally downloaded image
                Image(
                    painter = rememberAsyncImagePainter(imageFiles[itemIndex]),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .size(75.dp)
                        .clip(CircleShape),
                    //  .clickable { },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Show placeholder while loading
                Image(
                    painter = painterResource(R.drawable.ic_stat_name),  // Placeholder image
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape),
                    // .clickable { },
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(9.dp)) {
                Text(
                    text = contactName[itemIndex],
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF264273)
                )
                Text(
                    text = contactEmail[itemIndex],
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF264273)
                )
                Text(
                    text = contactNumber[itemIndex],
                    fontSize = 16.sp,
                    color = Color(0xFF3B4355)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    modifier = Modifier
                        .padding(vertical = 29.dp, horizontal = 5.dp)
                        .clickable(onClick = {
                      //      Log.d(
                       //         "Call Item",
                      //          contactName[itemIndex] + " Called!"
                       //     )
                        }),
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call contact",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF0D62CA))
                )
                Image(
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable(onClick = {  }),
                    imageVector = rememberVideocam(),
                    contentDescription = "Video call",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF0D62CA))
                )
                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun ContactsListItem(
    navController: NavController,
    contactProfilePhoto: List<String>,
    contactName: List<String>,
    contactEmail:List<String>,
    contactNumber: List<String>,
    contactUID: List<String>,
    modifier: Modifier = Modifier,
    context: Context,
) {
    VaultmessengerTheme {
        LazyColumn(
            modifier.padding(vertical = 80.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(contactProfilePhoto.size) { itemIndex ->
                ContactsItem(
                    navController = navController,
                    itemIndex = itemIndex,
                    contactProfilePhoto = contactProfilePhoto,
                    contactName = contactName,
                    contactNumber = contactNumber,
                    contactEmail = contactEmail,
                    contactUID = contactUID,
                    modifier = modifier,
                    context = context,
                )
            }
        }
    }
}
