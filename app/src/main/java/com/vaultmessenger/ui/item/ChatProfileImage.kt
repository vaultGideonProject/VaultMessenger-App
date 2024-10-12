package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.R
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.remoteImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ChatProfileImage(
    localMessage: LocalMessage,
    receiverUID:String,
    receiverUser: ReceiverUser,
    userList:LocalUser?
){
    val profilePictureUrl = if (localMessage.userId1 == receiverUID) {
        receiverUser.profilePictureUrl
    } else {
        userList?.profilePictureUrl
    }

    val context = LocalContext.current

    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(profilePictureUrl) {
        withContext(Dispatchers.IO) {
            // Log the URL for debugging

            if (!profilePictureUrl.isNullOrBlank() &&
                (profilePictureUrl.startsWith("http://") || profilePictureUrl.startsWith("https://"))) {
                // If the URL is valid, load the image
                imageFile = remoteImage(context, profilePictureUrl)
            } else {
                // Handle empty or invalid URL (e.g., use a placeholder image)
                println("URL is either empty or invalid: $profilePictureUrl")
                // Set a placeholder or default image here
            }
        }
    }
 if(imageFile != null) {
     Image(
         painter = rememberAsyncImagePainter(profilePictureUrl),
         contentDescription = "Profile Picture",
         modifier = Modifier
             .size(48.dp)
             .clip(CircleShape),
         contentScale = ContentScale.Crop
     )
 }else {
     // Show placeholder while loading
     Image(
         painter = painterResource(R.drawable.ic_account_circle_foreground),  // Placeholder image
         contentDescription = "Profile Image",
         modifier = Modifier
             .size(88.dp)
             .clip(CircleShape),
         contentScale = ContentScale.Crop
     )
 }
}