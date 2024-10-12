package com.vaultmessenger.nav


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.R
import com.vaultmessenger.modules.remoteImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbar(
    navController: NavHostController,
    receiverUID: String,
    profilePhoto: String) {
    var imageFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    LaunchedEffect(profilePhoto) {
        withContext(Dispatchers.IO) {
            // Log the URL for debugging
            println("Profile URL: $profilePhoto")

            if (!profilePhoto.isNullOrBlank() &&
                (profilePhoto.startsWith("http://") || profilePhoto.startsWith("https://"))) {
                // If the URL is valid, load the image
                imageFile = remoteImage(context, profilePhoto)
            } else {
                // Handle empty or invalid URL (e.g., use a placeholder image)
                println("URL is either empty or invalid: $profilePhoto")
                // Set a placeholder or default image here
            }
        }
    }

    TopAppBar(
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
              //  asyncImage(
                 //   model = profilePhoto, // The URL or model for the image
                 //   contentDescription = "Profile Image", // Description for accessibility
                //    placeholder = painterResource(id = R.drawable.ic_account_circle_foreground), // Placeholder while loading
                //    error = painterResource(id = R.drawable.ic_stat_name), // Error image if loading fails
               //     modifier = Modifier
             //           .size(55.dp)
             //           .clip(CircleShape)
              //          .clickable {  }, // Modifier to customize image dimensions
               //     contentScale = ContentScale.Crop, // How the image should be scaled
             //   )
                if (imageFile != null) {
                    // Use the locally downloaded image
                    Image(
                        painter = rememberAsyncImagePainter(imageFile),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape),
                        //  .clickable { },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show placeholder while loading
                    Image(
                        painter = painterResource(R.drawable.ic_account_circle_foreground),  // Placeholder image
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape),
                        // .clickable { },
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                Text(
                    text = receiverUID,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                   style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "menu items"
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.VideoCall,
                    modifier = Modifier.size(35.dp),
                    contentDescription = "video call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "phone call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "more options",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0D62CA),
            scrolledContainerColor = Color.Black,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color(0xffb3cbff)
        ),
    )
}

@Composable
fun rememberVideocam(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "videocam",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.292f, 33.083f)
                quadToRelative(-1.042f, 0f, -1.834f, -0.791f)
                quadToRelative(-0.791f, -0.792f, -0.791f, -1.834f)
                verticalLineTo(9.542f)
                quadToRelative(0f, -1.042f, 0.791f, -1.834f)
                quadToRelative(0.792f, -0.791f, 1.834f, -0.791f)
                horizontalLineToRelative(20.916f)
                quadToRelative(1.042f, 0f, 1.834f, 0.791f)
                quadToRelative(0.791f, 0.792f, 0.791f, 1.834f)
                verticalLineToRelative(8.5f)
                lineToRelative(5.375f, -5.375f)
                quadToRelative(0.292f, -0.292f, 0.73f, -0.146f)
                quadToRelative(0.437f, 0.146f, 0.437f, 0.604f)
                verticalLineToRelative(13.75f)
                quadToRelative(0f, 0.458f, -0.437f, 0.604f)
                quadToRelative(-0.438f, 0.146f, -0.73f, -0.187f)
                lineToRelative(-5.375f, -5.334f)
                verticalLineToRelative(8.5f)
                quadToRelative(0f, 1.042f, -0.791f, 1.834f)
                quadToRelative(-0.792f, 0.791f, -1.834f, 0.791f)
                close()
                moveToRelative(0f, -2.625f)
                horizontalLineToRelative(20.916f)
                verticalLineTo(9.542f)
                horizontalLineTo(6.292f)
                verticalLineToRelative(20.916f)
                close()
                moveToRelative(0f, 0f)
                verticalLineTo(9.542f)
                verticalLineToRelative(20.916f)
                close()
            }
        }.build()
    }
}