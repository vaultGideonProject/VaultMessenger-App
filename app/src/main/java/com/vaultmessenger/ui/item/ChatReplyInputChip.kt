package com.vaultmessenger.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vaultmessenger.R

@Composable
fun ChatReplyInputChip(
    messageText: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enabled by remember { mutableStateOf(true) }
    if (!enabled) return

    InputChip(
        avatar = {
            Icon(
                Icons.Filled.Commit,
                contentDescription = "Localized description",
                Modifier.size(InputChipDefaults.AvatarSize)
            )
        },
        onClick = { /* handle chip click if needed */ },
        label = {
            Text(
                text = messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                minLines = 1,
                maxLines = 6,
            )
        },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        enabled = false // Optional: Disable on close click
                        onClose()
                    }
            )
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(4.dp),
        selected = enabled
    )
}

