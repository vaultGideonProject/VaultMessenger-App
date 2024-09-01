package com.vaultmessenger.ui.item

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.R
import com.vaultmessenger.model.Conversation
import com.vaultmessenger.model.Message
import com.vaultmessenger.modules.VoiceRecorder
import com.vaultmessenger.viewModel.ChatViewModel
import com.vaultmessenger.viewModel.ConversationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PulsingMicIcon(
    chatViewModel: ChatViewModel,
    senderUID: String,
    receiverUID: String,
    onSuccess: (Boolean) -> Unit,
    conversationViewModel: ConversationViewModel,
    viewModelStoreOwner: ViewModelStoreOwner?,
    userName:String,
    profilePhoto:String,
) {
    var isRecording by remember { mutableStateOf(false) }
    var sendNotification by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    // Animation for pulsing effect
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f, // Scale up to 1.2x size when recording
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    // Launcher to request the RECORD_AUDIO permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, proceed with recording audio
            if (!isRecording) {}
        } else {
            // Permission denied, handle accordingly
            Log.d("PulsingMicIcon", "Permission denied")
        }
    }

    // Check if permission is already granted
    val isGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    val recorder by remember {
        mutableStateOf(VoiceRecorder(context = context))
    }

    fun startRecording() {
        if (isGranted) {
            isRecording = true
            Log.d("PulsingMicIcon", "Recording started")

            chatViewModel.viewModelScope.launch {
                chatViewModel.startVoiceRecording(context = context) // Ensure that context is not needed here
            }
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        if (isRecording) {
            val voiceMessage = Message(
                messageText = "sent you a Recording",
                name = userName,
                photoUrl = profilePhoto,
                voiceNoteURL = null,
                timestamp = System.currentTimeMillis().toString(),
                userId1 = senderUID,
                userId2 = receiverUID,
                conversationId = generateConversationId(),
                imageUrl = null,

            )
            chatViewModel.viewModelScope.launch {
                chatViewModel.stopVoiceRecording(
                    senderUid = senderUID, // Make sure these variables are initialized and available
                    receiverUid = receiverUID
                )
                conversationViewModel.viewModelScope.launch {
                    conversationViewModel.setConversationBySenderId(
                        senderId = senderUID,
                        receiverId = receiverUID,
                        viewModelStoreOwner = viewModelStoreOwner,
                        message = voiceMessage)
                }
            }

            isRecording = false
            Log.d("PulsingMicIcon", "Recording stopped")
        }
    }

    Icon(
        painter = painterResource(id = R.drawable.baseline_mic_24),
        contentDescription = if (isRecording) "Stop recording" else "Start recording",
        tint = if (isRecording) Color.Red else Color.White,
        modifier = Modifier
            .scale(scale) // Apply the scale to create the pulsing effect
            .combinedClickable(
                onClick = {
                    if (isRecording) {
                        stopRecording()
                        sendNotification = true

                    } else {
                        startRecording()
                        sendNotification = false
                    }
                    onSuccess(sendNotification)
                },
                onLongClick = {
                    // Handle long click if needed
                }
            )
    )
}
