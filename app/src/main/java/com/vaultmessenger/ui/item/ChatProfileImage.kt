package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.database.LocalMessage
import com.vaultmessenger.model.Message
import com.vaultmessenger.model.ReceiverUser
import com.vaultmessenger.model.User

@Composable
fun ChatProfileImage(
    localMessage: LocalMessage,
    receiverUID:String,
    receiverUser: ReceiverUser,
    userList:User?
){
    val profilePictureUrl = if (localMessage.userId1 == receiverUID) {
        receiverUser.profilePictureUrl
    } else {
        userList?.profilePictureUrl
    }

    Image(
        painter = rememberAsyncImagePainter(profilePictureUrl),
        contentDescription = "Profile Picture",
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, color = Color.Blue, shape = CircleShape),
        contentScale = ContentScale.Crop
    )
}