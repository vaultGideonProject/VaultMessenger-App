package com.vaultmessenger.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vaultmessenger.ui.theme.VaultmessengerTheme

// this is the structured template of the cards
@Composable
fun MessageItem(
    itemIndex: Int,
    profilePhoto: List<Int>,
    name: List<String>,
    messageContent: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(3.dp)
            .wrapContentSize(),
        colors = CardColors( containerColor = Color(0xFFE6E6E7),
        contentColor = Color.White,
    disabledContainerColor = Color.Gray,
    disabledContentColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Image(
                painter = painterResource(id = profilePhoto[itemIndex]),
                contentDescription = name[itemIndex],
                modifier = Modifier.size(105.dp)
            )
            Column(modifier = Modifier.padding(9.dp)) {
                Text(
                    text = name[itemIndex],
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF264273)
                )
                Text(
                    text = messageContent[itemIndex],
                    fontSize = 14.sp,
                    color = Color(0xFF3B4355)
                )
            }
        };
    }
}

//this cycles through ColumnItem() cards
@Composable
fun MessageListItem(
    imageId: List<Int>,
    names: List<String>,
    messageContent: List<String>,
    modifier: Modifier = Modifier
) {
    VaultmessengerTheme {
        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            items(imageId.size) { itemIndex ->
                MessageItem(
                    itemIndex = itemIndex,
                    profilePhoto = imageId,
                    name = names,
                    messageContent = messageContent,
                    modifier = modifier
                )
            }
        }
    }
}

