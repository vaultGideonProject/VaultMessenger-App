package com.vaultmessenger.ui.item

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.R
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.AndroidAudioRecorder
import com.vaultmessenger.viewModel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PulsingMicIcon(
    chatViewModel: ChatViewModel,
    senderUID: String,
    receiverUID: String
) {
    var isPulsing by remember { mutableStateOf(false) }

    // Animation for pulsing effect
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.2f else 1f, // Scale up to 1.2x size when pulsing
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    // Launcher to request the RECORD_AUDIO permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, proceed with recording audio
        } else {
            // Permission denied, handle accordingly
        }
    }
    val context = LocalContext.current

    // Check if permission is already granted
    val isGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    val recorder by lazy {
        AndroidAudioRecorder(context = context)
    }

    var audioFile: File? = null

    Icon(
        painter = painterResource(id = R.drawable.baseline_mic_24),
        contentDescription = "Mic",
        tint = Color.White,
        modifier = Modifier
            .scale(scale) // Apply the scale to create the pulsing effect
            .combinedClickable(
                onClick = {
                    // Handle single click
                },
                onLongClick = {
                    isPulsing = true // Start pulsing when recording starts

                    if (!isGranted) {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        // Permission is already granted, proceed with recording audio
                    }

                    // Debug log to ensure isPulsing is true and scale is updating
                    Log.d("PulsingMicIcon", "isPulsing set to true, scale: $scale")

                    chatViewModel.viewModelScope.launch {
                        Log.d("record", "is recording")
                        val voiceMessage = Message(
                            imageUrl = null,
                            voiceNoteURL = "test",
                            messageText = "",
                            name = "",
                            photoUrl = "",
                            timestamp = System.currentTimeMillis().toString(),
                            userId1 = senderUID,
                            userId2 = receiverUID,
                        )
                        chatViewModel.sendVoiceMessage(
                            senderUid = senderUID,
                            receiverUid = receiverUID,
                            message = voiceMessage
                        )
                        isPulsing = false // Stop pulsing when recording ends
                        Log.d("PulsingMicIcon", "isPulsing set to false")
                    }
                }
            )
    )
}

