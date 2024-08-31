package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vaultmessenger.nav.rememberVideocam
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.modules.FirebaseService

@Composable
fun ContactsItem(
    navController: NavController,
    itemIndex: Int,
    contactProfilePhoto: List<Int>,
    contactName: List<String>,
    contactNumber: List<String>,
    contactUID: List<String>,
    modifier: Modifier = Modifier
) {
    // Access FirebaseAuth
    val auth = FirebaseService.auth

// Access Firestore
    val firestore = FirebaseService.firestore

// Access FirebaseStorage
    val storage = FirebaseService.storage

    val senderUID = auth.currentUser?.uid
    val receiverUID = contactUID[itemIndex]
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
            Image(
                painter = painterResource(id = contactProfilePhoto[itemIndex]),
                contentDescription = contactName[itemIndex],
                modifier = Modifier.size(85.dp),
            )
            Column(modifier = Modifier.padding(9.dp)) {
                Text(
                    text = contactName[itemIndex],
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
    contactProfilePhoto: List<Int>,
    contactName: List<String>,
    contactNumber: List<String>,
    contactUID: List<String>,
    modifier: Modifier = Modifier
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
                    contactUID = contactUID,
                    modifier = modifier
                )
            }
        }
    }
}
