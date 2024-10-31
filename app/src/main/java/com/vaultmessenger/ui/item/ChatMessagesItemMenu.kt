package com.vaultmessenger.ui.item

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

@Composable
fun ChatMessagesItemMenu(expanded: MutableState<Boolean>, actionSelected: (String) -> Unit) {
    val itemPosition = remember { mutableIntStateOf(-1) }
    val menuActionList = listOf("reply", "forward", "delete")

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = {
            expanded.value = false
        }
    ) {
        menuActionList.forEachIndexed { index, menuActionItem ->
            DropdownMenuItem(
                text = {
                    Text(text = menuActionItem)
                },
                onClick = {
                    expanded.value = false
                    itemPosition.intValue = index
                    actionSelected(menuActionItem) // Call the actionSelected callback with the item
                }
            )
        }
    }
}
