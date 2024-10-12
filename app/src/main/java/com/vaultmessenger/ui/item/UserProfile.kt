package com.vaultmessenger.ui.item

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.R
import com.vaultmessenger.database.LocalUser
import com.vaultmessenger.model.User
import com.vaultmessenger.modules.remoteImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun UserProfile(user: LocalUser?, context: Context) {
    var imageFile by remember { mutableStateOf<File?>(null) }
    LaunchedEffect(user?.profilePictureUrl) {
        withContext(Dispatchers.IO) {
            val profileUrl = user?.profilePictureUrl
            // Log the URL for debugging
            println("Profile URL: $profileUrl")

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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageFile != null) {
            // Use the locally downloaded image
            Image(
                painter = rememberAsyncImagePainter(imageFile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show placeholder while loading
            Image(
                painter = painterResource(R.drawable.ic_account_circle_foreground),  // Placeholder image
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = user?.userName ?: "[UNKNOWN]",
            color = Color(0xFF565E71),
            fontWeight = FontWeight.Bold
        )
    }
}
