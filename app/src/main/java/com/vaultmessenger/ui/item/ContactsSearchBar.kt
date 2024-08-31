package com.vaultmessenger.ui.item

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ContactSearchBar(
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (isSearchExpanded) {
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .background(Color.White, MaterialTheme.shapes.small)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            IconButton(onClick = {
              //  Log.d("Contact Search Item", "Search action with query: ${searchText.text}")
                onSearchExpandedChange(false) // Contract back the search bar
                // Implement the search action here
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        } else {
            IconButton(onClick = {
              //  Log.d("Contact Search Item", "Search selected!")
                onSearchExpandedChange(true) // Expand the search bar
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        }
    }
}
