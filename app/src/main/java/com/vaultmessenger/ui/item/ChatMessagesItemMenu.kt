package com.vaultmessenger.ui.item

import androidx.compose.runtime.MutableState

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChatMessagesItemMenu(expanded: MutableState<Boolean>) {
    val isDropDownExpanded = remember { expanded }
    val itemPosition = remember { mutableStateOf(-1) } // Remember state for the selected position
    val usernames = listOf("reply", "forward", "delete") // Example usernames list

    DropdownMenu(
        expanded = isDropDownExpanded.value,
        onDismissRequest = {
            isDropDownExpanded.value = false
        }
    ) {
        usernames.forEachIndexed { index, username ->
            DropdownMenuItem(
                text = {
                    Text(text = username)
                },
                onClick = {
                    isDropDownExpanded.value = false
                    itemPosition.value = index // Update selected position
                }
            )
        }
    }
}
