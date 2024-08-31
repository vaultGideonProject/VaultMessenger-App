package com.vaultmessenger.ui.item

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VoiceNoteUI(
    isPlaying: Boolean,
    progress: Float, // Progress of the SeekBar (0f to 1f)
    duration: String, // Format "mm:ss"
    onPlayPauseClick: () -> Unit // Callback when the play/pause button is clicked
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onPlayPauseClick() }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }

        Slider(
            value = progress,
            onValueChange = {},
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.Gray,
                activeTrackColor = Color.Green,
                inactiveTrackColor = Color.LightGray
            )
        )

        Text(
            text = duration,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
