package com.vaultmessenger.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Colors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.vaultmessenger.ui.theme.VaultmessengerTheme
import com.vaultmessenger.ui.theme.errorContainerLight
import com.vaultmessenger.ui.theme.errorLight
import com.vaultmessenger.ui.theme.outlineLight
import com.vaultmessenger.ui.theme.outlineVariantLight
import com.vaultmessenger.ui.theme.primaryContainerLight
import com.vaultmessenger.ui.theme.primaryLight
import com.vaultmessenger.ui.theme.secondaryLight
import com.vaultmessenger.ui.theme.surfaceLight
import com.vaultmessenger.ui.theme.surfaceVariantLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    VaultmessengerTheme {
        OutlinedTextField(
            value = value,
            textStyle = TextStyle(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryLight,
                unfocusedTextColor = secondaryLight,
                disabledTextColor = outlineLight,
                errorTextColor = errorLight,
                focusedContainerColor = primaryContainerLight,
                unfocusedContainerColor = surfaceLight,
                disabledContainerColor = surfaceVariantLight,
                errorContainerColor = errorContainerLight,
                cursorColor = primaryLight,
                errorCursorColor = errorLight,
                selectionColors = TextSelectionColors(primaryLight, secondaryLight),
                focusedBorderColor = primaryLight,
                unfocusedBorderColor = outlineLight,
                disabledBorderColor = outlineVariantLight,
                errorBorderColor = errorLight,
                focusedLeadingIconColor = primaryLight,
                unfocusedLeadingIconColor = secondaryLight,
                disabledLeadingIconColor = outlineLight,
                errorLeadingIconColor = errorLight,
                focusedTrailingIconColor = primaryLight,
                unfocusedTrailingIconColor = secondaryLight,
                disabledTrailingIconColor = outlineLight,
                errorTrailingIconColor = errorLight,
                focusedLabelColor = primaryLight,
                unfocusedLabelColor = secondaryLight,
                disabledLabelColor = outlineLight,
                errorLabelColor = errorLight,
                focusedPlaceholderColor = primaryContainerLight,
                unfocusedPlaceholderColor = surfaceVariantLight,
                disabledPlaceholderColor = surfaceVariantLight,
                errorPlaceholderColor = errorContainerLight,
                focusedSupportingTextColor = primaryLight,
                unfocusedSupportingTextColor = secondaryLight,
                disabledSupportingTextColor = outlineLight,
                errorSupportingTextColor = errorLight,
                focusedPrefixColor = primaryLight,
                unfocusedPrefixColor = secondaryLight,
                disabledPrefixColor = outlineLight,
                errorPrefixColor = errorLight,
                focusedSuffixColor = primaryLight,
                unfocusedSuffixColor = secondaryLight,
                disabledSuffixColor = outlineLight,
                errorSuffixColor = errorLight
            )
            ,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)

        )
    }
}