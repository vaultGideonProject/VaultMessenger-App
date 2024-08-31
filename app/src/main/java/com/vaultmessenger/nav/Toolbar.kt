package com.vaultmessenger.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ProfileViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarWithDrawer(navController: NavHostController, content: @Composable () -> Unit) {
    val repository = FirebaseUserRepository()
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))
    val user by viewModel.user.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(
        //drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, viewModel)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Vault Messenger",
                                color = Color(0xFFFAFAFA),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("Profile") }) {
                            if (user?.profilePictureUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(user!!.profilePictureUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D62CA),
                        scrolledContainerColor = Color.Black,
                        actionIconContentColor = Color.Gray,
                        navigationIconContentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, viewModel: ProfileViewModel) {
    val user by viewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF565E71))
            .padding(16.dp)
    ) {
        if (user?.profilePictureUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user!!.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape),
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user?.userName ?: "Guest", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Drawer Items
        NavigationDrawerItem(
            label = { Text("Profile") },
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
            selected = false,
            onClick = {
                navController.navigate("Profile")
            }
        )
        NavigationDrawerItem(
            label = { Text("Messages") },
            icon = { Icon(Icons.Filled.Email, contentDescription = null) },
            selected = false,
            onClick = {
                navController.navigate("Messages")
            }
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            selected = false,
            onClick = {
                navController.navigate("Settings")
            }
        )
    }
}
