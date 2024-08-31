package com.vaultmessenger.nav

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import com.vaultmessenger.R
import com.vaultmessenger.ui.item.ContactSearchBar
import com.vaultmessenger.ui.item.ContactsAddNewContactsDialog
import com.vaultmessenger.viewModel.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsToolbar(navController: NavHostController) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
           // if (!isSearchExpanded) {
                Text("Select Contact", color = Color.White, fontWeight = FontWeight.Bold)
           // }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
        //    ContactSearchBar(
        //        isSearchExpanded = isSearchExpanded,
        //        onSearchExpandedChange = { expanded -> isSearchExpanded = expanded }
        //    )
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
fun addNewContact(
    contactsViewModel: ContactsViewModel,
    viewModelStoreOwner: ViewModelStoreOwner
): CardDefaults? {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(vertical = 0.dp)
            .wrapContentSize()
            .clickable(onClick = {
              showDialog = true
            })
            .background(color = Color(0xFF006AF7)),

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_account_circle_person_add_foreground),
                contentDescription = "Add new contact",
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(color = Color.White)
            )
            Column(modifier = Modifier.padding(9.dp)) {
                Text(
                    modifier = Modifier.padding(vertical = 18.dp),
                    text = "New Contact",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFFFFFFF)
                )
            }
        }
        ContactsAddNewContactsDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            contactsViewModel = contactsViewModel,
            viewModelStoreOwner = viewModelStoreOwner
            )

    }
    return null
}