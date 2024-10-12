package com.vaultmessenger.ui.item

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultmessenger.modules.ContactRepository
import com.vaultmessenger.modules.ReceiverUserRepository
import com.vaultmessenger.viewModel.ContactsViewModel
import com.vaultmessenger.viewModel.ContactsViewModelFactory
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun ContactsAddNewContactsDialog(
    showDialog: Boolean, onDismiss: () -> Unit,
    contactsViewModel: ContactsViewModel,
    viewModelStoreOwner: ViewModelStoreOwner
    ) {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }

    if (showDialog) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF232F34)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(color = Color.White, text = "Please Enter User's hash", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.LightGray, MaterialTheme.shapes.small)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onDismiss() }) {
                            Text("Cancel", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            contactsViewModel.viewModelScope.launch {
                                if(inputText.text.isEmpty()){
                                    contactsViewModel.setError("No User Selected")
                                    return@launch
                                }
                                contactsViewModel.addUser(
                                    receiverId = inputText.text,
                                    viewModelStoreOwner = viewModelStoreOwner
                                )
                            }

                      //      Log.d("Search Query", searchText.text)
                            onDismiss()
                            // Implement the search action here
                        }) {
                            Text("Find", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
