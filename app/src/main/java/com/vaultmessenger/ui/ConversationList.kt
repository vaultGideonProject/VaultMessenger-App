package com.vaultmessenger.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vaultmessenger.DrawerContent
import com.vaultmessenger.ProfileIcon
import com.vaultmessenger.modules.FirebaseService
import com.vaultmessenger.ui.item.ConversationItem
import com.vaultmessenger.viewModel.ConnectivityViewModel
import com.vaultmessenger.viewModel.ConversationViewModel
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ProfileViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationList(
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    conversationViewModel: ConversationViewModel,
    connectivityViewModel: ConnectivityViewModel,
    errorsViewModel: ErrorsViewModel,
) {
    val auth = FirebaseService.auth
    val userId = auth.currentUser?.uid ?: return
    val user by profileViewModel.user.collectAsState()
    val conversations by conversationViewModel.conversations.collectAsState(emptyList())
    val isConnected by connectivityViewModel.isConnected.observeAsState(true)
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by errorsViewModel.errorMessage.observeAsState(initial = "")
    val currentErrorMessage by rememberUpdatedState(errorMessage)

    // val snackbarHostState = remember { SnackbarHostState() }
    // val errorMessage by profileViewModel.errorMessage.collectAsState()
    // val currentErrorMessage by rememberUpdatedState(errorMessage)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Side effect for updating online status and loading conversations
    LaunchedEffect(userId) {
        profileViewModel.updateOnlineStatus(userId, "online")
    }

    // Use SideEffect to respond to state changes
    SideEffect {
        currentErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            errorsViewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(user, navController)
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if(isConnected){"Vault Messenger"}else{"NO INTERNET"},
                            color = Color(0xFFFAFAFA),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                modifier = Modifier.size(35.dp),
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("Profile") }) {
                            ProfileIcon(user)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if(isConnected){Color(0xFF0D62CA)}else{
                            Color(0xFFDA342E)
                        }
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("contacts") },
                    containerColor = Color(0xFF264273)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Create,
                        contentDescription = "New Message",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
        ) { contentPadding ->
            LazyColumn(modifier = Modifier.padding(contentPadding)) {
                //randomise the key to fix exception of criss crossing convos from user and improve performance
                items(conversations, key = { it.timestamp + conversations.size + 1}) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        navController = navController,
                        userId = userId,
                        errorsViewModel = errorsViewModel
                    )
                }
            }
        }
    }
}